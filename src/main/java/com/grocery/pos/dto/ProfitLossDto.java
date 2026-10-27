package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProfitLossDto {

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal grossSales = BigDecimal.ZERO;
    private BigDecimal saleDiscounts = BigDecimal.ZERO;
    private BigDecimal saleReturns = BigDecimal.ZERO;
    private BigDecimal netSalesRevenue = BigDecimal.ZERO;

    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal grossProfitMargin = BigDecimal.ZERO;

    private BigDecimal outputGst = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private BigDecimal netProfitMargin = BigDecimal.ZERO;

    private long totalSalesCount = 0;
    private long totalReturnsCount = 0;

    public ProfitLossDto() {
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getGrossSales() {
        return grossSales;
    }

    public void setGrossSales(BigDecimal grossSales) {
        this.grossSales = grossSales;
    }

    public BigDecimal getSaleDiscounts() {
        return saleDiscounts;
    }

    public void setSaleDiscounts(BigDecimal saleDiscounts) {
        this.saleDiscounts = saleDiscounts;
    }

    public BigDecimal getSaleReturns() {
        return saleReturns;
    }

    public void setSaleReturns(BigDecimal saleReturns) {
        this.saleReturns = saleReturns;
    }

    public BigDecimal getNetSalesRevenue() {
        return netSalesRevenue;
    }

    public void setNetSalesRevenue(BigDecimal netSalesRevenue) {
        this.netSalesRevenue = netSalesRevenue;
    }

    public BigDecimal getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(BigDecimal costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getGrossProfitMargin() {
        return grossProfitMargin;
    }

    public void setGrossProfitMargin(BigDecimal grossProfitMargin) {
        this.grossProfitMargin = grossProfitMargin;
    }

    public BigDecimal getOutputGst() {
        return outputGst;
    }

    public void setOutputGst(BigDecimal outputGst) {
        this.outputGst = outputGst;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getNetProfitMargin() {
        return netProfitMargin;
    }

    public void setNetProfitMargin(BigDecimal netProfitMargin) {
        this.netProfitMargin = netProfitMargin;
    }

    public long getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(long totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public long getTotalReturnsCount() {
        return totalReturnsCount;
    }

    public void setTotalReturnsCount(long totalReturnsCount) {
        this.totalReturnsCount = totalReturnsCount;
    }
}
