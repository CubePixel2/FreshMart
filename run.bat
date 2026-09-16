@echo off
title FreshMart Grocery Store POS & Inventory System
echo ===================================================================
echo   FreshMart - Modern Grocery Store Inventory and Billing System
echo ===================================================================
echo.

:: Check for JAVA_HOME; if not defined, check common JDK locations
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Java\jdk-21.0.12.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
        echo [INFO] Detected Java 21 at %JAVA_HOME%
    ) else if exist "C:\Program Files\Java\jdk-26.0.2.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
        echo [INFO] Detected Java at %JAVA_HOME%
    ) else if exist "C:\Program Files\Java\latest" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
        echo [INFO] Detected Java at %JAVA_HOME%
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo [INFO] Building and starting Spring Boot Application...
echo [INFO] Open your browser at http://localhost:8080 once started.
echo.

call .\mvnw.cmd spring-boot:run

pause
