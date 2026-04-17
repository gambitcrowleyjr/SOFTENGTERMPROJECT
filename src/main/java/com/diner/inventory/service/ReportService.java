package com.diner.inventory.service;

import com.diner.inventory.model.*;
import com.diner.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public Map<Long, Double> calculateTheoreticalUsage(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID && o.getCompletedAt() != null)
                .filter(o -> !o.getCompletedAt().isBefore(start) && !o.getCompletedAt().isAfter(end))
                .collect(Collectors.toList());

        Map<Long, Double> theoreticalUsage = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem orderItem : order.getItems()) {
                for (MenuItemIngredient ingredient : orderItem.getMenuItem().getIngredients()) {
                    Long itemId = ingredient.getInventoryItem().getId();
                    Double amount = ingredient.getQuantityRequired() * orderItem.getQuantity();
                    theoreticalUsage.put(itemId, theoreticalUsage.getOrDefault(itemId, 0.0) + amount);
                }
            }
        }
        return theoreticalUsage;
    }

    public Map<Long, Double> calculateActualUsage(Long startSnapshotId, Long endSnapshotId) {
        InventorySnapshot start = inventorySnapshotRepository.findById(startSnapshotId).orElseThrow();
        InventorySnapshot end = inventorySnapshotRepository.findById(endSnapshotId).orElseThrow();

        Map<Long, Double> actualUsage = new HashMap<>();
        
        for (InventoryItem item : start.getItemQuantities().keySet()) {
            Double startQty = start.getItemQuantities().getOrDefault(item, 0.0);
            Double endQty = end.getItemQuantities().getOrDefault(item, 0.0);
            actualUsage.put(item.getId(), startQty - endQty);
        }
        return actualUsage;
    }

    public List<Map<String, Object>> generateVarianceReport(Long startSnapshotId, Long endSnapshotId, LocalDateTime start, LocalDateTime end) {
        Map<Long, Double> theoretical = calculateTheoreticalUsage(start, end);
        Map<Long, Double> actual = calculateActualUsage(startSnapshotId, endSnapshotId);
        
        List<Map<String, Object>> report = new ArrayList<>();
        Set<Long> itemIds = new HashSet<>(theoretical.keySet());
        itemIds.addAll(actual.keySet());

        for (Long itemId : itemIds) {
            Double t = theoretical.getOrDefault(itemId, 0.0);
            Double a = actual.getOrDefault(itemId, 0.0);
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("itemId", itemId);
            entry.put("itemName", inventoryItemRepository.findById(itemId).map(InventoryItem::getName).orElse("Unknown"));
            entry.put("theoretical", t);
            entry.put("actual", a);
            entry.put("variance", t - a);
            entry.put("variancePercent", t != 0 ? (t - a) / t * 100 : 0);
            report.add(entry);
        }
        return report;
    }

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
