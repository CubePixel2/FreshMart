package com.grocery.pos.service;

import com.grocery.pos.dto.SaleReturnFormDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.SaleRepository;
import com.grocery.pos.repository.SaleReturnRepository;
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
public class SaleReturnService {

    private final SaleReturnRepository saleReturnRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final RealtimeSyncService realtimeSyncService;

    public SaleReturnService(SaleReturnRepository saleReturnRepository,
                             SaleRepository saleRepository,
                             ProductRepository productRepository,
                             RealtimeSyncService realtimeSyncService) {
        this.saleReturnRepository = saleReturnRepository;
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.realtimeSyncService = realtimeSyncService;
    }

    public List<SaleReturn> findAll() {
        return saleReturnRepository.findAllByOrderByReturnDateDesc();
    }

    public Optional<SaleReturn> findById(Long id) {
        return saleReturnRepository.findById(id);
    }

    public Optional<SaleReturn> findByReturnNumber(String returnNumber) {
        return saleReturnRepository.findByReturnNumber(returnNumber);
    }

    /**
     * Processes customer sale return and restores product inventory in @Transactional context
     */
    @Transactional
    public SaleReturn processSaleReturn(SaleReturnFormDto form, String processedBy) {
        Sale sale = saleRepository.findById(form.getSaleId())
                .orElseThrow(() -> new IllegalArgumentException("Original Sale Invoice not found: ID " + form.getSaleId()));

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setReturnNumber(generateReturnNumber());
        saleReturn.setOriginalSale(sale);
        saleReturn.setReturnDate(LocalDateTime.now());
        saleReturn.setReason(form.getReason() != null ? form.getReason().trim() : "Customer Return");
        saleReturn.setProcessedBy(processedBy != null ? processedBy : "Cashier");

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

                // Find original sale item to verify return rate
                BigDecimal refundRate = product.getSellingPrice();
                for (SaleItem si : sale.getItems()) {
                    if (si.getProduct() != null && si.getProduct().getId().equals(productId)) {
                        refundRate = si.getUnitPrice();
                        break;
                    }
                }

                SaleReturnItem item = new SaleReturnItem(product, qty, refundRate);
                saleReturn.addItem(item);
                totalRefund = totalRefund.add(item.getSubtotal());

                // RESTORE STOCK INVENTORY
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock + qty);
                productRepository.save(product);
            }
        }

        saleReturn.setRefundAmount(totalRefund);
        SaleReturn saved = saleReturnRepository.save(saleReturn);
        realtimeSyncService.notifySaleReturn(saved);
        return saved;
    }

    private synchronized String generateReturnNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = saleReturnRepository.count() + 1;
        return String.format("SR-%s-%04d", dateStr, count);
    }
}
