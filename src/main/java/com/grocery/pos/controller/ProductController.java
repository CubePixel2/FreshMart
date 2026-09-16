package com.grocery.pos.controller;

import com.grocery.pos.model.Category;
import com.grocery.pos.model.Product;
import com.grocery.pos.model.Supplier;
import com.grocery.pos.service.CategoryService;
import com.grocery.pos.service.ProductService;
import com.grocery.pos.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             SupplierService supplierService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Product> products = productService.filterProducts(categoryId, search);
        List<Category> categories = categoryService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("activeNav", "products");

        return "products/list";
    }

    @GetMapping("/low-stock")
    public String lowStockProducts(Model model) {
        List<Product> lowStock = productService.getLowStockProducts();
        model.addAttribute("products", lowStock);
        model.addAttribute("totalCount", lowStock.size());
        model.addAttribute("activeNav", "low-stock");
        return "products/low-stock";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("isNew", true);
        model.addAttribute("activeNav", "products");
        return "products/form";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productService.findById(id).map(product -> {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAll());
            model.addAttribute("isNew", false);
            model.addAttribute("activeNav", "products");
            return "products/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found!");
            return "redirect:/products";
        });
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              RedirectAttributes redirectAttributes) {
        // Barcode uniqueness check
        if (product.getId() == null) {
            if (productService.existsByBarcode(product.getBarcode())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Barcode '" + product.getBarcode() + "' is already in use!");
                return "redirect:/products/new";
            }
        } else {
            if (productService.existsByBarcodeAndIdNot(product.getBarcode(), product.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Barcode '" + product.getBarcode() + "' is already in use by another product!");
                return "redirect:/products/edit/" + product.getId();
            }
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product '" + product.getName() + "' saved successfully!");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete product because it has linked sales or dependencies.");
        }
        return "redirect:/products";
    }

    @PostMapping("/restock")
    public String restockProduct(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 @RequestParam(value = "redirect", defaultValue = "low-stock") String redirect,
                                 RedirectAttributes redirectAttributes) {
        try {
            Product p = productService.restock(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully added " + quantity + " " + p.getUnit() + " to " + p.getName() + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to restock: " + ex.getMessage());
        }

        if ("products".equals(redirect)) {
            return "redirect:/products";
        }
        return "redirect:/products/low-stock";
    }
}
