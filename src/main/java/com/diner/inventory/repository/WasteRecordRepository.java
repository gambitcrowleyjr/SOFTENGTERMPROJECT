package com.diner.inventory.repository;

import com.diner.inventory.model.WasteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WasteRecordRepository extends JpaRepository<WasteRecord, Long> {
    List<WasteRecord> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);
}
