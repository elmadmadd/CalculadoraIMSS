import flet as ft
from ui.theme import THEME

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
            options=[ft.dropdown.Option(o) if isinstance(o, str) else o for o in options],
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
        try:
            if self.page: self.update()
        except: pass

class JuicyProviderDropdown(JuicyDropdown):
    """
    Componente Dropdown especializado para proveedores financieros.
    Hereda todo el estilo y comportamiento dinámico de JuicyDropdown.
    """
    def __init__(self, label, options, on_change=None, value=None, expand=False):
        # Adaptador de firmas si fuera necesario, pero son compatibles
        super().__init__(label=label, options=options, on_change=on_change, value=value, expand=expand)
