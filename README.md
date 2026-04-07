# Diner Inventory System

A simple inventory management system built with Spring Boot, Thymeleaf, and SQLite.

## Prerequisites

- Java 21+
- Maven 3.x

## Quick Start

1.  **Clone the repository** (if using git).
2.  **Run the install script**:
    ```bash
    ./install.sh
    ```
3.  **Start the application**:
    ```bash
    mvn spring-boot:run
    ```
4.  **Access the application**:
    Open [http://localhost:8080](http://localhost:8080) in your browser.

## Project Structure

- `src/main/java`: Backend source code.
- `src/main/resources/templates`: HTML templates (Thymeleaf).
- `inventory.db`: SQLite database file (locally generated).

## Features

- Manage inventory items.
- Create and manage menu items and their ingredients.
- Waitstaff order tracking.
- Automatic database schema updates.
