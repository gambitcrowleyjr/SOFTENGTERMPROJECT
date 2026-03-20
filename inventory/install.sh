#!/bin/bash

# Exit on error
set -e

echo "=== Diner Inventory System Setup ==="

# 1. Check for Java 21
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java is not installed."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "Error: Java version 21 or higher is required. Current: $JAVA_VERSION"
    exit 1
fi
echo "✓ Java 21+ found."

# 2. Check for Maven
if ! command -v mvn >/dev/null 2>&1; then
    echo "Error: Maven is not installed."
    exit 1
fi
echo "✓ Maven found."

# 3. Build the project
echo "Building project (this may take a minute)..."
mvn clean install -DskipTests

echo ""
echo "=== Setup Complete! ==="
echo "To run the application, use:"
echo "  mvn spring-boot:run"
echo ""
echo "The application will be available at http://localhost:8080"
