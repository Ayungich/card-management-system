#!/bin/bash
# Скрипт для запуска тестов через Docker с Java 21

echo "Running tests in Docker container with Java 21..."
echo ""

# Если параметры не переданы, запускаем только unit-тесты
if [ $# -eq 0 ]; then
    echo "Running unit tests (integration tests are excluded in pom.xml)..."
    MVN_ARGS="test"
else
    echo "Running custom Maven command..."
    MVN_ARGS="$@"
fi

echo "Command: mvn $MVN_ARGS"
echo ""

docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-21-alpine mvn $MVN_ARGS

if [ $? -eq 0 ]; then
    echo ""
    echo "Tests completed successfully!"
else
    echo ""
    echo "Tests failed with exit code $?"
    exit $?
fi

