package com.diner.inventory.controller;

import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public String listInventory(Model model) {
        model.addAttribute("items", inventoryService.getAllInventory());
        return "inventory/list";
    }

    @GetMapping("/add-item")
    public String showAddItemForm(Model model) {
        model.addAttribute("item", new InventoryItem());
        return "inventory/add-item";
    }

    @PostMapping("/add-item")
    public String addItem(@ModelAttribute InventoryItem item, @RequestParam Double totalPrice, RedirectAttributes redirectAttributes) {
        if (item.getStockLevel() != null && item.getStockLevel() > 0) {
            item.setCurrentPrice(totalPrice / item.getStockLevel());
        } else {
            item.setCurrentPrice(0.0);
        }
        inventoryService.createInventoryItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "New item cataloged: " + item.getName());
        return "redirect:/inventory";
    }

    @GetMapping("/unload-orders")
    public String showUnloadOrders(Model model) {
        model.addAttribute("items", inventoryService.getAllInventory());
        return "inventory/unload-orders";
    }

    @PostMapping("/add-stock")
    public String addStock(@RequestParam Long itemId, @RequestParam Double amount, @RequestParam Double totalPrice, RedirectAttributes redirectAttributes) {
        Double pricePerUnit = (amount != null && amount > 0) ? totalPrice / amount : 0.0;
        inventoryService.addStock(itemId, amount, pricePerUnit);
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/inventory";
    }
}
