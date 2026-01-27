import flet as ft
from core.config import PRODUCTO_CONFIG
from ui.theme import THEME
from utils.formatting import clean_float

class DebtRow(ft.Container):
    def __init__(self, idx, on_rem_callback, on_upd_callback=None, is_even=False, on_enter_pressed=None):
        super().__init__()
        self.idx = idx
        self.on_rem_callback = on_rem_callback
        self.on_upd_callback = on_upd_callback
        self.is_complete = False

        # Animaciones de entrada/salida
        self.animate_opacity = ft.Animation(300, ft.AnimationCurve.EASE_OUT)
        self.animate_scale = ft.Animation(300, ft.AnimationCurve.EASE_OUT_BACK)
        self.animate_offset = ft.Animation(400, ft.AnimationCurve.EASE_OUT_QUART)

        # Diseño tipo "Tarjeta Flotante"
        self.padding = 15
        self.margin = ft.Margin(0, 0, 0, 10)
        self.bgcolor = "white"
        self.border_radius = 16
        self.shadow = ft.BoxShadow(
            blur_radius=10,
            color="#08000000",
            offset=ft.Offset(0, 4)
        )
        self.border = ft.Border.all(1, "#F1F5F9")

        # Callbacks wrappers
        def on_rem(e):
            if self.on_rem_callback:
                self.on_rem_callback(self.idx)

        def on_upd(e):
            if self.on_upd_callback:
                self.on_upd_callback(e)

        # Dropdown Financiera (Estilo Hero)
        self.dd_i = ft.Dropdown(
            options=[ft.dropdown.Option(x) for x in PRODUCTO_CONFIG],
            text_size=12,
            text_style=ft.TextStyle(weight="bold", color="#1F2937"),
            border_color="#E5E7EB",
            bgcolor="white",
            hint_text="Seleccionar Fin...",
            dense=True,
            content_padding=ft.Padding.symmetric(horizontal=10, vertical=0), # Menos padding vertical
            height=40, # Altura completa para que llene el row
            border_radius=8
        )
        self.dd_i.expand = True  # Establecer expand como propiedad después de la creación
        self.dd_i.on_change = lambda e: [on_upd(e), self._check_complete(), self._update_brand_style()]

        # Dropdown Plazo (Compacto)
        self.dd_p = ft.Dropdown(
            options=[ft.dropdown.Option(str(x)) for x in [12, 24, 36, 48, 54, 60]],
            text_size=12,
            border_color="#E5E7EB",
            bgcolor="#FFFFFF",
            hint_text="Meses",
            dense=True,
            content_padding=ft.Padding.symmetric(horizontal=6, vertical=4),
            height=40,
            filled=True,
            border_radius=6
        )
        self.dd_p.on_change = lambda e: [on_upd(e), self._check_complete()]

        # Inputs individuales (todos con height=40)
        self.tf_s = ft.TextField(
            hint_text="0.00", prefix=ft.Text("$"), text_size=14, text_style=ft.TextStyle(weight="bold"),
            border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.RIGHT, height=40, content_padding=8,
            keyboard_type=ft.KeyboardType.NUMBER,
            on_change=lambda e: [on_upd(e), self._check_complete()],
            on_blur=self._format_money_input,
            on_focus=self._unformat_money_input
        )

        self.tf_p = ft.TextField(
            hint_text="0.00", prefix=ft.Text("$"), text_size=14, text_style=ft.TextStyle(weight="bold"),
            border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.RIGHT, height=40, content_padding=8,
            keyboard_type=ft.KeyboardType.NUMBER,
            on_change=lambda e: [on_upd(e), self._check_complete()],
            on_blur=self._format_money_input,
            on_focus=self._unformat_money_input
        )

        self.tf_t = ft.TextField(hint_text="0.0", text_size=12, border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.CENTER, height=40, content_padding=8, keyboard_type=ft.KeyboardType.NUMBER, on_change=lambda e: [on_upd(e), self._check_complete()])
        self.tf_n = ft.TextField(hint_text="0", text_size=12, border_color="#E5E7EB", bgcolor="#FFFFFF", text_align=ft.TextAlign.CENTER, height=40, content_padding=8, keyboard_type=ft.KeyboardType.NUMBER, on_change=lambda e: self._check_complete(), on_submit=lambda e: on_enter_pressed() if on_enter_pressed else None)

        self.btn_eliminar = ft.IconButton(
            icon=ft.Icons.CLOSE_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=16, # Un poco más pequeña para elegancia
            on_click=on_rem,
            tooltip="Quitar crédito",
            style=ft.ButtonStyle(
                bgcolor={"": "transparent", "hover": "#FEE2E2"},
                shape=ft.CircleBorder(),
                padding=0
            )
        )

        # --- LAYOUT DE LA CARD ---
        self.content = ft.Column([
            # Header: Dropdown de Financiera (Ajuste definitivo de ancho)
            ft.Stack([
                # Fondo Beige anclado a izquierda y derecha para forzar ancho completo
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.ACCOUNT_BALANCE_ROUNDED, size=18, color="#059669"),
                        self.dd_i, # Dropdown se expandirá en el espacio disponible
                    ], vertical_alignment=ft.CrossAxisAlignment.CENTER, spacing=10),
                    bgcolor="#FFFBEB",
                    border=ft.Border.all(1, "#FDE68A"),
                    border_radius=12,
                    padding=ft.Padding.only(left=12, right=45, top=0, bottom=0), # Padding ajustado
                    left=0,
                    right=0,
                    height=52,
                ),
                # Botón Cerrar (X) transparente
                self._create_close_button(on_rem)
            ], height=52), # Altura fija para el Stack

            # Saldo y Descuento
            ft.Row([
                ft.Container(
                    content=ft.Column([
                        ft.Text("SALDO ACTUAL", size=10, weight="w600", color="#6B7280"),
                        self.tf_s
                    ], spacing=4),
                    bgcolor="#F0FDF4",
                    border=ft.Border.all(1, "#D1FAE5"),
                    border_radius=10,
                    padding=10,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("DESCUENTO MES", size=10, weight="w600", color="#6B7280"),
                        self.tf_p
                    ], spacing=4),
                    bgcolor="#EFF6FF",
                    border=ft.Border.all(1, "#DBEAFE"),
                    border_radius=10,
                    padding=10,
                    expand=True
                )
            ], spacing=10),

            # Tasa, Pagos, Plazo
            ft.Row([
                ft.Container(
                    content=ft.Column([
                        ft.Text("TASA %", size=9, weight="w500", color="#9CA3AF"),
                        self.tf_t
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("PAGOS", size=9, weight="w500", color="#9CA3AF"),
                        self.tf_n
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
                ft.Container(
                    content=ft.Column([
                        ft.Text("PLAZO", size=9, weight="w500", color="#9CA3AF"),
                        self.dd_p
                    ], spacing=4, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    bgcolor="#FAFAFA",
                    border_radius=8,
                    padding=8,
                    expand=True
                ),
            ], spacing=8)

        ], spacing=10)

    def _check_complete(self):
        try:
            complete = all([
                self.dd_i.value,
                clean_float(self.tf_s.value) > 0,
                clean_float(self.tf_p.value) > 0,
                clean_float(self.tf_t.value) > 0,
                clean_float(self.tf_n.value) > 0,
                self.dd_p.value
            ])

            if complete != self.is_complete:
                self.is_complete = complete
                self.border = ft.Border.all(2, THEME["colors"]["accent"] if complete else "#E2E8F0")
                self.update()
        except:
            pass

    def _format_money_input(self, e):
        """Formatea el input a moneda (ej. 23,000.00) al perder el foco"""
        try:
            val = e.control.value
            if not val: return
            # Limpiar caracteres previos
            clean_val = float(str(val).replace("$", "").replace(",", ""))
            # Formatear
            e.control.value = f"{clean_val:,.2f}"
            e.control.update()
        except:
            pass # Si no es un número válido, no hacer nada

    def _unformat_money_input(self, e):
        """Quita el formato para facilitar edición al ganar el foco"""
        try:
            val = e.control.value
            if not val: return
            # Quitar comas y formato
            clean_val = str(val).replace(",", "").replace("$", "")
            if "." in clean_val:
                 try:
                     f = float(clean_val)
                     if f.is_integer():
                         e.control.value = str(int(f)) # Quitar decimales .00 si es entero
                     else:
                         e.control.value = clean_val
                 except:
                     e.control.value = clean_val
            else:
                 e.control.value = clean_val
            e.control.update()
        except:
            pass

    def _create_close_button(self, on_rem):
        """Crea el botón de cerrar con el cursor correcto"""
        btn = ft.Container(
            content=ft.Icon(ft.Icons.CLOSE_ROUNDED, size=16, color="#9CA3AF"),
            right=12,
            top=18,
            on_click=on_rem,
            tooltip="Quitar crédito"
        )
        btn.mouse_cursor = ft.MouseCursor.CLICK  # Establecer como propiedad
        return btn

    def _update_brand_style(self, e=None):
        val = self.dd_i.value
        if not val:
            # Estilo default si no hay selección
            self.dd_i.bgcolor = "#FFFFFF"
            self.dd_i.text_style = ft.TextStyle(weight="bold", color="#1F2937")
            self.dd_i.border_color = "#E5E7EB"
            try:
                if getattr(self.dd_i, "page", None):
                    self.dd_i.update()
            except: pass
            return

        # Buscar estilo de marca
        brand_key = val.upper()
        # Mapeo simple de nombres parciales
        if "CONSUBANCO" in brand_key: brand_key = "CONSUBANCO"
        # ... (rest of logic can be added if needed, or simplified)

    def get_data(self):
        """Retorna los datos del crédito"""
        try:
            # Safely get values
            inst = self.dd_i.value
            saldo = self.tf_s.value
            pago = self.tf_p.value
            plazo = self.dd_p.value
            tasa = self.tf_t.value
            pagos = self.tf_n.value

            if not inst: return None

            return {
                "producto": inst,
                "saldo": saldo,
                "pago": pago,
                "plazo": int(plazo) if plazo else 0,
                "tasa": float(tasa) if tasa else 0.0,
                "pagos": int(pagos) if pagos else 0
            }
        except:
            return None
