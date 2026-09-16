package com.grocery.pos.controller;

import com.grocery.pos.dto.ChartDataDto;
import com.grocery.pos.dto.DashboardSummaryDto;
import com.grocery.pos.model.Product;
import com.grocery.pos.model.Sale;
import com.grocery.pos.service.CategoryService;
import com.grocery.pos.service.ProductService;
import com.grocery.pos.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final SaleService saleService;
    private final CategoryService categoryService;

    public DashboardController(ProductService productService,
                               SaleService saleService,
                               CategoryService categoryService) {
        this.productService = productService;
        this.saleService = saleService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardSummaryDto summary = saleService.getDashboardSummary();
        List<Product> lowStockProducts = productService.getLowStockProducts();
        List<Sale> recentSales = saleService.findRecentSales();

        model.addAttribute("summary", summary);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("activeNav", "dashboard");

        return "dashboard";
    }

    @GetMapping("/api/dashboard/chart-data")
    @ResponseBody
    public ResponseEntity<ChartDataDto> getChartData() {
        ChartDataDto chartData = saleService.getChartData();
        return ResponseEntity.ok(chartData);
    }
}
