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
    --add-data "core:core" \
    --add-data "ui:ui" \
    --add-data "utils:utils" \
    --hidden-import "PIL" \
    --hidden-import "flet" \
    --hidden-import "openpyxl" \
    --collect-all "flet" \
    main.py

echo "✅ Construcción finalizada. El ejecutable está en dist/ConsuCalcu.app"
