package com.diner.inventory.repository;

import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusNot(OrderStatus status);
    List<Order> findByStatusIn(Collection<OrderStatus> statuses);
    List<Order> findByStatus(OrderStatus status);
}
