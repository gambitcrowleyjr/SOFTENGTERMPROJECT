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
    public String addItem(@ModelAttribute InventoryItem item, RedirectAttributes redirectAttributes) {
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
    public String addStock(@RequestParam Long itemId, @RequestParam Double amount, RedirectAttributes redirectAttributes) {
        inventoryService.addStock(itemId, amount);
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/inventory";
    }
}
