package com.diner.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double stockLevel = 0.0;

    private Double currentPrice = 0.0;

    private Double previousPrice = 0.0;

    private boolean highValue = false;

    private Double alertThreshold = 0.0;

    private Double priceAlertThreshold = 0.0; // Percentage, e.g., 25.0 for 25%

    @Enumerated(EnumType.STRING)
    private UnitType unitType;
}
