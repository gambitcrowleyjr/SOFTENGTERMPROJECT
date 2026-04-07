package com.diner.inventory.controller;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.MenuItemIngredient;
import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.service.MenuService;
import com.diner.inventory.service.InventoryService;
import com.diner.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final InventoryItemRepository inventoryItemRepository;

    @GetMapping
    public String listMenu(Model model) {
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "menu/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        MenuItem menuItem = new MenuItem();
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("allInventoryItems", inventoryService.getAllInventory());
        return "menu/create";
    }

    @PostMapping("/create")
    public String createMenuItem(@ModelAttribute MenuItem menuItem, 
                                 @RequestParam(required = false) List<Long> inventoryItemIds,
                                 @RequestParam(required = false) List<Double> quantities) {
        List<MenuItemIngredient> ingredients = new ArrayList<>();
        if (inventoryItemIds != null && quantities != null) {
            for (int i = 0; i < inventoryItemIds.size(); i++) {
                Long invId = inventoryItemIds.get(i);
                Double qty = quantities.get(i);
                if (invId != null && qty != null) {
                    MenuItemIngredient ingredient = new MenuItemIngredient();
                    InventoryItem invItem = inventoryItemRepository.findById(invId).orElse(null);
                    ingredient.setInventoryItem(invItem);
                    ingredient.setQuantityRequired(qty);
                    ingredients.add(ingredient);
                }
            }
        }
        menuItem.setIngredients(ingredients);
        menuService.createMenuItem(menuItem);
        return "redirect:/menu";
    }

}
