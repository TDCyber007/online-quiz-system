@echo off
setlocal
cd /d "%~dp0"

if not exist out\com\quizsystem\Main.class (
    echo The project is not compiled yet, building it now...
    call build.bat
    if errorlevel 1 exit /b 1
)
echo Starting the Online Quiz System...
echo.
java -cp out com.quizsystem.Main
endlocal
