package com.grocery.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_returns")
public class ProductReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDateTime returnDate = LocalDateTime.now();

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    @Column(length = 100)
    private String processedBy;

    @OneToMany(mappedBy = "productReturn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductReturnItem> items = new ArrayList<>();

    public ProductReturn() {
    }

    public void addItem(ProductReturnItem item) {
        items.add(item);
        item.setProductReturn(this);
    }

    public void removeItem(ProductReturnItem item) {
        items.remove(item);
        item.setProductReturn(null);
    }

    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        if (items != null) {
            for (ProductReturnItem item : items) {
                if (item.getSubtotal() != null) {
                    sum = sum.add(item.getSubtotal());
                }
            }
        }
        this.totalRefundAmount = sum;
    }

    public int getTotalReturnedQty() {
        if (items == null) return 0;
        int qty = 0;
        for (ProductReturnItem item : items) {
            qty += (item.getQuantity() != null ? item.getQuantity() : 0);
        }
        return qty;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(BigDecimal totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public List<ProductReturnItem> getItems() {
        return items;
    }

    public void setItems(List<ProductReturnItem> items) {
        this.items = items;
    }
}
