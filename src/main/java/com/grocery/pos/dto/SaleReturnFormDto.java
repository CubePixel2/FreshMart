package com.grocery.pos.dto;

import java.util.ArrayList;
import java.util.List;

public class SaleReturnFormDto {

    private Long saleId;
    private String reason;
    private List<Long> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();

    public SaleReturnFormDto() {
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
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
}
