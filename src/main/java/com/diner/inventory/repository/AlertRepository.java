package com.diner.inventory.repository;

import com.diner.inventory.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByIsReadFalse();
    Optional<Alert> findByInventoryItemIdAndIsReadFalse(Long inventoryItemId);
}
