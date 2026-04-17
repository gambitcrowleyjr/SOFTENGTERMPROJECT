package com.diner.inventory.service;

import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.diner.inventory.model.InventorySnapshot;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ManagerService managerService;

    private final com.diner.inventory.repository.InventorySnapshotRepository inventorySnapshotRepository;

    public List<InventoryItem> getAllInventory() {
        return inventoryItemRepository.findAll();
    }

    @Transactional
    public void createSnapshot() {
        InventorySnapshot snapshot = new InventorySnapshot();
        List<InventoryItem> items = inventoryItemRepository.findAll();
        for (InventoryItem item : items) {
            snapshot.getItemQuantities().put(item, item.getStockLevel());
        }
        inventorySnapshotRepository.save(snapshot);
    }

    @Transactional
    public void recordWaste(Long itemId, Double amount) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setStockLevel(Math.max(0, item.getStockLevel() - amount));
        inventoryItemRepository.save(item);
        managerService.checkAndCreateAlert(item);
    }

    @Transactional
    public void addStock(Long itemId, Double amount, Double pricePerUnit) {
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
