package com.diner.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String tableNumber;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    public DiningTable(String tableNumber) {
        this.tableNumber = tableNumber;
    }
}
