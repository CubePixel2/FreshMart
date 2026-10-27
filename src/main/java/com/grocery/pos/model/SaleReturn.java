package com.grocery.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_returns")
public class SaleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale originalSale;

    @Column(nullable = false)
    private LocalDateTime returnDate = LocalDateTime.now();

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(length = 100)
    private String processedBy;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleReturnItem> items = new ArrayList<>();

    public SaleReturn() {
    }

    public void addItem(SaleReturnItem item) {
        items.add(item);
        item.setSaleReturn(this);
    }

    public void removeItem(SaleReturnItem item) {
        items.remove(item);
        item.setSaleReturn(null);
    }

    public void recalculateRefundTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        if (items != null) {
            for (SaleReturnItem item : items) {
                if (item.getSubtotal() != null) {
                    sum = sum.add(item.getSubtotal());
                }
            }
        }
        this.refundAmount = sum;
    }

    public int getTotalReturnedQty() {
        if (items == null) return 0;
        int qty = 0;
        for (SaleReturnItem item : items) {
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

    public Sale getOriginalSale() {
        return originalSale;
    }

    public void setOriginalSale(Sale originalSale) {
        this.originalSale = originalSale;
    }

    // Alias for getOriginalSale
    public Sale getSale() {
        return originalSale;
    }

    public void setSale(Sale originalSale) {
        this.originalSale = originalSale;
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

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public List<SaleReturnItem> getItems() {
        return items;
    }

    public void setItems(List<SaleReturnItem> items) {
        this.items = items;
    }
}
