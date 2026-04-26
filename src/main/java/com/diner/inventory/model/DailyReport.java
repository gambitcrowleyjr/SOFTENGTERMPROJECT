package com.diner.inventory.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyReport {
    private LocalDate date;
    private Double totalEarnings;
    private Double totalCosts;   // COGS from sold items
    private Double varianceCost; // Value of items missing/wasted
    private Double netProfit;    // Earnings - Costs - Variance
}
