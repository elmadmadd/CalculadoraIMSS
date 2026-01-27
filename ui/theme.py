"""
ui/theme.py
Sistema de Diseño Premium para ConsuCalcu Enterprise
Estética: Financial Glassmorphism 2.0 & Dynamic Themes

Autor: Lead Product Designer & Senior Flet Developer
Fecha: 2026-01-03
"""

import flet as ft
from typing import Dict, Optional, Callable

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

def get_theme_color(page, color_key):
    """
    Obtiene el color correcto según el tema activo (light/dark).
    """
    if not page:
        return THEME["colors"]["light"].get(color_key, "#FFFFFF")

    theme_mode = "dark" if page.theme_mode == ft.ThemeMode.DARK else "light"
    return THEME["colors"][theme_mode].get(color_key, "#FFFFFF")


class ThemeManager:
    """
    Gestor de tema que proporciona acceso dinámico a los colores según el modo activo.
    """
    _current_mode = "light"

    @classmethod
    def update_theme_mode(cls, is_dark):
        """Actualiza el modo de tema actual y los colores en THEME."""
        cls._current_mode = "dark" if is_dark else "light"

        # Actualizar las claves de acceso directo en THEME["colors"]
        current_colors = THEME["colors"][cls._current_mode]

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

# Colores de Marca (Financieras) - Tonos Pastel/Suaves
BRAND_COLORS = {
    "CONSUBANCO": {"bg": "#DBEAFE", "text": "#1E40AF", "border": "#93C5FD"}, # Azul
    "CONSUPAGO":  {"bg": "#DCFCE7", "text": "#166534", "border": "#86EFAC"}, # Verde
    "MASNOMINA":  {"bg": "#FEE2E2", "text": "#991B1B", "border": "#FCA5A5"}, # Rojo
    "OPCIPRES":   {"bg": "#FEF3C7", "text": "#92400E", "border": "#FCD34D"}, # Amarillo
    "DEFAULT":    {"bg": "#F3F4F6", "text": "#374151", "border": "#D1D5DB"}  # Gris
}
