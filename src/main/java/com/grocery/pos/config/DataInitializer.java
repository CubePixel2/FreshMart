package com.grocery.pos.config;

import com.grocery.pos.model.*;
import com.grocery.pos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           SaleRepository saleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already seeded
        }

        // 1. Seed Users (Standard Administrator & Staff roles)
        User admin = new User("admin", passwordEncoder.encode("admin123"), "Administrator", "admin@freshmart.in", Role.ROLE_ADMIN);
        User staff = new User("staff", passwordEncoder.encode("staff123"), "Staff Cashier", "staff@freshmart.in", Role.ROLE_STAFF);
        userRepository.saveAll(Arrays.asList(admin, staff));

        // 2. Seed Categories (Kerala context)
        Category catProduce = categoryRepository.save(new Category("Fresh Produce & Fruits", "Fresh Kerala vegetables, fruits, and greens", "bi-apple", "#10b981"));
        Category catDairy = categoryRepository.save(new Category("Dairy & Milma", "Milma milk, curds, paneer, and farm eggs", "bi-egg-fried", "#3b82f6"));
        Category catRiceOils = categoryRepository.save(new Category("Rice, Grains & Oils", "Palakkadan Matta, Basmati, and Kera Coconut Oil", "bi-inbox", "#f59e0b"));
        Category catBakery = categoryRepository.save(new Category("Bakery & Malabar Snacks", "Malabar parotta, banana chips, and bakery breads", "bi-cake", "#ec4899"));
        Category catSpices = categoryRepository.save(new Category("Kerala Spices & Tea", "Idukki Cardamom, Wayanad Black Pepper, and Munnar Tea", "bi-cup-straw", "#8b5cf6"));
        Category catBeverages = categoryRepository.save(new Category("Beverages & Drinks", "Filter coffee, tenders, juices, and mineral water", "bi-cup-straw", "#06b6d4"));
        Category catHousehold = categoryRepository.save(new Category("Household & Cleaning", "Detergents, soaps, and home cleaning essentials", "bi-stars", "#64748b"));

        // 3. Seed Kerala Suppliers
        Supplier supMilma = supplierRepository.save(new Supplier("Milma ERCMPU Ltd", "Gopinath Pillai", "orders@milma-kochi.com", "+91 484 2577123", "Edappally Dairy Campus, Kochi, Kerala 682024"));
        Supplier supKera = supplierRepository.save(new Supplier("Kera Coconut Producers Fed", "Manoj Varma", "sales@keracoconut.org", "+91 495 2441020", "Beach Road, Kozhikode, Kerala 673032"));
        Supplier supPalakkad = supplierRepository.save(new Supplier("Palakkad Rice & Agro Mills", "Sivadasan Nair", "contact@palakkadmatta.com", "+91 491 2548890", "Industrial Estate, Kanjikode, Palakkad, Kerala 678621"));
        Supplier supWayanad = supplierRepository.save(new Supplier("Wayanad Bio Spices Co-op", "Biju Mathew", "info@wayanadspices.in", "+91 4936 202450", "Main Bazaar, Kalpetta, Wayanad, Kerala 673121"));
        Supplier supClean = supplierRepository.save(new Supplier("Kerala Hygiene Essentials", "Afsal Rahman", "orders@keralahygiene.com", "+91 487 2321100", "Kuruppam Road, Thrissur, Kerala 680001"));

        // 4. Seed Realistic Kerala Grocery Products with realistic INR (₹) prices
        // Note: 4 items intentionally set below or at minStockThreshold to demonstrate Low Stock Alerts with pulse animations!
        List<Product> products = Arrays.asList(
                new Product("Kerala Nendran Bananas (ഏത്തപ്പഴം)", "89010001", catProduce, supPalakkad, new BigDecimal("45.00"), new BigDecimal("65.00"), 40, 15, "kg", null, "Ripe yellow sweet organic Kerala Nendran bananas"),
                new Product("Farm Fresh Tapioca / Kappa (കപ്പ)", "89010002", catProduce, supPalakkad, new BigDecimal("25.00"), new BigDecimal("40.00"), 35, 10, "kg", null, "Freshly harvested white fibrous cooking tapioca"),
                new Product("Milma Rich Whole Milk 500ml", "89010003", catDairy, supMilma, new BigDecimal("26.00"), new BigDecimal("30.00"), 30, 10, "pouch", null, "Pasteurized homogenized standardized milk (Milma Blue)"),
                new Product("Farm Fresh Country Eggs / നാടൻ മുട്ട (6pk)", "89010004", catDairy, supMilma, new BigDecimal("42.00"), new BigDecimal("55.00"), 4, 12, "pack", null, "Rich nutrient brown free-range eggs [LOW STOCK]"),
                new Product("Milma Malabar Curd 500g", "89010005", catDairy, supMilma, new BigDecimal("32.00"), new BigDecimal("38.00"), 3, 10, "pouch", null, "Pure cultured creamy fresh curd [LOW STOCK]"),
                new Product("Pure Kera Coconut Oil 1 Litre Pouch", "89010006", catRiceOils, supKera, new BigDecimal("180.00"), new BigDecimal("225.00"), 25, 8, "pouch", null, "100% pure roasted double filtered Kerala coconut oil"),
                new Product("Palakkadan Vadi Matta Rice 5kg Bag", "89010007", catRiceOils, supPalakkad, new BigDecimal("260.00"), new BigDecimal("320.00"), 2, 8, "bag", null, "Authentic nutrient-dense Kerala red rice [LOW STOCK]"),
                new Product("Royal Aged Basmati Rice 1kg", "89010008", catRiceOils, supPalakkad, new BigDecimal("110.00"), new BigDecimal("145.00"), 22, 8, "pack", null, "Aromatic long grain premium basmati rice"),
                new Product("Malabar Frozen Parotta (5 pcs pack)", "89010009", catBakery, supMilma, new BigDecimal("55.00"), new BigDecimal("75.00"), 18, 6, "pack", null, "Flaky multi-layered authentic Malabar parotta"),
                new Product("Crispy Coconut Banana Chips 200g (ഉപ്പേരി)", "89010010", catBakery, supKera, new BigDecimal("85.00"), new BigDecimal("120.00"), 25, 10, "pack", null, "Crisp golden raw banana chips fried in pure coconut oil"),
                new Product("Whole Wheat Sliced Bread 400g", "89010011", catBakery, supMilma, new BigDecimal("35.00"), new BigDecimal("45.00"), 20, 8, "loaf", null, "Freshly baked fiber-rich brown sandwich bread"),
                new Product("Idukki Green Cardamom 100g (ഏലക്ക)", "89010012", catSpices, supWayanad, new BigDecimal("280.00"), new BigDecimal("360.00"), 5, 8, "pack", null, "Bold grade premium aromatic cardamom pods [LOW STOCK]"),
                new Product("Wayanad Black Pepper Whole 100g (കുരുമുളക്)", "89010013", catSpices, supWayanad, new BigDecimal("85.00"), new BigDecimal("115.00"), 30, 10, "pack", null, "Sun-dried bold pungent black peppercorns"),
                new Product("Kannan Devan Munnar Tea 500g", "89010014", catSpices, supWayanad, new BigDecimal("170.00"), new BigDecimal("215.00"), 28, 8, "pack", null, "Strong aroma CTC plantation tea from High Range Munnar"),
                new Product("Wayanad Robusta Filter Coffee 250g", "89010015", catBeverages, supWayanad, new BigDecimal("125.00"), new BigDecimal("165.00"), 22, 6, "pack", null, "80:20 rich aromatic south Indian filter blend"),
                new Product("Pure Natural Tender Coconut Water 200ml", "89010016", catBeverages, supKera, new BigDecimal("35.00"), new BigDecimal("50.00"), 35, 12, "bottle", null, "Chilled pure natural Kerala elaneer"),
                new Product("Coconut Shell Charcoal Dish Bar 250g", "89010017", catHousehold, supClean, new BigDecimal("22.00"), new BigDecimal("30.00"), 40, 10, "bar", null, "Natural grease-cutting scrub dishwash bar"),
                new Product("Herbal Floor Cleaner Lemon & Pine 1L", "89010018", catHousehold, supClean, new BigDecimal("120.00"), new BigDecimal("160.00"), 25, 8, "bottle", null, "Natural insect repellent anti-bacterial floor sanitizer")
        );
        productRepository.saveAll(products);

        // 5. Seed Past Sales / Invoices across past 6 days & today with Indian names & UPI payments
        LocalDateTime now = LocalDateTime.now();
        createSampleSale("INV-" + now.minusDays(5).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0001",
                now.minusDays(5).withHour(10).withMinute(30), admin, "Suresh Kumar", "+91 98470 11223", PaymentMethod.UPI,
                Arrays.asList(new ItemSeed(products.get(0), 2), new ItemSeed(products.get(2), 3), new ItemSeed(products.get(5), 1)),
                BigDecimal.ZERO);

        createSampleSale("INV-" + now.minusDays(4).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0002",
                now.minusDays(4).withHour(14).withMinute(15), staff, "Priya Nair", "+91 94471 22334", PaymentMethod.CASH,
                Arrays.asList(new ItemSeed(products.get(8), 2), new ItemSeed(products.get(9), 1), new ItemSeed(products.get(13), 1)),
                BigDecimal.valueOf(10.00));

        createSampleSale("INV-" + now.minusDays(3).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0003",
                now.minusDays(3).withHour(16).withMinute(45), staff, "Mohd. Faisal", "+91 98950 33445", PaymentMethod.UPI,
                Arrays.asList(new ItemSeed(products.get(6), 1), new ItemSeed(products.get(12), 2), new ItemSeed(products.get(14), 1)),
                BigDecimal.ZERO);

        createSampleSale("INV-" + now.minusDays(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0004",
                now.minusDays(2).withHour(11).withMinute(20), admin, "Anjali Thomas", "+91 97455 44556", PaymentMethod.CARD,
                Arrays.asList(new ItemSeed(products.get(1), 3), new ItemSeed(products.get(3), 1), new ItemSeed(products.get(7), 2)),
                BigDecimal.valueOf(15.00));

        createSampleSale("INV-" + now.minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0005",
                now.minusDays(1).withHour(18).withMinute(10), staff, "Deepak Varma", "+91 94000 55667", PaymentMethod.UPI,
                Arrays.asList(new ItemSeed(products.get(5), 2), new ItemSeed(products.get(10), 2), new ItemSeed(products.get(15), 3)),
                BigDecimal.ZERO);

        createSampleSale("INV-" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0006",
                now.minusHours(3).withMinute(15), admin, "Lakshmi Pillai", "+91 98460 66778", PaymentMethod.UPI,
                Arrays.asList(new ItemSeed(products.get(0), 3), new ItemSeed(products.get(8), 2), new ItemSeed(products.get(13), 1)),
                BigDecimal.valueOf(20.00));

        createSampleSale("INV-" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0007",
                now.minusHours(1).withMinute(40), staff, "Vishnu Nambiar", "+91 94465 77889", PaymentMethod.CASH,
                Arrays.asList(new ItemSeed(products.get(2), 4), new ItemSeed(products.get(9), 2), new ItemSeed(products.get(14), 1)),
                BigDecimal.ZERO);
    }

    private static class ItemSeed {
        Product product;
        int qty;
        ItemSeed(Product p, int q) { this.product = p; this.qty = q; }
    }

    private void createSampleSale(String invoiceNo, LocalDateTime date, User cashier, String customer, String phone,
                                  PaymentMethod method, List<ItemSeed> items, BigDecimal discount) {
        Sale sale = new Sale();
        sale.setInvoiceNumber(invoiceNo);
        sale.setSaleDate(date);
        sale.setCashier(cashier);
        sale.setCashierName(cashier != null && cashier.getRole() == Role.ROLE_ADMIN ? "Counter #1" : "Counter #2");
        sale.setCustomerName(customer);
        sale.setCustomerPhone(phone);
        sale.setPaymentMethod(method);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemSeed itemSeed : items) {
            BigDecimal price = itemSeed.product.getSellingPrice();
            SaleItem saleItem = new SaleItem(sale, itemSeed.product, itemSeed.product.getName(), price, itemSeed.qty);
            subtotal = subtotal.add(saleItem.getSubtotal());
            sale.addItem(saleItem);
        }

        sale.setSubtotal(subtotal);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        sale.setTaxRate(BigDecimal.valueOf(5.00)); // 5% GST (2.5% CGST + 2.5% SGST)
        sale.setTaxAmount(tax);
        sale.setDiscountAmount(discount);
        BigDecimal grandTotal = subtotal.add(tax).subtract(discount);
        sale.setGrandTotal(grandTotal);
        sale.setAmountPaid(grandTotal);
        sale.setChangeReturned(BigDecimal.ZERO);

        saleRepository.save(sale);
    }
}
