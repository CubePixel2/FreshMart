package com.grocery.pos.repository;

import com.grocery.pos.model.ProductReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReturnRepository extends JpaRepository<ProductReturn, Long> {

    Optional<ProductReturn> findByReturnNumber(String returnNumber);

    List<ProductReturn> findAllByOrderByReturnDateDesc();

    List<ProductReturn> findBySupplierId(Long supplierId);

    List<ProductReturn> findByReturnDateBetweenOrderByReturnDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(pr.totalRefundAmount), 0) FROM ProductReturn pr WHERE pr.returnDate BETWEEN :start AND :end")
    BigDecimal sumTotalRefundAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(pr.totalRefundAmount), 0) FROM ProductReturn pr")
    BigDecimal sumTotalRefundAmount();
}
