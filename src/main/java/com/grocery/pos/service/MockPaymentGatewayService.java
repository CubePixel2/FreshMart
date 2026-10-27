package com.grocery.pos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.pos.config.PaymentGatewayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayService.class);

    @Value("${payment.gateway.mode:mock}")
    private String gatewayMode;

    @Value("${payment.gateway.key:dummy}")
    private String gatewayKey;

    @Value("${payment.gateway.secret:dummy}")
    private String gatewaySecret;

    private final PaymentGatewayConfig config;

    public MockPaymentGatewayService(PaymentGatewayConfig config) {
        this.config = config;
    }

    @Override
    public Map<String, Object> createOrder(BigDecimal amount, String receipt, String notes) {
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();
        String orderId = "MOCK_ORD_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("gatewayKey", gatewayKey);
        response.put("mode", gatewayMode);
        response.put("receipt", receipt != null ? receipt : ("RCP-" + System.currentTimeMillis()));
        response.put("notes", notes != null ? notes : "Online Gateway POS Checkout");
        response.put("status", "created");

        log.info("Mock Payment Gateway Order Created: orderId={}, amountInPaise={}, mode={}",
                orderId, amountInPaise, gatewayMode);
        return response;
    }

    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        log.info("Verifying Online Payment: orderId={}, paymentId={}, mode={}", orderId, paymentId, gatewayMode);
        if ("mock".equalsIgnoreCase(gatewayMode) || "test".equalsIgnoreCase(gatewayMode)) {
            return true;
        }
        return paymentId != null && !paymentId.isBlank();
    }

    @Override
    public Map<String, Object> createRazorpayOrder(BigDecimal amount, String receipt, String notes) {
        if (!config.isRazorpayEnabled()) {
            throw new IllegalStateException("Razorpay Payment Gateway is currently disabled.");
        }

        long amountInPaise = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        // 1. If valid live/test credentials provided, attempt real Razorpay Orders API call
        Map<String, Object> realOrder = tryCreateRealRazorpayOrder(amountInPaise, receipt, notes);
        if (realOrder != null) {
            return realOrder;
        }

        // 2. Otherwise generate realistic mock order with isMock=true flag for client simulator
        String orderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("amount", amountInPaise);
        response.put("currency", config.getRazorpayCurrency());
        response.put("keyId", config.getRazorpayKeyId());
        response.put("receipt", receipt != null ? receipt : ("RCP-" + System.currentTimeMillis()));
        response.put("mode", config.getRazorpayMode());
        response.put("notes", notes != null ? notes : "FreshMart Grocery Checkout");
        response.put("status", "created");
        response.put("isMock", true);

        log.info("Generated Razorpay Mock Order: orderId={}, amountInPaise={}", orderId, amountInPaise);
        return response;
    }

    private Map<String, Object> tryCreateRealRazorpayOrder(long amountInPaise, String receipt, String notes) {
        String keyId = config.getRazorpayKeyId();
        String keySecret = config.getRazorpayKeySecret();

        // Check if dummy key or mock placeholder
        if (keyId == null || keyId.isBlank() || keyId.contains("freshmart12345") || keyId.equalsIgnoreCase("dummy")
                || keySecret == null || keySecret.isBlank() || keySecret.equalsIgnoreCase("dummy") || keySecret.contains("67890")) {
            return null;
        }

        try {
            String auth = Base64.getEncoder().encodeToString((keyId.trim() + ":" + keySecret.trim()).getBytes(StandardCharsets.UTF_8));

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("amount", amountInPaise);
            reqBody.put("currency", config.getRazorpayCurrency() != null ? config.getRazorpayCurrency() : "INR");
            reqBody.put("receipt", receipt != null ? receipt : ("RCP-" + System.currentTimeMillis()));
            Map<String, String> notesMap = new HashMap<>();
            notesMap.put("source", "FreshMart POS");
            notesMap.put("info", notes != null ? notes : "Retail POS Checkout");
            reqBody.put("notes", notesMap);

            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(reqBody);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .timeout(Duration.ofSeconds(7))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rzpRes = mapper.readValue(response.body(), Map.class);
                String realOrderId = (String) rzpRes.get("id");
                log.info("Real Razorpay API Order successfully created: {}", realOrderId);

                Map<String, Object> res = new HashMap<>();
                res.put("orderId", realOrderId);
                res.put("amount", amountInPaise);
                res.put("currency", config.getRazorpayCurrency());
                res.put("keyId", keyId.trim());
                res.put("receipt", receipt);
                res.put("mode", config.getRazorpayMode());
                res.put("notes", notes);
                res.put("status", "created");
                res.put("isMock", false);
                return res;
            } else {
                log.warn("Razorpay API call returned HTTP {}: {}. Falling back to sandbox simulation.",
                        response.statusCode(), response.body());
            }
        } catch (Exception ex) {
            log.warn("Could not reach Razorpay API: {}. Falling back to sandbox simulation.", ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null) {
            return false;
        }

        // Mock / simulation verification always passes
        if ("TEST".equalsIgnoreCase(config.getRazorpayMode()) || "mock".equalsIgnoreCase(gatewayMode)
                || (signature != null && (signature.startsWith("simulated_") || signature.startsWith("sig_") || signature.contains("test")))
                || (orderId.startsWith("order_mock_"))) {
            return true;
        }

        try {
            String payload = orderId + "|" + paymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    config.getRazorpayKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hmacBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(signature);
        } catch (Exception ex) {
            log.error("Error verifying signature", ex);
            return "TEST".equalsIgnoreCase(config.getRazorpayMode()) || "mock".equalsIgnoreCase(gatewayMode);
        }
    }

    @Override
    public Map<String, Object> createStripePaymentIntent(BigDecimal amount, String currency, String customerName, String customerPhone) {
        if (!config.isStripeEnabled()) {
            throw new IllegalStateException("Stripe Card Gateway is currently disabled.");
        }

        long amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();
        String clientSecret = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20) + "_secret_" + UUID.randomUUID().toString().substring(0, 10);
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", clientSecret);
        response.put("paymentIntentId", paymentIntentId);
        response.put("publishableKey", config.getStripePublishableKey());
        response.put("amount", amountInSmallestUnit);
        response.put("currency", currency != null ? currency.toLowerCase() : config.getStripeCurrency().toLowerCase());
        response.put("customerName", customerName);
        response.put("customerPhone", customerPhone);
        response.put("mode", config.getStripeMode());
        response.put("status", "requires_payment_method");

        return response;
    }

    @Override
    public Map<String, Object> generateUpiPayment(BigDecimal amount, String invoiceRef, String note) {
        if (!config.isUpiEnabled()) {
            throw new IllegalStateException("Dynamic UPI QR Gateway is currently disabled.");
        }

        String vpa = config.getUpiVpa();
        String merchantName = config.getUpiMerchantName();
        String formattedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String txnNote = (note != null && !note.isBlank()) ? note : "Payment for " + (invoiceRef != null ? invoiceRef : "FreshMart Grocery");

        String upiUri = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=%s",
                urlEncode(vpa),
                urlEncode(merchantName),
                urlEncode(formattedAmount),
                urlEncode(txnNote));

        if (invoiceRef != null && !invoiceRef.isBlank()) {
            upiUri += "&tr=" + urlEncode(invoiceRef);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("upiUri", upiUri);
        response.put("vpa", vpa);
        response.put("merchantName", merchantName);
        response.put("amount", formattedAmount);
        response.put("currency", "INR");
        response.put("transactionNote", txnNote);
        response.put("reference", invoiceRef);

        return response;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }
}
