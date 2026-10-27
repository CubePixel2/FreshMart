package com.grocery.pos.service;

import com.grocery.pos.dto.ProductReturnFormDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.ProductReturnRepository;
import com.grocery.pos.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductReturnService {

    private final ProductReturnRepository productReturnRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final RealtimeSyncService realtimeSyncService;

    public ProductReturnService(ProductReturnRepository productReturnRepository,
                                SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                RealtimeSyncService realtimeSyncService) {
        this.productReturnRepository = productReturnRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.realtimeSyncService = realtimeSyncService;
    }

    public List<ProductReturn> findAll() {
        return productReturnRepository.findAllByOrderByReturnDateDesc();
    }

    public Optional<ProductReturn> findById(Long id) {
        return productReturnRepository.findById(id);
    }

    public Optional<ProductReturn> findByReturnNumber(String returnNumber) {
        return productReturnRepository.findByReturnNumber(returnNumber);
    }

    /**
     * Processes stock return to supplier (damage/expiry/defect) and decreases product inventory in @Transactional context
     */
    @Transactional
    public ProductReturn processProductReturn(ProductReturnFormDto form, String processedBy) {
        Supplier supplier = supplierRepository.findById(form.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + form.getSupplierId()));

        ProductReturn pr = new ProductReturn();
        pr.setReturnNumber(generateReturnNumber());
        pr.setSupplier(supplier);
        pr.setReturnDate(LocalDateTime.now());
        pr.setReason(form.getReason() != null ? form.getReason().trim() : "Damaged / Expired stock return");
        pr.setProcessedBy(processedBy != null ? processedBy : "Administrator");

        BigDecimal totalRefund = BigDecimal.ZERO;

        if (form.getProductIds() != null) {
            for (int i = 0; i < form.getProductIds().size(); i++) {
                Long productId = form.getProductIds().get(i);
                if (productId == null) continue;

                int qty = (form.getQuantities() != null && form.getQuantities().size() > i && form.getQuantities().get(i) != null)
                        ? form.getQuantities().get(i) : 0;
                if (qty <= 0) continue;

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                if (currentStock < qty) {
                    throw new IllegalStateException("Insufficient stock for " + product.getName() + 
                            ". Available: " + currentStock + ", requested return: " + qty);
                }

                BigDecimal unitCost = (form.getUnitCosts() != null && form.getUnitCosts().size() > i && form.getUnitCosts().get(i) != null)
                        ? form.getUnitCosts().get(i) : product.getCostPrice();

                ProductReturnItem item = new ProductReturnItem(product, qty, unitCost);
                pr.addItem(item);
                totalRefund = totalRefund.add(item.getSubtotal());

                // DECREASE STOCK INVENTORY
                product.setStockQuantity(currentStock - qty);
                productRepository.save(product);
            }
        }

        pr.setTotalRefundAmount(totalRefund);
        ProductReturn saved = productReturnRepository.save(pr);
        realtimeSyncService.notifyProductReturn(saved);
        return saved;
    }

    private synchronized String generateReturnNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = productReturnRepository.count() + 1;
        return String.format("PR-%s-%04d", dateStr, count);
    }
}
