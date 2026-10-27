package com.grocery.pos.controller;

import com.grocery.pos.dto.GstReturnDto;
import com.grocery.pos.dto.ProfitLossDto;
import com.grocery.pos.dto.TrialBalanceDto;
import com.grocery.pos.model.ProductReturn;
import com.grocery.pos.model.SaleReturn;
import com.grocery.pos.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 1. PROFIT AND LOSS
    @GetMapping("/profit-loss")
    public String profitAndLossReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        ProfitLossDto pl = reportService.generateProfitAndLossReport(startDate, endDate);

        model.addAttribute("report", pl);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "report-pl");
        return "reports/profit-loss";
    }

    // 2. TRIAL BALANCE
    @GetMapping("/trial-balance")
    public String trialBalanceReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        TrialBalanceDto tb = reportService.generateTrialBalanceReport(startDate, endDate);

        model.addAttribute("report", tb);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "report-tb");
        return "reports/trial-balance";
    }

    // 3. SALE RETURNS REPORT
    @GetMapping("/sale-returns")
    public String saleReturnsReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<SaleReturn> returns = reportService.getSaleReturnsReport(startDate, endDate);
        BigDecimal totalRefund = returns.stream()
                .map(SaleReturn::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("returns", returns);
        model.addAttribute("totalRefund", totalRefund);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "report-sr");
        return "reports/sale-returns";
    }

    // 4. PRODUCT RETURNS REPORT (TO SUPPLIERS)
    @GetMapping("/product-returns")
    public String productReturnsReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<ProductReturn> returns = reportService.getProductReturnsReport(startDate, endDate);
        BigDecimal totalRefund = returns.stream()
                .map(ProductReturn::getTotalRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("returns", returns);
        model.addAttribute("totalRefund", totalRefund);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "report-pr");
        return "reports/product-returns";
    }

    // 5. VAT / GST TAX RETURN
    @GetMapping("/gst-return")
    public String gstReturnReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        GstReturnDto gst = reportService.generateGstReturnReport(startDate, endDate);

        model.addAttribute("report", gst);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "report-gst");
        return "reports/gst-return";
    }
}
