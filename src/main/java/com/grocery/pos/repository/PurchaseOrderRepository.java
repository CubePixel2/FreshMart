package com.grocery.pos.repository;

import com.grocery.pos.model.PurchaseOrder;
import com.grocery.pos.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findAllByOrderByOrderDateDesc();

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    long countByStatus(PurchaseOrderStatus status);
}
