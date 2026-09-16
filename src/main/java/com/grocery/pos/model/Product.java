package com.grocery.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer minStockThreshold = 10;

    @Column(length = 30)
    private String unit = "pcs"; // kg, pcs, pack, liter, box, etc.

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 500)
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Product() {
    }

    public Product(String name, String barcode, Category category, Supplier supplier,
                   BigDecimal costPrice, BigDecimal sellingPrice, Integer stockQuantity,
                   Integer minStockThreshold, String unit, String imageUrl, String description) {
        this.name = name;
        this.barcode = barcode;
        this.category = category;
        this.supplier = supplier;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.minStockThreshold = minStockThreshold != null ? minStockThreshold : 10;
        this.unit = unit != null ? unit : "pcs";
        this.imageUrl = imageUrl;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper status methods
    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && minStockThreshold != null
                && stockQuantity > 0 && stockQuantity <= minStockThreshold;
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return "OUT_OF_STOCK";
        } else if (isLowStock()) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }

    public BigDecimal getProfitMargin() {
        if (sellingPrice != null && costPrice != null) {
            return sellingPrice.subtract(costPrice);
        }
        return BigDecimal.ZERO;
    }

    public double getProfitMarginPercentage() {
        if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0 && sellingPrice != null) {
            BigDecimal margin = sellingPrice.subtract(costPrice);
            return margin.divide(costPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(Integer minStockThreshold) {
        this.minStockThreshold = minStockThreshold;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
