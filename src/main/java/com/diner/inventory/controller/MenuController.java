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

}
