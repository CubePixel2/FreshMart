package com.grocery.pos.controller;

import com.grocery.pos.model.Supplier;
import com.grocery.pos.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String listSuppliers(Model model) {
        List<Supplier> suppliers = supplierService.findAll();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("newSupplier", new Supplier());
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/list";
    }

    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute("newSupplier") Supplier supplier,
                               RedirectAttributes redirectAttributes) {
        supplierService.save(supplier);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier saved successfully!");
        return "redirect:/suppliers";
    }

    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete supplier with associated products.");
        }
        return "redirect:/suppliers";
    }
}
