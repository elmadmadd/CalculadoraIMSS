import flet as ft

# ==========================================
# RESPONSIVE MANAGER
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
