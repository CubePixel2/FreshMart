package com.grocery.pos.controller;

import com.grocery.pos.dto.ProductReturnFormDto;
import com.grocery.pos.dto.SaleReturnFormDto;
import com.grocery.pos.model.*;
import com.grocery.pos.service.ProductReturnService;
import com.grocery.pos.service.ProductService;
import com.grocery.pos.service.SaleReturnService;
import com.grocery.pos.service.SaleService;
import com.grocery.pos.service.SupplierService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/returns")
public class ReturnController {

    private final SaleReturnService saleReturnService;
    private final ProductReturnService productReturnService;
    private final SaleService saleService;
    private final SupplierService supplierService;
    private final ProductService productService;

    public ReturnController(SaleReturnService saleReturnService,
                            ProductReturnService productReturnService,
                            SaleService saleService,
                            SupplierService supplierService,
                            ProductService productService) {
        this.saleReturnService = saleReturnService;
        this.productReturnService = productReturnService;
        this.saleService = saleService;
        this.supplierService = supplierService;
        this.productService = productService;
    }

    // =========================================================================
    // 1. SALE RETURNS (CUSTOMER RETURNS)
    // =========================================================================

    @GetMapping("/sales")
    public String listSaleReturns(Model model) {
        model.addAttribute("returns", saleReturnService.findAll());
        model.addAttribute("activeNav", "sale-returns");
        return "returns/sales/list";
    }

    @GetMapping("/sales/new")
    public String newSaleReturnForm(@RequestParam(value = "saleId", required = false) Long saleId,
                                    Model model) {
        SaleReturnFormDto form = new SaleReturnFormDto();
        if (saleId != null) {
            form.setSaleId(saleId);
            saleService.findById(saleId).ifPresent(sale -> {
                model.addAttribute("selectedSale", sale);
            });
        }

        model.addAttribute("form", form);
        model.addAttribute("recentSales", saleService.findRecentSales());
        model.addAttribute("allSales", saleService.findAll());
        model.addAttribute("activeNav", "sale-returns");
        return "returns/sales/form";
    }

    @PostMapping("/sales/save")
    public String saveSaleReturn(@ModelAttribute("form") SaleReturnFormDto form,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String processedBy = authentication != null ? authentication.getName() : "Cashier";
            SaleReturn saved = saleReturnService.processSaleReturn(form, processedBy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Sale Return #" + saved.getReturnNumber() + " processed successfully! Stock restored to inventory.");
            return "redirect:/returns/sales/" + saved.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing sale return: " + ex.getMessage());
            return "redirect:/returns/sales/new" + (form.getSaleId() != null ? "?saleId=" + form.getSaleId() : "");
        }
    }

    @GetMapping("/sales/{id}")
    public String viewSaleReturnDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return saleReturnService.findById(id)
                .map(ret -> {
                    model.addAttribute("returnObj", ret);
                    model.addAttribute("activeNav", "sale-returns");
                    return "returns/sales/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Sale Return record not found");
                    return "redirect:/returns/sales";
                });
    }

    // =========================================================================
    // 2. PRODUCT RETURNS (TO SUPPLIERS)
    // =========================================================================

    @GetMapping("/products")
    public String listProductReturns(Model model) {
        model.addAttribute("returns", productReturnService.findAll());
        model.addAttribute("activeNav", "product-returns");
        return "returns/products/list";
    }

    @GetMapping("/products/new")
    public String newProductReturnForm(Model model) {
        model.addAttribute("form", new ProductReturnFormDto());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("activeNav", "product-returns");
        return "returns/products/form";
    }

    @PostMapping("/products/save")
    public String saveProductReturn(@ModelAttribute("form") ProductReturnFormDto form,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            String processedBy = authentication != null ? authentication.getName() : "Administrator";
            ProductReturn saved = productReturnService.processProductReturn(form, processedBy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Product Return #" + saved.getReturnNumber() + " to supplier confirmed! Inventory deducted.");
            return "redirect:/returns/products/" + saved.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing product return: " + ex.getMessage());
            return "redirect:/returns/products/new";
        }
    }

    @GetMapping("/products/{id}")
    public String viewProductReturnDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productReturnService.findById(id)
                .map(ret -> {
                    model.addAttribute("returnObj", ret);
                    model.addAttribute("activeNav", "product-returns");
                    return "returns/products/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Product Return record not found");
                    return "redirect:/returns/products";
                });
    }
}
