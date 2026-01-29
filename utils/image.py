import sys
import os
import time
import logging
from pathlib import Path
from core.config import CONSTANTES

try:
    from PIL import Image, ImageDraw, ImageFont
    HAVE_PIL = True
except ImportError:
    HAVE_PIL = False

logger = logging.getLogger(__name__)

def resource_path(relative_path):
    """ Obtiene la ruta absoluta al recurso, funciona para dev y para PyInstaller """
    try:
        # PyInstaller crea una carpeta temporal en _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

class GeneradorImagen:


    # Usamos la función para asegurar que encuentre el archivo dentro del .exe
    TEMPLATE_PATH = resource_path("plantilla_consubanco.png")
    FONT_PATH = resource_path("Roboto-Bold.ttf")

    # --- COORDENADAS EXACTAS (2000x1428) ---
    COORDS = {
        "monto": (455, 700),    # Bajamos Y para no tapar "MONTO TOTAL" (antes 680)
        "pago":  (441, 1150),   # Bajamos Y para no tapar "PAGO MENSUAL" (antes 1140)
        "plazo": (1226, 1127)   # Bajamos Y para alinear con Pago (antes 1141)
    }

    TEXT_COLOR = (255, 255, 255)

    # --- CACHÉ DE RECURSOS (PERFORMANCE) ---
    _template_cache = None
    _font_cache = {}

    @classmethod
    def get_template(cls):
        """Obtiene la plantilla desde caché o la carga por primera vez."""
        if cls._template_cache is None:
            try:
                logger.info("Cargando plantilla de imagen por primera vez...")
                img = Image.open(cls.TEMPLATE_PATH).convert("RGBA")
                if img.size != (2000, 1428):
                    img = img.resize((2000, 1428), Image.Resampling.LANCZOS)
                cls._template_cache = img
                logger.info("Plantilla cargada y cacheada exitosamente")
            except FileNotFoundError as e:
                logger.error(f"Plantilla no encontrada: {e}")
                raise
        return cls._template_cache.copy()  # Retorna una copia para evitar modificar el caché

    @classmethod
    def get_font(cls, size):
        """Obtiene fuente desde caché o la carga por primera vez."""
        if size not in cls._font_cache:
            try:
                cls._font_cache[size] = ImageFont.truetype(cls.FONT_PATH, size)
                logger.debug(f"Fuente tamaño {size} cargada y cacheada")
            except Exception as e:
                logger.warning(f"No se pudo cargar fuente personalizada: {e}")
                cls._font_cache[size] = ImageFont.load_default()
        return cls._font_cache[size]

    @staticmethod
    def crear_card_img(data):
        if not HAVE_PIL: return False, "Librería Pillow no instalada"

        try:
            # 1. Cargar la Plantilla Base desde CACHÉ (10x más rápido)
            try:
                base_img = GeneradorImagen.get_template()
            except FileNotFoundError:
                return False, f"Falta plantilla: {GeneradorImagen.TEMPLATE_PATH}"

            draw = ImageDraw.Draw(base_img)

            # 2. Cargar Fuentes desde CACHÉ
            # Reducido drásticamente para encajar en el diseño (ajustado final)
            font_big = GeneradorImagen.get_font(100)
            font_med = GeneradorImagen.get_font(100)

            # 3. Preparar los datos
            val_monto = data.get('remanente', 0) if 'remanente' in data else data.get('monto', 0)
            txt_monto = f"${val_monto:,.2f}"
            txt_pago = f"${data.get('pago_visual', 0):,.2f}"
            txt_plazo = f"{data.get('plazo', 0)} MESES"

            # 4. Dibujar textos centrados
            GeneradorImagen._draw_text_centered(draw, GeneradorImagen.COORDS["monto"], txt_monto, font_big)
            GeneradorImagen._draw_text_centered(draw, GeneradorImagen.COORDS["pago"], txt_pago, font_big)
            GeneradorImagen._draw_text_centered(draw, GeneradorImagen.COORDS["plazo"], txt_plazo, font_med)

            # 5. Guardar
            final_img = base_img.convert("RGB")
            filename = f"Oferta_{data.get('producto', 'Financiera')}_{int(time.time())}.png"
            output_path = CONSTANTES["RUTAS"]["DOCS"] / filename
            CONSTANTES["RUTAS"]["DOCS"].mkdir(parents=True, exist_ok=True)

            final_img.save(output_path, quality=95)
            return True, str(output_path)

        except FileNotFoundError as e:
            logger.error(f"Archivo de plantilla no encontrado: {e}")
            return False, f"Falta plantilla: {GeneradorImagen.TEMPLATE_PATH}"
        except IOError as e:
            logger.error(f"Error de I/O generando imagen: {e}")
            return False, f"Error guardando imagen: {str(e)}"
        except Exception as e:
            logger.error(f"Error inesperado generando imagen: {e}", exc_info=True)
            return False, str(e)

    @staticmethod
    def _draw_text_centered(draw, center_xy, text, font, fill=(255, 255, 255)):
        cx, cy = center_xy
        bbox = draw.textbbox((0, 0), text, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]

        x = cx - (text_width / 2)
        # Ajuste visual vertical
        y = cy - (text_height / 2) - (bbox[3] * 0.15)

        draw.text((x, y), text, font=font, fill=fill)
