package com.diner.inventory.service;

import com.diner.inventory.model.SupplyOrder;
import com.diner.inventory.model.SupplyOrderItem;
import com.diner.inventory.model.SupplyOrderStatus;
import com.diner.inventory.repository.SupplyOrderRepository;
import com.diner.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplyOrderService {
    private final SupplyOrderRepository supplyOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryService inventoryService;

    public List<SupplyOrder> getAllSupplyOrders() {
        return supplyOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SupplyOrder> getPendingSupplyOrders() {
        return supplyOrderRepository.findByStatus(SupplyOrderStatus.PENDING);
    }

    public SupplyOrder getSupplyOrderById(Long id) {
        return supplyOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Supply Order not found"));
    }

    @Transactional
    public SupplyOrder createSupplyOrder(Map<Long, Double> itemsWithQuantities) {
        SupplyOrder order = new SupplyOrder();
        
        for (Map.Entry<Long, Double> entry : itemsWithQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                var invItem = inventoryItemRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Item not found: " + entry.getKey()));
                
                if (invItem.getUnitType() == com.diner.inventory.model.UnitType.UNIT && entry.getValue() % 1 != 0) {
                    throw new RuntimeException("Quantity must be a whole number for unit-based items: " + invItem.getName());
                }

                SupplyOrderItem orderItem = new SupplyOrderItem();
                orderItem.setInventoryItem(invItem);
                orderItem.setQuantityOrdered(entry.getValue());
                orderItem.setPriceAtOrder(invItem.getCurrentPrice());
                orderItem.setSupplyOrder(order);
                order.getItems().add(orderItem);
            }
        }

        if (order.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create an empty order sheet.");
        }

        return supplyOrderRepository.save(order);
    }

    @Transactional
    public void validateSupplyOrder(Long orderId, Map<Long, Double> receivedQuantities, Map<Long, Double> receivedPrices) {
        SupplyOrder order = getSupplyOrderById(orderId);
        if (order.getStatus() != SupplyOrderStatus.PENDING) {
            throw new RuntimeException("Order is already processed.");
        }

        for (SupplyOrderItem item : order.getItems()) {
            Double received = receivedQuantities.getOrDefault(item.getId(), 0.0);
            Double price = receivedPrices.getOrDefault(item.getId(), item.getInventoryItem().getCurrentPrice());
            
            if (item.getInventoryItem().getUnitType() == com.diner.inventory.model.UnitType.UNIT && received != null && received % 1 != 0) {
                throw new RuntimeException("Received quantity must be a whole number for unit-based items: " + item.getInventoryItem().getName());
            }

            item.setQuantityReceived(received);
            
            // Update stock and price in inventory
            if (received > 0) {
                inventoryService.addStock(item.getInventoryItem().getId(), received, price);
            }
        }

        order.setStatus(SupplyOrderStatus.RECEIVED);
        order.setReceivedAt(LocalDateTime.now());
        supplyOrderRepository.save(order);
    }
}
