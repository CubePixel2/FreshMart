package com.grocery.pos.controller;

import com.grocery.pos.dto.PurchaseInvoiceFormDto;
import com.grocery.pos.dto.PurchaseOrderFormDto;
import com.grocery.pos.model.*;
import com.grocery.pos.service.ProductService;
import com.grocery.pos.service.PurchaseService;
import com.grocery.pos.service.SupplierService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final ProductService productService;

    public PurchaseController(PurchaseService purchaseService,
                              SupplierService supplierService,
                              ProductService productService) {
        this.purchaseService = purchaseService;
        this.supplierService = supplierService;
        this.productService = productService;
    }

    // =========================================================================
    // 1. PURCHASE ORDERS
    // =========================================================================

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", purchaseService.findAllOrders());
        model.addAttribute("activeNav", "purchase-orders");
        return "purchases/orders/list";
    }

    @GetMapping("/orders/new")
    public String newOrderForm(Model model) {
        model.addAttribute("form", new PurchaseOrderFormDto());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("statuses", PurchaseOrderStatus.values());
        model.addAttribute("activeNav", "purchase-orders");
        return "purchases/orders/form";
    }

    @PostMapping("/orders/save")
    public String saveOrder(@ModelAttribute("form") PurchaseOrderFormDto form,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "Administrator";
            PurchaseOrder saved = purchaseService.createOrder(form, createdBy);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase Order #" + saved.getPoNumber() + " created successfully!");
            return "redirect:/purchases/orders/" + saved.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating purchase order: " + ex.getMessage());
            return "redirect:/purchases/orders/new";
        }
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return purchaseService.findOrderById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("statuses", PurchaseOrderStatus.values());
                    model.addAttribute("activeNav", "purchase-orders");
                    return "purchases/orders/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Purchase Order not found");
                    return "redirect:/purchases/orders";
                });
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam("status") PurchaseOrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            purchaseService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + ex.getMessage());
        }
        return "redirect:/purchases/orders/" + id;
    }

    @PostMapping("/orders/{id}/to-invoice")
    public String convertOrderToInvoice(@PathVariable Long id,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "Administrator";
            PurchaseInvoice invoice = purchaseService.createInvoiceFromOrder(id, createdBy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Purchase Invoice #" + invoice.getInvoiceNumber() + " generated and stock added to inventory!");
            return "redirect:/purchases/invoices/" + invoice.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating invoice from order: " + ex.getMessage());
            return "redirect:/purchases/orders/" + id;
        }
    }

    // =========================================================================
    // 2. PURCHASE INVOICES
    // =========================================================================

    @GetMapping("/invoices")
    public String listInvoices(Model model) {
        model.addAttribute("invoices", purchaseService.findAllInvoices());
        model.addAttribute("activeNav", "purchase-invoices");
        return "purchases/invoices/list";
    }

    @GetMapping("/invoices/new")
    public String newInvoiceForm(@RequestParam(value = "orderId", required = false) Long orderId,
                                 Model model) {
        PurchaseInvoiceFormDto form = new PurchaseInvoiceFormDto();
        if (orderId != null) {
            form.setPurchaseOrderId(orderId);
            purchaseService.findOrderById(orderId).ifPresent(order -> {
                form.setSupplierId(order.getSupplier().getId());
                order.getItems().forEach(it -> {
                    form.getProductIds().add(it.getProduct().getId());
                    form.getQuantities().add(it.getOrderedQty());
                    form.getUnitCosts().add(it.getUnitCost());
                });
            });
        }

        model.addAttribute("form", form);
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("orders", purchaseService.findAllOrders());
        model.addAttribute("paymentStatuses", PurchasePaymentStatus.values());
        model.addAttribute("activeNav", "purchase-invoices");
        return "purchases/invoices/form";
    }

    @PostMapping("/invoices/save")
    public String saveInvoice(@ModelAttribute("form") PurchaseInvoiceFormDto form,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "Administrator";
            PurchaseInvoice saved = purchaseService.createInvoice(form, createdBy);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Purchase Invoice #" + saved.getInvoiceNumber() + " saved successfully!" + 
                    (saved.isPosted() ? " (Stock inward posted to inventory)" : ""));
            return "redirect:/purchases/invoices/" + saved.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating purchase invoice: " + ex.getMessage());
            return "redirect:/purchases/invoices/new";
        }
    }

    @GetMapping("/invoices/{id}")
    public String viewInvoiceDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return purchaseService.findInvoiceById(id)
                .map(invoice -> {
                    model.addAttribute("invoice", invoice);
                    model.addAttribute("paymentStatuses", PurchasePaymentStatus.values());
                    model.addAttribute("activeNav", "purchase-invoices");
                    return "purchases/invoices/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Purchase Invoice not found");
                    return "redirect:/purchases/invoices";
                });
    }

    @PostMapping("/invoices/{id}/post")
    public String postInvoiceToInventory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PurchaseInvoice posted = purchaseService.postInvoice(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Goods from Invoice #" + posted.getInvoiceNumber() + " successfully received and added to product stock quantities!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error posting invoice: " + ex.getMessage());
        }
        return "redirect:/purchases/invoices/" + id;
    }

    @PostMapping("/invoices/{id}/payment")
    public String updatePaymentStatus(@PathVariable Long id,
                                      @RequestParam("paymentStatus") PurchasePaymentStatus paymentStatus,
                                      RedirectAttributes redirectAttributes) {
        try {
            purchaseService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Payment status updated to " + paymentStatus.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating payment status: " + ex.getMessage());
        }
        return "redirect:/purchases/invoices/" + id;
    }
}
