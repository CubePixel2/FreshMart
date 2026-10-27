package com.grocery.pos;

import com.grocery.pos.controller.RealtimeApiController;
import com.grocery.pos.dto.*;
import com.grocery.pos.service.RealtimeSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RealtimeReportsAndReturnsTest {

    @Autowired
    private RealtimeSyncService realtimeSyncService;

    @Autowired
    private RealtimeApiController realtimeApiController;

    @Test
    void testSseSubscribeAndBroadcast() {
        SseEmitter emitter = realtimeSyncService.subscribe();
        assertNotNull(emitter, "SseEmitter should not be null");

        RealtimeEvent event = new RealtimeEvent(
                "SALE_RETURN_CREATED",
                "SR-TEST-001",
                new BigDecimal("350.00"),
                "Test Customer",
                "Cashier Aman",
                "Customer return processed"
        );
        realtimeSyncService.broadcast(event);

        List<RealtimeEvent> recent = realtimeSyncService.getRecentEvents();
        assertFalse(recent.isEmpty(), "Recent events should contain the broadcasted event");
        assertEquals("SALE_RETURN_CREATED", recent.get(0).getType());
        assertEquals("SR-TEST-001", recent.get(0).getEntityNumber());
    }

    @Test
    void testLiveSummaryApi() {
        ResponseEntity<RealtimeSummaryDto> response = realtimeApiController.getLiveSummary();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        RealtimeSummaryDto dto = response.getBody();
        assertNotNull(dto);
        assertNotNull(dto.getServerTime());
        assertTrue(dto.getTodaySalesCount() >= 0);
        assertTrue(dto.getTodayReturnsCount() >= 0);
    }

    @Test
    void testSaleReturnsApi() {
        ResponseEntity<List<RealtimeSaleReturnDto>> response = realtimeApiController.getSaleReturns();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testProductReturnsApi() {
        ResponseEntity<List<RealtimeProductReturnDto>> response = realtimeApiController.getProductReturns();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testRealtimeProfitLossApi() {
        LocalDate today = LocalDate.now();
        ResponseEntity<ProfitLossDto> response = realtimeApiController.getProfitLoss(today.withDayOfMonth(1), today);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        ProfitLossDto pl = response.getBody();
        assertNotNull(pl);
        assertNotNull(pl.getGrossSales());
        assertNotNull(pl.getNetProfit());
    }

    @Test
    void testRealtimeTrialBalanceApi() {
        LocalDate today = LocalDate.now();
        ResponseEntity<TrialBalanceDto> response = realtimeApiController.getTrialBalance(today.withDayOfMonth(1), today);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        TrialBalanceDto tb = response.getBody();
        assertNotNull(tb);
        assertNotNull(tb.getTotalDebit());
        assertNotNull(tb.getTotalCredit());
        assertNotNull(tb.getAccounts());
    }

    @Test
    void testRealtimeGstReturnApi() {
        LocalDate today = LocalDate.now();
        ResponseEntity<GstReturnDto> response = realtimeApiController.getGstReturn(today.withDayOfMonth(1), today);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        GstReturnDto gst = response.getBody();
        assertNotNull(gst);
        assertNotNull(gst.getTaxableSales());
        assertNotNull(gst.getNetGstPayable());
    }

    @Test
    void testRealtimeSaleReturnsReportApi() {
        LocalDate today = LocalDate.now();
        ResponseEntity<Map<String, Object>> response = realtimeApiController.getSaleReturnsReport(today.withDayOfMonth(1), today);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("returns"));
        assertTrue(body.containsKey("totalRefund"));
    }

    @Test
    void testRealtimeProductReturnsReportApi() {
        LocalDate today = LocalDate.now();
        ResponseEntity<Map<String, Object>> response = realtimeApiController.getProductReturnsReport(today.withDayOfMonth(1), today);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("returns"));
        assertTrue(body.containsKey("totalRefund"));
    }
}
