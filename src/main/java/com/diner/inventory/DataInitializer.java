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

    @Override
    public void run(String... args) throws Exception {
        if (diningTableRepository.count() == 0) {
            for (int i = 1; i <= 50; i++) {
                diningTableRepository.save(new DiningTable(String.valueOf(i)));
            }
        }
    }
}
