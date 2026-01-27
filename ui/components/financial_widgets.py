"""
FINANCIAL_WIDGETS.py
Widgets Financieros Premium para Dashboard
"""
import flet as ft

class FinancialHealthWidget(ft.Container):
    """
    Widget de Salud Financiera EDITABLE.
    Permite ajustar la liquidez directamente desde el widget.
    """
    def __init__(self, value=0.0, max_value=1.0, on_change=None):
        super().__init__()
        self.value = value
        self.max_value = max_value if max_value > 0 else 1.0
        self.on_change_callback = on_change
        
        # TextField editable para el monto
        self.amount_input = ft.TextField(
            value="0",
            text_size=24,
            text_align=ft.TextAlign.LEFT,
            border=ft.InputBorder.NONE,
            content_padding=ft.Padding.all(0),
            color="#111827",
            cursor_color="#10B981",
            selection_color="#10B98133",
            text_style=ft.TextStyle(
                size=24,
                weight=ft.FontWeight.W_800,
                font_family="Outfit"
            ),
            width=180,
            height=36,
            keyboard_type=ft.KeyboardType.NUMBER,
            on_change=self._handle_input_change,
            on_blur=self._format_on_blur,
            hint_text="0",
            hint_style=ft.TextStyle(color="#D1D5DB")
        )
        
        self.label_text = ft.Text(
            "AJUSTE DE LIQUIDEZ", 
            size=11, 
            weight=ft.FontWeight.BOLD,
            color="#6B7280",
            font_family="Outfit"
        )
        
        self.status_badge = ft.Container(
            content=ft.Row([
                ft.Icon(ft.Icons.EDIT_ROUNDED, size=14, color="#3B82F6"),
                ft.Text("EDITABLE", size=10, weight="bold", color="#3B82F6")
            ], spacing=4, tight=True),
            padding=ft.Padding.symmetric(horizontal=8, vertical=4),
            border_radius=20,
            bgcolor="#EFF6FF",
        )
        
        # Mini gráfico de indicador
        self.indicator_bar = ft.Container(
            width=60,
            height=6,
            border_radius=10,
            bgcolor="#10B981",
            animate=ft.Animation(600, ft.AnimationCurve.EASE_OUT_CUBIC)
        )
        
        # Layout Principal
        self.content = ft.Container(
            content=ft.Row([
                # Icono circular
                ft.Container(
                    content=ft.Icon(ft.Icons.TUNE_ROUNDED, color="#3B82F6", size=28),
                    padding=12,
                    bgcolor="#EFF6FF",
                    border_radius=16,
                    border=ft.Border.all(1, "#DBEAFE")
                ),
                
                # Input Numérico
                ft.Column([
                    self.label_text,
                    ft.Row([
                        self.amount_input,
                        ft.Container(width=10),
                        self.status_badge
                    ], alignment=ft.MainAxisAlignment.START, vertical_alignment=ft.CrossAxisAlignment.CENTER)
                ], spacing=2),
                
                ft.Container(expand=True),
                
                # Indicador visual
                ft.Column([
                    ft.Text("Impacto", size=10, color="#9CA3AF", text_align=ft.TextAlign.RIGHT),
                    ft.Row([
                         ft.Container(width=4, height=4, border_radius=4, bgcolor="#E5E7EB"),
                         ft.Container(width=4, height=4, border_radius=4, bgcolor="#E5E7EB"),
                         self.indicator_bar
                    ], spacing=2, alignment=ft.MainAxisAlignment.END)
                ], alignment=ft.MainAxisAlignment.CENTER, horizontal_alignment=ft.CrossAxisAlignment.END)
                
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            padding=20,
            bgcolor="#FFFFFF",
            border_radius=16,
            border=ft.Border.all(1, "#DBEAFE"),
            shadow=ft.BoxShadow(
                blur_radius=20,
                color="#3B82F610",
                offset=ft.Offset(0, 4)
            )
        )
    
    def _handle_input_change(self, e):
        """Maneja cambios en el input"""
        try:
            # Limpiar y validar
            raw_value = e.control.value.replace(",", "").replace("$", "").strip()
            if raw_value == "" or raw_value == "-":
                return
            
            value = float(raw_value)
            self.value = value
            
            # Llamar callback si existe
            if self.on_change_callback:
                self.on_change_callback(value)
                
        except ValueError:
            pass
    
    def _format_on_blur(self, e):
        """Formatea el valor al perder foco"""
        try:
            raw_value = e.control.value.replace(",", "").replace("$", "").strip()
            if raw_value == "":
                e.control.value = "0"
            else:
                value = float(raw_value)
                e.control.value = f"{value:,.0f}"
            e.control.update()
        except ValueError:
            e.control.value = "0"
            e.control.update()
        
    def update_data(self, liquidity, capacity):
        """Actualiza los datos visuales con animación"""
        self.value = liquidity
        
        # 1. Actualizar TextField (solo si no está siendo editado)
        if self.amount_input.value != str(int(liquidity)):
            self.amount_input.value = f"{liquidity:,.0f}"
        
        # 2. Lógica de Estado (Semáforo) - Simplificada para modo edición
        if liquidity > 2000:
            color = "#059669"
            bg = "#ECFDF5"
        elif liquidity > 0:
            color = "#D97706"
            bg = "#FFFBEB"
        elif liquidity == 0:
            color = "#3B82F6"
            bg = "#EFF6FF"
        else:
            color = "#DC2626"
            bg = "#FEF2F2"
        
        # Color del input
        if liquidity > 0:
             self.amount_input.color = "#111827" 
        elif liquidity < 0:
             self.amount_input.color = "#DC2626"
        else:
             self.amount_input.color = "#9CA3AF"
        
        # 3. Animar barra lateral
        percent = min(max(abs(liquidity) / 10000, 0.1), 1.0)
        self.indicator_bar.width = 20 + (percent * 60)
        self.indicator_bar.bgcolor = color
        
        self.update()

def create_financial_widget():
    return FinancialHealthWidget()


class PaymentCapacityWidget(ft.Container):
    """
    Widget para ingresar la Capacidad de Pago disponible del cliente.
    Diseño moderno y editable, optimizado para 720p y 1080p.
    """
    def __init__(self, value=0.0, on_change=None):
        super().__init__()
        self.value = value
        self.on_change_callback = on_change
        
        # TextField editable
        self.capacity_input = ft.TextField(
            value="0",
            text_size=24,
            text_align=ft.TextAlign.LEFT,
            border=ft.InputBorder.NONE,
            content_padding=ft.Padding.only(bottom=8), # Ajuste para centrado vertical
            color="#059669",
            cursor_color="#059669",
            selection_color="#05966933",
            text_style=ft.TextStyle(
                size=24,
                weight=ft.FontWeight.W_900,
                font_family="Outfit"
            ),
            prefix=ft.Text("$"),
            prefix_style=ft.TextStyle(
                size=24,
                weight=ft.FontWeight.W_900,
                color="#059669",
                font_family="Outfit"
            ),
            width=150,
            height=32,
            keyboard_type=ft.KeyboardType.NUMBER,
            input_filter=ft.InputFilter(regex_string=r"[0-9.]", replacement_string=""), # Solo números
            on_change=self._handle_input_change,
            on_blur=self._format_on_blur,
            on_focus=self._handle_focus, # Limpiar al enfocar
            hint_text="0",
            hint_style=ft.TextStyle(color="#D1D5DB")
        )
        
        # Layout compacto
        self.content = ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, color="#059669", size=24),
                        padding=10,
                        bgcolor="#ECFDF5",
                        border_radius=12,
                        border=ft.Border.all(1, "#D1FAE5")
                    ),
                    ft.Column([
                        ft.Text("CAPACIDAD DE PAGO", size=10, weight="bold", color="#6B7280", font_family="Outfit"),
                        ft.Row([
                            self.capacity_input,
                        ], spacing=0, alignment=ft.MainAxisAlignment.START)
                    ], spacing=2, expand=True),
                ], spacing=12, alignment=ft.MainAxisAlignment.START),
                
                # Badge editable (más pequeño)
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.EDIT_ROUNDED, size=14, color="#059669"),
                        ft.Text("Editable", size=9, color="#059669", weight="bold")
                    ], spacing=4, alignment=ft.MainAxisAlignment.CENTER),
                    padding=ft.Padding.symmetric(horizontal=8, vertical=4),
                    bgcolor="#ECFDF5",
                    border_radius=8,
                    alignment=ft.Alignment(0, 0)
                )
            ], spacing=10),
            padding=16,
            height=140,  # Altura fija para igualar con PensionEstimatorWidget
            bgcolor="#FFFFFF",
            border_radius=14,
            border=ft.Border.all(1, "#D1FAE5"),
            shadow=ft.BoxShadow(
                blur_radius=15,
                color="#05966908",
                offset=ft.Offset(0, 3)
            )
        )
    
    def _handle_focus(self, e):
        """Limpia el campo si es 0 al enfocar."""
        if self.capacity_input.value == "0" or self.capacity_input.value == "0.00":
            self.capacity_input.value = ""
            self.capacity_input.update()

    def _handle_input_change(self, e):
        val = e.control.value
        
        # 1. Si está vacío (borró todo), FORZAR "0" visualmente
        if val is None or len(val) == 0:
            e.control.value = "0"
            e.control.update()
            
            # Actualizar valor interno
            self.value = 0.0
            if self.on_change_callback:
                self.on_change_callback(0.0)
            return

        # 2. Eliminar ceros a la izquierda (ej: "05" -> "5") excepto "0."
        if val.startswith("0") and len(val) > 1 and not val.startswith("0."):
            val = val.lstrip("0")
            e.control.value = val 
            e.control.update()
        
        
        try:
            # Limpiar comas y símbolos para conversión numérica interna
            raw_value = val.replace(",", "").replace("$", "").strip()
            
            value = float(raw_value)
            self.value = value
            if self.on_change_callback:
                self.on_change_callback(value)
        except ValueError:
            pass
    
    def _format_on_blur(self, e):
        try:
            raw_value = e.control.value.replace(",", "").replace("$", "").strip()
            if raw_value == "":
                e.control.value = "0"
            else:
                value = float(raw_value)
                e.control.value = f"{value:,.0f}"
            e.control.update()
        except ValueError:
            e.control.value = "0"
            e.control.update()


class PensionCalculatorWidget(ft.Container):
    """
    Widget para calcular el 30% disponible de la pensión.
    Se activa cuando la capacidad de pago es 0.
    """
    def __init__(self, on_change=None):
        super().__init__()
        self.total_pension = 0.0
        self.available_amount = 0.0
        self.on_change_callback = on_change
        
        # TextField para pensión total
        self.pension_input = ft.TextField(
            value="0",
            text_size=20,
            text_align=ft.TextAlign.LEFT,
            border=ft.InputBorder.NONE,
            content_padding=ft.Padding.all(0),
            color="#111827",
            cursor_color="#D97706",
            selection_color="#D9770633",
            text_style=ft.TextStyle(
                size=20,
                weight=ft.FontWeight.W_700,
                font_family="Outfit"
            ),
            width=160,
            height=32,
            keyboard_type=ft.KeyboardType.NUMBER,
            on_change=self._handle_pension_change,
            on_blur=self._format_on_blur,
            hint_text="0",
            hint_style=ft.TextStyle(color="#D1D5DB")
        )
        
        # Texto de resultado (30%)
        self.result_text = ft.Text(
            "$0.00",
            size=24,
            weight="w900",
            color="#D97706",
            font_family="Outfit"
        )
        
        # Layout
        self.content = ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.ELDERLY_ROUNDED, color="#D97706", size=28),
                        padding=12,
                        bgcolor="#FFFBEB",
                        border_radius=14,
                        border=ft.Border.all(1, "#FEF3C7")
                    ),
                    ft.Column([
                        ft.Text("CÁLCULO PENSIONADOS", size=11, weight="bold", color="#6B7280"),
                        ft.Text("30% Disponible", size=10, color="#D97706", weight="w600")
                    ], spacing=2),
                ], spacing=12),
                
                ft.Container(height=12),
                
                # Input de pensión total
                ft.Column([
                    ft.Text("Pensión Total Mensual", size=12, color="#6B7280", weight="w600"),
                    ft.Row([
                        ft.Text("$", size=20, weight="w700", color="#111827"),
                        self.pension_input
                    ], spacing=4)
                ], spacing=4),
                
                ft.Divider(height=1, color="#F3F4F6"),
                
                # Resultado
                ft.Row([
                    ft.Column([
                        ft.Text("Capacidad Disponible (30%)", size=11, color="#6B7280"),
                        self.result_text
                    ], spacing=2),
                    ft.Container(
                        content=ft.Icon(ft.Icons.CALCULATE_ROUNDED, size=24, color="#D97706"),
                        padding=8,
                        bgcolor="#FFFBEB",
                        border_radius=12
                    )
                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN)
                
            ], spacing=8),
            padding=20,
            bgcolor="#FFFFFF",
            border_radius=16,
            border=ft.Border.all(1, "#FEF3C7"),
            shadow=ft.BoxShadow(
                blur_radius=20,
                color="#D9770610",
                offset=ft.Offset(0, 4)
            )
        )
    
    def _handle_pension_change(self, e):
        try:
            raw_value = e.control.value.replace(",", "").replace("$", "").strip()
            if raw_value == "":
                self.total_pension = 0.0
                self.available_amount = 0.0
            else:
                self.total_pension = float(raw_value)
                self.available_amount = self.total_pension * 0.30
            
            self.result_text.value = f"${self.available_amount:,.2f}"
            self.result_text.update()
            
            if self.on_change_callback:
                self.on_change_callback(self.available_amount)
        except ValueError:
            pass
    
    def _format_on_blur(self, e):
        try:
            raw_value = e.control.value.replace(",", "").replace("$", "").strip()
            if raw_value == "":
                e.control.value = "0"
            else:
                value = float(raw_value)
                e.control.value = f"{value:,.0f}"
            e.control.update()
        except ValueError:
            e.control.value = "0"
            e.control.update()


class PensionEstimatorWidget(ft.Container):
    """
    Widget que calcula la Pensión Total Aproximada.
    Fórmula: Pensión Total = (Descuento Actual + Capacidad de Pago) / 0.30
    Optimizado para 720p y 1080p.
    """
    def __init__(self):
        super().__init__()
        self.current_discount = 0.0
        self.payment_capacity = 0.0
        self.total_pension = 0.0
        self.used_30_percent = 0.0
        
        # Textos de resultado (tamaños reducidos)
        self.used_amount_text = ft.Text(
            "$0.00",
            size=16,
            weight="w700",
            color="#3B82F6",
            font_family="Outfit"
        )
        
        self.total_pension_text = ft.Text(
            "$0.00",
            size=22,
            weight="w900",
            color="#111827",
            font_family="Outfit"
        )
        
        # Barra de progreso visual (más pequeña)
        self.progress_bar = ft.Container(
            width=0,
            height=6,
            border_radius=8,
            bgcolor="#3B82F6",
            animate=ft.Animation(500, ft.AnimationCurve.EASE_OUT)
        )
        
        # Layout compacto
        self.content = ft.Container(
            content=ft.Column([
                # Header compacto
                ft.Row([
                    ft.Container(
                        content=ft.Icon(ft.Icons.CALCULATE_ROUNDED, color="#3B82F6", size=24),
                        padding=10,
                        bgcolor="#EFF6FF",
                        border_radius=12,
                        border=ft.Border.all(1, "#DBEAFE")
                    ),
                    ft.Column([
                        ft.Text("PENSIÓN APROXIMADA", size=10, weight="bold", color="#6B7280"),
                        ft.Text("Cálculo Automático", size=9, color="#3B82F6", weight="w600")
                    ], spacing=1, expand=True),
                    ft.Container(
                        content=ft.Text("30%", size=12, weight="bold", color="#3B82F6"),
                        padding=ft.Padding.symmetric(horizontal=10, vertical=4),
                        bgcolor="#EFF6FF",
                        border_radius=16,
                        border=ft.Border.all(1, "#DBEAFE")
                    )
                ], spacing=10, alignment=ft.MainAxisAlignment.START),
                
                ft.Container(height=12),
                
                # 30% Usado (compacto)
                ft.Column([
                    ft.Row([
                        ft.Text("30% Usado", size=11, color="#6B7280", weight="w600"),
                        self.used_amount_text
                    ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    
                    # Barra de progreso (más pequeña)
                    ft.Container(
                        content=ft.Stack([
                            ft.Container(
                                width=220,
                                height=6,
                                border_radius=8,
                                bgcolor="#F3F4F6"
                            ),
                            self.progress_bar
                        ]),
                        width=220
                    )
                ], spacing=6),
                
                ft.Divider(height=1, color="#F3F4F6"),
                
                # Pensión Total Estimada
                ft.Column([
                    ft.Text("Pensión Total Estimada", size=11, color="#6B7280", weight="w600"),
                    ft.Row([
                        self.total_pension_text,
                        ft.Container(
                            content=ft.Icon(ft.Icons.INFO_OUTLINE, size=18, color="#9CA3AF"),
                            tooltip="Calculado a partir del descuento actual y capacidad de pago"
                        )
                    ], spacing=6)
                ], spacing=4),
                
            ], spacing=10),
            padding=16,
            height=140,  # Altura fija para igualar con PaymentCapacityWidget
            bgcolor="#FFFFFF",
            border_radius=14,
            border=ft.Border.all(1, "#DBEAFE"),
            shadow=ft.BoxShadow(
                blur_radius=15,
                color="#3B82F608",
                offset=ft.Offset(0, 3)
            )
        )
    
    def update_calculation(self, current_discount, payment_capacity):
        """
        Actualiza el cálculo de la pensión.
        current_discount: Descuento actual (suma de pagos de deudas)
        payment_capacity: Capacidad de pago disponible
        """
        self.current_discount = current_discount
        self.payment_capacity = payment_capacity
        
        # Calcular 30% usado
        self.used_30_percent = current_discount + payment_capacity
        
        # Calcular pensión total (inversa del 30%)
        if self.used_30_percent > 0:
            self.total_pension = self.used_30_percent / 0.30
        else:
            self.total_pension = 0.0
        
        # Actualizar textos
        self.used_amount_text.value = f"${self.used_30_percent:,.2f}"
        self.total_pension_text.value = f"${self.total_pension:,.2f}"
        
        # Actualizar barra de progreso (máximo 220px)
        if self.total_pension > 0:
            percentage = min((self.used_30_percent / self.total_pension), 1.0)
            self.progress_bar.width = percentage * 220
        else:
            self.progress_bar.width = 0
        
        self.update()


class CapacityProgressBar(ft.Container):
    """
    Barra de progreso visual para la Capacidad de Pago (30%).
    Muestra semáforo (Verde/Amarillo/Rojo) y disponibilidad con feedback semántico.
    """
    def __init__(self, current_discount=0.0, available_capacity=0.0):
        super().__init__()
        self.current_discount = current_discount
        self.available_capacity = available_capacity
        
        # Componentes UI
        self.lbl_usage = ft.Text("0.0%", size=12, weight="bold", color="#374151")
        self.lbl_available = ft.Text("$0.00", size=12, weight="bold", color="#10B981")
        self.progress_fill = ft.Container(
            height=8, 
            width=0,
            border_radius=4,
            bgcolor="#10B981",
            animate=ft.Animation(600, ft.AnimationCurve.EASE_OUT_CUBIC)
        )
        self.status_icon = ft.Icon(ft.Icons.CHECK_CIRCLE_ROUNDED, size=14, color="#10B981")
        
        # Construir layout inicial
        self.content = ft.Column([
            ft.Row([
                ft.Row([
                    ft.Icon(ft.Icons.PIE_CHART_ROUNDED, size=14, color="#6B7280"),
                    ft.Text("USO DE CAPACIDAD (30%)", size=10, weight="bold", color="#6B7280")
                ], spacing=4),
                self.lbl_usage
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            # Barra de progreso
            ft.Stack([
                ft.Container(height=8, bgcolor="#F3F4F6", border_radius=4, width=float("inf")),
                self.progress_fill
            ]),
            
            # Disponibilidad
            ft.Row([
                ft.Text("Disponible:", size=11, color="#6B7280"),
                ft.Row([
                    self.status_icon,
                    self.lbl_available
                ], spacing=4, tight=True)
            ], alignment=ft.MainAxisAlignment.END)
            
        ], spacing=6)
        
        self.bgcolor = "#ECFDF5"
        self.padding = 12
        self.border_radius = 12
        self.border = ft.Border.all(1, "#10B98140")
        self.shadow = None

    def update_data(self, current_discount, available_capacity):
        self.current_discount = current_discount
        self.available_capacity = available_capacity
        total_capacity = current_discount + available_capacity
        
        if total_capacity <= 0:
            percentage = 0
            total_capacity = 1.0 
        else:
            percentage = (current_discount / total_capacity) * 100
        
        # Determinar colores y estados
        if percentage >= 100:
            color = "#EF4444" # Rojo
            bg_color = "#FEF2F2"
            icon = ft.Icons.WARNING_ROUNDED
        elif percentage >= 90:
            color = "#F59E0B" # Naranja
            bg_color = "#FFFBEB"
            icon = ft.Icons.WARNING_AMBER_ROUNDED
        elif percentage >= 70:
            color = "#D97706" # Amarillo oscuro
            bg_color = "#FFFBEB"
            icon = ft.Icons.INFO_OUTLINE_ROUNDED
        else:
            color = "#10B981" # Verde
            bg_color = "#ECFDF5"
            icon = ft.Icons.CHECK_CIRCLE_ROUNDED

        # Actualizar UI
        self.progress_fill.bgcolor = color
        
        # Flet permite porcentajes como string "50%" si el padre tiene ancho definido.
        # Aquí el padre es el Stack (width infinito) dentro de Column.
        # Para evitar problemas con widths indefinidos, calculamos ancho relativo si es posible,
        # pero "XX%" es lo más flexible aquí.
        visual_percentage = min(percentage, 100.0)
        self.progress_fill.width = f"{visual_percentage}%" 
        
        self.lbl_usage.value = f"{percentage:.1f}%"
        self.lbl_usage.color = color
        
        self.lbl_available.value = f"${available_capacity:,.2f}"
        self.lbl_available.color = color
        
        self.status_icon.name = icon
        self.status_icon.color = color
        
        self.border = ft.Border.all(1, f"{color}40") 
        self.bgcolor = bg_color 
        
        self.update()


class ValidationGate(ft.Container):
    """Componente individual para una compuerta de validación."""
    def __init__(self, gate_number, title):
        super().__init__(
            animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(300, ft.AnimationCurve.EASE_OUT_BACK)
        )
        self.gate_number = gate_number
        self.title = title
        self.status = "pending"
        
        self.icon = ft.Icon(ft.Icons.CIRCLE_OUTLINED, size=20, color="#9CA3AF")
        self.title_text = ft.Text(f"Compuerta {gate_number}: {title}", size=12, weight="bold", color="#374151")
        self.message_text = ft.Text("", size=11, color="#6B7280")
        self.suggestion_container = ft.Container(visible=False)
        
        self.content = ft.Column([
            ft.Row([
                self.icon,
                ft.Column([self.title_text, self.message_text], spacing=2, expand=True)
            ], spacing=8),
            self.suggestion_container
        ], spacing=4)
        
        self.padding = 12
        self.border_radius = 8
        self.bgcolor = "#F9FAFB"
        self.border = ft.Border.all(1, "#E5E7EB")
    
    def update_status(self, status, message="", suggestion=""):
        self.status = status
        self.message = message
        
        if status == "pass":
            self.icon.name = ft.Icons.CHECK_CIRCLE
            self.icon.color = "#10B981"
            self.bgcolor = "#ECFDF5"
            self.border = ft.Border.all(1, "#10B98140")
            self.message_text.color = "#059669"
        elif status == "warning":
            self.icon.name = ft.Icons.WARNING_AMBER_ROUNDED
            self.icon.color = "#F59E0B"
            self.bgcolor = "#FFFBEB"
            self.border = ft.Border.all(1, "#F59E0B40")
            self.message_text.color = "#D97706"
        elif status == "fail":
            self.icon.name = ft.Icons.CANCEL
            self.icon.color = "#EF4444"
            self.bgcolor = "#FEF2F2"
            self.border = ft.Border.all(1, "#EF444440")
            self.message_text.color = "#DC2626"
        else:
            self.icon.name = ft.Icons.CIRCLE_OUTLINED
            self.icon.color = "#9CA3AF"
            self.bgcolor = "#F9FAFB"
            self.border = ft.Border.all(1, "#E5E7EB")
            self.message_text.color = "#6B7280"
        
        self.message_text.value = message
        
        if suggestion:
            self.suggestion_container.content = ft.Container(
                content=ft.Row([
                    ft.Icon(ft.Icons.LIGHTBULB_OUTLINE, size=14, color="#3B82F6"),
                    ft.Text(suggestion, size=10, color="#3B82F6", italic=True)
                ], spacing=4),
                bgcolor="#EFF6FF",
                border_radius=6,
                padding=8
            )
            self.suggestion_container.visible = True
        else:
            self.suggestion_container.visible = False
        
        if getattr(self, "page", None):
            try:
                self.update()
            except:
                pass


class ValidationGatesWidget(ft.Container):
    """Widget que muestra las 4 compuertas de validación para refinanciamiento."""
    def __init__(self):
        super().__init__()
        
        self.gate1 = ValidationGate(1, "Tasa de Interés")
        self.gate2 = ValidationGate(2, "Antigüedad (Pagos)")
        self.gate3 = ValidationGate(3, "Incremento Descuento")
        self.gate4 = ValidationGate(4, "Aumento de Plazo")
        
        # Resumen inicial (estado de espera)
        self.summary_icon = ft.Icon(ft.Icons.PENDING_OUTLINED, size=20, color="#9CA3AF")
        self.summary_text = ft.Text("Esperando datos...", size=12, weight="bold", color="#6B7280")
        self.summary_badge = ft.Container(
            content=ft.Text("—", size=11, weight="bold", color="#6B7280"),
            bgcolor="#F3F4F6",
            padding=ft.Padding.symmetric(horizontal=8, vertical=4),
            border_radius=12
        )
        
        # Estado de expansión (INICIALMENTE COLAPSADO)
        self.is_expanded = False
        self.toggle_icon = ft.IconButton(
            icon=ft.Icons.EXPAND_MORE,
            icon_size=20,
            tooltip="Expandir",
            on_click=self._toggle_expand
        )
        
        # Contenedor de las compuertas (colapsable)
        self.gates_container = ft.Column([
            self.gate1,
            self.gate2,
            self.gate3,
            self.gate4,
        ], spacing=8)
        
        # Wrapper Animado para efecto de expansión
        self.gates_wrapper = ft.Container(
            content=self.gates_container,
            height=0,                  # Altura inicial (colapsado)
            opacity=0,                 # Opacidad inicial
            # Restauramos animación general para animar la altura (slide)
            animate=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT),
            animate_opacity=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT),
            clip_behavior=ft.ClipBehavior.HARD_EDGE, # Cortar contenido al colapsar
        )
        
        self.content = ft.Column([
            # Header con botón de colapsar
            ft.Row([
                ft.Icon(ft.Icons.VERIFIED_USER_OUTLINED, size=18, color="#3B82F6"),
                ft.Text("VALIDACIONES DE ELEGIBILIDAD", size=11, weight="bold", color="#374151"),
                ft.Container(expand=True),
                self.toggle_icon
            ], spacing=6, alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            # Resumen (siempre visible)
            ft.Row([
                self.summary_icon,
                self.summary_text,
                ft.Container(expand=True),
                self.summary_badge
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
            
            # Espaciador animado (visible solo al expandir)
            ft.Container(height=8, visible=True), # Siempre visible para mantener layout estable
            
            # Compuertas (colapsables con animación)
            self.gates_wrapper,
            
        ], spacing=8)
        
        self.bgcolor = "#FFFFFF"
        self.padding = 16
        self.border_radius = 12
        self.border = ft.Border.all(1, "#E5E7EB")
        self.shadow = ft.BoxShadow(blur_radius=10, color="#00000008", offset=ft.Offset(0, 2))
    
    def _toggle_expand(self, e):
        """Alterna entre expandido y contraído con animación de slide"""
        self.is_expanded = not self.is_expanded
        
        # Usamos altura fija para permitir la animación de slide
        # 340px es suficiente para las 4 compuertas standard
        self.gates_wrapper.height = 340 if self.is_expanded else 0
        self.gates_wrapper.opacity = 1 if self.is_expanded else 0
        
        self.toggle_icon.icon = ft.Icons.EXPAND_LESS if self.is_expanded else ft.Icons.EXPAND_MORE
        self.toggle_icon.tooltip = "Contraer" if self.is_expanded else "Expandir"
        
        self.update()
    
    
    def validate_credits(self, credits_data, producto="CONSUPAGO", incremento_liquidez=0, plazo_nuevo=60):
        """
        Valida créditos usando la lógica real de las 4 compuertas:
        1. Tasa de Interés
        2. Antigüedad (Pagos Acumulados)
        3. Incremento de Descuento
        4. Aumento de Plazo
        
        Requiere UNANIMIDAD: todas las deudas deben cumplir AL MENOS UNA razón en común.
        """
        if not credits_data or len(credits_data) == 0:
            self._reset_gates()
            return
        
        # Calcular razones para cada deuda
        sets_deudas = []
        
        for credit in credits_data:
            razones = set()
            tasa = float(credit.get('tasa', credit.get('tasa_interes', 0)))
            pagos = int(credit.get('pagos', credit.get('pagos_realizados', 0)))
            plazo_old = int(credit.get('plazo', credit.get('plazo_original', 0)))
            producto_origen = str(credit.get('producto', '')).upper()
            
            # COMPUERTA 1: Tasa de Interés
            if producto in ["MASNOMINA", "CONSUPAGO"]:
                razones.add("Tasa")
            elif producto == "OPCIPRES" and tasa <= 2.57:
                razones.add("Tasa")
            elif producto == "CONSUBANCO" and tasa <= 2.40:
                razones.add("Tasa")
            
            # COMPUERTA 2: Antigüedad (Pagos Acumulados)
            # Reglas: 
            # - 16+ pagos: Refinanciamiento Tradicional
            # - 24+ pagos: Refinanciamiento con Retención (Mejor tasa)
            
            if pagos >= 16:
                razones.add("Antigüedad")
            
            # Guardar info extra para mensajes detallados
            credit['_es_retencion'] = pagos >= 24
            credit['_es_tradicional'] = pagos >= 16
            
            # COMPUERTA 3: Incremento de Descuento
            inc = float(incremento_liquidez)
            if producto == "MASNOMINA":
                razones.add("Incremento Descuento")
            elif producto == "CONSUPAGO" and inc >= 750.0 and tasa <= 2.67:
                razones.add("Incremento Descuento")
            elif producto == "OPCIPRES" and inc >= 750.0 and tasa <= 2.67:
                razones.add("Incremento Descuento")
            elif producto == "CONSUBANCO" and inc >= 1000.0 and tasa <= 2.57:
                razones.add("Incremento Descuento")
            
            # COMPUERTA 4: Aumento de Plazo
            if (plazo_old <= 12 and plazo_nuevo >= 36) or \
               (plazo_old <= 24 and plazo_nuevo >= 48) or \
               (plazo_old in [36, 48] and plazo_nuevo >= 54) or \
               (plazo_old == 54 and plazo_nuevo == 60):
                razones.add("Plazo")
            
            sets_deudas.append(razones)
        
        # Evaluar Unanimidad (Intersección)
        if not sets_deudas:
            self._reset_gates()
            return
        
        interseccion = set.intersection(*sets_deudas)
        
        # Actualizar UI de cada compuerta
        self.gate1.update_status(
            "pass" if "Tasa" in interseccion else "fail",
            "✓ Todas las deudas cumplen tasa" if "Tasa" in interseccion else "Tasas no cumplen para todas",
            "" if "Tasa" in interseccion else "💡 Verifica tasas según submarina"
        )
        
        # Verificar si califican para retención (24+ pagos)
        todos_retencion = all(c.get('_es_retencion', False) for c in credits_data)
        
        msg_antiguedad = "✓ +16 Pagos (Tradicional)"
        if todos_retencion:
            msg_antiguedad = "✓ +24 Pagos (Apto Retención)"

        self.gate2.update_status(
            "pass" if "Antigüedad" in interseccion else "fail",
            msg_antiguedad if "Antigüedad" in interseccion else "Requiere al menos 16 pagos",
            "" if "Antigüedad" in interseccion else "💡 Se requieren mínimo 16 pagos para refinanciar"
        )
        
        self.gate3.update_status(
            "pass" if "Incremento Descuento" in interseccion else "fail",
            "✓ Incremento suficiente" if "Incremento Descuento" in interseccion else f"Requiere +${750 if producto != 'CONSUBANCO' else 1000}",
            "" if "Incremento Descuento" in interseccion else "💡 Ajustar liquidez para incrementar descuento"
        )
        
        self.gate4.update_status(
            "pass" if "Plazo" in interseccion else "fail",
            "✓ Aumento de plazo válido" if "Plazo" in interseccion else "Plazos no permiten aumento",
            "" if "Plazo" in interseccion else "💡 Seleccionar plazo mayor (54 o 60 meses)"
        )
        
        # Actualizar resumen
        passed = len(interseccion)
        if interseccion:
            self._update_summary(4, 4)  # Aprobado por unanimidad
        else:
            self._update_summary(0, 4)  # No hay unanimidad
    
    def _reset_gates(self):
        """Resetea todas las compuertas al estado inicial (esperando datos)"""
        self.gate1.update_status("pending", "Agrega créditos para validar")
        self.gate2.update_status("pending", "Agrega créditos para validar")
        self.gate3.update_status("pending", "Agrega créditos para validar")
        self.gate4.update_status("pending", "Agrega créditos para validar")
        
        # Estado de espera en el resumen
        self.summary_icon.name = ft.Icons.PENDING_OUTLINED
        self.summary_icon.color = "#9CA3AF"
        self.summary_text.value = "Esperando datos..."
        self.summary_text.color = "#6B7280"
        self.summary_badge.content.value = "—"
        self.summary_badge.bgcolor = "#F3F4F6"
        self.summary_badge.content.color = "#6B7280"
        
        # Colapsar automáticamente cuando no hay datos
        if self.is_expanded:
            self.is_expanded = False
            self.gates_container.visible = False
            self.toggle_icon.icon = ft.Icons.EXPAND_MORE
            self.toggle_icon.tooltip = "Expandir"
        
        # Solo actualizar si el control está en la página
        if hasattr(self, 'page') and self.page:
            try:
                self.update()
            except:
                pass
    
    def _update_summary(self, passed, total):
        """Actualiza el resumen de validaciones"""
        self.summary_badge.content.value = f"{passed}/{total}"
        
        # Expandir automáticamente cuando hay resultados
        if not self.is_expanded and total > 0:
            self.is_expanded = True
            self.gates_container.visible = True
            self.toggle_icon.icon = ft.Icons.EXPAND_LESS
            self.toggle_icon.tooltip = "Contraer"
        
        if passed == total:
            self.summary_icon.name = ft.Icons.CHECK_CIRCLE
            self.summary_icon.color = "#10B981"
            self.summary_text.value = "✓ Elegible para refinanciamiento"
            self.summary_text.color = "#059669"
            self.summary_badge.bgcolor = "#ECFDF5"
            self.summary_badge.content.color = "#059669"
        elif passed >= total / 2:
            self.summary_icon.name = ft.Icons.WARNING_AMBER
            self.summary_icon.color = "#F59E0B"
            self.summary_text.value = "⚠️ Requiere atención"
            self.summary_text.color = "#D97706"
            self.summary_badge.bgcolor = "#FFFBEB"
            self.summary_badge.content.color = "#D97706"
        else:
            self.summary_icon.name = ft.Icons.CANCEL
            self.summary_icon.color = "#EF4444"
            self.summary_text.value = "❌ NO elegible"
            self.summary_text.color = "#DC2626"
            self.summary_badge.bgcolor = "#FEF2F2"
            self.summary_badge.content.color = "#DC2626"
        
        # Solo actualizar si el control está en la página
        if hasattr(self, 'page') and self.page:
            try:
                self.update()
            except:
                pass
