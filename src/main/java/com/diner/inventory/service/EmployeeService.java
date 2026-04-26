package com.diner.inventory.service;

import com.diner.inventory.model.DiningTable;
import com.diner.inventory.model.Employee;
import com.diner.inventory.model.Section;
import com.diner.inventory.repository.DiningTableRepository;
import com.diner.inventory.repository.EmployeeRepository;
import com.diner.inventory.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final SectionRepository sectionRepository;
    private final DiningTableRepository diningTableRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public List<DiningTable> getAllTables() {
        return diningTableRepository.findAll();
    }

    public List<DiningTable> getUnassignedTables() {
        return diningTableRepository.findBySectionIsNull();
    }

    public List<DiningTable> getAssignedTables() {
        return diningTableRepository.findBySectionIsNotNull();
    }

    public void saveTable(DiningTable table) {
        diningTableRepository.save(table);
    }

    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    public void saveSection(Section section) {
        sectionRepository.save(section);
    }

    @Transactional
    public void assignTableToSection(String tableNumber, Long sectionId) {
        DiningTable table = diningTableRepository.findByTableNumber(tableNumber)
                .orElseGet(() -> new DiningTable(tableNumber));
        Section section = sectionRepository.findById(sectionId).orElseThrow();
        table.setSection(section);
        diningTableRepository.save(table);
    }

    @Transactional
    public void unassignTableFromSection(String tableNumber) {
        DiningTable table = diningTableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        table.setSection(null);
        diningTableRepository.save(table);
    }

    @Transactional
    public void assignSectionToEmployee(Long employeeId, Long sectionId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        Section section = sectionRepository.findById(sectionId).orElseThrow();
        
        section.setAssignedEmployee(employee);
        sectionRepository.save(section);
    }

    @Transactional
    public void unassignSectionFromEmployee(Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElseThrow();
        section.setAssignedEmployee(null);
        sectionRepository.save(section);
    }
}
