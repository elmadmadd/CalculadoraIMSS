"""
MINIMAL_BUTTON_COMPONENT.py
Botones Premium con Animaciones Fluidas
Fintech UX 2026 - Light Mode Optimized
"""

import flet as ft

class MinimalButton(ft.Container):
    """
    Botón minimalista moderno con animaciones premium.
    
    Características:
    - Hover con scale animation (1.02x)
    - Ripple effect nativo
    - Sombras dinámicas
    - Transiciones suaves (300ms)
    - 3 variantes: primary, secondary, ghost
    - 3 tamaños: sm, md, lg
    """
    
    def __init__(
        self,
        text: str,
        on_click=None,
        icon=None,
        variant="primary",  # primary, secondary, ghost
        size="md",  # sm, md, lg
        width=None,
        expand=False,
        border_radius=12,
        **kwargs
    ):
        self.text = text
        self.on_click = on_click
        self.icon = icon
        self.variant = variant
        self.size = size
        self.width = width
        self.expand = expand
        self.border_radius = border_radius
        
        super().__init__(**kwargs)
        
        # Construir botón
        self.content = self.build()
    
    def build(self):
        # Colores según variante (Light Mode)
        if self.variant == "primary":
            bg_color = "#10B981"  # Emerald 500
            text_color = "#FFFFFF"
            hover_bg = "#059669"  # Emerald 600
            border_color = None
        elif self.variant == "secondary":
            bg_color = "transparent"
            text_color = "#10B981"
            hover_bg = "#ECFDF510"  # Emerald 50 con 10% opacity
            border_color = "#10B981"
        else:  # ghost
            bg_color = "transparent"
            text_color = "#6B7280"  # Gray 500
            hover_bg = "#F3F4F610"  # Gray 100 con 10% opacity
            border_color = None
        
        # Tamaños
        sizes = {
            "sm": {"height": 36, "padding": 12, "text_size": 13},
            "md": {"height": 44, "padding": 16, "text_size": 14},
            "lg": {"height": 52, "padding": 20, "text_size": 16},
        }
        size_config = sizes[self.size]
        
        # Contenido del botón
        content_items = []
        if self.icon:
            content_items.append(
                ft.Icon(
                    self.icon,
                    size=size_config["text_size"] + 4,
                    color=text_color,
                )
            )
        content_items.append(
            ft.Text(
                self.text,
                size=size_config["text_size"],
                weight=ft.FontWeight.W_600,
                color=text_color,
            )
        )
        
        # Contenedor del botón con animaciones
        self.button_container = ft.Container(
            content=ft.Row(
                content_items,
                alignment=ft.MainAxisAlignment.CENTER,
                spacing=8,
            ),
            bgcolor=bg_color,
            border=ft.Border.all(1.5, border_color) if border_color else None,
            border_radius=self.border_radius,
            padding=ft.Padding.symmetric(
                horizontal=size_config["padding"],
                vertical=0
            ),
            height=size_config["height"],
            width=self.width,
            on_click=self.on_click,
            on_hover=self._on_hover,
            ink=True,  # Ripple effect
            # ANIMACIONES PREMIUM
            animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
            animate_opacity=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
        )
        
        # Guardar colores para hover
        self._default_bg = bg_color
        self._hover_bg = hover_bg
        self._text_color = text_color
        
        return self.button_container
    
    def _on_hover(self, e):
        """Animaciones de hover suaves"""
        if e.data == "true":
            # Hover IN
            self.button_container.bgcolor = self._hover_bg
            self.button_container.scale = 1.02  # Crece ligeramente
            
            # Añadir sombra sutil
            if self.variant == "primary":
                self.button_container.shadow = ft.BoxShadow(
                    blur_radius=12,
                    spread_radius=0,
                    color="#10B98140",  # Emerald con 25% opacity
                    offset=ft.Offset(0, 4),
                )
        else:
            # Hover OUT
            self.button_container.bgcolor = self._default_bg
            self.button_container.scale = 1.0
            self.button_container.shadow = None
        
        self.button_container.update()


# Ejemplo de uso
def ejemplo_botones():
    return ft.Column([
        MinimalButton("Calcular", icon=ft.Icons.CALCULATE, variant="primary", size="lg"),
        ft.Container(height=10),
        MinimalButton("Cancelar", variant="secondary", size="md"),
        ft.Container(height=10),
        MinimalButton("Ayuda", icon=ft.Icons.HELP_OUTLINE, variant="ghost", size="sm"),
    ])
