#!/bin/bash

# Exit on error
set -e

echo "=== Diner Inventory System Setup ==="

# 1. Update package list if we're on Ubuntu/Debian
if command -v apt-get >/dev/null 2>&1; then
    echo "Updating package lists..."
    sudo apt-get update -y
fi

# 2. Check and Install Java 21
if ! command -v java >/dev/null 2>&1; then
    echo "Java not found. Installing OpenJDK 21..."
    sudo apt-get install -y openjdk-21-jdk
else
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        echo "Updating Java to version 21..."
        sudo apt-get install -y openjdk-21-jdk
    fi
fi
echo "✓ Java 21+ ready."

# 3. Check and Install Maven
if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven not found. Installing Maven..."
    sudo apt-get install -y maven
fi
echo "✓ Maven ready."

# 4. Build the project
echo "Building project (this may take a minute)..."
mvn clean install -DskipTests

echo ""
echo "=== Setup Complete! ==="
echo "To run the application, use:"
echo "  mvn spring-boot:run"
echo ""
echo "The application will be available at http://localhost:8080"
