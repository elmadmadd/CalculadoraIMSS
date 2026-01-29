"""
PROGRESS INDICATOR COMPONENT
============================
Indicador de progreso con pasos informativos.
Compatible con Flet sin UserControl.
"""

import flet as ft
import time

def create_progress_overlay(page: ft.Page):
    """
    Crear overlay de progreso con pasos.
    
    Returns:
        dict con métodos: show(), hide(), next_step(), set_step()
    """
    
    current_step = 0
    total_steps = 0
    start_time = 0
    steps = []
    
    # Texto del paso actual
    step_text = ft.Text(
        "",
        size=14,
        weight=ft.FontWeight.BOLD,
        color="white"
    )
    
    # Barra de progreso
    progress_bar = ft.ProgressBar(
        width=400,
        height=8,
        color="green",
        bgcolor="#FFFFFF40"
    )
    
    # Tiempo estimado
    time_text = ft.Text(
        "",
        size=12,
        color="#FFFFFFB3"
    )
    
    # Container principal
    container = ft.Container(
        content=ft.Column([
            step_text,
            progress_bar,
            time_text
        ], spacing=10, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
        bgcolor="#1E293BF2",  # Blue grey 900 con opacidad
        padding=30,
        border_radius=16,
        width=500,
        visible=False
    )
    
    # Overlay
    overlay = ft.Container(
        content=ft.Stack([
            ft.Container(
                bgcolor="#000000B3",  # Black con opacidad
                expand=True
            ),
            ft.Container(
                content=container,
                alignment=ft.Alignment(0, 0),
                expand=True
            )
        ]),
        visible=False,
        expand=True
    )
    
    def show(step_list: list):
        """Mostrar indicador de progreso"""
        nonlocal current_step, total_steps, start_time, steps
        
        total_steps = len(step_list)
        steps = step_list
        current_step = 0
        start_time = time.time()
        
        overlay.visible = True
        container.visible = True
        
        _update_display()
        
        if page:
            page.update()
    
    def hide():
        """Ocultar indicador"""
        overlay.visible = False
        container.visible = False
        if page:
            page.update()
    
    def next_step():
        """Avanzar al siguiente paso"""
        nonlocal current_step
        if current_step < total_steps - 1:
            current_step += 1
            _update_display()
            if page:
                page.update()
    
    def set_step(step_index: int):
        """Ir a un paso específico"""
        nonlocal current_step
        if 0 <= step_index < total_steps:
            current_step = step_index
            _update_display()
            if page:
                page.update()
    
    def _update_display():
        """Actualizar visualización"""
        # Actualizar texto del paso
        if current_step < len(steps):
            step_text.value = f"⏳ {steps[current_step]}"
        
        # Actualizar barra de progreso
        progress = (current_step + 1) / total_steps if total_steps > 0 else 0
        progress_bar.value = progress
        
        # Calcular tiempo estimado
        elapsed = time.time() - start_time
        if current_step > 0:
            avg_time_per_step = elapsed / (current_step + 1)
            remaining_steps = total_steps - (current_step + 1)
            estimated_remaining = avg_time_per_step * remaining_steps
            
            if estimated_remaining < 1:
                time_text.value = "Casi listo..."
            else:
                time_text.value = f"Tiempo estimado: {estimated_remaining:.1f}s"
        else:
            time_text.value = "Calculando..."
    
    return {
        'overlay': overlay,
        'show': show,
        'hide': hide,
        'next_step': next_step,
        'set_step': set_step
    }


def create_cnca_progress_steps():
    """Pasos predefinidos para CNCA"""
    return [
        "Validando deudas...",
        "Calculando OPCIPRES...",
        "Calculando CONSUBANCO...",
        "Calculando MASNOMINA...",
        "Calculando CONSUPAGO...",
        "Optimizando combinaciones...",
        "Generando resultados..."
    ]


def create_nuevo_progress_steps():
    """Pasos predefinidos para Nuevo"""
    return [
        "Validando datos...",
        "Calculando opciones...",
        "Aplicando filtros...",
        "Ordenando resultados..."
    ]


def create_seguro_progress_steps():
    """Pasos predefinidos para Seguro"""
    return [
        "Validando edad...",
        "Consultando tarifas...",
        "Calculando prima...",
        "Generando dictamen..."
    ]
