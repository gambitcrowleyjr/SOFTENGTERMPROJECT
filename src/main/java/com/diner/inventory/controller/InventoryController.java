package com.diner.inventory.controller;

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

    @GetMapping("/unload-orders")
    public String showUnloadOrders(Model model) {
        model.addAttribute("items", inventoryService.getAllInventory());
        return "inventory/unload-orders";
    }

    @PostMapping("/snapshot")
    public String saveSnapshot(RedirectAttributes redirectAttributes) {
        inventoryService.createSnapshot();
        redirectAttributes.addFlashAttribute("successMessage", "Inventory snapshot saved successfully!");
        return "redirect:/inventory";
    }

    @PostMapping("/add-stock")
    public String addStock(@RequestParam Long itemId, @RequestParam Double amount, @RequestParam Double totalPrice, RedirectAttributes redirectAttributes) {
        Double pricePerUnit = (amount != null && amount > 0) ? totalPrice / amount : 0.0;
        inventoryService.addStock(itemId, amount, pricePerUnit);
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/inventory";
    }
    
    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        model.addAttribute("sales", 1000);
        model.addAttribute("materials", 400);
        model.addAttribute("waste", 100);

        return "reports";
    }

}
