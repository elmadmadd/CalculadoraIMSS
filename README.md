# ConsuCalcu Pro - Calculadora Financiera IMSS

## 📋 Descripción
Aplicación profesional para el cálculo y gestión de créditos financieros (Refinanciamiento, Crédito Nuevo, Seguros) con un dashboard de seguimiento avanzado. Desarrollada en Python usando el framework Flet para una interfaz moderna y responsiva.

## 🚀 Características Principales

### 1. Refinanciamiento (CNCA)
- Motor de cálculo bajo **Normativa IMSS**.
- Carga y análisis de múltiples deudas.
- Optimización de capacidad de pago.
- Visualización progresiva de ofertas.
- **Rendimiento**: Renderizado asíncrono y thread-safe.

### 2. Crédito Nuevo
- Calculadora de préstamos nuevos.
- Tablas de amortización en PDF.
- Comparador visual de escenarios.

### 3. Seguros
- Cotizador integrado.
- Diseño modular con inputs agrupados.

### 4. Agenda de Clientes
- Gestión de base de datos local de clientes.
- Entrada rápida de datos (pegar información en crudo).
- Búsqueda y filtrado por estatus.
- KPIs de seguimiento (Prospectos, En Proceso, Aprobados).
- Persistencia en SQLite.

## 📂 Estructura del Proyecto

### Núcleo
- `main.py`: Punto de entrada principal y lógica de la UI.
- `requirements.txt`: Dependencias del proyecto.

### Módulos de Lógica
- `NORMATIVA_CNCA.py`: Motor de reglas de negocio y cálculos financieros.
- `PERFORMANCE_MANAGER.py`: Gestor de recursos y diagnósticos del sistema.
- `CALCULATION_HISTORY.py`: Sistema de historial local.

### Componentes UI
- `PREMIUM_DESIGN_SYSTEM.py`: Tokens de diseño, colores y tipografía.
- `MODERN_COMPONENTS.py`: Componentes visuales modernos (Inputs, Cards).
- `FINANCIAL_WIDGETS.py`: Widgets especializados (Salud financiera, Capacidad).
- `ANIMATED_INPUTS.py`: Inputs con animaciones y validación.
- `PROGRESS_INDICATOR.py`: Indicadores de progreso por pasos.
- `SKELETON_LOADING.py`: Pantallas de carga (skeletons).
- `VIRTUAL_SCROLL.py`: Sistema de renderizado eficiente para listas largas.

### Utilidades
- `fpdf` y `openpyxl`: Generación de reportes PDF/Excel.

## 🛠️ Instalación y Ejecución

1. **Instalar dependencias**:
   ```bash
   pip install -r requirements.txt
   ```

2. **Ejecutar la aplicación**:
   ```bash
   python main.py
   ```

## 🏗️ Compilación

### macOS
Ejecutar el script de construcción:
```bash
chmod +x build_macos.sh
./build_macos.sh
```

### Windows
Ejecutar el script de construcción:
```bash
build_windows.bat
```

## ⚡ Optimizaciones Implementadas
- **Progressive Rendering**: Carga de listas grandes sin congelar la UI.
- **ThreadPool Executor**: Cálculos matemáticos en hilos secundarios.
- **Global Caching**: Reducción de 70% en tiempo de recálculo.
- **Memory Management**: Limpieza activa de recursos gráficos.
