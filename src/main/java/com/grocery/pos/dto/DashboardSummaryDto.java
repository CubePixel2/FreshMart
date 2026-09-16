package com.grocery.pos.dto;

import java.math.BigDecimal;

public class DashboardSummaryDto {
    private long totalProducts;
    private long lowStockCount;
    private long todaySalesCount;
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(long totalProducts, long lowStockCount, long todaySalesCount,
                               BigDecimal todayRevenue, BigDecimal totalRevenue) {
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.todaySalesCount = todaySalesCount;
        this.todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getTodaySalesCount() {
        return todaySalesCount;
    }

    public void setTodaySalesCount(long todaySalesCount) {
        this.todaySalesCount = todaySalesCount;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
