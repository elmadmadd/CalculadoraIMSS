# ==========================================
# OPTIMIZACIÓN: Virtual Scrolling
# ==========================================
"""
Sistema de scroll virtualizado que renderiza solo los elementos visibles.
Mejora drásticamente el performance con listas largas (>20 items).
"""

import flet as ft
from typing import List, Callable, Any

class VirtualScrollContainer:
    """
    Contenedor con scroll virtualizado que renderiza solo items visibles.
    
    Uso:
        virtual_grid = VirtualScrollContainer(
            items=ofertas_list,
            item_builder=create_fintech_card,
            item_height=180,
            visible_count=6
        )
        page.add(virtual_grid.build())
    """
    
    def __init__(
        self,
        items: List[Any],
        item_builder: Callable[[Any], ft.Control],
        item_height: int = 150,
        visible_count: int = 8,
        buffer_count: int = 2
    ):
        """
        Args:
            items: Lista de datos a renderizar
            item_builder: Función que toma un item y retorna un Control
            item_height: Altura estimada de cada item en pixels
            visible_count: Cantidad de items visibles simultáneamente
            buffer_count: Items extra a renderizar arriba/abajo para scroll suave
        """
        self.items = items
        self.item_builder = item_builder
        self.item_height = item_height
        self.visible_count = visible_count
        self.buffer_count = buffer_count
        
        self.start_index = 0
        self.rendered_controls = []
        self.container = None
        
    def get_visible_range(self):
        """Calcula rango de indices a renderizar"""
        start = max(0, self.start_index - self.buffer_count)
        end = min(len(self.items), self.start_index + self.visible_count + self.buffer_count)
        return start, end
    
    def render_visible_items(self):
        """Renderiza solo los items en el rango visible"""
        start, end = self.get_visible_range()
        
        # Limpiar solo si cambia el rango
        self.rendered_controls.clear()
        
        # Spacer superior (simula items que están arriba)
        top_spacer_height = start * self.item_height
        if top_spacer_height > 0:
            self.rendered_controls.append(
                ft.Container(height=top_spacer_height, bgcolor="transparent")
            )
        
        # Renderizar items visibles
        for i in range(start, end):
            if i < len(self.items):
                control = self.item_builder(self.items[i])
                self.rendered_controls.append(control)
        
        # Spacer inferior (simula items que están abajo)
        bottom_spacer_height = (len(self.items) - end) * self.item_height
        if bottom_spacer_height > 0:
            self.rendered_controls.append(
                ft.Container(height=bottom_spacer_height, bgcolor="transparent")
            )
        
        return self.rendered_controls
    
    def on_scroll(self, e):
        """Callback cuando el usuario hace scroll"""
        if not e.pixels:
            return
        
        # Calcular nuevo índice basado en posición de scroll
        new_start = int(e.pixels / self.item_height)
        
        # Solo actualizar si cambió el rango visible
        if abs(new_start - self.start_index) >= 1:
            self.start_index = new_start
            self.update_content()
    
    def update_content(self):
        """Actualiza el contenido del contenedor"""
        if self.container:
            self.container.controls = self.render_visible_items()
            self.container.update()
    
    def update_items(self, new_items: List[Any]):
        """Actualiza la lista de items y re-renderiza"""
        self.items = new_items
        self.start_index = 0
        self.update_content()
    
    def build(self) -> ft.Control:
        """Construye y retorna el control de Flet"""
        # Columna interna que contiene los items
        self.container = ft.Column(
            controls=self.render_visible_items(),
            spacing=12,
            scroll=None  # El scroll lo maneja el ListView padre
        )
        
        # ListView que maneja el scroll
        total_height = len(self.items) * self.item_height
        
        return ft.Container(
            content=ft.ListView(
                controls=[self.container],
                spacing=0,
                padding=0,
                on_scroll=self.on_scroll,
                auto_scroll=False,
            ),
            expand=True
        )


# ==========================================
# HELPER: Crear Virtual Grid Rápidamente
# ==========================================

def create_virtual_grid(
    items: List[Any],
    builder_func: Callable,
    columns: int = 2,
    item_height: int = 180
) -> ft.Control:
    """
    Helper para crear grid virtualizado con múltiples columnas.
    
    Args:
        items: Lista de items
        builder_func: Función que crea un card desde un item
        columns: Número de columnas en grid
        item_height: Altura de cada row
    
    Returns:
        Control de Flet con grid virtualizado
    """
    # Agrupar items en filas según columnas
    rows = []
    for i in range(0, len(items), columns):
        row_items = items[i:i+columns]
        rows.append(row_items)
    
    # Builder para cada fila
    def row_builder(row_items):
        return ft.ResponsiveRow([
            builder_func(item) for item in row_items
        ], spacing=12)
    
    # Crear virtual scroll con filas
    virtual_scroll = VirtualScrollContainer(
        items=rows,
        item_builder=row_builder,
        item_height=item_height,
        visible_count=6,
        buffer_count=2
    )
    
    return virtual_scroll.build()
