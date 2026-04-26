package com.diner.inventory.service;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.MenuItemIngredient;
import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderStatus;
import com.diner.inventory.repository.MenuItemRepository;
import com.diner.inventory.repository.InventoryItemRepository;
import com.diner.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuItemRepository menuItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ManagerService managerService;
    private final OrderRepository orderRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findByDeletedFalse();
    }

    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id).orElse(null);
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
    public void updateMenuItem(Long id, MenuItem updatedMenuItem, List<MenuItemIngredient> newIngredients) {
        MenuItem existingMenuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        
        existingMenuItem.setName(updatedMenuItem.getName());
        existingMenuItem.setCategory(updatedMenuItem.getCategory());
        existingMenuItem.setPrice(updatedMenuItem.getPrice());
        
        // Clear existing ingredients and add new ones
        existingMenuItem.getIngredients().clear();
        if (newIngredients != null) {
            for (MenuItemIngredient ingredient : newIngredients) {
                ingredient.setMenuItem(existingMenuItem);
                existingMenuItem.getIngredients().add(ingredient);
            }
        }
        
        menuItemRepository.save(existingMenuItem);
    }

    @Transactional
    public void deductStock(MenuItem menuItem, int quantity) {
        for (MenuItemIngredient ingredient : menuItem.getIngredients()) {
            InventoryItem inventoryItem = ingredient.getInventoryItem();
            Double currentStock = inventoryItem.getStockLevel();
            Double requiredQuantity = ingredient.getQuantityRequired() * quantity;
            
            if (currentStock < requiredQuantity) {
                throw new RuntimeException("Insufficient stock for: " + inventoryItem.getName());
            }
            
            inventoryItem.setStockLevel(currentStock - requiredQuantity);
            inventoryItemRepository.save(inventoryItem);
            managerService.checkAndCreateAlert(inventoryItem);
        }
    }

    @Transactional
    public void processOrder(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        deductStock(menuItem, 1);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        
        // Find all orders that contain this menu item and are active
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.CANCELLED) {
                boolean containsItem = order.getItems().stream()
                        .anyMatch(item -> item.getMenuItem() != null && item.getMenuItem().getId().equals(id));
                if (containsItem) {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                }
            }
        }
        
        menuItem.setDeleted(true);
        menuItemRepository.save(menuItem);
    }

    public double calculateMenuItemCost(MenuItem menuItem) {
        double cost = 0.0;
        for (MenuItemIngredient ingredient : menuItem.getIngredients()) {
            cost += ingredient.getInventoryItem().getCurrentPrice() * ingredient.getQuantityRequired();
        }
        return cost;
    }
}
