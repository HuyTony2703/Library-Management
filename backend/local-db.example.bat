@echo off
REM Legacy manual config example.
REM start-backend.bat now asks for database info on first run and saves it to:
REM %APPDATA%\LibraDesk
REM
REM Use this file only if you run the backend manually and want to set
REM environment variables yourself.
set "DB_URL=jdbc:sqlserver://localhost:1433;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true"
set "DB_USERNAME=sa"
set "DB_PASSWORD=your_sql_server_password"
