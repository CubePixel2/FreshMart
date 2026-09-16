package com.grocery.pos.repository;

import com.grocery.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<Product> findByCategory_Id(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockThreshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minStockThreshold")
    long countLowStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) AND (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY p.name ASC")
    List<Product> filterProducts(@Param("categoryId") Long categoryId, @Param("query") String query);
}
