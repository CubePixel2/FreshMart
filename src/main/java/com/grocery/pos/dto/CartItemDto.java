package com.grocery.pos.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long productId;
    private String barcode;
    private String name;
    private BigDecimal price;
    private int quantity;

    public CartItemDto() {
    }

    public CartItemDto(Long productId, String barcode, String name, BigDecimal price, int quantity) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
