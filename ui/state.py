import asyncio
import logging
import flet as ft
from utils.formatting import clean_float

logger = logging.getLogger(__name__)

class AutoSaveManager:
    """
    Gestor de autoguardado automático para prevenir pérdida de datos.
    """
    def __init__(self, save_interval: int = 30):
        """
        Args:
            save_interval: Intervalo de guardado en segundos (default: 30)
        """
        self.save_interval = save_interval
        self.last_data = None
        self.is_running = False
        self._task = None

    async def start(self, page, get_data_func, save_func):
        """
        Inicia el autoguardado automático.

        Args:
            page: Instancia de la página de Flet
            get_data_func: Función que retorna los datos a guardar
            save_func: Función que guarda los datos
        """
        self.is_running = True
        logger.info(f"Autoguardado iniciado (cada {self.save_interval}s)")

        while self.is_running:
            await asyncio.sleep(self.save_interval)

            try:
                current_data = get_data_func()

                # Solo guardar si hay cambios
                if current_data != self.last_data and current_data:
                    save_func(current_data)
                    self.last_data = current_data
                    logger.debug("Autoguardado completado")
            except Exception as e:
                logger.error(f"Error en autoguardado: {e}")

    def stop(self):
        """Detiene el autoguardado."""
        self.is_running = False
        logger.info("Autoguardado detenido")

class LoadingState:
    """
    Gestor de estados de carga con mensajes específicos.
    """
    def __init__(self, loading_overlay):
        self.overlay = loading_overlay
        self.current_message = None

    def show(self, message: str = "Procesando...", animation: str = "default"):
        """
        Muestra el overlay de carga con un mensaje específico.

        Args:
            message: Mensaje a mostrar
            animation: Tipo de animación ("default", "fire", "success")
        """
        self.current_message = message
        if hasattr(self.overlay, 'show'):
            self.overlay.show(animation)
        logger.debug(f"Loading: {message}")

    def hide(self):
        """Oculta el overlay de carga."""
        if hasattr(self.overlay, 'hide'):
            self.overlay.hide()
        self.current_message = None
        logger.debug("Loading oculto")

    async def with_loading(self, func, message: str = "Procesando...", *args, **kwargs):
        """
        Ejecuta una función con loading state automático.

        Uso:
            resultado = await loading_state.with_loading(
                generar_pdf,
                "Generando PDF...",
                form_data,
                credito_data
            )
        """
        self.show(message)
        try:
            from core.system import run_in_executor
            if asyncio.iscoroutinefunction(func):
                result = await func(*args, **kwargs)
            else:
                # Assuming run_in_executor is available or we use default loop
                loop = asyncio.get_event_loop()
                result = await loop.run_in_executor(None, lambda: func(*args, **kwargs))
            return result
        finally:
            self.hide()
