package com.diner.inventory.repository;

import com.diner.inventory.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT m FROM MenuItem m WHERE m.deleted = false OR m.deleted IS NULL")
    List<MenuItem> findByDeletedFalse();
}
