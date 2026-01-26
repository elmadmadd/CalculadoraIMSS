# Instalación en Windows - ConsuCalcu

## 📋 Requisitos Previos

- **Python 3.8+** instalado
- **pip** actualizado

## 🚀 Instalación Rápida

### 1. Instalar dependencias

```bash
pip install -r requirements.txt
```

### 2. Ejecutar la aplicación

```bash
python main.py
```

## 🔊 Audio Seguro en Windows

La aplicación usa **pygame** para reproducir sonidos de forma segura, sin disparar alertas de antivirus.

### ¿Por qué pygame?

- ✅ **Biblioteca Python pura** - No ejecuta comandos del sistema
- ✅ **Sin PowerShell** - No dispara alertas de Windows Defender
- ✅ **Multiplataforma** - Funciona en Windows, macOS y Linux
- ✅ **Bajo consumo** - Optimizado para audio corto

### Fallback automático

Si pygame no está disponible, la app usa:
- **Windows**: `winsound` (biblioteca estándar de Python)
- **macOS**: `afplay` (comando nativo)

## 📦 Crear Ejecutable (.exe)

### Opción 1: Script automático

```bash
build_windows.bat
```

### Opción 2: Manual con PyInstaller

```bash
pyinstaller --noconfirm --onedir --windowed --name "ConsuCalcu" ^
    --icon "assets/icon.ico" ^
    --add-data "assets;assets" ^
    --hidden-import "pygame" ^
    --hidden-import "pygame.mixer" ^
    --collect-all "flet" ^
    --collect-all "pygame" ^
    main.py
```

El ejecutable estará en: `dist\ConsuCalcu\ConsuCalcu.exe`

## 🛡️ Seguridad en Windows

### Evitar falsos positivos de antivirus:

1. **Usar pygame** (ya implementado) ✅
2. **Firmar el ejecutable** (opcional):
   ```bash
   signtool sign /f certificado.pfx /p password ConsuCalcu.exe
   ```
3. **Agregar excepción en Windows Defender** (si es necesario):
   - Configuración → Seguridad de Windows → Protección antivirus
   - Agregar `dist\ConsuCalcu` a exclusiones

## 🔧 Solución de Problemas

### Error: "pygame no disponible"

```bash
pip install pygame>=2.5.0
```

### Error: "No se puede reproducir audio"

- Verifica que los archivos `.mp3` estén en `assets/`
- Comprueba que el volumen del sistema esté activado

### Antivirus bloquea el ejecutable

1. Verifica que pygame esté instalado
2. Reconstruye el ejecutable con `build_windows.bat`
3. Si persiste, agrega una excepción en el antivirus

## 📝 Notas

- La primera ejecución puede tardar unos segundos
- El audio es opcional; la app funciona sin él
- Todos los sonidos son archivos MP3 cortos (<1 segundo)
