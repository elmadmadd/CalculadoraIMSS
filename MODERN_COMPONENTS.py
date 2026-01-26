"""
MODERN COMPONENTS
=================
Componentes UI modernos y reutilizables para la aplicación.
Diseño premium con glassmorphism, gradientes y animaciones suaves.
"""

import flet as ft

# ==========================================
# SECTION HEADER COMPONENT
# ==========================================

def create_section_header(title: str, subtitle: str = None, icon: str = None, gradient_colors: list = None):
    """
    Crea un encabezado de sección moderno con estilo editorial limpio.
    ACTUALIZADO ENERO 2026: Gradientes removidos para reducir ruido visual.
    
    Args:
        title: Título principal
        subtitle: Subtítulo opcional
        icon: Icono opcional (ft.Icons)
        gradient_colors: DEPRECADO - Se ignora para mantener diseño limpio
    """
    header_content = []
    
    # Fila con icono y título
    title_row = []
    if icon:
        title_row.append(
            ft.Icon(icon, size=32, color="#0A1929")  # Navy 900 siempre
        )
    
    title_col = [
        ft.Text(
            title, 
            size=28, 
            weight=ft.FontWeight.BOLD, 
            color="#0A1929"  # Navy 900 - Sólido y legible
        )
    ]
    
    if subtitle:
        title_col.append(
            ft.Text(
                subtitle, 
                size=14, 
                color="#64748B",  # Gris medio
                opacity=0.9
            )
        )
    
    title_row.append(ft.Column(title_col, spacing=4))
    header_content.append(ft.Row(title_row, spacing=15))
    
    # Diseño limpio sin gradiente
    return ft.Container(
        content=ft.Column(header_content, spacing=10),
        padding=ft.Padding.symmetric(vertical=15),
        border_radius=0,  # Sin bordes redondeados para look editorial
    )


# ==========================================
# STAT CARD COMPONENT (KPI)
# ==========================================

def create_stat_card(
    title: str, 
    value: str, 
    icon: str, 
    gradient_colors: list = None,
    trend: str = None,
    trend_positive: bool = True,
    animate: bool = True  # Nuevo parámetro para control de rendimiento
):
    """
    Crea una tarjeta de estadística moderna.
    """
    if gradient_colors is None:
        gradient_colors = ["#3B82F6", "#2563EB"]
    
    content_items = [
        ft.Row([
            ft.Icon(icon, size=36, color="white"),
            ft.Text(value, size=32, weight=ft.FontWeight.BOLD, color="white")
        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
        ft.Text(title, size=13, color="white", opacity=0.9, weight=ft.FontWeight.W_500)
    ]
    
    if trend:
        trend_color = "#10B981" if trend_positive else "#EF4444"
        trend_icon = ft.Icons.TRENDING_UP if trend_positive else ft.Icons.TRENDING_DOWN
        content_items.append(
            ft.Row([
                ft.Icon(trend_icon, size=16, color=trend_color),
                ft.Text(trend, size=12, color=trend_color, weight=ft.FontWeight.BOLD)
            ], spacing=4)
        )
    
    return ft.Container(
        content=ft.Column(content_items, spacing=8),
        padding=20,
        border_radius=16,
        gradient=ft.LinearGradient(
            begin=ft.Alignment(-1, -1),
            end=ft.Alignment(1, 1),
            colors=gradient_colors
        ),
        shadow=ft.BoxShadow(
            blur_radius=20,
            color="#40000000",
            offset=ft.Offset(0, 8)
        ),
        # Animación condicional
        animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT) if animate else None
    )


# ==========================================
# INFO CARD COMPONENT
# ==========================================

def create_info_card(
    title: str,
    value: str,
    icon: str = None,
    color: str = "#3B82F6",
    bg_color: str = "#EFF6FF",
    size: str = "medium",
    animate: bool = False # Por defecto desactivado para listas largas
):
    """
    Crea una tarjeta de información con diseño limpio.
    """
    sizes = {
        "small": {"title": 11, "value": 16, "padding": 12},
        "medium": {"title": 12, "value": 20, "padding": 15},
        "large": {"title": 14, "value": 24, "padding": 20}
    }
    
    s = sizes.get(size, sizes["medium"])
    
    content = []
    
    if icon:
        content.append(
            ft.Icon(icon, size=s["value"], color=color)
        )
    
    content.append(
        ft.Column([
            ft.Text(title, size=s["title"], color="#64748B", weight=ft.FontWeight.W_500),
            ft.Text(value, size=s["value"], color="#0F172A", weight=ft.FontWeight.BOLD)
        ], spacing=4)
    )
    
    return ft.Container(
        content=ft.Row(content, spacing=12, alignment=ft.MainAxisAlignment.CENTER),
        padding=s["padding"],
        border_radius=12,
        bgcolor=bg_color,
        border=ft.Border.all(1, color + "20"),
        shadow=ft.BoxShadow(
            blur_radius=8,
            color="#10000000",
            offset=ft.Offset(0, 2)
        ),
        animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT) if animate else None
    )


# ==========================================
# FEATURE CARD COMPONENT
# ==========================================

def create_feature_card(
    title: str,
    description: str,
    icon: str,
    color: str = "#10B981",
    on_click = None
):
    """
    Crea una tarjeta de característica con hover effect.
    
    Args:
        title: Título de la característica
        description: Descripción
        icon: Icono
        color: Color del acento
        on_click: Función callback al hacer click
    """
    return ft.Container(
        content=ft.Column([
            ft.Container(
                content=ft.Icon(icon, size=32, color=color),
                width=60,
                height=60,
                border_radius=30,
                bgcolor=color + "20",
                alignment=ft.Alignment(0, 0)
            ),
            ft.Text(title, size=16, weight=ft.FontWeight.BOLD, color="#0F172A"),
            ft.Text(description, size=13, color="#64748B", max_lines=2)
        ], spacing=12, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
        padding=20,
        border_radius=16,
        bgcolor="#FFFFFF",
        border=ft.Border.all(1, "#E2E8F0"),
        shadow=ft.BoxShadow(
            blur_radius=12,
            color="#20000000",
            offset=ft.Offset(0, 4)
        ),
        on_click=on_click,
        animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
        ink=True
    )


# ==========================================
# GLASSMORPHIC CARD
# ==========================================

def create_glass_card(
    content,
    padding: int = 20,
    blur: int = 10,
    opacity: float = 0.7
):
    """
    Crea una tarjeta con efecto glassmorphism.
    
    Args:
        content: Contenido de la tarjeta
        padding: Padding interno
        blur: Nivel de blur
        opacity: Opacidad del fondo
    """
    return ft.Container(
        content=content,
        padding=padding,
        border_radius=16,
        bgcolor=f"#FFFFFF{int(opacity * 255):02x}",
        border=ft.Border.all(1, "#FFFFFF40"),
        shadow=ft.BoxShadow(
            blur_radius=24,
            color="#30000000",
            offset=ft.Offset(0, 8)
        ),
        blur=ft.Blur(blur, blur)
    )


# ==========================================
# EMPTY STATE COMPONENT
# ==========================================

def create_empty_state(
    icon: str,
    title: str,
    description: str,
    action_text: str = None,
    on_action = None,
    icon_color: str = "#94A3B8"
):
    """
    Crea un estado vacío elegante.
    
    Args:
        icon: Icono a mostrar
        title: Título del mensaje
        description: Descripción
        action_text: Texto del botón de acción (opcional)
        on_action: Callback del botón
        icon_color: Color del icono
    """
    content = [
        ft.Container(
            content=ft.Icon(icon, size=80, color=icon_color),
            padding=40,
            border_radius=100,
            bgcolor="#F1F5F9"
        ),
        ft.Text(title, size=20, weight=ft.FontWeight.BOLD, color="#0F172A"),
        ft.Text(
            description, 
            size=14, 
            color="#64748B", 
            text_align=ft.TextAlign.CENTER,
            max_lines=3
        )
    ]
    
    if action_text and on_action:
        content.append(
            ft.Button(
                text=action_text,
                on_click=on_action,
                bgcolor="#10B981",
                color="white",
                style=ft.ButtonStyle(
                    shape=ft.RoundedRectangleBorder(radius=12),
                    padding=ft.Padding.symmetric(horizontal=24, vertical=12)
                )
            )
        )
    
    return ft.Container(
        content=ft.Column(
            content,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20
        ),
        alignment=ft.Alignment(0, 0),
        expand=True
    )


# ==========================================
# DIVIDER WITH TEXT
# ==========================================

def create_divider_with_text(text: str, color: str = "#CBD5E1"):
    """
    Crea un divisor con texto en el centro.
    
    Args:
        text: Texto a mostrar
        color: Color del divisor
    """
    return ft.Row([
        ft.Container(
            content=ft.Divider(color=color, height=1),
            expand=True
        ),
        ft.Text(text, size=12, color="#94A3B8", weight=ft.FontWeight.W_500),
        ft.Container(
            content=ft.Divider(color=color, height=1),
            expand=True
        )
    ], spacing=12, alignment=ft.MainAxisAlignment.CENTER)


# ==========================================
# PROGRESS BAR WITH LABEL
# ==========================================

def create_labeled_progress(
    label: str,
    value: float,
    max_value: float = 100,
    color: str = "#10B981",
    show_percentage: bool = True
):
    """
    Crea una barra de progreso con etiqueta.
    
    Args:
        label: Etiqueta de la barra
        value: Valor actual
        max_value: Valor máximo
        color: Color de la barra
        show_percentage: Mostrar porcentaje
    """
    percentage = (value / max_value) * 100 if max_value > 0 else 0
    
    return ft.Column([
        ft.Row([
            ft.Text(label, size=13, color="#64748B", weight=ft.FontWeight.W_500),
            ft.Text(
                f"{percentage:.0f}%" if show_percentage else f"{value}/{max_value}",
                size=13,
                color="#0F172A",
                weight=ft.FontWeight.BOLD
            )
        ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
        ft.ProgressBar(
            value=percentage / 100,
            color=color,
            bgcolor=color + "20",
            border_radius=4,
            height=8
        )
    ], spacing=8)


# ==========================================
# BADGE COMPONENT
# ==========================================

def create_badge(
    text: str,
    variant: str = "primary",
    size: str = "medium"
):
    """
    Crea un badge/etiqueta moderna.
    
    Args:
        text: Texto del badge
        variant: Variante de color ("primary", "success", "warning", "error", "info")
        size: Tamaño ("small", "medium", "large")
    """
    # CYBERPUNK FINANCIAL 2026: Badges con fondos oscuros y texto neón
    variants = {
        "primary": {"bg": "#1E293B", "text": "#84CC16"},      # Slate 800 + Lime
        "success": {"bg": "#1E293B", "text": "#D9F99D"},      # Slate 800 + Lime brillante
        "warning": {"bg": "#1E293B", "text": "#FCA5A5"},      # Slate 800 + Red pastel
        "error": {"bg": "#1E293B", "text": "#FCA5A5"},        # Slate 800 + Red pastel
        "info": {"bg": "#1E293B", "text": "#84CC16"}          # Slate 800 + Lime
    }
    
    sizes = {
        "small": {"text": 10, "padding": 4},
        "medium": {"text": 11, "padding": 6},
        "large": {"text": 12, "padding": 8}
    }
    
    v = variants.get(variant, variants["primary"])
    s = sizes.get(size, sizes["medium"])
    
    return ft.Container(
        content=ft.Text(
            text,
            size=s["text"],
            color=v["text"],
            weight=ft.FontWeight.BOLD
        ),
        padding=ft.Padding.symmetric(horizontal=s["padding"] * 2, vertical=s["padding"]),
        border_radius=12,
        bgcolor=v["bg"],
        border=ft.Border.all(1, "#84CC1620")  # Borde Lime sutil
    )
