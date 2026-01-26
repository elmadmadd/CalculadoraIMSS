"""
SKELETON_LOADING.py
Componentes de skeleton loading para feedback visual durante cálculos
"""
import flet as ft

def create_skeleton_offer_card():
    """Crea una tarjeta skeleton animada para carga (Efecto Shimmer Simulado)"""
    shim_col = "#E2E8F0" # Slate 200 - Color base del skeleton
    bg_col = "#FFFFFF"   # Fondo blanco para la card
    
    return ft.Container(
        padding=25, # Padding generoso igual que las cards reales
        bgcolor=bg_col,
        border_radius=16,
        border=ft.Border.all(1, "#E5E7EB"),
        shadow=ft.BoxShadow(blur_radius=5, color="#05000000", offset=ft.Offset(0, 2)),
        content=ft.Column([
            # Header sim (Icon + Title)
            ft.Row([
                ft.Container(width=45, height=45, border_radius=12, bgcolor=shim_col),
                ft.Column([
                    ft.Container(width=140, height=18, border_radius=4, bgcolor=shim_col),
                    ft.Container(width=90, height=12, border_radius=4, bgcolor=shim_col),
                ], spacing=6)
            ], spacing=15),
            
            ft.Container(height=20),
            
            # Stats Row 1
            ft.Row([
                ft.Container(width=100, height=14, border_radius=4, bgcolor=shim_col),
                ft.Container(width=120, height=24, border_radius=6, bgcolor=shim_col),
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            ft.Container(height=12),
            
            # Stats Row 2
            ft.Row([
                ft.Container(width=90, height=14, border_radius=4, bgcolor=shim_col),
                ft.Container(width=100, height=20, border_radius=6, bgcolor=shim_col),
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            ft.Divider(height=25, color="#F1F5F9"),
            
            # Footer Actions
            ft.Row([
                ft.Container(width=80, height=14, border_radius=4, bgcolor=shim_col),
                ft.Container(width=24, height=24, border_radius=12, bgcolor=shim_col)
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN)
        ]),
        # Animación de pulso continuo
        animate_opacity=ft.Animation(1200, ft.AnimationCurve.EASE_IN_OUT),
        opacity=0.5  # Opacidad inicial más baja para efecto de pulso
    )

def create_skeleton_grid(count=3):
    """Crea un grid de skeletons para mostrar durante cálculos"""
    return ft.GridView(
        max_extent=360,
        child_aspect_ratio=1.0,
        spacing=20,
        run_spacing=20,
        padding=10,
        controls=[create_skeleton_offer_card() for _ in range(count)]
    )

def create_pulsing_skeleton():
    """Crea un skeleton con efecto de pulso"""
    skeleton = create_skeleton_offer_card()
    
    # Animar opacidad para efecto de pulso
    async def pulse_animation():
        while True:
            skeleton.opacity = 0.3
            skeleton.update()
            await asyncio.sleep(0.8)
            skeleton.opacity = 0.6
            skeleton.update()
            await asyncio.sleep(0.8)
    
    return skeleton

def create_loading_overlay(message="Calculando mejores opciones..."):
    """Crea un overlay de carga con mensaje"""
    return ft.Container(
        content=ft.Column([
            ft.ProgressRing(
                width=40,
                height=40,
                stroke_width=3,
                color="#10B981"
            ),
            ft.Text(
                message,
                size=14,
                color="#64748B",
                weight=ft.FontWeight.W_500
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=16),
        alignment=ft.Alignment(0, 0),
        padding=40,
        bgcolor="#FFFFFF",
        border_radius=16,
        shadow=ft.BoxShadow(
            blur_radius=20,
            color="#20000000",
            offset=ft.Offset(0, 8)
        )
    )
