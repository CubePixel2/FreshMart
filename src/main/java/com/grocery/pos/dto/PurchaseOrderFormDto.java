package com.grocery.pos.dto;

import com.grocery.pos.model.PurchaseOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderFormDto {

    private Long supplierId;
    private LocalDate orderDate = LocalDate.now();
    private LocalDate expectedDate;
    private PurchaseOrderStatus status = PurchaseOrderStatus.ORDERED;
    private String notes;

    private List<Long> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private List<BigDecimal> unitCosts = new ArrayList<>();

    public PurchaseOrderFormDto() {
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public List<BigDecimal> getUnitCosts() {
        return unitCosts;
    }

    public void setUnitCosts(List<BigDecimal> unitCosts) {
        this.unitCosts = unitCosts;
    }
}
