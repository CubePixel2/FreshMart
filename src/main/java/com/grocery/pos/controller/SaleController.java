package com.grocery.pos.controller;

import com.grocery.pos.model.Sale;
import com.grocery.pos.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public String listSales(Model model) {
        List<Sale> sales = saleService.findAll();
        model.addAttribute("sales", sales);
        model.addAttribute("activeNav", "sales");
        return "sales/list";
    }

    @GetMapping("/{id}")
    public String viewSaleDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return saleService.findById(id).map(sale -> {
            model.addAttribute("sale", sale);
            model.addAttribute("activeNav", "sales");
            return "sales/invoice";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Invoice not found!");
            return "redirect:/sales";
        });
    }

    @GetMapping("/{id}/invoice")
    public String printInvoice(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return saleService.findById(id).map(sale -> {
            model.addAttribute("sale", sale);
            model.addAttribute("activeNav", "sales");
            model.addAttribute("autoPrint", true);
            return "sales/invoice";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Invoice not found!");
            return "redirect:/sales";
        });
    }
}
