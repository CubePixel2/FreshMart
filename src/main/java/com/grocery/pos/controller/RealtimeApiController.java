package com.grocery.pos.controller;

import com.grocery.pos.dto.*;
import com.grocery.pos.model.ProductReturn;
import com.grocery.pos.model.SaleReturn;
import com.grocery.pos.service.ProductReturnService;
import com.grocery.pos.service.RealtimeSyncService;
import com.grocery.pos.service.ReportService;
import com.grocery.pos.service.SaleReturnService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeApiController {

    private final RealtimeSyncService realtimeSyncService;
    private final SaleReturnService saleReturnService;
    private final ProductReturnService productReturnService;
    private final ReportService reportService;

    public RealtimeApiController(RealtimeSyncService realtimeSyncService,
                                 SaleReturnService saleReturnService,
                                 ProductReturnService productReturnService,
                                 ReportService reportService) {
        this.realtimeSyncService = realtimeSyncService;
        this.saleReturnService = saleReturnService;
        this.productReturnService = productReturnService;
        this.reportService = reportService;
    }

    /**
     * 1. Real-time Server-Sent Events (SSE) live push stream
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return realtimeSyncService.subscribe();
    }

    /**
     * 2. Real-time aggregated live summary snapshot
     */
    @GetMapping("/summary")
    public ResponseEntity<RealtimeSummaryDto> getLiveSummary() {
        return ResponseEntity.ok(realtimeSyncService.getLiveSummary());
    }

    /**
     * 3. Recent Real-time events list
     */
    @GetMapping("/events/recent")
    public ResponseEntity<List<RealtimeEvent>> getRecentEvents() {
        return ResponseEntity.ok(realtimeSyncService.getRecentEvents());
    }

    /**
     * 4. Real-time customer sale returns list
     */
    @GetMapping("/returns/sales")
    public ResponseEntity<List<RealtimeSaleReturnDto>> getSaleReturns() {
        List<RealtimeSaleReturnDto> list = saleReturnService.findAll().stream()
                .map(RealtimeSaleReturnDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * 5. Real-time product returns to suppliers list
     */
    @GetMapping("/returns/products")
    public ResponseEntity<List<RealtimeProductReturnDto>> getProductReturns() {
        List<RealtimeProductReturnDto> list = productReturnService.findAll().stream()
                .map(RealtimeProductReturnDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * 6. Real-time Profit & Loss statement data
     */
    @GetMapping("/reports/profit-loss")
    public ResponseEntity<ProfitLossDto> getProfitLoss(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        ProfitLossDto report = reportService.generateProfitAndLossReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * 7. Real-time Trial Balance ledger accounts
     */
    @GetMapping("/reports/trial-balance")
    public ResponseEntity<TrialBalanceDto> getTrialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        TrialBalanceDto report = reportService.generateTrialBalanceReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * 8. Real-time VAT / GST Tax return calculation
     */
    @GetMapping("/reports/gst-return")
    public ResponseEntity<GstReturnDto> getGstReturn(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        GstReturnDto report = reportService.generateGstReturnReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * 9. Real-time Customer Sale Returns Report
     */
    @GetMapping("/reports/sale-returns")
    public ResponseEntity<Map<String, Object>> getSaleReturnsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<SaleReturn> returns = reportService.getSaleReturnsReport(startDate, endDate);
        BigDecimal totalRefund = returns.stream()
                .map(SaleReturn::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RealtimeSaleReturnDto> dtoList = returns.stream()
                .map(RealtimeSaleReturnDto::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("startDate", startDate);
        resp.put("endDate", endDate);
        resp.put("count", dtoList.size());
        resp.put("totalRefund", totalRefund);
        resp.put("returns", dtoList);

        return ResponseEntity.ok(resp);
    }

    /**
     * 10. Real-time Product Returns to Vendors Report
     */
    @GetMapping("/reports/product-returns")
    public ResponseEntity<Map<String, Object>> getProductReturnsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<ProductReturn> returns = reportService.getProductReturnsReport(startDate, endDate);
        BigDecimal totalRefund = returns.stream()
                .map(ProductReturn::getTotalRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RealtimeProductReturnDto> dtoList = returns.stream()
                .map(RealtimeProductReturnDto::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("startDate", startDate);
        resp.put("endDate", endDate);
        resp.put("count", dtoList.size());
        resp.put("totalRefund", totalRefund);
        resp.put("returns", dtoList);

        return ResponseEntity.ok(resp);
    }
}
