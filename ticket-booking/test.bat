@echo off
set BASE=http://localhost:8081/api
set CURL="C:\Windows\System32\curl.exe"

echo.
echo ========================================
echo  TICKET BOOKING SYSTEM - API TEST SUITE
echo ========================================

echo.
echo [1] Health Check
%CURL% -s %BASE%/actuator/health
echo.

echo.
echo [2] Get Public Events
%CURL% -s "%BASE%/events/public" | findstr /C:"totalElements"
echo.

echo.
echo [3] Register New User
%CURL% -s -X POST "%BASE%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":\"EMP001\",\"fullName\":\"John Doe\",\"email\":\"john@college.edu\",\"password\":\"Pass@1234\",\"confirmPassword\":\"Pass@1234\",\"department\":\"Computer Science\",\"phone\":\"9876543210\"}"
echo.

echo.
echo [4] Register Second User
%CURL% -s -X POST "%BASE%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":\"EMP002\",\"fullName\":\"Jane Smith\",\"email\":\"jane@college.edu\",\"password\":\"Pass@1234\",\"confirmPassword\":\"Pass@1234\",\"department\":\"Electronics\",\"phone\":\"9876543211\"}"
echo.

echo.
echo [5] Admin Login
%CURL% -s -X POST "%BASE%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@college.edu\",\"password\":\"Admin@1234\"}" > admin_token.json
type admin_token.json
echo.

echo.
echo [6] Extract Admin Token
for /f "usebackq tokens=*" %%a in (`powershell -Command "(Get-Content admin_token.json | ConvertFrom-Json).data.accessToken"`) do set TOKEN=%%a
echo Token starts with: %TOKEN:~0,30%...
echo.

echo.
echo [7] Get Admin Dashboard
%CURL% -s -H "Authorization: Bearer %TOKEN%" "%BASE%/admin/dashboard"
echo.

echo.
echo [8] Get All Users (Admin)
%CURL% -s -H "Authorization: Bearer %TOKEN%" "%BASE%/admin/users"
echo.

echo.
echo [9] Search Events
%CURL% -s "%BASE%/events/public/search?keyword=tech"
echo.

echo.
echo [10] Get Event by ID
%CURL% -s "%BASE%/events/public/10000000-0000-0000-0000-000000000001"
echo.

echo.
echo [11] Test Invalid Login (wrong password)
%CURL% -s -X POST "%BASE%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@college.edu\",\"password\":\"WrongPass\"}"
echo.

echo.
echo [12] Test Validation Error (missing fields)
%CURL% -s -X POST "%BASE%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"bad-email\"}"
echo.

echo.
echo [13] Test Unauthorized Access (no token)
%CURL% -s "%BASE%/admin/dashboard"
echo.

echo.
echo [14] Forgot Password (email enumeration safe)
%CURL% -s -X POST "%BASE%/auth/forgot-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"nonexistent@college.edu\"}"
echo.

echo.
echo ========================================
echo  ALL TESTS COMPLETE
echo  Swagger UI: http://localhost:8081/api/swagger-ui.html
echo  H2 Console: http://localhost:8081/api/h2-console
echo ========================================
