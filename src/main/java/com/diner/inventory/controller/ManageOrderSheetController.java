package com.diner.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ManagerOrderSheetController {

    @GetMapping("/manager/order-sheet/create")
    public String showCreateOrderSheetPage() {
        return "create-order-sheet";
    }

    @PostMapping("/manager/order-sheet/create")
    public String createOrderSheet() {
        return "redirect:/manager/order-sheet/create";
    }
}
