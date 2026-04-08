package com.diner.inventory.service;

import com.diner.inventory.model.DailyReport;
import com.diner.inventory.model.Order;
import com.diner.inventory.model.OrderItem;
import com.diner.inventory.model.OrderStatus;
import com.diner.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;

    public List<DailyReport> getReportsByDateRange(LocalDate start, LocalDate end) {
        List<Order> paidOrders = orderRepository.findByStatus(OrderStatus.PAID);
        
        Map<LocalDate, List<Order>> ordersByDate = paidOrders.stream()
                .filter(o -> o.getCompletedAt() != null)
                .filter(o -> !o.getCompletedAt().toLocalDate().isBefore(start) && !o.getCompletedAt().toLocalDate().isAfter(end))
                .collect(Collectors.groupingBy(o -> o.getCompletedAt().toLocalDate()));

        List<DailyReport> reports = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(date, Collections.emptyList());
            
            double earnings = 0.0;
            double costs = 0.0;
            
            for (Order order : dayOrders) {
                for (OrderItem item : order.getItems()) {
                    earnings += (item.getPriceAtOrder() != null ? item.getPriceAtOrder() : 0.0) * item.getQuantity();
                    costs += (item.getCostAtOrder() != null ? item.getCostAtOrder() : 0.0) * item.getQuantity();
                }
            }
            reports.add(new DailyReport(date, earnings, costs, earnings - costs));
        }
        
        // Return reverse chronological order (newest first)
        Collections.reverse(reports);
        return reports;
    }
}
