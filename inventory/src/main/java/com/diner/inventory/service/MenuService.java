package com.diner.inventory.service;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.MenuItemIngredient;
import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.repository.MenuItemRepository;
import com.diner.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuItemRepository menuItemRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Transactional
    public void createMenuItem(MenuItem menuItem) {
        // Associate ingredients with the menu item
        if (menuItem.getIngredients() != null) {
            for (MenuItemIngredient ingredient : menuItem.getIngredients()) {
                ingredient.setMenuItem(menuItem);
            }
        }
        menuItemRepository.save(menuItem);
    }

    @Transactional
    public void processOrder(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        for (MenuItemIngredient ingredient : menuItem.getIngredients()) {
            InventoryItem inventoryItem = ingredient.getInventoryItem();
            Double currentStock = inventoryItem.getStockLevel();
            Double requiredQuantity = ingredient.getQuantityRequired();
            
            if (currentStock < requiredQuantity) {
                throw new RuntimeException("Insufficient stock for: " + inventoryItem.getName());
            }
            
            inventoryItem.setStockLevel(currentStock - requiredQuantity);
            inventoryItemRepository.save(inventoryItem);
        }
    }
}
