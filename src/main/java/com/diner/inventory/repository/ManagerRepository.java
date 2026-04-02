package com.diner.inventory.repository;

import com.diner.inventory.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    Optional<Manager> findByAuthCode(String authCode);
}
