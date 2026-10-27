package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RealtimeSummaryDto {

    private int todaySalesCount;
    private BigDecimal todaySalesTotal = BigDecimal.ZERO;
    private int todayReturnsCount;
    private BigDecimal todayRefundTotal = BigDecimal.ZERO;
    private int todayProductReturnsCount;
    private BigDecimal todayProductRefundTotal = BigDecimal.ZERO;
    private BigDecimal grossProfitToday = BigDecimal.ZERO;
    private long lowStockCount;
    private long totalProducts;
    private BigDecimal totalInventoryValue = BigDecimal.ZERO;
    private String serverTime;
    private List<RealtimeEvent> recentEvents = new ArrayList<>();

    public RealtimeSummaryDto() {
    }

    // Getters and Setters
    public int getTodaySalesCount() { return todaySalesCount; }
    public void setTodaySalesCount(int todaySalesCount) { this.todaySalesCount = todaySalesCount; }

    public BigDecimal getTodaySalesTotal() { return todaySalesTotal; }
    public void setTodaySalesTotal(BigDecimal todaySalesTotal) { this.todaySalesTotal = todaySalesTotal; }

    public int getTodayReturnsCount() { return todayReturnsCount; }
    public void setTodayReturnsCount(int todayReturnsCount) { this.todayReturnsCount = todayReturnsCount; }

    public BigDecimal getTodayRefundTotal() { return todayRefundTotal; }
    public void setTodayRefundTotal(BigDecimal todayRefundTotal) { this.todayRefundTotal = todayRefundTotal; }

    public int getTodayProductReturnsCount() { return todayProductReturnsCount; }
    public void setTodayProductReturnsCount(int todayProductReturnsCount) { this.todayProductReturnsCount = todayProductReturnsCount; }

    public BigDecimal getTodayProductRefundTotal() { return todayProductRefundTotal; }
    public void setTodayProductRefundTotal(BigDecimal todayProductRefundTotal) { this.todayProductRefundTotal = todayProductRefundTotal; }

    public BigDecimal getGrossProfitToday() { return grossProfitToday; }
    public void setGrossProfitToday(BigDecimal grossProfitToday) { this.grossProfitToday = grossProfitToday; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public String getServerTime() { return serverTime; }
    public void setServerTime(String serverTime) { this.serverTime = serverTime; }

    public List<RealtimeEvent> getRecentEvents() { return recentEvents; }
    public void setRecentEvents(List<RealtimeEvent> recentEvents) { this.recentEvents = recentEvents; }
}
