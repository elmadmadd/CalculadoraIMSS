#!/bin/bash

# Script de Compilación para macOS
# ConsuCalcu Pro

echo "🏗️  Iniciando proceso de construcción para macOS..."

# Limpiar builds previos
rm -rf build dict

echo "📦 Empaquetando aplicación..."
pyinstaller --noconfirm --onedir --windowed --name "ConsuCalcu" \
    --icon "assets/icon.icns" \
    --add-data "assets:assets" \
    --add-data "NORMATIVA_CNCA.py:." \
    --add-data "PERFORMANCE_MANAGER.py:." \
    --add-data "PREMIUM_DESIGN_SYSTEM.py:." \
    --add-data="*.py:." \
    --hidden-import "PIL" \
    --hidden-import "flet" \
    --hidden-import "openpyxl" \
    --collect-all "flet" \
    main.py

echo "✅ Construcción finalizada. El ejecutable está en dist/ConsuCalcu.app"
