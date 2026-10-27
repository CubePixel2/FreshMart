package com.grocery.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_return_items")
public class ProductReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_return_id", nullable = false)
    private ProductReturn productReturn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    public ProductReturnItem() {
    }

    public ProductReturnItem(Product product, Integer quantity, BigDecimal unitCost) {
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost;
        recalculateSubtotal();
    }

    public void recalculateSubtotal() {
        if (unitCost != null && quantity != null) {
            this.subtotal = unitCost.multiply(BigDecimal.valueOf(quantity));
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

    public ProductReturn getProductReturn() {
        return productReturn;
    }

    public void setProductReturn(ProductReturn productReturn) {
        this.productReturn = productReturn;
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

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
