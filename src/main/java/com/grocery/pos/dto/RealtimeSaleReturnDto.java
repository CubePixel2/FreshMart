package com.grocery.pos.dto;

import com.grocery.pos.model.SaleReturn;
import com.grocery.pos.model.SaleReturnItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RealtimeSaleReturnDto {

    private Long id;
    private String returnNumber;
    private String returnDate;
    private Long originalSaleId;
    private String originalInvoiceNumber;
    private String customerName;
    private String customerPhone;
    private int totalReturnedQty;
    private BigDecimal refundAmount;
    private String reason;
    private String processedBy;
    private List<ItemDto> items = new ArrayList<>();

    public RealtimeSaleReturnDto() {
    }

    public static RealtimeSaleReturnDto fromEntity(SaleReturn entity) {
        RealtimeSaleReturnDto dto = new RealtimeSaleReturnDto();
        dto.setId(entity.getId());
        dto.setReturnNumber(entity.getReturnNumber());
        if (entity.getReturnDate() != null) {
            dto.setReturnDate(entity.getReturnDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        }
        if (entity.getOriginalSale() != null) {
            dto.setOriginalSaleId(entity.getOriginalSale().getId());
            dto.setOriginalInvoiceNumber(entity.getOriginalSale().getInvoiceNumber());
            dto.setCustomerName(entity.getOriginalSale().getCustomerName());
            dto.setCustomerPhone(entity.getOriginalSale().getCustomerPhone());
        }
        dto.setTotalReturnedQty(entity.getTotalReturnedQty());
        dto.setRefundAmount(entity.getRefundAmount() != null ? entity.getRefundAmount() : BigDecimal.ZERO);
        dto.setReason(entity.getReason());
        dto.setProcessedBy(entity.getProcessedBy() != null ? entity.getProcessedBy() : "Cashier");

        if (entity.getItems() != null) {
            for (SaleReturnItem item : entity.getItems()) {
                ItemDto idto = new ItemDto();
                idto.setProductName(item.getProduct() != null ? item.getProduct().getName() : "Unknown");
                idto.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
                idto.setRefundRate(item.getRefundRate() != null ? item.getRefundRate() : BigDecimal.ZERO);
                idto.setSubtotal(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
                dto.getItems().add(idto);
            }
        }
        return dto;
    }

    public static class ItemDto {
        private String productName;
        private int quantity;
        private BigDecimal refundRate;
        private BigDecimal subtotal;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getRefundRate() { return refundRate; }
        public void setRefundRate(BigDecimal refundRate) { this.refundRate = refundRate; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public Long getOriginalSaleId() { return originalSaleId; }
    public void setOriginalSaleId(Long originalSaleId) { this.originalSaleId = originalSaleId; }
    public String getOriginalInvoiceNumber() { return originalInvoiceNumber; }
    public void setOriginalInvoiceNumber(String originalInvoiceNumber) { this.originalInvoiceNumber = originalInvoiceNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public int getTotalReturnedQty() { return totalReturnedQty; }
    public void setTotalReturnedQty(int totalReturnedQty) { this.totalReturnedQty = totalReturnedQty; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }
}
