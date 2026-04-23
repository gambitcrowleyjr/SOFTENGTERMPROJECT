package com.diner.inventory;

import com.diner.inventory.model.DiningTable;
import com.diner.inventory.repository.DiningTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DiningTableRepository diningTableRepository;
    private final com.diner.inventory.repository.InventoryItemRepository inventoryItemRepository;

    @Override
    public void run(String... args) throws Exception {
        if (inventoryItemRepository.count() == 0) {
            com.diner.inventory.model.InventoryItem i1 = new com.diner.inventory.model.InventoryItem();
            i1.setName("Burger Patty");
            i1.setStockLevel(100.0);
            i1.setCurrentPrice(1.50);
            i1.setPreviousPrice(1.40);
            i1.setUnitType(com.diner.inventory.model.UnitType.UNIT);
            inventoryItemRepository.save(i1);

            com.diner.inventory.model.InventoryItem i2 = new com.diner.inventory.model.InventoryItem();
            i2.setName("Cheese Slice");
            i2.setStockLevel(200.0);
            i2.setCurrentPrice(0.20);
            i2.setPreviousPrice(0.18);
            i2.setUnitType(com.diner.inventory.model.UnitType.UNIT);
            inventoryItemRepository.save(i2);
        }
    }
}
