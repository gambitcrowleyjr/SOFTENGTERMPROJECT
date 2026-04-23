package com.diner.inventory.controller;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.MenuItemIngredient;
import com.diner.inventory.service.InventoryService;
import com.diner.inventory.service.ManagerService;
import com.diner.inventory.service.MenuService;
import com.diner.inventory.service.ReportService;
import com.diner.inventory.repository.InventoryItemRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.diner.inventory.repository.InventorySnapshotRepository;
import com.diner.inventory.model.InventorySnapshot;
import com.diner.inventory.model.InventoryItem;
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

    private final ReportService reportService;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final com.diner.inventory.service.EmployeeService employeeService;

    @GetMapping("/audit")
    public String showAuditForm(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("snapshots", inventorySnapshotRepository.findAll());
        return "manager/audit";
    }

    @PostMapping("/audit/results")
    public String showAuditResults(@RequestParam(required = false) Long startId, @RequestParam(required = false) Long endId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        
        if (startId == null || endId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select two valid snapshots.");
            return "redirect:/manager/audit";
        }

        var startSnap = inventorySnapshotRepository.findById(startId);
        var endSnap = inventorySnapshotRepository.findById(endId);

        if (startSnap.isEmpty() || endSnap.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected snapshots could not be found.");
            return "redirect:/manager/audit";
        }
        
        model.addAttribute("report", reportService.generateVarianceReport(startId, endId, startSnap.get().getCreatedAt(), endSnap.get().getCreatedAt()));
        return "manager/audit-results";
    }

    @GetMapping("/waste")
    public String showWasteForm(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("items", inventoryService.getAllInventory());
        return "manager/waste-record";
    }

    @PostMapping("/waste")
    public String recordWaste(@RequestParam Long itemId, @RequestParam Double amount, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        inventoryService.recordWaste(itemId, amount);
        return "redirect:/manager/dashboard";
    }

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

    @GetMapping("/employees")
    public String listEmployees(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("sections", employeeService.getAllSections());
        return "manager/employees";
    }

    @PostMapping("/employees/add")
    public String addEmployee(@RequestParam String employeeId, @RequestParam String name, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        com.diner.inventory.model.Employee employee = new com.diner.inventory.model.Employee();
        employee.setEmployeeId(employeeId);
        employee.setName(name);
        employeeService.saveEmployee(employee);
        return "redirect:/manager/employees";
    }

    @GetMapping("/sections")
    public String listSections(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("sections", employeeService.getAllSections());
        model.addAttribute("tables", employeeService.getAllTables());
        return "manager/sections";
    }

    @PostMapping("/sections/add")
    public String addSection(@RequestParam String name, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        com.diner.inventory.model.Section section = new com.diner.inventory.model.Section();
        section.setName(name);
        employeeService.saveSection(section);
        return "redirect:/manager/sections";
    }

    @PostMapping("/sections/assign-table")
    public String assignTable(@RequestParam String tableNumber, @RequestParam Long sectionId, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        employeeService.assignTableToSection(tableNumber, sectionId);
        return "redirect:/manager/sections";
    }

    @PostMapping("/employees/assign-section")
    public String assignSection(@RequestParam Long employeeId, @RequestParam Long sectionId, HttpSession session) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        employeeService.assignSectionToEmployee(employeeId, sectionId);
        return "redirect:/manager/employees";
    }

    @GetMapping("/reports")
    public String showReports(@RequestParam(defaultValue = "daily") String range, HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        
        java.time.LocalDate end = java.time.LocalDate.now();
        java.time.LocalDate start;
        
        switch (range) {
            case "weekly": start = end.minusWeeks(1); break;
            case "monthly": start = end.minusMonths(1); break;
            default: start = end; break;
        }
        
        model.addAttribute("reports", reportService.getReportsByDateRange(start, end));
        model.addAttribute("range", range);
        return "manager/reports";
    }

    @GetMapping("/menu/modify")
    public String showModifySelection(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "menu/modify-select";
    }

    @GetMapping("/menu/create")
    public String showCreateForm(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        MenuItem menuItem = new MenuItem();
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("allInventoryItems", inventoryService.getAllInventory());
        return "menu/create";
    }

    @PostMapping("/menu/create")
    public String createMenuItem(@ModelAttribute MenuItem menuItem,
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
        menuItem.setIngredients(ingredients);
        menuService.createMenuItem(menuItem);
        return "redirect:/menu";
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

    @GetMapping("/menu/delete")
    public String showDeleteSelection(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "menu/delete-select";
    }

    @PostMapping("/menu/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        try {
            menuService.deleteMenuItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete menu item. It may be referenced in existing orders.");
        }
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("managerAuth");
        return "redirect:/";
    }

    @GetMapping("/inventory/add-item")
    public String showAddItemForm(HttpSession session, Model model) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        model.addAttribute("item", new InventoryItem());
        return "inventory/add-item";
    }

    @PostMapping("/inventory/add-item")
    public String addItem(@ModelAttribute InventoryItem item, 
                          @RequestParam Double totalPrice, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (session.getAttribute("managerAuth") == null) return "redirect:/manager/login";
        if (item.getStockLevel() != null && item.getStockLevel() > 0) {
            item.setCurrentPrice(totalPrice / item.getStockLevel());
        } else {
            item.setCurrentPrice(0.0);
        }
        inventoryService.createInventoryItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "New item cataloged: " + item.getName());
        return "redirect:/inventory";
    }
}
