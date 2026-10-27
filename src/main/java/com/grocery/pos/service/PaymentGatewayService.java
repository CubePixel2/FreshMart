package com.grocery.pos.service;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayService {

    /**
     * Standard Online Gateway: Creates a payment order for checkout
     */
    Map<String, Object> createOrder(BigDecimal amount, String receipt, String notes);

    /**
     * Standard Online Gateway: Verifies online payment reference
     */
    boolean verifyPayment(String orderId, String paymentId, String signature);

    /**
     * Creates a Razorpay Order
     */
    Map<String, Object> createRazorpayOrder(BigDecimal amount, String receipt, String notes);

    /**
     * Verifies Razorpay Signature
     */
    boolean verifyRazorpaySignature(String orderId, String paymentId, String signature);

    /**
     * Creates a Stripe Payment Intent
     */
    Map<String, Object> createStripePaymentIntent(BigDecimal amount, String currency, String customerName, String customerPhone);

    /**
     * Generates a standard NPCI UPI Deep Link and QR code data
     */
    Map<String, Object> generateUpiPayment(BigDecimal amount, String invoiceRef, String note);
}
