package com.grocery.pos.dto;

import com.grocery.pos.model.PurchasePaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseInvoiceFormDto {

    private String invoiceNumber;
    private Long purchaseOrderId;
    private Long supplierId;
    private LocalDate invoiceDate = LocalDate.now();
    private PurchasePaymentStatus paymentStatus = PurchasePaymentStatus.UNPAID;
    private boolean postImmediately = true;
    private String notes;

    private List<Long> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private List<BigDecimal> unitCosts = new ArrayList<>();
    private List<BigDecimal> taxAmounts = new ArrayList<>();

    public PurchaseInvoiceFormDto() {
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public PurchasePaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PurchasePaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isPostImmediately() {
        return postImmediately;
    }

    public void setPostImmediately(boolean postImmediately) {
        this.postImmediately = postImmediately;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public List<BigDecimal> getUnitCosts() {
        return unitCosts;
    }

    public void setUnitCosts(List<BigDecimal> unitCosts) {
        this.unitCosts = unitCosts;
    }

    public List<BigDecimal> getTaxAmounts() {
        return taxAmounts;
    }

    public void setTaxAmounts(List<BigDecimal> taxAmounts) {
        this.taxAmounts = taxAmounts;
    }
}
