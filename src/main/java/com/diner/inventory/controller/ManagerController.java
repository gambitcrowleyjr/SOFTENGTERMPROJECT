package com.diner.inventory.controller;

import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.service.InventoryService;
import com.diner.inventory.service.ManagerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;
    private final InventoryService inventoryService;

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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("managerAuth");
        return "redirect:/";
    }
}
