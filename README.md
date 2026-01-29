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

## 📂 Estructura del Proyecto (Modular)

El proyecto ha sido refactorizado para mejorar la mantenibilidad y escalabilidad:

- `core/`: Lógica de negocio (Cálculos, Configuración, Datos).
- `ui/`: Interfaz de Usuario.
    - `views/`: Pantallas principales (Refinanciamiento, Nuevo, Seguros, Agenda).
    - `components/`: Componentes reutilizables (Tarjetas, Inputs, Sidebar).
    - `layouts/`: Estructuras de página.
- `utils/`: Utilidades generales (PDF, Excel, Imágenes).
- `main.py`: Punto de entrada de la aplicación.

## 🛠️ Instalación y Ejecución

### Requisitos Previos
- Python 3.10 o superior.

### Pasos Rápidos

1.  **Instalar dependencias**:
    ```bash
    pip install -r requirements.txt
    ```

2.  **Ejecutar la aplicación**:
    ```bash
    python main.py
    ```

### Scripts de Ayuda
Hemos incluido scripts para facilitar el inicio:

- **Windows**: Doble clic en `run_app.bat`
- **macOS/Linux**: Ejecutar `./run_app.sh` en la terminal.

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
- **Modular Architecture**: Código limpio y organizado por responsabilidad.
