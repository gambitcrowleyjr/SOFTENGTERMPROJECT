package com.diner.inventory.repository;

import com.diner.inventory.model.SupplyOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyOrderItemRepository extends JpaRepository<SupplyOrderItem, Long> {
}
