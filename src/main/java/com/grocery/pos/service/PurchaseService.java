package com.grocery.pos.service;

import com.grocery.pos.dto.PurchaseInvoiceFormDto;
import com.grocery.pos.dto.PurchaseOrderFormDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.PurchaseInvoiceRepository;
import com.grocery.pos.repository.PurchaseOrderRepository;
import com.grocery.pos.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final RealtimeSyncService realtimeSyncService;

    public PurchaseService(PurchaseOrderRepository purchaseOrderRepository,
                           PurchaseInvoiceRepository purchaseInvoiceRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           RealtimeSyncService realtimeSyncService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.realtimeSyncService = realtimeSyncService;
    }

    // =========================================================================
    // PURCHASE ORDERS
    // =========================================================================

    public List<PurchaseOrder> findAllOrders() {
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc();
    }

    public Optional<PurchaseOrder> findOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public Optional<PurchaseOrder> findOrderByPoNumber(String poNumber) {
        return purchaseOrderRepository.findByPoNumber(poNumber);
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderFormDto form, String createdBy) {
        Supplier supplier = supplierRepository.findById(form.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + form.getSupplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setSupplier(supplier);
        po.setOrderDate(form.getOrderDate() != null ? form.getOrderDate() : LocalDate.now());
        po.setExpectedDate(form.getExpectedDate());
        po.setStatus(form.getStatus() != null ? form.getStatus() : PurchaseOrderStatus.ORDERED);
        po.setNotes(form.getNotes());
        po.setCreatedBy(createdBy != null ? createdBy : "Administrator");
        po.setCreatedAt(LocalDateTime.now());

        if (form.getProductIds() != null) {
            for (int i = 0; i < form.getProductIds().size(); i++) {
                Long pId = form.getProductIds().get(i);
                if (pId == null) continue;

                Product product = productRepository.findById(pId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + pId));

                int qty = (form.getQuantities() != null && form.getQuantities().size() > i && form.getQuantities().get(i) != null)
                        ? form.getQuantities().get(i) : 1;
                BigDecimal unitCost = (form.getUnitCosts() != null && form.getUnitCosts().size() > i && form.getUnitCosts().get(i) != null)
                        ? form.getUnitCosts().get(i) : product.getCostPrice();

                PurchaseOrderItem item = new PurchaseOrderItem(product, qty, unitCost);
                po.addItem(item);
            }
        }

        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder updateOrderStatus(Long orderId, PurchaseOrderStatus newStatus) {
        PurchaseOrder po = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found: " + orderId));
        po.setStatus(newStatus);
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        purchaseOrderRepository.deleteById(orderId);
    }

    // =========================================================================
    // PURCHASE INVOICES
    // =========================================================================

    public List<PurchaseInvoice> findAllInvoices() {
        return purchaseInvoiceRepository.findAllByOrderByInvoiceDateDesc();
    }

    public Optional<PurchaseInvoice> findInvoiceById(Long id) {
        return purchaseInvoiceRepository.findById(id);
    }

    @Transactional
    public PurchaseInvoice createInvoice(PurchaseInvoiceFormDto form, String createdBy) {
        Supplier supplier = supplierRepository.findById(form.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + form.getSupplierId()));

        PurchaseInvoice invoice = new PurchaseInvoice();
        String invNo = form.getInvoiceNumber();
        if (invNo == null || invNo.isBlank()) {
            invNo = generateInvoiceNumber();
        }
        invoice.setInvoiceNumber(invNo.trim());
        invoice.setSupplier(supplier);
        invoice.setInvoiceDate(form.getInvoiceDate() != null ? form.getInvoiceDate() : LocalDate.now());
        invoice.setPaymentStatus(form.getPaymentStatus() != null ? form.getPaymentStatus() : PurchasePaymentStatus.UNPAID);
        invoice.setNotes(form.getNotes());
        invoice.setCreatedBy(createdBy != null ? createdBy : "Administrator");

        if (form.getPurchaseOrderId() != null) {
            purchaseOrderRepository.findById(form.getPurchaseOrderId()).ifPresent(invoice::setPurchaseOrder);
        }

        if (form.getProductIds() != null) {
            for (int i = 0; i < form.getProductIds().size(); i++) {
                Long pId = form.getProductIds().get(i);
                if (pId == null) continue;

                Product product = productRepository.findById(pId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + pId));

                int qty = (form.getQuantities() != null && form.getQuantities().size() > i && form.getQuantities().get(i) != null)
                        ? form.getQuantities().get(i) : 1;
                BigDecimal unitCost = (form.getUnitCosts() != null && form.getUnitCosts().size() > i && form.getUnitCosts().get(i) != null)
                        ? form.getUnitCosts().get(i) : product.getCostPrice();
                BigDecimal tax = (form.getTaxAmounts() != null && form.getTaxAmounts().size() > i && form.getTaxAmounts().get(i) != null)
                        ? form.getTaxAmounts().get(i) : BigDecimal.ZERO;

                PurchaseInvoiceItem item = new PurchaseInvoiceItem(product, qty, unitCost, tax);
                invoice.addItem(item);
            }
        }

        invoice.recalculateTotals();
        PurchaseInvoice savedInvoice = purchaseInvoiceRepository.save(invoice);

        // If postImmediately is checked, increase stock right away
        if (form.isPostImmediately()) {
            postInvoice(savedInvoice.getId());
        }

        return savedInvoice;
    }

    @Transactional
    public PurchaseInvoice createInvoiceFromOrder(Long orderId, String createdBy) {
        PurchaseOrder po = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found: " + orderId));

        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setPurchaseOrder(po);
        invoice.setSupplier(po.getSupplier());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setPaymentStatus(PurchasePaymentStatus.UNPAID);
        invoice.setCreatedBy(createdBy != null ? createdBy : "Administrator");
        invoice.setNotes("Created from PO #" + po.getPoNumber());

        for (PurchaseOrderItem poItem : po.getItems()) {
            BigDecimal tax = poItem.getSubtotal().multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            PurchaseInvoiceItem item = new PurchaseInvoiceItem(poItem.getProduct(), poItem.getOrderedQty(), poItem.getUnitCost(), tax);
            invoice.addItem(item);
        }

        invoice.recalculateTotals();
        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
        return postInvoice(saved.getId());
    }

    /**
     * Confirms and posts a Purchase Invoice to inventory:
     * Increases product stock quantities with @Transactional safety.
     */
    @Transactional
    public PurchaseInvoice postInvoice(Long invoiceId) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Invoice not found: " + invoiceId));

        if (invoice.isPosted()) {
            return invoice; // Already posted, skip
        }

        // Increase product stocks
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentStock + item.getQuantity());

            // Update cost price to latest received unit cost if specified
            if (item.getUnitCost() != null && item.getUnitCost().compareTo(BigDecimal.ZERO) > 0) {
                product.setCostPrice(item.getUnitCost());
            }

            productRepository.save(product);
        }

        invoice.setPosted(true);
        invoice.setPostedAt(LocalDateTime.now());

        // Update linked Purchase Order status if applicable
        if (invoice.getPurchaseOrder() != null) {
            PurchaseOrder po = invoice.getPurchaseOrder();
            po.setStatus(PurchaseOrderStatus.RECEIVED);
            purchaseOrderRepository.save(po);
        }

        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
        realtimeSyncService.notifyPurchaseInvoice(saved);
        return saved;
    }

    @Transactional
    public PurchaseInvoice updatePaymentStatus(Long invoiceId, PurchasePaymentStatus status) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Invoice not found: " + invoiceId));
        invoice.setPaymentStatus(status);
        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
        realtimeSyncService.notifyPurchaseInvoice(saved);
        return saved;
    }

    @Transactional
    public void deleteInvoice(Long invoiceId) {
        purchaseInvoiceRepository.deleteById(invoiceId);
    }

    private synchronized String generatePoNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderRepository.count() + 1;
        return String.format("PO-%s-%04d", dateStr, count);
    }

    private synchronized String generateInvoiceNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseInvoiceRepository.count() + 1;
        return String.format("PINV-%s-%04d", dateStr, count);
    }
}
