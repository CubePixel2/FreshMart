package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductReturnFormDto {

    private Long supplierId;
    private String reason;
    private List<Long> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private List<BigDecimal> unitCosts = new ArrayList<>();

    public ProductReturnFormDto() {
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
