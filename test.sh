#!/bin/bash
# Скрипт для запуска тестов через Docker с Java 21

echo "Running tests in Docker container with Java 21..."
echo ""

docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-21-alpine mvn "$@"

if [ $? -eq 0 ]; then
    echo ""
    echo "Tests completed successfully!"
else
    echo ""
    echo "Tests failed with exit code $?"
    exit $?
fi

