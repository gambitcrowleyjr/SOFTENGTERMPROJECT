package com.diner.inventory.repository;

import com.diner.inventory.model.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {
}
