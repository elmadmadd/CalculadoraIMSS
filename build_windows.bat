@echo off
echo 🏗️  Iniciando proceso de construccion para Windows...

REM Limpiar builds previos
if exist build rd /s /q build
if exist dist rd /s /q dist

echo 📦 Empaquetando aplicacion...
pyinstaller --noconfirm --onedir --windowed --name "ConsuCalcu" ^
    --icon "assets/icon.ico" ^
    --add-data "assets;assets" ^
    --add-data "core;core" ^
    --add-data "ui;ui" ^
    --add-data "utils;utils" ^
    --hidden-import "PIL" ^
    --hidden-import "flet" ^
    --hidden-import "openpyxl" ^
    --hidden-import "pygame" ^
    --hidden-import "pygame.mixer" ^
    --collect-all "flet" ^
    --collect-all "pygame" ^
    main.py

echo ✅ Construccion finalizada. El ejecutable esta en dist\ConsuCalcu.exe
pause
