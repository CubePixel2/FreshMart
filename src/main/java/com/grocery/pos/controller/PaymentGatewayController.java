package com.grocery.pos.controller;

import com.grocery.pos.config.PaymentGatewayConfig;
import com.grocery.pos.dto.ApiResponseDto;
import com.grocery.pos.service.PaymentGatewayService;
import com.grocery.pos.service.SaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentGatewayController {

    private final PaymentGatewayConfig config;
    private final PaymentGatewayService paymentGatewayService;
    private final SaleService saleService;

    public PaymentGatewayController(PaymentGatewayConfig config,
                                    PaymentGatewayService paymentGatewayService,
                                    SaleService saleService) {
        this.config = config;
        this.paymentGatewayService = paymentGatewayService;
        this.saleService = saleService;
    }

    // =========================================================================
    // REST APIs for POS Client Checkout Modal
    // =========================================================================

    @GetMapping("/api/payment/config")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> getPublicConfig() {
        return ResponseEntity.ok(ApiResponseDto.ok("Payment configurations fetched", config.getPublicConfig()));
    }

    @PostMapping("/api/payment/online/create-order")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> createOnlineOrder(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0").toString());
            String receipt = (String) request.get("receipt");
            String notes = (String) request.get("notes");

            Map<String, Object> order = paymentGatewayService.createOrder(amount, receipt, notes);
            return ResponseEntity.ok(ApiResponseDto.ok("Online order created", order));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("Failed to create online order: " + ex.getMessage()));
        }
    }

    @PostMapping("/api/payment/online/verify")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> verifyOnlinePayment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String paymentId = request.get("paymentId");
            String signature = request.get("signature");

            boolean isValid = paymentGatewayService.verifyPayment(orderId, paymentId, signature);
            if (isValid) {
                Map<String, Object> data = new HashMap<>();
                data.put("verified", true);
                data.put("paymentId", paymentId);
                data.put("orderId", orderId);
                return ResponseEntity.ok(ApiResponseDto.ok("Payment verified successfully", data));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("Payment verification failed"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Error verifying payment: " + ex.getMessage()));
        }
    }

    @PostMapping("/api/payment/razorpay/create-order")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> createRazorpayOrder(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0").toString());
            String receipt = (String) request.get("receipt");
            String notes = (String) request.get("notes");

            Map<String, Object> order = paymentGatewayService.createRazorpayOrder(amount, receipt, notes);
            return ResponseEntity.ok(ApiResponseDto.ok("Razorpay order created", order));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("Failed to create Razorpay order: " + ex.getMessage()));
        }
    }

    @PostMapping("/api/payment/razorpay/verify")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> verifyRazorpayPayment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String paymentId = request.get("paymentId");
            String signature = request.get("signature");

            boolean isValid = paymentGatewayService.verifyRazorpaySignature(orderId, paymentId, signature);
            if (isValid) {
                Map<String, Object> data = new HashMap<>();
                data.put("verified", true);
                data.put("paymentId", paymentId);
                data.put("orderId", orderId);
                return ResponseEntity.ok(ApiResponseDto.ok("Payment verified successfully", data));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("Invalid Razorpay payment signature"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Error verifying payment: " + ex.getMessage()));
        }
    }

    @PostMapping("/api/payment/stripe/create-intent")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> createStripeIntent(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0").toString());
            String currency = (String) request.getOrDefault("currency", "inr");
            String customerName = (String) request.get("customerName");
            String customerPhone = (String) request.get("customerPhone");

            Map<String, Object> intent = paymentGatewayService.createStripePaymentIntent(amount, currency, customerName, customerPhone);
            return ResponseEntity.ok(ApiResponseDto.ok("Stripe PaymentIntent created", intent));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("Failed to create Stripe payment intent: " + ex.getMessage()));
        }
    }

    @PostMapping("/api/payment/upi/generate")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> generateUpiPayment(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0").toString());
            String ref = (String) request.get("reference");
            String note = (String) request.get("note");

            Map<String, Object> upiData = paymentGatewayService.generateUpiPayment(amount, ref, note);
            return ResponseEntity.ok(ApiResponseDto.ok("UPI details generated", upiData));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("Failed to generate UPI payment: " + ex.getMessage()));
        }
    }

    // =========================================================================
    // Admin Management View & Update Controller
    // =========================================================================

    @GetMapping("/settings/payment-gateways")
    public String paymentGatewaysSettings(Model model) {
        model.addAttribute("config", config);
        model.addAttribute("recentSales", saleService.findRecentSales());
        model.addAttribute("activeNav", "payment-gateways");
        return "settings/payment-gateways";
    }

    @PostMapping("/settings/payment-gateways/update")
    public String updatePaymentGateways(
            @RequestParam(value = "razorpayEnabled", defaultValue = "false") boolean razorpayEnabled,
            @RequestParam("razorpayKeyId") String razorpayKeyId,
            @RequestParam("razorpayKeySecret") String razorpayKeySecret,
            @RequestParam("razorpayMode") String razorpayMode,
            @RequestParam(value = "stripeEnabled", defaultValue = "false") boolean stripeEnabled,
            @RequestParam("stripePublishableKey") String stripePublishableKey,
            @RequestParam("stripeSecretKey") String stripeSecretKey,
            @RequestParam("stripeMode") String stripeMode,
            @RequestParam(value = "upiEnabled", defaultValue = "false") boolean upiEnabled,
            @RequestParam("upiVpa") String upiVpa,
            @RequestParam("upiMerchantName") String upiMerchantName,
            @RequestParam(value = "posTerminalEnabled", defaultValue = "false") boolean posTerminalEnabled,
            @RequestParam("posTerminalId") String posTerminalId,
            RedirectAttributes redirectAttributes) {

        config.setRazorpayEnabled(razorpayEnabled);
        config.setRazorpayKeyId(razorpayKeyId.trim());
        config.setRazorpayKeySecret(razorpayKeySecret.trim());
        config.setRazorpayMode(razorpayMode);

        config.setStripeEnabled(stripeEnabled);
        config.setStripePublishableKey(stripePublishableKey.trim());
        config.setStripeSecretKey(stripeSecretKey.trim());
        config.setStripeMode(stripeMode);

        config.setUpiEnabled(upiEnabled);
        config.setUpiVpa(upiVpa.trim());
        config.setUpiMerchantName(upiMerchantName.trim());

        config.setPosTerminalEnabled(posTerminalEnabled);
        config.setPosTerminalId(posTerminalId.trim());

        redirectAttributes.addFlashAttribute("successMessage", "Payment Gateway settings updated successfully!");
        return "redirect:/settings/payment-gateways";
    }
}
