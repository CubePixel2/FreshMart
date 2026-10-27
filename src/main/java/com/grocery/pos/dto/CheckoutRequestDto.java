package com.grocery.pos.dto;

import com.grocery.pos.model.PaymentMethod;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckoutRequestDto {

    private List<CartItemDto> items = new ArrayList<>();
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    private String paymentGateway = "CASH";
    private String transactionId;
    private String paymentReference;
    private String paymentStatus = "SUCCESS";
    private String orderId;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private String notes;

    public CheckoutRequestDto() {
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        if (this.paymentReference == null) {
            this.paymentReference = transactionId;
        }
    }

    public String getPaymentReference() {
        if (paymentReference != null && !paymentReference.isBlank()) {
            return paymentReference;
        }
        return transactionId;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
        if (this.transactionId == null) {
            this.transactionId = paymentReference;
        }
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
