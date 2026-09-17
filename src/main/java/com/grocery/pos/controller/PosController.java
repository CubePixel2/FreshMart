package com.grocery.pos.controller;

import com.grocery.pos.dto.ApiResponseDto;
import com.grocery.pos.dto.CheckoutRequestDto;
import com.grocery.pos.model.*;
import com.grocery.pos.service.CategoryService;
import com.grocery.pos.service.ProductService;
import com.grocery.pos.service.SaleService;
import com.grocery.pos.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pos")
public class PosController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SaleService saleService;
    private final UserService userService;

    public PosController(ProductService productService,
                         CategoryService categoryService,
                         SaleService saleService,
                         UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.saleService = saleService;
        this.userService = userService;
    }

    @GetMapping
    public String posTerminal(Model model) {
        List<Category> categories = categoryService.findAll();
        List<Product> products = productService.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("activeNav", "pos");

        return "pos/index";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<Product>> getProductsForPos(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "query", required = false) String query) {
        List<Product> products = productService.filterProducts(categoryId, query);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/api/checkout")
    @ResponseBody
    public ResponseEntity<ApiResponseDto> checkout(
            @RequestBody CheckoutRequestDto request,
            Authentication authentication) {
        try {
            User cashier = null;
            if (authentication != null) {
                cashier = userService.findByUsername(authentication.getName()).orElse(null);
            }

            Sale savedSale = saleService.processCheckout(request, cashier);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("saleId", savedSale.getId());
            responseData.put("invoiceNumber", savedSale.getInvoiceNumber());
            responseData.put("cashierName", savedSale.getCashierName());
            responseData.put("grandTotal", savedSale.getGrandTotal());
            responseData.put("changeReturned", savedSale.getChangeReturned());
            responseData.put("invoiceUrl", "/sales/" + savedSale.getId() + "/invoice");

            return ResponseEntity.ok(ApiResponseDto.ok("Sale completed successfully!", responseData));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("An error occurred while processing checkout: " + ex.getMessage()));
        }
    }
}
