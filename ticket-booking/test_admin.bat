@echo off
set BASE=http://localhost:8081/api
set CURL="C:\Windows\System32\curl.exe"

echo.
echo === Enable Admin via H2 Console (direct DB update) ===
echo Updating admin user to enabled=true via H2 REST...

echo.
echo === Register a test user and enable via API ===
%CURL% -s -X POST "%BASE%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":\"ADMIN002\",\"fullName\":\"Test Admin\",\"email\":\"testadmin@college.edu\",\"password\":\"Admin@1234\",\"confirmPassword\":\"Admin@1234\",\"department\":\"IT\",\"phone\":\"9000000001\"}"
echo.

echo.
echo === Get verification token from DB via H2 ===
%CURL% -s -X POST "http://localhost:8081/api/h2-console/query" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "sql=UPDATE+USERS+SET+ENABLED=TRUE,ROLE='ADMIN'+WHERE+EMAIL='testadmin@college.edu'"
echo.

echo.
echo === Try Admin Login ===
%CURL% -s -X POST "%BASE%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@college.edu\",\"password\":\"Admin@1234\"}" > admin_resp.json
type admin_resp.json
echo.

echo.
echo === Try testadmin Login (still needs email verify) ===
%CURL% -s -X POST "%BASE%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"testadmin@college.edu\",\"password\":\"Admin@1234\"}"
echo.

pause
