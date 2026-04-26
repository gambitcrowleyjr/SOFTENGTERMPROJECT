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
    private final InventoryItemRepository inventoryItemRepository;
    private final WasteRecordRepository wasteRecordRepository;

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

            // Calculate Waste Cost for this day from recorded waste events
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            double wasteCost = wasteRecordRepository.findByRecordedAtBetween(dayStart, dayEnd)
                    .stream()
                    .mapToDouble(r -> r.getQuantity() * r.getCostAtTime())
                    .sum();

            reports.add(new DailyReport(date, earnings, costs, wasteCost, earnings - costs - wasteCost));
        }
        
        // Return reverse chronological order (newest first)
        Collections.reverse(reports);
        return reports;
    }

    public List<Map<String, Object>> getWasteBreakdown(LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);
        
        List<WasteRecord> records = wasteRecordRepository.findByRecordedAtBetween(startTime, endTime);
        
        Map<Long, List<WasteRecord>> groupedByItem = records.stream()
                .collect(Collectors.groupingBy(r -> r.getInventoryItem().getId()));

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (Map.Entry<Long, List<WasteRecord>> entry : groupedByItem.entrySet()) {
            InventoryItem item = inventoryItemRepository.findById(entry.getKey()).orElse(null);
            if (item == null) continue;

            double totalQty = entry.getValue().stream().mapToDouble(WasteRecord::getQuantity).sum();
            double totalCost = entry.getValue().stream().mapToDouble(r -> r.getQuantity() * r.getCostAtTime()).sum();

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("itemName", item.getName());
            itemData.put("quantity", totalQty);
            itemData.put("cost", totalCost);
            breakdown.add(itemData);
        }
        return breakdown;
    }
}
