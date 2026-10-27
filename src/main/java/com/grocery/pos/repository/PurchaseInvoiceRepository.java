package com.grocery.pos.repository;

import com.grocery.pos.model.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByInvoiceNumber(String invoiceNumber);

    List<PurchaseInvoice> findAllByOrderByInvoiceDateDesc();

    List<PurchaseInvoice> findBySupplierId(Long supplierId);

    List<PurchaseInvoice> findByInvoiceDateBetweenOrderByInvoiceDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.grandTotal), 0) FROM PurchaseInvoice p WHERE p.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumGrandTotalBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseInvoice p WHERE p.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.taxAmount), 0) FROM PurchaseInvoice p WHERE p.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTaxAmountBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
