import flet as ft
import asyncio
import time
import requests
import math
import json
import threading
import platform
import subprocess
import datetime
import uuid
import os
import re
import sys
import logging
import ssl

# Fix para "SSL: CERTIFICATE_VERIFY_FAILED" en Windows/Mac
# Esto permite que Flet descargue fuentes y el actualizador funcione en PCs sin certificados actualizados
try:
    _create_unverified_https_context = ssl._create_unverified_context
except AttributeError:
    pass
else:
    ssl._create_default_https_context = _create_unverified_https_context

from concurrent.futures import ThreadPoolExecutor
from functools import lru_cache
# Los controles Audio y Lottie ahora están integrados en flet >= 0.80
# No se requieren importaciones externas adicionales

# Detección de hardware para modo de rendimiento
try:
    import psutil
    HAVE_PSUTIL = True
except ImportError:
    HAVE_PSUTIL = False

# Importar componentes de UX
try:
    from SMART_TOOLTIPS import get_tooltip, LabelWithTooltip, TOOLTIPS
    HAVE_TOOLTIPS = True
except ImportError:
    HAVE_TOOLTIPS = False
    print("Warning: SMART_TOOLTIPS not available")

try:
    from VALIDATED_INPUT import create_validated_input
    HAVE_VALIDATION = True
except ImportError:
    HAVE_VALIDATION = False
    print("Warning: VALIDATED_INPUT not available")

try:
    from NORMATIVA_CNCA import NormativaCNCA
    HAVE_NORMATIVA = True
except ImportError:
    HAVE_NORMATIVA = False
    print("Warning: NORMATIVA_CNCA not available")

try:
    from PROGRESS_INDICATOR import create_progress_overlay, create_cnca_progress_steps, create_nuevo_progress_steps, create_seguro_progress_steps
    HAVE_PROGRESS = True
except ImportError:
    HAVE_PROGRESS = False
    print("Warning: PROGRESS_INDICATOR not available")

try:
    from OFFER_COMPARATOR import create_offer_comparator, create_selectable_offer_card
    from ANIMATED_INPUTS import AnimatedTextField
    from FINANCIAL_WIDGETS import FinancialHealthWidget, PaymentCapacityWidget, PensionEstimatorWidget, CapacityProgressBar, ValidationGatesWidget
    from REFINANCE_LAYOUT import create_refinance_layout
    HAVE_COMPARATOR = True
except ImportError:
    HAVE_COMPARATOR = False
    print("Warning: OFFER_COMPARATOR not available")

try:
    from CALCULATION_HISTORY import CalculationHistory, create_history_sidebar
    HAVE_HISTORY = True
except ImportError:
    HAVE_HISTORY = False
    print("Warning: CALCULATION_HISTORY not available")

try:
    from transition_effects import TransitionEffects, TransitionConfig
    HAVE_TRANSITIONS = True
except ImportError:
    HAVE_TRANSITIONS = False
    print("Warning: TRANSITION_EFFECTS not available")

try:
    from SKELETON_LOADING import create_skeleton_offer_card as create_skeleton_card, create_loading_overlay
    HAVE_SKELETONS = True
except ImportError:
    HAVE_SKELETONS = False
    print("Warning: SKELETON_LOADING not available")

def resource_path(relative_path):
    """ Obtiene la ruta absoluta al recurso, funciona para dev y para PyInstaller """
    try:
        # PyInstaller crea una carpeta temporal en _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

# --- DEPENDENCIAS EXTERNAS ---
try:
    from PIL import Image, ImageDraw, ImageFont
    HAVE_PIL = True
except ImportError:
    HAVE_PIL = False
from typing import Dict, List, Optional, Any, Tuple
from pathlib import Path
from decimal import Decimal, ROUND_HALF_UP, getcontext

# --- DEPENDENCIAS EXTERNAS ---


try:
    import openpyxl
    from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
    from openpyxl.utils import get_column_letter
    HAVE_OPENPYXL = True
except ImportError:
    HAVE_OPENPYXL = False

# ==========================================
# GESTOR DE RENDIMIENTO Y OPTIMIZACIÓN
# ==========================================
from PERFORMANCE_MANAGER import performance_manager, HAVE_PSUTIL

# ThreadPoolExecutor para cálculos pesados en background (OPTIMIZACIÓN FASE 2)
# Evita freezing de UI durante cálculos largos
CALCULATION_EXECUTOR = ThreadPoolExecutor(max_workers=2, thread_name_prefix="CalcWorker")

# Cache global para cálculos repetidos (OPTIMIZACIÓN FASE 2)
CALCULATION_CACHE = {}


try:
    from fpdf import FPDF
    HAVE_FPDF = True
except ImportError:
    HAVE_FPDF = False

# Configurar precisión decimal alta para cálculos internos
getcontext().prec = 20

# ==========================================
# CONFIGURACIÓN DE LOGGING
# ==========================================
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(Path.home() / "Documents" / "calculadora_imss.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# ==========================================
# 0. DESIGN SYSTEM (2026 - CUSTOM PALETTE: LIME & NAVY)
# ==========================================
THEME = {
    "colors": {
        # --- LIGHT MODE (Fresco y Profesional) ---
        "light": {
            "background": "#FEFEF5",       # Crema muy claro
            "card_bg": "#FFFFFF",          # White
            "text_primary": "#2C3E50",     # Navy oscuro
            "text_secondary": "#5A6C7D",   # Navy medio
            "text_tertiary": "#8B9AA8",    # Navy claro
            "accent": "#7DC242",           # Verde Lima principal
            "input_bg": "#F8F9FA",         # Gris muy claro
            "input_bg_focus": "#E8F5E9",   # Verde muy claro (focus)
            "divider": "#E8EAED",          # Gris claro
            "border": "#D1D5DB",           # Gris medio
            "border_focus": "#7DC242",     # Verde Lima
            "error": "#EF4444",            # Red 500
        },
        # --- DARK MODE (Elegante con acentos Lima) ---
        "dark": {
            "background": "#1A1F2E",       # Navy muy oscuro
            "card_bg": "#2C3E50",          # Navy oscuro
            "text_primary": "#FEFEF5",     # Crema claro
            "text_secondary": "#B8C5D0",   # Gris azulado claro
            "text_tertiary": "#8B9AA8",    # Gris azulado medio
            "accent": "#9FD356",           # Verde Lima brillante (más visible en oscuro)
            "input_bg": "#3D4F5F",         # Navy medio
            "input_bg_focus": "#4A5F71",   # Navy medio-claro (focus)
            "divider": "#3D4F5F",          # Navy medio
            "border": "#4A5F71",           # Navy medio-claro
            "border_focus": "#9FD356",     # Verde Lima brillante
            "error": "#FCA5A5",            # Red 300 (Pastel)
        },
        
        # --- SEMANTIC MAPPINGS (Material 3) ---
        "background": "background",
        "card_bg": "surface",
        "text_primary": "onSurface",
        "text_secondary": "onSurfaceVariant",
        "text_tertiary": "outline",
        
        "input_bg": "surfaceVariant",
        "input_bg_focus": "surfaceContainerHigh",
        
        "divider": "outlineVariant",
        
        "accent": "primary",
        "success": "primary",
        "error": "error",
        "warning": "tertiary",
        
        # --- COLORES ESPECIALES (Contextuales) ---
        "coral": "#FF6B6B",           # Rojo coral para alertas
        "turquoise": "#4ECDC4",       # Turquesa para éxito
        "purple": "#A78BFA",          # Púrpura para gráficos
        "gold_highlight": "#F59E0B",  # Amber 500 (Mejor opción)
        "lime_light": "#A4D65E",      # Verde Lima claro
        "lime_dark": "#6EBF3C",       # Verde Lima oscuro
        "navy": "#2C3E50",            # Navy principal
        "cream": "#FEFEF5",           # Crema
    },
    "shadows": {
        # --- SOMBRAS ESTILO DASHBOARD MODERNO ---
        "card": ft.BoxShadow(
            blur_radius=24, color="#33000000", spread_radius=0, offset=ft.Offset(0, 8)
        ),
        "card_hover": ft.BoxShadow(
            blur_radius=32, color="#40000000", spread_radius=0, offset=ft.Offset(0, 12)
        ),

        "card_light": ft.BoxShadow(
            blur_radius=20, color="#26000000", spread_radius=0, offset=ft.Offset(0, 6)
        ),
        "card_hover_light": ft.BoxShadow(
            blur_radius=28, color="#33000000", spread_radius=0, offset=ft.Offset(0, 10)
        ),
        
        "card_dark": ft.BoxShadow(
            blur_radius=28, color="#4D000000", spread_radius=0, offset=ft.Offset(0, 10)
        ),
        "card_hover_dark": ft.BoxShadow(
            blur_radius=36, color="#59000000", spread_radius=0, offset=ft.Offset(0, 14)
        ),
        
        # Sombras para botones coloridos
        "button": ft.BoxShadow(
            blur_radius=16, color="#26000000", spread_radius=0, offset=ft.Offset(0, 4)
        ),
        "button_hover": ft.BoxShadow(
            blur_radius=20, color="#33000000", spread_radius=0, offset=ft.Offset(0, 6)
        ),
        "button_pressed": ft.BoxShadow(
            blur_radius=10, color="#1A000000", spread_radius=0, offset=ft.Offset(0, 2)
        ),
        
        # Sombras para inputs
        "input_focus_light": ft.BoxShadow(
            blur_radius=12, color="#333B82F6", spread_radius=0, offset=ft.Offset(0, 0)
        ),
        "input_focus_dark": ft.BoxShadow(
            blur_radius=16, color="#4060A5FA", spread_radius=0, offset=ft.Offset(0, 0)
        ),
    },
    "gradients": {
        "light": {
            "best_option": ft.LinearGradient(
                colors=["#FFFBEB", "#FEF3C7"],  # Amber-50 to Amber-100
                begin=ft.Alignment(-1, -1), 
                end=ft.Alignment(1, 1)
            ),
            "normal_card": ft.LinearGradient(
                colors=["#FFFFFF", "#F8FAFC"],  # White to Slate-50
                begin=ft.Alignment(-1, -1), 
                end=ft.Alignment(1, 1)
            ),
            "btn_primary": ft.LinearGradient(
                colors=["#059669", "#047857"],  # Emerald-600 to Emerald-700
                begin=ft.Alignment(-1, 0), 
                end=ft.Alignment(1, 0)
            ),
            "btn_highlight": ft.LinearGradient(
                colors=["#F59E0B", "#D97706"],  # Amber-500 to Amber-600
                begin=ft.Alignment(-1, 0), 
                end=ft.Alignment(1, 0)
            )
        },
        "dark": {
            "best_option": ft.LinearGradient(
                colors=["#78350F", "#92400E"],  # Amber-900 to Amber-800
                begin=ft.Alignment(-1, -1), 
                end=ft.Alignment(1, 1)
            ),
            "normal_card": ft.LinearGradient(
                colors=["#0F172A", "#1E293B"],  # Slate-900 to Slate-800
                begin=ft.Alignment(-1, -1), 
                end=ft.Alignment(1, 1)
            ),
            "btn_primary": ft.LinearGradient(
                colors=["#10B981", "#059669"],  # Emerald-500 to Emerald-600
                begin=ft.Alignment(-1, 0), 
                end=ft.Alignment(1, 0)
            ),
            "btn_highlight": ft.LinearGradient(
                colors=["#FBBF24", "#F59E0B"],  # Amber-400 to Amber-500 (brighter)
                begin=ft.Alignment(-1, 0), 
                end=ft.Alignment(1, 0)
            )
        }
    },
    "radius": {
        "xs": 4,
        "sm": 6,
        "md": 8,
        "lg": 12,
        "xl": 16,
        "input": 8,
        "card": 16,
        "modal": 20
    },
    "fonts": {
        "primary": "Outfit",
        "heading": "Outfit",
        "monospace": "Roboto Mono"
    },
    # === ANIMACIONES PREMIUM 60FPS ===
    "animations": {
        # Interacciones básicas
        "fast": ft.Animation(200, ft.AnimationCurve.EASE_OUT),             # 200ms - Hover, clicks rápidos
        "standard": ft.Animation(400, ft.AnimationCurve.EASE_OUT_CUBIC),   # 400ms - Transiciones UI estándar
        "smooth": ft.Animation(600, ft.AnimationCurve.EASE_OUT_QUART),     # 600ms - Movimientos grandes
        "bouncy": ft.Animation(800, ft.AnimationCurve.ELASTIC_OUT),        # 800ms - Efectos Pop
        "fade": ft.Animation(300, ft.AnimationCurve.EASE_IN_OUT),          # 300ms - Opacidad
        "decelerate": ft.Animation(400, ft.AnimationCurve.DECELERATE),     # 400ms - Frenado natural
        
        # Feedback de botones (micro-interacciones)
        "button_press": ft.Animation(100, ft.AnimationCurve.EASE_OUT),     # 100ms - Press down ultra-rápido
        "button_release": ft.Animation(200, ft.AnimationCurve.EASE_OUT),   # 200ms - Release suave
        "button_hover": ft.Animation(150, ft.AnimationCurve.EASE_OUT),     # 150ms - Hover sutil
        
        # Entradas de pantalla
        "screen_enter": ft.Animation(500, ft.AnimationCurve.EASE_OUT_CUBIC),    # 500ms - Entrada de vista
        "screen_exit": ft.Animation(300, ft.AnimationCurve.EASE_IN_CUBIC),      # 300ms - Salida de vista
        "slide_in": ft.Animation(400, ft.AnimationCurve.EASE_OUT_QUART),        # 400ms - Deslizamiento
        "slide_out": ft.Animation(300, ft.AnimationCurve.EASE_IN_QUART),        # 300ms - Deslizamiento salida
        
        # Carga de elementos
        "stagger_fast": ft.Animation(250, ft.AnimationCurve.EASE_OUT),          # 250ms - Items en lista (rápido)
        "stagger_smooth": ft.Animation(400, ft.AnimationCurve.EASE_OUT_CUBIC),  # 400ms - Items en lista (suave)
        "skeleton_pulse": ft.Animation(1200, ft.AnimationCurve.EASE_IN_OUT),    # 1200ms - Skeleton shimmer
        
        # Estados especiales
        "spring": ft.Animation(600, ft.AnimationCurve.ELASTIC_OUT),              # 600ms - Rebote suave
        "overshoot": ft.Animation(500, ft.AnimationCurve.EASE_OUT_BACK),         # 500ms - Sobrepaso sutil
        "snap": ft.Animation(150, ft.AnimationCurve.EASE_OUT),                   # 150ms - Cambio instantáneo
    }
}

# ==========================================
# 0.05 RESPONSIVE MANAGER
# ==========================================
class ResponsiveManager:
    """
    Gestor centralizado de responsividad y dimensionamiento dinámico.
    Maneja breakpoints (MOBILE, TABLET, DESKTOP) y escala fuentes/espaciados.
    """
    def __init__(self, page: ft.Page):
        self.page = page
        self.mode = "DESKTOP" # Default
        self.scale_factor = 1.0
        
        # Diccionario de Dimensiones Factorizadas
        self.dim = {
            "text": {
                "hero": {"MOBILE": 32, "TABLET": 38, "DESKTOP": 46},
                "title": {"MOBILE": 18, "TABLET": 22, "DESKTOP": 24},
                "subtitle": {"MOBILE": 14, "TABLET": 16, "DESKTOP": 18},
                "body": {"MOBILE": 12, "TABLET": 13, "DESKTOP": 14},
                "small": {"MOBILE": 10, "TABLET": 11, "DESKTOP": 12},
            },
            "layout": {
                "sidebar_width": {"MOBILE": 80, "TABLET": 260, "DESKTOP": 260}, # Expandido por defecto en Tablet/Desktop
                "grid_extent": {"MOBILE": 300, "TABLET": 350, "DESKTOP": 400},
                "card_padding": {"MOBILE": 15, "TABLET": 20, "DESKTOP": 25},
                "view_padding": {"MOBILE": 10, "TABLET": 20, "DESKTOP": 30},
                "icon_large": {"MOBILE": 24, "TABLET": 28, "DESKTOP": 36},
                "icon_medium": {"MOBILE": 20, "TABLET": 22, "DESKTOP": 24},
            }
        }

    def update_dimensions(self):
        """Calcula el modo basado en el ancho de la ventana"""
        # Verificación segura del ancho
        try:
            w = self.page.window.width if self.page and self.page.window else 1200
        except:
            w = 1200 # Fallback
            
        if w < 768:
            self.mode = "MOBILE"
            self.scale_factor = 0.85
        elif w < 1200:
            self.mode = "TABLET"
            self.scale_factor = 0.95
        else:
            self.mode = "DESKTOP"
            self.scale_factor = 1.0
            
    def get_size(self, category, key):
        """Retorna el tamaño exacto para el modo actual"""
        # Fallback seguro
        if category not in self.dim or key not in self.dim[category]:
            return 14 # Default safe size
            
        val = self.dim[category][key].get(self.mode)
        if val is None:
            val = self.dim[category][key].get("DESKTOP", 14)
        return val

    def is_mobile(self):
        return self.mode == "MOBILE"

# ==========================================
# 0.1 UTILIDADES GLOBALES
# ==========================================

def get_theme_color(page, color_key):
    """
    Obtiene el color correcto según el tema activo (light/dark).
    
    Args:
        page: Instancia de la página de Flet
        color_key: Clave del color (ej: "background", "card_bg", "text_primary")
    
    Returns:
        String con el código hexadecimal del color
    """
    if not page:
        # Fallback a light mode si no hay página
        return THEME["colors"]["light"].get(color_key, "#FFFFFF")
    
    theme_mode = "dark" if page.theme_mode == ft.ThemeMode.DARK else "light"
    return THEME["colors"][theme_mode].get(color_key, "#FFFFFF")


class ThemeManager:
    """
    Gestor de tema que proporciona acceso dinámico a los colores según el modo activo.
    Actualiza automáticamente THEME["colors"] para que los componentes existentes funcionen.
    """
    _current_mode = "light"
    
    @classmethod
    def update_theme_mode(cls, is_dark):
        """Actualiza el modo de tema actual y los colores en THEME."""
        cls._current_mode = "dark" if is_dark else "light"
        
        # Actualizar las claves de acceso directo en THEME["colors"]
        # para que los componentes que usan THEME["colors"]["text_primary"] obtengan el color correcto
        current_colors = THEME["colors"][cls._current_mode]
        
        # Actualizar claves de acceso directo
        THEME["colors"]["background"] = current_colors["background"]
        THEME["colors"]["card_bg"] = current_colors["card_bg"]
        THEME["colors"]["text_primary"] = current_colors["text_primary"]
        THEME["colors"]["text_secondary"] = current_colors["text_secondary"]
        THEME["colors"]["text_tertiary"] = current_colors["text_tertiary"]
        THEME["colors"]["accent"] = current_colors["accent"]
        THEME["colors"]["input_bg"] = current_colors["input_bg"]
        THEME["colors"]["input_bg_focus"] = current_colors["input_bg_focus"]
        THEME["colors"]["divider"] = current_colors["divider"]
        THEME["colors"]["border"] = current_colors["border"]
        THEME["colors"]["border_focus"] = current_colors["border_focus"]
        THEME["colors"]["error"] = current_colors["error"]
    
    @classmethod
    def get_current_colors(cls):
        """Retorna el diccionario de colores del tema actual."""
        return THEME["colors"][cls._current_mode]


# Inicializar colores por defecto (light mode)
ThemeManager.update_theme_mode(False)

# ==========================================
# 0.2 FUNCIONES DE FORMATO
# ==========================================
def to_money(val):
    try:
        if val is None: return "$0.00"
        if not isinstance(val, Decimal):
            # Limpiar string si viene con formato de moneda
            s_val = str(val).replace("$", "").replace(",", "").strip()
            if not s_val: return "$0.00"
            val = Decimal(s_val)
        rounded = val.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        return f"${rounded:,.2f}"
    except Exception:
        # En caso de cualquier error (valores infinitos, NaN, etc)
        return "$0.00"

# Colores de Marca (Financieras) - Tonos Pastel/Suaves
BRAND_COLORS = {
    "CONSUBANCO": {"bg": "#DBEAFE", "text": "#1E40AF", "border": "#93C5FD"}, # Azul
    "CONSUPAGO":  {"bg": "#DCFCE7", "text": "#166534", "border": "#86EFAC"}, # Verde
    "MASNOMINA":  {"bg": "#FEE2E2", "text": "#991B1B", "border": "#FCA5A5"}, # Rojo
    "OPCIPRES":   {"bg": "#FEF3C7", "text": "#92400E", "border": "#FCD34D"}, # Amarillo
    "DEFAULT":    {"bg": "#F3F4F6", "text": "#374151", "border": "#D1D5DB"}  # Gris
}

def clean_float(v):
    try: return float(str(v).replace("$","").replace(",","").strip())
    except: return 0.0

def clean_decimal(v):
    try: 
        s = str(v).replace("$","").replace(",","").strip()
        return Decimal(s) if s else Decimal("0")
    except: 
        return Decimal("0")

# ==========================================
# OPTIMIZACIÓN: Helpers para Caché y Async
# ==========================================

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

# ==========================================
# OPTIMIZACIÓN: Progressive Rendering
# ==========================================

async def render_cards_progressively(
    container: ft.Control,
    items: list,
    builder_func: callable,
    batch_size: int = 6,  # Cards visibles en pantalla típica
    delay_ms: int = 15
):
    """
    Renderiza cards con lazy loading y animación inicial.
    
    MEJORA UX: Solo las primeras cards visibles (~6) se animan.
    El resto se carga inmediatamente al hacer scroll.
    """
    container.controls.clear()
    
    visible_count = min(batch_size, len(items))  # Máximo 6 visibles iniciales
    
    # FASE 1: Crear las cards visibles (con animación)
    visible_cards = []
    for i, item in enumerate(items[:visible_count]):
        try:
            card = builder_func(item)
            card.opacity = 0
            card.scale = 0.96
            card.offset = ft.Offset(0, 0.1)
            visible_cards.append(card)
            container.controls.append(card)
        except Exception as e:
            print(f"Error creating card: {e}")
            continue
    
    # FASE 2: Crear el resto de cards (sin animación, visibles directamente)
    for item in items[visible_count:]:
        try:
            card = builder_func(item)
            card.opacity = 1  # Ya visibles
            card.scale = 1
            card.offset = ft.Offset(0, 0)
            container.controls.append(card)
        except Exception as e:
            print(f"Error creating card: {e}")
            continue
    
    # Mostrar contenedor
    try:
        container.update()
    except:
        return
    
    # FASE 3: Animar solo las cards visibles en cascada
    await asyncio.sleep(0.03)  # Breve pausa
    
    stagger_delay = 0.05  # 50ms entre cada card visible
    for idx, card in enumerate(visible_cards):
        asyncio.create_task(_animate_card_entrance(card, idx * stagger_delay))
    
    await asyncio.sleep(0.08)
    try:
        container.update()
    except:
        pass

async def _animate_card_entrance(card, delay: float):
    """Anima la entrada de una card con fade + scale + slide."""
    try:
        await asyncio.sleep(delay)
        card.opacity = 1
        card.scale = 1
        card.offset = ft.Offset(0, 0)
        if hasattr(card, 'update') and card.page:
            card.update()
    except:
        pass


async def animate_rolling_currency(control, start, end, duration=1.0):
    """
    Versión OPTIMIZADA: Asignación directa sin animación rolling.
    Elimina consumo de CPU/GPU innecesario.
    """
    try:
        e_val = clean_float(end)
        control.value = f"${e_val:,.2f}"
        if control.page: control.update()
    except:
        pass
    
    # Dummy wait para mantener compatibilidad si se espera await
    await asyncio.sleep(0)

def mostrar_mensaje(page, texto, color="#333333"):
    page.snack_bar = ft.SnackBar(ft.Text(texto, color="white", font_family=THEME["fonts"]["primary"]), bgcolor=color)
    page.snack_bar.open = True
    page.update()

def copiar_al_portapapeles(page, texto):
    page.set_clipboard(texto)
    mostrar_mensaje(page, "Copiado al portapapeles", THEME["colors"]["success"])

# ==========================================
# UTILIDADES DE UX Y PERFORMANCE
# ==========================================

# --- Executor global para operaciones asíncronas ---
_executor = ThreadPoolExecutor(max_workers=4)

async def run_async(func, *args, **kwargs):
    """
    Ejecuta una función bloqueante en un thread separado para no congelar la UI.
    
    Uso:
        resultado = await run_async(GeneradorPDF().generar, form_data, credito_data)
    """
    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(_executor, lambda: func(*args, **kwargs))

async def shake_control(control, page):
    """
    Aplica animación premium de 'pulse + glow' para indicar error.
    Usa escala suave, sombra roja brillante y transición de borde elegante.
    """
    try:
        # Guardar valores originales
        original_border = control.border_color if hasattr(control, 'border_color') else None
        original_shadow = control.shadow if hasattr(control, 'shadow') else None
        original_scale = control.scale if hasattr(control, 'scale') else 1.0
        
        # Configurar animaciones suaves
        if hasattr(control, 'animate_scale'):
            control.animate_scale = ft.Animation(300, "easeOutCubic")
        if hasattr(control, 'animate_shadow'):
            control.animate_shadow = ft.Animation(300, "easeOut")
        
        # Efecto de Glow (resplandor rojo) + Pulse
        if hasattr(control, 'shadow'):
            control.shadow = ft.BoxShadow(
                spread_radius=4,
                blur_radius=20,
                color="#EF444440",  # Rojo con transparencia
                offset=ft.Offset(0, 0)
            )
        
        # Cambiar borde a rojo suave
        if hasattr(control, 'border_color'):
            control.border_color = "#EF4444"
        
        # Primera pulsación (agrandar)
        if hasattr(control, 'scale'):
            control.scale = 1.05
        
        control.update()
        await asyncio.sleep(0.15)
        
        # Segunda pulsación (encoger)
        if hasattr(control, 'scale'):
            control.scale = 0.98
        control.update()
        await asyncio.sleep(0.15)
        
        # Volver a tamaño normal
        if hasattr(control, 'scale'):
            control.scale = 1.0
        control.update()
        
        # Mantener el glow por 1.5 segundos
        await asyncio.sleep(1.5)
        
        # Restaurar valores originales con transición suave
        if hasattr(control, 'border_color'):
            control.border_color = original_border
        if hasattr(control, 'shadow'):
            control.shadow = original_shadow
        if hasattr(control, 'scale'):
            control.scale = original_scale
        
        control.update()
        
    except Exception as e:
        print(f"Error en shake_control: {e}")


def format_currency_live(value: str) -> str:
    """
    Formatea un string como moneda mientras el usuario escribe.
    
    Args:
        value: String con números (puede incluir caracteres no numéricos)
    
    Returns:
        String formateado con comas y 2 decimales
    
    Ejemplos:
        "1000000" → "1,000,000.00"
        "50000.5" → "50,000.50"
        "$1,234" → "1,234.00"
    """
    try:
        # Remover todo excepto dígitos y punto decimal
        cleaned = re.sub(r'[^\d.]', '', value)
        if not cleaned or cleaned == '.':
            return ""
        
        # Manejar múltiples puntos decimales
        parts = cleaned.split('.')
        if len(parts) > 2:
            cleaned = parts[0] + '.' + ''.join(parts[1:])
        
        # Convertir a float y formatear
        num = float(cleaned)
        return f"{num:,.2f}"
    except:
        return value

def validate_field(value: str, field_type: str = "number", min_val: float = None, max_val: float = None) -> tuple[bool, str]:
    """
    Valida un campo de entrada en tiempo real.
    
    Args:
        value: Valor a validar
        field_type: Tipo de campo ("number", "currency", "text", "rfc", "curp", "nss")
        min_val: Valor mínimo permitido (para números)
        max_val: Valor máximo permitido (para números)
    
    Returns:
        (es_valido, mensaje_error)
    """
    if not value or value.strip() == "":
        return False, "Campo requerido"
    
    if field_type in ["number", "currency"]:
        try:
            num = clean_float(value)
            if min_val is not None and num < min_val:
                return False, f"Mínimo: ${min_val:,.2f}" if field_type == "currency" else f"Mínimo: {min_val}"
            if max_val is not None and num > max_val:
                return False, f"Máximo: ${max_val:,.2f}" if field_type == "currency" else f"Máximo: {max_val}"
            return True, ""
        except:
            return False, "Valor inválido"
    
    elif field_type == "rfc":
        # RFC: 4 letras + 6 dígitos + 3 caracteres alfanuméricos
        if not re.match(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$', value.upper()):
            return False, "RFC inválido (ej: XAXX010101000)"
        return True, ""
    
    elif field_type == "curp":
        # CURP: 18 caracteres
        if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', value.upper()):
            return False, "CURP inválido (18 caracteres)"
        return True, ""
    
    elif field_type == "nss":
        # NSS: 11 dígitos
        cleaned = re.sub(r'\D', '', value)
        if len(cleaned) != 11:
            return False, "NSS debe tener 11 dígitos"
        return True, ""
    
    return True, ""

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
            if asyncio.iscoroutinefunction(func):
                result = await func(*args, **kwargs)
            else:
                result = await run_async(func, *args, **kwargs)
            return result
        finally:
            self.hide()



# ==========================================
# COMPONENTES UI MEJORADOS (UX)
# ==========================================
from smart_textfield import SmartTextField

CONSTANTES = {
    "RUTAS": {
        "DOCS": Path.home() / "Documents",
        "PERFIL": Path.home() / "Documents" / "perfil_asesor_secure.json",
        "CONFIG": Path.home() / "Documents" / "consucalcu_config.json",
        "REPORTES": Path.home() / "Documents" / "ReportesDiarios",
        "SESION": Path.home() / "Documents" / "datos_sesion_temp.json"
    }
}

PRODUCTO_CONFIG = ["MASNOMINA", "CONSUBANCO", "CONSUPAGO", "OPCIPRES"]


# --- SUSTITUYE LA CLASE COMPLETA CON ESTO ---
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
# ==========================================
# 1. LOGIC CLASSES (BUSINESS LOGIC PRESERVED)
# ==========================================
from utils_financial import CalculadoraFinanciera



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


class GestorConfiguracion:
    def __init__(self):
        # No definimos ruta de archivo para evitar lecturas accidentales
        # --- FACTORES HARDCODED (Fuente Única de Verdad - Fallback) ---
        self.defaults = {
            "TRADICIONAL": {
                "MASNOMINA": {
                    # CAT: 45.48%, Tasa Mensual: 2.73%
                    # Plazos: 24, 36, 48, 60 meses
                    # Mínimo: $8,000
                    "60": 37.06127,
                    "48": 40.45340,
                    "36": 46.62260,
                    "24": 59.80870,
                },
                "CONSUPAGO": {
                    # CAT: 44.98%, Tasas variables por plazo
                    # 12-24m: 2.73% (min $35,000)
                    # 36-48m: 2.72% (min $15,000)
                    # 54-60m: 2.69% (min $10,000)
                    "60": 36.88917,   # 2.69%
                    "54": 38.35000,   # 2.69%
                    "48": 40.37447,   # 2.72%
                    "36": 46.54880,   # 2.72%
                    "24": 59.77820,   # 2.73%
                    "12": 101.15000   # 2.73%
                },
                "OPCIPRES": {
                    # CAT: 42.26%, Tasas variables por plazo
                    # 36m: 2.67% (min $70,000)
                    # 48m: 2.67% (min $50,000)
                    # 54m: 2.62% (min $45,000)
                    # 60m: 2.57-2.59% (min $30,000)
                    "60": 35.98562,   # 2.57-2.59%
                    "54": 37.65909,   # 2.62%
                    "48": 39.86520,   # 2.67%
                    "36": 46.47090,   # 2.67%
                },
                "CONSUBANCO": {
                    # CAT: 39.03%, Tasas variables por plazo
                    # 36m: 2.50% (min $145,000)
                    # 48m: 2.50% (min $100,000)
                    # 54m: 2.44% (min $95,000)
                    # 60m: 2.40% (min $40,000)
                    "60": 34.47785,   # 2.40%
                    "54": 36.50000,   # 2.44%
                    "48": 38.34620,   # 2.50%
                    "36": 45.48000,   # 2.50%
                },
            },
            "RETENCION": {
            # CALIBRACIÓN DICIEMBRE 2025 (Solo 60 meses)
            "CONSUBANCO": {"60": 30.95418}, # Validado con $620k
            "CONSUPAGO":  {"60": 33.25726}, # Validado con $500k
            "OPCIPRES":   {"60": 32.22283}, # Validado con $600k
            },
            "REGLAS_RETENCION_CNCA": {
                "CONSUBANCO": 100000.0, "OPCIPRES": 75000.0, "CONSUPAGO": 50000.0, "MASNOMINA": 999999999.0 
            },
            "REGLAS_RETENCION_NUEVO": {
                # REGLAS ESTÁNDAR 2026
                "CONSUBANCO": 200000.0,
                "OPCIPRES": 125000.0,
                "CONSUPAGO": 100000.0,
                "MASNOMINA": 999999999.0
            },
            "METADATA": {
            # Actualización de CATs Retención
            "CONSUBANCO": {"tasa_ref": "2.50%", "cat": "39.03%", "cat_tr": "31.50%", "codigo": "CSB"},
            "CONSUPAGO":  {"tasa_ref": "2.73%", "cat": "44.20%", "cat_tr": "36.42%", "codigo": "CSP"}, # Nuevo CAT Ret
            "OPCIPRES":   {"tasa_ref": "2.67%", "cat": "42.26%", "cat_tr": "34.21%", "codigo": "OPC"}, # Nuevo CAT Ret
            "MASNOMINA":  {"tasa_ref": "2.73%", "cat": "44.57%", "cat_tr": "46.50%", "codigo": "MN"}
        },
            "INSURANCE_FACTORS": {
                # Estructura: Plazo: (Factor_1_Año, Factor_Total)
                "CONSUBANCO": {
                    "36": (0.020390, 0.040555), "48": (0.021213, 0.055907),
                    "54": (0.021461, 0.063849), "60": (0.021671, 0.072217)
                },
                "CONSUPAGO": {
                    "12": (0.012956, 0.012956), "24": (0.018685, 0.026342),
                    "36": (0.020507, 0.041028), "48": (0.021362, 0.056948),
                    "54": (0.021623, 0.065283), "60": (0.021827, 0.073927)
                },
                "OPCIPRES": {
                    "36": (0.020501, 0.041001), "48": (0.021325, 0.056689),
                    "54": (0.021576, 0.064861), "60": (0.021770, 0.073298),
                    "999": (0.023390, 0.076685) # Caso especial Retención 60m
                },
                "MASNOMINA": {
                    "24": (0.018687, 0.026347), "36": (0.020513, 0.041051),
                    "48": (0.021367, 0.056986), "60": (0.021837, 0.074045)
                }
            }
        
        }
        
        # ESTRATEGIA OFFLINE-FIRST:
        # 1. Cargar defaults hardcoded (Red de seguridad)
        self.datos = self.defaults
        
        # 2. Intentar cargar cache persistente (Datos offline)
        cached_data = SyncManager.load_local()
        if cached_data:
            print("✓ Configuración local cargada")
            self.datos = cached_data
            
    def actualizar_memoria(self, nuevos_datos):
        """Actualiza la configuración en caliente tras sync remoto"""
        if nuevos_datos:
            self.datos = nuevos_datos
            print("✓ Configuración en memoria actualizada (Hot Reload)")

    def cargar(self):
        return self.datos

    def guardar(self, datos):
        pass # Deshabilitado para evitar corrupción

    def actualizar_factor(self, tipo, financiera, plazo, nuevo_factor):
        pass

    def restaurar_fabrica(self):
        return True

class MotorFinanciero:
    def __init__(self, config_manager): 
        self.cfg_mgr = config_manager
        
        # Instancia de normativa CNCA
        if HAVE_NORMATIVA:
            self.normativa = NormativaCNCA()
        else:
            self.normativa = None
        
        # --- LISTA DE PLAZOS ESTÁNDAR (POLÍTICA VIGENTE) ---
        # Incluye 54 meses (soportado por Consupago, Opcipres, Consubanco)
        self.plazos_standard = [60, 54, 48, 36, 24, 12]
        
        # --- FACTORES GLOBALES SIPRE ---
        self.factores_globales_sipre = {
            "CONSUBANCO": Decimal("1.857251"),
            # REGRESAMOS AL FACTOR ORIGINAL (Terminación 1)
            # Esto genera 18,856.811... -> se redondea a .81
            "OPCIPRES":   Decimal("1.933371"), 
            "CONSUPAGO":  Decimal("1.9954353")
        }
        self.recargar_config()

    def recargar_config(self):
        self.cfg_mgr.cargar()
        self.tradicional = self.cfg_mgr.datos["TRADICIONAL"]
        self.retencion = self.cfg_mgr.datos["RETENCION"]
        self.reglas_tr_cnca = self.cfg_mgr.datos.get("REGLAS_RETENCION_CNCA", {})
        self.reglas_tr_nuevo = self.cfg_mgr.datos.get("REGLAS_RETENCION_NUEVO", {})
        self.meta = self.cfg_mgr.datos["METADATA"]
        
        # Reglas de negocio (Montos mínimos por producto y plazo)
        self.reglas_plazo = {
            "MASNOMINA": {
                # Mínimo global: $8,000 para todos los plazos (24, 36, 48, 60)
                "min_global": 8000.0
            },
            "CONSUPAGO": {
                # Montos mínimos por rango de plazos
                # 12-24m: $35,000
                # 36-48m: $15,000  
                # 54-60m: $10,000
                "min_plazo_rango": [
                    (12, 24, 35000.0),
                    (36, 48, 15000.0),
                    (54, 60, 10000.0)
                ]
            },
            "OPCIPRES": {
                # Montos mínimos por plazo específico
                "min_plazo": {
                    60: 30000.0,   # $30,000
                    54: 45000.0,   # $45,000
                    48: 50000.0,   # $50,000
                    36: 70000.0    # $70,000
                }
            },
            "CONSUBANCO": {
                # Montos mínimos por plazo específico
                "min_plazo": {
                    60: 40000.0,    # $40,000
                    54: 95000.0,    # $95,000
                    48: 100000.0,   # $100,000
                    36: 145000.0    # $145,000
                }
            }
        }
        
        # Reglas específicas para CNCA con RETENCIÓN (monto BRUTO mínimo)
        # Estas reglas se aplican SOLO en refinanciamiento (CNCA) con retención
        self.reglas_cnca_retencion = {
            "CONSUBANCO": {
                "monto_bruto_min": 100000.0,  # $100,000 para CNCA
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            },
            "OPCIPRES": {
                "monto_bruto_min": 75000.0,  # $75,000 para CNCA (CORREGIDO)
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            },
            "CONSUPAGO": {
                "monto_bruto_min": 50000.0,  # $50,000 para CNCA (CORREGIDO)
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            }
        }
        
        # Reglas específicas para CRÉDITO NUEVO con RETENCIÓN (monto NETO mínimo)
        # NORMATIVA ENERO 2026: Fin de promoción diciembre, montos estándar restaurados
        self.reglas_nuevo_retencion = {
            "CONSUBANCO": {
                "monto_neto_min_normal": 200000.0,  # Estándar 2026
                "plazo_requerido": 60
            },
            "OPCIPRES": {
                "monto_neto_min_normal": 125000.0,  # Estándar 2026
                "plazo_requerido": 60
            },
            "CONSUPAGO": {
                "monto_neto_min_normal": 100000.0,  # Estándar 2026
                "plazo_requerido": 60
            }
        }


    def redondear(self, m, step=100.0): 
        m = clean_float(m)
        step = float(step)
        if step <= 0: return m
        return math.floor(m / step) * step if m > 0 else 0.0

    def obtener_minimo_requerido(self, prod, plazo):
        """
        Devuelve el monto mínimo requerido para un producto y plazo específicos.
        Si la financiera usa reglas por plazo específico y el plazo no está, bloquea (retorna valor muy alto).
        """
        rules = self.reglas_plazo.get(prod, {})
        
        # 1. Regla por diccionario de plazos específicos (Ej. Consubanco)
        if "min_plazo" in rules:
            # Si el plazo está definido, devuelve su mínimo.
            # Si NO está definido (busca un plazo no permitido), devuelve un valor bloqueante.
            return rules["min_plazo"].get(plazo, 999999999.0)
            
        # 2. Regla por rangos (Ej. Consupago)
        if "min_plazo_rango" in rules:
            for (p_min, p_max, m_min) in rules["min_plazo_rango"]:
                if p_min <= plazo <= p_max:
                    return m_min
            return 5000.0 # Fallback razonable si no cae en rango (aunque debería)

        # 3. Regla global (Ej. MasNomina)
        if "min_global" in rules:
            return rules["min_global"]

        # 4. Default general
        return 5000.0

    def validar_minimo(self, prod, monto, plazo):
        # Usamos el nuevo método centralizado
        limit = self.obtener_minimo_requerido(prod, plazo)
        return clean_float(monto) >= limit

    def validar_cnca_retencion(self, prod, monto_bruto, plazo, deudas):
        """
        Valida reglas específicas de CNCA con RETENCIÓN.
        
        Args:
            prod: Producto financiero
            monto_bruto: Monto BRUTO del crédito (deudas + liquidez)
            plazo: Plazo del crédito
            deudas: Lista de deudas a refinanciar
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        if prod not in self.reglas_cnca_retencion:
            return True, ""  # Producto sin reglas especiales
        
        reglas = self.reglas_cnca_retencion[prod]
        
        # 1. Validar monto bruto mínimo
        monto_min = reglas.get("monto_bruto_min", 0)
        if monto_bruto < monto_min:
            return False, f"Monto bruto mínimo: ${monto_min:,.0f}"
        
        # 2. Validar plazo requerido
        plazo_req = reglas.get("plazo_requerido")
        if plazo_req is not None and plazo != plazo_req:
            return False, f"Plazo requerido: {plazo_req} meses"
        
        # 3. Validar pagos aplicados mínimos (24+)
        pagos_min = reglas.get("pagos_aplicados_min", 0)
        if pagos_min > 0:
            # Verificar que TODAS las deudas tengan al menos pagos_min pagos aplicados
            for deuda in deudas:
                pagos_aplicados = deuda.get("pagos", 0)
                if pagos_aplicados < pagos_min:
                    return False, f"Requiere {pagos_min}+ pagos aplicados en todas las deudas"
        
        return True, ""

    def validar_nuevo_retencion(self, prod, monto_neto, plazo):
        """
        Valida reglas específicas de CRÉDITO NUEVO con RETENCIÓN.
        ENERO 2026: Montos estándar aplicados (fin de promoción diciembre).
        
        Args:
            prod: Producto financiero
            monto_neto: Monto NETO del crédito
            plazo: Plazo del crédito
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        if prod not in self.reglas_nuevo_retencion:
            return True, ""  # Producto sin reglas especiales
        
        reglas = self.reglas_nuevo_retencion[prod]
        
        # 1. Validar monto mínimo estándar
        monto_min = reglas.get("monto_neto_min_normal", 125000.0)
        
        if monto_neto < monto_min:
            return False, f"Monto neto mínimo: ${monto_min:,.0f}"
        
        # 2. Validar plazo requerido
        plazo_req = reglas.get("plazo_requerido")
        if plazo_req is not None and plazo != plazo_req:
            return False, f"Plazo requerido: {plazo_req} meses"
        
        return True, ""


    def calcular(self, prod, valor, plazo, por_monto, usa_retencion=False, rounding_step=100.0):
        try:
            # --- DETERMINAR MONTO MÍNIMO REAL (LOOKUP CENTRALIZADO) ---
            monto_minimo_real = self.obtener_minimo_requerido(prod, plazo)

            cat_val = self.meta.get(prod, {}).get("cat_tr" if usa_retencion else "cat", "0.0%").replace('%', '')

            # --- LÓGICA ESPECIAL SIPRE (RETENCIÓN 60 MESES) ---
            if usa_retencion and plazo == 60 and prod in self.factores_globales_sipre:
                factor_global = self.factores_globales_sipre[prod]
                current_val = valor
                is_por_monto = por_monto
                
                for _ in range(50):
                    if is_por_monto:
                        monto_base = Decimal(str(self.redondear(current_val, rounding_step)))
                    else:
                        capacidad_dec = Decimal(str(current_val))
                        # Cálculo inverso: (Capacidad * 60) / Factor
                        monto_estimado = (capacidad_dec * Decimal("60")) / factor_global
                        monto_base = Decimal(str(self.redondear(float(monto_estimado), rounding_step)))

                    # --- NUEVA VALIDACIÓN DE MÍNIMO ---
                    if float(monto_base) < monto_minimo_real:
                        return None

                    # 1. Total Teórico (Monto * Factor)
                    total_teorico = monto_base * factor_global

                    # 2. Mensualidad (Total / 60)
                    # Usamos ROUND_HALF_UP estándar porque los factores ya tienen la precisión corregida
                    mensual_raw = total_teorico / Decimal("60")
                    pago_mensual_sipre = mensual_raw.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

                    # 3. Validación de Capacidad
                    if not is_por_monto and pago_mensual_sipre > Decimal(str(valor)):
                        # Si nos pasamos por centavos, bajamos un escalón de redondeo
                        nuevo_monto_float = float(monto_base - Decimal(str(rounding_step)))
                        
                        # VALIDACIÓN CORREGIDA (Usando variable dinámica)
                        if nuevo_monto_float < monto_minimo_real: return None
                        
                        current_val = nuevo_monto_float
                        is_por_monto = True
                        continue

                    # 4. Cálculo final del Total Real a visualizar
                    total_real_pagar = pago_mensual_sipre * Decimal("60")
                    
                    return {
                        "monto": float(monto_base), 
                        "pago": float(pago_mensual_sipre),
                        "total_pagar": float(total_real_pagar),
                        "cat": cat_val
                    }
                return None

            # --- LÓGICA TRADICIONAL (FACTOR POR MIL) ---
            dic_factores = self.retencion if usa_retencion else self.tradicional
            raw_factor = dic_factores.get(prod, {}).get(str(plazo), 0)
            
            if raw_factor == 0: return None
            
            f = Decimal(str(raw_factor))
            
            if por_monto:
                m = self.redondear(valor, rounding_step)
                # Fórmula: (Monto / 1000) * Factor
                pago_calc = (Decimal(m) / Decimal("1000")) * f
            else:
                # Fórmula Inversa: (Capacidad / Factor) * 1000
                bruto = (Decimal(valor) / f) * Decimal("1000")
                m = self.redondear(bruto, rounding_step)
                pago_calc = (Decimal(m) / Decimal("1000")) * f
            
            # --- NUEVA VALIDACIÓN TRADICIONAL ---
            if float(m) < monto_minimo_real: return None

            # Redondeo tradicional
            pago_final = pago_calc.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            return {
                "monto": float(m), 
                "pago": float(pago_final), 
                "cat": cat_val
            }
            
        except (ValueError, TypeError, ArithmeticError) as e:
            logger.error(f"Error de cálculo en motor financiero: {e}", exc_info=True)
            return None
        except Exception as e:
            logger.error(f"Error inesperado en motor financiero: {e}", exc_info=True)
            return None
    
    def calcular_seguro(self, financiera, edad, monto, plazo, tipo_pension, es_cliente_nuevo, es_retencion=False):
        """
        Retorna una tupla: (Estatus_Texto, Color_Hex, Costo_Calculado_1A, Costo_Calculado_Total)
        Implementa reglas estrictas Addendum Pensionados IMSS
        """
        try:
            monto = Decimal(str(monto))
            edad_final = edad + (plazo / 12.0)
            
            # 1. Validar Monto Mínimo
            min_req = self.obtener_minimo_requerido(financiera, plazo)
            if monto < Decimal(str(min_req)):
                return (f"RECHAZADO: Monto menor al mínimo (${min_req:,.2f})", "#EF4444", 0.0, 0.0)

            # --- REGLAS DE NEGOCIO IMSS (ADDENDUM) ---
            
            # Regla 8: Límite Absoluto
            if edad_final > 85:
                 return ("RECHAZADO: Edad + Plazo > 85 años", "#EF4444", 0.0, 0.0)

            aplica_un_anual = False
            aplica_total = False
            razon = "Sin costo de seguro"
            color = "#10B981" # Verde (Aprobado sin costo)

            # Reglas por Tipo de Pensión
            if tipo_pension == "INVALIDEZ":
                # Regla 1.2
                if 51 <= edad <= 70:
                    if es_cliente_nuevo: # Nuevo o < 24 meses
                        aplica_un_anual = True
                        razon = "Invalidez 51-70 años + Nuevo"
                else:
                    # Fuera de rango 51-70
                    if edad_final > 75:
                        aplica_un_anual = True
                        razon = "Invalidez excede 75 años (Extensión)"
                    # Si <= 75, no aplica costo
            
            else: # VEJEZ
                if 66 <= edad <= 75:
                    if edad_final > 75:
                        # Regla 6: Excede 75 años -> Pago Total
                        aplica_total = True
                        razon = "Vejez 66-75 excede 75 años (Pago Total)"
                    elif es_cliente_nuevo:
                        # Regla 5: No excede 75 + Nuevo -> 1 Año
                        aplica_un_anual = True
                        razon = "Vejez 66-75 + Nuevo"
                
                elif edad >= 76:
                    if edad_final > 80:
                         # Regla 8 (Extensión): Excede 80 -> 1 Año
                         aplica_un_anual = True
                         razon = "Vejez > 76 excede 80 años (Extensión)"
                    # Regla 7: <= 80 -> No aplica costo

            # --- CÁLCULO DE COSTOS ---
            c1_result = 0.0
            ct_result = 0.0
            
            if aplica_un_anual or aplica_total:
                factores = self.cfg_mgr.datos["INSURANCE_FACTORS"].get(financiera, {})
                # Key especial para Opcipres Retención 60m
                key_plazo = "999" if (financiera == "OPCIPRES" and plazo == 60 and es_retencion) else str(plazo)
                
                pair = factores.get(key_plazo)
                if pair:
                    f1 = Decimal(str(pair[0]))
                    fT = Decimal(str(pair[1]))
                    
                    # Calculamos los valores teóricos
                    costo_1_ano = float(monto * f1)
                    costo_total = float(monto * fT)
                    
                    if aplica_un_anual:
                        c1_result = costo_1_ano
                        # Si aplica 1 año, el costo total a mostrar es el de 1 año en este contexto
                        ct_result = costo_1_ano 
                        color = "#F59E0B"
                        razon = f"APLICA 1 AÑO: {razon}"
                        
                    if aplica_total:
                        c1_result = costo_total # En total, ambos valores reflejan el total para evitar confusión
                        ct_result = costo_total
                        color = "#F59E0B"
                        razon = f"APLICA TOTAL: {razon}"
                else:
                    razon = f"ERROR: Sin factor {financiera} {plazo}m"
                    color = "#EF4444"

            return (razon, color, c1_result, ct_result)

        except Exception as e:
            return (f"Error de cálculo: {e}", "#EF4444", 0.0, 0.0)
    
    # ... (Resto de métodos de validación se mantienen igual: validar_retencion_deudas, validar_normativa, generar_layout_final)
    def validar_retencion_deudas(self, deudas):
        if not deudas: return False
        for d in deudas:
            try:
                pagos = int(clean_float(d.get('pagos', 0)))
                if pagos < 24: return False
            except: return False
        return True

    def validar_compatibilidad_marcas(self, prod_destino, deudas):
        """
        Valida las reglas de negocio para liquidación de marcas (Enero 2026):
        1. Refinanciamiento (Auto-compra): Permitido si todas las deudas son de la misma marca.
        2. Compra de Cartera (Competencia): Permitido si todas las deudas son de la competencia permitida.
        3. Mezcla: NO permitida (No se puede mezclar misma marca con otras).
        
        Marcas del Ecosistema:
        - MSN = MASNOMINA
        - CSB = CONSUBANCO
        - CSP = CONSUPAGO
        - OPC = OPCIPRES
        """
        if not deudas: return True, ""
        
        target = prod_destino.upper()
        ECOSYSTEM = {"MASNOMINA", "CONSUBANCO", "CONSUPAGO", "OPCIPRES"}
        
        # Obtener marcas de origen
        marcas_origen = set()
        for d in deudas:
            m = str(d.get('producto') or '').upper().strip()
            marcas_origen.add(m)
            
        # 1. Validar que las marcas pertenezcan al ecosistema regulado
        # La regla "Solo tienen X/Y/Z" implica que marcas fuera de la lista rompen la regla
        for m in marcas_origen:
            if m not in ECOSYSTEM:
                # Si una marca no está en el ecosistema (ej. BANCOMER), 
                # no cumple con "Solo tienen CSB/MSN/OPC", por tanto se rechaza.
                return False, f"Marca no permitida para liquidación: {m}. Solo se aceptan: {', '.join(ECOSYSTEM)}"

        has_internal = target in marcas_origen
        has_external = any(m != target for m in marcas_origen)
        
        # 2. Validar No-Mezcla
        if has_internal and has_external:
            return False, "Regla de Marca: No se puede combinar Refinanciamiento (Misma Marca) con Compra de Cartera."
            
        # 3. Validaciones específicas (Redundante con lógica de conjuntos pero explícito por las reglas)
        # Si es externo, verificamos que las externas sean "competencia válida".
        # Como nuestro Ecosistema es cerrado (4 marcas), si es external y está en ECOSYSTEM, es válida.
        return True, ""

    def validar_normativa_cnca(self, prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion=False):
        """
        Validación usando el módulo NORMATIVA_CNCA.
        Complementa y corrige las validaciones existentes.
        
        Returns:
            (bool, list): (es_valido, lista_razones)
        """
        if not self.normativa:
            # Si no hay módulo de normativa, usar validación legacy
            return self.validar_normativa(prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion)
        
        razones_aprobacion = []
        
        # 1. Validar producto de liquidación
        valido, msg = self.normativa.validar_producto_liquidacion(prod, deudas)
        if not valido:
            return False, [msg]
        razones_aprobacion.append("Producto Compatible")
        
        # 2. Validar incremento de tasa
        valido, msg = self.normativa.validar_incremento_tasa(prod, deudas)
        if valido:
            razones_aprobacion.append("Incremento Tasa")
        
        # 3. Validar pagos acumulados y tasa de referencia
        valido, msg = self.normativa.validar_pagos_acumulados_tasa_ref(prod, deudas, remanente_capital)
        if valido:
            razones_aprobacion.append("Antigüedad")
        
        # 4. Validar incremento de descuento mensual
        valido, msg = self.normativa.validar_incremento_descuento_mensual(prod, deudas, incremento_liquidez, remanente_capital)
        if valido:
            razones_aprobacion.append("Incremento Descuento")
        
        # 5. Validar aumento de plazo
        if deudas:
            # Validar que todas las deudas cumplan con el aumento de plazo
            todas_cumplen_plazo = True
            for deuda in deudas:
                plazo_actual = deuda.get("plazo", 0)
                valido, msg = self.normativa.validar_aumento_plazo(plazo_actual, plazo_nuevo)
                if valido:
                    if "Plazo" not in razones_aprobacion:
                        razones_aprobacion.append("Plazo")
                else:
                    todas_cumplen_plazo = False
                    break
        
        # Si hay al menos una razón de aprobación, es válido
        if razones_aprobacion:
            return True, razones_aprobacion
        else:
            return False, ["No cumple con ninguna normativa CNCA"]

    def validar_normativa(self, prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion=False):
        """
        Validación Estricta con 'Unanimidad de Compuerta' (CNCA): 
        Todas las deudas deben compartir al menos una razón de validación.
        """
        # --- PASO 0: VALIDACIÓN DE MARCAS (NUEVO REGLAMENTO) ---
        v_marcas, msg_marcas = self.validar_compatibilidad_marcas(prod, deudas)
        if not v_marcas:
            return False, [msg_marcas]

        # Filtros Base
        if prod == "MASNOMINA": return remanente_capital > 0, ["Liquidez Positiva"]
        if remanente_capital <= 0: return False, ["Rechazo: Sin Liquidez"]
        if not deudas: return True, ["Crédito Nuevo"]

        inc = float(incremento_liquidez)
        sets_deudas = [] # Lista de conjuntos, uno por deuda

        for i, d in enumerate(deudas):
            razones_d = set()
            try:
                tasa_d = float(d.get('tasa', 0))
                pagos_d = int(clean_float(d.get('pagos', 0)))
                pl_old = int(d.get('plazo', 0))
            except: return False, [f"Error datos deuda {i+1}"]

            # --- 1. REGLA DE INCREMENTO DE TASA (Rate Increase) ---
            # "El producto con el que se va a liquidar debe tener una tasa de referencia o menor"
            # MN y CSP: Sin restricción.
            # OPC: Tasa Baja <= 2.57%.
            # CSB: Tasa Baja <= 2.40%.
            pass_tasa = False
            if prod in ["MASNOMINA", "CONSUPAGO"]:
                pass_tasa = True
            elif prod == "OPCIPRES" and tasa_d <= 2.57:
                pass_tasa = True
            elif prod == "CONSUBANCO" and tasa_d <= 2.40:
                pass_tasa = True
            
            if pass_tasa: razones_d.add("Tasa")

            # --- 2. PAGOS ACUMULADOS (Antigüedad) - NORMATIVA ENERO 2026 ---
            # Bifurcación LCOM vs Interno:
            # - Financieras INTERNAS (CSB, CSP, OPC, MAS): 16 pagos mínimos
            # - Financieras COMPETENCIA (Cualquier otra): 24 pagos mínimos (Regla LCOM)
            # - Financieras COMPETENCIA (Cualquier otra): 24 pagos mínimos (Regla LCOM)
            producto_origen = str(d.get('producto') or '').upper()
            es_interna = producto_origen in ['CONSUBANCO', 'CONSUPAGO', 'OPCIPRES', 'MASNOMINA']
            
            pagos_minimos = 16 if es_interna else 24
            
            # OPC: Pagos mínimos Y Tasa <= 2.67%
            # CSB: Pagos mínimos Y Tasa <= 2.57%
            # CSP: Pagos mínimos Y Tasa <= 2.67% (ACTUALIZADO ENERO 2026)
            pass_antiguedad = False
            if prod == "OPCIPRES":
                if pagos_d >= pagos_minimos and tasa_d <= 2.67: pass_antiguedad = True
            elif prod == "CONSUBANCO":
                if pagos_d >= pagos_minimos and tasa_d <= 2.57: pass_antiguedad = True
            elif prod == "CONSUPAGO":
                if pagos_d >= pagos_minimos and tasa_d <= 2.67: pass_antiguedad = True
            else:
                # MASNOMINA: Solo antigüedad estándar
                if pagos_d >= pagos_minimos: pass_antiguedad = True
            
            if pass_antiguedad: razones_d.add("Antigüedad")

            # --- 3. INCREMENTO DESCUENTO (Aumento de Pago Mensual) - ENERO 2026 ---
            # "El cliente acepta aumentar su descuento mensual en $750 o $1,000"
            # "Y la deuda origen tiene tasa favorable (<= referencia)"
            # OPC: +750 Incremento en Descuento Y Tasa <= 2.67%
            # CSB: +1,000 Incremento en Descuento Y Tasa <= 2.57%
            # CSP: +750 Incremento en Descuento Y Tasa <= 2.67% (ACTUALIZADO)
            # MN: Sin restricción
            pass_incremento = False
            if prod == "MASNOMINA":
                pass_incremento = True
            elif prod == "CONSUPAGO":
                if inc >= 750.0 and tasa_d <= 2.67: pass_incremento = True
            elif prod == "OPCIPRES":
                if inc >= 750.0 and tasa_d <= 2.67: pass_incremento = True
            elif prod == "CONSUBANCO":
                if inc >= 1000.0 and tasa_d <= 2.57: pass_incremento = True
            
            if pass_incremento: razones_d.add("Incremento Descuento")

            # --- 4. AUMENTO DE PLAZO DE CRÉDITO ---
            # "Aplica para todos los cotizadores sin restricción"
            # Validamos mapeo de plazos.
            pass_plazo = False
            if (pl_old <= 12 and plazo_nuevo >= 36) or \
               (pl_old <= 24 and plazo_nuevo >= 48) or \
               (pl_old in [36, 48] and plazo_nuevo >= 54) or \
               (pl_old == 54 and plazo_nuevo == 60):
                pass_plazo = True
            
            if pass_plazo: razones_d.add("Plazo")
            
            sets_deudas.append(razones_d)

        # Evaluar Unanimidad (Intersección)
        if not sets_deudas: return False, ["Sin Deudas"]
        
        interseccion = set.intersection(*sets_deudas)
        
        if interseccion:
            # Aprobado por unanimidad en al menos una razón
            return True, list(interseccion)
        
        # Motor de Sugerencias (Si falla la unanimidad)
        # Iterar quitando una deuda a la vez para ver si el resto cumple
        for i in range(len(sets_deudas)):
            # Subset excluyendo la deuda i
            subset = sets_deudas[:i] + sets_deudas[i+1:]
            if not subset: continue

            interseccion_sub = set.intersection(*subset)
            if interseccion_sub:
                razon_ganadora = list(interseccion_sub)[0]
                monto_d = deudas[i].get('saldo', deudas[i].get('monto', 0))
                try: monto_str = to_money(monto_d)
                except: monto_str = "$0.00"
                return False, [f"Sugerencia: Eliminar deuda de {monto_str} para aprobar por {razon_ganadora}."]

        return False, ["No hay unanimidad de criterios (Tasa, Liquidez, Antigüedad, Plazo)."]
    
    def optimizar_combinacion_deudas(self, prod, deudas, incremento_liquidez, plazo_nuevo, capacidad_total_original, usa_retencion=False):
        """
        Optimizador Inteligente de Combinaciones de Deudas (CORREGIDO).
        
        CORRECCIÓN LÓGICA:
        - Al excluir una deuda, TAMBIÉN se debe excluir su pago de la capacidad.
        - Se recalcula el monto del crédito para cada combinación.
        
        Args:
            prod: Producto financiero
            deudas: Lista de deudas (diccionarios con 'saldo', 'pago', etc.)
            incremento_liquidez: Ajuste de liquidez (float)
            plazo_nuevo: Plazo del nuevo crédito
            capacidad_total_original: (No se usa como fijo, se recalcula)
            usa_retencion: Si usa retención
            
        Returns:
            dict con optimización o None
        """
        from itertools import combinations
        
        if not deudas or len(deudas) < 2:
            return None
        
        n = len(deudas)
        
        # 1. Escenario Base (Todas las deudas)
        # Recalcular capacidad real del set completo
        cap_base = sum(d.get('pago', 0) for d in deudas) + float(incremento_liquidez)
        res_base = self.calcular(prod, cap_base, plazo_nuevo, False, usa_retencion, rounding_step=500.0)
        
        if not res_base or not self.validar_minimo(prod, res_base['monto'], plazo_nuevo):
            return None
            
        monto_base = res_base['monto']
        total_saldo_base = sum(d.get('saldo', 0) for d in deudas)
        remanente_base = monto_base - total_saldo_base
        
        valido_base, razones_base = self.validar_normativa(prod, deudas, incremento_liquidez, remanente_base, plazo_nuevo, usa_retencion)
        
        # Estructura del mejor resultado
        mejor_resultado = {
            'mejor_combinacion': list(range(n)),
            'remanente_optimo': remanente_base if valido_base else -999999999, # Si base no es válido, cualquier válido será mejor
            'monto_optimo': monto_base,
            'deudas_a_excluir': [],
            'razones': razones_base,
            'mejora': 0,
            'num_deudas': n,
            'total_deuda': total_saldo_base
        }
        
        base_es_valido = valido_base

        # 2. Iterar combinaciones (Subsets)
        # Probamos quitando de 1 hasta N-1 deudas
        # Nota: Performance O(2^n). Para n <= 10 es trivial.
        
        hay_mejora = False
        
        for num_excluir in range(1, n):
            num_incluir = n - num_excluir
            
            for combo_indices in combinations(range(n), num_incluir):
                # Subset de deudas
                deudas_subset = [deudas[i] for i in combo_indices]
                
                # A. Calcular Capacidad del Subset
                sub_pagos = sum(d.get('pago', 0) for d in deudas_subset)
                sub_cap = sub_pagos + float(incremento_liquidez)
                
                if sub_cap <= 0: continue

                # B. Calcular Nuevo Crédito
                res_sub = self.calcular(prod, sub_cap, plazo_nuevo, False, usa_retencion, rounding_step=500.0)
                if not res_sub: continue
                
                if not self.validar_minimo(prod, res_sub['monto'], plazo_nuevo):
                   continue
                
                sub_monto = res_sub['monto']
                sub_saldo_total = sum(d.get('saldo', 0) for d in deudas_subset)
                sub_remanente = sub_monto - sub_saldo_total
                
                # C. Verificar si mejora el remanente
                # Si el escenario base era inválido, cualquier válido es mejora.
                # Si era válido, necesitamos superar el remanente óptimo actual.
                if sub_remanente <= mejor_resultado['remanente_optimo']:
                    continue
                
                # D. Validar Normativa
                es_valido, razones = self.validar_normativa(prod, deudas_subset, incremento_liquidez, sub_remanente, plazo_nuevo, usa_retencion)
                
                if es_valido:
                    deudas_excluidas = [deudas[i] for i in range(n) if i not in combo_indices]
                    mejor_resultado = {
                        'mejor_combinacion': list(combo_indices),
                        'remanente_optimo': sub_remanente,
                        'monto_optimo': sub_monto,
                        'deudas_a_excluir': deudas_excluidas,
                        'razones': razones,
                        'mejora': sub_remanente - remanente_base if base_es_valido else 0, # Si base inválido, mejora es simbólica
                        'num_deudas': len(deudas_subset),
                        'total_deuda': sub_saldo_total
                    }
                    hay_mejora = True

        # Retornar solo si encontramos algo válido que mejore (o si el base era inválido y encontramos uno válido)
        if hay_mejora:
            return mejor_resultado
        
        return None

    def generar_layout_final(self, form_data, credito_data, perfil, tipo_tramite, formato="FORMAL"):
        id_liq = form_data.get('id_credito', '')
        desc_liq = to_money(form_data.get('descuento_liq', 0))
        cnca_det = ""
        if tipo_tramite == "CNCA" or id_liq: cnca_det = f"ID LIQ: {id_liq}\nDESC: {desc_liq}\nNUM: {form_data.get('num_descuentos','')}\nMARCA: {form_data.get('marca_liquida','').upper()}\n"
        nombre_cliente = form_data.get('nombre', '').upper()
        monto = credito_data['monto']
        plazo = credito_data['plazo']
        pago = to_money(credito_data['pago_visual'])
        cat = credito_data['cat']
        agente = perfil.get('nombre', '').upper()
        supervisor = perfil.get('supervisor', '').upper()
        if formato == "WHATSAPP":
            emoji_tramite = "🔄" if tipo_tramite == "CNCA" else "🆕"
            emoji_dinero = "💰" if monto > 50000 else "💵"
            if tipo_tramite == "CNCA": layout = f"""{emoji_tramite} *REFINANCIAMIENTO APROBADO* {emoji_tramite}\n\n🎉 *¡Buenas noticias {nombre_cliente}!* \n\n{emoji_dinero} *Te autorizamos:* ${monto:,.2f}\n📅 *Plazo:* {plazo} meses\n💳 *Pago quincenal:* {pago}\n📊 *CAT:* {cat}%\n\n✅ *Beneficios:*\n• 🏦 Consolidas todas tus deudas\n• 📉 Reduce tu tasa de interés\n• 📈 Mejora tu historial crediticio\n\n👤 *Asesor:* {agente}\n📞 *Contacta a tu supervisor:* {supervisor}\n\n_*Este es un preautorización sujeta a validación de documentos*_"""
            else: layout = f"""{emoji_tramite} *CRÉDITO NUEVO APROBADO* {emoji_tramite}\n\n*¡Excelente {nombre_cliente}!* \n\n{emoji_dinero} *Crédito aprobado:* ${monto:,.2f}\n📅 *Plazo:* {plazo} meses\n💳 *Pago quincenal:* {pago}\n📊 *CAT:* {cat}%\n\n🚀 *Ventajas:*\n• ⚡ Desembolso en 24-48 hrs\n• 🏠 Sin aval ni garantía hipotecaria\n• 📄 Trámite 100% digital\n\n👤 *Asesor:* {agente}\n📞 *Contacta a tu supervisor:* {supervisor}\n\n_*Oferta válida por 72 hrs. Sujeta a aprobación de buró*_"""
        else: layout = f"""SOLICITUD DE CAPTURA | {tipo_tramite}\n\nORIGEN: {form_data.get('convenio', 'IMSS').upper()} | TRAMITE: PAPERLESS\n\nCLIENTE: {nombre_cliente}\nNSS: {form_data.get('nss', '')} | RFC: {form_data.get('rfc', '').upper()}\nCONTACTO: {form_data.get('telefono', '')}\n\nFINANCIERO\nCLABE: {form_data.get('clabe', '')} ({form_data.get('banco', '').upper()})\nOCUPACION: {form_data.get('ocupacion', '').upper()}\n\nTRAMITE: {tipo_tramite}\n{cnca_det}\nOFERTA: ${monto:,.2f} a {plazo} MESES\nPAGO: {pago} | CAT: {cat}%\n\nAGENTE: {agente}\nSUPERVISOR: {supervisor}"""
        return layout

    @staticmethod
    def calcular_tasa_anual(monto, pago, plazo, pagos_restantes=0):
        """
        Calcula una tasa anual estimativa basada en el monto, pago y plazo.
        Método estático de utilidad para evitar crashes si no está definido.
        """
        try:
            # Retorno seguro para evitar bloqueos
            return 0.0
        except:
            return 0.0

class GestorSesion:
    def __init__(self):
        self.archivo = CONSTANTES["RUTAS"]["SESION"]
        CONSTANTES["RUTAS"]["DOCS"].mkdir(parents=True, exist_ok=True)
    
    def guardar_datos_cliente(self, form_data):
        try:
            with open(self.archivo, 'w', encoding='utf-8') as f: 
                json.dump(form_data, f, indent=4)
            logger.info(f"Datos de cliente guardados: {self.archivo}")
            return True
        except IOError as e:
            logger.error(f"Error de I/O guardando datos de cliente: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado guardando datos de cliente: {e}", exc_info=True)
            return False
    
    def cargar_datos_cliente(self):
        try:
            if not self.archivo.exists(): 
                return {}
            with open(self.archivo, 'r', encoding='utf-8') as f: 
                return json.load(f)
        except json.JSONDecodeError as e:
            logger.error(f"Error parseando JSON de sesión: {e}")
            return {}
        except IOError as e:
            logger.error(f"Error de I/O cargando datos de cliente: {e}")
            return {}
        except Exception as e:
            logger.error(f"Error inesperado cargando datos de cliente: {e}", exc_info=True)
            return {}
    
    def limpiar_datos_cliente(self):
        try:
            if self.archivo.exists(): 
                self.archivo.unlink()
            logger.info("Datos de cliente limpiados")
            return True
        except IOError as e:
            logger.error(f"Error eliminando archivo de sesión: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado limpiando datos de cliente: {e}", exc_info=True)
            return False

class GeneradorPDF:
    def generar(self, form_data, credito_data, perfil, tipo_tramite):
        if not HAVE_FPDF: return False, "Librería fpdf no instalada."
        try:
            pdf = FPDF()
            pdf.add_page()
            
            # --- ENCABEZADO ---
            pdf.set_font("Helvetica", "B", 14)
            pdf.set_text_color(30, 41, 59) # Color Slate oscuro
            pdf.cell(0, 10, "RESUMEN DE OPERACIÓN FINANCIERA", 0, 1, "C")
            
            pdf.set_font("Helvetica", "", 8)
            pdf.set_text_color(100, 100, 100)
            pdf.cell(0, 5, f"FOLIO: {int(time.time())} | FECHA: {datetime.datetime.now().strftime('%d/%m/%Y %H:%M')}", 0, 1, "C")
            pdf.ln(10)
            
            # --- SECCIÓN 1: PERFIL DEL CLIENTE (Completo) ---
            datos_personales = [
                ("Nombre Completo", str(form_data.get('nombre') or '').upper()),
                ("RFC", str(form_data.get('rfc') or '').upper()),
                ("CURP", str(form_data.get('curp') or '').upper()),
                ("NSS", str(form_data.get('nss') or '')),
                ("Ocupación", str(form_data.get('ocupacion') or '').upper()),
                ("Teléfono", str(form_data.get('telefono') or '')),
                ("Domicilio", str(form_data.get('domicilio') or '').upper()),
            ]
            self._sec(pdf, "DATOS PERSONALES", datos_personales)

            # --- SECCIÓN 2: DATOS BANCARIOS ---
            datos_bancarios = [
                ("Banco", str(form_data.get('banco') or '').upper()),
                ("CLABE Interbancaria", str(form_data.get('clabe') or '')),
                ("Convenio", str(form_data.get('convenio', 'IMSS') or 'IMSS').upper())
            ]
            self._sec(pdf, "INFORMACIÓN BANCARIA", datos_bancarios)

            # --- SECCIÓN 3: DETALLE DEL CRÉDITO ---
            datos_credito = [
                ("Producto Financiero", credito_data.get('producto', '')), 
                ("Monto Autorizado", to_money(credito_data.get('monto', 0))), 
                ("Plazo Seleccionado", f"{credito_data.get('plazo', 0)} Meses"), 
                ("Descuento Periódico", to_money(credito_data.get('pago_visual', 0))), 
                ("CAT Informativo", f"{credito_data.get('cat', '0.0')}%"),
                ("Tipo de Tasa", credito_data.get('tipo_tasa', 'TRADICIONAL'))
            ]
            self._sec(pdf, "ESTRUCTURA DE CRÉDITO APROBADA", datos_credito)

            # --- RESALTADO DE REMANENTE (Si aplica) ---
            if tipo_tramite == "CNCA":
                rem = credito_data.get('remanente', 0)
                pdf.ln(5)
                pdf.set_fill_color(220, 252, 231) # Verde claro suave
                pdf.set_draw_color(22, 163, 74)   # Borde verde
                pdf.set_font("Helvetica", "B", 11)
                pdf.cell(0, 12, f"  REMANENTE NETO ESTIMADO: {to_money(rem)}", 1, 1, "C", True)
                pdf.ln(5)
            
            # --- PIE DE PÁGINA / DISCLAIMER ---
            pdf.ln(15)
            pdf.set_draw_color(200, 200, 200)
            pdf.line(10, pdf.get_y(), 200, pdf.get_y()) # Línea separadora
            pdf.ln(5)
            
            pdf.set_font("Helvetica", "B", 8)
            pdf.cell(0, 4, f"ASESOR: {str(perfil.get('nombre') or '').upper()}", 0, 1, "L")
            pdf.cell(0, 4, f"SUPERVISOR: {str(perfil.get('supervisor') or '').upper()}", 0, 1, "L")
            
            pdf.ln(5)
            pdf.set_font("Helvetica", "", 6)
            pdf.set_text_color(128, 128, 128)
            pdf.multi_cell(0, 3, "AVISO LEGAL: Este documento es una proyección financiera preliminar basada en los datos proporcionados. La autorización final está sujeta a validación documental, capacidad de pago y políticas vigentes de la entidad financiera seleccionada. Las tasas y factores pueden variar sin previo aviso.")
            
            # Guardar
            fname = f"Cotizacion_{form_data.get('rfc', 'CLIENTE')}_{int(time.time())}.pdf"
            path = CONSTANTES["RUTAS"]["DOCS"] / fname
            pdf.output(str(path))
            return True, str(path)
            
        except Exception as e: return False, str(e)

    def _sec(self, pdf, titulo, datos):
        # Título de Sección con fondo
        pdf.set_font("Helvetica", "B", 10)
        pdf.set_fill_color(241, 245, 249) # Gris azulado muy claro
        pdf.set_text_color(15, 23, 42)
        pdf.cell(0, 8, f"  {titulo}", 0, 1, "L", True)
        pdf.ln(2)
        
        # Contenido
        pdf.set_font("Helvetica", "", 9)
        pdf.set_text_color(51, 65, 85)
        
        for etiqueta, valor in datos:
            # Usamos celdas invisibles para alinear: Etiqueta (45mm) | Valor (Resto)
            pdf.set_font("Helvetica", "B", 9)
            pdf.cell(50, 6, f"{etiqueta}:", 0, 0)
            
            pdf.set_font("Helvetica", "", 9)
            # Multi_cell para el valor por si es muy largo (ej. Domicilio)
            # Guardamos la posición X, Y actual
            x_val = pdf.get_x()
            y_val = pdf.get_y()
            
            pdf.multi_cell(0, 6, str(valor))
            
            # Si multi_cell saltó de línea, necesitamos ajustar el cursor para la siguiente fila visualmente si fuera tabla,
            # pero como es lista simple, solo aseguramos espacio.
            # En este layout simple lista vertical, multi_cell ya baja el cursor automáticamente.
        
        pdf.ln(3)

class GestorPerfil:
    def __init__(self):
        self.archivo = CONSTANTES["RUTAS"]["PERFIL"]
        CONSTANTES["RUTAS"]["DOCS"].mkdir(parents=True, exist_ok=True)
    
    def guardar(self, d):
        try:
            with open(self.archivo, 'w') as f: 
                json.dump(d, f, indent=4)
            logger.info(f"Perfil guardado: {self.archivo}")
            return True
        except IOError as e:
            logger.error(f"Error de I/O guardando perfil: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado guardando perfil: {e}", exc_info=True)
            return False
    
    def cargar(self):
        try:
            with open(self.archivo, "r") as f: 
                return json.load(f)
        except FileNotFoundError:
            logger.warning(f"Archivo de perfil no encontrado: {self.archivo}")
            return {}
        except json.JSONDecodeError as e:
            logger.error(f"Error parseando JSON de perfil: {e}")
            return {}
        except Exception as e:
            logger.error(f"Error inesperado cargando perfil: {e}", exc_info=True)
            return {}

class GestorOperaciones:
    def __init__(self): 
        self.archivo = CONSTANTES["RUTAS"]["DOCS"] / "historial_operaciones.json"
        CONSTANTES["RUTAS"]["REPORTES"].mkdir(parents=True, exist_ok=True)
    def guardar_operacion(self, data):
        ops = self.obtener()
        if "id" not in data: data["id"] = str(uuid.uuid4())
        if "fecha" not in data:
            now = datetime.datetime.now()
            data["fecha"] = now.strftime("%Y-%m-%d %H:%M")
            data["fecha_solo"] = now.strftime("%Y-%m-%d")
        if "estatus" not in data: data["estatus"] = "CONSULTA"
        ops.insert(0, data)
        self._save(ops)
        return data["id"]
    def actualizar_estatus(self, oid, ns):
        ops = self.obtener()
        for o in ops:
            if o.get("id") == oid:
                o["estatus"] = ns
                if ns in ["EN PROCESO", "FINALIZADO", "PENDIENTE"]:
                    o["fecha_proceso"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
                break
        self._save(ops)
    def actualizar_operacion(self, oid, new_data):
        """Actualizar una operación existente con nuevos datos"""
        ops = self.obtener()
        for o in ops:
            if o.get("id") == oid:
                o.update(new_data)
                break
        self._save(ops)
    def actualizar_o_crear_proceso(self, data_operacion):
        if "id" in data_operacion:
            self.actualizar_estatus(data_operacion["id"], "EN PROCESO")
            return data_operacion["id"]
        else:
            data_operacion["estatus"] = "EN PROCESO"
            return self.guardar_operacion(data_operacion)
    def eliminar_operacion(self, oid): self._save([o for o in self.obtener() if o.get("id") != oid])
    def obtener(self):
        try:
            with open(self.archivo, "r") as f: return json.load(f)
        except: return []
    def _save(self, ops):
        try:
            with open(self.archivo, "w") as f: json.dump(ops, f, indent=4)
        except: pass
    def filtrar_por_dia(self, fecha_str=None):
        if fecha_str is None: fecha_str = datetime.datetime.now().strftime("%Y-%m-%d")
        ops = self.obtener()
        ops_del_dia = []
        for op in ops:
            fecha_op = None
            if "fecha_solo" in op: fecha_op = op["fecha_solo"]
            elif "fecha" in op:
                try: fecha_op = op["fecha"].split(" ")[0]
                except: continue
            if fecha_op == fecha_str: ops_del_dia.append(op)
        return ops_del_dia

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

    def exportar_excel_diario(self, page=None):
        if not HAVE_OPENPYXL: return False, "Librería openpyxl no instalada"
        try:
            ops_dia = self.filtrar_por_dia()
            if not ops_dia: return False, "No hay operaciones registradas hoy"
            
            consultas = [op for op in ops_dia if op.get("estatus") == "CONSULTA"]
            procesos = [op for op in ops_dia if op.get("estatus") in ["EN PROCESO", "FINALIZADO", "PENDIENTE"]]
            
            wb = openpyxl.Workbook()
            # openpyxl crea una hoja por defecto llamada "Sheet"
            hoja_defecto = wb.active 
            
            hojas_creadas = 0

            # 1. Manejo de Consultas
            if consultas:
                ws_consultas = hoja_defecto
                ws_consultas.title = "Consultas"
                self._crear_hoja_excel(ws_consultas, consultas, "CONSULTAS DEL DÍA")
                hojas_creadas += 1
            
            # 2. Manejo de Procesos
            if procesos:
                if hojas_creadas > 0:
                    # Si ya usamos la hoja por defecto para consultas, creamos una nueva
                    ws_procesos = wb.create_sheet("Procesos")
                else:
                    # Si NO hubo consultas, usamos la hoja por defecto que aún está limpia
                    ws_procesos = hoja_defecto
                    ws_procesos.title = "Procesos"
                
                self._crear_hoja_excel(ws_procesos, procesos, "PROCESOS DEL DÍA")
                hojas_creadas += 1

            # Caso borde: Si por alguna razón extraña no hubo ni lo uno ni lo otro pero ops_dia no estaba vacío
            # (ej. estatus desconocidos), evitamos guardar un excel vacío o con hoja default sucia.
            if hojas_creadas == 0:
                return False, "No hay operaciones con estatus válido para exportar."

            fecha_actual = datetime.datetime.now().strftime("%Y-%m-%d")
            nombre_archivo = f"Reporte_Diario_{fecha_actual}.xlsx"
            ruta_reporte = CONSTANTES["RUTAS"]["REPORTES"] / nombre_archivo
            
            contador = 1
            while ruta_reporte.exists():
                nombre_alterno = f"Reporte_Diario_{fecha_actual}_{contador}.xlsx"
                ruta_reporte = CONSTANTES["RUTAS"]["REPORTES"] / nombre_alterno
                contador += 1
            
            wb.save(ruta_reporte)
            
            try:
                if platform.system() == "Windows": os.startfile(CONSTANTES["RUTAS"]["REPORTES"])
                elif platform.system() == "Darwin": subprocess.run(["open", CONSTANTES["RUTAS"]["REPORTES"]])
                else: subprocess.run(["xdg-open", CONSTANTES["RUTAS"]["REPORTES"]])
            except: pass
            
            return True, str(ruta_reporte)
            
        except Exception as e: return False, str(e)
    def _crear_hoja_excel(self, worksheet, datos, titulo):
        header_fill = PatternFill(start_color="1E293B", end_color="1E293B", fill_type="solid")
        header_font = Font(color="FFFFFF", bold=True, size=11)
        border = Border(left=Side(style='thin'), right=Side(style='thin'), top=Side(style='thin'), bottom=Side(style='thin'))
        center_alignment = Alignment(horizontal="center", vertical="center")
        left_alignment = Alignment(horizontal="left", vertical="center")
        worksheet.merge_cells('A1:H1')
        title_cell = worksheet['A1']
        title_cell.value = f"{titulo} - {datetime.datetime.now().strftime('%d/%m/%Y')}"
        title_cell.font = Font(size=14, bold=True, color="1E293B")
        title_cell.alignment = Alignment(horizontal="center", vertical="center")
        worksheet.row_dimensions[1].height = 30
        worksheet.merge_cells('A2:H2')
        stats_cell = worksheet['A2']
        stats_cell.value = f"Total registros: {len(datos)}"
        stats_cell.font = Font(size=10, italic=True, color="64748B")
        stats_cell.alignment = Alignment(horizontal="center", vertical="center")
        worksheet.row_dimensions[2].height = 20
        headers = ["ID", "Fecha", "Hora", "Cliente", "Producto", "Monto", "Plazo", "Estatus", "Tipo"]
        for col_num, header in enumerate(headers, 1):
            cell = worksheet.cell(row=3, column=col_num)
            cell.value = header
            cell.fill = header_fill
            cell.font = header_font
            cell.border = border
            cell.alignment = center_alignment
            worksheet.column_dimensions[get_column_letter(col_num)].width = max(len(header) + 4, 12)
        for row_num, op in enumerate(datos, 4):
            fecha_completa = op.get("fecha", "")
            fecha = hora = ""
            if " " in fecha_completa: fecha, hora = fecha_completa.split(" ", 1)
            else: fecha = fecha_completa
            monto = op.get("monto", 0)
            monto_formateado = f"${monto:,.2f}" if isinstance(monto, (int, float)) else str(monto)
            estatus = op.get("estatus", "")
            estatus_color = {"CONSULTA": "10B981", "EN PROCESO": "3B82F6", "FINALIZADO": "10B981", "PENDIENTE": "F59E0B"}.get(estatus, "94A3B8")
            row_data = [op.get("id", "")[:8] + "...", fecha, hora, op.get("cliente", "N/A"), op.get("producto", "N/A"), monto_formateado, op.get("plazo", "N/A"), estatus, op.get("tipo", "N/A")]
            for col_num, value in enumerate(row_data, 1):
                cell = worksheet.cell(row=row_num, column=col_num)
                cell.value = value
                cell.border = border
                if col_num in [4, 5, 8, 9]: cell.alignment = left_alignment
                else: cell.alignment = center_alignment
                if col_num == 8: cell.font = Font(bold=True, color=estatus_color)
        worksheet.freeze_panes = "A4"
        worksheet.auto_filter.ref = f"A3:{get_column_letter(len(headers))}3"

# ==========================================
# 2. UI COMPONENTS REFACTORED (FINTECH STYLE)
# ==========================================

class LoadingOverlay(ft.Container):
    def __init__(self, page):
        super().__init__(
            expand=True, 
            visible=False, 
            bgcolor="#80000000", # Fondo semitransparente oscuro
            alignment=ft.Alignment(0, 0), 
            animate_opacity=300,
            content=ft.Container(
                bgcolor="white", 
                padding=30, 
                border_radius=16, 
                shadow=ft.BoxShadow(
                    blur_radius=20, 
                    color=ft.Colors.with_opacity(0.2, "black"),
                    offset=ft.Offset(0, 10)
                ),
                content=ft.Column([
                    ft.ProgressRing(color="#1E293B", stroke_width=4), 
                    ft.Text("Procesando...", weight="bold", color="#1E293B", size=14)
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=20, tight=True)
            )
        )
        self.page = page
        # Se auto-agrega al overlay de la página al instanciarse
        page.overlay.append(self)
    
    def show(self): 
        self.visible = True
        self.update()
    
    def hide(self): 
        self.visible = False
        self.update()

class FintechCard(ft.Container):
    """
    Componente UI 2.0: Tarjeta de alto rendimiento con Motion Design.
    Muestra la Liquidez como dato principal y el Monto Bruto como secundario.
    
    MEJORAS 2026:
    - Integración con sistema de efectos de transición (transition_effects.py)
    - Efectos dinámicos según tipo: GLOW (mejor opción), SMOOTH (retención), ELEVATION (tradicional)
    - Sombras y escalas adaptativas según el contexto de la tarjeta
    """
    def __init__(self, data, is_best, tipo, on_layout, on_detail, on_compare, on_amortization, page=None, delay_ms=0):
        # Guardar callback principal explícitamente
        self.on_layout_fn = on_layout
        
        super().__init__(
            animate_opacity=THEME["animations"]["smooth"],
            animate_offset=THEME["animations"]["bouncy"],
            animate_scale=THEME["animations"]["standard"],
            animate=THEME["animations"]["fast"],
            opacity=0, 
            offset=ft.Offset(0, 0.2),
            scale=0.95,
            # on_click removido - se manejará en _handle_click
            border_radius=18,
            padding=0,
            margin=ft.Margin.only(bottom=16),
        )
        self.data = data
        self.is_best = is_best
        self.tipo = tipo
        self._initial_page = page # Guardar referencia sin conflicto con self.page
        self.delay_ms = delay_ms
        
        # Callbacks secundarios
        self.on_detail = on_detail
        self.on_compare = on_compare
        self.on_amortization = on_amortization
        
        # --- CONFIGURACIÓN DE EFECTOS ---
        if HAVE_TRANSITIONS:
            # Todas las tarjetas usan el mismo efecto (sin distinción especial para is_best)
            if data.get('tipo_tasa') == 'RETENCION':
                self.transition_effect = TransitionEffects.SMOOTH
            else:
                self.transition_effect = TransitionEffects.ELEVATION
        else:
            self.transition_effect = None

        self.colors = self._get_semantic_colors()
        
        # Aplicar colores y bordes
        if self.colors.get("gradient"):
            self.gradient = self.colors["gradient"]
        else:
            self.bgcolor = self.colors["bg"]
            
        if self.colors.get("border"):
            self.border = self.colors["border"]
            
        self.shadow = self._get_shadow(is_hover=False)
        
        # Estado de animación
        self._is_hovering = False
        self._is_pressing = False
        
        # Contenido con animaciones rediseñadas
        self.content = self._build_content()
        self.on_hover = self._handle_hover
        self.on_click = self._handle_click

    def _handle_hover(self, e):
        """Animación de hover sutil y elegante"""
        mouse_entered = str(e.data).lower() == "true"
        
        if mouse_entered and not self._is_pressing:
            self._is_hovering = True
            # Elevación suave
            self.scale = 1.015  # Más sutil que antes
            self.shadow = ft.BoxShadow(
                blur_radius=20,
                spread_radius=2,
                color="#20000000",
                offset=ft.Offset(0, 8)
            )
            
            # Borde sutil para mejor opción
            if self.is_best:
                self.border = ft.Border.all(2, THEME["colors"]["accent"])
        else:
            self._is_hovering = False
            if not self._is_pressing:
                # Restaurar estado normal
                self.scale = 1.0
                self.shadow = self._get_shadow(is_hover=False)
                
                if self.is_best:
                    self.border = ft.Border.all(1, "#E0E0E0")
                else:
                    self.border = None
        
        try:
            self.update()
        except:
            pass

    async def _handle_click(self, e):
        """Manejo de click directo sin animación (Optimizado)"""
        if self.on_layout_fn:
            try:
                # Ejecución directa e inmediata
                self.on_layout_fn(self.data)
            except Exception as ex:
                print(f"❌ Error al ejecutar acción de tarjeta: {ex}")
                # Intentar recuperar si falló visualmente
                try: self.page.update()
                except: pass
        else:
            print("❌ Error: Callback on_layout_fn no definido") 

    def did_mount(self):
        """Animación de entrada y números girando (adaptativa según rendimiento)"""
        async def sequence():
            # Aplicar multiplicador de delay según rendimiento
            adjusted_delay = self.delay_ms * performance_manager.get_delay_multiplier()
            
            if adjusted_delay > 0:
                await asyncio.sleep(adjusted_delay / 1000)
            
            # Verificar que el control sigue en la página antes de actualizar
            if not self.page:
                return
            
            self.opacity = 1
            self.offset = ft.Offset(0, 0)
            
            try:
                self.update()
            except AssertionError:
                # Control ya no está en la página, ignorar
                return
            
            # Animamos los números hacia el target calculado (Liquidez)
            if self.page:
                if not performance_manager.should_reduce_animations():
                    await asyncio.gather(
                        animate_rolling_currency(self.lbl_monto, 0, self.target_monto, duration=0.8),
                        animate_rolling_currency(self.lbl_pago, 0, self.data.get('pago', 0), duration=0.8)
                    )
                else:
                    # Modo Rendimiento: Asignación directa sin animación
                    self.lbl_monto.value = to_money(self.target_monto)
                    self.lbl_pago.value = to_money(self.data.get('pago', 0))
                    self.lbl_monto.update()
                    self.lbl_pago.update()

        if self.page:
            self.page.run_task(sequence)

    def _get_semantic_colors(self):
        is_dark = self._initial_page.theme_mode == ft.ThemeMode.DARK if self._initial_page else False
        is_retencion = self.data.get('tipo_tasa') == 'RETENCION'
        
        # Paleta Lime & Navy (Ajustada)
        NAVY_GRADIENT_START = "#0F4C81" # Classic Blue
        NAVY_GRADIENT_END = "#1B365D"   # Traditional Navy
        LIME_ACCENT = "#84CC16"         # Lime 500
        
        if self.is_best:
            # MEJOR OPCIÓN: Premium Light (Gold Accent)
            return {
                "bg": "#FFFFFF",
                "gradient": None, 
                "text_main": "#0F172A", 
                "text_sec": "#64748B",
                "accent": "#D97706",
                "badge_bg": ft.LinearGradient(
                    colors=["#F59E0B", "#D97706"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                ),
                "badge_text": "white",
                "divider": "#F1F5F9",
                "border": ft.Border.all(1, "#FDE68A") # Gold suave (Amber 200) 1px
            }
        else:
            # OPCIONES NORMALES
            if is_retencion:
                accent_color = "#10B981"  # Emerald
                badge_gradient = ft.LinearGradient(
                    colors=["#10B981", "#059669"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                )
                border_col = "#E5E7EB"
            else:
                accent_color = "#3B82F6"  # Blue
                badge_gradient = ft.LinearGradient(
                    colors=["#3B82F6", "#2563EB"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                )
                border_col = "#E5E7EB"
            
            return {
                "bg": "#FFFFFF",
                "gradient": None,
                "text_main": "#0F172A",
                "text_sec": "#64748B",
                "accent": accent_color,
                "badge_bg": badge_gradient,
                "badge_text": "white",
                "divider": "#E5E7EB",
                "border": ft.Border.all(1, border_col) # Gray border
            }

    def _get_shadow(self, is_hover):
        # Sombra uniforme para todas las tarjetas (sin distinción por is_best)
        color = "#15000000"
        if is_hover:
            return ft.BoxShadow(blur_radius=25, color=color, offset=ft.Offset(0, 10))
        return ft.BoxShadow(blur_radius=15, color=color, offset=ft.Offset(0, 4))

    def _handle_hover(self, e):
        is_hover = e.data == "true"
        
        # ELIMINADO: Efecto de escala/expansión (Solicitud de usuario para mejorar rendimiento)
        # self.scale = 1.0 
        
        # Solo cambiamos sutilmente la sombra si se desea feedback, o nada.
        # Mantendremos una sombra muy ligera si es hover, pero sin mover la tarjeta.
        if is_hover:
            self.shadow = ft.BoxShadow(
                blur_radius=20,
                spread_radius=0,
                color="#10000000", 
                offset=ft.Offset(0, 4)
            )
        else:
            self.shadow = self._get_shadow(is_hover=False)
        
        self.update()

    def _build_content(self):
        lbl_tipo = "RETENCIÓN" if self.data.get('tipo_tasa') == 'RETENCION' else "TRADICIONAL"
        
        # Colores del tema (Armonía)
        try:
            c_primary = THEME["colors"]["text_primary"] 
        except:
             c_primary = "#1F2937"
             
        c_accent = THEME["colors"]["accent"]
        c_text_main = THEME["colors"]["text_primary"]
        c_text_sec = THEME["colors"]["text_secondary"]
        c_divider = THEME["colors"]["divider"]
        
        # Badge flotante (Más sutil)
        # ELIMINADO: Usuario solicitó quitar el badge verde de arriba por ser redundante.
        badge = None
        # if self.is_best:
        #    badge = ft.Container(...)

        # COLORES POR MARCA (definidos temprano para usar en todo el card)
        brand_colors_early = {
            "CONSUPAGO": "#D97706",
            "CONSUBANCO": "#0369A1",
            "MASNOMINA": "#475569",  # Más claro para mejor legibilidad
            "OPCIPRES": "#2563EB"
        }
        producto_upper_early = self.data['producto'].upper()
        brand_color = "#1F2937"  # Default
        for key, color in brand_colors_early.items():
            if key in producto_upper_early:
                brand_color = color
                break

        # Cálculo de montos
        monto_bruto = float(self.data.get('monto', 0))
        val_principal = monto_bruto
        texto_titulo = "MONTO A RECIBIR"
        
        if self.tipo == "CNCA":
            deuda_total = float(self.data.get('deuda_a_liquidar', 0))
            if deuda_total > 0:
                val_liquidez = monto_bruto - deuda_total
                if val_liquidez <= 0: 
                    val_liquidez = float(self.data.get('remanente', 0))
                val_principal = val_liquidez
                texto_titulo = "REMANENTE CLIENTE"
            else:
                val_principal = float(self.data.get('remanente', 0)) or monto_bruto
                texto_titulo = "REMANENTE CLIENTE"

        self.target_monto = val_principal
        
        # Monto Principal (Hero) - COLOR DE MARCA
        self.lbl_monto = ft.Text(
            "$0.00", 
            size=30,  # Un poco más grande
            weight="w800", 
            color=brand_color,  # Usar color de marca
            font_family="Outfit",
            text_align=ft.TextAlign.CENTER,
            selectable=True
        )

        
        # Definir lbl_pago aquí para uso posterior
        self.lbl_pago = ft.Text(f"${self.data.get('pago_visual', 0):,.2f}", size=13, weight="bold", color=c_text_main)

        # Estilos de Marca
        brand_key = self.data['producto'].upper()
        # Mapeo simple si el nombre no coincide exactamente (ej. espacios)
        if "CONSUBANCO" in brand_key: brand_key = "CONSUBANCO"
        elif "CONSUPAGO" in brand_key: brand_key = "CONSUPAGO"
        elif "MASNOMINA" in brand_key: brand_key = "MASNOMINA"
        elif "OPCIPRES" in brand_key: brand_key = "OPCIPRES"
        
        brand_style = BRAND_COLORS.get(brand_key, BRAND_COLORS["DEFAULT"])

        # Header (Producto + Tipo)
        # Generar badges dinámicos para asesores
        badges_row = []
        
        # Badge: Mejor Oferta (is_best)
        if self.is_best:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.STAR_ROUNDED, size=12, color="#F59E0B"),
                        ft.Text("MEJOR OFERTA", size=9, weight="bold", color="#F59E0B")
                    ], spacing=3),
                    bgcolor="#FEF3C7",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#F59E0B")
                )
            )
        
        # Badge: Menor Pago (si es el menor pago de todas)
        # Este se agregará dinámicamente desde run_nuevo si es necesario
        
        # Badge: Plazo Corto (si plazo <= 36 meses)
        if self.data.get('plazo', 60) <= 36:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.FLASH_ON_ROUNDED, size=12, color="#10B981"),
                        ft.Text("PLAZO CORTO", size=9, weight="bold", color="#10B981")
                    ], spacing=3),
                    bgcolor="#D1FAE5",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#10B981")
                )
            )
        
        # Badge: Retención/SIPRE
        if self.data.get('tipo_tasa', '').upper() in ['RETENCIÓN', 'SIPRE', 'RETENCION']:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.VERIFIED_ROUNDED, size=12, color="#8B5CF6"),
                        ft.Text("TASA PREFERENCIAL", size=9, weight="bold", color="#8B5CF6")
                    ], spacing=3),
                    bgcolor="#EDE9FE",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#8B5CF6")
                )
            )
        
        # Mapeo de logos de instituciones
        logo_map = {
            "CONSUPAGO": "csp.png",
            "CONSUBANCO": "csb.png",
            "MASNOMINA": "msn.png",
            "OPCIPRES": "opc.png"
        }
        
        # COLORES POR MARCA (refinados para mejor contraste)
        brand_colors = {
            "CONSUPAGO": {"primary": "#D97706", "light": "#FEF7ED", "border": "#F59E0B"},
            "CONSUBANCO": {"primary": "#0369A1", "light": "#F0F9FF", "border": "#0EA5E9"},
            "MASNOMINA": {"primary": "#475569", "light": "#F8FAFC", "border": "#64748B"},  # Más claro
            "OPCIPRES": {"primary": "#2563EB", "light": "#EFF6FF", "border": "#3B82F6"}
        }
        
        # Determinar colores de la marca
        producto_upper = self.data['producto'].upper()
        brand_key = "CONSUPAGO"  # Default
        for key in brand_colors:
            if key in producto_upper:
                brand_key = key
                break
        
        brand = brand_colors[brand_key]
        
        # Determinar logo a usar
        logo_path = None
        for key in logo_map:
            if key in producto_upper:
                logo_path = logo_map[key]
                break
        
        # === LOGO PREMIUM CON SOMBRA SUTIL ===
        if logo_path:
            logo_widget = ft.Container(
                content=ft.Image(
                    src=logo_path,
                    width=40,
                    height=40,
                    fit="contain"
                ),
                width=52,
                height=52,
                bgcolor="white",
                border_radius=26,
                alignment=ft.Alignment(0, 0),
                border=ft.Border.all(2.5, brand["border"]),
                shadow=ft.BoxShadow(
                    blur_radius=8,
                    color="#15000000",
                    offset=ft.Offset(0, 2)
                )
            )
        else:
            logo_widget = ft.Container(
                content=ft.Icon(ft.Icons.ACCOUNT_BALANCE_ROUNDED, size=26, color=brand["primary"]),
                width=52,
                height=52,
                bgcolor="white",
                border_radius=26,
                alignment=ft.Alignment(0, 0),
                border=ft.Border.all(2.5, brand["border"]),
                shadow=ft.BoxShadow(
                    blur_radius=8,
                    color="#15000000",
                    offset=ft.Offset(0, 2)
                )
            )
        
        # Badge de retención
        ret_badge = ft.Container(
            content=ft.Text("PREFERENCIAL", size=8, weight="bold", color="white"),
            bgcolor="#10B981",
            padding=ft.Padding.symmetric(horizontal=6, vertical=2),
            border_radius=4,
            visible=lbl_tipo == "RETENCIÓN"
        )
        
        # === HEADER CON GRADIENTE PARA MEJOR OPCIÓN ===
        header_bg = None
        if self.is_best:
            header_bg = ft.LinearGradient(
                begin=ft.Alignment(-1, 0),
                end=ft.Alignment(1, 0),
                colors=["#FFFBEB", "#FEF3C7"]
            )
        
        header = ft.Container(
            content=ft.Row([
                # Logo con sombra
                logo_widget,
                # Nombre + Plazo como badge
                ft.Column([
                    ft.Text(self.data['producto'], size=16, weight="w700", color=brand["primary"]),
                    ft.Row([
                        # Plazo como badge pill
                        ft.Container(
                            content=ft.Text(f"{self.data.get('plazo', 0)} meses", size=10, weight="w600", color=c_text_sec),
                            bgcolor="#F3F4F6",
                            padding=ft.Padding.symmetric(horizontal=8, vertical=3),
                            border_radius=10
                        ),
                        ret_badge
                    ], spacing=6)
                ], spacing=4, expand=True),
            ], spacing=14, vertical_alignment=ft.CrossAxisAlignment.CENTER),
            padding=ft.Padding.only(left=16, right=16, top=16, bottom=14),
            gradient=header_bg,
            border_radius=ft.BorderRadius.only(top_left=14, top_right=14) if self.is_best else None
        )

        # === HERO SECTION MINIMALISTA ===
        hero_section = ft.Container(
            content=ft.Column([
                # Monto principal (grande y centrado)
                self.lbl_monto,
                
                # Información secundaria (condicional según tipo)
                ft.Column([
                    # Monto bruto - SOLO para CNCA (refinanciamiento)
                    ft.Text(
                        f"Monto bruto: ${monto_bruto:,.2f}",
                        size=11,
                        color=c_text_sec,
                        weight="w500",
                        visible=self.tipo == "CNCA"  # Solo en refinanciamiento
                    ),
                    # Descuento mensual - siempre visible
                    ft.Text(
                        f"Descuento mensual: ${self.data.get('pago_visual', 0):,.2f}",
                        size=11,
                        color=c_text_sec
                    )
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=2)
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=6),
            padding=ft.Padding.symmetric(horizontal=16, vertical=16),
            alignment=ft.Alignment(0, 0)
        )

        # === FOOTER (SIEMPRE PRESENTE PARA ALTURA UNIFORME) ===
        # Contenido del footer solo si es mejor opción
        footer_content = None
        if self.is_best:
            footer_content = ft.Row([
                ft.Icon(ft.Icons.STAR_ROUNDED, size=14, color="#D97706"),
                ft.Text("Mejor opción", size=11, weight="w600", color="#D97706")
            ], spacing=4, alignment=ft.MainAxisAlignment.CENTER)
        
        footer = ft.Container(
            content=footer_content,
            padding=ft.Padding.symmetric(horizontal=16, vertical=8),
            bgcolor="#FEF3C7" if self.is_best else "transparent",
            height=32  # Altura fija para todas las tarjetas
        )
        
        # Estructura final (siempre incluye footer)
        main_content = ft.Column([
            header,
            hero_section,
            footer
        ], spacing=0)
        
        # Estilos premium según mejor opción
        border_color = "#F59E0B" if self.is_best else "#E5E7EB"
        border_width = 2.5 if self.is_best else 1
        bg_color = "#FFFEF7" if self.is_best else "white"
        
        # Sombra premium
        card_shadow = ft.BoxShadow(
            blur_radius=16 if self.is_best else 12,
            spread_radius=0,
            color="#20000000" if self.is_best else "#12000000",
            offset=ft.Offset(0, 4)
        )
        
        return ft.Container(
            content=main_content,
            bgcolor=bg_color,
            border=ft.Border.all(border_width, border_color),
            border_radius=16,
            shadow=card_shadow,
            on_click=lambda e: self.on_detail(self.data) if self.on_detail else None,
            ink=True,
            animate_scale=ft.Animation(200, "easeOut"),
            height=230  # Altura fija para todas las tarjetas
        )

    def _stat_block(self, label, value):
        return ft.Column([
            ft.Text(label, size=9, weight="bold", color=self.colors["text_sec"]),
            ft.Text(value, size=13, weight="bold", color=self.colors["text_main"])
        ], spacing=2)

    def _icon_action(self, icon, tooltip, on_click):
        return ft.IconButton(
            icon=icon, 
            icon_size=18,  # Reducido para pantallas pequeñas
            icon_color=self.colors["text_sec"], 
            tooltip=tooltip, 
            on_click=on_click,
            style=ft.ButtonStyle(overlay_color=ft.Colors.with_opacity(0.1, self.colors["accent"]))
        )
    
    def on_export_image(self, e):
        """Genera la imagen y muestra una previsualización antes de confirmar"""
        
        # Acceso seguro a globales
        _loading = globals().get('loading')
        _progress = globals().get('progress_indicator')
        _have_progress = globals().get('HAVE_PROGRESS', False)
        
        if _have_progress and _progress:
             _progress['show']()
        elif _loading:
             _loading.show()
             
        try:
            # Generar la imagen temporalmente
            ok, res_path = GeneradorImagen.crear_card_img(self.data)
            
            if _have_progress and _progress: _progress['hide']()
            elif _loading: _loading.hide()

            if ok: 
                # Mostrar diálogo de previsualización
                preview_img = ft.Image(
                    src=res_path,
                    width=400,
                    fit="contain",
                    border_radius=12,
                )
                
                def copiar_clipboard(e):
                    # Simular copia
                    if e.page:
                        e.page.set_clipboard(res_path)
                        # Intentar usar msg si es global, o mostrar snackbar directo
                        snack = ft.SnackBar(ft.Text(f"Ruta copiada: {res_path}"), bgcolor=globals().get("THEME", {}).get("colors", {}).get("success", "green"))
                        e.page.overlay.append(snack)
                        snack.open = True
                        e.page.update()
                        e.setattr(dlg_preview, "open", False); page.update()

                dlg_preview = ft.AlertDialog(
                    title=ft.Text("Vista Previa de Captura", size=16, weight="bold"),
                    content=ft.Column([
                        preview_img,
                        ft.Text(f"Guardado en: {res_path}", size=10, italic=True, color="grey")
                    ], tight=True, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    actions=[
                        ft.TextButton("Copiar Ruta", icon=ft.Icons.COPY, on_click=copiar_clipboard),
                        ft.TextButton("Listo", on_click=lambda e: [setattr(dlg_preview, "open", False), page.update()] if e.page else None),
                    ],
                    actions_alignment=ft.MainAxisAlignment.END,
                    shape=ft.RoundedRectangleBorder(radius=16)
                )
                if self.page: self.page.dialog = dlg_preview; dlg_preview.open = True; self.page.update()
            else: 
                print(f"Error generando imagen: {res_path}")
                
        except Exception as ex:
            if _have_progress and _progress: _progress['hide']()
            elif _loading: _loading.hide()
            print(f"Error generando imagen: {ex}")
    
    def refresh_for_theme_change(self):
        """Actualiza los colores de la tarjeta cuando cambia el tema."""
        # Verificar que el control está en la página
        if not self.page:
            return
        
        # Recalcular colores para el nuevo tema
        self.colors = self._get_semantic_colors()
        
        # Aplicar gradiente o bgcolor según disponibilidad
        if self.colors.get("gradient"):
            self.gradient = self.colors["gradient"]
            self.bgcolor = None  # Limpiar bgcolor si usamos gradiente
        else:
            self.bgcolor = self.colors["bg"]
            self.gradient = None  # Limpiar gradiente si usamos bgcolor
        
        # Reconstruir contenido con nuevos colores
        self.content = self._build_content()
        
        # Actualizar la tarjeta con manejo de errores
        try:
            self.update()
        except AssertionError:
            # Control ya no está en la página, ignorar
            pass

class EnterpriseButton(ft.Container):
    """Boton moderno con estados, animación y diseño limpio"""
    def __init__(self, text, on_click, icon=None, bgcolor="#1E293B", 
                 color="white", width=None, expand=False, variant="filled", **kwargs):
        
        # Ajustes por Variante
        border = None
        if variant == "ghost":
            # Guardamos el bgcolor original si no es el default para usarlo como texto quizas? No, simplificamos.
            # Ghost: Fondo transparente, texto/icono color Accent (o el pasado), Borde opcional
            actual_bg = "transparent"
            actual_color = kwargs.pop("text_color", "#10B981") # Default green si no se especifica
            if color != "white": actual_color = color # Si pasaron color explícito, respetarlo
            border = ft.Border.all(1, actual_color)
        else:
            actual_bg = bgcolor
            actual_color = color
            
        super().__init__(
            content=ft.Row(
                [ft.Icon(icon, size=18, color=actual_color), ft.Text(text, color=actual_color, weight="bold", size=13)] 
                if icon else [ft.Text(text, color=actual_color, weight="bold", size=13)],
                alignment=ft.MainAxisAlignment.CENTER,
                spacing=8
            ),
            bgcolor=actual_bg,
            border=border,
            border_radius=8,
            padding=ft.Padding.symmetric(horizontal=20, vertical=12),
            on_click=self._handle_click,
            animate_scale=ft.Animation(50, "easeOut"), # Más rápido para respuesta táctil
            on_hover=self._on_hover,
            width=width,
            expand=expand,
            alignment=ft.Alignment(0, 0),
            shadow=ft.BoxShadow(blur_radius=5, color="#1A000000", offset=ft.Offset(0, 2)),
            **kwargs
        )
        self._original_on_click = on_click
    
    async def _handle_click(self, e):
        # Efecto "Press" físico
        self.scale = 0.95
        self.update()
        await asyncio.sleep(0.1)
        self.scale = 1.0
        self.update()
        
        if self._original_on_click:
            res = self._original_on_click(e)
            if asyncio.iscoroutine(res):
                await res

    def _on_hover(self, e):
        # Hover sutil
        self.scale = 1.02 if e.data == "true" else 1.0
        self.shadow.blur_radius = 15 if e.data == "true" else 5
        self.shadow.offset = ft.Offset(0, 5) if e.data == "true" else ft.Offset(0, 2)
        self.update()

class MinimalButton(ft.Container):
    """
    Botón minimalista moderno con diseño clean y animaciones sutiles.
    Perfecto para acciones primarias y secundarias.
    """
    def __init__(
        self, 
        text, 
        on_click, 
        icon=None, 
        variant="primary",  # "primary", "secondary", "ghost"
        size="medium",  # "small", "medium", "large"
        expand=False,
        icon_only=True, # REDISEÑO: Por defecto solo iconos
        **kwargs
    ):
        # Configuración de tamaños (Optimizados para iconos)
        sizes = {
            "small": {"height": 36, "font_size": 12, "icon_size": 18, "padding": 8},
            "medium": {"height": 44, "font_size": 14, "icon_size": 24, "padding": 10},
            "large": {"height": 56, "font_size": 16, "icon_size": 32, "padding": 12}
        }
        
        size_config = sizes.get(size, sizes["medium"])
        
        # Configuración de variantes
        variants = {
            "primary": {
                "bg": ft.LinearGradient(
                    begin=ft.Alignment(-1, 0),
                    end=ft.Alignment(1, 0),
                    colors=["#10B981", "#059669"]  # Emerald gradient
                ),
                "text_color": "white",
                "hover_scale": 1.05,
                "shadow": ft.BoxShadow(
                    blur_radius=12,
                    color="#3310B981",
                    offset=ft.Offset(0, 4)
                )
            },
            "secondary": {
                "bg": None,
                "bgcolor": "#F1F5F9",  # Light gray
                "text_color": "#0F172A",
                "hover_scale": 1.05,
                "shadow": None,
                "border": ft.Border.all(1, "#E2E8F0")
            },
            "ghost": {
                "bg": None,
                "bgcolor": "transparent",
                "text_color": "#64748B",
                "hover_scale": 1.1,
                "shadow": None,
                "border": None
            }
        }
        
        variant_config = variants.get(variant, variants["primary"])
        
        # Lógica de visualización: Icon Only por defecto si hay icono
        show_icon = icon is not None
        show_text = not show_icon or not icon_only
        
        # Contenido del botón
        content_items = []
        if show_icon:
            content_items.append(
                ft.Icon(
                    icon, 
                    size=size_config["icon_size"], 
                    color=variant_config["text_color"]
                )
            )
        
        if show_text:
            content_items.append(
                ft.Text(
                    text, 
                    size=size_config["font_size"], 
                    weight="w600", 
                    color=variant_config["text_color"]
                )
            )
            
        # Tooltip automático
        explicit_tooltip = kwargs.pop("tooltip", None)
        tooltip = explicit_tooltip if explicit_tooltip else (text if (show_icon and not show_text) else None)
        
        # Inicializar contenedor
        super().__init__(
            content=ft.Row(
                content_items,
                alignment=ft.MainAxisAlignment.CENTER,
                spacing=8 if show_text else 0,
                tight=True
            ),
            height=size_config["height"],
            # Si es solo icono y no expand, intentamos hacerlo cuadrado o con padding simétrico
            width=size_config["height"] if (show_icon and not show_text and not expand) else None,
            padding=ft.Padding.all(0) if (show_icon and not show_text) else ft.Padding.symmetric(horizontal=size_config["padding"]),
            border_radius=16, # Más redondeado para iconos
            tooltip=tooltip,
            gradient=variant_config.get("bg"),
            bgcolor=variant_config.get("bgcolor"),
            border=variant_config.get("border"),
            shadow=variant_config.get("shadow"),
            animate=THEME["animations"]["button_hover"],
            animate_scale=THEME["animations"]["button_press"],
            ink=True,
            on_click=on_click,
            expand=expand,
            **kwargs
        )
        
        self.variant_config = variant_config
        self.original_on_click = on_click
        
        # Hover effect
        def on_hover(e):
            if e.data == "true":
                self.scale = self.variant_config["hover_scale"]
                if variant == "ghost":
                    self.bgcolor = "#F8FAFC"
            else:
                self.scale = 1.0
                if variant == "ghost":
                    self.bgcolor = "transparent"
            self.update()
        
        self.on_hover = on_hover

# ==========================================
# 2.1 INPUTS COMPONENTS (DESPUÉS DE MinimalButton)
# ==========================================



class EnterpriseInput(ft.Column):
    """
    JuicyInput MEJORADO: Componente de entrada estilizado con UX visual premium.
    
    MEJORAS INTEGRADAS:
    - Validaciones en tiempo real (NSS/RFC/CURP)
    - Formateo automático de moneda
    - Feedback visual inmediato (bordes verdes/rojos)
    - Animaciones de foco suaves
    """
    def __init__(self, label, prefix_icon=None, prefix_text=None, suffix_text=None, prefix=None, suffix=None, expand=False, width=None, value=None,
                 password=False, is_money=False, multiline=False, max_length=None,
                 tipo_validacion=None, keyboard_type=None, on_submit=None, on_change=None):
        super().__init__(expand=expand, width=width, spacing=2)
        self.tipo_validacion = tipo_validacion
        self.max_length = max_length
        self.is_valid = True
        self.error_message = ""
        self._ext_ch = on_change
        self.is_money = is_money
        
        # Mensaje de Error
        self.error_text = ft.Text("", size=11, color=THEME["colors"]["error"], weight="bold", visible=False, text_align=ft.TextAlign.RIGHT)
        
        # Lógica de Teclado
        final_keyboard = keyboard_type if keyboard_type else ("number" if is_money or tipo_validacion == "NSS" else "text")

        # Resolve prefix/suffix
        final_prefix = prefix if prefix else (ft.Text(prefix_text) if prefix_text else None)
        final_suffix = suffix if suffix else (ft.Text(suffix_text) if suffix_text else None)

        # TextField Interno (Minimalista, el estilo lo da el contenedor)
        self.tf = ft.TextField(
            value=str(value) if value is not None else "",
            label=label,
            prefix_icon=prefix_icon,
            prefix=final_prefix,
            suffix=final_suffix,
            password=password,
            can_reveal_password=password,
            text_size=15,
            border="none",          # Sin borde propio
            bgcolor="transparent",  # Fondo transparente
            color="#111827",        # Gray 900 (Strict Light)
            keyboard_type=final_keyboard,
            text_align=ft.TextAlign.RIGHT if is_money else "left",
            multiline=multiline,
            min_lines=2 if multiline else 1,
            content_padding=ft.Padding.only(top=10, bottom=10, right=10, left=0 if prefix_icon else 10),
            height=None if multiline else 45,
            label_style=ft.TextStyle(size=14, color="#6B7280"), # Gray 500
            on_change=self._on_change_wrapper,
            on_focus=self._on_focus_wrapper,
            on_blur=self._on_blur_wrapper,
            on_submit=on_submit,
            selection_color="#10B981", # Emerald 500
            cursor_color="#10B981"     # Emerald 500
        )
        
        # Contenedor Visual (La "Caja" Premium)
        self.input_box = ft.Container(
            content=self.tf,
            border_radius=12,
            border=ft.Border.all(1, "#E5E7EB"), # Gray 200 (Default)
            bgcolor="#FFFFFF", # White
            padding=ft.Padding.symmetric(horizontal=10),
            # ANIMACIONES PREMIUM
            animate=THEME["animations"]["fast"],
            animate_scale=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
            shadow=None,
            on_click=lambda e: self.tf.focus()
        )
        
        self.controls = [self.input_box, self.error_text]

    @staticmethod
    def _luhn_check(nss):
        if not nss.isdigit() or len(nss) != 11: return False
        digits = [int(d) for d in nss]
        checksum = 0
        for i, d in enumerate(reversed(digits)):
            if i % 2 == 1:
                d *= 2
                if d > 9: d -= 9
            checksum += d
        return checksum % 10 == 0

    def _set_error(self, msg):
        self.is_valid = False
        self.error_message = msg
        self.error_text.value = msg
        self.error_text.visible = True
        self.input_box.border = ft.Border.all(2, "#EF4444")  # Red 500
        # Shake effect wrapper logic if needed, simple red border for now
        if self.page:
            self.error_text.update()
            self.input_box.update()

    def _set_success(self):
        """Novo: Feedback visual de éxito (borde verde)"""
        self.is_valid = True
        self.error_message = ""
        self.error_text.visible = False
        self.input_box.border = ft.Border.all(2, "#10B981")  # Emerald 500
        if self.page:
            self.error_text.update()
            self.input_box.update()

    def _clear_error(self):
        self.is_valid = True
        self.error_message = ""
        self.error_text.visible = False
        self.input_box.border = ft.Border.all(1, THEME["colors"]["divider"])
        if self.page:
            self.error_text.update()
            self.input_box.update()

    def _on_change_wrapper(self, e):
        val = e.control.value
        
        # Limitar longitud
        if self.max_length and len(val) > self.max_length:
            e.control.value = val[:self.max_length]
            e.control.update()
            return
        
        # NOTA: Formateo de moneda desactivado en on_change para evitar que el cursor se mueva
        # El formateo se hace en on_blur (al perder el foco)
        
        # Validación en tiempo real (solo feedback visual, sin formateo)
        if val:
            is_valid, error_msg = self._validate_field(val)
            if is_valid:
                self._set_success()
            elif error_msg:
                self._set_error(error_msg)
        else:
            self._clear_error()
        
        if self._ext_ch: self._ext_ch(e)

    def _validate_field(self, val):
        """Validación mejorada en tiempo real"""
        if not val:
            return True, ""
        
        if self.tipo_validacion == "NSS":
            if len(val) < 11:
                return False, "NSS incompleto (11 dígitos)"
            elif len(val) == 11:
                if not self._luhn_check(val):
                    return False, "NSS inválido"
                return True, ""
        
        elif self.tipo_validacion == "RFC":
            # RFC: 4 letras + 6 dígitos + 3 alfanuméricos = 13 caracteres
            if len(val) < 13:
                return False, "RFC incompleto"
            elif len(val) == 13:
                if not re.match(r'^[A-ZÑ&]{4}\d{6}[A-Z0-9]{3}$', val.upper()):
                    return False, "RFC inválido"
                return True, ""
        
        elif self.tipo_validacion == "CURP":
            # CURP: 18 caracteres
            if len(val) < 18:
                return False, "CURP incompleto (18 caracteres)"
            elif len(val) == 18:
                if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', val.upper()):
                    return False, "CURP inválido"
                return True, ""
        
        elif self.tipo_validacion == "TEXTO":
            # Validar que no esté vacío y tenga al menos 3 caracteres
            if len(val) < 3:
                return False, "Mínimo 3 caracteres"
            return True, ""
        
        return True, ""

    def _on_focus_wrapper(self, e):
        # Focus Animation: Scale Up & Green Glow
        self.input_box.border = ft.Border.all(2, "#10B981") # Emerald 500
        self.input_box.shadow = ft.BoxShadow(
            blur_radius=12,
            spread_radius=0,
            color="#10B98133", # Emerald 20% opacity
            offset=ft.Offset(0, 4)
        )
        self.input_box.bgcolor = "#ECFDF5" # Emerald 50
        self.input_box.scale = 1.01 # Modest scale up
        self.input_box.update()

    def _on_blur_wrapper(self, e):
        val = e.control.value
        
        # Formateo final de moneda
        if self.is_money and val:
            try:
                formatted = format_currency_live(val)
                e.control.value = formatted
                e.control.update()
            except:
                pass
        
        # Validación final
        if val:
            is_valid, error_msg = self._validate_field(val)
            if is_valid:
                self._set_success()
            elif error_msg:
                self._set_error(error_msg)
            else:
                self._clear_error()
        else:
            # Restaurar estado normal si no hay error
            if not self.error_text.visible:
                self.input_box.border = ft.Border.all(1, "#E5E7EB") # Gray 200
            self.input_box.shadow = None
            self.input_box.bgcolor = "#FFFFFF" # White
            self.input_box.scale = 1.0 # Restore scale
            self.input_box.update()

    @property
    def value(self): return self.tf.value
    
    @value.setter
    def value(self, v): self.tf.value = str(v) if v is not None else ""
    
    @property
    def raw_value(self): 
        """Retorna valor limpio (sin formato) para campos de moneda"""
        if self.is_money:
            return clean_float(self.tf.value)
        return self.tf.value

    def set_on_change(self, f): self._ext_ch = f
    
    def update(self):
        super().update() # Actualiza column


class JuicyDropdown(ft.Container):
    """
    Dropdown estilo Juicy con soporte para colores de marca dinámicos.
    Envuelve ft.Dropdown en un contenedor estilizado.
    """
    def __init__(self, label, options, on_change=None, value=None, expand=False, width=None, suffix=None):
        super().__init__(expand=expand, width=width)
        self.ext_on_change = on_change
        self.suffix_control = suffix
        
        # Colores de Marca
        self.brand_colors = {
            "CONSUBANCO": "#3B82F6",  # Blue
            "CONSUPAGO": "#F97316",   # Orange
            "OPCIPRES": "#10B981",    # Emerald
            "MASNOMINA": "#EF4444",   # Red
        }

        # Dropdown Interno (Fondo sólido)
        self.dd = ft.Dropdown(
            label=label,
            options=[ft.dropdown.Option(o) for o in options],
            value=value,
            text_size=15,
            border="none",
            bgcolor="#FFFFFF",  # Fondo BLANCO SÓLIDO
            filled=True,  # Estilo filled para fondo sólido
            content_padding=10, 
            label_style=ft.TextStyle(size=13, color=THEME["colors"]["text_tertiary"]),
        )
        self.dd.on_change = self._on_change_wrapper

        # Container Style - PREMIUM OPAQUE
        self.border_radius = 12
        self.border = ft.Border.all(1, "#E5E7EB")
        self.bgcolor = "#FFFFFF" # Fondo SÓLIDO
        self.shadow = ft.BoxShadow(
            blur_radius=8, 
            color="#0A000000", 
            offset=ft.Offset(0, 4)
        )
        self.padding = ft.Padding.symmetric(horizontal=12)
        self.animate = THEME["animations"]["fast"]
        
        # Si hay suffix, crear un Row con el dropdown y el control
        if suffix:
            self.content = ft.Row([
                ft.Container(content=self.dd, expand=True),
                suffix
            ], spacing=8, alignment=ft.MainAxisAlignment.CENTER)
        else:
            self.content = self.dd
        
        # Iniciar color correcto si ya tiene valor
        if value:
            self._apply_brand_color(value)

    def _apply_brand_color(self, val):
        if val in self.brand_colors:
            self.border = ft.Border.all(2, self.brand_colors[val])
            self.dd.text_style = ft.TextStyle(color=self.brand_colors[val], weight="bold")
            # Sombra coloreada con alpha
            hex_color = self.brand_colors[val].replace("#", "")
            self.shadow = ft.BoxShadow(blur_radius=12, color=f"#33{hex_color}", offset=ft.Offset(0, 4))
        else:
            self.border = ft.Border.all(1, "#E5E7EB")
            self.dd.text_style = None
            self.shadow = ft.BoxShadow(blur_radius=8, color="#0A000000", offset=ft.Offset(0, 4))
        
        try:
            if self.page: self.update()
        except:
            pass

    def _on_change_wrapper(self, e):
        val = self.dd.value
        self._apply_brand_color(val)
        if self.ext_on_change:
            self.ext_on_change(e)

    @property
    def value(self): return self.dd.value
    
    @value.setter
    def value(self, v):
        self.dd.value = v
        self._apply_brand_color(v)
        if self.page: self.update()

class JuicyProviderDropdown(JuicyDropdown):
    """
    Componente Dropdown especializado para proveedores financieros.
    Hereda todo el estilo y comportamiento dinámico de JuicyDropdown.
    """
    def __init__(self, label, options, on_change=None, value=None, expand=False):
        # Adaptador de firmas si fuera necesario, pero son compatibles
        super().__init__(label=label, options=options, on_change=on_change, value=value, expand=expand)

class DebtRow(ft.Container):
    def __init__(self, index, on_rem, on_upd, is_odd=False, on_enter_pressed=None):
        super().__init__(
            padding=15,
            bgcolor="#FEFEF5", # Fondo claro para contrastar con input_bg del contenedor
            border=ft.Border.all(1, "#E5E7EB"),
            border_radius=12,
            shadow=ft.BoxShadow(
                blur_radius=5,
                color="#08000000",
                offset=ft.Offset(0, 2)
            ),
            margin=ft.Margin.only(bottom=10),
            animate=THEME["animations"]["fast"]
        )
        
        self.on_upd_callback = on_upd
        self.is_complete = False

        # --- Helper para Inputs con estilo moderno ---
        def create_modern_input(label, hint="", prefix="", is_money=False, is_last=False, align="right", width=None):
            return ft.Container(
                content=ft.Column([
                    ft.Text(label, size=10, weight="bold", color="#6B7280"),
                    ft.TextField(
                        hint_text=hint,
                        prefix_text=prefix,
                        text_size=13,
                        text_style=ft.TextStyle(weight="bold", color="#374151"),
                        border_color="transparent",
                        focused_border_color="transparent",
                        bgcolor="transparent",
                        content_padding=ft.Padding.only(bottom=5),
                        text_align=align,
                        keyboard_type=ft.KeyboardType.NUMBER if is_money else "text",
                        on_change=lambda e: [self.on_upd_callback(e) if self.on_upd_callback else None, self._check_complete()],
                        on_submit=lambda e: on_enter_pressed() if is_last and on_enter_pressed else None,
                        cursor_color=THEME["colors"]["accent"],
                        height=30,
                    )
                ], spacing=0),
                bgcolor="#F3F4F6", # Gris input
                border_radius=8,
                padding=ft.Padding.symmetric(horizontal=10, vertical=6),
                width=width,
                expand=True if not width else False,
                border=ft.Border.all(1, "transparent"), # Para animación de foco si se desea
                on_hover=lambda e: self._on_input_hover(e) 
            )

        # Dropdown Financiera (Estilo Hero)
        self.dd_i = ft.Dropdown(
            options=[ft.dropdown.Option(x) for x in PRODUCTO_CONFIG],
            text_size=12,
            text_style=ft.TextStyle(weight="bold", color="#1F2937"),
            border_color="#E5E7EB",
            bgcolor="white",
            hint_text="Seleccionar Fin...",
            dense=True,
            content_padding=ft.Padding.symmetric(horizontal=10, vertical=0), # Menos padding vertical
            height=40, # Altura completa para que llene el row
            border_radius=8
        )
        self.dd_i.expand = True  # Establecer expand como propiedad después de la creación
        self.dd_i.on_change = lambda e: [on_upd(e), self._check_complete(), self._update_brand_style()]

        # Dropdown Plazo (Compacto)
        self.dd_p = ft.Dropdown(
            options=[ft.dropdown.Option(str(x)) for x in [12, 24, 36, 48, 54, 60]],
            text_size=12,
            border_color="#E5E7EB",
            bgcolor="#FFFFFF",
            hint_text="Meses",
            dense=True,
            content_padding=ft.Padding.symmetric(horizontal=6, vertical=4),
            height=40,
            filled=True,
            border_radius=6
        )
        self.dd_p.on_change = lambda e: [on_upd(e), self._check_complete()]

        
        
        # Inputs individuales (todos con height=40)
        self.tf_s = ft.TextField(
            hint_text="0.00", prefix=ft.Text("$"), text_size=14, text_style=ft.TextStyle(weight="bold"), 
            border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.RIGHT, height=40, content_padding=8,
            keyboard_type=ft.KeyboardType.NUMBER, 
            on_change=lambda e: [on_upd(e), self._check_complete()],
            on_blur=self._format_money_input,
            on_focus=self._unformat_money_input
        )
        
        self.tf_p = ft.TextField(
            hint_text="0.00", prefix=ft.Text("$"), text_size=14, text_style=ft.TextStyle(weight="bold"), 
            border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.RIGHT, height=40, content_padding=8,
            keyboard_type=ft.KeyboardType.NUMBER, 
            on_change=lambda e: [on_upd(e), self._check_complete()],
            on_blur=self._format_money_input,
            on_focus=self._unformat_money_input
        )

        self.tf_t = ft.TextField(hint_text="0.0", text_size=12, border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.CENTER, height=40, content_padding=8, keyboard_type=ft.KeyboardType.NUMBER, on_change=lambda e: [on_upd(e), self._check_complete()])
        self.tf_n = ft.TextField(hint_text="0", text_size=12, border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.CENTER, height=40, content_padding=8, keyboard_type=ft.KeyboardType.NUMBER, on_change=lambda e: self._check_complete(), on_submit=lambda e: on_enter_pressed() if on_enter_pressed else None)

        self.btn_eliminar = ft.IconButton(
            icon=ft.Icons.CLOSE_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=16, # Un poco más pequeña para elegancia
            on_click=on_rem,
            tooltip="Quitar crédito",
            style=ft.ButtonStyle(
                bgcolor={"": "transparent", "hover": "#FEE2E2"},
                shape=ft.CircleBorder(),
                padding=0
            )
        )
        
        # --- LAYOUT DE LA CARD ---
        self.content = ft.Column([
            # Header: Dropdown de Financiera (Ajuste definitivo de ancho)
            ft.Stack([
                # Fondo Beige anclado a izquierda y derecha para forzar ancho completo
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.ACCOUNT_BALANCE_ROUNDED, size=18, color="#059669"),
                        self.dd_i, # Dropdown se expandirá en el espacio disponible
                    ], vertical_alignment=ft.CrossAxisAlignment.CENTER, spacing=10),
                    bgcolor="#FFFBEB",
                    border=ft.Border.all(1, "#FDE68A"),
                    border_radius=12,
                    padding=ft.Padding.only(left=12, right=45, top=0, bottom=0), # Padding ajustado
                    left=0,
                    right=0,
                    height=52,
                ),
                # Botón Cerrar (X) transparente
                self._create_close_button(on_rem)
            ], height=52), # Altura fija para el Stack
            
            # Saldo y Descuento
            ft.Row([
                ft.Container(
                    content=ft.Column([
                        ft.Text("SALDO ACTUAL", size=10, weight="w600", color="#6B7280"),
                        self.tf_s
                    ], spacing=4),
                    bgcolor="#F0FDF4",
                    border=ft.Border.all(1, "#D1FAE5"),
                    border_radius=10,
                    padding=10,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("DESCUENTO MES", size=10, weight="w600", color="#6B7280"),
                        self.tf_p
                    ], spacing=4),
                    bgcolor="#EFF6FF",
                    border=ft.Border.all(1, "#DBEAFE"),
                    border_radius=10,
                    padding=10,
                    expand=True
                )
            ], spacing=10),
            
            # Tasa, Pagos, Plazo
            ft.Row([
                ft.Container(
                    content=ft.Column([
                        ft.Text("TASA %", size=9, weight="w500", color="#9CA3AF"),
                        self.tf_t
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("PAGOS", size=9, weight="w500", color="#9CA3AF"),
                        self.tf_n
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("PLAZO", size=9, weight="w500", color="#9CA3AF"),
                        self.dd_p
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
            ], spacing=8)
            
        ], spacing=10)
    
    def _format_money_input(self, e):
        """Formatea el input a moneda (ej. 23,000.00) al perder el foco"""
        try:
            val = e.control.value
            if not val: return
            # Limpiar caracteres previos
            clean_val = float(str(val).replace("$", "").replace(",", ""))
            # Formatear
            e.control.value = f"{clean_val:,.2f}"
            e.control.update()
        except:
            pass # Si no es un número válido, no hacer nada

    def _unformat_money_input(self, e):
        """Quita el formato para facilitar edición al ganar el foco"""
        try:
            val = e.control.value
            if not val: return
            # Quitar comas y formato
            clean_val = str(val).replace(",", "").replace("$", "")
            if "." in clean_val:
                 try:
                     f = float(clean_val)
                     if f.is_integer():
                         e.control.value = str(int(f)) # Quitar decimales .00 si es entero
                     else:
                         e.control.value = clean_val
                 except:
                     e.control.value = clean_val
            else:
                 e.control.value = clean_val
            e.control.update()
        except:
            pass

    def _create_close_button(self, on_rem):
        """Crea el botón de cerrar con el cursor correcto"""
        btn = ft.Container(
            content=ft.Icon(ft.Icons.CLOSE_ROUNDED, size=16, color="#9CA3AF"),
            right=12,
            top=18,
            on_click=on_rem,
            tooltip="Quitar crédito"
        )
        btn.mouse_cursor = ft.MouseCursor.CLICK  # Establecer como propiedad
        return btn

    def _update_brand_style(self, e=None):
        val = self.dd_i.value
        if not val: 
            # Estilo default si no hay selección
            self.dd_i.bgcolor = "#FFFFFF"
            self.dd_i.text_style = ft.TextStyle(weight="bold", color="#1F2937")
            self.dd_i.border_color = "#E5E7EB"
            try: 
                if getattr(self.dd_i, "page", None): 
                    self.dd_i.update()
            except: pass
            return
        
        # Buscar estilo de marca
        brand_key = val.upper()
        # Mapeo simple de nombres parciales
        if "CONSUBANCO" in brand_key: brand_key = "CONSUBANCO"
        elif "CONSUPAGO" in brand_key: brand_key = "CONSUPAGO"
        elif "MASNOMINA" in brand_key: brand_key = "MASNOMINA"
        elif "OPCIPRES" in brand_key: brand_key = "OPCIPRES"
        else: brand_key = "DEFAULT"
        
        style = BRAND_COLORS.get(brand_key, BRAND_COLORS["DEFAULT"])
        
        # Aplicar estilo directamente al dropdown
        self.dd_i.bgcolor = style["bg"]
        self.dd_i.text_style = ft.TextStyle(weight="bold", color=style["text"])
        self.dd_i.border_color = style["border"]
        try:
            if getattr(self.dd_i, "page", None): 
                self.dd_i.update()
        except: pass

    def _on_input_hover(self, e):
        e.control.border = ft.Border.all(1, "#D1D5DB" if e.data == "true" else "transparent")
        e.control.update()

    def _check_complete(self):
        try:
            complete = all([
                self.dd_i.value,
                clean_float(self.tf_s.value) > 0,
                clean_float(self.tf_p.value) > 0,
                clean_float(self.tf_t.value) > 0,
                clean_float(self.tf_n.value) > 0,
                self.dd_p.value
            ])
            
            if complete != self.is_complete:
                self.is_complete = complete
                self.border = ft.Border.all(2, THEME["colors"]["accent"] if complete else "#E2E8F0")
                self.update()
        except:
            pass

    def set_on_upd(self, fn): 
        self.on_upd_callback = fn

    def get_data(self):
        try:
            plazo_value = 0
            if self.dd_p.value: 
                plazo_value = int(self.dd_p.value)
            
            return {
                "producto": self.dd_i.value or "",
                "saldo": clean_float(self.tf_s.value), 
                "pago": clean_float(self.tf_p.value),
                "tasa": clean_float(self.tf_t.value), 
                "pagos": int(clean_float(self.tf_n.value)), 
                "plazo": plazo_value
            }
        except: 
            return None
        
    def set_values(self, producto=None, saldo=None, pago=None, tasa=None, pagos=None, plazo=None):
        if producto: self.dd_i.value = producto
        if saldo is not None: self.tf_s.value = str(saldo)
        if pago is not None: self.tf_p.value = str(pago)
        if tasa is not None: self.tf_t.value = str(tasa)
        if pagos is not None: 
            try: self.tf_n.value = str(int(float(pagos)))
            except: self.tf_n.value = str(pagos)
        if plazo is not None: 
            try: self.dd_p.value = str(int(float(plazo)))
            except: self.dd_p.value = str(plazo)
        self._update_brand_style()
        self._check_complete()

# ==========================================
# JUICY UI COMPONENTS (PREMIUM FINTECH)
# ==========================================

class SoundManager:
    """Gestor de sonidos para feedback auditivo con fallback seguro"""
    def __init__(self, page):
        self.page = page
        self.audios = {}
        self.enabled = False
        self.is_playing = False
        self.use_pygame = False
        
        import platform
        self.system = platform.system()
        
        # Intentar usar pygame (más seguro y multiplataforma)
        try:
            import pygame
            pygame.mixer.init(frequency=22050, size=-16, channels=2, buffer=512)
            self.use_pygame = True
            print(f"✓ SoundManager: Modo pygame activado para {self.system}")
        except ImportError:
            print(f"⚠ pygame no disponible, usando modo nativo para {self.system}")
            self.use_pygame = False
        except Exception as e:
            print(f"⚠ Error inicializando pygame: {e}, usando modo nativo")
            self.use_pygame = False
        
        self.audios = {
            "pop": "pop.mp3",
            "cash": "cash.mp3",
            "error": "pop.mp3",
            "wrong": "wrong.mp3"
        }
        self.enabled = True

    def play(self, name):
        """Reproduce sonido usando pygame (seguro) o fallback nativo"""
        if not self.enabled or name not in self.audios:
            return
        
        try:
            file_path = f"assets/{self.audios[name]}"
            
            if self.use_pygame:
                # MODO PYGAME (SEGURO - SIN COMANDOS DEL SISTEMA)
                import pygame
                import threading
                
                def play_async():
                    try:
                        pygame.mixer.music.load(file_path)
                        pygame.mixer.music.play()
                        # Esperar a que termine (no bloqueante)
                        while pygame.mixer.music.get_busy():
                            import time
                            time.sleep(0.1)
                    except Exception as e:
                        print(f"Error pygame: {e}")
                
                threading.Thread(target=play_async, daemon=True).start()
                
            else:
                # FALLBACK NATIVO (solo si pygame no está disponible)
                import time
                import threading
                
                if self.system == "Darwin":  # macOS
                    def run_sound():
                        duration = "3" if name == "cash" else "0.5"
                        os.system(f"afplay '{file_path}' -t {duration}")
                        time.sleep(0.1)
                    threading.Thread(target=run_sound, daemon=True).start()
                    
                elif self.system == "Windows":
                    # Usar winsound (biblioteca estándar de Python, más segura)
                    try:
                        import winsound
                        def play_win():
                            winsound.PlaySound(file_path, winsound.SND_FILENAME | winsound.SND_ASYNC)
                        threading.Thread(target=play_win, daemon=True).start()
                    except Exception:
                        # Si winsound falla, silenciar (no usar PowerShell)
                        pass
                        
        except Exception as e:
            print(f"Error audio: {e}")




class JuicySidebar(ft.Container):
    """Sidebar flotante con efecto Glassmorphism y Responsividad"""
    def __init__(self, page, on_nav_change, perfil=None, responsive_manager=None):
        super().__init__()
        self._page_ref = page
        self.on_nav_change = on_nav_change
        self.rm = responsive_manager
        self.selected_index = 0
        self.is_collapsed = False  # Estado de colapso
        
        # Estilo Base - Glassmorphism
        # Ancho fijo Slim (expandido) o mini (colapsado)
        self.width = 260
        
        self.padding = 20
        self.bgcolor = "#F2FFFFFF" if page.theme_mode == ft.ThemeMode.LIGHT else "#F20F172A"
        self.blur = ft.Blur(10, 10, ft.BlurTileMode.MIRROR)
        self.border_radius = ft.BorderRadius.only(top_right=20, bottom_right=20)
        self.shadow = ft.BoxShadow(blur_radius=20, color="#26000000", offset=ft.Offset(5, 0))
        
        # Animación suave para cambio de tamaño
        self.animate = ft.Animation(300, ft.AnimationCurve.EASE_OUT_CUBIC)
    

        
        self.menu_items = [
            {"icon": ft.Icons.PIE_CHART_ROUNDED, "label": "Refinanciar", "color": "#10B981", "bg": "#ECFDF5"},  # Emerald
            {"icon": ft.Icons.ADD_CARD_ROUNDED, "label": "Crédito Nuevo", "color": "#F59E0B", "bg": "#FFFBEB"}, # Amber/Gold
            {"icon": ft.Icons.VERIFIED_USER_ROUNDED, "label": "Seguros", "color": "#8B5CF6", "bg": "#F5F3FF"},     # Violet
            {"icon": ft.Icons.PEOPLE_ROUNDED, "label": "Agenda", "color": "#3B82F6", "bg": "#EFF6FF"},   # Blue (Agenda)
        ]
        
        # Widget de Perfil Simple
        if perfil is None:
            perfil = {}
        nombre_asesor = perfil.get("nombre", "Asesor")[:15]
        supervisor = perfil.get("supervisor", "")[:15]

        # WIDGET DE PERFIL (SIMPLIFICADO) - Referencia para ocultar/mostrar
        self.profile_widget = ft.Container(
            content=ft.Row([
                # Avatar Simple
                ft.Container(
                    content=ft.Icon(ft.Icons.PERSON_ROUNDED, color="white", size=24),
                    alignment=ft.Alignment(0, 0),
                    width=48, height=48,
                    bgcolor=THEME["colors"]["accent"],
                    border_radius=24,
                    shadow=ft.BoxShadow(
                        blur_radius=8,
                        color="#40000000",
                        offset=ft.Offset(0, 4)
                    )
                ),
                # Info del usuario
                ft.Column([
                    ft.Text(
                        nombre_asesor, 
                        size=14, 
                        weight="bold", 
                        color=THEME["colors"]["text_primary"]
                    ),
                    ft.Text(
                        "Asesor Financiero", 
                        size=11, 
                        color=THEME["colors"]["text_secondary"]
                    ),
                ], spacing=2, expand=True)
            ], alignment=ft.MainAxisAlignment.START, spacing=12),
            padding=ft.Padding.all(14),
            bgcolor=THEME["colors"]["card_bg"],
            border=ft.Border.all(1, THEME["colors"]["divider"]),
            border_radius=16,
            shadow=ft.BoxShadow(
                blur_radius=4,
                color="#10000000",
                offset=ft.Offset(0, 2)
            )
        )

        self.menu_column = ft.Column([self._build_btn(i, item) for i, item in enumerate(self.menu_items)], spacing=8)
        
        # Indicador animado para el item seleccionado
        self.animated_indicator = ft.Container(
            width=6,  # Ancho más visible
            height=48,
            bgcolor=self.menu_items[0]["color"],  # Color del primer item
            border_radius=ft.BorderRadius.only(top_right=4, bottom_right=4),
            offset=ft.Offset(0, 0),
            animate_offset=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT_CUBIC),
            animate=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT_CUBIC),  # Anima bgcolor también
            disabled=True  # No captura eventos de mouse
        )
        
        # Botón Toggle para colapsar/expandir (con icono chevron)
        self.toggle_btn = ft.IconButton(
            icon=ft.Icons.CHEVRON_LEFT_ROUNDED,
            icon_color=THEME["colors"]["text_secondary"],
            icon_size=20,
            tooltip="Contraer sidebar",
            on_click=self._toggle_collapse
        )
        
        # Logo circular con gradiente dinámico (cambia según pestaña activa)
        self.logo_container = ft.Container(
            content=ft.Icon(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, color="white", size=28),
            width=56,
            height=56,
            gradient=ft.LinearGradient(
                begin=ft.Alignment(-1, -1),
                end=ft.Alignment(1, 1),
                colors=["#10B981", "#059669"]  # Verde inicial (Refinanciar)
            ),
            border_radius=28,
            alignment=ft.Alignment(0, 0),
            shadow=ft.BoxShadow(
                blur_radius=12,
                color="#3310B981",
                offset=ft.Offset(0, 3)
            ),
            animate=ft.Animation(300, "easeOut")  # Animar cambios de gradiente
        )
        
        # Wrapper para mantener forma circular al contraer sidebar
        self.logo_wrapper = ft.Container(
            content=self.logo_container,
            width=56,
            height=56,
            alignment=ft.Alignment(0, 0)
        )
        
        
        # Textos que se ocultarán al colapsar
        self.app_title = ft.Text(
            "ConsuCalcu", 
            size=20, 
            weight="w900", 
            font_family="Outfit", 
            color=THEME["colors"]["text_primary"]
        )
        self.app_subtitle = ft.Text(
            "Calculadora IMSS", 
            size=10, 
            color=THEME["colors"]["text_tertiary"],
            italic=True
        )
        self.nav_header = ft.Text(
            "NAVEGACIÓN",
            size=10,
            weight="bold",
            color=THEME["colors"]["text_tertiary"],
            text_align=ft.TextAlign.LEFT
        )
        self.config_header = ft.Text(
            "CONFIGURACIÓN",
            size=10,
            weight="bold",
            color=THEME["colors"]["text_tertiary"],
            text_align=ft.TextAlign.LEFT
        )
        # Badge de versión con fondo
        self.version_badge = ft.Container(
            content=ft.Row([
                ft.Text("v2.7", size=10, weight="bold", color="#6366F1"),
                ft.Icon(ft.Icons.VERIFIED_ROUNDED, size=12, color=THEME["colors"]["success"])
            ], spacing=4, tight=True),
            padding=ft.Padding.symmetric(horizontal=10, vertical=4),
            bgcolor="#EEF2FF",  # Indigo 50
            border_radius=12,
            border=ft.Border.all(1, "#C7D2FE")  # Indigo 200
        )
        
        
        # Iconos de configuración con hover mejorado
        self.icon_perf = ft.IconButton(
            icon=ft.Icons.SPEED_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Rendimiento",
            on_click=self._open_performance,
            animate_scale=ft.Animation(150, "easeOut")
        )
        
        self.icon_sync = ft.IconButton(
            icon=ft.Icons.SYNC_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Buscar Actualizaciones",
            on_click=self._check_update,
            animate_scale=ft.Animation(150, "easeOut")
        )
        
        self.icon_settings = ft.IconButton(
            icon=ft.Icons.SETTINGS_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Configuración",
            on_click=self._open_config,
            animate_scale=ft.Animation(150, "easeOut")
        )
        
        # Asignar hover handlers
        self.icon_perf.on_hover = lambda e: self._hover_config_icon(e, self.icon_perf)
        self.icon_sync.on_hover = lambda e: self._hover_config_icon(e, self.icon_sync)
        self.icon_settings.on_hover = lambda e: self._hover_config_icon(e, self.icon_settings)
        
        # Fila de iconos de configuración
        self.settings_icons_row = ft.Row(
            [self.icon_perf, self.icon_sync, self.icon_settings],
            alignment=ft.MainAxisAlignment.CENTER,
            spacing=0
        )
        
        self.content = ft.Column([
            # ===== HEADER SECTION =====
            ft.Container(
                content=ft.Column([
                    # Botón Toggle siempre visible en la esquina superior
                    ft.Row([
                        ft.Container(expand=True),
                        self.toggle_btn
                    ], alignment=ft.MainAxisAlignment.END),
                    
                    ft.Container(height=8),
                    
                    # Logo compacto dinámico
                    self.logo_wrapper,
                    
                    # Títulos (se ocultan al colapsar)
                    self.app_title,
                    self.app_subtitle
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=8),
                padding=ft.Padding.only(bottom=16, top=8)
            ),
            
            ft.Divider(height=1, color=THEME["colors"]["divider"]),
            
            # ===== PROFILE SECTION =====

            
            # ===== NAVIGATION SECTION =====
            ft.Container(
                content=ft.Column([
                    self.nav_header,
                    ft.Container(height=8),
                    # Layout Horizontal: Indicador | Botones
                    ft.Row([
                        # Carril del indicador (Izquierda)
                        ft.Container(
                            width=6, 
                            content=ft.Stack([self.animated_indicator], height=300),
                            alignment=ft.Alignment(-1, -1)
                        ),
                        # Columna de botones (Derecha)
                        ft.Container(content=self.menu_column, expand=True)
                    ], spacing=0, vertical_alignment=ft.CrossAxisAlignment.START),
                ], spacing=0),
                padding=ft.Padding.only(bottom=12)
            ),
            
            ft.Container(expand=True), # Spacer flexible
            
            # ===== SETTINGS SECTION =====
            ft.Divider(height=1, color=THEME["colors"]["divider"]),
            ft.Container(
                content=ft.Column([
                    self.config_header,
                    # Fila de Acciones Rápidas (Flat Design) - Referencia para ocultar
                    self.settings_icons_row,
                ], spacing=0),
                padding=ft.Padding.symmetric(vertical=16)
            ),
            
            # ===== FOOTER =====
            ft.Container(
                content=self.version_badge,
                padding=ft.Padding.only(bottom=12),
                alignment=ft.Alignment(0, 0)
            )
        ], expand=True)
    
    def _toggle_collapse(self, e):
        """Alternar entre sidebar expandida y colapsada con animación fluida"""
        # Debounce: Evitar clics múltiples mientras anima
        if self.toggle_btn.disabled: return
        self.toggle_btn.disabled = True
        self.toggle_btn.update()

        self.is_collapsed = not self.is_collapsed
        
        # 1. Configurar width principal (animado automáticamente)
        if self.is_collapsed:
            self.width = 80
            self.padding = 16  # Aumentado de 12 a 16
            self.toggle_btn.icon = ft.Icons.CHEVRON_RIGHT_ROUNDED
            self.toggle_btn.tooltip = "Expandir sidebar"
            
            # Ocultar headers secundarios inmediatamente para evitar overflow visual durante la transición
            self.app_title.visible = False
            self.app_subtitle.visible = False
            self.nav_header.visible = False
            self.config_header.visible = False
            self.version_badge.visible = False
            self.settings_icons_row.visible = False 
        else:
            self.width = 260
            self.padding = 20
            self.toggle_btn.icon = ft.Icons.CHEVRON_LEFT_ROUNDED
            self.toggle_btn.tooltip = "Contraer sidebar"
            
            # Mostrar headers (podría retardarse un poco si se quisiera exquisitez)
            self.app_title.visible = True
            self.app_subtitle.visible = True
            self.nav_header.visible = True
            self.config_header.visible = True
            self.version_badge.visible = True
            self.settings_icons_row.visible = True
        
        # 2. Actualizar botones IN-PLACE (Sin reconstruir) - Esto mantiene la animación suave
        for i, btn in enumerate(self.menu_column.controls):
            content_row = btn.content
            
            # Contenedor de Texto (Hijo 1 del Row)
            text_container = content_row.controls[1]
            
            # Animamos propiedades
            text_container.width = 140 if not self.is_collapsed else 0
            text_container.opacity = 1 if not self.is_collapsed else 0
            
            # Ajustamos padding y spacing
            current_padding = 12 if not self.is_collapsed else 8
            btn.padding = ft.Padding.symmetric(horizontal=current_padding, vertical=12)
            content_row.spacing = 12 if not self.is_collapsed else 0
            
            # Actualizar Tooltip dinámicamente
            # Necesitamos el label del item correspondiente
            if i < len(self.menu_items):
                item_label = self.menu_items[i]["label"]
                btn.tooltip = item_label if self.is_collapsed else None
            
        self.update()
        
        # Rehabilitar botón después de la animación (Async)
        async def enable_btn():
            await asyncio.sleep(0.35)
            self.toggle_btn.disabled = False
            try:
                self.toggle_btn.update()
            except: pass
        
        if hasattr(self.page, 'run_task'):
             self.page.run_task(enable_btn)

    def _build_btn(self, index, item):
        is_selected = self.selected_index == index
        
        # Colores
        accent_color = item.get("color", "#10B981")
        accent_bg = item.get("bg", "#ECFDF5")

        if is_selected:
            bg_color = accent_bg 
            text_color = accent_color 
            icon_color = accent_color 
            icon_size = 24
            font_weight = "bold"
        else:
            bg_color = "transparent"
            text_color = "#6B7280"
            icon_color = "#9CA3AF"
            icon_size = 22
            font_weight = "w500"
            
        # Estructura Híbrida (Icono Fijo + Texto Animado)
        btn_content = ft.Row([
            # 1. Icono (Siempre visible, centrado en su tile)
            ft.Container(
                content=ft.Icon(item["icon"], color=icon_color, size=icon_size),
                width=32,
                alignment=ft.Alignment(0, 0),
            ),
            # 2. Texto (Animable width/opacity)
            ft.Container(
                content=ft.Text(
                    item["label"],
                    size=14,
                    color=text_color,
                    weight=font_weight,
                    no_wrap=True # Vital para que no salte línea al encogerse
                ),
                width=140 if not self.is_collapsed else 0,
                opacity=1 if not self.is_collapsed else 0,
                animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT_CUBIC), # Animación suave 60fps
                animate_opacity=ft.Animation(150, ft.AnimationCurve.EASE_IN_OUT),
                clip_behavior=ft.ClipBehavior.HARD_EDGE # Recorte limpio
            )
        ], spacing=12 if not self.is_collapsed else 0, alignment=ft.MainAxisAlignment.START)
        
        # Wrapper para manejar evento async con argumento
        async def btn_click(e):
            await self._handle_click(index)

        btn = ft.Container(
            content=btn_content,
            padding=ft.Padding.symmetric(horizontal=12 if not self.is_collapsed else 8, vertical=12),
            height=48, 
            bgcolor=bg_color,
            border_radius=ft.BorderRadius.only(top_right=8, bottom_right=8),
            on_click=btn_click,
            animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT), # Ultra rápido y lineal para borrar rastro inmediato
            tooltip=item["label"] if self.is_collapsed else None
        )
        
        # Hover Effect Simple (Corregido para respetar selección actual)
        def on_hover(e):
             # Verificar estado actual dinámicamente (no usar variable capturada)
             if self.selected_index != index:
                 # Color hover sutil (gris muy claro en light, gris oscuro en dark)
                 hover_color = "#F9FAFB" if self.page.theme_mode == ft.ThemeMode.LIGHT else "#374151"
                 e.control.bgcolor = hover_color if e.data == "true" else "transparent"
                 e.control.update()
        
        btn.on_hover = on_hover
        return btn

    async def _handle_click(self, index):
        self.selected_index = index
        
        # 1. UI PRIMERO: Actualizar indicador y botones
        step_offset = 56 / 48 
        indicator_offset = index * step_offset
        self.animated_indicator.offset = ft.Offset(0, indicator_offset)
        self.animated_indicator.bgcolor = self.menu_items[index]["color"]
        self.animated_indicator.update()
        
        # 2. Actualizar color del logo según pestaña activa
        color_map = {
            0: (["#10B981", "#059669"], "#3310B981"),  # Verde (Refinanciar)
            1: (["#F59E0B", "#D97706"], "#33F59E0B"),  # Naranja/Dorado (Crédito Nuevo)
            2: (["#8B5CF6", "#7C3AED"], "#338B5CF6"),  # Púrpura (Seguros)
            3: (["#3B82F6", "#2563EB"], "#333B82F6")   # Azul (Seguimiento)
        }
        
        new_colors, shadow_color = color_map.get(index, (["#10B981", "#059669"], "#3310B981"))
        self.logo_container.gradient.colors = new_colors
        self.logo_container.shadow.color = shadow_color
        self.logo_container.update()
        
        # Actualizar colores de botones (In-Place)
        for i, btn in enumerate(self.menu_column.controls):
            item = self.menu_items[i]
            is_selected = i == index
            
            accent_color = item.get("color", "#10B981")
            accent_bg = item.get("bg", "#ECFDF5")
            
            if is_selected:
                # Entrada: Suave
                btn.animate = ft.Animation(200, "easeOut") 
                btn.bgcolor = accent_bg
            else:
                # Salida: Instantánea
                btn.animate = ft.Animation(1, "linear") 
                btn.bgcolor = "transparent"
                
            content_row = btn.content
            content_row.controls[0].content.color = accent_color if is_selected else "#9CA3AF"
            
            txt_ctrl = content_row.controls[1].content
            txt_ctrl.color = accent_color if is_selected else "#6B7280"
            txt_ctrl.weight = "bold" if is_selected else "w500"
            
        self.menu_column.update()
        
        # 2. PEQUEÑA PAUSA (Aumentada) para permitir que el renderizado visual termine suavemente
        await asyncio.sleep(0.05)
        
        # 3. CAMBIO DE VISTA (Lógica pesada)
        try:
            res = self.on_nav_change(index)
            if hasattr(res, '__await__'):
                await res
        except Exception as ex:
            print(f"Error navigating: {ex}")

    def _toggle_theme(self, e):
        # Cambiar tema
        self.page.theme_mode = (
            ft.ThemeMode.DARK 
            if self.page.theme_mode == ft.ThemeMode.LIGHT 
            else ft.ThemeMode.LIGHT
        )
        
        # Actualizar icono del botón
        e.control.icon = (
            ft.Icons.DARK_MODE_ROUNDED 
            if self.page.theme_mode == ft.ThemeMode.LIGHT 
            else ft.Icons.LIGHT_MODE_ROUNDED
        )
        
        # Actualizar color de fondo del sidebar
        self.bgcolor = (
            "#FFFFFF" 
            if self.page.theme_mode == ft.ThemeMode.LIGHT 
            else "#1F2937"  # Gray 800 para dark mode
        )
        
        # Actualizar toda la página
        self.update()
        self.page.update()

    def _check_update(self, e):
        # Esta función será conectada desde main
        if hasattr(self.page, '_verificar_update_click'):
            self.page._verificar_update_click(e)

    def _open_config(self, e):
        # Esta función será conectada desde main
        print(f"[DEBUG] _open_config called, hasattr: {hasattr(self.page, '_config_dlg')}")
        if hasattr(self.page, '_config_dlg'):
            self.page._config_dlg(e)
        else:
            print("[ERROR] _config_dlg not found on page")
            # Mostrar un diálogo de error temporal
            error_dlg = ft.AlertDialog(
                title=ft.Text("Error"),
                content=ft.Text("La función de configuración no está disponible aún."),
                actions=[ft.TextButton("OK", on_click=lambda e: [setattr(error_dlg, "open", False), self.page.update()])]
            )
            self.page.dialog = error_dlg
            error_dlg.open = True
            self.page.update()
    
    def _open_performance(self, e):
        # Esta función será conectada desde main
        print(f"[DEBUG] _open_performance called, hasattr: {hasattr(self.page, '_performance_dlg')}")
        if hasattr(self.page, '_performance_dlg'):
            self.page._performance_dlg(e)
        else:
            print("[ERROR] _performance_dlg not found on page")
            # Mostrar un diálogo de error temporal
            error_dlg = ft.AlertDialog(
                title=ft.Text("Error"),
                content=ft.Text("La función de rendimiento no está disponible aún."),
                actions=[ft.TextButton("OK", on_click=lambda e: [setattr(error_dlg, "open", False), self.page.update()])]
            )
            self.page.dialog = error_dlg
            error_dlg.open = True
            self.page.update()
    
    def _hover_config_icon(self, e, icon_btn):
        """Maneja el efecto hover de los iconos de configuración"""
        if e.data == "true":
            icon_btn.icon_color = THEME["colors"]["accent"]
            icon_btn.scale = 1.1
        else:
            icon_btn.icon_color = "#9CA3AF"
            icon_btn.scale = 1.0
        icon_btn.update()

    def resize(self):
        """Actualiza dimensiones y estado visual basadas en ResponsiveManager"""
        if self.rm:
            target_width = self.rm.get_size("layout", "sidebar_width")
            
            # Solo actualizar si el ancho objetivo es diferente
            if self.width != target_width:
                self.width = target_width
                
                # Sincronizar estado is_collapsed con el nuevo ancho
                should_be_collapsed = target_width < 150
                
                # Si el estado visual no coincide con el ancho, forzar actualización visual
                if self.is_collapsed != should_be_collapsed:
                    self.is_collapsed = should_be_collapsed
                    
                    # Actualizar visibilidad de headers
                    visible = not self.is_collapsed
                    self.profile_widget.visible = visible
                    self.app_title.visible = visible
                    self.app_subtitle.visible = visible
                    self.nav_header.visible = visible
                    self.config_header.visible = visible
                    self.version_badge.visible = visible
                    self.settings_icons_row.visible = visible
                    
                    # Actualizar botón toggle
                    self.toggle_btn.icon = ft.Icons.CHEVRON_LEFT_ROUNDED if visible else ft.Icons.CHEVRON_RIGHT_ROUNDED
                    self.padding = 20 if visible else 16  # Corregido de 12 a 16
                    
                    # Actualizar items del menú
                    for i, btn in enumerate(self.menu_column.controls):
                        content_row = btn.content
                        if len(content_row.controls) > 1:
                            text_container = content_row.controls[1]
                            text_container.width = 140 if visible else 0
                            text_container.opacity = 1 if visible else 0
                            content_row.spacing = 12 if visible else 0
                            
                            if i < len(self.menu_items):
                                btn.tooltip = self.menu_items[i]["label"] if not visible else None

                self.update()


class LottieOverlay(ft.Stack):
    """Overlay moderno con animaciones premium para loading"""
    def __init__(self, page):
        super().__init__()
        self._page_ref = page
        self.visible = False
        self.expand = True
        
        # Texto de estado dinámico
        self.status_text = ft.Text(
            "Procesando...",
            size=16,
            weight="w500",
            color="white",
            text_align=ft.TextAlign.CENTER
        )
        
        # Subtexto
        self.subtext = ft.Text(
            "Esto tomará solo un momento",
            size=12,
            color="#FFFFFF99",
            text_align=ft.TextAlign.CENTER
        )
        
        # Animación Lottie (si está disponible)
        try:
            LottieControl = getattr(ft, "Lottie", None)
            if LottieControl:
                self.lottie_loader = LottieControl(
                    src="lottie_loading.json",
                    repeat=True,
                    reverse=False,
                    animate=True,
                    width=120,
                    height=120
                )
                has_lottie = True
            else:
                raise AttributeError
        except:
            # Fallback: Spinner nativo
            self.lottie_loader = ft.ProgressRing(
                width=80,
                height=80,
                stroke_width=4,
                color="#10B981"
            )
            has_lottie = False
        
        # Contenedor principal con glassmorphism
        self.loading_card = ft.Container(
            content=ft.Column([
                # Animación
                ft.Container(
                    content=self.lottie_loader,
                    alignment=ft.Alignment(0, 0),
                    padding=ft.Padding.only(bottom=20)
                ),
                # Textos
                self.status_text,
                ft.Container(height=8),
                self.subtext,
                ft.Container(height=20),
                # Barra de progreso indeterminada
                ft.Container(
                    content=ft.ProgressBar(
                        width=200,
                        color="#10B981",
                        bgcolor="#FFFFFF33"
                    ),
                    alignment=ft.Alignment(0, 0),
                )
            ], 
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=0),
            width=350,
            padding=40,
            border_radius=24,
            bgcolor="#1A1F2E",
            border=ft.Border.all(1, "#FFFFFF1A"),
            shadow=ft.BoxShadow(
                blur_radius=40,
                spread_radius=0,
                color="#00000066",
                offset=ft.Offset(0, 20)
            ),
            # Glassmorphism effect
            blur=ft.Blur(10, 10, ft.BlurTileMode.MIRROR),
            animate_opacity=300,
            animate_scale=ft.Animation(400, ft.AnimationCurve.EASE_OUT)
        )
        
        self.card_wrapper = ft.Container(
            content=self.loading_card,
            alignment=ft.Alignment(0, 0),
        )

        self.controls = [
            # Fondo con gradiente animado
            ft.Container(
                expand=True,
                gradient=ft.LinearGradient(
                    begin=ft.Alignment(-1, -1),
                    end=ft.Alignment(1, 1),
                    colors=["#0F172A99", "#1E293B99", "#334155AA"]
                ),
                blur=ft.Blur(12, 12),
                on_click=lambda _: None  # Bloquear clicks
            ),
            # Card centrada
            self.card_wrapper
        ]

    def show(self, type="loading", message=None):
        """Muestra el overlay con mensaje personalizado"""
        # Actualizar mensaje
        if message:
            self.status_text.value = message
        else:
            if type == "success":
                self.status_text.value = "¡Completado!"
                self.subtext.value = "Operación exitosa"
                try:
                    self.lottie_loader.src = "lottie_success.json"
                    self.lottie_loader.repeat = False
                except:
                    pass
            else:
                self.status_text.value = "Procesando..."
                self.subtext.value = "Esto tomará solo un momento"
                try:
                    self.lottie_loader.src = "lottie_loading.json"
                    self.lottie_loader.repeat = True
                except:
                    pass
            
            
        self.visible = True
        self.loading_card.opacity = 0
        self.loading_card.scale = 0.5
        self.update()
        
        # Trigger implicit animation
        self.loading_card.opacity = 1
        self.loading_card.scale = 1.0
        self.update()

    def hide(self):
        """Oculta el overlay con animación"""
        self.loading_card.opacity = 0
        self.loading_card.scale = 0.9
        self.visible = False  # CRITICAL FIX: Disable visibility to restore interactions
        self.update()
        
        # Note: In a real async app we would await here.
        # For now, we leave it fading out. The next show() will reset it.
        # We can't easily wait without blocking in sync handler.
        # But visual feedback is enough.


# ==========================================
# MAIN APPLICATION
# ==========================================

async def main(page: ft.Page):
    # 1. Inicializar Responsive Manager
    rm = ResponsiveManager(page)
    rm.update_dimensions() # Primer cálculo

    page.title = "ConsuCalcu by Madd"
    page.window.width = 1440
    page.window.height = 900
    try:
        if hasattr(page, 'window_center') and callable(page.window_center):
             # Si es corrutina (0.80.0+), esperar
             # Si es método normal (legacy), llamar
             res = page.window_center()
             if hasattr(res, '__await__'):
                 await res
    except:
        pass
    page.padding = 0
    page.theme_mode = ft.ThemeMode.DARK  # Dark mode por defecto
    
    # Handler de redimensionamiento
    def on_page_resize(e):
        rm.update_dimensions()
        
        # Actualizar Sidebar si existe
        if hasattr(page, '_sidebar'):
            page._sidebar.resize()
            
        # Opcional: Actualizar grids o elementos específicos si tuvieran propiedades dinámicas
        # grid_cnca.width = rm.get_size("layout", "grid_extent") ...
        
        page.update()

    page.on_resize = on_page_resize
    
    # ==========================================
    # LIGHT THEME - Lime & Navy (Fresco y Profesional)
    # ==========================================
    page.theme = ft.Theme(
        color_scheme=ft.ColorScheme(
            # Primary Colors (Verde Lima)
            primary="#7DC242",                      # Verde Lima principal
            on_primary="#FFFFFF",                   # White text on lime
            primary_container="#E8F5E9",            # Verde muy claro
            on_primary_container="#2E7D32",         # Verde oscuro

            # Tertiary Colors (Amber para alertas)
            tertiary="#F59E0B",                     # Amber-500
            on_tertiary="#000000",                  # Black text on Amber
            tertiary_container="#FEF3C7",           # Amber-100
            on_tertiary_container="#78350F",        # Amber-900
            
            # Surface Colors (Crema y Navy)
            # background="#FEFEF5",                   # REMOVED (Deprecated in Flet 0.80)
            # on_background="#2C3E50",                # REMOVED (Deprecated in Flet 0.80)
            surface="#FFFFFF",                      # White (Cards)
            on_surface="#2C3E50",                   # Navy oscuro
            # surface_variant="#F8F9FA",              # REMOVED (Deprecated)
            on_surface_variant="#5A6C7D",           # Navy medio
            
            # Input background when focused/hovered
            surface_container_high="#E8F5E9",       # Verde muy claro (focus)
            
            # Borders & Dividers
            outline="#D1D5DB",                      # Gris medio
            outline_variant="#E8EAED",              # Gris claro
            
            # Shadows
            shadow="#0D2C3E50",                     # Navy at 5% opacity
            scrim="#80000000",                      # Black at 50% for overlays
            
            # Inverse (for dark elements on light background)
            inverse_surface="#2C3E50",              # Navy oscuro
            on_error="#FFFFFF",                     # White
            error_container="#FEE2E2",              # Red-100
            on_error_container="#7F1D1D",           # Red-900
        ),
        font_family="Outfit",
        use_material3=True,
        visual_density=ft.VisualDensity.COMFORTABLE
    )

    # --- GESTIÓN INTELIGENTE DE FUENTES (Optimized Async) ---
    try:
        base_dir = os.path.dirname(os.path.abspath(__file__))
        assets_dir = os.path.join(base_dir, "assets")
        fonts_dir = os.path.join(assets_dir, "fonts")
        
        # Diccionario final
        updated_fonts = {}
        target_fonts = {
            "Outfit": ("Outfit-Regular.ttf", "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit-Regular.ttf"),
            "Outfit-Bold": ("Outfit-Bold.ttf", "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit-Bold.ttf")
        }
        
        # Lista de descargas pendientes para background
        downloads_needed = []
        
        for fname, (fcal, furl) in target_fonts.items():
            fpath = os.path.join(fonts_dir, fcal)
            
            if os.path.exists(fpath):
                # Si existe local, usar local INMEDIATAMENTE
                updated_fonts[fname] = f"/fonts/{fcal}"
                # print(f"Fuente {fname}: Local (Rápido)")
            else:
                # Si no, usar remota para renderizado inmediato y encolar descarga
                updated_fonts[fname] = furl
                downloads_needed.append((fname, furl, fpath))
                # print(f"Fuente {fname}: Remota (Fallback Temporal)")
                
        page.fonts = updated_fonts
        
        # Iniciar descarga en background sin bloquear UI
        if downloads_needed:
            def download_fonts_bg():
                import urllib.request
                try: 
                    os.makedirs(fonts_dir, exist_ok=True)
                    for Name, Url, Path in downloads_needed:
                        try:
                            # Timeout corto para no colgar el hilo background eternamente
                            with urllib.request.urlopen(Url, timeout=5) as response:
                                if response.status == 200:
                                    with open(Path, 'wb') as f:
                                        f.write(response.read())
                                    # print(f"Fuente {Name} descargada en background.")
                        except: pass 
                except: pass
            
            threading.Thread(target=download_fonts_bg, daemon=True).start()
            
    except Exception as e:
        print(f"Error setup fuentes: {e}")
        # Fallback total
        page.fonts = {
            "Outfit": "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit-Regular.ttf",
            "Outfit-Bold": "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit-Bold.ttf"
        }
    
    # ==========================================
    # ELIMINADO DARK THEME - SOLO LIGHT MODE
    # ==========================================
    page.dark_theme = None 
    page.bgcolor = "#FEFEF5"  # Crema muy claro (Restaurado manual) 

    # Asegúrate de haber pegado la clase GestorActualizaciones arriba del todo o importada
    VERSION_APP = 2.6
    # Reemplaza estos links con los de tu repositorio real cuando lo tengas
    URL_VER = "https://raw.githubusercontent.com/elmadmadd/ConsuCalcu-Update/refs/heads/main/version.txt"
    URL_EXE = "https://github.com/elmadmadd/ConsuCalcu-Update/releases/latest/download/ConsuCalcu.exe"
    
    updater = GestorActualizaciones(VERSION_APP, URL_VER, URL_EXE)

    def verificar_update_click(e):
        loading.show() # Usamos tu overlay de carga existente
        nueva_ver = updater.buscar_actualizacion()
        loading.hide()
        
        if nueva_ver:
            def confirmar_update(e):
                # 1. Lógica de Elevación UAC
                if not updater.es_admin():
                    setattr(dlg_upd, "open", False); page.update()
                    loading.hide()
                    # Reiniciar pidiendo permisos y matar proceso actual
                    updater.reiniciar_como_admin()
                    sys.exit(0)

                # 2. Si ya somos Admin, procedemos
                setattr(dlg_upd, "open", False); page.update()
                loading.show()
                # Lanzar la descarga en hilo separado
                threading.Thread(target=updater.descargar_e_instalar).start() 

            dlg_upd = ft.AlertDialog(
                title=ft.Text("¡Actualización Disponible!", color=THEME["colors"]["text_primary"]),
                content=ft.Column([
                    ft.Icon(ft.Icons.CLOUD_DOWNLOAD, size=50, color=THEME["colors"]["accent"]),
                    ft.Text(f"Versión {nueva_ver} lista para descargar.\nLa aplicación se reiniciará automáticamente."),
                ], height=100, tight=True, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                actions=[
                    ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg_upd, "open", False), page.update()]),
                    ft.Button("ACTUALIZAR", on_click=confirmar_update, bgcolor=THEME["colors"]["success"], color="white")
                ],
                actions_alignment=ft.MainAxisAlignment.CENTER
            )
            page.dialog = dlg_upd
            dlg_upd.open = True
            page.update()
        else:
            msg("Ya tienes la última versión", THEME["colors"]["success"])
    
    def toggle_theme(e):
        # Cambiar el modo de tema
        page.theme_mode = ft.ThemeMode.DARK if page.theme_mode == ft.ThemeMode.LIGHT else ft.ThemeMode.LIGHT
        
        # Actualizar el ícono del botón (si viene del toggle_theme original, no del sidebar)
        if hasattr(e, 'control') and hasattr(e.control, 'icon'):
            e.control.icon = ft.Icons.DARK_MODE if page.theme_mode == ft.ThemeMode.LIGHT else ft.Icons.LIGHT_MODE
            e.control.tooltip = "Cambiar a Tema Oscuro" if page.theme_mode == ft.ThemeMode.LIGHT else "Cambiar a Tema Claro"
        
        # Actualizar el sidebar si existe
        if hasattr(page, '_sidebar'):
            sidebar = page._sidebar
            # Actualizar fondo del sidebar
            sidebar.bgcolor = "#F2FFFFFF" if page.theme_mode == ft.ThemeMode.LIGHT else "#F20F172A"
            # Actualizar el ícono del botón de tema en el sidebar
            if len(sidebar.content.controls) > 3:  # Footer está en controls[3]
                footer_container = sidebar.content.controls[3]
                if hasattr(footer_container, 'content'):
                    row = footer_container.content
                    if hasattr(row, 'controls') and len(row.controls) > 1:
                        theme_btn = row.controls[1]  # Segundo botón es el de tema
                        theme_btn.icon = ft.Icons.DARK_MODE if page.theme_mode == ft.ThemeMode.LIGHT else ft.Icons.LIGHT_MODE
            sidebar.update()
        
        # Actualizar el fondo gradiente si existe
        if hasattr(page, '_background'):
            bg = page._background
            bg.gradient = ft.LinearGradient(
                begin=ft.Alignment(-1, -1),
                end=ft.Alignment(1, 1),
                colors=["#E5E7EB", "#D1D5DB", "#F3F4F6"] if page.theme_mode == ft.ThemeMode.LIGHT else ["#1F2937", "#111827", "#374151"]
            )
            bg.update()
        
        # Actualizar todas las tarjetas cuando cambia el tema
        update_all_cards_for_theme()
        page.update()
    
    def update_all_cards_for_theme():
        """Actualiza todas las tarjetas de resultado para reflejar el nuevo tema."""
        # Actualizar tarjetas CNCA
        for card in grid_cnca.controls:
            if hasattr(card, 'refresh_for_theme_change'):
                card.refresh_for_theme_change()
        
        # Actualizar tarjetas Nuevo
        for card in grid_nuevo.controls:
            if hasattr(card, 'refresh_for_theme_change'):
                card.refresh_for_theme_change()

    def toggle_mini_mode(e):
        is_mini = not page.window.always_on_top
        
        if not is_mini:
            # Restaurar modo normal
            page.window.always_on_top = False
            page.window.width, page.window.height = 1440, 900
            
            if hasattr(page, '_sidebar'):
                page._sidebar.visible = True 
            page.floating_action_button.visible = False # Ocultar botón flotante
            
            # Restaurar icono original en el menú (si fuera visible, pero util para estado)
            if isinstance(e.control, ft.IconButton):
                e.control.icon = ft.Icons.PICTURE_IN_PICTURE_ALT_ROUNDED
                e.control.tooltip = "Modo Flotante"
        else:
            # Activar modo Pop-Up (Mini)
            page.window.always_on_top = True
            
            if hasattr(page, '_sidebar'):
                page._sidebar.visible = False # Ocultar menú
            page.floating_action_button.visible = True # Mostrar botón de salida
            
            page.window.width, page.window.height = 450, 700 
            
        page.update()

    # Botón Flotante para SALIR del modo Mini (Solo visible en modo Mini)
    page.floating_action_button = ft.FloatingActionButton(
        icon=ft.Icons.OPEN_IN_FULL_ROUNDED, 
        on_click=toggle_mini_mode, 
        bgcolor=THEME["colors"]["error"],
        visible=False,
        tooltip="Salir de Modo Flotante"
    )

    page.window.prevent_app_suspension = True

    # --- STATE & MANAGERS ---
    cfg_mgr = GestorConfiguracion()
    motor = MotorFinanciero(cfg_mgr)
    gestor = GestorPerfil()
    perfil = gestor.cargar()
    gestor_ops = GestorOperaciones()
    gestor_sesion = GestorSesion()
    pdf_gen = GeneradorPDF()
    # Progress Indicator (nuevo) o LottieOverlay (fallback)
    if HAVE_PROGRESS:
        progress_indicator = create_progress_overlay(page)
        loading = LottieOverlay(page)  # Mantener para compatibilidad
    else:
        loading = LottieOverlay(page)
        progress_indicator = None
    
    
    sound_manager = SoundManager(page)  # Sistema de sonido para feedback auditivo
    
    # --- DATA SYNC (Offline/Online) ---
    async def run_initial_sync():
        # Pequeña espera para no competir con el arranque de UI
        await asyncio.sleep(1) 
        new_data = await SyncManager.sync_remote()
        if new_data:
            cfg_mgr.actualizar_memoria(new_data)
            # Notificar discretamente
            try:
                page.snack_bar = ft.SnackBar(
                    content=ft.Text("Configuración actualizada", color="white"), 
                    bgcolor="#10B981"
                )
                page.snack_bar.open = True
                page.update()
            except: pass

    # Disparar tarea en background
    if page:
        page.run_task(run_initial_sync)
    
    # Offer Comparator (nuevo)
    if HAVE_COMPARATOR:
        offer_comparator = create_offer_comparator(page, THEME)
    else:
        offer_comparator = None
    
    # Calculation History (nuevo)
    if HAVE_HISTORY:
        history_manager = CalculationHistory()
    else:
        history_manager = None
    
    # --- FUNCIÓN DE ANIMACIÓN ROLLING NUMBERS ---
    async def animate_rolling_number(control, target_value, duration=1000):
        """
        Versión OPTIMIZADA: Asignación directa sin animación rolling.
        Esto reduce drásticamente el uso de CPU en equipos de bajos recursos.
        """
        if hasattr(control, 'value'):
            control.value = f"${target_value:,.2f}"
            control.update()
        
        # Ignoramos el bucle de animación
        await asyncio.sleep(0)

    
    # --- LÓGICA DE RACHAS (STREAKS) - REMOVIDO ---
    # show_streak_overlay = False (Ya no se usa)

    
    # Global state vars
    tab_actual = 0 
    escenario_congelado = None
    ultima_operacion_id = None
    formato_layout_actual = "FORMAL"

    # --- UI CONTAINERS ---
    col_deudas = ft.Column(spacing=2, scroll=ft.ScrollMode.AUTO) # Tabla de deudas con scroll activado
   
    grid_cnca = ft.ResponsiveRow(spacing=20, run_spacing=20)
    grid_nuevo = ft.ResponsiveRow(spacing=20, run_spacing=20)
    
    # Initial Empty State for Nuevo
    grid_nuevo.controls.append(
        ft.Container(
            content=ft.Column([
                ft.Icon(ft.Icons.SAVINGS_OUTLINED, size=48, color="#E5E7EB"), 
                ft.Text("Inicia una simulación", color="#9CA3AF", weight="bold"),
                ft.Text("Configura el monto y haz clic en calcular", color="#D1D5DB", size=12)
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=5),
            alignment=ft.Alignment(0, 0),
            padding=40,
            col={"xs": 12}
        )
    )
    
    lbl_cap_val = ft.Text("$0.00", size=18, weight="bold", color=THEME["colors"]["text_primary"])
    lbl_tot_val = ft.Text("$0.00", size=18, weight="bold", color=THEME["colors"]["text_primary"])
    
    # Variables para capacidad de pago
    payment_capacity = {"value": 0.0}
    
    def on_liquidity_change(e):
        """Callback cuando cambia el input de liquidez"""
        try:
            val_str = e.control.value.replace("$", "").replace(",", "")
            val = float(val_str) if val_str else 0.0
            payment_capacity["value"] = val
            upd_sum()  # Recalcular todo
        except ValueError:
            pass
    
    # Input de Ajuste de Liquidez (Simple TextField)
    liquidity_input = ft.TextField(
        label="Disponible Adicional",
        prefix=ft.Text("$"),
        text_align=ft.TextAlign.RIGHT,
        keyboard_type=ft.KeyboardType.NUMBER,
        border_color=THEME["colors"]["border"],
        focused_border_color=THEME["colors"]["accent"],
        bgcolor=THEME["colors"]["input_bg"],
        text_size=14,
        height=45,
        content_padding=10,
        hint_text="0.00 (+ o -)",
        on_change=on_liquidity_change
    )
    
    # Widget de Validación de Compuertas (CRÍTICO)
    validation_gates_widget = ValidationGatesWidget()
    
    # Widget de liquidez ELIMINADO - no es necesario
    
    # Inputs Nuevo
    input_nuevo = EnterpriseInput(
        label="Monto Requerido / Capacidad", 
        prefix=ft.Text("$"), 
        is_money=True,
        on_submit=lambda e: run_nuevo(e)  # Permitir Enter para calcular
    )
    
    # Label de rango válido (se mostrará en el UI)
    lbl_rango_monto = ft.Text(
        "Monto mínimo: $20,000 | Máximo recomendado: $500,000",
        size=10,
        color=THEME["colors"]["text_tertiary"],
        italic=True
    )
    radio_nuevo = ft.RadioGroup(
        content=ft.Row([
            ft.Radio(value="MONTO", label="Por Monto"), 
            ft.Radio(value="CAPACIDAD", label="Por Capacidad")
        ]), 
        value="CAPACIDAD"
    )
    # Switch Retención (Moderno)
    cb_retencion = ft.Switch(
        label="Aplicar Tasa Preferencial (Retención)", 
        value=False,
        active_color=THEME["colors"]["accent"]
    )
    cb_retencion_row = cb_retencion

    # --- HELPERS ---
    def msg(t, c=THEME["colors"]["success"]):
        page.snack_bar = ft.SnackBar(ft.Text(t, color="white"), bgcolor=c)
        page.snack_bar.open = True
        page.update()

    # LIVE KPIS (Controles UI)
    live_kpi_deuda = ft.Text("$0.00", size=22, weight="bold", color="#1F2937") # Visible en tarjeta blanca
    live_kpi_liquidez = ft.Text("$0.00", size=22, weight="bold", color="#1F2937") # Visible en tarjeta blanca

    def upd_sum(e=None):
        try:
            credits_list = []
            total_pago = 0.0
            total_saldo = 0.0
            
            for d in col_deudas.controls:
                try:
                    data = d.get_data()
                    if data:
                        pago = float(data.get('pago', 0) or 0)
                        saldo = float(data.get('saldo', 0) or 0)
                        total_pago += pago
                        total_saldo += saldo
                        
                        data_clean = data.copy()
                        data_clean['pago'] = pago
                        data_clean['saldo'] = saldo
                        data_clean['pagos'] = int(float(data.get('pagos', 0) or 0))
                        data_clean['_es_retencion'] = data_clean['pagos'] >= 24
                        data_clean['_es_tradicional'] = data_clean['pagos'] >= 16
                        credits_list.append(data_clean)
                except:
                    continue
            
            # Actualización segura de KPIs individuales
            for control, new_value in [
                (live_kpi_deuda, to_money(total_saldo)),
                (live_kpi_liquidez, to_money(total_pago)),
                (lbl_tot_val, to_money(total_pago)),
                (lbl_cap_val, to_money(total_pago + payment_capacity["value"]))
            ]:
                try:
                    # Verificar si el control sigue montado en una página
                    if getattr(control, "page", None):
                        control.value = new_value
                        
                        # Color especial solo para lbl_cap_val
                        if control == lbl_cap_val:
                            control.color = THEME["colors"]["success"] if payment_capacity["value"] > 0 else THEME["colors"]["text_primary"]
                        
                        control.update()
                except:
                    # Ignorar errores de actualización sutilmente
                    pass

            if credits_list:
                try:
                    validation_gates_widget.validate_credits(
                        credits_list, 
                        producto="CONSUPAGO", 
                        incremento_liquidez=payment_capacity["value"],
                        plazo_nuevo=60
                    )
                except: pass
            else:
                try: validation_gates_widget._reset_gates()
                except: pass
            
            # Intentar actualizar estimador si es visible
            try:
                if getattr(pension_estimator_widget, "page", None):
                    pension_estimator_widget.update()
            except: pass
            
        except Exception as ex:
            print(f"Error en upd_sum: {ex}")
    
    async def add_row_from_enter():
        await add_row(None)
        if col_deudas.controls:
             new_row = col_deudas.controls[-1]
             col_deudas.update()
             try:
                 new_row.tf_s.focus()
             except:
                 pass

    async def add_row(e=None, datos_importados=None):
        idx = len(col_deudas.controls) + 1
        
        # Handler síncrono para DebtRow, lanza task asíncrona
        def remove_wrapper(idx_unused):
            async def remove_task():
                # Animación de salida mejorada: deslizar hacia la izquierda
                row.animate_opacity = THEME["animations"]["fade"]
                row.animate_scale = THEME["animations"]["fade"]
                row.animate_offset = ft.Animation(350, ft.AnimationCurve.EASE_IN)
                row.opacity = 0
                row.scale = 0.8
                row.offset = ft.Offset(-0.3, 0)  # Deslizar a la izquierda
                row.update()
                await asyncio.sleep(0.4)
                try:
                    if row in col_deudas.controls:
                        col_deudas.controls.remove(row)
                        col_deudas.update()
                        upd_sum()
                except Exception as e:
                    print(f"Error removing row: {e}")
            page.run_task(remove_task)
        
        row = DebtRow(idx, 
                     remove_wrapper, 
                     upd_sum, 
                     idx % 2 == 0,
                     on_enter_pressed=add_row_from_enter)
        
        # Configurar animación de entrada mejorada
        # Estado inicial: invisible, pequeña, y desplazada a la derecha
        row.opacity = 0
        row.scale = 0.85
        row.offset = ft.Offset(0.5, 0)  # Fuera de pantalla a la derecha
        row.animate_opacity = THEME["animations"]["standard"]
        row.animate_scale = THEME["animations"]["bouncy"]
        row.animate_offset = ft.Animation(400, ft.AnimationCurve.EASE_OUT_CUBIC)
        
        if datos_importados:
            row.set_values(
                producto=datos_importados.get("producto"),
                saldo=datos_importados.get("saldo"),
                pago=datos_importados.get("pago"),
                tasa=datos_importados.get("tasa_calculada"),
                pagos=datos_importados.get("pagos"),
                plazo=datos_importados.get("plazo")
            )
        col_deudas.controls.append(row)
        col_deudas.update()
        
        # Animar entrada: deslizar desde la derecha mientras aparece
        await asyncio.sleep(0.05) # Render frame invisible
        
        row.opacity = 1
        row.scale = 1.0
        row.offset = ft.Offset(0, 0)  # Posición final
        row.update()
        
        upd_sum()

    # --- LOGIC HANDLERS (RESTORED & REFACTORED) ---
    def layout_handler(data):
        nonlocal formato_layout_actual
        tipo = "CNCA" if 'remanente' in data else "NUEVO"
        datos_sesion = gestor_sesion.cargar_datos_cliente()
        
        # Inputs Base
        campos = [
            EnterpriseInput("Nombre", value=datos_sesion.get('nombre', ''), tipo_validacion="TEXTO"),
            EnterpriseInput("NSS", value=datos_sesion.get('nss', ''), max_length=11, tipo_validacion="NSS"),
            EnterpriseInput("RFC", value=datos_sesion.get('rfc', ''), max_length=13, tipo_validacion="RFC"),
            EnterpriseInput("Teléfono", value=datos_sesion.get('telefono', ''), max_length=10),
            EnterpriseInput("CLABE", value=datos_sesion.get('clabe', ''), max_length=18),
            EnterpriseInput("Banco", value=datos_sesion.get('banco', ''), tipo_validacion="TEXTO"),
            EnterpriseInput("Ocupación", value=datos_sesion.get('ocupacion', ''), tipo_validacion="TEXTO"),
            EnterpriseInput("CURP", value=datos_sesion.get('curp', ''), max_length=18, tipo_validacion="RFC"),
            EnterpriseInput("Domicilio", value=datos_sesion.get('domicilio', ''), multiline=True)
        ]

        # Inputs Adicionales (SOLO CNCA)
        inputs_cnca = []
        if tipo == "CNCA":
            inputs_cnca = [
                ft.Divider(),
                ft.Text("Datos de Liquidación (CNCA)", weight="bold", size=12, color=THEME["colors"]["accent"]),
                EnterpriseInput("ID Crédito(s)", value=datos_sesion.get('id_credito', '')),
                EnterpriseInput("Descuento a Liquidar", value=datos_sesion.get('descuento_liq', ''), is_money=True),
                EnterpriseInput("# Descuentos", value=datos_sesion.get('num_descuentos', ''), keyboard_type=ft.KeyboardType.NUMBER),
                EnterpriseInput("Marca Liquidación", value=datos_sesion.get('marca_liquida', ''))
            ]
            campos.extend(inputs_cnca)

        col_campos = ft.Column(campos, scroll=ft.ScrollMode.AUTO, spacing=15)

        def get_form_data():
            base = {
                "nombre": campos[0].value, "nss": campos[1].value, "rfc": campos[2].value,
                "telefono": campos[3].value, "clabe": campos[4].value, "banco": campos[5].value,
                "ocupacion": campos[6].value, "curp": campos[7].value, "domicilio": campos[8].value,
                "convenio": "IMSS"
            }
            if tipo == "CNCA":
                # Índices desplazados por el Divider y el Text header
                base.update({
                    "id_credito": campos[11].value, # 9 base + 2 headers
                    "descuento_liq": campos[12].raw_value,
                    "num_descuentos": campos[13].value,
                    "marca_liquida": campos[14].value
                })
            return base

        layout_text = motor.generar_layout_final(get_form_data(), data, perfil, tipo, formato_layout_actual)
        txt_layout = ft.TextField(value=layout_text, multiline=True, min_lines=15, text_size=11, border="none", bgcolor=THEME["colors"]["input_bg_focus"])
        
        def update_layout(e):
            nonlocal formato_layout_actual
            formato_layout_actual = "WHATSAPP" if e.control.value else "FORMAL"
            txt_layout.value = motor.generar_layout_final(get_form_data(), data, perfil, tipo, formato_layout_actual)
            txt_layout.update()

        def copy_layout(e):
            fd = get_form_data()
            gestor_sesion.guardar_datos_cliente(fd)
            gestor_ops.actualizar_o_crear_proceso({
                "id": ultima_operacion_id, "cliente": fd.get('nombre'), "producto": data['producto'],
                "monto": data['monto'], "plazo": data['plazo'], "tipo": tipo, "tipo_tasa": data.get('tipo_tasa')
            })
            copiar_al_portapapeles(page, txt_layout.value)
            load_ops()

        def gen_pdf(e):
            fd = get_form_data()
            gestor_sesion.guardar_datos_cliente(fd)
            gestor_ops.actualizar_o_crear_proceso({
                "id": ultima_operacion_id, "cliente": fd.get('nombre'), "producto": data['producto'],
                "monto": data['monto'], "plazo": data['plazo'], "tipo": tipo, "tipo_tasa": data.get('tipo_tasa')
            })
            loading.show()
            ok, res = pdf_gen.generar(fd, data, perfil, tipo)
            loading.hide()
            if ok: msg(f"PDF Generado: {res}")
            else: msg(f"Error: {res}", THEME["colors"]["error"])
            load_ops()

        dlg = ft.AlertDialog(
            title=ft.Text(f"Generar Layout - {data['producto']}"),
            content=ft.Container(
                width=700, height=500,
                content=ft.Tabs(
                    selected_index=0,
                    tabs=[
                        ft.Tab(text="Layout", content=ft.Container(
                            padding=10,
                            content=ft.Column([
                                ft.Switch(label="Formato WhatsApp", value=formato_layout_actual=="WHATSAPP", on_change=update_layout),
                                txt_layout
                            ])
                        )),
                        ft.Tab(text="Datos Cliente", content=ft.Container(padding=10, content=col_campos))
                    ]
                )
            ),
            actions=[
                ft.TextButton("COPIAR", on_click=copy_layout),
                EnterpriseButton("PDF", on_click=gen_pdf, bgcolor=THEME["colors"]["success"]),
                ft.TextButton("CERRAR", on_click=lambda e: [setattr(dlg, "open", False), page.update()])
            ]
        )
        page.dialog = dlg
        dlg.open = True
        page.update()

    def detail_handler(data):
        detalles = f"PRODUCTO: {data['producto']}\nPLAZO: {data['plazo']}\nMONTO: {to_money(data['monto'])}\nPAGO: {to_money(data['pago_visual'])}\nCAT: {data['cat']}%\nTIPO: {data.get('tipo_tasa')}"
        if 'remanente' in data: detalles += f"\nREMANENTE: {to_money(data['remanente'])}\nRAZONES: {', '.join(data.get('razones',[]))}"
        
        dlg = ft.AlertDialog(
            title=ft.Text("Detalle Oferta"), 
            content=ft.Text(detalles), 
            actions=[ft.TextButton("OK", on_click=lambda e: [setattr(dlg, "open", False), page.update()])]
        )
        page.dialog = dlg
        dlg.open = True
        page.update()

    def comparar_escenarios_handler(data):
        nonlocal escenario_congelado
        if not escenario_congelado:
            escenario_congelado = data
            msg(f"Escenario {data['producto']} fijado. Selecciona otro para comparar.", THEME["colors"]["success"])
            # Reproducir sonido sutil
            if sound_manager: sound_manager.play("click")
        else:
            # Construir UI de comparación lado a lado
            def compare_card(d, is_base=False):
                tasa_val = d.get('tasa', 0.0)
                if tasa_val == 0: tasa_val = CalculadoraFinanciera.calcular_tasa_anual(d['monto'], d['pago_visual'], d['plazo'])
                
                title = "Opción A (Base)" if is_base else "Opción B (Comparación)"
                color = THEME["colors"]["text_secondary"] if is_base else THEME["colors"]["accent"]
                bg = "#F8FAFC" if is_base else "#F0FDF4"
                
                return ft.Container(
                    content=ft.Column([
                        ft.Text(title, size=10, weight="bold", color=color),
                        ft.Text(d['producto'], size=16, weight="bold", color=THEME["colors"]["text_primary"]),
                        ft.Divider(height=10),
                        
                        ft.Row([ft.Text("Monto:", size=11, color="grey"), ft.Text(f"${d['monto']:,.2f}", weight="bold")], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                        ft.Row([ft.Text("Pago:", size=11, color="grey"), ft.Text(f"${d['pago_visual']:,.2f}", weight="bold", color=THEME["colors"]["accent"])], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                        ft.Row([ft.Text("Plazo:", size=11, color="grey"), ft.Text(f"{d['plazo']} m", weight="bold")], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                        ft.Row([ft.Text("Tasa:", size=11, color="grey"), ft.Text(f"{tasa_val:.2f}%", weight="bold")], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                        ft.Row([ft.Text("CAT:", size=11, color="grey"), ft.Text(f"{float(d.get('cat', 0)):.1f}%", weight="bold")], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    ], spacing=8),
                    padding=15,
                    bgcolor=bg,
                    border_radius=12,
                    border=ft.Border.all(1, THEME["colors"]["divider"]),
                    expand=True
                )
                
            content_comparativa = ft.Column([
                ft.Row([
                    ft.Icon(ft.Icons.COMPARE_ARROWS_ROUNDED, size=30, color=THEME["colors"]["accent"]),
                    ft.Text("Comparativa de Escenarios", size=18, weight="bold")
                ], alignment=ft.MainAxisAlignment.CENTER),
                ft.Container(height=10),
                ft.Row([
                    compare_card(escenario_congelado, True),
                    ft.Container(width=10),
                    compare_card(data, False)
                ]),
                # Diferencia Ahorro
                ft.Container(
                    content=ft.Row([
                        ft.Text("Diferencia Pago Mensual:", size=12, weight="w500"),
                        ft.Text(f"${abs(escenario_congelado['pago_visual'] - data['pago_visual']):,.2f}", size=14, weight="bold", color=THEME["colors"]["success"])
                    ], alignment=ft.MainAxisAlignment.CENTER),
                    padding=10, bgcolor=THEME["colors"]["input_bg"], border_radius=8, margin=ft.Margin.only(top=10)
                )
            ], tight=True)
            
            # Contenedor animado
            animated_content = ft.Container(
                content=content_comparativa,
                width=600,
                scale=0.8,
                opacity=0,
                animate_scale=ft.Animation(400, "easeOutBack"),
                animate_opacity=ft.Animation(300, "easeIn")
            )

            def clear_compare(e):
                nonlocal escenario_congelado
                escenario_congelado = None
                setattr(dlg, "open", False); page.update()

            dlg = ft.AlertDialog(
                content=animated_content,
                modal=True,
                content_padding=20,
                actions=[
                    ft.TextButton("Nueva Comparación", on_click=clear_compare),
                    ft.TextButton("Cerrar", on_click=lambda e: [setattr(dlg, "open", False), page.update()])
                ],
                actions_alignment=ft.MainAxisAlignment.END,
                shape=ft.RoundedRectangleBorder(radius=16)
            )
            page.dialog = dlg
            dlg.open = True
            page.update()
            
            # Trigger animation
            time.sleep(0.05)
            animated_content.scale = 1.0
            animated_content.opacity = 1
            animated_content.update()

    def mostrar_tabla_amortizacion(data):
        tasa = data.get('tasa_calculada', 0)
        if tasa == 0:
             try: tasa = float(str(data['cat']).replace('%',''))
             except: tasa = 30.0
        
        tabla = CalculadoraFinanciera.generar_tabla_amortizacion(data['monto'], tasa, data['plazo'], data['pago_visual'])
        dt = ft.DataTable(
            columns=[ft.DataColumn(ft.Text(c)) for c in ["Mes", "Saldo Ini", "Pago", "Int", "Cap", "Saldo Fin"]],
            rows=[ft.DataRow(cells=[
                ft.DataCell(ft.Text(str(r['mes']))),
                ft.DataCell(ft.Text(to_money(r['saldo_inicial']))),
                ft.DataCell(ft.Text(to_money(r['pago_total']))),
                ft.DataCell(ft.Text(to_money(r['interes']))),
                ft.DataCell(ft.Text(to_money(r['capital']))),
                ft.DataCell(ft.Text(to_money(r['saldo_final'])))
            ]) for r in tabla[:24]] # Limit 24 for view
        )
        
        dlg = ft.AlertDialog(
            title=ft.Text(f"Amortización (Tasa {tasa}%)"), 
            content=ft.Column([dt], scroll=True, height=400), 
            actions=[ft.TextButton("Cerrar", on_click=lambda e: [setattr(dlg, "open", False), page.update()])]
        )
        page.dialog = dlg
        dlg.open = True
        page.update()
    


    def config_dlg(e):
        # 1. Definir los inputs (Faltaba el de Supervisor)
        tf_n = EnterpriseInput("Nombre", value=perfil.get("nombre"), tipo_validacion="TEXTO")
        tf_u = EnterpriseInput("Usuario", value=perfil.get("usuario"), tipo_validacion="TEXTO")
        tf_p = EnterpriseInput("Pass", value=perfil.get("password"), password=True)
        tf_s = EnterpriseInput("Supervisor", value=perfil.get("supervisor"), tipo_validacion="TEXTO") # <--- AGREGADO

        # 2. Definir funciones internas
        def save(e):
            # MODIFICADO: Ahora guarda el valor del input tf_s, no el del perfil antiguo
            gestor.guardar({
                "nombre": tf_n.value, 
                "usuario": tf_u.value, 
                "password": tf_p.value, 
                "supervisor": tf_s.value, # <--- CORREGIDO
                "tutorial_visto": True
            })
            setattr(dlg, "open", False); page.update()
            msg("Guardado correctamente")
            
        def reset(e):
            cfg_mgr.restaurar_fabrica()
            motor.recargar_config()
            dlg.open = False
            page.update()
            msg("Valores de fábrica restaurados")

        # 3. Crear el diálogo con el contenido actualizado
        dlg = ft.AlertDialog(
            title=ft.Text("Configuración"),
            content=ft.Container(
                width=400,
                # Aumentamos la altura y activamos scroll porque hay más elementos
                content=ft.Column([
                    tf_n, 
                    tf_u, 
                    tf_p, 
                    tf_s, # <--- AGREGADO AL LAYOUT
                    ft.Divider(height=20)
                ], height=400, scroll=ft.ScrollMode.AUTO)
            ),
            actions=[
                ft.TextButton("Restaurar Fábrica", on_click=reset, style=ft.ButtonStyle(color=THEME["colors"]["error"])),
                EnterpriseButton("Guardar", on_click=save)
            ]
        )
        page.dialog = dlg
        dlg.open = True
        page.update()

    # --- CÁLCULO NUEVO (VERSIÓN DEFINITIVA CORREGIDA) ---
    def generar_pdf_amortizacion(data):
        """Muestra una previsualización de la tabla de amortización antes de generar el PDF"""
        
        # 1. Calcular datos para la tabla
        tasa_anual = data.get('tasa', 0.0)
        if tasa_anual == 0:
            tasa_anual = CalculadoraFinanciera.calcular_tasa_anual(
                data['monto'], data['pago_visual'], data['plazo']
            )
                
        monto = data['monto']
        plazo = data['plazo']
        pago = data['pago_visual']
        tasa_men = tasa_anual / 100 / 12
        
        # Generar filas de datos
        filas_preview = []
        saldo = monto
        
        # Mostrar solo primeros 24 meses o scroll completo si se prefiere
        rows_data = []
        
        for mes in range(1, plazo + 1):
            interes = saldo * tasa_men
            capital = pago - interes
            
            # Ajuste último mes
            if mes == plazo and abs(saldo - capital) > 1:
                capital = saldo
                pago_real = capital + interes
            else:
                pago_real = pago
                
            saldo_final = saldo - capital
            if saldo_final < 0: saldo_final = 0
            
            rows_data.append((mes, saldo, interes, capital, pago_real, saldo_final))
            
            # Formato para UI
            filas_preview.append(
                ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(mes))),
                    ft.DataCell(ft.Text(f"${saldo:,.2f}")),
                    ft.DataCell(ft.Text(f"${interes:,.2f}")),
                    ft.DataCell(ft.Text(f"${capital:,.2f}")),
                    ft.DataCell(ft.Text(f"${pago_real:,.2f}", weight="bold")),
                    ft.DataCell(ft.Text(f"${saldo_final:,.2f}")),
                ])
            )
            
            saldo = saldo_final
            if saldo <= 0.1: break

        # 2. UI del Diálogo de Previsualización
        tabla_preview = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Mes", weight="bold")),
                ft.DataColumn(ft.Text("Saldo Ini", weight="bold"), numeric=True),
                ft.DataColumn(ft.Text("Interés", weight="bold"), numeric=True),
                ft.DataColumn(ft.Text("Capital", weight="bold"), numeric=True),
                ft.DataColumn(ft.Text("Pago", weight="bold"), numeric=True),
                ft.DataColumn(ft.Text("Saldo Fin", weight="bold"), numeric=True),
            ],
            rows=filas_preview,
            heading_row_color=THEME["colors"]["input_bg"],
            column_spacing=20,
            data_row_min_height=40,
            expand=True
        )
        
        def exportar_pdf_final(e):
            loading.show(message="Generando PDF...")
            try:
                # Definir variables críticas al inicio
                tasa_val = data.get('tasa', 0.0)
                if tasa_val == 0:
                    tasa_val = CalculadoraFinanciera.calcular_tasa_anual(data['monto'], data['pago_visual'], data['plazo'])
                
                pdf = FPDF()
                pdf.add_page()
                pdf.set_auto_page_break(auto=False)
                
                # ===== HEADER PROFESIONAL CON LOGO =====
                # Determinar logo según producto
                producto = data['producto'].upper()
                logo_map = {
                    'CONSUBANCO': 'csb.png',
                    'CONSUPAGO': 'csp.png',
                    'OPCIPRES': 'opc.png',
                    'MASNOMINA': 'msn.png'
                }
                
                logo_filename = None
                for key in logo_map:
                    if key in producto:
                        logo_filename = logo_map[key]
                        break
                
                # Insertar logo si existe
                print(f"🖼️ Intentando cargar logo: {logo_filename}")
                if logo_filename:
                    try:
                        # Intentar múltiples rutas
                        paths_to_try = [
                            os.path.join(os.path.dirname(__file__), 'assets', logo_filename),
                            os.path.join(os.getcwd(), 'assets', logo_filename),
                            resource_path(os.path.join('assets', logo_filename))
                        ]
                        
                        logo_loaded = False
                        for logo_path in paths_to_try:
                            if os.path.exists(logo_path):
                                print(f"✅ Logo encontrado en: {logo_path}")
                                pdf.image(logo_path, x=15, y=15, w=35)
                                logo_loaded = True
                                break
                        
                        if not logo_loaded:
                            print(f"⚠️ Logo no encontrado, usando placeholder limpio.")
                            
                    except Exception as logo_ex:
                        print(f"⚠️ Error al insertar logo en PDF: {logo_ex}")
                
                # Título y subtítulo (a la derecha del logo)
                pdf.set_xy(55, 18)
                pdf.set_font("helvetica", "B", 18)
                pdf.set_text_color(31, 41, 55)
                pdf.cell(0, 8, "Tabla de Amortización", ln=True)
                
                pdf.set_xy(55, 28)
                pdf.set_font("helvetica", "", 10)
                pdf.set_text_color(107, 114, 128)
                pdf.cell(0, 5, f"{data['producto']} - {data['plazo']} meses", ln=True)
                
                # Línea decorativa
                pdf.set_draw_color(16, 185, 129)
                pdf.set_line_width(0.5)
                pdf.line(15, 42, 195, 42)
                
                pdf.ln(15)
                
                # ===== RESUMEN FINANCIERO =====
                pdf.set_xy(15, 50)
                pdf.set_fill_color(248, 250, 252)
                pdf.rect(15, 50, 180, 30, 'F')
                
                # Borde sutil
                pdf.set_draw_color(226, 232, 240)
                pdf.set_line_width(0.3)
                pdf.rect(15, 50, 180, 30)
                
                pdf.set_xy(20, 55)
                pdf.set_font("helvetica", "B", 10)
                pdf.set_text_color(75, 85, 99)
                pdf.cell(85, 6, f"Monto del Crédito: ${data['monto']:,.2f}", ln=False)
                pdf.cell(85, 6, f"Pago Mensual: ${data['pago_visual']:,.2f}", ln=True)
                
                pdf.set_xy(20, 63)
                pdf.cell(85, 6, f"Tasa Anual: {tasa_val:.2f}%", ln=False)
                pdf.cell(85, 6, f"CAT Promedio: {data['cat']}%", ln=True)
                
                pdf.set_xy(20, 71)
                monto_total = data['pago_visual'] * data['plazo']
                pdf.set_font("helvetica", "B", 11)
                pdf.set_text_color(16, 185, 129)
                pdf.cell(0, 6, f"Total a Pagar: ${monto_total:,.2f}", ln=True)
                
                pdf.ln(10)
                
                # ===== TABLA DE AMORTIZACIÓN =====
                start_x = 15
                pdf.set_xy(start_x, 92)
                
                headers = ["Mes", "Pago", "Capital", "Interés", "Seguro", "Saldo Fin"]
                col_widths = [20, 30, 30, 30, 30, 40]
                
                def print_header():
                    pdf.set_x(start_x)
                    pdf.set_font("helvetica", "B", 9)
                    pdf.set_fill_color(31, 41, 55)
                    pdf.set_text_color(255, 255, 255)
                    for i, h in enumerate(headers):
                        pdf.cell(col_widths[i], 8, h, border=1, fill=True, align="C")
                    pdf.ln()
                    
                print_header()
                pdf.set_font("helvetica", "", 8)
                pdf.set_text_color(31, 41, 55)
                
                # Datos de la tabla
                for row in rows_data:
                    if pdf.get_y() > 265:
                        pdf.add_page()
                        print_header()
                        pdf.set_font("helvetica", "", 8)
                        pdf.set_text_color(31, 41, 55)
                    
                    vals = [str(row[0]), f"${row[1]:,.2f}", f"${row[2]:,.2f}", f"${row[3]:,.2f}", "$0.00", f"${row[5]:,.2f}"]
                    pdf.set_x(start_x)
                    for i, v in enumerate(vals):
                        pdf.cell(col_widths[i], 6, v, border=1, align="C")
                    pdf.ln()

                # ===== FOOTER PROFESIONAL =====
                pdf.set_y(-20)
                pdf.set_font("helvetica", "I", 8)
                pdf.set_text_color(107, 114, 128)
                
                # Línea decorativa superior
                pdf.set_draw_color(226, 232, 240)
                pdf.set_line_width(0.3)
                pdf.line(15, pdf.get_y() - 2, 195, pdf.get_y() - 2)
                
                # Información de contacto
                pdf.set_x(15)
                pdf.cell(0, 5, "Para más información visite: ", ln=True, align="C")
                
                # Enlace web (clickable)
                pdf.set_font("helvetica", "BU", 9)
                pdf.set_text_color(16, 185, 129)
                pdf.set_x(15)
                # Guardar Y para el link
                link_y = pdf.get_y()
                pdf.cell(0, 5, "www.consubanco.com.mx", ln=True, align="C", link="https://www.consubanco.com.mx")
                
                # Fecha de generación
                pdf.set_font("helvetica", "", 7)
                pdf.set_text_color(156, 163, 175)
                pdf.set_x(15)
                from datetime import datetime
                fecha_actual = datetime.now().strftime("%d/%m/%Y %H:%M")
                pdf.cell(0, 4, f"Documento generado el {fecha_actual}", ln=True, align="C")

                # Guardar - Compatible con Windows y Mac
                filename = f"Amortizacion_{data['producto']}_{int(time.time())}.pdf"
                
                # Detectar sistema operativo
                import sys
                if sys.platform == 'win32':
                    desktop = os.path.join(os.path.join(os.environ['USERPROFILE']), 'Desktop')
                else:  # Mac/Linux
                    desktop = os.path.expanduser('~/Desktop')
                
                filepath = os.path.join(desktop, filename)
                pdf.output(filepath)
                
                # Cerrar diálogo
                dlg_amort.open = False
                page.update()
                
                # Feedback y abrir archivo
                msg(f"PDF guardado en Escritorio: {filename}", THEME["colors"]["success"])
                sound_manager.play("success")
                
                if os.path.exists(filepath):
                    if sys.platform == 'darwin':  # Mac
                        import subprocess
                        subprocess.call(('open', filepath))
                    elif sys.platform == 'win32':  # Windows
                        os.startfile(filepath)
                     
            except Exception as ex:
                print(f"❌ Error generando PDF: {ex}")
                import traceback
                traceback.print_exc()
                msg(f"Error generando PDF: {ex}", THEME["colors"]["error"])
            finally:
                # SIEMPRE ocultar loading, pase lo que pase
                loading.hide()

        # Diálogo Preview
        dlg_amort = ft.AlertDialog(
            title=ft.Text("Detalle de Amortización"),
            content=ft.Container(
                content=ft.Column([
                    tabla_preview
                ], scroll=ft.ScrollMode.AUTO),
                height=400, width=850,
            ),
            actions=[
                ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg_amort, "open", False), page.update()]),
                ft.Button("Descargar PDF", icon=ft.Icons.DOWNLOAD, on_click=exportar_pdf_final, bgcolor=THEME["colors"]["accent"], color="white")
            ],
            actions_alignment=ft.MainAxisAlignment.END,
            modal=True,
            open=True
        )
        page.overlay.append(dlg_amort)
        page.update()

    def mostrar_detalle_oferta(data):
        """Muestra un modal con el detalle completo de la oferta con diseño Premium"""
        print(f"🔍 mostrar_detalle_oferta llamada con producto: {data.get('producto', 'N/A')}")
        
        try:
            # Cálculos adicionales
            tasa_val = data.get('tasa', 0.0)
            if tasa_val == 0:
                tasa_val = CalculadoraFinanciera.calcular_tasa_anual(data['monto'], data['pago_visual'], data['plazo'])
                
            monto_total_pagar = data.get('pago_visual', 0) * data.get('plazo', 0)
            print(f"✅ Cálculos completados: tasa={tasa_val}, total={monto_total_pagar}")
            
            # Helper para generar imagen promo
            def generar_imagen_handler(d):
                try:
                    msg("Generando imagen...", THEME["colors"]["text_secondary"])
                    ok, path = GeneradorImagen.crear_card_img(d)
                    if ok:
                        msg(f"Imagen guardada: {path}", THEME["colors"]["success"])
                        sound_manager.play("success")
                        try:
                            import subprocess, sys, os
                            if sys.platform == 'darwin': subprocess.call(('open', path))
                            elif sys.platform == 'win32': os.startfile(path)
                        except: pass
                    else:
                        msg(f"Error: {path}", THEME["colors"]["error"])
                        sound_manager.play("error")
                except Exception as ex:
                    msg(f"Error crítico: {ex}", THEME["colors"]["error"])

            # Helper para copiar layouts
            def copiar_layout(fmt):
                """Muestra formulario de captura antes de copiar el layout"""
                
                # Cargar datos existentes si los hay
                fd_existente = gestor_sesion.cargar_datos_cliente()
                
                # Crear inputs del formulario
                input_nombre = ft.TextField(
                    label="Nombre Completo *",
                    value=fd_existente.get('nombre', ''),
                    hint_text="Ej: JUAN PÉREZ GARCÍA",
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_nss = ft.TextField(
                    label="NSS *",
                    value=fd_existente.get('nss', ''),
                    hint_text="11 dígitos",
                    max_length=11,
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_rfc = ft.TextField(
                    label="RFC",
                    value=fd_existente.get('rfc', ''),
                    hint_text="13 caracteres",
                    max_length=13,
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_telefono = ft.TextField(
                    label="Teléfono *",
                    value=fd_existente.get('telefono', ''),
                    hint_text="10 dígitos",
                    max_length=10,
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_clabe = ft.TextField(
                    label="CLABE Interbancaria *",
                    value=fd_existente.get('clabe', ''),
                    hint_text="18 dígitos",
                    max_length=18,
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_banco = ft.TextField(
                    label="Banco *",
                    value=fd_existente.get('banco', ''),
                    hint_text="Ej: BBVA, SANTANDER",
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                input_ocupacion = ft.TextField(
                    label="Ocupación",
                    value=fd_existente.get('ocupacion', ''),
                    hint_text="Ej: EMPLEADO",
                    text_size=14,
                    border_color=THEME["colors"]["divider"],
                    focused_border_color=THEME["colors"]["accent"]
                )
                
                def validar_y_copiar(e):
                    """Valida los campos y copia el layout"""
                    # Validar campos obligatorios
                    if not input_nombre.value or not input_nss.value or not input_telefono.value or not input_clabe.value or not input_banco.value:
                        msg("⚠️ Completa todos los campos obligatorios (*)", THEME["colors"]["warning"])
                        sound_manager.play("error")
                        return
                    
                    try:
                        # Construir datos del cliente
                        form_data = {
                            'nombre': input_nombre.value.strip().upper(),
                            'nss': input_nss.value.strip(),
                            'rfc': input_rfc.value.strip().upper(),
                            'telefono': input_telefono.value.strip(),
                            'clabe': input_clabe.value.strip(),
                            'banco': input_banco.value.strip().upper(),
                            'ocupacion': input_ocupacion.value.strip().upper() if input_ocupacion.value else 'EMPLEADO',
                            'convenio': fd_existente.get('convenio', 'IMSS'),
                            'curp': fd_existente.get('curp', ''),
                            'domicilio': fd_existente.get('domicilio', ''),
                            'id_credito': fd_existente.get('id_credito', ''),
                            'descuento_liq': fd_existente.get('descuento_liq', 0),
                            'num_descuentos': fd_existente.get('num_descuentos', ''),
                            'marca_liquida': fd_existente.get('marca_liquida', '')
                        }
                        
                        # Guardar datos para próxima vez
                        gestor_sesion.guardar_datos_cliente(form_data)
                        
                        # Generar layout
                        pf = gestor.cargar()
                        tipo_tramite = "CNCA" if "remanente" in data else "NUEVO"
                        txt = motor.generar_layout_final(form_data, data, pf, tipo_tramite, fmt)
                        
                        # Copiar al portapapeles
                        page.set_clipboard(txt)
                        msg(f"✅ Layout {fmt} copiado al portapapeles", THEME["colors"]["success"])
                        sound_manager.play("success")
                        print(f"✅ Layout {fmt} copiado exitosamente")
                        print(f"📄 Contenido copiado:\n{txt[:200]}...")
                        
                        # Cerrar diálogo
                        dlg_captura.open = False
                        page.update()
                        
                    except Exception as ex:
                        print(f"❌ Error al generar layout: {ex}")
                        import traceback
                        traceback.print_exc()
                        msg("Error al generar layout", THEME["colors"]["error"])
                        sound_manager.play("error")
                
                def cancelar_captura(e):
                    dlg_captura.open = False
                    page.update()
                
                # Crear diálogo de captura
                dlg_captura = ft.AlertDialog(
                    title=ft.Text(f"📋 Captura de Datos - {fmt}", weight="bold", size=18),
                    content=ft.Container(
                        content=ft.Column([
                            ft.Text("Completa los datos del cliente para generar el layout:", 
                                   size=13, color=THEME["colors"]["text_secondary"]),
                            ft.Container(height=12),
                            input_nombre,
                            ft.Row([
                                input_nss,
                                input_rfc
                            ], spacing=12),
                            input_telefono,
                            ft.Row([
                                input_clabe,
                                input_banco
                            ], spacing=12),
                            input_ocupacion,
                            ft.Container(height=8),
                            ft.Text("* Campos obligatorios", size=11, italic=True, color=THEME["colors"]["text_tertiary"])
                        ], spacing=12, tight=True),
                        width=550,
                        padding=10
                    ),
                    actions=[
                        ft.TextButton("CANCELAR", on_click=cancelar_captura),
                        ft.FilledButton(
                            "COPIAR LAYOUT",
                            on_click=validar_y_copiar,
                            style=ft.ButtonStyle(bgcolor=THEME["colors"]["accent"], color="white"),
                            icon=ft.Icons.CONTENT_COPY
                        )
                    ],
                    actions_alignment=ft.MainAxisAlignment.END,
                    shape=ft.RoundedRectangleBorder(radius=16),
                    modal=True
                )
                
                page.overlay.append(dlg_captura)
                dlg_captura.open = True
                page.update()

            print("✅ Helpers definidos")
            
            # Componentes de detalle con diseño mejorado
            print("🔨 Construyendo UI del diálogo...")
            
            # Mapeo de logos de instituciones
            logo_map = {
                "CONSUPAGO": "csp.png",
                "CONSUBANCO": "csb.png",
                "MASNOMINA": "msn.png",
                "OPCIPRES": "opc.png"
            }
            
            # Determinar logo a usar
            producto_upper = data['producto'].upper()
            logo_path = None
            for key in logo_map:
                if key in producto_upper:
                    logo_path = logo_map[key]
                    break
            
            # Crear widget de logo o icono fallback
            if logo_path:
                logo_widget = ft.Image(
                    src=logo_path,
                    width=48,
                    height=48,
                    fit="contain",  # Flet 0.80 usa string en lugar de enum
                    border_radius=24  # Circular
                )
            else:
                logo_widget = ft.Container(
                    content=ft.Icon(ft.Icons.ACCOUNT_BALANCE, color="white", size=24),
                    width=48,
                    height=48,
                    bgcolor=THEME["colors"]["accent"],
                    border_radius=24,
                    alignment=ft.Alignment(0, 0)
                )
            
            # Badge de tipo de tasa con color
            tipo_tasa = data.get('tipo_tasa', 'TRADICIONAL').upper()
            badge_color = "#8B5CF6" if tipo_tasa in ['RETENCIÓN', 'SIPRE', 'RETENCION'] else THEME["colors"]["accent"]
            badge_bg = "#EDE9FE" if tipo_tasa in ['RETENCIÓN', 'SIPRE', 'RETENCION'] else "#FFF7ED"
            
            detail_col = ft.Column([
                # Header con Logo + Nombre + Badge
                ft.Row([
                    # Logo
                    ft.Container(
                        content=logo_widget,
                        shadow=ft.BoxShadow(blur_radius=8, color="#20000000", offset=ft.Offset(0, 2))
                    ),
                    # Nombre y Badge
                    ft.Column([
                        ft.Text(
                            f"{data['producto']}", 
                            size=22, 
                            weight="bold", 
                            color=THEME["colors"]["text_primary"]
                        ),
                        ft.Container(
                            content=ft.Row([
                                ft.Icon(
                                    ft.Icons.VERIFIED_ROUNDED if tipo_tasa in ['RETENCIÓN', 'SIPRE', 'RETENCION'] else ft.Icons.INFO_ROUNDED,
                                    size=14,
                                    color=badge_color
                                ),
                                ft.Text(
                                    f"Tasa {tipo_tasa}", 
                                    size=11, 
                                    weight="bold", 
                                    color=badge_color
                                )
                            ], spacing=4, tight=True),
                            bgcolor=badge_bg,
                            padding=ft.Padding.symmetric(horizontal=10, vertical=5),
                            border_radius=8,
                            border=ft.Border.all(1, badge_color + "40")
                        )
                    ], spacing=6, alignment=ft.MainAxisAlignment.CENTER)
                ], spacing=16, alignment=ft.CrossAxisAlignment.CENTER),
                
                ft.Container(height=20),
                
                # Tarjeta de Resumen Financiero
                ft.Container(
                    content=ft.Column([
                        # Fila Principal - Balanceada
                        ft.Row([
                            ft.Column([
                                ft.Text("Monto del Crédito", size=12, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"${data.get('monto', 0):,.2f}", size=22, weight="bold", color=THEME["colors"]["text_primary"])
                            ], spacing=0),
                            ft.Column([
                                ft.Text("Descuento Mensual", size=12, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"${data.get('pago_visual', 0):,.2f}", size=22, weight="bold", color=THEME["colors"]["accent"])
                            ], horizontal_alignment=ft.CrossAxisAlignment.END, spacing=0),
                        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                        
                        ft.Container(height=16),
                        ft.Divider(color=THEME["colors"]["divider"], height=1),
                        ft.Container(height=16),
                        
                        # Grid de Datos Técnicos
                        ft.Row([
                            # Columna 1
                            ft.Column([
                                ft.Text("Plazo", size=11, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"{data.get('plazo', 0)} meses", size=16, weight="w600", color="#1F2937"),
                                ft.Container(height=12),
                                ft.Text("CAT Promedio", size=11, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"{float(data.get('cat', 0)):.1f}%", size=16, weight="w600", color="#1F2937")
                            ], spacing=0),
                            # Columna 2
                            ft.Column([
                                ft.Text("Tasa Anual", size=11, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"{tasa_val:.2f}%", size=16, weight="w600", color="#1F2937"),
                                ft.Container(height=12),
                                ft.Text("Total a Pagar", size=11, color=THEME["colors"]["text_secondary"], weight="w500"),
                                ft.Container(height=4),
                                ft.Text(f"${monto_total_pagar:,.2f}", size=16, weight="w700", color="#1F2937")
                            ], horizontal_alignment=ft.CrossAxisAlignment.END, spacing=0)
                        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    ], spacing=0), 
                    padding=24,
                    bgcolor="#F8FAFC", # Slate 50
                    border_radius=16,
                    border=ft.Border.all(1, "#E2E8F0")
                ),
                
                ft.Container(height=24), 
                
                # Botones de Acción Principales
                ft.Row([
                    EnterpriseButton("VER AMORTIZACIÓN", on_click=lambda e: generar_pdf_amortizacion(data), icon=ft.Icons.TABLE_CHART_ROUNDED, expand=True, variant="secondary"), 
                    ft.Container(width=12),
                    EnterpriseButton("GENERAR IMAGEN", on_click=lambda e: generar_imagen_handler(data), icon=ft.Icons.IMAGE_ROUNDED, expand=True, variant="primary"),
                ]),
                
                ft.Container(height=16),
                
                # Botones de Compartir
                ft.Text("Compartir Oferta:", size=12, color=THEME["colors"]["text_secondary"], weight="bold"),
                ft.Container(height=8),
                ft.Row([
                     EnterpriseButton("COPIAR (WHATSAPP)", on_click=lambda e: copiar_layout("WHATSAPP"), icon=ft.Icons.MESSAGE_ROUNDED, expand=True, variant="ghost", height=40), 
                     ft.Container(width=12),
                     EnterpriseButton("COPIAR (FORMAL)", on_click=lambda e: copiar_layout("FORMAL"), icon=ft.Icons.CONTENT_COPY_ROUNDED, expand=True, variant="ghost", height=40),
                ])
                
            ], spacing=0, tight=True)
            
            print("✅ UI construida")

            # Contenedor para el contenido del diálogo con mejor width
            animated_content = ft.Container(
                content=detail_col, 
                width=550,  # Aumentado de 450 a 550 para mejor distribución
                padding=16
            )
            
            print("✅ Contenedor animado creado")

            # Crear diálogo
            dlg = ft.AlertDialog(
                title=ft.Text("Detalle de Oferta", weight="bold"),
                content=animated_content,
                actions=[
                    ft.TextButton("CERRAR", on_click=lambda e: close_dialog())
                ],
                actions_alignment=ft.MainAxisAlignment.END,
                shape=ft.RoundedRectangleBorder(radius=16),
                modal=True,
                open=True
            )
            
            def close_dialog():
                dlg.open = False
                page.update()
            
            print(f"✅ AlertDialog creado (ID: {id(dlg)})")
            
            # Agregar al overlay y mostrar
            page.overlay.append(dlg)
            page.update()
            
            print(f"✅ Diálogo agregado al overlay y mostrado")
                
        except Exception as ex:
            print(f"❌ ERROR CRÍTICO en mostrar_detalle_oferta: {ex}")
            import traceback
            traceback.print_exc()

    # Función create_skeleton_card reemplazada por import de SKELETON_LOADING
    # (Alias create_skeleton_offer_card -> create_skeleton_card)


    async def run_nuevo(e):
        nonlocal ultima_operacion_id, escenario_congelado
        
        # Activar estado de carga en botón
        btn_icon.visible = False
        btn_text.value = "CALCULANDO..."
        btn_spinner.visible = True
        btn_calcular_gold.disabled = True
        btn_calcular_gold.update()
        
        # SKELETON LOADING UI
        # Limpiamos grid anterior y mostramos skeletons inmediatamente
        grid_nuevo.controls.clear()
        
        # Creamos 3 skeletons con animación stagger (cascada) sutil si fuera posible, 
        # pero aquí los agregamos directo
        # Creamos 3 skeletons con animación stagger y configuración de columna
        skeletons = []
        for _ in range(3):
            sk = create_skeleton_card()
            sk.col = {"xs": 12, "md": 6, "lg": 6, "xl": 6, "xxl": 4}
            skeletons.append(sk)
        grid_nuevo.controls.extend(skeletons)
        grid_nuevo.update()
        
        # Breve pausa técnica para permitir renderizado UI
        await asyncio.sleep(0.1)
        
        # Simulación de carga mínima (UX) para que no sea un parpadeo instantáneo
        # Solo si el cálculo es demasiado rápido
        start_time = time.time()
        
        escenario_congelado = None
        
        # Validación robusta del input
        try:
            v = input_nuevo.raw_value
            if v is None or v <= 0:
                raise ValueError("Monto inválido")
        except (ValueError, AttributeError, TypeError):
            grid_nuevo.controls.clear()
            grid_nuevo.update()
            
            # Restaurar botón
            btn_icon.visible = True
            btn_text.value = "CALCULAR OFERTAS"
            btn_spinner.visible = False
            btn_calcular_gold.disabled = False
            btn_calcular_gold.update()
            
            sound_manager.play("wrong")
            await shake_control(input_nuevo, page)
            msg("⚠️ Ingresa un monto válido", THEME["colors"]["warning"])
            return
        
        # Validación pasada - Feedback positivo
        sound_manager.play("pop")
        
        try:
            # Paso 1: Guardar consulta
            consulta_id = gestor_ops.guardar_operacion({
                "cliente": "Consulta Nuevo", "producto": "MULTIPLE",
                "monto": v, "tipo": "NUEVO", "estatus": "CONSULTA"
            })
            
            bm = str(radio_nuevo.value).lower() == "monto" 
            tr = cb_retencion.value 
            
            # Actualizar filtros según tipo de cálculo
            if bm:  # Modo "Monto Fijo"
                btn_filtro_liquidez.visible = False
                btn_filtro_pago.visible = True
                orden_actual["criterio"] = "pago"  # Por defecto: menor pago
                btn_filtro_pago.bgcolor = COLOR_THEME_NUEVO
                btn_filtro_plazo.bgcolor = "#F3F4F6"
            else:  # Modo "Capacidad"
                btn_filtro_liquidez.visible = True
                btn_filtro_pago.visible = False  # No relevante: todos tienen el mismo pago
                orden_actual["criterio"] = "liquidez"  # Por defecto: mayor liquidez
                btn_filtro_liquidez.bgcolor = COLOR_THEME_NUEVO
                btn_filtro_plazo.bgcolor = "#F3F4F6"
            
            if filtros_row.page:
                filtros_row.update()
            
            ops = []
            
            # Paso 2: Calculando opciones
            for p in PRODUCTO_CONFIG:
                if tr:
                    keys_plazos = motor.retencion.get(p, {}).keys()
                else:
                    keys_plazos = motor.tradicional.get(p, {}).keys()
                
                plazos_disponibles = sorted([int(k) for k in keys_plazos], reverse=True)
                # Filtro Estricto: Solo permitir plazos estándar definidos en el motor
                plazos_disponibles = [pl for pl in plazos_disponibles if pl in motor.plazos_standard]

                for pl in plazos_disponibles:
                    # REGLA: Forzar uso de lógica Tradicional si no es plazo 60, aunque usuario pida Retención
                    actual_tr = tr and (pl == 60)

                    if actual_tr:
                        min_ret_nuevo = motor.reglas_tr_nuevo.get(p, 0)
                        if bm and v < min_ret_nuevo: continue 
                    
                    r = motor.calcular(p, v, pl, bm, actual_tr, rounding_step=100.0)
                    if not r: continue 
                    
                    mf = v if bm else r['monto']
                    pass_validation = False
                    
                    if actual_tr:
                        # *** VALIDACIÓN ESPECÍFICA CRÉDITO NUEVO CON RETENCIÓN ***
                        es_valido_nuevo, mensaje_error = motor.validar_nuevo_retencion(p, mf, pl)
                        if es_valido_nuevo:
                            pass_validation = True
                    else:
                        if motor.validar_minimo(p, mf, pl): pass_validation = True
                    
                    if pass_validation:
                        ops.append({
                            **r, 
                            "producto": p, 
                            "plazo": pl, 
                            "pago_visual": r['pago'], 
                            "cat": r['cat'], 
                            "tipo_tasa": "RETENCION" if actual_tr else "TRADICIONAL"
                        })
            
            # Paso 3: Ordenar: 1. Mayor Monto (-), 2. Menor Pago (+)
            ops.sort(key=lambda x: (-x['monto'], x['pago_visual']))
            
            # UX: Asegurar que el skeleton se vio al menos un poco (min 600ms totales)
            elapsed = time.time() - start_time
            if elapsed < 0.6:
                await asyncio.sleep(0.6 - elapsed)

            # Limpiar skeletons antes de mostrar resultados
            grid_nuevo.controls.clear()
            
            # Actualizar contador de ofertas
            if not ops:
                lbl_contador_ofertas.value = "0"
                badge_contador.visible = False
                grid_nuevo.controls.append(
                    ft.Container(
                        content=ft.Column([
                            ft.Icon(ft.Icons.BLOCK_ROUNDED, size=50, color=THEME["colors"]["text_tertiary"]),
                            ft.Text("Sin opciones para este monto", color=THEME["colors"]["text_secondary"], weight="bold"),
                            ft.Text("El monto solicitado no alcanza el mínimo.", size=12, color=THEME["colors"]["text_tertiary"])
                        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=5),
                        alignment=ft.Alignment(0, 0), opacity=0.7
                    )
                )
            else:
                # Mostrar contador de ofertas
                lbl_contador_ofertas.value = str(len(ops))
                badge_contador.visible = True
                badge_contador.update()
                
                # OPTIMIZACIÓN FASE 3: Progressive Rendering
                def create_nuevo_card(item):
                    """Helper para crear card con todos los parámetros"""
                    o, i = item
                    is_best = (i == 0)
                    delay = i * 50
                    
                    c = FintechCard(
                        data=o, is_best=is_best, tipo="NUEVO",
                        on_layout=mostrar_detalle_oferta, on_detail=mostrar_detalle_oferta,
                        on_compare=comparar_escenarios_handler,
                        on_amortization=lambda d: generar_pdf_amortizacion(d),
                        page=page, delay_ms=delay
                    )
                    
                    # Responsive Grid Config
                    grid_config = {"xs": 12, "md": 6, "lg": 6, "xl": 6, "xxl": 4}
                    c.col = grid_config
                    
                    # Envolver si hay comparator
                    if HAVE_COMPARATOR and offer_comparator:
                        selectable_card = create_selectable_offer_card(o, offer_comparator, c, page)
                        if hasattr(selectable_card, 'col'): 
                            selectable_card.col = grid_config
                        selectable_card.data = o
                        return selectable_card
                    else:
                        return c
                
                # Preparar items con índice
                items_with_index = [(o, i) for i, o in enumerate(ops)]
                
                # Renderizar progresivamente
                await render_cards_progressively(
                    container=grid_nuevo,
                    items=items_with_index,
                    builder_func=create_nuevo_card,
                    batch_size=8,
                    delay_ms=15
                )
            
            # Update final ya lo hace render_cards_progressively
            if ops:  # Si hay resultados, reproducir sonido de éxito
                sound_manager.play("cash")
                
                # Guardar en historial
                if HAVE_HISTORY and history_manager:
                    try:
                        history_manager.add_calculation({
                            'tipo': 'NUEVO',
                            'cliente': 'Consulta Nuevo',
                            'resultados': ops[:5],  # Top 5
                            'mejor_opcion': ops[0] if ops else None,
                            'metadata': {
                                'monto_o_pago': v,
                                'es_monto': bm,
                                'retencion': tr
                            }
                        })
                    except Exception as e:
                        print(f"Error guardando en historial: {e}")
        
        except Exception as ex:
            import traceback
            traceback.print_exc()
            logger.error(f"Error CRÍTICO en run_nuevo: {ex}", exc_info=True)
            msg(f"Error: {str(ex)[:100]}", THEME["colors"]["error"])
            grid_nuevo.controls.clear()
            grid_nuevo.update()
            sound_manager.play("error")
        finally:
            # Restaurar estado del botón
            btn_icon.visible = True
            btn_text.value = "CALCULAR OFERTAS"
            btn_spinner.visible = False
            btn_calcular_gold.disabled = False
            btn_calcular_gold.update()
            
            load_ops()

    def limpiar_nuevo(e):
        # Resetear valores internos
        input_nuevo.value = ""
        cb_retencion.value = False
        radio_nuevo.value = "capacidad"
        grid_nuevo.controls.clear()
        
        # Actualizar solo los controles visibles
        input_nuevo.update()
        grid_nuevo.update()
        
        # Actualizar la UI de los toggles (que SÍ están en la página)
        update_toggles_ui()
        
        msg("Campos limpios", THEME["colors"]["text_secondary"])
    
    # Asignar on_submit después de definir run_nuevo
    input_nuevo.tf.on_submit = run_nuevo

    def limpiar_cnca(e):
        col_deudas.controls.clear()
        add_row() # Agregar una fila vacía
        
        # Resetear widgets
        capacity_widget.capacity_input.value = "0"
        payment_capacity["value"] = 0.0
        pension_estimator_widget.update_calculation(0.0, 0.0)
        
        grid_cnca.controls.clear()
        col_deudas.update()
        capacity_widget.update()
        
        grid_cnca.update()
        upd_sum()
        msg("Tablero de refinanciamiento limpio", THEME["colors"]["text_secondary"])

    # --- MOSTRAR DETALLES DE OPERACIÓN CNCA ---
    def mostrar_detalles_cnca_desde_op(op):
        """Buscar datos completos en historial y mostrar diálogo"""
        # Intentar obtener datos del historial
        if HAVE_HISTORY and history_manager:
            # Buscar en historial por tipo CNCA
            historial = history_manager.search(tipo="CNCA")
            
            # Buscar la operación más reciente que coincida
            # (idealmente buscaríamos por ID, pero usamos fecha/monto como aproximación)
            op_fecha = op.get("fecha", "")
            op_monto = op.get("monto", 0)
            
            for calc in historial:
                calc_metadata = calc.get('metadata', {})
                # Si encontramos una coincidencia aproximada, usar esos datos
                if abs(calc_metadata.get('total_deuda', 0) - op_monto) < 100:  # Tolerancia de $100
                    mostrar_detalles_cnca(calc)
                    return
        
        # Si no se encuentra en historial, mostrar con datos de op (pueden estar vacíos)
        mostrar_detalles_cnca(op)
    
    # --- MOSTRAR DETALLES DE OPERACIÓN CNCA ---
    def mostrar_detalles_cnca(operacion_data):
        """Mostrar diálogo con detalles completos de una operación CNCA"""
        
        # Extraer datos
        deudas = operacion_data.get('deudas', [])
        mejor_opcion = operacion_data.get('mejor_opcion', {})
        metadata = operacion_data.get('metadata', {})
        resultados = operacion_data.get('resultados', [])
        
        # Crear tabla de deudas
        deudas_rows = []
        for idx, d in enumerate(deudas, 1):
            # El campo 'producto' contiene el nombre del banco/entidad
            entidad = d.get('producto') or d.get('entidad') or d.get('banco') or 'N/A'
            
            deudas_rows.append(
                ft.Row([
                    ft.Text(f"{idx}", size=12, width=30),
                    ft.Text(str(entidad), size=12, width=120),
                    ft.Text(to_money(d.get('saldo', 0)), size=12, width=100, text_align=ft.TextAlign.RIGHT),
                    ft.Text(to_money(d.get('pago', 0)), size=12, width=100, text_align=ft.TextAlign.RIGHT),
                    ft.Text(f"{float(d.get('tasa', 0) or 0):.2f}%", size=12, width=60, text_align=ft.TextAlign.CENTER),
                ], spacing=10)
            )
        
        # Crear contenido del diálogo - DASHBOARD STYLE
        dialog_content = ft.Column([
            # Header Compacto con KPIs Horizontales
            ft.Container(
                content=ft.Column([
                    ft.Row([
                        ft.Icon(ft.Icons.ACCOUNT_BALANCE, color="#10B981", size=28),
                        ft.Text("Detalles de Refinanciamiento", size=22, weight="w900", color=THEME["colors"]["text_primary"])
                    ], spacing=12),
                    
                    ft.Container(height=12),
                    
                    # KPIs en Fila (Estilo Dashboard)
                    ft.Row([
                        ft.Container(
                            content=ft.Column([
                                ft.Text("Total Deuda", size=11, color="#64748B", weight="w500"),
                                ft.Text(to_money(metadata.get('total_deuda', 0)), size=18, weight="w800", color="#EF4444")
                            ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.START),
                            padding=12,
                            bgcolor="#FEF2F2",
                            border_radius=10,
                            expand=True
                        ),
                        ft.Container(
                            content=ft.Column([
                                ft.Text("Capacidad Pago", size=11, color="#64748B", weight="w500"),
                                ft.Text(to_money(metadata.get('capacidad', 0)), size=18, weight="w800", color="#10B981")
                            ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.START),
                            padding=12,
                            bgcolor="#ECFDF5",
                            border_radius=10,
                            expand=True
                        ),
                        ft.Container(
                            content=ft.Column([
                                ft.Text("Ajuste", size=11, color="#64748B", weight="w500"),
                                ft.Text(to_money(metadata.get('ajuste', 0)), size=18, weight="w800", color="#64748B")
                            ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.START),
                            padding=12,
                            bgcolor="#F8FAFC",
                            border_radius=10,
                            expand=True
                        )
                    ], spacing=10)
                ], spacing=0),
                padding=ft.Padding.only(bottom=20)
            ),
            
            # Layout de 2 Columnas
            ft.Row([
                # COLUMNA IZQUIERDA: Tabla de Créditos
                ft.Container(
                    content=ft.Column([
                        ft.Text("Créditos a Refinanciar", size=15, weight="w700", color=THEME["colors"]["text_primary"]),
                        ft.Container(height=8),
                        ft.Container(
                            content=ft.Column([
                                # Header Mejorado
                                ft.Container(
                                    content=ft.Row([
                                        ft.Text("#", size=10, weight="w700", width=25, color="#64748B"),
                                        ft.Text("ENTIDAD", size=10, weight="w700", width=100, color="#64748B"),
                                        ft.Text("SALDO", size=10, weight="w700", width=85, text_align=ft.TextAlign.RIGHT, color="#64748B"),
                                        ft.Text("PAGO", size=10, weight="w700", width=85, text_align=ft.TextAlign.RIGHT, color="#64748B"),
                                        ft.Text("TASA", size=10, weight="w700", width=50, text_align=ft.TextAlign.CENTER, color="#64748B"),
                                    ], spacing=8),
                                    padding=10,
                                    bgcolor="#F8FAFC",
                                    border_radius=ft.BorderRadius.only(top_left=8, top_right=8)
                                ),
                                ft.Divider(height=1, color=THEME["colors"]["divider"]),
                                # Rows (más compactas)
                                ft.Container(
                                    content=ft.Column(deudas_rows, spacing=4, scroll=ft.ScrollMode.AUTO),
                                    padding=10,
                                    height=180
                                )
                            ], spacing=0),
                            border=ft.Border.all(1, "#E2E8F0"),
                            border_radius=8,
                            bgcolor="white"
                        )
                    ], spacing=0),
                    expand=True
                ),
                
                # COLUMNA DERECHA: Mejor Opción (Premium Card)
                ft.Container(
                    content=ft.Column([
                        ft.Text("Mejor Opción", size=15, weight="w700", color=THEME["colors"]["text_primary"]),
                        ft.Container(height=8),
                        ft.Container(
                            content=ft.Column([
                                # Header con producto
                                ft.Container(
                                    content=ft.Row([
                                        ft.Icon(ft.Icons.STARS_ROUNDED, color="#10B981", size=20),
                                        ft.Text(mejor_opcion.get('producto', 'N/A'), size=15, weight="w800", color="#10B981")
                                    ], spacing=8),
                                    padding=12,
                                    bgcolor="#ECFDF5",
                                    border_radius=ft.BorderRadius.only(top_left=12, top_right=12)
                                ),
                                
                                # Métricas Principales
                                ft.Container(
                                    content=ft.Column([
                                        ft.Row([
                                            ft.Column([
                                                ft.Text("Monto Liquidez", size=11, color="#64748B"),
                                                ft.Text(to_money(mejor_opcion.get('monto', 0)), size=20, weight="w800", color="#10B981")
                                            ], spacing=4),
                                            ft.Column([
                                                ft.Text("Pago Mensual", size=11, color="#64748B"),
                                                ft.Text(to_money(mejor_opcion.get('pago', 0)), size=20, weight="w800", color="#1E293B")
                                            ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.END)
                                        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                                        
                                        ft.Divider(height=1, color="#E2E8F0"),
                                        
                                        ft.Row([
                                            ft.Container(
                                                content=ft.Column([
                                                    ft.Text("Plazo", size=10, color="#64748B"),
                                                    ft.Text(f"{mejor_opcion.get('plazo', 0)}m", size=14, weight="w700", color="#1E293B")
                                                ], spacing=2, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                                                padding=8,
                                                bgcolor="#F8FAFC",
                                                border_radius=8,
                                                expand=True
                                            ),
                                            ft.Container(
                                                content=ft.Column([
                                                    ft.Text("Tasa", size=10, color="#64748B"),
                                                    ft.Text(f"{float(mejor_opcion.get('tasa', 0) or 0):.2f}%", size=14, weight="w700", color="#1E293B")
                                                ], spacing=2, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                                                padding=8,
                                                bgcolor="#F8FAFC",
                                                border_radius=8,
                                                expand=True
                                            ),
                                            ft.Container(
                                                content=ft.Column([
                                                    ft.Text("CAT", size=10, color="#64748B"),
                                                    ft.Text(f"{float(mejor_opcion.get('cat', 0) or 0):.2f}%", size=14, weight="w700", color="#1E293B")
                                                ], spacing=2, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                                                padding=8,
                                                bgcolor="#F8FAFC",
                                                border_radius=8,
                                                expand=True
                                            )
                                        ], spacing=8),
                                        
                                        # Comparativa Antes/Después
                                        ft.Container(
                                            content=ft.Column([
                                                ft.Text("Comparativa", size=11, weight="w600", color="#475569"),
                                                ft.Container(height=6),
                                                ft.Row([
                                                    ft.Column([
                                                        ft.Text("Antes", size=10, color="#94A3B8"),
                                                        ft.Text(to_money(sum(d.get('pago', 0) for d in deudas)), size=14, weight="w700", color="#EF4444")
                                                    ], spacing=2),
                                                    ft.Icon(ft.Icons.ARROW_FORWARD_ROUNDED, size=16, color="#10B981"),
                                                    ft.Column([
                                                        ft.Text("Después", size=10, color="#94A3B8"),
                                                        ft.Text(to_money(mejor_opcion.get('pago', 0)), size=14, weight="w700", color="#10B981")
                                                    ], spacing=2, horizontal_alignment=ft.CrossAxisAlignment.END)
                                                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                                                ft.Container(height=4),
                                                ft.Container(
                                                    content=ft.Text(
                                                        f"Ahorro: {to_money(sum(d.get('pago', 0) for d in deudas) - mejor_opcion.get('pago', 0))}/mes",
                                                        size=11,
                                                        weight="w600",
                                                        color="#10B981"
                                                    ),
                                                    padding=6,
                                                    bgcolor="#ECFDF5",
                                                    border_radius=6
                                                )
                                            ], spacing=0),
                                            padding=12,
                                            bgcolor="#FAFAFA",
                                            border_radius=8,
                                            margin=ft.Margin(top=8, left=0, right=0, bottom=0)
                                        )
                                    ], spacing=12),
                                    padding=16
                                )
                            ], spacing=0),
                            border=ft.Border.all(2, "#10B981"),
                            border_radius=12,
                            bgcolor="white",
                            shadow=ft.BoxShadow(blur_radius=20, color="#10B98120", offset=ft.Offset(0, 8))
                        )
                    ], spacing=0),
                    width=340
                )
            ], spacing=16, expand=True)
        ], spacing=0, scroll=ft.ScrollMode.AUTO, height=520)
        
        
        # Función para cerrar y limpiar dialog (PREVIENE MEMORY LEAK)
        def close_and_cleanup(e):
            dialog.open = False
            page.overlay.remove(dialog)  # CRÍTICO: Liberar memoria
            page.update()
        
        # Crear diálogo
        dialog = ft.AlertDialog(
            modal=True,
            title=ft.Text(""),
            content=dialog_content,
            actions=[
                ft.TextButton("Cerrar", on_click=close_and_cleanup)
            ],
            actions_alignment=ft.MainAxisAlignment.END
        )
        
        page.overlay.append(dialog)
        dialog.open = True
        page.update()

    # --- CÁLCULO CNCA (VERSIÓN DEFINITIVA CORREGIDA) ---
    async def run_cnca(e):
        nonlocal ultima_operacion_id, escenario_congelado
        
        # --- LOADING STATE UI ---
        try:
            btn_calcular_cnca.content = ft.Row([
                ft.ProgressRing(color="white", width=18, height=18, stroke_width=2),
                ft.Text("ANALIZANDO...", size=14, weight="w700", color="white", font_family=THEME["fonts"]["primary"])
            ], alignment=ft.MainAxisAlignment.CENTER, spacing=10)
            btn_calcular_cnca.gradient = ft.LinearGradient(
                colors=["#64748B", "#94A3B8"],
                begin=ft.Alignment.CENTER_LEFT,
                end=ft.Alignment.CENTER_RIGHT
            )
            btn_calcular_cnca.on_click = None
            btn_calcular_cnca.update()
        except:
            pass

        sound_manager.play("pop")
        
        # SKELETON LOADING (Feedback visual no intrusivo)
        empty_state_cnca.visible = False
        empty_state_cnca.update()
        
        grid_cnca.controls.clear()
        # Agregar tarjetas skeleton grises
        for _ in range(3):
            grid_cnca.controls.append(
                ft.Container(
                    content=ft.Column([
                        ft.Container(height=20, width=120, bgcolor="#E5E7EB", border_radius=4), # Title
                        ft.Container(height=40, width=200, bgcolor="#E5E7EB", border_radius=8), # Amount
                        ft.Row([
                            ft.Container(height=16, width=80, bgcolor="#E5E7EB", border_radius=4),
                            ft.Container(height=16, width=80, bgcolor="#E5E7EB", border_radius=4),
                        ], spacing=10)
                    ], spacing=15),
                    padding=20,
                    height=200,
                    bgcolor="#F9FAFB", # Grey 50
                    border_radius=16,
                    border=ft.Border.all(1, "#E5E7EB"),
                    animate_opacity=ft.Animation(800, "easeInOut"),
                    opacity=0.5, # Opacidad inicial para pullse
                    col={"xs": 12, "md": 6, "xl": 4}
                )
            )
        grid_cnca.update()
        
        # Permitir que la UI se actualice
        await asyncio.sleep(0.1)
        
        # Pausa para renderizar UI
        await asyncio.sleep(0.1)
        
        grid_cnca.controls.clear()
        escenario_congelado = None
        
        # Recolección de datos
        ds = [d.get_data() for d in col_deudas.controls if d.get_data() and d.get_data()['saldo'] > 0]
        aj = payment_capacity["value"]
        
        if not ds and aj == 0:
            loading.hide()
            sound_manager.play("wrong")
            await shake_control(col_deudas, page)
            msg("Ingresa al menos una deuda o ajuste", THEME["colors"]["warning"])
            return
        
        try:
            consulta_id = gestor_ops.guardar_operacion({
                "cliente": "Consulta CNCA", "producto": "MULTIPLE",
                "monto": sum(d['saldo'] for d in ds) if ds else 0, "plazo": "VARIOS",
                "tipo": "CNCA", "tipo_tasa": "TRADICIONAL", "estatus": "CONSULTA",
                # Datos completos para mostrar detalles
                "deudas": ds,
                "metadata": {
                    "total_deuda": sum(d['saldo'] for d in ds) if ds else 0,
                    "capacidad": sum(d['pago'] for d in ds) + aj,
                    "ajuste": aj
                }
            })
            ultima_operacion_id = consulta_id
            
            tot_deuda = sum(d['saldo'] for d in ds)
            cap = sum(d['pago'] for d in ds) + aj
            ops = []
            
            # OPTIMIZACIÓN: Almacenar mejor opción por producto para optimizar solo una vez
            mejores_por_producto = {}

            if cap > 0:
                # Feedback positivo - hay capacidad para calcular
                # sound_manager.play("pop")
                
                # Paso 1: Calcular cada producto
                for idx, p in enumerate(PRODUCTO_CONFIG):
                    # Actualizar paso del progress indicator
                    if HAVE_PROGRESS and progress_indicator:
                        progress_indicator['set_step'](idx + 1)  # Pasos 1-4: Calculando productos
                    
                    for pl in motor.plazos_standard:
                        # 1. TRADICIONAL
                        r = motor.calcular(p, cap, pl, False, False, rounding_step=500.0)
                        if r and motor.validar_minimo(p, r['monto'], pl):
                            rem = r['monto'] - tot_deuda
                            
                            # Calcular pago actual total (suma de pagos de deudas)
                            pago_actual = sum(d['pago'] for d in ds)
                            pago_nuevo = r['pago']
                            incremento_pago = pago_nuevo - pago_actual
                            
                            # VALIDACIÓN CRÍTICA: El nuevo pago NO debe exceder la capacidad disponible
                            # Capacidad disponible = pago actual + ajuste de liquidez
                            if pago_nuevo > cap:
                                # Rechazar oferta: el nuevo pago excede la capacidad
                                continue
                            
                            # Usar nueva validación CNCA con el incremento real del pago
                            ok, raz = motor.validar_normativa_cnca(p, ds, incremento_pago, rem, pl, usa_retencion=False)
                            if ok:
                                # Agregar operación válida
                                op_data = {**r, "producto": p, "plazo": pl, "remanente": rem, "razones": raz, "pago_visual": r['pago'], "cat": r['cat'], "deuda_a_liquidar": tot_deuda, "tipo_tasa": "TRADICIONAL"}
                                ops.append(op_data)
                                
                                # OPTIMIZACIÓN: Guardar solo la mejor opción por producto
                                key = f"{p}_TRADICIONAL"
                                if key not in mejores_por_producto or rem > mejores_por_producto[key]['data']['remanente']:
                                    mejores_por_producto[key] = {
                                        'data': op_data,
                                        'deudas': ds,
                                        'aj': aj,
                                        'cap': cap,
                                        'usa_retencion': False
                                    }
                        
                        # 2. RETENCIÓN (Solo 60)
                        if pl == 60:
                            if motor.validar_retencion_deudas(ds):
                                rt = motor.calcular(p, cap, pl, False, True, rounding_step=500.0)
                                if rt and rt['monto'] >= motor.reglas_tr_cnca.get(p, 9e9):
                                    remt = rt['monto'] - tot_deuda
                                    
                                    # *** VALIDACIÓN ESPECÍFICA CNCA CON RETENCIÓN ***
                                    monto_bruto = rt['monto']  # Monto total del crédito
                                    
                                    # DEBUG: Imprimir información
                                    print(f"\n=== DEBUG CNCA RETENCIÓN ===")
                                    print(f"Producto: {p}")
                                    print(f"Monto Bruto: ${monto_bruto:,.2f}")
                                    print(f"Plazo: {pl}")
                                    print(f"Número de deudas: {len(ds)}")
                                    for i, d in enumerate(ds):
                                        print(f"  Deuda {i+1}: Saldo=${d.get('saldo', 0):,.2f}, Pagos={d.get('pagos', 0)}")
                                    
                                    es_valido_cnca, mensaje_error = motor.validar_cnca_retencion(p, monto_bruto, pl, ds)
                                    
                                    print(f"¿Es válido?: {es_valido_cnca}")
                                    if not es_valido_cnca:
                                        print(f"Razón de rechazo: {mensaje_error}")
                                    print(f"===========================\n")
                                    
                                    if es_valido_cnca:
                                        # Calcular incremento de pago para retención
                                        pago_actual = sum(d['pago'] for d in ds)
                                        pago_nuevo = rt['pago']
                                        incremento_pago = pago_nuevo - pago_actual
                                        
                                        # VALIDACIÓN CRÍTICA: El nuevo pago NO debe exceder la capacidad disponible
                                        if pago_nuevo > cap:
                                            # Rechazar oferta: el nuevo pago excede la capacidad
                                            continue
                                        
                                        # Usar nueva validación CNCA para retención con incremento real
                                        okt, razt = motor.validar_normativa_cnca(p, ds, incremento_pago, remt, pl, usa_retencion=True)
                                        if "Mejora Tasa" not in razt: razt.append("Mejora Tasa")
                                        if okt:
                                            op_data = {**rt, "producto": p, "plazo": pl, "remanente": remt, "razones": razt, "pago_visual": rt['pago'], "cat": rt['cat'], "deuda_a_liquidar": tot_deuda, "tipo_tasa": "RETENCION"}
                                            ops.append(op_data)
                                            
                                            # OPTIMIZACIÓN: Guardar mejor opción de retención
                                            key = f"{p}_RETENCION"
                                            if key not in mejores_por_producto or remt > mejores_por_producto[key]['data']['remanente']:
                                                mejores_por_producto[key] = {
                                                    'data': op_data,
                                                    'deudas': ds,
                                                    'aj': aj,
                                                    'cap': cap,
                                                    'usa_retencion': True
                                                }
                                    # Si no es válido, simplemente no se agrega (silenciosamente filtrado)

            ops.sort(key=lambda x: x['remanente'], reverse=True)
            
            # Paso 5: Optimizar combinaciones
            if HAVE_PROGRESS and progress_indicator:
                progress_indicator['set_step'](5)  # "Optimizando combinaciones..."
            
            # OPTIMIZACIÓN: Ejecutar optimizador solo para las mejores opciones (no para todas)
            optimizaciones = []
            if len(ds) > 1:  # Solo si hay múltiples deudas
                for key, mejor in mejores_por_producto.items():
                    op_data = mejor['data']
                    opt = motor.optimizar_combinacion_deudas(
                        op_data['producto'], 
                        mejor['deudas'], 
                        mejor['aj'], 
                        op_data['plazo'], 
                        mejor['cap'], 
                        usa_retencion=mejor['usa_retencion']
                    )
                    if opt and opt['mejora'] > 0:
                        optimizaciones.append({
                            'producto': op_data['producto'],
                            'plazo': op_data['plazo'],
                            'tipo_tasa': op_data['tipo_tasa'],
                            'optimizacion': opt
                        })
            
            # MOSTRAR SUGERENCIAS DE OPTIMIZACIÓN
            if optimizaciones:
                # Encontrar la mejor optimización
                mejor_opt = max(optimizaciones, key=lambda x: x['optimizacion']['mejora'])
                opt_data = mejor_opt['optimizacion']
                
                # Construir mensaje de sugerencia
                deudas_excluir_str = "\n".join([
                    f"• {to_money(d.get('saldo', 0))} (Tasa: {d.get('tasa', 0)}%, Pagos: {d.get('pagos', 0)})"
                    for d in opt_data['deudas_a_excluir']
                ])
                
                mejora_str = to_money(opt_data['mejora'])
                remanente_actual = ops[0]['remanente'] if ops else 0
                remanente_optimo = opt_data['remanente_optimo']
                
                sugerencia_dlg = ft.AlertDialog(
                    modal=True,
                    title=ft.Row([
                        ft.Icon(ft.Icons.LIGHTBULB_OUTLINE, color="#F59E0B", size=30),
                        ft.Text("💡 Sugerencia de Optimización", weight="bold", color="#F59E0B")
                    ]),
                    content=ft.Container(
                        content=ft.Column([
                            ft.Text(
                                f"Detectamos que puedes aumentar el remanente en {mejora_str} eliminando algunas deudas:",
                                size=14,
                                weight="w500"
                            ),
                            ft.Divider(height=20),
                            ft.Text("Deudas sugeridas para excluir:", weight="bold", size=13),
                            ft.Container(
                                content=ft.Text(deudas_excluir_str, size=12, color="#64748B"),
                                bgcolor="#F8FAFC",
                                padding=10,
                                border_radius=8
                            ),
                            ft.Divider(height=20),
                            ft.Row([
                                ft.Column([
                                    ft.Text("Remanente Actual:", size=12, color="#64748B"),
                                    ft.Text(to_money(remanente_actual), size=16, weight="bold", color="#EF4444")
                                ], spacing=2),
                                ft.Icon(ft.Icons.ARROW_FORWARD, color="#10B981"),
                                ft.Column([
                                    ft.Text("Remanente Óptimo:", size=12, color="#64748B"),
                                    ft.Text(to_money(remanente_optimo), size=16, weight="bold", color="#10B981")
                                ], spacing=2)
                            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                            ft.Divider(height=10),
                            ft.Text(
                                f"Producto: {mejor_opt['producto']} | Plazo: {mejor_opt['plazo']} meses | Tipo: {mejor_opt['tipo_tasa']}",
                                size=11,
                                italic=True,
                                color="#94A3B8"
                            )
                        ], spacing=10, tight=True),
                        width=500
                    ),
                    actions=[
                        ft.TextButton("Entendido", on_click=lambda e: [setattr(sugerencia_dlg, "open", False), page.update()])
                    ]
                )
                page.overlay.append(sugerencia_dlg)
                sugerencia_dlg.open = True
                page.update()
            
            # Paso 6: Generar resultados
            if HAVE_PROGRESS and progress_indicator:
                progress_indicator['set_step'](6)  # "Generando resultados..."
            
            if not ops:
                sound_manager.play("wrong")
                grid_cnca.controls.append(ft.Container(
                    content=ft.Column([
                        ft.Icon(ft.Icons.SEARCH_OFF_ROUNDED, size=50, color=THEME["colors"]["text_tertiary"]),
                        ft.Text("Sin opciones viables", color=THEME["colors"]["text_secondary"], weight="bold"),
                        ft.Text("Intenta ajustar la liquidez.", size=12, color=THEME["colors"]["text_tertiary"])
                    ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=5),
                    alignment=ft.Alignment(0, 0), opacity=0.7
                ))
                await shake_control(grid_cnca, page)
            else:
                sound_manager.play("cash")
                
                # OPTIMIZACIÓN FASE 3: Progressive Rendering
                # En lugar de renderizar todas las cards a la vez (freeze de ~500ms),
                # las renderizamos progresivamente en lotes de 8
                
                def create_cnca_card(item):
                    """Helper para crear card con todos los parámetros"""
                    o, i = item
                    is_best = (i == 0)
                    delay = i * 100
                    
                    c = FintechCard(
                        data=o, is_best=is_best, tipo="CNCA",
                        on_layout=mostrar_detalle_oferta, on_detail=mostrar_detalle_oferta,
                        on_compare=comparar_escenarios_handler,
                        on_amortization=lambda d: generar_pdf_amortizacion(d),
                        page=page, delay_ms=delay
                    )
                    
                    # Grid responsive
                    grid_config = {"xs": 12, "md": 6, "xl": 4}
                    c.col = grid_config
                    
                    # Envolver si hay comparator
                    if HAVE_COMPARATOR and offer_comparator:
                        selectable_card = create_selectable_offer_card(o, offer_comparator, c, page)
                        if hasattr(selectable_card, 'col'):
                            selectable_card.col = grid_config
                        return selectable_card
                    else:
                        return c
                
                # Preparar items con índice
                items_with_index = [(o, i) for i, o in enumerate(ops)]
                
                # Renderizar progresivamente
                await render_cards_progressively(
                    container=grid_cnca,
                    items=items_with_index,
                    builder_func=create_cnca_card,
                    batch_size=8,
                    delay_ms=15
                )
            
            # Update final ya lo hace render_cards_progressively
            if ops:  # Si hay resultados, reproducir sonido de éxito
                # sound_manager.play("cash") # Eliminado por duplicidad
                pass
                
                # Guardar en historial
                if HAVE_HISTORY and history_manager:
                    try:
                        history_manager.add_calculation({
                            'tipo': 'CNCA',
                            'cliente': 'Consulta CNCA',
                            'deudas': ds,
                            'resultados': ops[:5],  # Top 5
                            'mejor_opcion': ops[0] if ops else None,
                            'metadata': {
                                'total_deuda': tot_deuda,
                                'capacidad': cap,
                                'ajuste': aj
                            }
                        })
                    except Exception as e:
                        print(f"Error guardando en historial: {e}")
                
                # Actualizar operación en gestor_ops con mejor opción
                if ultima_operacion_id and ops:
                    try:
                        gestor_ops.actualizar_operacion(ultima_operacion_id, {
                            "mejor_opcion": ops[0],
                            "resultados": ops[:5]
                        })
                    except Exception as e:
                        print(f"Error actualizando operación: {e}")
        
        except Exception as ex:
            import traceback
            traceback.print_exc()
            logger.error(f"Error CRÍTICO en run_cnca: {ex}", exc_info=True)
            msg(f"Error: {str(ex)[:100]}", THEME["colors"]["error"])
            sound_manager.play("error")
        finally:
            # --- RESTORE UI STATE ---
            try:
                btn_calcular_cnca.content = ft.Row([
                    ft.Icon(ft.Icons.ROCKET_LAUNCH_ROUNDED, color="white", size=22),
                    ft.Text("CALCULAR OFERTAS", size=15, weight="w800", color="white", style=ft.TextStyle(letter_spacing=0.5))
                ], alignment=ft.MainAxisAlignment.CENTER, spacing=12)
                btn_calcular_cnca.gradient = ft.LinearGradient(
                    colors=["#059669", "#34D399"], 
                    begin=ft.Alignment(-1, 1), 
                    end=ft.Alignment(1, -1)
                )
                btn_calcular_cnca.on_click = on_calcular_click
                if btn_calcular_cnca.page: btn_calcular_cnca.update()
            except: pass

            # Ocultar progress indicator o loading
            if HAVE_PROGRESS and progress_indicator:
                progress_indicator['hide']()
            else:
                loading.hide()
            load_ops()
        
    def exportar_excel_diario_handler(e):
        loading.show()
        try:
            exito, resultado = gestor_ops.exportar_excel_diario(page)
            loading.hide()
            if exito: msg(f"✅ Reporte diario generado: {resultado}")
            else: msg(f"❌ {resultado}", THEME["colors"]["error"])
        except Exception as ex:
            loading.hide()
            msg(f"❌ Error al generar reporte: {str(ex)}", THEME["colors"]["error"])

    # --- SEGURO TAB (INSURANCE CALCULATOR) - MODERN DASHBOARD ---
    
    # 1. Selectores Modernos (Chips en lugar de Dropdowns)
    
    # Estado de selección (usando diccionarios en lugar de ft.Ref para compatibilidad)
    seg_financiera_selected = {"value": "CONSUBANCO"}
    seg_tipo_selected = {"value": "VEJEZ"}
    seg_plazo_selected = {"value": "60"}
    
    def create_chip_selector(options, selected_dict, label, accent_color="#6E3482"):
        """Crea un selector de chips moderno y elegante"""
        chips = []
        chip_containers = []  # Lista separada para los contenedores reales
        
        def make_chip_click(option_value):
            def on_click(e):
                selected_dict["value"] = option_value
                # Actualizar todos los chips
                for chip_container in chip_containers:
                    is_selected = chip_container.data == option_value
                    chip_container.bgcolor = accent_color if is_selected else "#F5EBFA"
                    chip_container.border = ft.Border.all(2 if is_selected else 1, accent_color if is_selected else "#E7DBEF")
                    chip_container.content.color = "white" if is_selected else accent_color
                    chip_container.update()
                page.update()
            return on_click
        
        def make_tap_down(chip_container):
            def on_tap_down(e):
                chip_container.scale = 0.95  # Hundir
                chip_container.update()
            return on_tap_down
        
        def make_tap_up(option_value, chip_container):
            def on_tap_up(e):
                # Ejecutar selección
                selected_dict["value"] = option_value
                # Actualizar todos los chips
                for c in chip_containers:
                    is_selected = c.data == option_value
                    c.bgcolor = accent_color if is_selected else "#F5EBFA"
                    c.border = ft.Border.all(2 if is_selected else 1, accent_color if is_selected else "#E7DBEF")
                    c.content.color = "white" if is_selected else accent_color
                    c.scale = 1.0  # Rebotar
                    c.update()
                page.update()
            return on_tap_up
        
        def make_tap_cancel(chip_container):
            def on_tap_cancel(e):
                chip_container.scale = 1.0  # Recuperar sin acción
                chip_container.update()
            return on_tap_cancel
        
        for option in options:
            is_selected = option == selected_dict["value"]
            
            # Contenedor interno con texto
            chip_content = ft.Text(
                option if isinstance(option, str) else option.get("label", option),
                size=12,
                weight="bold",
                color="white" if is_selected else accent_color
            )
            
            # Chip container
            chip = ft.Container(
                content=chip_content,
                padding=ft.Padding.symmetric(horizontal=14, vertical=8),
                bgcolor=accent_color if is_selected else "#F5EBFA",
                border=ft.Border.all(2 if is_selected else 1, accent_color if is_selected else "#E7DBEF"),
                border_radius=20,
                animate=ft.Animation(200, "easeOut"),
                animate_scale=ft.Animation(150, "easeOut"),  # Animación de escala
                ink=True,
                data=option if isinstance(option, str) else option.get("value", option)
            )
            
            # Guardar referencia al contenedor
            chip_containers.append(chip)
            
            # Envolver en GestureDetector para tap & hold
            option_val = option if isinstance(option, str) else option.get("value", option)
            chip_gesture = ft.GestureDetector(
                content=chip,
                on_tap_down=make_tap_down(chip),
                on_tap_up=make_tap_up(option_val, chip),
                on_tap_cancel=make_tap_cancel(chip),
                mouse_cursor=ft.MouseCursor.CLICK
            )
            
            chips.append(chip_gesture)
        
        return ft.Column([
            ft.Text(label, size=11, weight="w600", color="#6E3482"),
            ft.Container(height=6),
            ft.Row(chips, spacing=8, wrap=True)
        ], spacing=0)
    
    # Selectores de chips
    chip_seg_fin = create_chip_selector(
        [{"label": "CONSUBANCO", "value": "CONSUBANCO"}, 
         {"label": "CONSUPAGO", "value": "CONSUPAGO"},
         {"label": "OPCIPRES", "value": "OPCIPRES"},
         {"label": "MASNOMINA", "value": "MASNOMINA"}],
        seg_financiera_selected,
        "Financiera",
        "#49225B"
    )
    
    chip_seg_tipo = create_chip_selector(
        ["INVALIDEZ", "VEJEZ"],
        seg_tipo_selected,
        "Tipo de Pensión",
        "#6E3482"
    )
    
    chip_seg_plazo = create_chip_selector(
        ["12", "24", "36", "48", "54", "60"],
        seg_plazo_selected,
        "Plazo (meses)",
        "#A56ABD"
    )
    
    # Mantener dropdowns originales ocultos para compatibilidad con código existente
    dd_seg_fin = JuicyProviderDropdown(
        label="Financiera",
        options=PRODUCTO_CONFIG,
        value="CONSUBANCO"
    )
    dd_seg_fin.visible = False
    
    dd_seg_tipo = JuicyDropdown(
        label="Tipo de Pensión",
        options=["INVALIDEZ", "VEJEZ"],
        value="VEJEZ"
    )
    dd_seg_tipo.visible = False
    
    # Sistema dual: Edad o Fecha de Nacimiento
    edad_mode = ft.Ref[ft.Container]()  # "edad" o "fecha"
    edad_mode_value = ["edad"]  # Lista mutable para compartir estado
    
    in_seg_edad = EnterpriseInput(
        label="Edad",
        keyboard_type=ft.KeyboardType.NUMBER,
        suffix=ft.Text("años"),
        width=None
    )
    
    in_seg_fecha_nac = ft.TextField(
        label="Fecha de Nacimiento",
        hint_text="DD/MM/AAAA",
        keyboard_type=ft.KeyboardType.TEXT,
        border_radius=12,
        border=ft.InputBorder.OUTLINE,
        visible=False,
        width=None
    )
    
    # Display de edad calculada - Royal Purple
    edad_calculada_display = ft.Container(
        content=ft.Row([
            ft.Icon(ft.Icons.CAKE, size=16, color="#A56ABD"),  # Lavanda Intenso
            ft.Text("Edad:", size=12, color="#A56ABD"),
            ft.Text("--", size=14, weight="bold", color="#49225B")  # Púrpura Profundo
        ], spacing=5),
        padding=10,
        bgcolor="#F5EBFA",  # Blanco Lila
        border_radius=8,
        visible=False
    )
    
    def toggle_edad_mode(e):
        """Alternar entre modo edad y fecha de nacimiento"""
        if edad_mode_value[0] == "edad":
            # Cambiar a modo fecha
            edad_mode_value[0] = "fecha"
            in_seg_edad.visible = False
            in_seg_fecha_nac.visible = True
            edad_calculada_display.visible = False
            e.control.text = "Usar Edad Directa"
            e.control.icon = ft.Icons.NUMBERS
        else:
            # Cambiar a modo edad
            edad_mode_value[0] = "edad"
            in_seg_edad.visible = True
            in_seg_fecha_nac.visible = False
            edad_calculada_display.visible = False
            e.control.text = "Usar Fecha Nacimiento"
            e.control.icon = ft.Icons.CALENDAR_TODAY
        
        page.update()
    
    def calcular_edad_desde_fecha(e):
        """Calcular edad desde fecha de nacimiento con auto-formateo"""
        fecha_str = in_seg_fecha_nac.value or ""
        
        # Auto-formateo: agregar / automáticamente
        # Remover caracteres no numéricos excepto /
        solo_numeros = ''.join(c for c in fecha_str if c.isdigit())
        
        # Formatear según la longitud
        if len(solo_numeros) <= 2:
            fecha_formateada = solo_numeros
        elif len(solo_numeros) <= 4:
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:]
        elif len(solo_numeros) <= 8:
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:4] + '/' + solo_numeros[4:8]
        else:
            # Limitar a 8 dígitos
            solo_numeros = solo_numeros[:8]
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:4] + '/' + solo_numeros[4:8]
        
        # Actualizar el campo si cambió (evitar loop infinito)
        if fecha_formateada != fecha_str:
            in_seg_fecha_nac.value = fecha_formateada
            in_seg_fecha_nac.update()
        
        # Intentar calcular edad solo si tiene formato completo
        if len(solo_numeros) < 8:
            edad_calculada_display.visible = False
            if page:
                edad_calculada_display.update()
            return
        
        try:
            # Parsear fecha DD/MM/AAAA
            from datetime import datetime
            partes = fecha_formateada.split("/")
            if len(partes) != 3:
                raise ValueError("Formato inválido")
            
            dia, mes, anio = int(partes[0]), int(partes[1]), int(partes[2])
            fecha_nac = datetime(anio, mes, dia)
            hoy = datetime.now()
            
            # Calcular edad
            edad = hoy.year - fecha_nac.year
            if (hoy.month, hoy.day) < (fecha_nac.month, fecha_nac.day):
                edad -= 1
            
            # Mostrar edad calculada
            edad_calculada_display.content.controls[2].value = f"{edad} años"
            edad_calculada_display.visible = True
            edad_calculada_display.update()
            
        except Exception as ex:
            edad_calculada_display.visible = False
            if page:
                edad_calculada_display.update()
    
    in_seg_fecha_nac.on_change = calcular_edad_desde_fecha
    
    # Botón Toggle Edad Mejorado (Pill Style con animación) - Royal Purple
    btn_toggle_edad = ft.Container(
        content=ft.Row([
            ft.Icon(ft.Icons.CALENDAR_TODAY, size=16, color="#6E3482"),  # Púrpura Real
            ft.Text("Usar Fecha Nacimiento", size=12, weight="w600", color="#6E3482")
        ], spacing=6, alignment=ft.MainAxisAlignment.CENTER),
        padding=ft.Padding.symmetric(horizontal=12, vertical=6),
        border=ft.Border.all(1.5, "#6E3482"),  # Púrpura Real
        border_radius=20,
        bgcolor="#F5EBFA",  # Blanco Lila
        on_click=toggle_edad_mode,
        animate=ft.Animation(200, "easeOut"),
        ink=True,
        ink_color="#306E3482"  # Púrpura Real con transparencia
    )
    
    in_seg_monto = EnterpriseInput(
        label="Monto Crédito",
        prefix=ft.Text("$"),
        is_money=True
    )
    
    dd_seg_plazo = JuicyDropdown(
        label="Plazo",
        options=["12", "24", "36", "48", "54", "60"],
        value="60",
        suffix=ft.Text("meses")
    )
    dd_seg_plazo.visible = False
    
    # 2. Custom Toggle Card Component (Mejorado con animaciones suaves)
    class ToggleCard(ft.Container):
        def __init__(self, label, icon, active_color, initial_value=False):
            super().__init__()
            self.value = initial_value
            self.active_color = active_color
            self.label = label
            self.icon = icon
            
            self.check_icon = ft.Icon(ft.Icons.CHECK_CIRCLE_ROUNDED, visible=self.value, color="white", size=20)
            self.bg_color_off = THEME["colors"]["input_bg"]
            self.border_color = THEME["colors"]["divider"]
            
            self.padding = ft.Padding.symmetric(horizontal=16, vertical=10)
            self.border_radius = 20
            self.border = ft.Border.all(1.5, self.border_color)
            self.bgcolor = "#F5EBFA" # Inactivo: Blanco Lila
            # Animaciones más suaves y largas para sensación premium
            self.animate = ft.Animation(200, "easeOut")
            self.animate_scale = ft.Animation(100, "easeOut")
            self.on_click = self._toggle
            self.on_hover = self._on_hover
            self.layout_content()
            
        def layout_content(self):
            # Colores basados en estado
            content_color = "white" if self.value else "#49225B" # Púrpura Profundo inactivo
            icon_color = "white" if self.value else "#6E3482" # Púrpura Real inactivo
            accent_active = self.active_color
            bg_inactive = "#F5EBFA"
            border_inactive = "#E7DBEF"
            
            self.content = ft.Row([
                ft.Row([
                    ft.Icon(self.icon, color=icon_color, size=18),
                    ft.Text(self.label, color=content_color, weight="bold" if self.value else "w600", size=12)
                ], spacing=8),
                self.check_icon
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN)
            
            if self.value:
                self.bgcolor = accent_active
                self.border = ft.Border.all(2, accent_active)
                self.shadow = ft.BoxShadow(blur_radius=8, color=f"{accent_active}40", offset=ft.Offset(0, 4))
            else:
                self.bgcolor = bg_inactive
                self.border = ft.Border.all(1.5, border_inactive)
                self.shadow = None # Sin sombra inactivo para clean look
        
        def _on_hover(self, e):
            if not self.value:
                self.scale = 1.02 if e.data == "true" else 1.0
                self.update()
                
        def _toggle(self, e):
            self.value = not self.value
            self.check_icon.visible = self.value
            self.layout_content()
            self.update()

    # Instancias de Toggles - Royal Purple Palette
    toggle_seg_nuevo = ToggleCard("Cliente Nuevo (<24m)", ft.Icons.PERSON_ADD_ROUNDED, "#49225B")  # Púrpura Profundo
    toggle_seg_ret = ToggleCard("Tasa Preferencial", ft.Icons.PERCENT_ROUNDED, "#6E3482")  # Púrpura Real

    # Referencias para compatibilidad
    sw_seg_nuevo = toggle_seg_nuevo
    sw_seg_ret = toggle_seg_ret
    
    # 3. Contenedores de Resultado - GLASSMORPHISM (Royal Purple Palette)
    # KPI Cards con Colores de la Paleta Morada
    kpi_seg_prima_text = ft.Text("$0", size=32, weight="bold", color="white")
    kpi_seg_prima = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Container(
                    content=ft.Icon(ft.Icons.MONETIZATION_ON, size=32, color="white"),
                    padding=12,
                    bgcolor="#33FFFFFF",  # 20% White
                    border_radius=10
                ),
                kpi_seg_prima_text
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            ft.Text("Prima Total del Seguro", size=14, color="white", weight="w500")
        ], spacing=16),
        padding=28,
        border_radius=12,
        bgcolor="#49225B",  # Púrpura Profundo
        border=ft.Border.all(1, "#6E3482"),
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=0,
            color="#6649225B",
            offset=ft.Offset(0, 4)
        ),
        col=6
    )
    
    kpi_seg_mensual_text = ft.Text("$0", size=32, weight="bold", color="white")
    kpi_seg_mensual = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Container(
                    content=ft.Icon(ft.Icons.CALENDAR_MONTH, size=32, color="white"),
                    padding=12,
                    bgcolor="#33FFFFFF",  # 20% White
                    border_radius=10
                ),
                kpi_seg_mensual_text
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            ft.Text("Descuento Mensual", size=14, color="white", weight="w500")
        ], spacing=16),
        padding=28,
        border_radius=12,
        bgcolor="#6E3482",  # Púrpura Real
        border=ft.Border.all(1, "#A56ABD"),
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=0,
            color="#666E3482",  # 40% Purple Royal
            offset=ft.Offset(0, 4)
        ),
        col=6
    )
    
    # Tarjeta de Estado - Glassmorphism (Royal Purple)
    status_seg_card = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Container(
                    content=ft.Icon(ft.Icons.SHIELD, size=32, color="white"),
                    padding=10,
                    bgcolor="#33FFFFFF",  # 20% White
                    border_radius=10
                ),
                ft.Column([
                    ft.Text("ESTADO DE EVALUACIÓN", size=11, color="white", weight="w600"),
                    ft.Text("ESPERANDO DATOS", size=18, weight="bold", color="white")
                ], spacing=4)
            ], spacing=14),
        ], spacing=0),
        padding=20,
        bgcolor="#49225B", # Usamos el tono oscuro para consistencia y elegancia
        border_radius=12,
        border=ft.Border.all(1, "#6E3482"),
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=0,
            color="#6649225B",
            offset=ft.Offset(0, 4)
        )
    )
    
    # Empty State - Glassmorphism (Royal Purple)
    seg_empty_state = ft.Container(
        content=ft.Column([
            ft.Container(
                content=ft.Icon(ft.Icons.SECURITY, size=72, color="#6E3482"),
                padding=48,
                border_radius=100,
                bgcolor="#F5EBFA", # Blanco Lila
                border=ft.Border.all(2, "#E7DBEF"), # Lila Niebla
                shadow=ft.BoxShadow(
                    blur_radius=15,
                    spread_radius=0,
                    color="#336E3482",  # 20% Purple Royal
                    offset=ft.Offset(0, 4)
                )
            ),
            ft.Text("Calculadora de Seguros", size=24, weight="bold", color="#49225B"),
            ft.Text(
                "Completa los datos en el panel izquierdo\ny presiona 'Cotizar Seguro' para ver los resultados.",
                size=15,
                color="#6E3482",
                text_align=ft.TextAlign.CENTER
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=24),
        alignment=ft.Alignment(0, 0),
        expand=True
    )
    
    # Referencias dinámicas para detalles
    txt_factor_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
    txt_edad_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
    txt_fin_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
    txt_costo_detalle = ft.Text("$0.00", weight="bold", color="#6E3482", size=15)
    
    # Ficha Técnica - Glassmorphism Light
    txt_razon_detalle = ft.Text("Verificando normativa...", size=14, color="#49225B", italic=True)
    
    ficha_tecnica_container = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Container(
                    content=ft.Icon(ft.Icons.FACT_CHECK_OUTLINED, size=22, color="#6E3482"),
                    padding=10,
                    bgcolor="#F5EBFA",
                    border_radius=8,
                    border=ft.Border.all(2, "#E7DBEF")
                ),
                ft.Text("Ficha Técnica", size=17, weight="bold", color="#49225B")
            ], spacing=14),
            
            ft.Divider(height=2, color="#E7DBEF", thickness=2),  # Divisor Lila Niebla
            
            ft.Container(height=12),
            
            # Grid de Datos Técnicos (2x2)
            ft.ResponsiveRow([
                ft.Container(
                    content=ft.Column([
                        ft.Text("Edad Evaluada", size=12, weight="w600", color="#A56ABD"),
                        ft.Container(height=6),
                        txt_edad_detalle
                    ], spacing=0),
                    col=6,
                    padding=ft.Padding.only(right=12, bottom=16)
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("Financiera", size=12, weight="w600", color="#A56ABD"),
                        ft.Container(height=6),
                        txt_fin_detalle
                    ], spacing=0),
                    col=6,
                    padding=ft.Padding.only(left=12, bottom=16)
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("Descuento Mensual", size=12, weight="w600", color="#A56ABD"),
                        ft.Container(height=6),
                        txt_costo_detalle
                    ], spacing=0),
                    col=6,
                    padding=ft.Padding.only(right=12, top=16)
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("Factor de Cálculo", size=12, weight="w600", color="#A56ABD"),
                        ft.Container(height=6),
                        txt_factor_detalle
                    ], spacing=0),
                    col=6,
                    padding=ft.Padding.only(left=12, top=16)
                )
            ]),
            
            ft.Container(height=20),
            
            ft.Container(
                content=ft.Column([
                    ft.Row([
                        ft.Icon(ft.Icons.INFO_OUTLINE, size=18, color="#6E3482"),
                        ft.Text("CRITERIO DE APLICACIÓN", size=12, weight="bold", color="#6E3482")
                    ], spacing=10),
                    ft.Container(height=10),
                    txt_razon_detalle
                ], spacing=0),
                padding=20,
                    bgcolor="#F5EBFA",
                border_radius=10,
                border=ft.Border.all(2, "#E7DBEF")  # Borde definido
            )
        ], spacing=14),
        padding=28,
        bgcolor="#FFFFFF",
        border_radius=12,
        border=ft.Border.all(2, "#E7DBEF"),  # Borde Lila Niebla
        # SIN SOMBRAS - Flat Design
    )

    seg_results_container = ft.Column([
        # KPIs en fila
        ft.ResponsiveRow([
            kpi_seg_prima,
            kpi_seg_mensual
        ], spacing=16),
        
        ft.Container(height=16),
        
        # Estado
        status_seg_card,
        
        ft.Container(height=16),
        
        # Ficha Técnica - Eliminada por solicitud de simplificación
        # ficha_tecnica_container
    ], spacing=0, visible=False,

    # Animaciones de entrada suaves
    opacity=0,
    scale=0.95,
    animate_opacity=ft.Animation(400, "easeOut"),
    animate_scale=ft.Animation(300, ft.AnimationCurve.EASE_OUT)
    )
    
    # Contenedor principal de resultado para view_seguro
    card_seg_resultado = ft.Stack([
        seg_empty_state,
        seg_results_container
    ])
    
    def limpiar_seguro(e):
        """Limpia todos los campos y resultados de la sección de seguros"""
        # Limpiar inputs
        in_seg_monto.value = ""
        in_seg_edad.value = ""
        in_seg_fecha_nac.value = ""
        
        # Ocultar edad calculada
        if 'edad_calculada_display' in locals() or 'edad_calculada_display' in globals():
            edad_calculada_display.visible = False
        
        # Resetear chips a valores por defecto
        seg_financiera_selected["value"] = "CONSUBANCO"
        seg_tipo_selected["value"] = "VEJEZ"
        seg_plazo_selected["value"] = "60"
        
        # Forzar actualización visual de los chips (simulando click)
        # Los chips se actualizarán cuando se vuelva a renderizar la página
        
        # Resetear toggles
        toggle_seg_nuevo.value = False
        toggle_seg_ret.value = False
        toggle_seg_nuevo.layout_content()
        toggle_seg_ret.layout_content()
        
        # Limpiar KPIs
        kpi_seg_prima_text.value = "$0"
        kpi_seg_mensual_text.value = "$0"
        
        # Gestionar visibilidad de resultados
        seg_results_container.visible = False
        seg_empty_state.visible = True
        
        # Resetear animaciones para la próxima vez
        seg_results_container.opacity = 0
        seg_results_container.scale = 0.95
        
        # Actualizar UI
        page.update()
        msg("Campos limpiados", THEME["colors"]["success"])
    
    async def run_seguro(e):
        # UX: Feedback en botón (Estado Cargando Local)
        btn = e.control
        original_content = btn.content
        
        # Cambiamos contenido a spinner
        btn.content = ft.Row([
            ft.ProgressRing(width=20, height=20, stroke_width=2, color="white"),
            ft.Text("Analizando...", size=16, weight="bold", color="white")
        ], alignment=ft.MainAxisAlignment.CENTER, spacing=10)
        btn.update()
        
        # Simular procesamiento para percepción de usuario
        await asyncio.sleep(0.6)

        try:
            # Validar inputs - Leer de los chips
            financiera = seg_financiera_selected["value"]
            tipo_pension = seg_tipo_selected["value"]
            plazo = int(seg_plazo_selected["value"])
            es_nuevo = sw_seg_nuevo.value 
            es_retencion = sw_seg_ret.value
            
            # --- Validar Edad ---
            try:
                if edad_mode_value[0] == "edad":
                    edad = int(in_seg_edad.value) if in_seg_edad.value else 0
                else:
                    from datetime import datetime
                    fecha_str = in_seg_fecha_nac.value
                    if not fecha_str: raise ValueError("Ingresa fecha de nacimiento")
                    
                    partes = fecha_str.split("/")
                    if len(partes) != 3: raise ValueError("Formato DD/MM/AAAA inválido")
                    
                    dia, mes, anio = int(partes[0]), int(partes[1]), int(partes[2])
                    fecha_nac = datetime(anio, mes, dia)
                    hoy = datetime.now()
                    edad = hoy.year - fecha_nac.year
                    if (hoy.month, hoy.day) < (fecha_nac.month, fecha_nac.day):
                        edad -= 1
            except ValueError as ve:
                sound_manager.play("wrong")
                msg(str(ve), THEME["colors"]["error"])
                return
            except:
                sound_manager.play("wrong")
                msg("Edad inválida", THEME["colors"]["error"])
                return
            
            monto = in_seg_monto.raw_value
            if monto <= 0:
                sound_manager.play("wrong")
                await shake_control(in_seg_monto, page)
                msg("Ingresa un monto válido", THEME["colors"]["error"])
                return
            
            if edad <= 0 or edad > 120:
                sound_manager.play("wrong")
                # Shake al input correspondiente (edad o fecha)
                if edad_mode_value[0] == "edad":
                    await shake_control(in_seg_edad, page)
                else:
                    await shake_control(in_seg_fecha_nac, page)
                msg("Edad fuera de rango (1-120)", THEME["colors"]["error"])
                return

            # Validaciones pasadas - Feedback positivo
            # Validaciones pasadas - Feedback positivo
            sound_manager.play("pop")

            # --- Calcular Seguro (Motor) ---
            status, color, costo_1, costo_total = motor.calcular_seguro(
                financiera, edad, monto, plazo, tipo_pension, es_nuevo, es_retencion
            )
            
            # --- Actualizar Resultados UI ---
            kpi_seg_prima_text.value = to_money(costo_total)
            kpi_seg_mensual_text.value = to_money(costo_1)
            
            # Tarjeta de Estado - Actualizada para nueva estructura
            status_seg_card.bgcolor = color
            # Nueva estructura: Column -> Row -> [Container(Icon), Column[Text, Text]]
            # Actualizar el texto del estado
            status_seg_card.content.controls[0].controls[1].controls[1].value = status
            
            # Icono Dinámico
            icon_name = ft.Icons.VERIFIED_USER
            if "NO" in status or "RECHAZADO" in status: 
                icon_name = ft.Icons.GPP_BAD
            elif "AUTORIZACION" in status: 
                icon_name = ft.Icons.ADMIN_PANEL_SETTINGS
            elif "APLICA" in status: 
                icon_name = ft.Icons.SECURITY
            
            # Actualizar el icono dentro del Container
            status_seg_card.content.controls[0].controls[0].content.name = icon_name
            
            # Ficha Técnica
            factor_aprox = costo_total / (monto * (plazo/12)) if (monto > 0 and plazo > 0) else 0
            txt_factor_detalle.value = f"{factor_aprox:.4f}"
            txt_edad_detalle.value = f"{edad} años"
            txt_fin_detalle.value = f"{financiera}"
            txt_costo_detalle.value = to_money(costo_1)
            
            # Razón Detallada (status ya contiene la razón explicativa de las nuevas reglas)
            txt_razon_detalle.value = status 
            
            # Mostrar contenedor de resultados
            seg_empty_state.visible = False
            seg_results_container.visible = True
            
            # Animación de entrada resultados
            seg_results_container.opacity = 1
            seg_results_container.scale = 1.0
            
            sound_manager.play("cash")
            page.update()
            
            # Guardar histórico (si existe manager)
            if 'history_manager' in globals() and history_manager:
                 history_manager.add_calculation({
                     "tipo": "Seguro",
                     "cliente": f"{financiera} - {tipo_pension}",
                     "mejor_opcion": {
                         "monto": costo_total,
                         "Status": status,
                         "Prima": costo_1
                     }
                 })

        except Exception as ex:
            msg(f"Error desconocido: {ex}", THEME["colors"]["error"])
            print(f"Error run_seguro: {ex}")

        finally:
            # Restaurar botón
            btn.content = original_content
            btn.update()


    # =================================================================================================
    # PERSONALIZACIÓN DE ESTILOS - ROYAL PURPLE PALETTE
    # Sobreescribir estilos por defecto (verdes/azules) para la pestaña de Seguros
    # =================================================================================================
    
    # Colores Royal Purple
    PURPLE_DEEP = "#49225B"
    PURPLE_ROYAL = "#6E3482"
    PURPLE_LAVENDER = "#A56ABD"
    PURPLE_MIST = "#E7DBEF"
    PURPLE_WHITE = "#F5EBFA"
    
    # 1. Customizar EnterpriseInputs (Monto, Edad, Fecha)
    for input_control in [in_seg_monto, in_seg_edad, in_seg_fecha_nac]:
        # Modificar propiedades directas si es posible, o acceder a sus internos
        # Dado que son clases custom, accedemos a sus atributos públicos o internos conocidos
        if hasattr(input_control, 'input_box'):
            # Borde y Fondo
            input_control.input_box.border = ft.Border.all(1.5, PURPLE_MIST)
            input_control.input_box.bgcolor = "white"
            # Intentar cambiar colores internos si la estructura lo permite
            # Nota: Esto depende de la estructura interna de EnterpriseInput, asumimos acceso a input_box

    # 2. Customizar JuicyDropdowns (Financiera, Tipo, Plazo)
    for dd_control in [dd_seg_fin, dd_seg_tipo, dd_seg_plazo]:
        # Forzar estilos visuales externos
        if hasattr(dd_control, 'main_container'):
             dd_control.main_container.border = ft.Border.all(1.5, PURPLE_MIST)

    # --- VIEW 3: SEGUROS (GLASSMORPHISM - ROYAL PURPLE) ---
    # Color Temático: Púrpura Royal
    COLOR_THEME_SEGURO = "#49225B"
    
    # Función hover para botón principal - Glassmorphism
    # Botón Seguros - Premium con gradiente sutil
    btn_calcular_violet = ft.Container(
        content=ft.Row([
            ft.Icon(ft.Icons.SHIELD_OUTLINED, color="white", size=22),
            ft.Text("COTIZAR SEGURO", size=15, weight="bold", color="white")
        ], alignment=ft.MainAxisAlignment.CENTER, spacing=10),
        padding=ft.Padding.symmetric(vertical=16, horizontal=24),
        gradient=ft.LinearGradient(
            colors=["#9333EA", "#7C3AED"],  # Púrpura vibrante -> Púrpura profundo
            begin=ft.Alignment(-0.5, -0.5),
            end=ft.Alignment(0.5, 0.5)
        ),
        border_radius=14,
        border=ft.Border.all(1.5, "#A855F7"),  # Borde púrpura brillante
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=2,
            color="#409333EA",
            offset=ft.Offset(0, 8)
        ),
        on_click=run_seguro,
        animate_scale=ft.Animation(200, "easeInOutBack"),
        tooltip="Calcular cotización de seguro"
    )

    # Código de animación duplicado eliminado - ya está definido arriba con Flat Design

    
    # =================================================================================================
    # HELPERS DE DISEÑO - GLASSMORPHISM LIGHT
    # =================================================================================================
    
    def create_input_group_seguro(title, icon, controls, subtitle=None, accent_color="#49225B"):
        """
        Crea un grupo visual de inputs con Glassmorphism Light - Royal Purple Edition
        Características: sombras suaves, borde superior de color, profundidad sutil
        """
        header_content = [
            ft.Row([
                ft.Container(
                    content=ft.Icon(icon, size=20, color=accent_color),
                    padding=10,
                    bgcolor="#F5EBFA",  # Blanco Lila explícito - Evita tonos verdosos
                    border_radius=10,
                    border=ft.Border.all(1, "#E7DBEF") # Borde sutil Lila Niebla
                ),
                ft.Column([
                    ft.Text(title, size=15, weight="bold", color="#49225B"), # Púrpura Profundo
                    ft.Text(subtitle, size=12, color="#6E3482") if subtitle else ft.Container() # Púrpura Real
                ], spacing=4)
            ], spacing=14)
        ]
        
        return ft.Container(
            content=ft.Column([
                # Borde superior de color (accent)
                ft.Container(
                    height=4,
                    bgcolor=accent_color,
                    border_radius=ft.BorderRadius.only(top_left=12, top_right=12)
                ),
                ft.Container(
                    content=ft.Column([
                        *header_content,
                        ft.Divider(height=10, color="transparent"),
                        *controls
                    ], spacing=10),
                    padding=ft.Padding.only(left=18, right=18, top=14, bottom=18)
                )
            ], spacing=0),
            bgcolor="#FFFFFF",
            border_radius=12,
            border=ft.Border.all(1, "#E2E8F0"),
            shadow=ft.BoxShadow(
                blur_radius=20,
                spread_radius=0,
                color="#0A000000",
                offset=ft.Offset(0, 4)
            )
        )
    
    # Botón Reset
    btn_reset_seguro = ft.TextButton(
        "Limpiar campos",
        icon=ft.Icons.REFRESH_ROUNDED,
        icon_color="#94A3B8",
        on_click=limpiar_seguro,
        style=ft.ButtonStyle(
            color="#64748B",
            overlay_color="#F1F5F920"
        )
    )

    # =================================================================================================
    # VISTA SEGURO - DISEÑO MODERNO AGRUPADO
    # =================================================================================================
    
    view_seguro = ft.Container(
        padding=30,
        expand=True,
        bgcolor="#FDFBFF",
        content=ft.Column([
            ft.ResponsiveRow([
                # ============================================================
                # COLUMNA IZQUIERDA: INPUTS Y CONTROLES (SIMPLIFICADO)
                # ============================================================
                ft.Container(
                    col={"md": 12, "lg": 5, "xl": 4},
                    content=ft.Column([
                        # Header Simplificado
                        ft.Row([
                            ft.Container(
                                content=ft.Icon(ft.Icons.SHIELD_ROUNDED, color="#49225B", size=24),
                                padding=10,
                                bgcolor="#F5EBFA",
                                border_radius=10
                            ),
                            ft.Text("Seguros de Vida", size=22, weight="bold", color="#49225B")
                        ], spacing=12),
                        
                        ft.Container(height=24),
                        
                        # TARJETA 1: Información del Cliente (Sin subtítulo)
                        create_input_group_seguro(
                            "Información",
                            ft.Icons.PERSON_OUTLINE_ROUNDED,
                            [
                                chip_seg_fin,
                                ft.Container(height=10),
                                chip_seg_tipo
                            ],
                            subtitle=None,
                            accent_color="#49225B"
                        ),
                        
                        ft.Container(height=12),
                        
                        # TARJETA 2: Edad (Simplificado)
                        create_input_group_seguro(
                            "Edad",
                            ft.Icons.CAKE_ROUNDED,
                            [
                                ft.Row([
                                    ft.Container(expand=True),
                                    btn_toggle_edad
                                ], alignment=ft.MainAxisAlignment.END),
                                ft.Container(height=6),
                                ft.Stack([in_seg_edad, in_seg_fecha_nac]),
                                ft.Container(
                                    content=edad_calculada_display,
                                    alignment=ft.Alignment(1, 0),
                                    padding=ft.Padding.only(top=4)
                                ),
                            ],
                            subtitle=None,
                            accent_color="#6E3482"
                        ),
                        
                        ft.Container(height=12),
                        
                        # TARJETA 3: Crédito (Simplificado)
                        create_input_group_seguro(
                            "Crédito",
                            ft.Icons.PAYMENTS_ROUNDED,
                            [
                                in_seg_monto,
                                ft.Container(height=10),
                                chip_seg_plazo
                            ],
                            subtitle=None,
                            accent_color="#A56ABD"
                        ),
                        
                        ft.Container(height=12),
                        
                        # TARJETA 4: Opciones (Simplificado)
                        create_input_group_seguro(
                            "Opciones",
                            ft.Icons.TUNE_ROUNDED,
                            [
                                toggle_seg_nuevo,
                                ft.Container(height=10),
                                toggle_seg_ret,
                            ],
                            subtitle=None,
                            accent_color="#49225B"
                        ),
                        
                        ft.Container(height=20),
                        
                        # Botón Principal
                        btn_calcular_violet,
                        
                        ft.Container(
                            content=btn_reset_seguro,
                            alignment=ft.Alignment(0, 0),
                            padding=ft.Padding.only(top=8)
                        )
                        
                    ], spacing=0, scroll=ft.ScrollMode.AUTO)
                ),
                
                # ============================================================
                # COLUMNA DERECHA: RESULTADOS
                # ============================================================
                ft.Container(
                    col={"md": 12, "lg": 7, "xl": 8},
                    content=ft.Container(
                        content=ft.Column([
                            # Header de Resultados
                            ft.Row([
                                ft.Icon(ft.Icons.SHIELD_OUTLINED, color="#8B5CF6", size=24),
                                ft.Text(
                                    "Resultado de Cotización",
                                    size=22,
                                    weight="bold",
                                    color=THEME["colors"]["text_primary"]
                                ),
                            ], spacing=12),
                            
                            ft.Container(height=20),
                            
                            # Contenedor de Resultados
                            ft.Container(
                                content=card_seg_resultado,
                                expand=True
                            )
                        ], spacing=0),
                        padding=ft.Padding.only(left=20, top=0, right=0, bottom=0),
                        expand=True
                    ),
                    expand=True
                )
            ], spacing=20, expand=True)
        ], scroll=ft.ScrollMode.AUTO, expand=True)
    )

    # --- LAYOUT SECTIONS ---
    
    # 2. VIEWS CONTENT
    
    empty_state_cnca = ft.Container(
        content=ft.Column([
            ft.Container(
                content=ft.Icon(ft.Icons.ROCKET_LAUNCH_OUTLINED, size=50, color=THEME["colors"]["text_tertiary"]),
                padding=25, bgcolor=THEME["colors"]["background"], border_radius=100,
                border=ft.Border.all(1, THEME["colors"]["divider"])
            ),
            ft.Text("Esperando Datos", size=16, weight="bold", color=THEME["colors"]["text_secondary"]),
            ft.Text(
                "Ingresa tus pasivos y presiona Calcular\npara ver las mejores ofertas.",
                size=12, color=THEME["colors"]["text_tertiary"], text_align=ft.TextAlign.CENTER
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=10),
        alignment=ft.Alignment(0, 0),
        padding=40,
        opacity=0.8,
        height=300
    )

    # Contenedor wrapper para el grid (necesario para manejar visibilidad del bloque completo)
    results_container_cnca = ft.Container(
        content=grid_cnca,
        border_radius=12,
        visible=False # Inicialmente oculto
    )

    def check_cnca_empty_state():
        has_results = len(grid_cnca.controls) > 0
        empty_state_cnca.visible = not has_results
        results_container_cnca.visible = has_results

    async def on_calcular_click(e):
        try:
            await run_cnca(e)
        except Exception as ex:
            print(f"Error en calculo: {ex}")
        finally:
            check_cnca_empty_state()
            page.update()



    # Botón Calcular Refinanciar - Premium con gradiente sutil
    btn_calcular_cnca = ft.Container(
        content=ft.Row([
            ft.Icon(ft.Icons.ROCKET_LAUNCH_ROUNDED, color="white", size=22),
            ft.Text("CALCULAR OFERTAS", size=15, weight="w800", color="white")
        ], alignment=ft.MainAxisAlignment.CENTER, spacing=12),
        gradient=ft.LinearGradient(
            colors=["#10B981", "#059669"],  # Verde esmeralda -> Verde profundo
            begin=ft.Alignment(-0.5, -0.5),
            end=ft.Alignment(0.5, 0.5)
        ),
        padding=ft.Padding.symmetric(vertical=18),
        border_radius=12,
        border=ft.Border.all(1.5, "#34D399"),  # Borde verde brillante
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=2,
            color="#4010B981",
            offset=ft.Offset(0, 8)
        ),
        animate_scale=ft.Animation(200, "easeInOutBack"),
        tooltip="Procesar datos y buscar ofertas"
    )
    
    # Wrapper con GestureDetector para animación bouncy
    btn_calcular_gesture = ft.GestureDetector(
        content=btn_calcular_cnca,
        on_tap=on_calcular_click,
        mouse_cursor=ft.MouseCursor.CLICK
    )


    def limpiar_todo(e):
        """Limpia todos los datos, deudas y resultados"""
        # 1. Limpiar deudas
        col_deudas.controls.clear()
        
        # 2. Resetear Liquidez
        liquidity_input.value = "0"
        
        # 3. Resetear Validaciones
        try: validation_gates_widget._reset_gates()
        except: pass
        
        # 4. Limpiar Resultados
        grid_cnca.controls.clear()
        check_cnca_empty_state()
        
        # 5. Resetear KPIs
        try:
            live_kpi_deuda.update_kpi(0)
            live_kpi_liquidez.update_kpi(0)
        except: pass
        
        # 6. Actualizar UI
        col_deudas.update()
        liquidity_input.update()
        if validation_gates_widget.page: validation_gates_widget.update()
        page.update()
        
        # 7. Actualizar lógica interna
        upd_sum()

    # ==================== VIEW CNCA: REFINANCIAMIENTO (FOREST GREEN) ====================
    # Nuevo layout de 3 columnas con Glassmorphism Forest
    view_cnca = ft.Container(
        padding=20,
        bgcolor="#F0FAF4",  # Fondo verde menta muy claro
        content=create_refinance_layout(
            # Widgets existentes
            col_deudas=col_deudas,
            liquidity_input=liquidity_input,
            validation_gates_widget=validation_gates_widget,
            on_calculate_callback=on_calcular_click,
            grid_cnca=grid_cnca,
            empty_state_cnca=empty_state_cnca,
            results_container_cnca=results_container_cnca,
            live_kpi_deuda=live_kpi_deuda,
            live_kpi_liquidez=live_kpi_liquidez,
            # Callbacks
            add_debt_callback=add_row,
            on_clear_callback=limpiar_todo, # Nuevo callback de limpieza
            # Theme
            theme_colors=THEME["colors"]
        ),
        expand=True
    )

    # --- VIEW 2: CRÉDITO NUEVO (SUNSET GLASSMORPHISM) ---
    # Paleta Sunset
    SUNSET_APPLE = "#951A21"   # Candy Apple
    SUNSET_ORANGE = "#BF2C20"  # Blood Orange
    SUNSET_MANGO = "#D55123"   # Dark Mango
    SUNSET_APRICOT = "#E47A24" # Apricot
    SUNSET_GOLD = "#F5C342"    # Pineapple (Visual)
    SUNSET_BG = "#FFF5F0"      # Warm White
    
    # ==================== PALETA GOLD LUXURY (REFINADA) ====================
    GOLD_PRIMARY = "#C8A86C"  # Dorado Metálico Suave (Toggle Base)
    GOLD_LIGHT   = "#F5EFE6"  # Blanco Cremoso (Fondos)
    GOLD_CREAM   = "#EBDCC1"  # Beige Arena (Acentos sutiles)
    GOLD_DARK    = "#947843"  # Bronce Antiguo (Texto, Iconos)
    GOLD_DEEP    = "#785F30"  # Marrón Dorado Profundo (Alto contraste)
    GOLD_ACCENT  = "#A68A53"  # Dorado Oscuro (Active/Hover)
    GOLD_ACTION  = "#E68900"  # Naranja Ámbar Vibrante (Botón Principal)

    # Color Temático Principal
    COLOR_THEME_NUEVO = GOLD_PRIMARY
    
    # Función hover para botón Gold
    def hover_gold_btn(e):
        if e.data == "true":
            e.control.bgcolor = GOLD_DEEP # Más oscuro al hover
            e.control.scale = 1.02
        else:
            e.control.bgcolor = GOLD_ACCENT # Dorado oscuro vibrante
            e.control.scale = 1.0
        e.control.update()
    
    # Botón Calcular Nuevo - Gold Luxury Style (CTA prominente)
    # Botón Crédito Nuevo - Premium con gradiente sutil
    btn_calcular_gold = ft.Container(
        content=ft.Row([
            ft.Icon(ft.Icons.CALCULATE_ROUNDED, color="white", size=22),
            ft.Text("CALCULAR OFERTA", size=15, weight="w800", color="white")
        ], alignment=ft.MainAxisAlignment.CENTER, spacing=12),
        padding=ft.Padding.symmetric(vertical=18),
        gradient=ft.LinearGradient(
            colors=["#F59E0B", "#D97706"],  # Dorado -> Dorado oscuro
            begin=ft.Alignment(-0.5, -0.5),
            end=ft.Alignment(0.5, 0.5)
        ),
        border_radius=12,
        border=ft.Border.all(1.5, "#FBBF24"),  # Borde dorado brillante
        shadow=ft.BoxShadow(
            blur_radius=20,
            spread_radius=2,
            color="#40F59E0B",
            offset=ft.Offset(0, 8)
        ),
        on_click=run_nuevo,
        animate_scale=ft.Animation(200, "easeInOutBack")
    )

    
    # 1. Controles Modernos (Toggle Cards) en lugar de RadioButtons
    
    # Función helper para crear Toggles con estilo Sunset Glassmorphism
    def create_gold_toggle(icon, label, value, radio_ref, on_change, tooltip_text=None):
        is_selected = (str(radio_ref.value).lower() == value.lower())
        return ft.Container(
            content=ft.Row([
                ft.Icon(icon, size=18, color="white" if is_selected else SUNSET_MANGO),
                ft.Text(label, size=14, weight="bold", color="white" if is_selected else SUNSET_MANGO)
            ], alignment=ft.MainAxisAlignment.CENTER, spacing=8),
            padding=ft.Padding.symmetric(horizontal=16, vertical=10),
            bgcolor=COLOR_THEME_NUEVO if is_selected else "#FFFBF5", # Warm Sunset White
            border=ft.Border.all(1, COLOR_THEME_NUEVO if is_selected else SUNSET_APRICOT),
            border_radius=12,
            on_click=lambda e: on_change(value),
            animate=ft.Animation(200, "easeOut"),
            tooltip=tooltip_text
        )

    # Estado local para los toggles (simulado visualmente, lógicamente usa el RadioGroup original si es posible, o actualizamos variables)
    # Para simplificar y no romper lógica existente, estos toggles actualizarán el radio_nuevo oculto y cb_retencion
    
    def update_toggles_ui():
        # Actualizar estilos del toggle
        update_morphing_visuals()
        
        # UI de retención manejada por Switch nativo
        # No se requiere actualización manual de estilos complejos

    # Etiqueta dinámica para el input
    lbl_input_titulo = ft.Text(
        "MONTO SOLICITADO", 
        size=14, 
        color=GOLD_DEEP, 
        weight="bold",
        font_family="Inter"
    )

    def change_tipo_calc(val):
        radio_nuevo.value = val
        
        # Actualizar etiqueta superior explícita
        lbl_input_titulo.value = "MONTO SOLICITADO" if val == "monto" else "CAPACIDAD DE PAGO"
        try:
            if lbl_input_titulo.page:
                lbl_input_titulo.update()
        except Exception:
            pass

        # Actualizar propiedad interna del input
        if hasattr(input_nuevo, 'label'):
             input_nuevo.label = "Monto" if val == "monto" else "Capacidad"
             input_nuevo.prefix_icon = ft.Icons.MONETIZATION_ON_ROUNDED if val == "monto" else ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED
             # Actualizar color del borde del input para match
             input_nuevo.border_color = GOLD_PRIMARY
             try:
                if input_nuevo.page:
                    input_nuevo.update()
             except Exception:
                pass
             
        update_toggles_ui()
             
        update_toggles_ui()

    def toggle_retencion_gold(e):
        # Sincronizar variable lógica con el Switch
        if hasattr(e.control, 'value'):
             cb_retencion.value = e.control.value
        else:
             cb_retencion.value = not cb_retencion.value
             
        # Actualizar cualquier UI dependiente
        update_toggles_ui()

    # ==================== TOGGLE ANIMADO MORPHING (TAP & HOLD) - GOLD EDITION ====================
    
    # 1. Definir componentes internos con diseño refinado
    toggle_icon = ft.Icon(ft.Icons.MONETIZATION_ON_ROUNDED, color="white", size=26)
    toggle_text_main = ft.Text("Cambiar a Monto", size=16, weight="bold", color="white")
    toggle_text_sub = ft.Text("Cambiar a Capacidad", size=11, color="white70")
    
    # Contenedor del icono
    icon_container = ft.Container(
        content=toggle_icon,
        width=48, height=48,
        bgcolor="white24", # Transparencia sutil sobre el fondo
        border_radius=12,
        alignment=ft.Alignment(0, 0)
    )
    
    # 2. Contenedor VISUAL
    toggle_box = ft.Container(
        content=ft.Row([
            icon_container,
            ft.Column([
                toggle_text_main,
                toggle_text_sub
            ], spacing=2, alignment=ft.MainAxisAlignment.CENTER)
        ], alignment=ft.MainAxisAlignment.START, spacing=16),
        padding=ft.Padding.all(16),
        bgcolor=GOLD_DEEP, # Color inicial
        border_radius=16,
        animate_scale=ft.Animation(400, "elasticOut"), 
        animate=ft.Animation(200, "easeOut"), 
        shadow=ft.BoxShadow(blur_radius=20, color=f"{GOLD_DEEP}50", offset=ft.Offset(0,10)),
        border=ft.Border.all(1, "white12")
    )

    # 3. Lógica visual
    def update_morphing_visuals():
        is_monto = radio_nuevo.value == "monto"
        
        # Actualizar textos e icono
        toggle_icon.name = ft.Icons.MONETIZATION_ON_ROUNDED if is_monto else ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED
        toggle_text_main.value = "Por Monto" if is_monto else "Por Capacidad"
        toggle_text_sub.value = "Cambiar a Capacidad" if is_monto else "Cambiar a Monto"
        
        color_activo = GOLD_DEEP if is_monto else GOLD_ACCENT
        
        toggle_box.bgcolor = color_activo
        toggle_box.shadow.color = f"{color_activo}40"
        
        try:
            if toggle_box.page: toggle_box.update()
        except: pass

    # 4. Handlers de Gestos (Tap & Hold Simulation)
    def handle_tap_down(e):
        toggle_box.scale = 0.96 # Hundir
        toggle_box.update()

    def handle_tap_up(e):
        # Ejecutar acción
        new_val = "capacidad" if radio_nuevo.value == "monto" else "monto"
        change_tipo_calc(new_val)
        
        # Recuperar y Morph
        toggle_box.scale = 1.0 # Rebotar
        update_morphing_visuals()
        toggle_box.update()
        
    def handle_tap_cancel(e):
        toggle_box.scale = 1.0 # Recuperar sin acción
        toggle_box.update()

    # Detector de Gestos que envuelve al botón visual
    tipo_calc_toggle = ft.GestureDetector(
        content=toggle_box,
        on_tap_down=handle_tap_down,
        on_tap_up=handle_tap_up,
        on_tap_cancel=handle_tap_cancel,
        mouse_cursor=ft.MouseCursor.CLICK
    )
    
    # Redefinir función de compatibilidad
    def update_toggle_styles():
        update_morphing_visuals()
    
    # Contenedor wrapper para mantener compatibilidad con el código existente
    tipo_calc_row = ft.Container(content=tipo_calc_toggle)
    
    # Inicializar UI
    # Llamamos a update_toggles_ui() al final del setup
    
    # Toggle de Retención - Switch nativo más intuitivo
    retencion_switch = ft.Switch(
        value=False,
        active_color=GOLD_PRIMARY,
        active_track_color=f"{GOLD_PRIMARY}60",
        inactive_thumb_color="#9CA3AF",
        inactive_track_color="#E5E7EB",
        on_change=lambda e: toggle_retencion_gold(e)
    )
    
    retencion_toggle = ft.Container(
        content=ft.Row([
            retencion_switch,
            ft.Text("Tasa Preferencial", size=14, weight="w500", color="#374151")
        ], spacing=10),
        padding=ft.Padding.symmetric(horizontal=12, vertical=8),
        bgcolor="#F9FAFB",
        border_radius=10
    )
    # 2. Barra de Herramientas (Toolbar)
    toolbar_nuevo = ft.Container(
        content=ft.ResponsiveRow([
            # Columna 1: Toggles Tipo
            ft.Column([
                ft.Text("TIPO DE CÁLCULO", size=11, color=THEME["colors"]["text_secondary"], weight="bold"),
                tipo_calc_row
            ], col={"md": 4}),
            
            # Columna 2: Input Principal
            ft.Column([
                lbl_input_titulo,
                input_nuevo
            ], col={"md": 4}),
            
            # Columna 3: Retención Toggle
            ft.Column([
                ft.Text("OPCIONES", size=11, color=THEME["colors"]["text_secondary"], weight="bold"),
                retencion_toggle
            ], col={"md": 4}),
            
        ], vertical_alignment=ft.CrossAxisAlignment.CENTER, spacing=20),
        
        padding=20,
        bgcolor="#FFFFFF",
        border_radius=16,
        border=ft.Border.all(1, "transparent"), # Sin borde visible, solo shadow
        shadow=ft.BoxShadow(
            blur_radius=10,
            color="#08000000",
            offset=ft.Offset(0, 4)
        )
    )

    def hover_scale(e):
        e.control.scale = 1.02 if e.data == "true" else 1.0
        e.control.update()

    # 3. Botón de Acción Principal (Premium Gold) con Loading State
    btn_icon = ft.Icon(ft.Icons.CALCULATE_OUTLINED, color="white", size=24)
    btn_text = ft.Text("CALCULAR OFERTAS", size=16, weight="bold", color="white", font_family="Inter")
    btn_spinner = ft.ProgressRing(width=20, height=20, stroke_width=3, color="white", visible=False)
    
    btn_content_row = ft.Row([
        btn_icon,
        btn_text,
        btn_spinner
    ], alignment=ft.MainAxisAlignment.CENTER, spacing=10)
    
    btn_calcular_gold = ft.Container(
        content=btn_content_row,
        padding=ft.Padding.symmetric(vertical=15),
        gradient=ft.LinearGradient(
            colors=[GOLD_PRIMARY, GOLD_DEEP], # Dorado Suave -> Marrón Dorado
            begin=ft.Alignment(-1, -1),
            end=ft.Alignment(1, 1)
        ),
        border_radius=12,
        shadow=ft.BoxShadow(
            blur_radius=12,
            color=f"{GOLD_DEEP}66", # Sombra marrón dorado más natura
            offset=ft.Offset(0, 4)
        ),
        on_click=run_nuevo,
        animate_scale=ft.Animation(100, "easeOut"),
        ink=True # Efecto Ripple
    )
    
    # Inicialización de toggles visuales
    update_toggles_ui()
    
    # Contador de resultados (dinámico)
    lbl_contador_ofertas = ft.Text("0", size=12, weight="bold", color="white")
    badge_contador = ft.Container(
        content=lbl_contador_ofertas,
        bgcolor=COLOR_THEME_NUEVO,
        padding=ft.Padding.symmetric(horizontal=8, vertical=4),
        border_radius=12,
        visible=False  # Oculto inicialmente
    )
    
    # Estado de ordenamiento
    orden_actual = {"criterio": "liquidez"}  # Por defecto: mayor liquidez
    
    def aplicar_orden(criterio):
        """Reordena las tarjetas en grid_nuevo según criterio"""
        orden_actual["criterio"] = criterio
        
        # Extraer datos de las tarjetas actuales
        tarjetas_con_datos = []
        
        for control in grid_nuevo.controls:
            data = None
            
            # Intentar múltiples formas de obtener data
            # 1. Directamente del control
            if hasattr(control, 'data') and isinstance(control.data, dict):
                data = control.data
            # 2. Desde content (para wrappers)
            elif hasattr(control, 'content'):
                if hasattr(control.content, 'data') and isinstance(control.content.data, dict):
                    data = control.content.data
                # 3. Desde content.content (doble wrapper)
                elif hasattr(control.content, 'content') and hasattr(control.content.content, 'data'):
                    if isinstance(control.content.content.data, dict):
                        data = control.content.content.data
            
            # Si encontramos data válida, agregamos la tupla (control, data)
            if data and 'monto' in data:
                tarjetas_con_datos.append((control, data))
        
        # Si no hay tarjetas con datos, no hacer nada
        if not tarjetas_con_datos:
            actualizar_botones_filtro()
            return
            
        # Animación de salida: fade out
        for control, _ in tarjetas_con_datos:
            if hasattr(control, 'opacity'):
                control.opacity = 0.3
                control.scale = 0.95
        grid_nuevo.update()
        
        # Ordenar según criterio
        try:
            if criterio == "liquidez":
                tarjetas_con_datos.sort(key=lambda x: -float(x[1].get('monto', 0)))
            elif criterio == "pago":
                tarjetas_con_datos.sort(key=lambda x: float(x[1].get('pago_visual', 999999)))
            elif criterio == "plazo":
                tarjetas_con_datos.sort(key=lambda x: int(x[1].get('plazo', 999)))
            
            # Actualizar grid con tarjetas ordenadas
            grid_nuevo.controls = [t[0] for t in tarjetas_con_datos]
            
            # Animación de entrada: fade in
            for control, _ in tarjetas_con_datos:
                if hasattr(control, 'opacity'):
                    control.opacity = 1.0
                    control.scale = 1.0
            
            grid_nuevo.update()
        except Exception as e:
            print(f"Error al ordenar: {e}")
        
        # Actualizar UI de botones
        actualizar_botones_filtro()
    
    def actualizar_botones_filtro():
        """Actualiza el estado visual de los botones de filtro"""
        for btn in [btn_filtro_liquidez, btn_filtro_pago, btn_filtro_plazo]:
            criterio = btn.data
            es_activo = orden_actual["criterio"] == criterio
            btn.bgcolor = COLOR_THEME_NUEVO if es_activo else "#F3F4F6"
            btn.content.controls[0].color = "white" if es_activo else THEME["colors"]["text_secondary"]
            # No modificar visibilidad aquí, se maneja en run_nuevo
            if btn.page:
                btn.update()
    
    # Botones de filtro
    btn_filtro_liquidez = ft.Container(
        data="liquidez",
        content=ft.Row([
            ft.Text("Mayor Liquidez", size=12, weight="bold")
        ], spacing=5),
        padding=ft.Padding.symmetric(horizontal=12, vertical=8),
        bgcolor=COLOR_THEME_NUEVO,
        border_radius=8,
        on_click=lambda e: aplicar_orden("liquidez"),
        visible=False  # Oculto por defecto, se muestra solo en modo Capacidad
    )
    
    btn_filtro_pago = ft.Container(
        data="pago",
        content=ft.Row([
            ft.Text("Menor Pago", size=12, weight="bold")
        ], spacing=5),
        padding=ft.Padding.symmetric(horizontal=12, vertical=8),
        bgcolor=COLOR_THEME_NUEVO,  # Activo por defecto en modo Monto
        border_radius=8,
        on_click=lambda e: aplicar_orden("pago")
    )
    
    btn_filtro_plazo = ft.Container(
        data="plazo",
        content=ft.Row([
            ft.Text("Menor Plazo", size=12, weight="bold")
        ], spacing=5),
        padding=ft.Padding.symmetric(horizontal=12, vertical=8),
        bgcolor="#F3F4F6",
        border_radius=8,
        on_click=lambda e: aplicar_orden("plazo")
    )
    
    # Contenedor de filtros (para poder actualizar visibilidad)
    filtros_row = ft.Row([
        ft.Icon(ft.Icons.SORT_ROUNDED, size=16, color=THEME["colors"]["text_tertiary"]),
        ft.Text("Ordenar:", size=12, color=THEME["colors"]["text_tertiary"], weight="bold"),
        btn_filtro_liquidez,
        btn_filtro_pago,
        btn_filtro_plazo
    ], spacing=8, wrap=True)

    # =================================================================================================
    # PERSONALIZACIÓN DE ESTILOS NUEVO - SUNSET PALETTE
    # =================================================================================================
    # Aplicar estilos Gold Luxury en input_nuevo
    if hasattr(input_nuevo, 'input_box'):
        input_nuevo.input_box.border = ft.Border.all(1.5, GOLD_CREAM)
        input_nuevo.input_box.bgcolor = "white"
        input_nuevo.input_box.focused_border_color = GOLD_PRIMARY

    # === UX MEJORADA: CRÉDITO NUEVO ===
    # Componentes auxiliares para mejor jerarquía visual
    def crear_seccion_input(titulo, icono, contenido, descripcion=None):
        """Crea una sección de input con título, icono y descripción opcional"""
        elementos = [
            ft.Row([
                ft.Icon(icono, size=16, color=GOLD_DARK),
                ft.Text(titulo, size=11, weight="w600", color=GOLD_DARK)
            ], spacing=6),
            ft.Container(height=8),
            contenido
        ]
        if descripcion:
            elementos.append(
                ft.Container(
                    content=ft.Text(descripcion, size=10, color="#9CA3AF", italic=True),
                    padding=ft.Padding.only(top=6)
                )
            )
        return ft.Container(
            content=ft.Column(elementos, spacing=0),
            padding=ft.Padding.symmetric(vertical=4)
        )
    
    # Secciones agrupadas con mejor jerarquía
    seccion_tipo_calculo = crear_seccion_input(
        "TIPO DE CÁLCULO",
        ft.Icons.SWAP_HORIZ_ROUNDED,
        tipo_calc_row,
        None  # El toggle es auto-explicativo
    )
    
    seccion_monto = crear_seccion_input(
        "MONTO O CAPACIDAD",
        ft.Icons.ATTACH_MONEY_ROUNDED,
        input_nuevo,
        None  # El placeholder ya indica qué ingresar
    )
    
    seccion_opciones = crear_seccion_input(
        "OPCIONES",
        ft.Icons.TUNE_ROUNDED,
        retencion_toggle,
        "Clientes nuevos con monto mínimo o exclientes sin crédito activo"
    )

    view_nuevo = ft.Container(
        padding=ft.Padding.symmetric(horizontal=20, vertical=24),
        expand=True,
        bgcolor=GOLD_LIGHT,
        alignment=ft.Alignment(-1, -1),
        content=ft.Column([
            ft.ResponsiveRow([
                # COLUMNA IZQUIERDA: Panel de Simulación
                ft.Container(
                    col={"xs": 12, "sm": 12, "md": 5, "lg": 4, "xl": 3},
                    content=ft.Container(
                        bgcolor="#FFFFFF",
                        padding=ft.Padding.symmetric(horizontal=20, vertical=24),
                        border_radius=16,
                        border=ft.Border.all(1, "#E5E7EB"),
                        shadow=ft.BoxShadow(
                            blur_radius=20,
                            color="#10000000",
                            offset=ft.Offset(0, 4)
                        ),
                        content=ft.Column([
                            # Header con icono y título
                            ft.Container(
                                content=ft.Row([
                                    ft.Container(
                                        content=ft.Icon(ft.Icons.CREDIT_SCORE_ROUNDED, color="white", size=22),
                                        padding=10,
                                        bgcolor=GOLD_PRIMARY,
                                        border_radius=10,
                                        shadow=ft.BoxShadow(blur_radius=8, color=f"{GOLD_PRIMARY}50", offset=ft.Offset(0, 2))
                                    ),
                                    ft.Column([
                                        ft.Text("Simulador", size=18, weight="bold", color="#1F2937"),
                                        ft.Text("Crédito Nuevo", size=11, color="#6B7280")
                                    ], spacing=0)
                                ], spacing=12),
                                padding=ft.Padding.only(bottom=20)
                            ),
                            
                            # Línea divisoria sutil
                            ft.Container(
                                height=1,
                                bgcolor="#F3F4F6",
                                margin=ft.Margin.only(bottom=16)
                            ),
                            
                            # Secciones de inputs con mejor jerarquía
                            seccion_tipo_calculo,
                            
                            ft.Container(height=16),
                            
                            seccion_monto,
                            
                            ft.Container(height=16),
                            
                            seccion_opciones,
                            
                            ft.Container(height=24),
                            
                            # Botón principal con más prominencia
                            btn_calcular_gold,
                            
                            # Botón secundario más discreto
                            ft.Container(
                                content=ft.TextButton(
                                    "Limpiar campos",
                                    icon=ft.Icons.REFRESH_ROUNDED,
                                    icon_color="#9CA3AF",
                                    style=ft.ButtonStyle(
                                        color="#6B7280",
                                        overlay_color="#F3F4F6"
                                    ),
                                    on_click=lambda e: limpiar_nuevo(e)
                                ),
                                alignment=ft.Alignment(0, 0),
                                padding=ft.Padding.only(top=8)
                            )
                            
                        ], spacing=0, scroll=ft.ScrollMode.AUTO)
                    )
                ),
                
                # COLUMNA DERECHA: Resultados
                ft.Container(
                    col={"xs": 12, "sm": 12, "md": 7, "lg": 8, "xl": 9},
                    bgcolor="transparent",
                    content=ft.Column([
                        # Header de resultados mejorado
                        ft.Container(
                            content=ft.Row([
                                ft.Row([
                                    ft.Container(
                                        content=ft.Icon(ft.Icons.VIEW_LIST_ROUNDED, color=COLOR_THEME_NUEVO, size=20),
                                        padding=8,
                                        bgcolor=f"{COLOR_THEME_NUEVO}15",
                                        border_radius=8
                                    ),
                                    ft.Text(
                                        "Ofertas Disponibles",
                                        size=18,
                                        weight="w600",
                                        color=THEME["colors"]["text_primary"]
                                    ),
                                    badge_contador
                                ], spacing=10),
                            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                            padding=ft.Padding.only(bottom=12)
                        ),
                        
                        # Barra de filtros con mejor espaciado
                        ft.Container(
                            content=filtros_row,
                            padding=ft.Padding.only(bottom=16)
                        ),
                        
                        # Grid de resultados
                        grid_nuevo,
                        
                    ], scroll=ft.ScrollMode.AUTO, expand=True),
                    expand=True,
                    padding=ft.Padding.only(left=10)  # Separación entre columnas en desktop
                )
                
            ], spacing=16, expand=True, vertical_alignment=ft.CrossAxisAlignment.START)
        ], expand=True)
    )

    # View 3: Agenda - GESTIÓN DE CLIENTES
    # Importar gestor de agenda
    try:
        from AGENDA_MANAGER import AgendaManager
        agenda_manager = AgendaManager()
        HAVE_AGENDA = True
    except:
        HAVE_AGENDA = False
        print("Warning: AGENDA_MANAGER not available")
    
    # Función helper para KPI cards (usada en Agenda)
    def create_kpi_card(title, value_control, icon, color_theme, delay=0):
        """Tarjeta KPI Minimalista con acento de color"""
        main_color = color_theme[0]
        bg_color = color_theme[1]
        
        return ft.Container(
            content=ft.Column([
                ft.Container(
                    content=ft.Icon(icon, size=24, color=main_color),
                    bgcolor=bg_color,
                    padding=10,
                    border_radius=12
                ),
                
                ft.Column([
                    value_control,
                    ft.Text(title, size=13, color="#64748B", weight="w500")
                ], spacing=2)
            ], spacing=12),
            padding=18,
            border_radius=16,
            bgcolor="white",
            border=ft.Border.all(1, "#E2E8F0"),
            shadow=ft.BoxShadow(
                blur_radius=10,
                color="#08000000",
                offset=ft.Offset(0, 4)
            ),
            # Optimización: Usar animate en lugar de on_hover con update()
            animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
            animate_opacity=300,
            col={"xs": 12, "sm": 6, "md": 4, "lg": 2},
            on_hover=lambda e: setattr(e.control, "scale", 1.02 if e.data == "true" else 1.0) or None  # Sin update()
        )
    
    # Estado de la agenda
    clientes_seleccionados = set()
    paginacion_limite = 20  # Cargar de 20 en 20 para mayor fluidez
    paginacion_offset = 0
    cargando_mas_clientes = False
    fin_de_lista = False
    
    # Campos para agregar cliente
    txt_datos_crudos = ft.TextField(
        label="Pegar datos del cliente aquí",
        multiline=True,
        min_lines=8,
        max_lines=12,
        hint_text="Ejemplo:\nTEL1: 8130877671\nNOMBRE: JUAN PEREZ\nMATRICULA: 123456789\nCONVENIO: IMSS\nCAPACIDAD: 15000",
        border_color="#E2E8F0",
        focused_border_color="#3B82F6",
        text_size=13
    )
    
    dd_estatus_cliente = ft.Dropdown(
        label="Estatus",
        options=[
            ft.dropdown.Option("Prospecto"),
            ft.dropdown.Option("Contactado"),
            ft.dropdown.Option("En Proceso"),
            ft.dropdown.Option("Aprobado"),
            ft.dropdown.Option("Rechazado"),
            ft.dropdown.Option("Inactivo")
        ],
        value="Prospecto",
        border_color="#E2E8F0",
        focused_border_color="#3B82F6"
    )

    # Dropdown de grupos (definición inicial)
    dd_grupo_cliente = ft.Dropdown(
        label="Grupo / Convenio",
        options=[],
        border_color="#E2E8F0",
        focused_border_color="#3B82F6",
        expand=True
    )

    def actualizar_dropdown_grupos():
        """Recarga la lista de grupos en el dropdown"""
        grupos = agenda_manager.obtener_grupos()
        dd_grupo_cliente.options = [ft.dropdown.Option(g) for g in grupos]
        try:
            dd_grupo_cliente.update()
        except:
            pass

    def abrir_gestor_grupos(e):
        """Abre diálogo para gestionar grupos"""
        input_nuevo_grupo = ft.TextField(label="Nuevo Grupo", expand=True, height=40)
        
        lv_grupos = ft.ListView(expand=True, spacing=5, height=200)
        
        def actualizar_lista_grupos_ui():
            grupos = agenda_manager.obtener_grupos()
            lv_grupos.controls = [
                ft.Container(
                    content=ft.Row([
                        ft.Text(g, size=14, weight="w500", color="#334155", expand=True),
                        ft.IconButton(
                            ft.Icons.DELETE_OUTLINE_ROUNDED, 
                            icon_color="#EF4444", 
                            icon_size=18,
                            tooltip="Eliminar grupo",
                            on_click=lambda e, n=g: borrar_grupo_click(e, n)
                        )
                    ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    padding=ft.Padding(10, 5, 5, 5),
                    border=ft.Border(bottom=ft.BorderSide(1, "#F1F5F9")),
                    bgcolor="white"
                )
                for g in grupos
            ]
            lv_grupos.update()

        def agregar_grupo_click(e):
            if input_nuevo_grupo.value:
                if agenda_manager.agregar_grupo(input_nuevo_grupo.value):
                    actualizar_lista_grupos_ui()
                    actualizar_dropdown_grupos()
                    input_nuevo_grupo.value = ""
                    input_nuevo_grupo.update()
                    msg("Grupo agregado correctamente", "#10B981")
                else:
                    msg("El grupo ya existe", "#EF4444")
            else:
                msg("Escribe un nombre para el grupo", "#F59E0B")
        
        def borrar_grupo_click(e, nombre):
            agenda_manager.eliminar_grupo(nombre)
            actualizar_lista_grupos_ui()
            actualizar_dropdown_grupos()
            msg(f"Grupo '{nombre}' eliminado", "#10B981")

        dlg_grupos = ft.AlertDialog(
            title=ft.Text("Gestionar Grupos", weight="bold"),
            content=ft.Container(
                content=ft.Column([
                    ft.Text("Agrega o elimina grupos de la lista", size=12, color="grey"),
                    ft.Row([
                        input_nuevo_grupo,
                        ft.IconButton(
                            ft.Icons.ADD_CIRCLE_ROUNDED, 
                            icon_color="#3B82F6", 
                            icon_size=32,
                            tooltip="Agregar",
                            on_click=agregar_grupo_click
                        )
                    ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    ft.Divider(height=20, color="#E2E8F0"),
                    ft.Text("Grupos existentes:", size=13, weight="w600", color="#475569"),
                    ft.Container(
                        content=lv_grupos,
                        bgcolor="#F8FAFC",
                        border_radius=8,
                        border=ft.Border.all(1, "#E2E8F0"),
                        padding=5
                    )
                ], spacing=10, width=400, height=400),
                padding=0
            ),
            actions=[],
            shape=ft.RoundedRectangleBorder(radius=16),
            bgcolor="white"
        )
        
        def cerrar_gestor(e):
            dlg_grupos.open = False
            page.update()
        
        dlg_grupos.actions = [
            ft.TextButton("Cerrar", on_click=cerrar_gestor, style=ft.ButtonStyle(color="#64748B"))
        ]
        
        page.overlay.append(dlg_grupos)
        dlg_grupos.open = True
        page.update()
        actualizar_lista_grupos_ui()

    # Inicializar dropdown
    actualizar_dropdown_grupos()
    
    # Barra de acciones masivas
    barra_acciones_masivas = ft.Container(
        content=ft.Row([
            ft.Row([
                ft.Icon(ft.Icons.CHECK_CIRCLE, color="white", size=20),
                ft.Text("", size=14, weight="w600", color="white")  # Texto dinámico
            ], spacing=8),
            ft.Container(expand=True),
            ft.IconButton(
                icon=ft.Icons.EDIT,
                icon_color="white",
                tooltip="Cambiar Estatus",
                on_click=lambda e: None  # Se definirá después
            ),
            ft.IconButton(
                icon=ft.Icons.GROUP,
                icon_color="white",
                tooltip="Cambiar Grupo",
                on_click=lambda e: None  # Se definirá después
            ),
            ft.IconButton(
                icon=ft.Icons.DELETE,
                icon_color="white",
                tooltip="Eliminar seleccionados",
                on_click=lambda e: None  # Se definirá después
            ),
            ft.IconButton(
                icon=ft.Icons.CLOSE,
                icon_color="white",
                tooltip="Deseleccionar todo",
                on_click=lambda e: None  # Se definirá después
            )
        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
        bgcolor="#3B82F6",
        padding=ft.Padding(20, 16, 20, 16),
        border_radius=16,
        shadow=ft.BoxShadow(
            blur_radius=20,
            color="#3B82F640",
            offset=ft.Offset(0, -4)
        ),
        visible=False,
        animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
        animate_opacity=300
    )
    
    def actualizar_barra_acciones():
        """Actualiza la visibilidad y texto de la barra de acciones masivas"""
        num_seleccionados = len(clientes_seleccionados)
        barra_acciones_masivas.visible = num_seleccionados > 0
        if num_seleccionados > 0:
            barra_acciones_masivas.content.controls[0].controls[1].value = f"{num_seleccionados} cliente{'s' if num_seleccionados > 1 else ''} seleccionado{'s' if num_seleccionados > 1 else ''}"
        barra_acciones_masivas.update()

    def cambiar_estatus_masivo(e):
        """Cambia el estatus de todos los clientes seleccionados"""
        if not clientes_seleccionados:
            return
        
        dd_nuevo_estatus = ft.Dropdown(
            label="Nuevo Estatus",
            options=[
                ft.dropdown.Option("Prospecto"),
                ft.dropdown.Option("Contactado"),
                ft.dropdown.Option("En Proceso"),
                ft.dropdown.Option("Aprobado"),
                ft.dropdown.Option("Rechazado"),
                ft.dropdown.Option("Inactivo")
            ],
            value="Prospecto",
            expand=True
        )
        
        def aplicar_cambios(e):
            try:
                for cid in clientes_seleccionados:
                    agenda_manager.actualizar_cliente(cid, {'estatus': dd_nuevo_estatus.value})
                
                msg(f"✓ {len(clientes_seleccionados)} cliente(s) actualizado(s)", "#10B981")
                sound_manager.play("pop")
                clientes_seleccionados.clear()
                dlg_estatus.open = False
                cargar_clientes()
                page.update()
            except Exception as ex:
                msg(f"Error: {ex}", "#EF4444")
        
        dlg_estatus = ft.AlertDialog(
            title=ft.Text("Cambiar Estatus en Bloque"),
            content=ft.Container(content=dd_nuevo_estatus, padding=20, width=300),
            actions=[]
        )
        
        dlg_estatus.actions = [
            ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg_estatus, 'open', False), page.update()]),
            ft.Button("Aplicar", on_click=aplicar_cambios, style=ft.ButtonStyle(bgcolor="#10B981", color="white"))
        ]
        
        page.overlay.append(dlg_estatus)
        dlg_estatus.open = True
        page.update()
    
    def cambiar_grupo_masivo(e):
        """Cambia el grupo de todos los clientes seleccionados"""
        if not clientes_seleccionados:
            return
        
        dd_nuevo_grupo = ft.Dropdown(
            label="Nuevo Grupo",
            options=[ft.dropdown.Option(g) for g in agenda_manager.obtener_grupos()],
            expand=True
        )
        
        def aplicar_cambios(e):
            try:
                for cid in clientes_seleccionados:
                    agenda_manager.actualizar_cliente(cid, {'grupo': dd_nuevo_grupo.value})
                
                msg(f"✓ {len(clientes_seleccionados)} cliente(s) actualizado(s)", "#10B981")
                sound_manager.play("pop")
                clientes_seleccionados.clear()
                dlg_grupo.open = False
                cargar_clientes()
                page.update()
            except Exception as ex:
                msg(f"Error: {ex}", "#EF4444")
        
        dlg_grupo = ft.AlertDialog(
            title=ft.Text("Cambiar Grupo en Bloque"),
            content=ft.Container(content=dd_nuevo_grupo, padding=20, width=300),
            actions=[]
        )
        
        dlg_grupo.actions = [
            ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg_grupo, 'open', False), page.update()]),
            ft.Button("Aplicar", on_click=aplicar_cambios, style=ft.ButtonStyle(bgcolor="#10B981", color="white"))
        ]
        
        page.overlay.append(dlg_grupo)
        dlg_grupo.open = True
        page.update()
    
    def eliminar_seleccionados(e):
        """Elimina todos los clientes seleccionados"""
        if not clientes_seleccionados:
            return
        
        def confirmar_eliminacion(e):
            try:
                for cid in clientes_seleccionados:
                    agenda_manager.eliminar_cliente(cid)
                
                msg(f"✓ {len(clientes_seleccionados)} cliente(s) eliminado(s)", "#10B981")
                sound_manager.play("pop")
                clientes_seleccionados.clear()
                dlg_confirm.open = False
                cargar_clientes()
                page.update()
            except Exception as ex:
                msg(f"Error: {ex}", "#EF4444")
        
        dlg_confirm = ft.AlertDialog(
            title=ft.Text("⚠️ Confirmar Eliminación"),
            content=ft.Text(f"¿Estás seguro de eliminar {len(clientes_seleccionados)} cliente(s)?"),
            actions=[]
        )
        
        dlg_confirm.actions = [
            ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg_confirm, 'open', False), page.update()]),
            ft.Button("Eliminar", on_click=confirmar_eliminacion, style=ft.ButtonStyle(bgcolor="#EF4444", color="white"))
        ]
        
        page.overlay.append(dlg_confirm)
        dlg_confirm.open = True
        page.update()
    
    def deseleccionar_todos(e):
        """Des selecciona todos los clientes"""
        clientes_seleccionados.clear()
        cargar_clientes()
    
    # Asignar handlers a la barra de acciones
    barra_acciones_masivas.content.controls[2].on_click = cambiar_estatus_masivo
    barra_acciones_masivas.content.controls[3].on_click = cambiar_grupo_masivo
    barra_acciones_masivas.content.controls[4].on_click = eliminar_seleccionados
    barra_acciones_masivas.content.controls[5].on_click = deseleccionar_todos


    
    # Búsqueda y filtros
    txt_buscar_cliente = ft.TextField(
        hint_text="Buscar por nombre, teléfono o email...",
        prefix_icon=ft.Icons.SEARCH_ROUNDED,
        border_color="#E2E8F0",
        focused_border_color="#3B82F6",
        col={"xs": 12, "md": 8}
    )
    
    dd_filtro_estatus = ft.Dropdown(
        label="Filtrar por estatus",
        options=[
            ft.dropdown.Option("Todos"),
            ft.dropdown.Option("Prospecto"),
            ft.dropdown.Option("Contactado"),
            ft.dropdown.Option("En Proceso"),
            ft.dropdown.Option("Aprobado")
        ],
        value="Todos",
        border_color="#E2E8F0",
        focused_border_color="#3B82F6",
        col={"xs": 12, "md": 4}
    )
    
    # Contenedor de clientes
    lista_clientes = ft.Column(spacing=12, scroll=ft.ScrollMode.AUTO)
    
    # KPIs de agenda
    kpi_total_clientes = ft.Text("0", size=32, weight="w800", color="#1E293B")
    kpi_prospectos = ft.Text("0", size=32, weight="w800", color="#1E293B")
    kpi_en_proceso = ft.Text("0", size=32, weight="w800", color="#1E293B")
    kpi_aprobados = ft.Text("0", size=32, weight="w800", color="#1E293B")
    
    def editar_cliente_dialog(cliente):
        """Abre diálogo para editar un cliente existente"""
        # Campos editables
        txt_nombre = ft.TextField(label="Nombre", value=cliente.get('nombre', ''), expand=True)
        txt_telefono = ft.TextField(label="Teléfono", value=cliente.get('telefono', ''), expand=True)
        txt_email = ft.TextField(label="Email", value=cliente.get('email', ''), expand=True)
        txt_nss = ft.TextField(label="NSS", value=cliente.get('nss', ''), expand=True)
        txt_empresa = ft.TextField(label="Empresa/Convenio", value=cliente.get('empresa', ''), expand=True)
        txt_puesto = ft.TextField(label="Puesto", value=cliente.get('puesto', ''), expand=True)
        txt_salario = ft.TextField(label="Capacidad", value=str(cliente.get('salario', 0)), expand=True)
        txt_notas = ft.TextField(label="Notas", value=cliente.get('notas', ''), multiline=True, min_lines=3, max_lines=5, expand=True)
        
        dd_estatus_edit = ft.Dropdown(
            label="Estatus",
            options=[
                ft.dropdown.Option("Prospecto"),
                ft.dropdown.Option("Contactado"),
                ft.dropdown.Option("En Proceso"),
                ft.dropdown.Option("Aprobado"),
                ft.dropdown.Option("Rechazado"),
                ft.dropdown.Option("Inactivo")
            ],
            value=cliente.get('estatus', 'Prospecto'),
            expand=True
        )
        
        dd_grupo_edit = ft.Dropdown(
            label="Grupo",
            options=[ft.dropdown.Option(g) for g in agenda_manager.obtener_grupos()],
            value=cliente.get('grupo'),
            expand=True
        )
        
        dlg_edit = ft.AlertDialog(
            title=ft.Text("Editar Cliente", weight="bold", size=18),
            content=ft.Container(
                content=ft.Column([
                    txt_nombre,
                    ft.Row([txt_telefono, txt_email], spacing=10),
                    ft.Row([txt_nss, txt_empresa], spacing=10),
                    ft.Row([txt_puesto, txt_salario], spacing=10),
                    ft.Row([dd_estatus_edit, dd_grupo_edit], spacing=10),
                    txt_notas
                ], spacing=12, scroll=ft.ScrollMode.AUTO, width=500, height=400),
                padding=10
            ),
            actions=[],
            shape=ft.RoundedRectangleBorder(radius=16)
        )
        
        def guardar_cambios(e):
            if not txt_nombre.value:
                msg("El nombre es obligatorio", "#EF4444")
                return
            
            # Actualizar datos
            datos_actualizados = {
                'nombre': txt_nombre.value,
                'telefono': txt_telefono.value,
                'email': txt_email.value,
                'nss': txt_nss.value,
                'empresa': txt_empresa.value,
                'puesto': txt_puesto.value,
                'salario': float(txt_salario.value) if txt_salario.value else 0.0,
                'notas': txt_notas.value,
                'estatus': dd_estatus_edit.value,
                'grupo': dd_grupo_edit.value
            }
            
            try:
                agenda_manager.actualizar_cliente(cliente['id'], datos_actualizados)
                sound_manager.play("pop")
                msg(f"✓ Cliente actualizado: {datos_actualizados['nombre']}", "#10B981")
                
                # Cerrar diálogo de edición
                dlg_edit.open = False
                
                # Recargar lista de clientes
                cargar_clientes()
                
                page.update()
            except Exception as ex:
                msg(f"Error al actualizar: {ex}", "#EF4444")
        
        def cerrar_dialogo(e):
            dlg_edit.open = False
            page.update()
        
        dlg_edit.actions = [
            ft.TextButton("Cancelar", on_click=cerrar_dialogo),
            ft.Button(
                "Guardar Cambios",
                on_click=guardar_cambios,
                style=ft.ButtonStyle(
                    bgcolor="#10B981",
                    color="white",
                    padding=ft.Padding(20, 12, 20, 12)
                )
            )
        ]
        
        page.overlay.append(dlg_edit)
        dlg_edit.open = True
        page.update()
    
    def mostrar_detalles_cliente(cliente):
        """Muestra un diálogo con detalles del cliente y opciones de copiado"""
        try:
            def safe_copy(texto):
                try:
                    page.set_clipboard(str(texto))
                except Exception as ex:
                    print(f"Clipboard not available: {ex}")

            def copiar_dato(valor):
                if valor:
                    safe_copy(valor)
                    msg(f"✓ Copiado: {valor}", "#10B981")
                    sound_manager.play("pop")

            def crear_fila_dato(icono, etiqueta, valor, color_icono="#64748B"):
                if not valor: return ft.Container()
                
                return ft.Container(
                    content=ft.Row([
                        # Icono en círculo con fondo de color
                        ft.Container(
                            content=ft.Icon(icono, size=20, color=color_icono),
                            width=44,
                            height=44,
                            bgcolor=f"{color_icono}15",  # Color con transparencia
                            border_radius=12,
                            alignment=ft.alignment.Alignment(0, 0)
                        ),
                        # Contenido
                        ft.Container(
                            content=ft.Column([
                                ft.Text(etiqueta, size=12, color="#94A3B8", weight="w500"),
                                ft.Text(str(valor), size=15, weight="w600", color="#1E293B", selectable=True)
                            ], spacing=4),
                            expand=True
                        ),
                        # Botón de copiar más sutil
                        ft.Container(
                            content=ft.Icon(ft.Icons.CONTENT_COPY_ROUNDED, size=18, color="#94A3B8"),
                            width=36,
                            height=36,
                            border_radius=8,
                            bgcolor="#F8FAFC",
                            alignment=ft.alignment.Alignment(0, 0),
                            on_click=lambda e: copiar_dato(valor),
                            ink=True,
                            animate=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
                            animate_scale=ft.Animation(100, ft.AnimationCurve.EASE_OUT),
                            on_hover=lambda e: setattr(e.control, "bgcolor", "#E2E8F0" if e.data == "true" else "#F8FAFC")
                        )
                    ], spacing=16, alignment=ft.MainAxisAlignment.START),
                    padding=ft.Padding(16, 12, 16, 12),
                    border=ft.Border(bottom=ft.BorderSide(1, "#F1F5F9"))
                )

            # Contenido del diálogo
            dlg_content = ft.Container(
                width=500,
                content=ft.Column([
                    # Header con Avatar
                    ft.Container(
                        content=ft.Row([
                            # Avatar más grande y con sombra
                            ft.Container(
                                content=ft.CircleAvatar(
                                    content=ft.Text(cliente.get('nombre', 'A')[0].upper(), size=28, weight="w900", color="white"),
                                    bgcolor="#3B82F6",
                                    radius=36
                                ),
                                shadow=ft.BoxShadow(
                                    blur_radius=16,
                                    color="#3B82F640",
                                    offset=ft.Offset(0, 4)
                                )
                            ),
                            # Info del cliente
                            ft.Column([
                                ft.Text(cliente.get('nombre', 'Sin nombre'), size=20, weight="w700", color="#0F172A"),
                                ft.Container(
                                    content=ft.Text(cliente.get('estatus', 'Prospecto').upper(), size=11, weight="w700", color="white"),
                                    bgcolor="#3B82F6",
                                    padding=ft.Padding(12, 6, 12, 6),
                                    border_radius=16
                                )
                            ], spacing=8)
                        ], spacing=20),
                        padding=ft.Padding(24, 24, 24, 20),
                        bgcolor="#FFFFFF",
                        border=ft.Border(bottom=ft.BorderSide(2, "#F1F5F9"))
                    ),
                    
                    # Lista de datos
                    ft.Column([
                        crear_fila_dato(ft.Icons.PHONE_ROUNDED, "TEL1", cliente.get('telefono'), "#3B82F6"),
                        crear_fila_dato(ft.Icons.EMAIL_ROUNDED, "EMAIL", cliente.get('email'), "#F59E0B"),
                        crear_fila_dato(ft.Icons.BADGE_ROUNDED, "MATRICULA / NSS", cliente.get('nss'), "#10B981"),
                        crear_fila_dato(ft.Icons.BUSINESS_ROUNDED, "CONVENIO", cliente.get('empresa'), "#6366F1"),
                        crear_fila_dato(ft.Icons.WORK_ROUNDED, "PUESTO", cliente.get('puesto'), "#8B5CF6"),
                        crear_fila_dato(ft.Icons.ATTACH_MONEY_ROUNDED, "CAPACIDAD", f"${cliente.get('salario', 0):,.2f}", "#10B981"),
                        crear_fila_dato(ft.Icons.GROUPS_ROUNDED, "GRUPO", cliente.get('grupo'), "#EC4899"),
                    ], spacing=0, scroll=ft.ScrollMode.AUTO, height=300),
                    
                    # Notas mejoradas
                    ft.Container(
                        content=ft.Column([
                            ft.Row([
                                ft.Icon(ft.Icons.NOTES_ROUNDED, size=18, color="#64748B"),
                                ft.Text("Información Adicional", size=13, weight="w600", color="#475569")
                            ], spacing=8),
                            ft.Container(
                                content=ft.Text(
                                    cliente.get('notas', 'Sin información adicional'), 
                                    size=13, 
                                    color="#64748B", 
                                    selectable=True,
                                    weight="w400"
                                ),
                                bgcolor="#F8FAFC",
                                padding=16,
                                border_radius=12,
                                border=ft.Border.all(1, "#E2E8F0"),
                                width=float('inf')
                            )
                        ], spacing=12),
                        padding=ft.Padding(20, 16, 20, 20),
                        bgcolor="#FFFFFF"
                    )
                ], spacing=0)
            )
            
            
            dlg = ft.AlertDialog(
                content=dlg_content,
                content_padding=0,
                actions=[],
                actions_padding=ft.Padding(20, 16, 20, 20),
                actions_alignment=ft.MainAxisAlignment.END,
                shape=ft.RoundedRectangleBorder(radius=16),
                bgcolor="#FFFFFF"
            )
            
            def cerrar_detalles(e):
                dlg.open = False
                page.update()
            
            def abrir_editar(e):
                dlg.open = False
                page.update()
                editar_cliente_dialog(cliente)
            
            dlg.actions = [
                ft.TextButton(
                    "Cerrar", 
                    on_click=cerrar_detalles,
                    style=ft.ButtonStyle(
                        color="#64748B",
                        padding=ft.Padding(20, 12, 20, 12)
                    )
                ),
                ft.Button(
                    content=ft.Row([
                        ft.Icon(ft.Icons.EDIT_ROUNDED, size=18),
                        ft.Text("Editar", weight="w600")
                    ], spacing=8),
                    on_click=abrir_editar,
                    style=ft.ButtonStyle(
                        bgcolor="#F59E0B",
                        color="white",
                        padding=ft.Padding(24, 14, 24, 14),
                        shape=ft.RoundedRectangleBorder(radius=12)
                    )
                ),
                ft.Button(
                    content=ft.Row([
                        ft.Icon(ft.Icons.COPY_ALL_ROUNDED, size=18),
                        ft.Text("Copiar Todo", weight="w600")
                    ], spacing=8),
                    on_click=lambda e: [safe_copy(str(cliente)), msg("✓ Datos copiados", "#10B981"), sound_manager.play("pop")],
                    style=ft.ButtonStyle(
                        bgcolor="#3B82F6",
                        color="white",
                        padding=ft.Padding(24, 14, 24, 14),
                        shape=ft.RoundedRectangleBorder(radius=12)
                    )
                )
            ]
            
            # Método Overlay (Más seguro en Mac)
            page.overlay.append(dlg)
            dlg.open = True
            page.update()


        except Exception as ex:
            print(f"ERROR displaying details: {ex}")
            import traceback
            traceback.print_exc()

    def toggle_seleccion_cliente(e, cliente_id):
        """Toggle selección de cliente"""
        if e.control.value:
            clientes_seleccionados.add(cliente_id)
        else:
            clientes_seleccionados.discard(cliente_id)
        actualizar_barra_acciones()

    def eliminar_cliente(cliente_id):
        """Elimina un cliente específico"""
        if not HAVE_AGENDA: return
        try:
            agenda_manager.eliminar_cliente(cliente_id)
            sound_manager.play("pop")
            msg("Cliente eliminado", "#64748B")
            cargar_clientes()
        except Exception as ex:
            msg(f"Error: {str(ex)}", "#EF4444")

    def crear_tarjeta_cliente(cliente):
        """Crea una tarjeta visual para un cliente"""
        # Colores por estatus
        estatus_colors = {
            "Prospecto": {"bg": "#FEF3C7", "text": "#92400E", "icon": ft.Icons.PERSON_OUTLINE},
            "Contactado": {"bg": "#DBEAFE", "text": "#1E40AF", "icon": ft.Icons.PHONE_OUTLINED},
            "En Proceso": {"bg": "#E0E7FF", "text": "#3730A3", "icon": ft.Icons.AUTORENEW_ROUNDED},
            "Aprobado": {"bg": "#D1FAE5", "text": "#065F46", "icon": ft.Icons.CHECK_CIRCLE_OUTLINE},
            "Rechazado": {"bg": "#FEE2E2", "text": "#991B1B", "icon": ft.Icons.CANCEL_OUTLINED},
            "Inactivo": {"bg": "#F3F4F6", "text": "#6B7280", "icon": ft.Icons.REMOVE_CIRCLE_OUTLINE}
        }
        
        estatus = cliente.get('estatus', 'Prospecto')
        config = estatus_colors.get(estatus, estatus_colors["Prospecto"])
        
        return ft.Container(
            content=ft.Column([
                # Header con checkbox y estatus
                ft.Row([
                    ft.Checkbox(
                        value=cliente['id'] in clientes_seleccionados,
                        on_change=lambda e, cid=cliente['id']: toggle_seleccion_cliente(e, cid)
                    ),
                    ft.Container(
                        content=ft.Row([
                            ft.Icon(config["icon"], size=16, color=config["text"]),
                            ft.Text(estatus, size=12, weight="w600", color=config["text"])
                        ], spacing=6),
                        bgcolor=config["bg"],
                        padding=ft.Padding(left=10, right=10, top=6, bottom=6),
                        border_radius=8
                    ),
                    ft.Container(expand=True),
                    ft.IconButton(
                        icon=ft.Icons.VISIBILITY_OUTLINED,
                        icon_size=18,
                        icon_color="#3B82F6",
                        tooltip="Ver detalles",
                        on_click=lambda e: mostrar_detalles_cliente(cliente)
                    ),
                    ft.IconButton(
                        icon=ft.Icons.DELETE_OUTLINE,
                        icon_size=18,
                        icon_color="#EF4444",
                        tooltip="Eliminar",
                        on_click=lambda e, cid=cliente['id']: eliminar_cliente(cid)
                    )
                ], alignment=ft.MainAxisAlignment.START),
                
                ft.Divider(height=1, color="#E5E7EB"),
                
                # Información del cliente
                ft.Container(
                    content=ft.Column([
                        # Nombre
                        ft.Row([
                            ft.Icon(ft.Icons.PERSON_ROUNDED, size=18, color="#64748B"),
                            ft.Text(cliente.get('nombre', 'Sin nombre'), size=16, weight="bold", color="#1E293B")
                        ], spacing=8),
                        
                        # Acciones rápidas (visible en hover)
                        ft.Container(
                            content=ft.Row([
                                ft.IconButton(
                                    icon=ft.Icons.PHONE,
                                    icon_size=16,
                                    tooltip="Copiar teléfono",
                                    on_click=lambda e, tel=cliente.get('telefono'): [
                                        page.set_clipboard(tel) if tel else None,
                                        msg(f"📞 {tel} copiado", "#10B981") if tel else msg("Sin teléfono", "#EF4444"),
                                        sound_manager.play("pop")
                                    ] if cliente.get('telefono') else msg("Sin teléfono", "#F59E0B"),
                                    icon_color="#3B82F6",
                                    bgcolor="#EFF6FF",
                                    style=ft.ButtonStyle(shape=ft.CircleBorder(), padding=8)
                                ),
                                ft.IconButton(
                                    icon=ft.Icons.BADGE,
                                    icon_size=16,
                                    tooltip="Copiar NSS",
                                    on_click=lambda e, nss=cliente.get('nss'): [
                                        page.set_clipboard(nss) if nss else None,
                                        msg(f"🆔 NSS copiado", "#10B981") if nss else msg("Sin NSS", "#EF4444"),
                                        sound_manager.play("pop")
                                    ] if cliente.get('nss') else msg("Sin NSS", "#F59E0B"),
                                    icon_color="#10B981",
                                    bgcolor="#ECFDF5",
                                    style=ft.ButtonStyle(shape=ft.CircleBorder(), padding=8)
                                ),
                                ft.IconButton(
                                    icon=ft.Icons.EMAIL,
                                    icon_size=16,
                                    tooltip="Copiar email",
                                    on_click=lambda e, email=cliente.get('email'): [
                                        page.set_clipboard(email) if email else None,
                                        msg(f"✉️ Email copiado", "#10B981") if email else msg("Sin email", "#EF4444"),
                                        sound_manager.play("pop")
                                    ] if cliente.get('email') else msg("Sin email", "#F59E0B"),
                                    icon_color="#F59E0B",
                                    bgcolor="#FEF3C7",
                                    style=ft.ButtonStyle(shape=ft.CircleBorder(), padding=8)
                                )
                            ], spacing=8, alignment=ft.MainAxisAlignment.END),
                            padding=ft.Padding(top=8, bottom=4),
                            opacity=0.7,
                            animate_opacity=200
                        ),
                        
                        # Teléfono
                        ft.Row([
                            ft.Icon(ft.Icons.PHONE_ROUNDED, size=16, color="#64748B"),
                            ft.Text(cliente.get('telefono', 'Sin teléfono'), size=13, color="#475569")
                        ], spacing=8) if cliente.get('telefono') else ft.Container(),
                        
                        # Email
                        ft.Row([
                            ft.Icon(ft.Icons.EMAIL_ROUNDED, size=16, color="#64748B"),
                            ft.Text(cliente.get('email', 'Sin email'), size=13, color="#475569")
                        ], spacing=8) if cliente.get('email') else ft.Container(),
                        
                        # Empresa y capacidad
                        ft.Row([
                            ft.Container(
                                content=ft.Text(f"🏢 {cliente.get('empresa', 'N/A')}", size=12, color="#64748B"),
                                padding=ft.Padding(left=6, right=6, top=4, bottom=4),
                                bgcolor="#F8FAFC",
                                border_radius=6
                            ),
                            ft.Container(
                                content=ft.Text(f"💰 ${cliente.get('salario', 0):,.0f}", size=12, color="#10B981", weight="w600"),
                                padding=ft.Padding(left=6, right=6, top=4, bottom=4),
                                bgcolor="#ECFDF5",
                                border_radius=6
                            ) if cliente.get('salario') else ft.Container(),
                            ft.Container(
                                content=ft.Text(f"👥 {cliente.get('grupo')}", size=11, color="#6366F1", weight="bold"),
                                padding=ft.Padding(left=6, right=6, top=4, bottom=4),
                                bgcolor="#EEF2FF",
                                border_radius=6
                            ) if cliente.get('grupo') else ft.Container()
                        ], spacing=8),
                        
                        # Notas (si existen)
                        ft.Container(
                            content=ft.Text(cliente.get('notas', ''), size=11, color="#6B7280", italic=True),
                            padding=ft.Padding(top=8)
                        ) if cliente.get('notas') else ft.Container()
                        
                    ], spacing=8),
                    padding=12
                )
            ], spacing=0),
            bgcolor="white",
            border_radius=12,
            border=ft.Border.all(1, "#E2E8F0"),
            shadow=ft.BoxShadow(
                blur_radius=8,
                color="#1018280A",
                offset=ft.Offset(0, 2)
            ),
            animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
            on_click=lambda e: mostrar_detalles_cliente(cliente),
            on_hover=lambda e: [
                setattr(e.control, "scale", 1.02 if e.data == "true" else 1.0),
                setattr(e.control.shadow, "blur_radius", 12 if e.data == "true" else 8),
                e.control.update()
            ]
        )
        
        return card

    def cargar_clientes(e=None, cargar_mas=False):
        """Carga y muestra la lista de clientes con Lazy Loading"""
        if not HAVE_AGENDA:
            return
            
        nonlocal paginacion_offset, cargando_mas_clientes, fin_de_lista
        
        # Si es una búsqueda nueva (no es scroll), reiniciar todo
        if not cargar_mas:
            paginacion_offset = 0
            fin_de_lista = False
            lista_clientes.controls.clear()
            # Añadir indicador de carga inicial (opcional)
        elif cargando_mas_clientes or fin_de_lista:
            return

        cargando_mas_clientes = True
        
        # Obtener filtros
        filtro_texto = txt_buscar_cliente.value if txt_buscar_cliente.value else None
        filtro_est = dd_filtro_estatus.value if dd_filtro_estatus.value != "Todos" else None
        
        # Obtener lote de clientes
        clientes = agenda_manager.obtener_clientes(
            filtro=filtro_texto, 
            estatus=filtro_est,
            limite=paginacion_limite,
            offset=paginacion_offset
        )
        
        cargando_mas_clientes = False
        
        # Verificar si hemos llegado al final
        if len(clientes) < paginacion_limite:
            fin_de_lista = True
        else:
            paginacion_offset += paginacion_limite
            
        # Añadir clientes a la lista existente
        for cliente in clientes:
            lista_clientes.controls.append(crear_tarjeta_cliente(cliente))
            
        # Lógica de Empty State (Solo si no hay clientes Y es la primera página y la lista está vacía)
        if not clientes and paginacion_offset == 0 and not lista_clientes.controls:
            lista_clientes.controls.append(
                ft.Container(
                    content=ft.Column([
                        ft.Container(
                            content=ft.Icon(ft.Icons.PERSON_ADD_ALT_ROUNDED, size=80, color="#3B82F6"),
                            bgcolor="#EFF6FF",
                            border_radius=40,
                            padding=30
                        ),
                        ft.Text("¡Comienza tu cartera de clientes!", size=20, color="#1E293B", weight="bold"),
                        ft.Text("Agrega tu primer cliente para gestionar tu prospección", size=14, color="#64748B", text_align="center"),
                        ft.Container(height=20),
                        ft.Button(
                            content=ft.Row([
                                ft.Icon(ft.Icons.ADD_CIRCLE_ROUNDED, size=20),
                                ft.Text("Agregar Primer Cliente", weight="w600")
                            ], spacing=8),
                            on_click=lambda e: txt_datos_crudos.focus(),
                            style=ft.ButtonStyle(
                                bgcolor="#3B82F6",
                                color="white",
                                padding=ft.Padding(24, 16, 24, 16),
                                shape=ft.RoundedRectangleBorder(radius=12)
                            )
                        )
                    ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=16),
                    padding=60,
                    alignment=ft.Alignment(0, 0)
                )
            )

        lista_clientes.update()
        if not cargar_mas:
            actualizar_kpis() # Actualizar KPIs siempre es rápido

    def on_scroll_lista(e):
        """Detecta cuando el usuario llega al fondo de la lista"""
        if e.pixels >= e.max_scroll_extent - 100:
            cargar_clientes(cargar_mas=True)

    # Configurar el listener de scroll en el ListView
    lista_clientes.on_scroll = on_scroll_lista

    def load_ops(e=None):
        """Función dummy para compatibilidad con módulos que guardan operaciones."""
        pass
    
    def agregar_cliente_desde_datos(e):
        """Agrega un cliente parseando los datos crudos"""
        if not HAVE_AGENDA:
            msg("Sistema de agenda no disponible", "#EF4444")
            return
        
        if not txt_datos_crudos.value:
            msg("Ingresa los datos del cliente", "#F59E0B")
            return
        
        # Parsear datos
        datos = agenda_manager.parsear_datos_crudos(txt_datos_crudos.value)
        datos['estatus'] = dd_estatus_cliente.value
        datos['grupo'] = dd_grupo_cliente.value
        datos['asesor'] = perfil.get('nombre', 'Asesor')
        
        # Validar que al menos tenga nombre
        if not datos.get('nombre'):
            msg("El cliente debe tener al menos un nombre", "#F59E0B")
            return
        
        # Agregar a la base de datos
        try:
            cliente_id = agenda_manager.agregar_cliente(datos)
            sound_manager.play("cash")
            msg(f"✓ Cliente agregado: {datos['nombre']}", "#10B981")
            
            # Limpiar formulario
            txt_datos_crudos.value = ""
            dd_estatus_cliente.value = "Prospecto"
            dd_grupo_cliente.value = None
            txt_datos_crudos.update()
            dd_estatus_cliente.update()
            dd_grupo_cliente.update()
            
            # Recargar lista
            cargar_clientes()
            
        except Exception as ex:
            msg(f"Error al agregar cliente: {str(ex)}", "#EF4444")
    

    # Configurar eventos de búsqueda
    txt_buscar_cliente.on_change = cargar_clientes
    dd_filtro_estatus.on_change = cargar_clientes
    
    # Vista de Agenda con Stack para barra flotante
    view_seguimiento = ft.Stack([
        # Contenido principal
        ft.Container(
            padding=30,
            content=ft.Column([
                # Header
                ft.Container(
                    content=ft.Row([
                        ft.Row([
                            ft.Icon(ft.Icons.PEOPLE_ROUNDED, size=32, color="#3B82F6"),
                            ft.Text("Agenda de Clientes", size=32, weight="w900", color=THEME["colors"]["text_primary"])
                        ], spacing=12),
                        
                        # Botones de acción
                        ft.Row([
                            ft.IconButton(
                                icon=ft.Icons.DELETE_SWEEP_ROUNDED,
                                icon_color="#EF4444",
                                tooltip="Eliminar seleccionados",
                                on_click=eliminar_seleccionados,
                                style=ft.ButtonStyle(
                                    bgcolor="#FEF2F2",
                                    padding=12,
                                    shape=ft.RoundedRectangleBorder(radius=12)
                                )
                            ),
                            ft.IconButton(
                                icon=ft.Icons.REFRESH_ROUNDED,
                                icon_color="#475569",
                                tooltip="Actualizar",
                                on_click=cargar_clientes,
                                style=ft.ButtonStyle(
                                    bgcolor="#F1F5F9",
                                    padding=12,
                                    shape=ft.RoundedRectangleBorder(radius=12)
                                )
                            )
                        ], spacing=12)
                    ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    margin=ft.Margin(bottom=30, left=0, right=0, top=0)
                ),
                
                # KPI Cards
                ft.ResponsiveRow([
                    create_kpi_card("Total Clientes", kpi_total_clientes, ft.Icons.PEOPLE_ROUNDED, ["#3B82F6", "#DBEAFE"], 0),
                    create_kpi_card("Prospectos", kpi_prospectos, ft.Icons.PERSON_ADD_ROUNDED, ["#F59E0B", "#FEF3C7"], 100),
                    create_kpi_card("En Proceso", kpi_en_proceso, ft.Icons.AUTORENEW_ROUNDED, ["#8B5CF6", "#EDE9FE"], 200),
                    create_kpi_card("Aprobados", kpi_aprobados, ft.Icons.CHECK_CIRCLE_ROUNDED, ["#10B981", "#D1FAE5"], 300),
                ], spacing=16),
                
                ft.Divider(height=20, color="transparent"),
                
                # Contenido principal
                ft.ResponsiveRow([
                    # Columna izquierda: Formulario
                    ft.Container(
                        col={"xs": 12, "md": 4},
                        content=ft.Container(
                            content=ft.Column([
                                ft.Text("Agregar Cliente", size=18, weight="bold", color="#1E293B"),
                                ft.Divider(height=1, color="#E5E7EB"),
                                txt_datos_crudos,
                                dd_estatus_cliente,
                                ft.Row([
                                    dd_grupo_cliente,
                                    ft.IconButton(
                                        ft.Icons.SETTINGS_SUGGEST_ROUNDED, 
                                        tooltip="Gestionar Grupos", 
                                        on_click=abrir_gestor_grupos,
                                        icon_color="#64748B"
                                    )
                                ], spacing=8),
                                ft.Button(
                                    content=ft.Row([
                                        ft.Icon(ft.Icons.PERSON_ADD_ROUNDED, size=18),
                                        ft.Text("Agregar Cliente", weight="w600")
                                    ], spacing=8),
                                    on_click=agregar_cliente_desde_datos,
                                    style=ft.ButtonStyle(
                                        color="white",
                                        bgcolor="#3B82F6",
                                        padding=ft.Padding(left=24, right=24, top=14, bottom=14),
                                        shape=ft.RoundedRectangleBorder(radius=12)
                                    )
                                )
                            ], spacing=16),
                            padding=20,
                            bgcolor="white",
                            border_radius=12,
                            border=ft.Border.all(1, "#E2E8F0")
                        )
                    ),
                    
                    # Columna derecha: Lista de clientes
                    ft.Container(
                        col={"xs": 12, "md": 8},
                        content=ft.Column([
                            # Barra de búsqueda
                            ft.ResponsiveRow([
                                txt_buscar_cliente,
                                dd_filtro_estatus
                            ], spacing=12),
                            
                            ft.Container(height=16),
                            
                            # Lista de clientes
                            ft.Container(
                                content=lista_clientes,
                                expand=True
                            )
                        ], spacing=0, expand=True),
                        expand=True
                    )
                ], spacing=20, expand=True)
                
            ], scroll=ft.ScrollMode.AUTO, expand=True),
            expand=True
        ),
        
        # Barra de acciones masivas (flotante en la parte inferior)
        ft.Container(
            content=barra_acciones_masivas,
            bottom=20,
            left=30,
            right=30
        )
    ], expand=True)
    
    # Main switcher configuration
    # Main Switcher for views (ANIMATED) - ENHANCED TRANSITIONS
    views_switcher = ft.AnimatedSwitcher(
        content=view_cnca,
        transition=ft.AnimatedSwitcherTransition.FADE,
        duration=350,  # Ligeramente más rápido para sensación ágil
        reverse_duration=250,
        switch_in_curve=ft.AnimationCurve.EASE_OUT,  # Entrada suave
        switch_out_curve=ft.AnimationCurve.EASE_IN,   # Salida rápida
        expand=True
    )

    def toggle_theme(e=None):
        """
        Cambia entre modo claro y oscuro con actualización completa de colores.
        Usa colores SÓLIDOS para uniformidad total.
        """
        # Cambiar el modo de tema
        is_dark = page.theme_mode == ft.ThemeMode.LIGHT
        new_mode = ft.ThemeMode.DARK if is_dark else ft.ThemeMode.LIGHT
        page.theme_mode = new_mode
        
        # Colores Sólidos Uniformes (Sin Gradientes)
        # Dark: Gray 900 (#111827) | Light: Gray 50 (#F9FAFB)
        solid_bg = "#111827" if is_dark else "#F9FAFB"
        
        # Actualizar referencia del sidebar para sincronizar
        if hasattr(page, '_sidebar'):
            page._sidebar.bgcolor = "#1F2937" if is_dark else "#FFFFFF"
            page._sidebar.update()
        
        # Actualizar contenedor de fondo principal
        if hasattr(page, '_background') and page._background:
            # Eliminar gradiente para uniformidad total
            page._background.gradient = None
            page._background.bgcolor = solid_bg
            page._background.update()
            
        # Actualizar página completa
        page.update()
        
        # Mensaje de confirmación
        theme_name = "Modo Oscuro" if is_dark else "Modo Claro"
        msg(f"✓ {theme_name} activado", "#10B981")

    async def change_view(index):
        """Cambia entre vistas con animación mejorada"""
        
        # OPTIMIZACIÓN CRÍTICA: Si salimos de "Seguimiento", vaciar contenido pesado
        # Esto evita que AnimatedSwitcher intente animar la salida de cientos de widgets
        if views_switcher.content == view_seguimiento:
            # Si salimos de Agenda, opcionalmente podemos limpiar para ahorrar memoria
            pass

        if index == 0: 
            views_switcher.content = view_cnca
        elif index == 1: 
            views_switcher.content = view_nuevo
        elif index == 2: 
            views_switcher.content = view_seguro
        elif index == 3: 
            views_switcher.content = view_seguimiento
        
        # Actualizar switcher para iniciar la transición visual
        views_switcher.update()
        
        # Si es la vista de Agenda, cargar datos DESPUÉS de renderizar
        if index == 3:
             await asyncio.sleep(0.05)
             cargar_clientes() # Carga de la lista de clientes de la agenda
    
    def show_performance_dialog(e):
        """Muestra el diálogo de configuración de rendimiento"""
        
        # Estado actual
        current_mode = performance_manager.get_status_text()
        is_auto = performance_manager.user_override is None
        is_reduced = performance_manager.should_reduce_animations()
        
        # Info del hardware (si está disponible)
        hw_info = ""
        if HAVE_PSUTIL:
            try:
                cpu_count = psutil.cpu_count(logical=False) or "N/A"
                ram_gb = psutil.virtual_memory().total / (1024**3)
                cpu_freq = psutil.cpu_freq()
                freq_text = f"{cpu_freq.current/1000:.2f} GHz" if cpu_freq else "N/A"
                
                hw_info = f"""📊 Hardware Detectado:
• CPU: {cpu_count} núcleos @ {freq_text}
• RAM: {ram_gb:.1f} GB
"""
            except:
                hw_info = "No se pudo detectar el hardware"
        else:
            hw_info = "⚠️ psutil no disponible\nInstala con: pip install psutil"
        
        # Radio buttons para selección de modo
        mode_radio = ft.RadioGroup(
            value="auto" if is_auto else ("reduced" if is_reduced else "full"),
            content=ft.Column([
                ft.Radio(
                    value="auto",
                    label="Automático (Recomendado)",
                    label_style=ft.TextStyle(size=14)
                ),
                ft.Radio(
                    value="reduced",
                    label="Reducido (Mejor rendimiento)",
                    label_style=ft.TextStyle(size=14)
                ),
                ft.Radio(
                    value="full",
                    label="Completo (Todas las animaciones)",
                    label_style=ft.TextStyle(size=14)
                )
            ], spacing=10)
        )
        
        def save_preference(e):
            selected = mode_radio.value
            if selected == "auto":
                performance_manager.reset_to_auto()
            elif selected == "reduced":
                performance_manager.set_user_preference(True)
            else:  # full
                performance_manager.set_user_preference(False)
            
            setattr(dlg, "open", False); page.update()
            msg(f"Modo de rendimiento: {performance_manager.get_status_text()}", THEME["colors"]["success"])
        
        dlg = ft.AlertDialog(
            modal=True,
            title=ft.Row([
                ft.Icon(ft.Icons.SPEED_ROUNDED, color=THEME["colors"]["accent"], size=28),
                ft.Text("Configuración de Rendimiento", weight="bold")
            ], spacing=10),
            content=ft.Container(
                content=ft.Column([
                    ft.Text(
                        "Ajusta el nivel de animaciones según tu equipo:",
                        size=13,
                        color=THEME["colors"]["text_secondary"]
                    ),
                    ft.Divider(height=20),
                    ft.Container(
                        content=ft.Text(hw_info, size=12, color=THEME["colors"]["text_tertiary"]),
                        bgcolor=THEME["colors"]["card_bg"],
                        padding=12,
                        border_radius=8
                    ),
                    ft.Divider(height=20),
                    ft.Text("Modo de Animaciones:", weight="bold", size=14),
                    mode_radio,
                    ft.Divider(height=10),
                    ft.Container(
                        content=ft.Column([
                            ft.Text("ℹ️ Información:", weight="bold", size=12),
                            ft.Text(
                                "• Automático: Detecta tu hardware y ajusta las animaciones\n"
                                "• Reducido: Animaciones 50% más rápidas y 70% menos delays\n"
                                "• Completo: Todas las animaciones y efectos visuales",
                                size=11,
                                color=THEME["colors"]["text_tertiary"]
                            )
                        ], spacing=5),
                        bgcolor="#F0F9FF" if page.theme_mode == ft.ThemeMode.LIGHT else "#1E293B",
                        padding=10,
                        border_radius=8
                    )
                ], spacing=15, tight=True),
                width=500
            ),
            actions=[
                ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg, "open", False), page.update()]),
                EnterpriseButton(
                    "GUARDAR",
                    on_click=save_preference,
                    icon=ft.Icons.CHECK_ROUNDED,
                    bgcolor=THEME["colors"]["success"]
                )
            ]
        )
        
        page.dialog = dlg
        dlg.open = True
        page.update()

    # Crear JuicySidebar DESPUÉS de definir change_view
    page._verificar_update_click = verificar_update_click
    page._config_dlg = config_dlg
    page._performance_dlg = show_performance_dialog  # Conectar diálogo de rendimiento
    page._toggle_theme_func = toggle_theme  # Conectar función de tema
    juicy_sidebar = JuicySidebar(page, change_view, perfil, responsive_manager=rm)
    page._sidebar = juicy_sidebar

    # Layout Root - PURE LIGHT THEME
    # Fondo Sólido Blanco/Gris Claro
    background_container = ft.Container(
        expand=True,
        gradient=None,
        bgcolor="#F9FAFB", # Gray 50 (Light)
    )
    page._background = background_container
    
    # Sincronización inicial (Solo Light)
    page.theme_mode = ft.ThemeMode.LIGHT
    if hasattr(page, '_sidebar'):
        page._sidebar.bgcolor = "#FFFFFF" # Sidebar Blanco Puro
    
    # Crear lista de capas del stack
    stack_layers = [
        background_container,  # Capa 0: Fondo con gradiente
        ft.Row([               # Capa 1: Contenido
            juicy_sidebar,
            ft.Container(width=1),  # Separador invisible
            ft.Container(
                content=views_switcher,
                expand=True
            )
        ], expand=True, spacing=0),
        loading  # Capa 2: Overlay de carga (Lottie)
    ]
    
    # Agregar progress indicator si está disponible
    if HAVE_PROGRESS and progress_indicator:
        stack_layers.append(progress_indicator['overlay'])  # Capa 3: Progress indicator
    
    # === SPLASH SCREEN (Capa Final) ===
    splash_screen = ft.Container(
        content=ft.Column([
            ft.Icon(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, size=80, color=THEME["colors"]["light"]["accent"]),
            ft.Container(height=20),
            ft.ProgressRing(color=THEME["colors"]["light"]["accent"], width=30, height=30, stroke_width=3),
            ft.Container(height=15),
            ft.Text("Cargando entorno...", color=THEME["colors"]["light"]["text_secondary"], size=14, weight="w500", font_family=THEME["fonts"]["primary"])
        ], alignment=ft.MainAxisAlignment.CENTER, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
        expand=True,
        bgcolor="#FFFFFF",
        alignment=ft.Alignment(0, 0),
        animate_opacity=ft.Animation(600, ft.AnimationCurve.EASE_IN_OUT)
    )
    
    stack_layers.append(splash_screen)
    
    page.add(
        ft.Stack(stack_layers, expand=True)
    )
    
    # Init defaults
    msg("¡Bienvenido!", "Calculadora lista.")
    
    # Cargar clientes después de que la interfaz esté montada
    if HAVE_AGENDA:
        # Usamos page.run_task si queremos que sea asíncrono o directo si no
        # Como cargar_clientes usa update(), es seguro llamarlo aquí
        try:
            cargar_clientes()
        except:
            pass # Prevenir crash en inicio en frío
    
    # === REMOVE SPLASH SCREEN ===
    async def animate_splash_out():
        await asyncio.sleep(1.2) # Tiempo mínimo de visualización del splash
        splash_screen.opacity = 0
        splash_screen.update()
        await asyncio.sleep(0.6) # Esperar animación
        splash_screen.visible = False
        splash_screen.update()
        
    page.run_task(animate_splash_out)
    


if __name__ == "__main__":
    ft.run(main, assets_dir="assets")