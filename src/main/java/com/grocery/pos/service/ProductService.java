package com.grocery.pos.service;

import com.grocery.pos.model.Product;
import com.grocery.pos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public boolean existsByBarcode(String barcode) {
        return productRepository.existsByBarcode(barcode);
    }

    public boolean existsByBarcodeAndIdNot(String barcode, Long id) {
        Optional<Product> existing = productRepository.findByBarcode(barcode);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public long countLowStockProducts() {
        return productRepository.countLowStockProducts();
    }

    public long count() {
        return productRepository.count();
    }

    public List<Product> filterProducts(Long categoryId, String query) {
        String sanitizedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return productRepository.filterProducts(categoryId, sanitizedQuery);
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchProducts(query.trim());
    }

    public synchronized Product deductStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (currentStock < quantity) {
            throw new IllegalStateException("Insufficient stock for " + product.getName() + 
                    ". Available: " + currentStock + ", Requested: " + quantity);
        }

        product.setStockQuantity(currentStock - quantity);
        return productRepository.save(product);
    }

    public Product restock(Long productId, int additionalQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + additionalQuantity);
        return productRepository.save(product);
    }
}
