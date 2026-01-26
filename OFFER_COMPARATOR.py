"""
OFFER COMPARATOR COMPONENT
==========================
Comparador visual de ofertas lado a lado.
Compatible con Flet sin UserControl.
"""

import flet as ft

def create_offer_comparator(page: ft.Page, theme: dict):
    """
    Crear comparador de ofertas.
    
    Returns:
        dict con métodos: add_offer(), remove_offer(), clear(), show_comparison()
    """
    
    selected_offers = []
    max_selections = 3
    
    def add_offer(offer_data: dict):
        """Agregar oferta a la comparación"""
        if len(selected_offers) >= max_selections:
            return False
        
        selected_offers.append(offer_data)
        return True
    
    def remove_offer(offer_data: dict):
        """Remover oferta de la comparación"""
        if offer_data in selected_offers:
            selected_offers.remove(offer_data)
    
    def clear():
        """Limpiar selección"""
        selected_offers.clear()
    
    def get_count():
        """Obtener número de ofertas seleccionadas"""
        return len(selected_offers)
    
    def show_comparison():
        """Mostrar diálogo de comparación"""
        if len(selected_offers) < 2:
            # Mostrar mensaje de error
            snackbar = ft.SnackBar(
                content=ft.Text("Selecciona al menos 2 ofertas para comparar"),
                bgcolor=ft.Colors.ORANGE
            )
            page.overlay.append(snackbar)
            snackbar.open = True
            page.update()
            return
        
        # Crear tabla comparativa
        comparison_table = _build_comparison_table()
        
        # Crear diálogo
        dialog = ft.AlertDialog(
            title=ft.Text("Comparación de Ofertas", size=20, weight=ft.FontWeight.BOLD),
            content=ft.Container(
                content=comparison_table,
                width=800,
                height=600
            ),
            actions=[
                ft.TextButton("Cerrar", on_click=lambda e: _close_dialog(dialog))
            ],
            actions_alignment=ft.MainAxisAlignment.END
        )
        
        page.overlay.append(dialog)
        dialog.open = True
        page.update()
    
    def _close_dialog(dialog):
        """Cerrar diálogo"""
        dialog.open = False
        page.update()
    
    def _build_comparison_table():
        """Construir tabla comparativa"""
        # Encabezados
        headers = ["Concepto"] + [f"Opción {i+1}" for i in range(len(selected_offers))]
        
        # Filas de datos
        rows_data = [
            ("Producto", [o.get('producto', 'N/A') for o in selected_offers]),
            ("Monto", [f"${float(o.get('monto', 0) or 0):,.2f}" for o in selected_offers]),
            ("Plazo", [f"{o.get('plazo', 0)} meses" for o in selected_offers]),
            ("Pago Mensual", [f"${float(o.get('pago_visual', 0) or 0):,.2f}" for o in selected_offers]),
            ("CAT", [f"{float(o.get('cat', 0) or 0):.2f}%" for o in selected_offers]),
            ("Tipo Tasa", [o.get('tipo_tasa', 'N/A') for o in selected_offers]),
        ]
        
        # Si es CNCA, agregar remanente
        if 'remanente' in selected_offers[0]:
            rows_data.append(
                ("Remanente", [f"${float(o.get('remanente', 0) or 0):,.2f}" for o in selected_offers])
            )
            rows_data.append(
                ("Razones", [", ".join(o.get('razones', [])) for o in selected_offers])
            )
        
        # Construir tabla con Containers
        table_rows = []
        
        # Header row
        header_row = ft.Row([
            ft.Container(
                content=ft.Text(h, weight=ft.FontWeight.BOLD, size=12),
                width=150 if i == 0 else 200,
                padding=10,
                bgcolor="#F1F5F9"
            )
            for i, h in enumerate(headers)
        ], spacing=0)
        table_rows.append(header_row)
        
        # Data rows
        for label, values in rows_data:
            # Determinar mejor valor para resaltar
            best_index = _get_best_index(label, values)
            
            cells = [
                ft.Container(
                    content=ft.Text(label, weight=ft.FontWeight.BOLD, size=11),
                    width=150,
                    padding=10,
                    bgcolor="#FFFFFF"
                )
            ]
            
            for i, value in enumerate(values):
                # Resaltar mejor valor
                is_best = (i == best_index)
                cell = ft.Container(
                    content=ft.Text(
                        value,
                        color="green" if is_best else None,
                        weight=ft.FontWeight.BOLD if is_best else None,
                        size=11
                    ),
                    width=200,
                    padding=10,
                    bgcolor="#D1FAE5" if is_best else "#FFFFFF"
                )
                cells.append(cell)
            
            row = ft.Row(cells, spacing=0)
            table_rows.append(row)
        
        # Crear columna con todas las filas
        return ft.Column(
            table_rows,
            scroll=ft.ScrollMode.AUTO,
            spacing=1
        )
    
    def _get_best_index(label: str, values: list) -> int:
        """Determinar cuál es el mejor valor"""
        try:
            if label in ["Monto", "Remanente"]:
                # Mayor es mejor
                numeric_values = [float(v.replace("$", "").replace(",", "")) for v in values]
                return numeric_values.index(max(numeric_values))
            elif label in ["Pago Mensual", "CAT"]:
                # Menor es mejor
                numeric_values = [float(v.replace("$", "").replace(",", "").replace("%", "")) for v in values]
                return numeric_values.index(min(numeric_values))
        except:
            pass
        
        return -1
    
    return {
        'add_offer': add_offer,
        'remove_offer': remove_offer,
        'clear': clear,
        'get_count': get_count,
        'show_comparison': show_comparison
    }


def create_selectable_offer_card(offer_data: dict, comparator: dict, original_card, page: ft.Page):
    """
    Crear tarjeta de oferta seleccionable mediante clic completo.
    """
    
    is_selected = False
    
    # Icono de check (indicador visual de selección)
    check_indicator = ft.Container(
        content=ft.Icon(ft.Icons.CHECK_CIRCLE, color="#10B981", size=28),
        top=10,
        right=10,
        visible=False,  # Invisible por defecto
        bgcolor="white", # Fondo blanco para resaltar sobre la tarjeta
        border_radius=50,
        padding=0,
        shadow=ft.BoxShadow(
            blur_radius=5,
            color="#20000000",
            offset=ft.Offset(0, 2)
        )
    )
    
    # Contenedor wrapper que maneja el efecto de selección y el evento de clic
    # CYBERPUNK FINANCIAL 2026: Neon Lime glow en lugar de Emerald
    card_wrapper = ft.Container(
        content=original_card,
        border=ft.Border.all(0, "transparent"), # Invisible por defecto
        border_radius=20, # Más radio para envolver suavemente
        padding=8,        # Espacio para que la tarjeta se eleve sin cortarse
        animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
        shadow=None,  # Se actualizará dinámicamente
        on_click=lambda e: _toggle_selection(e)
    )
    
    def _toggle_selection(e):
        """Alternar estado de selección"""
        nonlocal is_selected
        
        # Invertir estado deseado
        should_select = not is_selected
        
        if should_select:
            # Intentar agregar al comparador
            success = comparator['add_offer'](offer_data)
            if success:
                is_selected = True
                # Aplicar efecto de resplandor Lime (Neon Glow)
                card_wrapper.border = ft.Border.all(2, "#84CC16")  # Borde Lime sólido
                card_wrapper.shadow = ft.BoxShadow(
                    blur_radius=24,  # Alto blur para efecto de resplandor
                    color="#84CC1660",  # Lime con 40% opacity
                    offset=ft.Offset(0, 0),  # Sin offset para glow uniforme
                    spread_radius=4  # Expansión del resplandor
                )
                check_indicator.visible = True
                check_indicator.scale = 1.2
                check_indicator.animate_scale = ft.Animation(200, "elasticOut")
            else:
                # Fallo (límite alcanzado): Feedback visual de error
                snackbar = ft.SnackBar(
                    content=ft.Text("Máximo 3 ofertas para comparar"),
                    bgcolor=ft.Colors.ORANGE,
                    behavior=ft.SnackBarBehavior.FLOATING,
                    duration=2000
                )
                page.overlay.append(snackbar)
                snackbar.open = True
                
        else:
            # Remover selección
            comparator['remove_offer'](offer_data)
            is_selected = False
            # Quitar efectos
            card_wrapper.border = ft.Border.all(0, "transparent")
            card_wrapper.shadow = None
            check_indicator.visible = False
            check_indicator.scale = 1.0
            
        # Actualizar UI
        card_wrapper.update()
        check_indicator.update()
        if page: page.update()

    
    # Stack final combinando el wrapper interactivo y el indicador flotante
    return ft.Stack([
        card_wrapper,
        check_indicator
    ])
