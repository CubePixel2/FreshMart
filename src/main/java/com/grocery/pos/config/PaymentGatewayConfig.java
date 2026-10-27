package com.grocery.pos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentGatewayConfig {

    // Razorpay
    @Value("${payment.razorpay.enabled:true}")
    private boolean razorpayEnabled;

    @Value("${payment.razorpay.key-id:rzp_test_freshmart12345}")
    private String razorpayKeyId;

    @Value("${payment.razorpay.key-secret:test_secret_key_67890}")
    private String razorpayKeySecret;

    @Value("${payment.razorpay.currency:INR}")
    private String razorpayCurrency;

    @Value("${payment.razorpay.mode:TEST}")
    private String razorpayMode;

    // Stripe
    @Value("${payment.stripe.enabled:true}")
    private boolean stripeEnabled;

    @Value("${payment.stripe.publishable-key:pk_test_freshmart12345}")
    private String stripePublishableKey;

    @Value("${payment.stripe.secret-key:sk_test_freshmart12345}")
    private String stripeSecretKey;

    @Value("${payment.stripe.currency:inr}")
    private String stripeCurrency;

    @Value("${payment.stripe.mode:TEST}")
    private String stripeMode;

    // Dynamic UPI QR
    @Value("${payment.upi.enabled:true}")
    private boolean upiEnabled;

    @Value("${payment.upi.vpa:freshmart.kochi@okaxis}")
    private String upiVpa;

    @Value("${payment.upi.merchant-name:FreshMart Supermarket Kochi}")
    private String upiMerchantName;

    // Card POS Terminal
    @Value("${payment.pos-terminal.enabled:true}")
    private boolean posTerminalEnabled;

    @Value("${payment.pos-terminal.terminal-id:EDC-KOCHI-POS-01}")
    private String posTerminalId;

    // Getters and Setters
    public boolean isRazorpayEnabled() {
        return razorpayEnabled;
    }

    public void setRazorpayEnabled(boolean razorpayEnabled) {
        this.razorpayEnabled = razorpayEnabled;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }

    public String getRazorpayKeySecret() {
        return razorpayKeySecret;
    }

    public void setRazorpayKeySecret(String razorpayKeySecret) {
        this.razorpayKeySecret = razorpayKeySecret;
    }

    public String getRazorpayCurrency() {
        return razorpayCurrency;
    }

    public void setRazorpayCurrency(String razorpayCurrency) {
        this.razorpayCurrency = razorpayCurrency;
    }

    public String getRazorpayMode() {
        return razorpayMode;
    }

    public void setRazorpayMode(String razorpayMode) {
        this.razorpayMode = razorpayMode;
    }

    public boolean isStripeEnabled() {
        return stripeEnabled;
    }

    public void setStripeEnabled(boolean stripeEnabled) {
        this.stripeEnabled = stripeEnabled;
    }

    public String getStripePublishableKey() {
        return stripePublishableKey;
    }

    public void setStripePublishableKey(String stripePublishableKey) {
        this.stripePublishableKey = stripePublishableKey;
    }

    public String getStripeSecretKey() {
        return stripeSecretKey;
    }

    public void setStripeSecretKey(String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
    }

    public String getStripeCurrency() {
        return stripeCurrency;
    }

    public void setStripeCurrency(String stripeCurrency) {
        this.stripeCurrency = stripeCurrency;
    }

    public String getStripeMode() {
        return stripeMode;
    }

    public void setStripeMode(String stripeMode) {
        this.stripeMode = stripeMode;
    }

    public boolean isUpiEnabled() {
        return upiEnabled;
    }

    public void setUpiEnabled(boolean upiEnabled) {
        this.upiEnabled = upiEnabled;
    }

    public String getUpiVpa() {
        return upiVpa;
    }

    public void setUpiVpa(String upiVpa) {
        this.upiVpa = upiVpa;
    }

    public String getUpiMerchantName() {
        return upiMerchantName;
    }

    public void setUpiMerchantName(String upiMerchantName) {
        this.upiMerchantName = upiMerchantName;
    }

    public boolean isPosTerminalEnabled() {
        return posTerminalEnabled;
    }

    public void setPosTerminalEnabled(boolean posTerminalEnabled) {
        this.posTerminalEnabled = posTerminalEnabled;
    }

    public String getPosTerminalId() {
        return posTerminalId;
    }

    public void setPosTerminalId(String posTerminalId) {
        this.posTerminalId = posTerminalId;
    }

    /**
     * Safe public gateway configurations that can be exposed to frontend
     */
    public Map<String, Object> getPublicConfig() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> rzp = new HashMap<>();
        rzp.put("enabled", razorpayEnabled);
        rzp.put("keyId", razorpayKeyId);
        rzp.put("currency", razorpayCurrency);
        rzp.put("mode", razorpayMode);
        config.put("razorpay", rzp);

        Map<String, Object> stripe = new HashMap<>();
        stripe.put("enabled", stripeEnabled);
        stripe.put("publishableKey", stripePublishableKey);
        stripe.put("currency", stripeCurrency);
        stripe.put("mode", stripeMode);
        config.put("stripe", stripe);

        Map<String, Object> upi = new HashMap<>();
        upi.put("enabled", upiEnabled);
        upi.put("vpa", upiVpa);
        upi.put("merchantName", upiMerchantName);
        config.put("upi", upi);

        Map<String, Object> pos = new HashMap<>();
        pos.put("enabled", posTerminalEnabled);
        pos.put("terminalId", posTerminalId);
        config.put("posTerminal", pos);

        return config;
    }
}
