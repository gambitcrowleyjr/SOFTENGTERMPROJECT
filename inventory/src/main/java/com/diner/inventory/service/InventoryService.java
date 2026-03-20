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

    public List<InventoryItem> getAllInventory() {
        return inventoryItemRepository.findAll();
    }

    @Transactional
    public void addStock(Long itemId, Double amount) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setStockLevel(item.getStockLevel() + amount);
        inventoryItemRepository.save(item);
    }

    @Transactional
    public void createInventoryItem(InventoryItem item) {
        inventoryItemRepository.save(item);
    }
}
