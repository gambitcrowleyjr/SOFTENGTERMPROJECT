package com.diner.inventory.controller;

import com.diner.inventory.service.InventoryService;
import com.diner.inventory.service.SupplyOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final SupplyOrderService supplyOrderService;

    @GetMapping
    public String listInventory(Model model) {
        model.addAttribute("items", inventoryService.getAllInventory());
        return "inventory/list";
    }

    @GetMapping("/unload-orders")
    public String showUnloadOrders(@RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "10") int size, 
                                   Model model) {
        org.springframework.data.domain.Page<com.diner.inventory.model.SupplyOrder> orderPage = supplyOrderService.getPaginatedSupplyOrders(page, size);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "inventory/supply-orders/list";
    }

    @GetMapping("/supply-orders/validate/{id}")
    public String showValidateSupplyOrderForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", supplyOrderService.getSupplyOrderById(id));
        return "inventory/supply-orders/validate";
    }

    @PostMapping("/supply-orders/validate/{id}")
    public String validateSupplyOrder(@PathVariable Long id, 
                                      @RequestParam Map<String, String> params, 
                                      @RequestParam String validatedBy,
                                      RedirectAttributes redirectAttributes) {
        java.util.Map<Long, Double> receivedQuantities = new java.util.HashMap<>();
        java.util.Map<Long, Double> receivedPrices = new java.util.HashMap<>();
        
        params.forEach((key, value) -> {
            if (key.startsWith("received_") && !value.isEmpty()) {
                Long itemId = Long.parseLong(key.substring(9));
                receivedQuantities.put(itemId, Double.parseDouble(value));
            } else if (key.startsWith("price_") && !value.isEmpty()) {
                Long itemId = Long.parseLong(key.substring(6));
                receivedPrices.put(itemId, Double.parseDouble(value));
            }
        });

        try {
            supplyOrderService.validateSupplyOrder(id, receivedQuantities, receivedPrices, validatedBy);
            redirectAttributes.addFlashAttribute("successMessage", "Supply order validated and stock updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/unload-orders";
    }

    @PostMapping("/add-stock")
    public String addStock(@RequestParam Long itemId, @RequestParam Double amount, @RequestParam Double totalPrice, RedirectAttributes redirectAttributes) {
        Double pricePerUnit = (amount != null && amount > 0) ? totalPrice / amount : 0.0;
        inventoryService.addStock(itemId, amount, pricePerUnit);
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/inventory";
    }

}
