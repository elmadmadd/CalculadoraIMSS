"""
REFINANCE_LAYOUT.py
Layout de 3 columnas para pantalla de Refinanciamiento
Diseño Glassmorphism con paleta Forest Green 2026
"""
import flet as ft

# ==================== PALETA FOREST GREEN ====================
FOREST_DEEP = "#051F20"      # Verde Bosque Profundo
FOREST_DARK = "#0B2B26"      # Verde Oscuro
FOREST_MEDIUM_DARK = "#163832"  # Verde Medio Oscuro
FOREST_TEAL = "#235347"      # Verde Teal Medio
FOREST_SAGE = "#8EB69B"      # Verde Sage/Menta
FOREST_MINT = "#DAF1DE"      # Verde Menta Muy Claro
FOREST_BG = "#F0FAF4"        # Fondo general (verde muy muy claro)

def create_refinance_layout(
    # Widgets existentes
    col_deudas,
    liquidity_input,
    validation_gates_widget,
    on_calculate_callback,  # Función de callback para calcular
    grid_cnca,
    empty_state_cnca,
    results_container_cnca,
    live_kpi_deuda,
    live_kpi_liquidez,
    # Callbacks
    add_debt_callback,
    # Theme
    theme_colors,
    on_clear_callback=None # Callback para limpiar todo (Opcional, al final)
):
    """
    Crea el layout de 3 columnas para refinanciamiento con Glassmorphism Forest.
    """
    
    
    # ==================== COLUMNA 1: INPUTS ====================
    column_1_inputs = ft.Container(
        content=ft.Column([
            # Header con contador dinámico (podría mejorarse pasando el len)
            ft.Row([
                ft.Row([
                    ft.Icon(ft.Icons.LIST_ALT_ROUNDED, size=20, color=FOREST_TEAL),
                    ft.Text("TUS CRÉDITOS", size=14, weight="bold", color=theme_colors["text_primary"])
                ], spacing=8),
                
                # Botón Agregar sutil
                ft.IconButton(
                    icon=ft.Icons.ADD_CIRCLE_ROUNDED,
                    icon_color=FOREST_TEAL,
                    tooltip="Agregar crédito",
                    on_click=add_debt_callback
                )
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            ft.Divider(height=1, color=theme_colors["divider"]),
            
            # Lista de créditos (Scrollable)
            ft.Container(
                content=col_deudas,
                padding=ft.Padding.only(right=5, top=10, bottom=10),
                height=350,
            ),
            
            ft.Divider(height=1, color=theme_colors["divider"]),
            
            # Sección de Ajuste de Liquidez (Footer)
            ft.Container(
                content=ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.MONEY_OFF_CSRED_OUTLINED, size=20, color=FOREST_SAGE),
                        padding=10,
                        bgcolor=FOREST_MINT,
                        border_radius=8
                    ),
                    ft.Column([
                        ft.Text("AJUSTE DE LIQUIDEZ", size=10, weight="bold", color=theme_colors["text_secondary"]),
                        ft.Text("Suma (+) o resta (-) al descuento", size=9, color=theme_colors["text_tertiary"]),
                    ], spacing=2, expand=True),
                    ft.Container(
                        content=liquidity_input,
                        width=120
                    )
                ], spacing=10, alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                padding=ft.Padding.only(top=10, bottom=12)
            ),
            
            # Botones de Acción (Calcular y Limpiar)
            
            ft.Row([
                # Botón Calcular con animación mejorada
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.SEARCH_ROUNDED, color="white", size=20),
                        ft.Text("CALCULAR", size=14, weight="bold", color="white")
                    ], alignment=ft.MainAxisAlignment.CENTER, spacing=8),
                    bgcolor=FOREST_TEAL,
                    padding=ft.Padding.symmetric(vertical=14),
                    border_radius=10,
                    border=ft.Border.all(1, FOREST_MEDIUM_DARK),
                    shadow=ft.BoxShadow(
                        blur_radius=12,
                        color="#66235347",
                        offset=ft.Offset(0, 4)
                    ),
                    on_click=on_calculate_callback,
                    ink=True,
                    animate_scale=ft.Animation(200, "easeInOutBack"),
                    expand=True
                ),
                
                # Botón Limpiar (Secundario)
                ft.Container(
                    content=ft.Icon(ft.Icons.DELETE_SWEEP_ROUNDED, color="#DC2626", size=22),
                    tooltip="Borrar todo",
                    bgcolor="#FEF2F2", # Fondo rojo muy claro
                    padding=14,
                    border_radius=10,
                    border=ft.Border.all(1, "#FCA5A5"),
                    on_click=on_clear_callback,
                    ink=True,
                    animate_scale=ft.Animation(200, "easeInOutBack"),
                )
            ], spacing=10),
            
        ], spacing=0),
        padding=20,
        bgcolor=theme_colors["card_bg"],
        border_radius=16,
        border=ft.Border.all(1, theme_colors["divider"]),
        shadow=ft.BoxShadow(
            blur_radius=10,
            color="#05000000",
            offset=ft.Offset(0, 4)
        ),
        expand=True
    )
    
    # ==================== COLUMNA 2: VALIDACIONES ====================
    column_2_validations = ft.Container(
        content=ft.Column([
            # Widget de Validaciones
            validation_gates_widget,
            
        ], spacing=0),
        padding=20, # Estandarizado a 20
        bgcolor=theme_colors["card_bg"],
        border_radius=16,
        border=ft.Border.all(1, theme_colors["divider"]),
        shadow=ft.BoxShadow(
            blur_radius=10,
            color="#05000000",
            offset=ft.Offset(0, 4)
        ),
        expand=True
    )
    
    # ==================== COLUMNA 3: RESUMEN ====================
    column_3_summary = ft.Container(
        content=ft.Column([
            # Header
            ft.Row([
                ft.Icon(ft.Icons.ASSESSMENT_OUTLINED, size=20, color=FOREST_TEAL),
                ft.Text("RESUMEN", size=14, weight="bold", color=FOREST_DEEP)
            ], spacing=8),
            
            ft.Container(height=16),
            
            # KPI: Deuda Total
            ft.Container(
                content=ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.MONEY_OFF_ROUNDED, color=FOREST_DEEP, size=24),
                        padding=12,
                        bgcolor=FOREST_MINT,
                        border_radius=12
                    ),
                    ft.Column([
                        ft.Text("Deuda Total", size=13, weight="w600", color=theme_colors["text_secondary"]),
                        live_kpi_deuda
                    ], spacing=2, alignment=ft.MainAxisAlignment.CENTER)
                ], spacing=16, alignment=ft.MainAxisAlignment.START, vertical_alignment=ft.CrossAxisAlignment.CENTER),
                bgcolor="#FFFFFF",
                border=ft.Border.all(1, "#F3F4F6"),
                padding=16,
                border_radius=16,
                shadow=ft.BoxShadow(
                    blur_radius=4,
                    color="#05000000",
                    offset=ft.Offset(0, 2)
                )
            ),
            
            ft.Container(height=12),
            
            # KPI: Descuento Actual
            ft.Container(
                content=ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.PIE_CHART_ROUNDED, color=FOREST_TEAL, size=24),
                        padding=12,
                        bgcolor=FOREST_MINT,
                        border_radius=12
                    ),
                    ft.Column([
                        ft.Text("Descuento Actual", size=13, weight="w600", color=theme_colors["text_secondary"]),
                        live_kpi_liquidez
                    ], spacing=2, alignment=ft.MainAxisAlignment.CENTER)
                ], spacing=16, alignment=ft.MainAxisAlignment.START, vertical_alignment=ft.CrossAxisAlignment.CENTER),
                bgcolor="#FFFFFF",
                border=ft.Border.all(1, "#F3F4F6"),
                padding=16,
                border_radius=16,
                shadow=ft.BoxShadow(
                    blur_radius=4,
                    color="#05000000",
                    offset=ft.Offset(0, 2)
                )
            ),
            
            ft.Container(height=16),
            
            # Divider
            ft.Container(
                height=1,
                bgcolor=theme_colors["divider"]
            ),
            
            ft.Container(height=16),
            
            # Información adicional
            ft.Column([
                ft.Row([
                    ft.Icon(ft.Icons.INFO_OUTLINE, size=16, color=theme_colors["text_tertiary"]),
                    ft.Text("Proyección", size=12, weight="bold", color=theme_colors["text_secondary"])
                ], spacing=6),
                ft.Container(height=8),
                ft.Text(
                    "Los resultados se mostrarán después de calcular",
                    size=11,
                    color=theme_colors["text_tertiary"],
                    text_align=ft.TextAlign.CENTER
                )
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER),
            
        ], spacing=0, scroll=ft.ScrollMode.AUTO),
        padding=20, # Estandarizado a 20
        bgcolor=theme_colors["card_bg"],
        border_radius=16,
        border=ft.Border.all(1, theme_colors["divider"]),
        shadow=ft.BoxShadow(
            blur_radius=10,
            color="#05000000",
            offset=ft.Offset(0, 4)
        ),
        expand=True
    )
    
    # ==================== LAYOUT PRINCIPAL ====================
    main_layout = ft.Column([
        # Header de la pantalla
        ft.Container(
            content=ft.Row([
                ft.Icon(ft.Icons.ACCOUNT_BALANCE_ROUNDED, size=28, color=FOREST_TEAL),
                ft.Column([
                    ft.Text("Refinanciamiento", size=24, weight="bold", color=FOREST_DEEP),
                    ft.Text("Refinancia tus créditos y mejora tu liquidez", size=13, color=FOREST_MEDIUM_DARK)
                ], spacing=2)
            ], spacing=12),
            padding=ft.Padding.only(bottom=20)
        ),
        
        # Grid de 3 columnas (Responsive)
        ft.ResponsiveRow([
            # Columna 1: Inputs (30%)
            ft.Column([column_1_inputs], col={"xs": 12, "md": 12, "lg": 4}),
            
            # Columna 2: Resumen (Ahora en medio)
            ft.Column([column_3_summary], col={"xs": 12, "md": 12, "lg": 4}),
            
            # Columna 3: Validaciones (Ahora a la derecha)
            ft.Column([column_2_validations], col={"xs": 12, "md": 12, "lg": 4}),
        ], spacing=16, run_spacing=16, vertical_alignment=ft.CrossAxisAlignment.START),
        
        ft.Container(height=24),
        
        # Sección de Resultados
        ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Icon(ft.Icons.STARS_ROUNDED, size=24, color=FOREST_TEAL),
                    ft.Text("RESULTADOS", size=18, weight="bold", color=FOREST_DEEP)
                ], spacing=10),
                
                ft.Container(height=16),
                
                # Empty state o resultados
                ft.Stack([
                    empty_state_cnca,
                    results_container_cnca
                ])
            ], spacing=0),
            padding=20,
            bgcolor=theme_colors["card_bg"],
            border_radius=16,
            border=ft.Border.all(1, theme_colors["divider"]),
            shadow=ft.BoxShadow(
                blur_radius=10,
                color="#05000000",
                offset=ft.Offset(0, 4)
            ),
        )
        
    ], spacing=0, scroll=ft.ScrollMode.AUTO)
    
    return main_layout
