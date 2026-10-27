package com.grocery.pos.repository;

import com.grocery.pos.model.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    Optional<SaleReturn> findByReturnNumber(String returnNumber);

    List<SaleReturn> findAllByOrderByReturnDateDesc();

    List<SaleReturn> findByOriginalSaleId(Long saleId);

    List<SaleReturn> findByReturnDateBetweenOrderByReturnDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(sr.refundAmount), 0) FROM SaleReturn sr WHERE sr.returnDate BETWEEN :start AND :end")
    BigDecimal sumRefundAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(sr.refundAmount), 0) FROM SaleReturn sr")
    BigDecimal sumTotalRefundAmount();
}
