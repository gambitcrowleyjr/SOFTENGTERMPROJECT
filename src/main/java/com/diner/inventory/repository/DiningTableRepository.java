package com.diner.inventory.repository;

import com.diner.inventory.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    Optional<DiningTable> findByTableNumber(String tableNumber);
    List<DiningTable> findBySectionIsNull();
    List<DiningTable> findBySectionIsNotNull();
}
