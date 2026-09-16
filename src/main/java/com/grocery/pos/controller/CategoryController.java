package com.grocery.pos.controller;

import com.grocery.pos.model.Category;
import com.grocery.pos.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("newCategory", new Category());
        model.addAttribute("activeNav", "categories");
        return "categories/list";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("newCategory") Category category,
                               RedirectAttributes redirectAttributes) {
        if (category.getId() == null && categoryService.existsByName(category.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category '" + category.getName() + "' already exists!");
            return "redirect:/categories";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully!");
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete category with associated products.");
        }
        return "redirect:/categories";
    }
}
