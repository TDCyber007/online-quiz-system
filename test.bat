@echo off
setlocal
cd /d "%~dp0"

if not exist out\com\quizsystem\test\SelfTest.class (
    echo The project is not compiled yet, building it now...
    call build.bat
    if errorlevel 1 exit /b 1
)
echo Running the test suite...
echo.
java -cp out com.quizsystem.test.SelfTest
endlocal
