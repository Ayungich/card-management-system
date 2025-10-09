@echo off
setlocal enabledelayedexpansion

REM Script for running tests via Docker with Java 21

echo Running tests in Docker container with Java 21...
echo.

REM Get current directory with forward slashes for Docker
set "current_dir=%~dp0"
set "current_dir=%current_dir:~0,-1%"
set "current_dir=%current_dir:\=/%"

REM If no parameters provided, use test by default
if "%~1"=="" (
    set "MVN_ARGS=test"
    echo Running unit tests (integration tests are excluded in pom.xml^)...
) else (
    set "MVN_ARGS=%*"
    echo Running custom Maven command...
)

echo Command: mvn !MVN_ARGS!
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

