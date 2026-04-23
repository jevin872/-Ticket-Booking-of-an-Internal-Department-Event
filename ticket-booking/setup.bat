@echo off
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set MVN=C:\Users\ADMIN\apache-maven-3.9.9\bin\mvn.cmd

echo === Creating MySQL Database ===
%MYSQL% -u root -proot -e "CREATE DATABASE IF NOT EXISTS ticket_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo [ERROR] MySQL connection failed. Check credentials.
    pause
    exit /b 1
)
echo [OK] Database created.

echo === Building Spring Boot Application ===
cd /d %~dp0
%MVN% clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Build failed.
    pause
    exit /b 1
)
echo [OK] Build successful.

echo === Starting Application ===
java --enable-native-access=ALL-UNNAMED -jar target\ticket-booking-1.0.0.jar
pause
