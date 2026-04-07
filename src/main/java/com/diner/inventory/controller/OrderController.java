package com.diner.inventory.controller;

import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderStatus;
import com.diner.inventory.service.OrderService;
import com.diner.inventory.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MenuService menuService;

    @GetMapping("/new")
    public String showNewOrderForm(Model model) {
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "orders/new";
    }

    @PostMapping("/new")
    public String createOrder(@RequestParam String tableNumber, 
                             @RequestParam Map<String, String> allParams, 
                             RedirectAttributes redirectAttributes) {
        // Filter params that start with item_ to get quantities
        Map<Long, Integer> itemQuantities = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("item_"))
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().substring(5)),
                        e -> Integer.parseInt(e.getValue())
                ));
        
        try {
            orderService.createOrder(tableNumber, itemQuantities);
            redirectAttributes.addFlashAttribute("successMessage", "Order created for Table " + tableNumber);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
        }
        return "redirect:/orders/open";
    }

    @GetMapping("/open")
    public String listOpenOrders(Model model) {
        model.addAttribute("orders", orderService.getOpenOrders());
        return "orders/open";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/orders/open";
    }

    @GetMapping("/history")
    public String listHistory(Model model) {
        model.addAttribute("orders", orderService.getCompletedOrders());
        return "orders/history";
    }
}
