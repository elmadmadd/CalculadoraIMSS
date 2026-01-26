"""
CALCULATION HISTORY COMPONENT
==============================
Historial de cálculos con búsqueda y restauración.
Compatible con Flet sin UserControl.
"""

import flet as ft
import json
import datetime
from pathlib import Path

class CalculationHistory:
    """
    Gestor de historial de cálculos.
    
    Features:
    - Guardar últimos 50 cálculos
    - Búsqueda por cliente, monto, fecha
    - Restaurar cálculo anterior
    - Exportar historial
    """
    
    def __init__(self, storage_path: str = None):
        if storage_path is None:
            storage_path = Path.home() / ".consucalcu" / "history.json"
        
        self.storage_path = Path(storage_path)
        self.storage_path.parent.mkdir(parents=True, exist_ok=True)
        
        self.max_history = 50
        self.history = self._load_history()
    
    def add_calculation(self, calc_data: dict):
        """Agregar cálculo al historial"""
        # Agregar timestamp si no existe
        if 'timestamp' not in calc_data:
            calc_data['timestamp'] = datetime.datetime.now().isoformat()
        
        # Agregar ID único
        calc_data['id'] = f"{calc_data['tipo']}_{calc_data['timestamp']}"
        
        # Agregar al inicio
        self.history.insert(0, calc_data)
        
        # Mantener solo los últimos N
        self.history = self.history[:self.max_history]
        
        # Guardar
        self._save_history()
    
    def search(self, query: str = "", tipo: str = None):
        """Buscar en el historial"""
        results = self.history.copy()
        
        # Filtrar por tipo
        if tipo and tipo != "TODOS":
            results = [c for c in results if c.get('tipo') == tipo]
        
        # Filtrar por query
        if query:
            query = query.lower()
            filtered = []
            for calc in results:
                # Buscar en cliente
                if query in calc.get('cliente', '').lower():
                    filtered.append(calc)
                    continue
                
                # Buscar en monto
                if 'mejor_opcion' in calc and 'monto' in calc['mejor_opcion']:
                    if query in str(calc['mejor_opcion']['monto']):
                        filtered.append(calc)
                        continue
            
            results = filtered
        
        return results
    
    def get_recent(self, limit: int = 10):
        """Obtener cálculos recientes"""
        return self.history[:limit]
    
    def delete(self, calc_id: str):
        """Eliminar cálculo del historial"""
        self.history = [c for c in self.history if c.get('id') != calc_id]
        self._save_history()
    
    def clear(self):
        """Limpiar todo el historial"""
        self.history = []
        self._save_history()
    
    def _load_history(self):
        """Cargar historial desde disco"""
        if not self.storage_path.exists():
            return []
        
        try:
            with open(self.storage_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except:
            return []
    
    def _save_history(self):
        """Guardar historial a disco"""
        try:
            with open(self.storage_path, 'w', encoding='utf-8') as f:
                json.dump(self.history, f, indent=2, ensure_ascii=False)
        except Exception as e:
            print(f"Error guardando historial: {e}")


def create_history_sidebar(page: ft.Page, history_manager: CalculationHistory, on_load_calculation, theme: dict):
    """
    Crear sidebar con historial de cálculos.
    
    Returns:
        dict con 'control' y métodos refresh(), show(), hide()
    """
    
    # Campo de búsqueda
    search_field = ft.TextField(
        hint_text="Buscar...",
        prefix_icon=ft.Icons.SEARCH,
        border_radius=12,
        dense=True,
        on_change=lambda e: _update_history_list()
    )
    
    # Filtro por tipo
    type_filter = ft.Dropdown(
        options=[
            ft.dropdown.Option("TODOS"),
            ft.dropdown.Option("CNCA"),
            ft.dropdown.Option("NUEVO"),
            ft.dropdown.Option("SEGURO")
        ],
        value="TODOS",
        border_radius=12,
        dense=True,
        width=150
    )
    type_filter.on_change = lambda e: _update_history_list()
    
    # Lista de cálculos
    history_list = ft.Column(
        scroll=ft.ScrollMode.AUTO,
        spacing=10
    )
    
    # Container principal
    container = ft.Container(
        content=ft.Column([
            ft.Text("Historial", size=18, weight=ft.FontWeight.BOLD),
            ft.Divider(),
            search_field,
            type_filter,
            ft.Divider(),
            ft.Container(
                content=history_list,
                expand=True
            ),
            ft.Divider(),
            ft.Row([
                ft.IconButton(
                    icon=ft.Icons.DELETE_SWEEP,
                    tooltip="Limpiar historial",
                    on_click=lambda e: _clear_history()
                )
            ], alignment=ft.MainAxisAlignment.END)
        ], spacing=10),
        width=300,
        padding=15,
        bgcolor=theme["colors"]["card_bg"],
        visible=False
    )
    
    def _update_history_list():
        """Actualizar lista de historial"""
        query = search_field.value or ""
        tipo = type_filter.value
        
        results = history_manager.search(query=query, tipo=tipo)
        
        history_list.controls.clear()
        
        if not results:
            history_list.controls.append(
                ft.Text("No hay cálculos", color=theme["colors"]["text_tertiary"], italic=True)
            )
        else:
            for calc in results[:20]:  # Mostrar solo los primeros 20
                card = _build_history_card(calc)
                history_list.controls.append(card)
        
        if page:
            history_list.update()
    
    def _build_history_card(calc: dict):
        """Construir tarjeta de historial"""
        # Parsear timestamp
        try:
            dt = datetime.datetime.fromisoformat(calc['timestamp'])
            date_str = dt.strftime("%d/%m/%Y %H:%M")
        except:
            date_str = "Fecha desconocida"
        
        # Obtener datos clave
        tipo = calc.get('tipo', 'N/A')
        cliente = calc.get('cliente', 'Sin nombre')
        
        # Monto (si existe)
        monto_str = ""
        if 'mejor_opcion' in calc and 'monto' in calc['mejor_opcion']:
            monto = calc['mejor_opcion']['monto']
            monto_str = f"${monto:,.0f}"
        
        # Crear tarjeta
        card = ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Icon(
                        ft.Icons.HISTORY,
                        size=16,
                        color=theme["colors"]["accent"]
                    ),
                    ft.Text(tipo, size=12, weight=ft.FontWeight.BOLD)
                ], spacing=5),
                ft.Text(cliente, size=14, weight=ft.FontWeight.BOLD),
                ft.Text(monto_str, size=12, color=theme["colors"]["success"]) if monto_str else ft.Container(),
                ft.Text(date_str, size=10, color=theme["colors"]["text_tertiary"]),
                ft.Row([
                    ft.TextButton(
                        "Cargar",
                        on_click=lambda e, c=calc: _load_calculation(c)
                    ),
                    ft.IconButton(
                        icon=ft.Icons.DELETE,
                        icon_size=16,
                        on_click=lambda e, c=calc: _delete_calculation(c),
                        tooltip="Eliminar"
                    )
                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN)
            ], spacing=5),
            bgcolor=theme["colors"]["input_bg"],
            padding=10,
            border_radius=8,
            border=ft.Border.all(1, theme["colors"]["divider"])
        )
        
        return card
    
    def _load_calculation(calc: dict):
        """Cargar cálculo"""
        if on_load_calculation:
            on_load_calculation(calc)
    
    def _delete_calculation(calc: dict):
        """Eliminar cálculo"""
        history_manager.delete(calc['id'])
        _update_history_list()
    
    def _clear_history():
        """Limpiar historial"""
        def confirm_clear(e):
            history_manager.clear()
            _update_history_list()
            dialog.open = False
            page.update()
        
        dialog = ft.AlertDialog(
            title=ft.Text("Confirmar"),
            content=ft.Text("¿Estás seguro de limpiar todo el historial?"),
            actions=[
                ft.TextButton("Cancelar", on_click=lambda e: [setattr(dialog, "open", False), page.update()]),
                ft.TextButton("Limpiar", on_click=confirm_clear)
            ]
        )
        
        page.overlay.append(dialog)
        dialog.open = True
        page.update()
    
    def refresh():
        """Refrescar lista"""
        _update_history_list()
    
    def show():
        """Mostrar sidebar"""
        container.visible = True
        _update_history_list()
        if page:
            container.update()
    
    def hide():
        """Ocultar sidebar"""
        container.visible = False
        if page:
            container.update()
    
    # Inicializar lista
    _update_history_list()
    
    return {
        'control': container,
        'refresh': refresh,
        'show': show,
        'hide': hide
    }
