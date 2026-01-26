"""
PREMIUM_DESIGN_SYSTEM.py
Sistema de Diseño Premium para ConsuCalcu Enterprise
Estética: Financial Glassmorphism 2.0

Autor: Lead Product Designer & Senior Flet Developer
Fecha: 2026-01-03
"""

import flet as ft
from typing import Dict, Optional, Callable

# ==========================================
# 1. THEME SYSTEM - FINANCIAL GLASSMORPHISM 2.0
# ==========================================

class PremiumTheme:
    """
    Sistema de diseño premium con paleta Lime & Navy
    y efectos de glassmorphism avanzados.
    """
    
    # Paleta de Colores Premium - STRICT LIGHT MODE
    # ESTÉTICA: Limpia, Blanca, Corporativa
    COLORS = {
        # Fondos Claros
        "gray_50": "#F9FAFB",        # Fondo Principal (Page)
        "white": "#FFFFFF",          # Superficie (Cards/Sidebar)
        "gray_100": "#F3F4F6",       # Elementos secundarios
        
        # Bordes y Divisores
        "gray_200": "#E5E7EB",       # Bordes sutiles
        "gray_300": "#D1D5DB",       # Bordes input focus
        
        # Emerald (Acento Profesional)
        "emerald_500": "#10B981",    # Primary Action
        "emerald_600": "#059669",    # Hover Action
        "emerald_50": "#ECFDF5",     # Fondos suaves acento
        
        # Textos (Dark on Light)
        "text_primary": "#111827",   # Gray 900 - Títulos
        "text_secondary": "#4B5563", # Gray 600 - Cuerpos
        "text_tertiary": "#9CA3AF",  # Gray 400 - Help text
        
        # Neutrales Glassmorphism
        "glass_white": "#FFFFFFCC",  # 80% opacity
        "glass_border": "#E5E7EB80", # 50% opacity
        
        # Estados Semánticos
        "success": "#10B981",        # Emerald
        "warning": "#F59E0B",        # Amber
        "error": "#EF4444",          # Red
        "info": "#3B82F6",           # Blue
        
        # Mapeo Legacy (Para compatibilidad y evitar KeyError)
        "glass_dark": "#0000000D",   # 5% Black (Sutil)
        "glass_lime": "#FFFFFF00",   # Transparente (Eliminado efecto lime)
        "frosted": "#FFFFFFCC",      # Blanco translúcido
        "slate_900": "#FFFFFF",      # Reemplaza Dark Background
        "slate_800": "#FFFFFF",      # Reemplaza Dark Card
        "slate_700": "#E5E7EB",      # Reemplaza Dark Border
        "gray_900": "#FFFFFF",       # Reemplaza Dark Background
        "gray_800": "#FFFFFF",       # Reemplaza Dark Card
        "gray_700": "#E5E7EB",       # Reemplaza Dark Border
        "text_primary_dark": "#111827", # Ahora es oscuro
        "text_primary_light": "#111827",
        "text_secondary_dark": "#4B5563",
        "text_secondary_light": "#4B5563",
    }
    
    # Tipografía Premium (Outfit)
    TYPOGRAPHY = {
        "display": {
            "size": 56,
            "weight": ft.FontWeight.W_900,
        },
        "h1": {
            "size": 40,
            "weight": ft.FontWeight.W_900,
        },
        "h2": {
            "size": 32,
            "weight": ft.FontWeight.W_700,
        },
        "h3": {
            "size": 24,
            "weight": ft.FontWeight.W_700,
        },
        "body": {
            "size": 16,
            "weight": ft.FontWeight.W_400,
        },
        "label": {
            "size": 14,
            "weight": ft.FontWeight.W_700,
        },
        "caption": {
            "size": 12,
            "weight": ft.FontWeight.W_400,
        },
    }
    
    # Sombras Premium
    SHADOWS = {
        "glow_emerald": ft.BoxShadow(
            blur_radius=20,
            spread_radius=0,
            color="#10B98140",  # Emerald con 25% opacity
            offset=ft.Offset(0, 8),
        ),
        "elevation_1": ft.BoxShadow(
            blur_radius=10,
            spread_radius=-2,
            color="#00000015",
            offset=ft.Offset(0, 4),
        ),
        "elevation_2": ft.BoxShadow(
            blur_radius=20,
            spread_radius=-4,
            color="#00000020",
            offset=ft.Offset(0, 8),
        ),
        "elevation_3": ft.BoxShadow(
            blur_radius=30,
            spread_radius=-6,
            color="#00000030",
            offset=ft.Offset(0, 12),
        ),
    }
    
    @staticmethod
    def get_glass_surface(is_dark: bool = False) -> Dict:
        """
        Retorna propiedades para una superficie Clean Light.
        Se ignora is_dark para forzar uniformidad.
        """
        return {
            "bgcolor": PremiumTheme.COLORS["white"], # Fondo blanco sólido
            "gradient": None, # Eliminamos gradiente para diseño más limpio
            "border": ft.Border.all(1, PremiumTheme.COLORS["gray_200"]),
            "border_radius": 16, # Bordes menos agresivos
            "shadow": PremiumTheme.SHADOWS["elevation_1"]
        }


# ==========================================
# 2. PREMIUM COMPONENTS
# ==========================================

class GlassCard(ft.Container):
    """
    Tarjeta con efecto glassmorphism premium.
    """
    def __init__(
        self,
        content: ft.Control,
        is_dark: bool = False,
        elevation: int = 2,
        **kwargs
    ):
        glass_props = PremiumTheme.get_glass_surface(is_dark)
        
        super().__init__(
            content=content,
            gradient=glass_props["gradient"],
            border=glass_props["border"],
            border_radius=glass_props["border_radius"],
            padding=24,
            shadow=PremiumTheme.SHADOWS[f"elevation_{elevation}"],
            animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            **kwargs
        )


def create_liquidity_bar(value: float = 0, max_value: float = 100, height: int = 12):
    """
    Crea una barra de progreso premium para visualizar liquidez.
    Retorna un diccionario con el container y funciones de actualización.
    """
    # Calcular porcentaje inicial
    percentage = min(max(abs(value) / max_value if max_value > 0 else 0, 0), 1)
    
    # Color según estado
    is_positive = value >= 0
    bar_color = PremiumTheme.COLORS["emerald_500"] if is_positive else PremiumTheme.COLORS["error"]
    
    # Barra de progreso (mutable)
    progress_bar = ft.Container(
        width=percentage * 300,  # Ancho fijo de 300px
        height=height,
        gradient=ft.LinearGradient(
            colors=[bar_color, bar_color + "CC"],
            begin=ft.Alignment(-1, 0),
            end=ft.Alignment(1, 0),
        ),
        border_radius=height // 2,
        animate=ft.Animation(600, ft.AnimationCurve.EASE_OUT),
    )
    
    # Container principal
    container = ft.Container(
        content=ft.Stack([
            # Fondo
            ft.Container(
                height=height,
                bgcolor=PremiumTheme.COLORS["glass_dark"],
                border_radius=height // 2,
            ),
            # Barra de progreso
            progress_bar,
        ]),
    )
    
    # Función de actualización
    def update_bar(new_value: float, new_max: float = None):
        """Actualiza la barra con nuevos valores."""
        nonlocal value, max_value
        value = new_value
        if new_max is not None:
            max_value = new_max
        
        # Recalcular
        percentage = min(max(abs(value) / max_value if max_value > 0 else 0, 0), 1)
        is_positive = value >= 0
        bar_color = PremiumTheme.COLORS["emerald_500"] if is_positive else PremiumTheme.COLORS["error"]
        
        # Actualizar barra
        progress_bar.width = percentage * 300
        progress_bar.gradient = ft.LinearGradient(
            colors=[bar_color, bar_color + "CC"],
            begin=ft.Alignment(-1, 0),
            end=ft.Alignment(1, 0),
        )
        progress_bar.update()
    
    return {
        "container": container,
        "update": update_bar,
        "progress_bar": progress_bar,
    }


class SelectableChip(ft.Container):
    """
    Chip seleccionable premium para opciones (ej. plazos).
    """
    def __init__(
        self,
        label: str,
        value: any,
        is_selected: bool = False,
        on_select: Optional[Callable] = None,
        **kwargs
    ):
        self.label = label
        self.value = value
        self.is_selected = is_selected
        self.on_select_callback = on_select
        
        super().__init__(
            content=ft.Text(
                label,
                size=14,
                weight=ft.FontWeight.W_600,
                color="white" if is_selected else PremiumTheme.COLORS["text_secondary_light"],
            ),
            padding=ft.Padding.symmetric(horizontal=20, vertical=12),
            border_radius=20,
            gradient=ft.LinearGradient(
                colors=[
                    PremiumTheme.COLORS["emerald_500"],
                    PremiumTheme.COLORS["emerald_600"],
                ]
            ) if is_selected else None,
            bgcolor=PremiumTheme.COLORS["glass_dark"] if not is_selected else None,
            border=ft.Border.all(
                1,
                PremiumTheme.COLORS["emerald_500"] if is_selected else PremiumTheme.COLORS["glass_white"]
            ),
            shadow=PremiumTheme.SHADOWS["glow_lime"] if is_selected else None,
            animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
            on_click=self._handle_click,
            **kwargs
        )
    
    def _handle_click(self, e):
        """Maneja el click y notifica al callback."""
        if self.on_select_callback:
            self.on_select_callback(self.value)


class StatusBadge(ft.Container):
    """
    Badge gigante para mostrar estados (Seguros).
    """
    def __init__(
        self,
        status: str,
        status_type: str = "success",  # success, warning, error
        icon: ft.Icons = ft.Icons.CHECK_CIRCLE,
        **kwargs
    ):
        # Mapeo de colores
        color_map = {
            "success": PremiumTheme.COLORS["success"],
            "warning": PremiumTheme.COLORS["warning"],
            "error": PremiumTheme.COLORS["error"],
        }
        
        status_color = color_map.get(status_type, PremiumTheme.COLORS["info"])
        
        super().__init__(
            content=ft.Column([
                ft.Icon(icon, size=64, color=status_color),
                ft.Text(
                    status,
                    size=24,
                    weight=ft.FontWeight.W_700,
                    color=status_color,
                    text_align=ft.TextAlign.CENTER,
                ),
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=16),
            padding=40,
            border_radius=24,
            gradient=ft.LinearGradient(
                begin=ft.Alignment(-1, -1),
                end=ft.Alignment(1, 1),
                colors=[status_color + "20", status_color + "10"],
            ),
            border=ft.Border.all(2, status_color + "40"),
            shadow=ft.BoxShadow(
                blur_radius=30,
                color=status_color + "30",
                offset=ft.Offset(0, 10),
            ),
            **kwargs
        )


class PremiumSlider(ft.Container):
    """
    Slider interactivo premium con etiqueta flotante.
    """
    def __init__(
        self,
        min_value: float = 0,
        max_value: float = 100,
        value: float = 50,
        divisions: int = 10,
        label_format: str = "${:,.0f}",
        on_change: Optional[Callable] = None,
        **kwargs
    ):
        self.min_value = min_value
        self.max_value = max_value
        self.current_value = value
        self.label_format = label_format
        self.on_change_callback = on_change
        
        # Etiqueta flotante
        self.floating_label = ft.Text(
            self.label_format.format(value),
            size=20,
            weight=ft.FontWeight.W_700,
            color=PremiumTheme.COLORS["emerald_500"],
        )
        
        # Slider
        self.slider = ft.Slider(
            min=min_value,
            max=max_value,
            value=value,
            divisions=divisions,
            label="{value}",
            active_color=PremiumTheme.COLORS["emerald_500"],
            inactive_color=PremiumTheme.COLORS["glass_dark"],
            thumb_color=PremiumTheme.COLORS["emerald_500"],
            on_change=self._handle_change,
        )
        
        super().__init__(
            content=ft.Column([
                self.floating_label,
                self.slider,
            ], spacing=12),
            **kwargs
        )
    
    def _handle_change(self, e):
        """Actualiza la etiqueta y notifica al callback."""
        self.current_value = e.control.value
        self.floating_label.value = self.label_format.format(self.current_value)
        self.floating_label.update()
        
        if self.on_change_callback:
            self.on_change_callback(self.current_value)


# ==========================================
# 3. HELPER FUNCTIONS
# ==========================================

def create_premium_text(
    text: str,
    style: str = "body",
    color: Optional[str] = None,
    **kwargs
) -> ft.Text:
    """
    Crea un Text con la tipografía premium del sistema.
    
    Args:
        text: Texto a mostrar
        style: Estilo tipográfico (display, h1, h2, h3, body, label, caption)
        color: Color del texto (opcional)
    """
    typo = PremiumTheme.TYPOGRAPHY.get(style, PremiumTheme.TYPOGRAPHY["body"])
    
    return ft.Text(
        text,
        size=typo["size"],
        weight=typo["weight"],
        color=color,
        **kwargs
    )


def create_glass_container(
    content: ft.Control,
    is_dark: bool = False,
    **kwargs
) -> ft.Container:
    """
    Crea un contenedor con efecto glass rápidamente.
    """
    glass_props = PremiumTheme.get_glass_surface(is_dark)
    
    return ft.Container(
        content=content,
        gradient=glass_props["gradient"],
        border=glass_props["border"],
        border_radius=glass_props["border_radius"],
        padding=20,
        **kwargs
    )
