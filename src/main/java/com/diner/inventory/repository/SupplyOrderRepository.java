package com.diner.inventory.repository;

import com.diner.inventory.model.SupplyOrder;
import com.diner.inventory.model.SupplyOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {
    List<SupplyOrder> findByStatus(SupplyOrderStatus status);
    List<SupplyOrder> findAllByOrderByCreatedAtDesc();
}
