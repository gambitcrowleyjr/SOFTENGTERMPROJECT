package com.diner.inventory.service;

import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ManagerService managerService;
    private final com.diner.inventory.repository.WasteRecordRepository wasteRecordRepository;

    public List<InventoryItem> getAllInventory() {
        return inventoryItemRepository.findAll();
    }

    @Transactional
    public void recordWaste(Long itemId, Double amount) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setStockLevel(Math.max(0, item.getStockLevel() - amount));
        
        // Save waste record for reporting
        com.diner.inventory.model.WasteRecord record = new com.diner.inventory.model.WasteRecord();
        record.setInventoryItem(item);
        record.setQuantity(amount);
        record.setCostAtTime(item.getCurrentPrice());
        wasteRecordRepository.save(record);

        inventoryItemRepository.save(item);
        managerService.checkAndCreateAlert(item);
    }

    @Transactional
    public void addStock(Long itemId, Double amount, Double pricePerUnit) {
        if (amount != null && amount < 0) {
            throw new RuntimeException("Cannot add negative stock.");
        }
        
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setStockLevel(item.getStockLevel() + amount);
        
        if (pricePerUnit != null) {
            // Check for alert before updating saved price
            managerService.checkPriceAlert(item, pricePerUnit);
            
            // Always update to most recent order price
            item.setPreviousPrice(item.getCurrentPrice());
            item.setCurrentPrice(pricePerUnit);
        }
        
        inventoryItemRepository.save(item);
        managerService.checkAndCreateAlert(item);
    }

    @Transactional
    public void createInventoryItem(InventoryItem item) {
        inventoryItemRepository.save(item);
        managerService.checkAndCreateAlert(item);
    }
}
