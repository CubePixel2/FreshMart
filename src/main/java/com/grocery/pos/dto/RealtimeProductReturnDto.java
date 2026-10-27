package com.grocery.pos.dto;

import com.grocery.pos.model.ProductReturn;
import com.grocery.pos.model.ProductReturnItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RealtimeProductReturnDto {

    private Long id;
    private String returnNumber;
    private String returnDate;
    private Long supplierId;
    private String supplierName;
    private String contactPerson;
    private int totalReturnedQty;
    private BigDecimal totalRefundAmount;
    private String reason;
    private String processedBy;
    private List<ItemDto> items = new ArrayList<>();

    public RealtimeProductReturnDto() {
    }

    public static RealtimeProductReturnDto fromEntity(ProductReturn entity) {
        RealtimeProductReturnDto dto = new RealtimeProductReturnDto();
        dto.setId(entity.getId());
        dto.setReturnNumber(entity.getReturnNumber());
        if (entity.getReturnDate() != null) {
            dto.setReturnDate(entity.getReturnDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        }
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
            dto.setContactPerson(entity.getSupplier().getContactPerson());
        }
        dto.setTotalReturnedQty(entity.getTotalReturnedQty());
        dto.setTotalRefundAmount(entity.getTotalRefundAmount() != null ? entity.getTotalRefundAmount() : BigDecimal.ZERO);
        dto.setReason(entity.getReason());
        dto.setProcessedBy(entity.getProcessedBy() != null ? entity.getProcessedBy() : "Administrator");

        if (entity.getItems() != null) {
            for (ProductReturnItem item : entity.getItems()) {
                ItemDto idto = new ItemDto();
                idto.setProductName(item.getProduct() != null ? item.getProduct().getName() : "Unknown");
                idto.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
                idto.setUnitCost(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
                idto.setSubtotal(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
                dto.getItems().add(idto);
            }
        }
        return dto;
    }

    public static class ItemDto {
        private String productName;
        private int quantity;
        private BigDecimal unitCost;
        private BigDecimal subtotal;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
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
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public int getTotalReturnedQty() { return totalReturnedQty; }
    public void setTotalReturnedQty(int totalReturnedQty) { this.totalReturnedQty = totalReturnedQty; }
    public BigDecimal getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(BigDecimal totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }
}
