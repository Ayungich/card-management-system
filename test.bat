@echo off
setlocal enabledelayedexpansion

REM Скрипт для запуска тестов через Docker с Java 21

echo Running tests in Docker container with Java 21...
echo.

REM Получаем текущую директорию с прямыми слешами для Docker
set "current_dir=%cd%"
set "current_dir=%current_dir:\=/%"

REM Если параметры не переданы, используем test по умолчанию
if "%~1"=="" (
    set "MVN_ARGS=test"
) else (
    set "MVN_ARGS=%*"
)

echo Running: mvn !MVN_ARGS!
echo.

docker run --rm -v "%current_dir%:/app" -w /app maven:3.9-eclipse-temurin-21-alpine mvn !MVN_ARGS!

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Tests completed successfully!
) else (
    echo.
    echo Tests failed with exit code %ERRORLEVEL%
)

pause

