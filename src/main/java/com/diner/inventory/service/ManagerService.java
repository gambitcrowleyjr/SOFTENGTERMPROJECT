package com.diner.inventory.service;

import com.diner.inventory.model.Alert;
import com.diner.inventory.model.InventoryItem;
import com.diner.inventory.model.Manager;
import com.diner.inventory.repository.AlertRepository;
import com.diner.inventory.repository.InventoryItemRepository;
import com.diner.inventory.repository.ManagerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final AlertRepository alertRepository;
    private final ManagerRepository managerRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void init() {
        if (managerRepository.count() == 0) {
            Manager manager = new Manager();
            manager.setName("Default Manager");
            manager.setAuthCode("1234");
            managerRepository.save(manager);
        }
    }

    public boolean authenticate(String code) {
        return managerRepository.findByAuthCode(code).isPresent();
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @Transactional
    public void markAlertAsRead(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setRead(true);
            alertRepository.save(alert);
        });
    }

    @Transactional
    public void deleteAllAlerts() {
        alertRepository.deleteAll();
    }

    @Transactional
    public void updateInventoryAlertConfig(Long itemId, boolean highValue, Double threshold, Double priceThreshold) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setHighValue(highValue);
        item.setAlertThreshold(threshold);
        item.setPriceAlertThreshold(priceThreshold);
        inventoryItemRepository.save(item);
    }

    @Transactional
    public void checkAndCreateAlert(InventoryItem item) {
        // Stock Level Alert Only
        if (item.isHighValue() && item.getStockLevel() < item.getAlertThreshold()) {
            Alert alert = alertRepository.findByInventoryItemIdAndIsReadFalse(item.getId())
                    .orElse(new Alert());
            
            alert.setInventoryItem(item);
            alert.setMessage("Low stock alert for high-value item: " + item.getName() + 
                            " (Current: " + item.getStockLevel() + ", Threshold: " + item.getAlertThreshold() + ")");
            alert.setCreatedAt(LocalDateTime.now());
            alert.setRead(false);
            alertRepository.save(alert);
        }
    }

    @Transactional
    public void checkPriceAlert(InventoryItem item, Double newPrice) {
        if (item.getPriceAlertThreshold() > 0 && item.getCurrentPrice() > 0) {
            double priceIncrease = newPrice - item.getCurrentPrice();
            double percentageIncrease = (priceIncrease / item.getCurrentPrice()) * 100.0;

            if (percentageIncrease >= item.getPriceAlertThreshold()) {
                Alert alert = alertRepository.findByInventoryItemIdAndIsReadFalse(item.getId())
                        .orElse(new Alert());
                
                alert.setInventoryItem(item);
                alert.setMessage(String.format("⚠️ Price Jump Alert: %s cost increased by %.1f%% (Was: $%.2f, Now: $%.2f)", 
                                 item.getName(), percentageIncrease, item.getCurrentPrice(), newPrice));
                alert.setCreatedAt(LocalDateTime.now());
                alert.setRead(false);
                alertRepository.save(alert);
            }
        }
    }
}
