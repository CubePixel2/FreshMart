package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class GstReturnDto {

    private LocalDate startDate;
    private LocalDate endDate;

    // Outward Supplies (Sales)
    private BigDecimal taxableSales = BigDecimal.ZERO;
    private BigDecimal outputGstTotal = BigDecimal.ZERO;
    private BigDecimal outputCgst = BigDecimal.ZERO;
    private BigDecimal outputSgst = BigDecimal.ZERO;

    // Inward Supplies (Purchases)
    private BigDecimal taxablePurchases = BigDecimal.ZERO;
    private BigDecimal inputGstTotal = BigDecimal.ZERO;
    private BigDecimal inputCgst = BigDecimal.ZERO;
    private BigDecimal inputSgst = BigDecimal.ZERO;

    // Returns Adjustments
    private BigDecimal saleReturnsTotal = BigDecimal.ZERO;
    private BigDecimal saleReturnsGst = BigDecimal.ZERO;

    private BigDecimal productReturnsTotal = BigDecimal.ZERO;
    private BigDecimal productReturnsGst = BigDecimal.ZERO;

    // Net Payable
    private BigDecimal netGstPayable = BigDecimal.ZERO;
    private BigDecimal netCgstPayable = BigDecimal.ZERO;
    private BigDecimal netSgstPayable = BigDecimal.ZERO;

    public GstReturnDto() {
    }

    public void recalculateNet() {
        // Output GST adjusted by returns
        BigDecimal adjOutput = outputGstTotal.subtract(saleReturnsGst != null ? saleReturnsGst : BigDecimal.ZERO);
        if (adjOutput.compareTo(BigDecimal.ZERO) < 0) adjOutput = BigDecimal.ZERO;

        BigDecimal adjInput = inputGstTotal.subtract(productReturnsGst != null ? productReturnsGst : BigDecimal.ZERO);
        if (adjInput.compareTo(BigDecimal.ZERO) < 0) adjInput = BigDecimal.ZERO;

        this.netGstPayable = adjOutput.subtract(adjInput);
        if (this.netGstPayable.compareTo(BigDecimal.ZERO) < 0) {
            this.netGstPayable = BigDecimal.ZERO;
        }

        this.outputCgst = outputGstTotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        this.outputSgst = outputGstTotal.subtract(outputCgst);

        this.inputCgst = inputGstTotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        this.inputSgst = inputGstTotal.subtract(inputCgst);

        this.netCgstPayable = netGstPayable.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        this.netSgstPayable = netGstPayable.subtract(netCgstPayable);
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

    public BigDecimal getTaxableSales() {
        return taxableSales;
    }

    public void setTaxableSales(BigDecimal taxableSales) {
        this.taxableSales = taxableSales;
    }

    public BigDecimal getOutputGstTotal() {
        return outputGstTotal;
    }

    public void setOutputGstTotal(BigDecimal outputGstTotal) {
        this.outputGstTotal = outputGstTotal;
    }

    public BigDecimal getOutputCgst() {
        return outputCgst;
    }

    public void setOutputCgst(BigDecimal outputCgst) {
        this.outputCgst = outputCgst;
    }

    public BigDecimal getOutputSgst() {
        return outputSgst;
    }

    public void setOutputSgst(BigDecimal outputSgst) {
        this.outputSgst = outputSgst;
    }

    public BigDecimal getTaxablePurchases() {
        return taxablePurchases;
    }

    public void setTaxablePurchases(BigDecimal taxablePurchases) {
        this.taxablePurchases = taxablePurchases;
    }

    public BigDecimal getInputGstTotal() {
        return inputGstTotal;
    }

    public void setInputGstTotal(BigDecimal inputGstTotal) {
        this.inputGstTotal = inputGstTotal;
    }

    public BigDecimal getInputCgst() {
        return inputCgst;
    }

    public void setInputCgst(BigDecimal inputCgst) {
        this.inputCgst = inputCgst;
    }

    public BigDecimal getInputSgst() {
        return inputSgst;
    }

    public void setInputSgst(BigDecimal inputSgst) {
        this.inputSgst = inputSgst;
    }

    public BigDecimal getSaleReturnsTotal() {
        return saleReturnsTotal;
    }

    public void setSaleReturnsTotal(BigDecimal saleReturnsTotal) {
        this.saleReturnsTotal = saleReturnsTotal;
    }

    public BigDecimal getSaleReturnsGst() {
        return saleReturnsGst;
    }

    public void setSaleReturnsGst(BigDecimal saleReturnsGst) {
        this.saleReturnsGst = saleReturnsGst;
    }

    public BigDecimal getProductReturnsTotal() {
        return productReturnsTotal;
    }

    public void setProductReturnsTotal(BigDecimal productReturnsTotal) {
        this.productReturnsTotal = productReturnsTotal;
    }

    public BigDecimal getProductReturnsGst() {
        return productReturnsGst;
    }

    public void setProductReturnsGst(BigDecimal productReturnsGst) {
        this.productReturnsGst = productReturnsGst;
    }

    public BigDecimal getNetGstPayable() {
        return netGstPayable;
    }

    public void setNetGstPayable(BigDecimal netGstPayable) {
        this.netGstPayable = netGstPayable;
    }

    public BigDecimal getNetCgstPayable() {
        return netCgstPayable;
    }

    public void setNetCgstPayable(BigDecimal netCgstPayable) {
        this.netCgstPayable = netCgstPayable;
    }

    public BigDecimal getNetSgstPayable() {
        return netSgstPayable;
    }

    public void setNetSgstPayable(BigDecimal netSgstPayable) {
        this.netSgstPayable = netSgstPayable;
    }
}
