package com.grocery.pos.service;

import com.grocery.pos.dto.CartItemDto;
import com.grocery.pos.dto.ChartDataDto;
import com.grocery.pos.dto.CheckoutRequestDto;
import com.grocery.pos.dto.DashboardSummaryDto;
import com.grocery.pos.model.*;
import com.grocery.pos.repository.CategoryRepository;
import com.grocery.pos.repository.ProductRepository;
import com.grocery.pos.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       ProductService productService,
                       CategoryRepository categoryRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    public List<Sale> findAll() {
        return saleRepository.findAllByOrderBySaleDateDesc();
    }

    public List<Sale> findRecentSales() {
        return saleRepository.findTop10ByOrderBySaleDateDesc();
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    public Optional<Sale> findByInvoiceNumber(String invoiceNumber) {
        return saleRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional
    public Sale processCheckout(CheckoutRequestDto request, User cashier) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout.");
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber(generateInvoiceNumber());
        sale.setSaleDate(LocalDateTime.now());
        sale.setCashier(cashier);
        if (request.getCashierName() != null && !request.getCashierName().trim().isEmpty()) {
            sale.setCashierName(request.getCashierName().trim());
        } else if (cashier != null && cashier.getFullName() != null && !cashier.getFullName().isBlank()) {
            sale.setCashierName(cashier.getFullName());
        } else {
            sale.setCashierName("Cashier");
        }
        sale.setCustomerName((request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty())
                ? request.getCustomerName().trim() : "Walk-in Customer");
        sale.setCustomerPhone(request.getCustomerPhone());
        sale.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH);
        sale.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: ID " + itemDto.getProductId()));

            // Deduct stock in DB
            productService.deductStock(product.getId(), itemDto.getQuantity());

            BigDecimal itemPrice = product.getSellingPrice();
            SaleItem saleItem = new SaleItem(sale, product, product.getName(), itemPrice, itemDto.getQuantity());
            subtotal = subtotal.add(saleItem.getSubtotal());
            sale.addItem(saleItem);
        }

        sale.setSubtotal(subtotal);

        // Tax 5%
        BigDecimal taxRate = BigDecimal.valueOf(5.00);
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        sale.setTaxRate(taxRate);
        sale.setTaxAmount(taxAmount);

        // Discount
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal.add(taxAmount)) > 0) {
            discount = subtotal.add(taxAmount);
        }
        sale.setDiscountAmount(discount);

        // Grand Total = Subtotal + Tax - Discount
        BigDecimal grandTotal = subtotal.add(taxAmount).subtract(discount);
        sale.setGrandTotal(grandTotal);

        // Payment & Change
        BigDecimal paid = request.getAmountPaid() != null ? request.getAmountPaid() : grandTotal;
        sale.setAmountPaid(paid);
        if (paid.compareTo(grandTotal) >= 0) {
            sale.setChangeReturned(paid.subtract(grandTotal));
        } else {
            sale.setChangeReturned(BigDecimal.ZERO);
        }

        return saleRepository.save(sale);
    }

    public DashboardSummaryDto getDashboardSummary() {
        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countLowStockProducts();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todaySalesCount = saleRepository.countSalesSince(startOfDay);
        BigDecimal todayRevenue = saleRepository.sumRevenueSince(startOfDay);
        BigDecimal totalRevenue = saleRepository.sumTotalRevenue();

        return new DashboardSummaryDto(totalProducts, lowStockCount, todaySalesCount, todayRevenue, totalRevenue);
    }

    public ChartDataDto getChartData() {
        ChartDataDto chartData = new ChartDataDto();

        // 1. Last 7 days sales data
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> salesTotals = new ArrayList<>();

        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();
        List<Sale> recentSales = saleRepository.findBySaleDateAfter(sevenDaysAgo);

        for (Sale sale : recentSales) {
            LocalDate date = sale.getSaleDate().toLocalDate();
            dailyTotals.put(date, dailyTotals.getOrDefault(date, BigDecimal.ZERO).add(sale.getGrandTotal()));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.format(formatter));
            salesTotals.add(dailyTotals.getOrDefault(day, BigDecimal.ZERO));
        }

        chartData.setSalesDates(dates);
        chartData.setSalesTotals(salesTotals);

        // 2. Category distribution
        List<Category> categories = categoryRepository.findAll();
        List<String> catLabels = new ArrayList<>();
        List<Integer> catCounts = new ArrayList<>();
        List<String> catColors = new ArrayList<>();

        for (Category cat : categories) {
            catLabels.add(cat.getName());
            catCounts.add(cat.getProducts() != null ? cat.getProducts().size() : 0);
            catColors.add(cat.getBadgeColor() != null ? cat.getBadgeColor() : "#10b981");
        }

        chartData.setCategoryLabels(catLabels);
        chartData.setCategoryCounts(catCounts);
        chartData.setCategoryColors(catColors);

        return chartData;
    }

    private synchronized String generateInvoiceNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = saleRepository.count();
        int seq = (int) ((count % 10000) + 1);
        return String.format("INV-%s-%04d", dateStr, seq);
    }
}
