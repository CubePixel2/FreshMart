package com.grocery.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.pos.dto.CartItemDto;
import com.grocery.pos.dto.CheckoutRequestDto;
import com.grocery.pos.model.PaymentMethod;
import com.grocery.pos.model.Product;
import com.grocery.pos.model.Sale;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.SaleRepository;
import com.grocery.pos.service.PaymentGatewayService;
import com.grocery.pos.service.SaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RazorpayPaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private com.grocery.pos.repository.UserRepository userRepository;

    @Test
    @DisplayName("1. Verify payment config endpoint returns enabled Razorpay")
    public void testPaymentConfigEndpoint() throws Exception {
        mockMvc.perform(get("/api/payment/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.razorpay.enabled").value(true))
                .andExpect(jsonPath("$.data.razorpay.currency").value("INR"));
    }

    @Test
    @DisplayName("2. Create Razorpay order via REST endpoint")
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    public void testCreateRazorpayOrderEndpoint() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("amount", "250.00");
        req.put("receipt", "RCP-TEST-001");
        req.put("notes", "Unit Test POS");

        mockMvc.perform(post("/api/payment/razorpay/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andExpect(jsonPath("$.data.amount").value(25000))
                .andExpect(jsonPath("$.data.currency").value("INR"));
    }

    @Test
    @DisplayName("3. Verify Razorpay payment signature via REST endpoint")
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    public void testVerifyRazorpayPaymentEndpoint() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("orderId", "order_mock_test123");
        req.put("paymentId", "pay_rzp_mock456");
        req.put("signature", "simulated_sig_ok");

        mockMvc.perform(post("/api/payment/razorpay/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.paymentId").value("pay_rzp_mock456"));
    }

    @Test
    @DisplayName("4. Complete POS checkout using Razorpay payment method")
    public void testCheckoutWithRazorpay() {
        Product p = productRepository.findAll().get(0);
        int initialStock = p.getStockQuantity();

        CheckoutRequestDto dto = new CheckoutRequestDto();
        dto.setCashierName("Aiswarya R");
        dto.setCustomerName("Vijay Kumar");
        dto.setCustomerPhone("+91 98470 11990");
        dto.setPaymentMethod(PaymentMethod.RAZORPAY);
        dto.setPaymentGateway("RAZORPAY");
        dto.setTransactionId("pay_rzp_test_998877");
        dto.setPaymentReference("order_mock_112233");
        dto.setPaymentStatus("PAID");
        dto.setAmountPaid(p.getSellingPrice());

        CartItemDto cartItem = new CartItemDto();
        cartItem.setProductId(p.getId());
        cartItem.setName(p.getName());
        cartItem.setPrice(p.getSellingPrice());
        cartItem.setQuantity(1);
        dto.setItems(Collections.singletonList(cartItem));

        com.grocery.pos.model.User cashierUser = userRepository.findByUsername("admin").orElse(null);
        Sale sale = saleService.processCheckout(dto, cashierUser);

        assertThat(sale).isNotNull();
        assertThat(sale.getInvoiceNumber()).isNotNull();
        assertThat(sale.getCashierName()).isEqualTo("Aiswarya R");
        assertThat(sale.getPaymentGateway()).isEqualTo("RAZORPAY");
        assertThat(sale.getTransactionId()).isEqualTo("pay_rzp_test_998877");
        assertThat(sale.getPaymentStatus()).isEqualTo("PAID");

        // Verify stock decreased
        Product updatedProduct = productRepository.findById(p.getId()).orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(initialStock - 1);
    }
}
