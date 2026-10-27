package com.grocery.pos.repository;

import com.grocery.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    List<Sale> findAllByOrderBySaleDateDesc();

    List<Sale> findTop10ByOrderBySaleDateDesc();

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :startOfDay")
    long countSalesSince(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.saleDate >= :startOfDay")
    BigDecimal sumRevenueSince(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    BigDecimal sumGrandTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.subtotal), 0) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    BigDecimal sumSubtotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.taxAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    BigDecimal sumTaxAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.discountAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    BigDecimal sumDiscountAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Sale> findBySaleDateAfter(LocalDateTime since);
}
