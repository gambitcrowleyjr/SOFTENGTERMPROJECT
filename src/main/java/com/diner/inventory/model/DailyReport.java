package com.diner.inventory.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyReport {
    private LocalDate date;
    private Double totalEarnings;
    private Double totalCosts;
    private Double totalProfit;
}
