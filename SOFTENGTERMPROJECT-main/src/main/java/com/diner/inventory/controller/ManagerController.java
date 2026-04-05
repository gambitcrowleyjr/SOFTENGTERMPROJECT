package com.diner.inventory.controller;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.MenuItemIngredient;
import com.diner.inventory.service.InventoryService;
import com.diner.inventory.service.ManagerService;
import com.diner.inventory.service.MenuService;
import com.diner.inventory.repository.InventoryItemRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;
    private final InventoryService inventoryService;
    private final MenuService menuService;
    private final InventoryItemRepository inventoryItemRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "manager/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String authCode, HttpSession session, RedirectAttributes redirectAttributes) {
        if (managerService.authenticate(authCode)) {
            session.setAttribute("managerAuth", true);
            return "redirect:/manager/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Manager Auth Code");
            return "redirect:/manager/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        return "manager/dashboard";
    }

    @GetMapping("/inventory-config")
    public String inventoryConfig(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("items", inventoryService.getAllInventory());
        return "manager/inventory-config";
    }

    @PostMapping("/inventory-config")
    public String updateConfig(@RequestParam Long itemId, 
                               @RequestParam(defaultValue = "false") boolean highValue, 
                               @RequestParam Double alertThreshold,
                               @RequestParam Double priceThreshold,
                               HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        managerService.updateInventoryAlertConfig(itemId, highValue, alertThreshold, priceThreshold);
        return "redirect:/manager/inventory-config";
    }

    @GetMapping("/alerts")
    public String showAlerts(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("alerts", managerService.getAllAlerts());
        return "manager/alerts";
    }

    @PostMapping("/alerts/read/{id}")
    public String markAsRead(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        managerService.markAlertAsRead(id);
        return "redirect:/manager/alerts";
    }

    @PostMapping("/alerts/clear")
    public String clearAllAlerts(HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        managerService.deleteAllAlerts();
        return "redirect:/manager/alerts";
    }

    @GetMapping("/menu/modify")
    public String showModifySelection(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "menu/modify-select";
    }

    @GetMapping("/menu/modify/{id}")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        MenuItem menuItem = menuService.getMenuItemById(id);
        if (menuItem == null) {
            return "redirect:/manager/menu/modify";
        }
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("allInventoryItems", inventoryService.getAllInventory());
        return "menu/modify";
    }

    @PostMapping("/menu/modify/{id}")
    public String updateMenuItem(@PathVariable Long id,
                                 @ModelAttribute MenuItem menuItem,
                                 @RequestParam(required = false) List<Long> inventoryItemIds,
                                 @RequestParam(required = false) List<Double> quantities,
                                 HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        List<MenuItemIngredient> ingredients = new ArrayList<>();
        if (inventoryItemIds != null && quantities != null) {
            for (int i = 0; i < inventoryItemIds.size(); i++) {
                Long invId = inventoryItemIds.get(i);
                Double qty = quantities.get(i);
                if (invId != null && qty != null) {
                    final Double finalQty = qty;
                    inventoryItemRepository.findById(invId).ifPresent(invItem -> {
                        MenuItemIngredient ingredient = new MenuItemIngredient();
                        ingredient.setInventoryItem(invItem);
                        ingredient.setQuantityRequired(finalQty);
                        ingredients.add(ingredient);
                    });
                }
            }
        }
        menuService.updateMenuItem(id, menuItem, ingredients);
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("managerAuth");
        return "redirect:/";
    }
}
