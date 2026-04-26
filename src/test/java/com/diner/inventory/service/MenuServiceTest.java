package com.diner.inventory.service;

import com.diner.inventory.model.MenuItem;
import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderStatus;
import com.diner.inventory.repository.MenuItemRepository;
import com.diner.inventory.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testDeleteMenuItemCancelsActiveOrders() {
        // 1. Create a menu item
        MenuItem menuItem = new MenuItem();
        menuItem.setName("Test Burger");
        menuItem.setPrice(10.0);
        menuService.createMenuItem(menuItem);
        Long menuItemId = menuItem.getId();

        // 2. Create an order with that menu item
        Map<Long, Integer> items = new HashMap<>();
        items.put(menuItemId, 1);
        Order order = orderService.createOrder("A1", items);
        Long orderId = order.getId();

        assertEquals(OrderStatus.OPEN, order.getStatus());

        // 3. Delete the menu item
        menuService.deleteMenuItem(menuItemId);

        // 4. Verify the menu item is marked as deleted
        MenuItem deletedItem = menuItemRepository.findById(menuItemId).orElseThrow();
        assertTrue(deletedItem.getDeleted());

        // 5. Verify the order is now CANCELLED
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        
        // 6. Verify getAllMenuItems does not return the deleted item
        List<MenuItem> activeItems = menuService.getAllMenuItems();
        assertFalse(activeItems.stream().anyMatch(item -> item.getId().equals(menuItemId)));
    }
}
