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
    private final com.diner.inventory.service.EmployeeService employeeService;

    @GetMapping("/new")
    public String showNewOrderForm(Model model) {
        java.util.List<com.diner.inventory.model.MenuItem> allItems = menuService.getAllMenuItems();
        java.util.Map<String, java.util.List<com.diner.inventory.model.MenuItem>> groupedItems = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getCategory() != null ? item.getCategory() : "General"));
        
        model.addAttribute("groupedMenuItems", groupedItems);
        model.addAttribute("categories", groupedItems.keySet());
        model.addAttribute("tables", employeeService.getAllTables());
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
        model.addAttribute("orders", orderService.getFOHOrders());
        return "orders/open";
    }

    @GetMapping("/kitchen")
    public String kitchenView(Model model) {
        model.addAttribute("orders", orderService.getKitchenOrders());
        return "orders/kitchen";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, 
                              @RequestParam OrderStatus status, 
                              @RequestParam(required = false) String view,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        
        if ("kitchen".equals(view)) {
            return "redirect:/orders/kitchen";
        }
        return "redirect:/orders/open";
    }

    @GetMapping("/history")
    public String listHistory(Model model) {
        model.addAttribute("orders", orderService.getCompletedOrders());
        return "orders/history";
    }
}
