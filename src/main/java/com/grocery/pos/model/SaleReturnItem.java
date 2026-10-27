package com.grocery.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_return_items")
public class SaleReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id", nullable = false)
    private SaleReturn saleReturn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    public SaleReturnItem() {
    }

    public SaleReturnItem(Product product, Integer quantity, BigDecimal refundRate) {
        this.product = product;
        this.quantity = quantity;
        this.refundRate = refundRate;
        recalculateSubtotal();
    }

    public void recalculateSubtotal() {
        if (refundRate != null && quantity != null) {
            this.subtotal = refundRate.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SaleReturn getSaleReturn() {
        return saleReturn;
    }

    public void setSaleReturn(SaleReturn saleReturn) {
        this.saleReturn = saleReturn;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalculateSubtotal();
    }

    public BigDecimal getRefundRate() {
        return refundRate;
    }

    public void setRefundRate(BigDecimal refundRate) {
        this.refundRate = refundRate;
        recalculateSubtotal();
    }

    public BigDecimal getRefundPrice() {
        return refundRate;
    }

    public void setRefundPrice(BigDecimal refundPrice) {
        this.refundRate = refundPrice;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
