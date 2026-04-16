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

    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    public void saveSection(Section section) {
        sectionRepository.save(section);
    }

    @Transactional
    public void assignTableToSection(Long tableId, Long sectionId) {
        DiningTable table = diningTableRepository.findById(tableId).orElseThrow();
        Section section = sectionRepository.findById(sectionId).orElseThrow();
        table.setSection(section);
        diningTableRepository.save(table);
    }

    @Transactional
    public void assignSectionToEmployee(Long employeeId, Long sectionId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        Section section = sectionRepository.findById(sectionId).orElseThrow();
        employee.setAssignedSection(section);
        employeeRepository.save(employee);
    }
}
