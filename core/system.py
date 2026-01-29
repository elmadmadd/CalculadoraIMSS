import os
import sys
import json
import requests
import subprocess
import asyncio
import logging
import re
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor

logger = logging.getLogger(__name__)

# ThreadPoolExecutor para cálculos pesados en background
CALCULATION_EXECUTOR = ThreadPoolExecutor(max_workers=2, thread_name_prefix="CalcWorker")
CALCULATION_CACHE = {}

def get_cache_key(*args, **kwargs):
    """Genera clave única para caché de cálculos"""
    import hashlib
    key_str = str(args) + str(sorted(kwargs.items()))
    return hashlib.md5(key_str.encode()).hexdigest()

def get_cached_or_calculate(cache_key, calculation_func, *args, **kwargs):
    """Obtiene resultado de caché o calcula si no existe"""
    if cache_key in CALCULATION_CACHE:
        return CALCULATION_CACHE[cache_key]

    result = calculation_func(*args, **kwargs)
    CALCULATION_CACHE[cache_key] = result
    return result

async def run_in_executor(func, *args, **kwargs):
    """Ejecuta función pesada en ThreadPool para no bloquear UI"""
    loop = asyncio.get_event_loop()
    future = CALCULATION_EXECUTOR.submit(func, *args, **kwargs)
    return await loop.run_in_executor(None, future.result)

class SyncManager:
    """Gestor de Sincronización Offline/Online"""
    FILE_NAME = "imss_data_cache.json"
    # URL Placeholder - Reemplazar con la URL real de tu API/JSON raw
    UPDATE_URL = "https://raw.githubusercontent.com/usuario/repo/main/data.json"

    @classmethod
    def load_local(cls):
        """Carga configuración persistente local"""
        if os.path.exists(cls.FILE_NAME):
            try:
                with open(cls.FILE_NAME, "r", encoding="utf-8") as f:
                    print(f"Cargando configuración offline de: {cls.FILE_NAME}")
                    return json.load(f)
            except Exception as e:
                print(f"Error cargando cache local: {e}")
        return None

    @classmethod
    async def sync_remote(cls):
        """Descarga actualización en segundo plano"""
        try:
            print("Intentando sincronizar datos remotos...")
            # Usar urllib en un hilo para no bloquear la UI de Flet
            import urllib.request

            def _download():
                try:
                    with urllib.request.urlopen(cls.UPDATE_URL, timeout=3) as response:
                        if response.status == 200:
                            data = json.load(response)
                            # Guardar en disco para la próxima (Offline First)
                            with open(cls.FILE_NAME, "w", encoding="utf-8") as f:
                                json.dump(data, f, indent=4)
                            return data
                except Exception as ex:
                    print(f"Fallo conexión remota: {ex}")
                    return None

            data = await asyncio.to_thread(_download)
            if data:
                print("¡Datos actualizados desde la nube!")
                return data

        except Exception as e:
            print(f"Modo Offline activo (Error de red): {e}")
        return None

class GestorActualizaciones:
    def __init__(self, version_actual, url_version_txt, url_exe_descarga):
        self.version_actual = float(version_actual)
        self.url_version = url_version_txt
        self.url_exe = url_exe_descarga
        # Rutas absolutas para evitar errores de "Archivo no encontrado"
        self.exe_actual = sys.executable
        self.directorio = os.path.dirname(self.exe_actual)
        self.nombre_exe = "ConsuCalcu.exe"

    def es_admin(self):
        """Verifica si el proceso actual tiene permisos de administrador."""
        import ctypes
        try:
            return ctypes.windll.shell32.IsUserAnAdmin()
        except:
            return False

    def reiniciar_como_admin(self):
        """Reinicia el script/exe actual solicitando elevación UAC (Admin)."""
        import ctypes
        import sys

        # 'runas' es el verbo mágico de Windows para pedir elevación
        ctypes.windll.shell32.ShellExecuteW(
            None,
            "runas",
            sys.executable,
            # Si es script .py pasamos el script como argumento, si es exe pasamos vacío
            __file__ if not getattr(sys, 'frozen', False) else "",
            None,
            1
        )

    def buscar_actualizacion(self):
        """Retorna True si hay una versión nueva"""
        try:
            logger.info("Buscando actualizaciones...")
            # verify=False para evitar el error SSL: CERTIFICATE_VERIFY_FAILED que reportas
            r = requests.get(self.url_version, timeout=5, verify=False)
            if r.status_code == 200:
                # Limpieza por si se cuela formato RTF o espacios
                texto_limpio = r.text.strip().split('\n')[0].replace("{\\rtf1", "").strip()
                try:
                    # Busca el primer número flotante en el texto
                    version_remota = float(re.findall(r"[\d\.]+", texto_limpio)[0])
                except (ValueError, IndexError) as e:
                    logger.error(f"Error parseando versión remota: {e}")
                    version_remota = float(texto_limpio)

                if version_remota > self.version_actual:
                    logger.info(f"Nueva versión disponible: {version_remota}")
                    return version_remota
            return None
        except requests.RequestException as e:
            logger.error(f"Error de red buscando actualización: {e}")
            return None
        except Exception as e:
            logger.error(f"Error inesperado buscando actualización: {e}", exc_info=True)
            return None

    def descargar_e_instalar(self, progress_callback=None):
        try:
            logger.info("Iniciando descarga de actualización...")
            ruta_temp = os.path.join(self.directorio, "update_temp.exe")
            ruta_bat = os.path.join(self.directorio, "updater.bat")

            # Descarga con headers para evitar bloqueos simples
            headers = {'User-Agent': 'Mozilla/5.0'}
            # verify=False para evitar errores de certificado en la descarga del EXE
            with requests.get(self.url_exe, stream=True, headers=headers, verify=False) as r:
                r.raise_for_status()
                total = int(r.headers.get('content-length', 0))
                if total < 1048576:  # 1MB mínimo
                    logger.warning(f"Archivo sospechosamente pequeño: {total} bytes")
                    return False

                dl = 0
                with open(ruta_temp, 'wb') as f:
                    for chunk in r.iter_content(8192):
                        dl += len(chunk)
                        f.write(chunk)
                        if progress_callback and total > 0:
                            progress_callback(dl/total)

            logger.info("Descarga completada, preparando instalación...")
            # Script BAT mejorado: Espera activa y manejo de comillas
            bat_script = f"""
@echo off
taskkill /F /PID {os.getpid()}
:loop
timeout /t 1 >nul
del "{self.exe_actual}" 2>nul
if exist "{self.exe_actual}" goto loop
move /Y "{ruta_temp}" "{self.exe_actual}"
start "" "{self.exe_actual}"
del "%~f0"
"""
            with open(ruta_bat, "w") as f:
                f.write(bat_script)

            # Ejecutar BAT y salir inmediatamente
            subprocess.Popen([ruta_bat], shell=True, creationflags=subprocess.CREATE_NO_WINDOW if sys.platform=='win32' else 0)
            logger.info("Actualización iniciada, cerrando aplicación...")
            os._exit(0)

        except requests.RequestException as e:
            logger.error(f"Error de red durante actualización: {e}")
            return False
        except IOError as e:
            logger.error(f"Error de I/O durante actualización: {e}")
            try:
                os.remove(ruta_temp)
            except:
                pass
            return False
        except Exception as e:
            logger.error(f"Error inesperado durante actualización: {e}", exc_info=True)
            try:
                os.remove(ruta_temp)
            except:
                pass
            return False
