package com.diner.inventory.service;

import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderItem;
import com.diner.inventory.model.OrderStatus;
import com.diner.inventory.model.MenuItem;
import com.diner.inventory.repository.OrderRepository;
import com.diner.inventory.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuService menuService;

    @Transactional
    public Order createOrder(String tableNumber, Map<Long, Integer> itemQuantities) {
        Order order = new Order();
        order.setTableNumber(tableNumber);
        
        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                MenuItem menuItem = menuItemRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Menu item not found: " + entry.getKey()));
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenuItem(menuItem);
                orderItem.setQuantity(entry.getValue());
                order.getItems().add(orderItem);
            }
        }
        
        return orderRepository.save(order);
    }

    public List<Order> getOpenOrders() {
        return orderRepository.findByStatusNot(OrderStatus.PAID);
    }

    public List<Order> getCompletedOrders() {
        return orderRepository.findByStatus(OrderStatus.PAID);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Deduct stock when transition to COOKED or if skipping to it
        if (newStatus == OrderStatus.COOKED && order.getStatus() == OrderStatus.OPEN) {
            for (OrderItem item : order.getItems()) {
                menuService.deductStock(item.getMenuItem(), item.getQuantity());
            }
        }
        
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.PAID) {
            order.setCompletedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
    }
}
