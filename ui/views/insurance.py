import flet as ft
import time
import asyncio
import logging
from datetime import datetime
from ui.theme import THEME
from ui.components.enterprise_input import EnterpriseInput
from ui.components.dropdown import JuicyDropdown, JuicyProviderDropdown
from ui.utils import shake_control
from core.engine import MotorFinanciero
from core.config import PRODUCTO_CONFIG
from utils.formatting import to_money

logger = logging.getLogger(__name__)

# Colores Royal Purple
PURPLE_DEEP = "#49225B"
PURPLE_ROYAL = "#6E3482"
PURPLE_LAVENDER = "#A56ABD"
PURPLE_MIST = "#E7DBEF"
PURPLE_WHITE = "#F5EBFA"

class InsuranceView(ft.Container):
    def __init__(self, page, config_manager, sound_manager=None, history_manager=None):
        super().__init__(expand=True)
        self.main_page = page
        self.config_manager = config_manager
        self.sound_manager = sound_manager
        self.history_manager = history_manager
        self.motor = MotorFinanciero(config_manager)

        # State
        self.seg_financiera_selected = {"value": "CONSUBANCO"}
        self.seg_tipo_selected = {"value": "VEJEZ"}
        self.seg_plazo_selected = {"value": "60"}
        self.edad_mode_value = ["edad"]

        self.setup_ui()

    def setup_ui(self):
        # --- SELECTORS ---
        self.chip_seg_fin = self.create_chip_selector(
            [{"label": "CONSUBANCO", "value": "CONSUBANCO"},
             {"label": "CONSUPAGO", "value": "CONSUPAGO"},
             {"label": "OPCIPRES", "value": "OPCIPRES"},
             {"label": "MASNOMINA", "value": "MASNOMINA"}],
            self.seg_financiera_selected,
            "Financiera",
            "#49225B"
        )

        self.chip_seg_tipo = self.create_chip_selector(
            ["INVALIDEZ", "VEJEZ"],
            self.seg_tipo_selected,
            "Tipo de Pensión",
            "#6E3482"
        )

        self.chip_seg_plazo = self.create_chip_selector(
            ["12", "24", "36", "48", "54", "60"],
            self.seg_plazo_selected,
            "Plazo (meses)",
            "#A56ABD"
        )

        # --- EDAD ---
        self.in_seg_edad = EnterpriseInput(label="Edad", keyboard_type=ft.KeyboardType.NUMBER, suffix=ft.Text("años"))

        self.in_seg_fecha_nac = ft.TextField(
            label="Fecha de Nacimiento", hint_text="DD/MM/AAAA",
            keyboard_type=ft.KeyboardType.TEXT, border_radius=12,
            border=ft.InputBorder.OUTLINE, visible=False
        )
        self.in_seg_fecha_nac.on_change = self.calcular_edad_desde_fecha

        self.edad_calculada_display = ft.Container(
            content=ft.Row([
                ft.Icon(ft.Icons.CAKE, size=16, color="#A56ABD"),
                ft.Text("Edad:", size=12, color="#A56ABD"),
                ft.Text("--", size=14, weight="bold", color="#49225B")
            ], spacing=5),
            padding=10, bgcolor="#F5EBFA", border_radius=8, visible=False
        )

        self.btn_toggle_edad = ft.Container(
            content=ft.Row([
                ft.Icon(ft.Icons.CALENDAR_TODAY, size=16, color="#6E3482"),
                ft.Text("Usar Fecha Nacimiento", size=12, weight="w600", color="#6E3482")
            ], spacing=6, alignment=ft.MainAxisAlignment.CENTER),
            padding=ft.Padding.symmetric(horizontal=12, vertical=6),
            border=ft.Border.all(1.5, "#6E3482"), border_radius=20,
            bgcolor="#F5EBFA", on_click=self.toggle_edad_mode,
            animate=ft.Animation(200, "easeOut"), ink=True
        )

        # --- MONTO ---
        self.in_seg_monto = EnterpriseInput(label="Monto Crédito", prefix=ft.Text("$"), is_money=True)

        # --- TOGGLES ---
        self.toggle_seg_nuevo = self.ToggleCard("Cliente Nuevo (<24m)", ft.Icons.PERSON_ADD_ROUNDED, "#49225B")
        self.toggle_seg_ret = self.ToggleCard("Tasa Preferencial", ft.Icons.PERCENT_ROUNDED, "#6E3482")

        # --- RESULTS ---
        self.kpi_seg_prima_text = ft.Text("$0", size=32, weight="bold", color="white")
        self.kpi_seg_mensual_text = ft.Text("$0", size=32, weight="bold", color="white")

        self.kpi_seg_prima = self._create_kpi(ft.Icons.MONETIZATION_ON, self.kpi_seg_prima_text, "Prima Total del Seguro", "#49225B", "#6E3482")
        self.kpi_seg_mensual = self._create_kpi(ft.Icons.CALENDAR_MONTH, self.kpi_seg_mensual_text, "Descuento Mensual", "#6E3482", "#A56ABD")

        self.status_seg_card = ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Container(content=ft.Icon(ft.Icons.SHIELD, size=32, color="white"), padding=10, bgcolor="#33FFFFFF", border_radius=10),
                    ft.Column([
                        ft.Text("ESTADO DE EVALUACIÓN", size=11, color="white", weight="w600"),
                        ft.Text("ESPERANDO DATOS", size=18, weight="bold", color="white")
                    ], spacing=4)
                ], spacing=14),
            ], spacing=0),
            padding=20, bgcolor="#49225B", border_radius=12, border=ft.Border.all(1, "#6E3482"),
            shadow=ft.BoxShadow(blur_radius=20, spread_radius=0, color="#6649225B", offset=ft.Offset(0, 4))
        )

        # Info Detalles
        self.txt_factor_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
        self.txt_edad_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
        self.txt_fin_detalle = ft.Text("-", weight="bold", color="#49225B", size=15)
        self.txt_costo_detalle = ft.Text("$0.00", weight="bold", color="#6E3482", size=15)
        self.txt_razon_detalle = ft.Text("Verificando normativa...", size=14, color="#49225B", italic=True)

        # Result Containers
        self.seg_empty_state = ft.Container(
            content=ft.Column([
                ft.Container(content=ft.Icon(ft.Icons.SECURITY, size=72, color="#6E3482"), padding=48, border_radius=100, bgcolor="#F5EBFA", border=ft.Border.all(2, "#E7DBEF")),
                ft.Text("Calculadora de Seguros", size=24, weight="bold", color="#49225B"),
                ft.Text("Completa los datos en el panel izquierdo\ny presiona 'Cotizar Seguro' para ver los resultados.", size=15, color="#6E3482", text_align=ft.TextAlign.CENTER)
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=24),
            alignment=ft.Alignment(0, 0), expand=True
        )

        self.seg_results_container = ft.Column([
            ft.ResponsiveRow([self.kpi_seg_prima, self.kpi_seg_mensual], spacing=16),
            ft.Container(height=16),
            self.status_seg_card,
            ft.Container(height=16),

            ft.Container(
                content=ft.Column([
                    ft.Row([
                        ft.Container(content=ft.Icon(ft.Icons.FACT_CHECK_OUTLINED, size=22, color="#6E3482"), padding=10, bgcolor="#F5EBFA", border_radius=8, border=ft.Border.all(2, "#E7DBEF")),
                        ft.Text("Ficha Técnica", size=17, weight="bold", color="#49225B")
                    ], spacing=14),
                    ft.Divider(height=2, color="#E7DBEF", thickness=2),
                    ft.Container(height=12),
                    ft.ResponsiveRow([
                        ft.Container(content=ft.Column([ft.Text("Edad Evaluada", size=12, weight="w600", color="#A56ABD"), ft.Container(height=6), self.txt_edad_detalle], spacing=0), col=6, padding=ft.Padding.only(right=12, bottom=16)),
                        ft.Container(content=ft.Column([ft.Text("Financiera", size=12, weight="w600", color="#A56ABD"), ft.Container(height=6), self.txt_fin_detalle], spacing=0), col=6, padding=ft.Padding.only(left=12, bottom=16)),
                        ft.Container(content=ft.Column([ft.Text("Descuento Mensual", size=12, weight="w600", color="#A56ABD"), ft.Container(height=6), self.txt_costo_detalle], spacing=0), col=6, padding=ft.Padding.only(right=12, top=16)),
                        ft.Container(content=ft.Column([ft.Text("Factor de Cálculo", size=12, weight="w600", color="#A56ABD"), ft.Container(height=6), self.txt_factor_detalle], spacing=0), col=6, padding=ft.Padding.only(left=12, top=16))
                    ]),
                    ft.Container(height=20),
                    ft.Container(content=ft.Column([
                        ft.Row([ft.Icon(ft.Icons.INFO_OUTLINE, size=18, color="#6E3482"), ft.Text("CRITERIO DE APLICACIÓN", size=12, weight="bold", color="#6E3482")], spacing=10),
                        ft.Container(height=10),
                        self.txt_razon_detalle
                    ], spacing=0), padding=20, bgcolor="#F5EBFA", border_radius=10, border=ft.Border.all(2, "#E7DBEF"))
                ], spacing=14),
                padding=28, bgcolor="#FFFFFF", border_radius=12, border=ft.Border.all(2, "#E7DBEF")
            )
        ], spacing=0, visible=False, opacity=0, scale=0.95, animate_opacity=ft.Animation(400, "easeOut"), animate_scale=ft.Animation(300, ft.AnimationCurve.EASE_OUT))

        self.card_seg_resultado = ft.Stack([self.seg_empty_state, self.seg_results_container])

        # Buttons
        self.btn_calcular_violet = ft.Container(
            content=ft.Row([ft.Icon(ft.Icons.SHIELD_OUTLINED, color="white", size=22), ft.Text("COTIZAR SEGURO", size=15, weight="bold", color="white")], alignment=ft.MainAxisAlignment.CENTER, spacing=10),
            padding=ft.Padding.symmetric(vertical=16, horizontal=24),
            gradient=ft.LinearGradient(colors=["#9333EA", "#7C3AED"], begin=ft.Alignment(-0.5, -0.5), end=ft.Alignment(0.5, 0.5)),
            border_radius=14, border=ft.Border.all(1.5, "#A855F7"),
            shadow=ft.BoxShadow(blur_radius=20, spread_radius=2, color="#409333EA", offset=ft.Offset(0, 8)),
            on_click=lambda e: asyncio.create_task(self.run_seguro(e)), animate_scale=ft.Animation(200, "easeInOutBack")
        )

        self.btn_reset_seguro = ft.TextButton("Limpiar campos", icon=ft.Icons.REFRESH_ROUNDED, icon_color="#94A3B8", on_click=self.limpiar_seguro, style=ft.ButtonStyle(color="#64748B", overlay_color="#F1F5F920"))

        # Main Layout
        self.padding = 30
        self.bgcolor = "#FDFBFF"
        self.content = ft.Column([
            ft.ResponsiveRow([
                # Left Column
                ft.Container(col={"md": 12, "lg": 5, "xl": 4}, content=ft.Column([
                    ft.Row([ft.Container(content=ft.Icon(ft.Icons.SHIELD_ROUNDED, color="#49225B", size=24), padding=10, bgcolor="#F5EBFA", border_radius=10), ft.Text("Seguros de Vida", size=22, weight="bold", color="#49225B")], spacing=12),
                    ft.Container(height=24),
                    self._create_input_group("Información", ft.Icons.PERSON_OUTLINE_ROUNDED, [self.chip_seg_fin, ft.Container(height=10), self.chip_seg_tipo], "#49225B"),
                    ft.Container(height=12),
                    self._create_input_group("Edad", ft.Icons.CAKE_ROUNDED, [
                        ft.Row([ft.Container(expand=True), self.btn_toggle_edad], alignment=ft.MainAxisAlignment.END),
                        ft.Container(height=6),
                        ft.Stack([self.in_seg_edad, self.in_seg_fecha_nac]),
                        ft.Container(content=self.edad_calculada_display, alignment=ft.Alignment(1, 0), padding=ft.Padding.only(top=4))
                    ], "#6E3482"),
                    ft.Container(height=12),
                    self._create_input_group("Crédito", ft.Icons.PAYMENTS_ROUNDED, [self.in_seg_monto, ft.Container(height=10), self.chip_seg_plazo], "#A56ABD"),
                    ft.Container(height=12),
                    self._create_input_group("Opciones", ft.Icons.TUNE_ROUNDED, [self.toggle_seg_nuevo, ft.Container(height=10), self.toggle_seg_ret], "#49225B"),
                    ft.Container(height=20),
                    self.btn_calcular_violet,
                    ft.Container(content=self.btn_reset_seguro, alignment=ft.Alignment(0, 0), padding=ft.Padding.only(top=8))
                ], spacing=0, scroll=ft.ScrollMode.AUTO)),
                # Right Column
                ft.Container(col={"md": 12, "lg": 7, "xl": 8}, content=ft.Container(content=ft.Column([
                    ft.Row([ft.Icon(ft.Icons.SHIELD_OUTLINED, color="#8B5CF6", size=24), ft.Text("Resultado de Cotización", size=22, weight="bold", color=THEME["colors"]["text_primary"])], spacing=12),
                    ft.Container(height=20),
                    ft.Container(content=self.card_seg_resultado, expand=True)
                ], spacing=0), padding=ft.Padding.only(left=20), expand=True), expand=True)
            ], spacing=20, expand=True)
        ], scroll=ft.ScrollMode.AUTO, expand=True)

        # Apply Styles
        self._apply_custom_styles()

    def create_chip_selector(self, options, selected_dict, label, accent_color="#6E3482"):
        chips = []
        chip_containers = []

        def on_select(option_val):
            selected_dict["value"] = option_val
            for c in chip_containers:
                is_sel = c.data == option_val
                c.bgcolor = accent_color if is_sel else "#F5EBFA"
                c.border = ft.Border.all(2 if is_sel else 1, accent_color if is_sel else "#E7DBEF")
                c.content.color = "white" if is_sel else accent_color
                c.scale = 1.0
                c.update()
            self.main_page.update()

        for option in options:
            opt_val = option if isinstance(option, str) else option.get("value", option)
            label_txt = option if isinstance(option, str) else option.get("label", option)
            is_selected = opt_val == selected_dict["value"]

            chip = ft.Container(
                content=ft.Text(label_txt, size=12, weight="bold", color="white" if is_selected else accent_color),
                padding=ft.Padding.symmetric(horizontal=14, vertical=8),
                bgcolor=accent_color if is_selected else "#F5EBFA",
                border=ft.Border.all(2 if is_selected else 1, accent_color if is_selected else "#E7DBEF"),
                border_radius=20, animate_scale=ft.Animation(150, "easeOut"),
                data=opt_val, ink=True,
                on_click=lambda e, v=opt_val: on_select(v)
            )
            chip_containers.append(chip)
            chips.append(chip)

        return ft.Column([
            ft.Text(label, size=11, weight="w600", color="#6E3482"),
            ft.Container(height=6),
            ft.Row(chips, spacing=8, wrap=True)
        ], spacing=0)

    class ToggleCard(ft.Container):
        def __init__(self, label, icon, active_color, initial_value=False):
            super().__init__()
            self.value = initial_value
            self.active_color = active_color
            self.label = label
            self.icon = icon

            self.check_icon = ft.Icon(ft.Icons.CHECK_CIRCLE_ROUNDED, visible=self.value, color="white", size=20)
            self.padding = ft.Padding.symmetric(horizontal=16, vertical=10)
            self.border_radius = 20
            self.animate = ft.Animation(200, "easeOut")
            self.on_click = self._toggle
            self.layout_content()

        def layout_content(self):
            content_color = "white" if self.value else "#49225B"
            icon_color = "white" if self.value else "#6E3482"

            self.content = ft.Row([
                ft.Row([ft.Icon(self.icon, color=icon_color, size=18), ft.Text(self.label, color=content_color, weight="bold" if self.value else "w600", size=12)], spacing=8),
                self.check_icon
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN)

            if self.value:
                self.bgcolor = self.active_color
                self.border = ft.Border.all(2, self.active_color)
            else:
                self.bgcolor = "#F5EBFA"
                self.border = ft.Border.all(1.5, "#E7DBEF")

        def _toggle(self, e):
            self.value = not self.value
            self.check_icon.visible = self.value
            self.layout_content()
            self.update()

    def _create_kpi(self, icon, text_ctrl, label, bg_col, border_col):
        return ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Container(content=ft.Icon(icon, size=32, color="white"), padding=12, bgcolor="#33FFFFFF", border_radius=10),
                    text_ctrl
                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                ft.Text(label, size=14, color="white", weight="w500")
            ], spacing=16),
            padding=28, border_radius=12, bgcolor=bg_col, border=ft.Border.all(1, border_col),
            shadow=ft.BoxShadow(blur_radius=20, spread_radius=0, color=f"{bg_col}66", offset=ft.Offset(0, 4)),
            col=6
        )

    def _create_input_group(self, title, icon, controls, accent_color="#49225B"):
        return ft.Container(
            content=ft.Column([
                ft.Container(height=4, bgcolor=accent_color, border_radius=ft.BorderRadius.only(top_left=12, top_right=12)),
                ft.Container(content=ft.Column([
                    ft.Row([
                        ft.Container(content=ft.Icon(icon, size=20, color=accent_color), padding=10, bgcolor="#F5EBFA", border_radius=10, border=ft.Border.all(1, "#E7DBEF")),
                        ft.Text(title, size=15, weight="bold", color="#49225B")
                    ], spacing=14),
                    ft.Divider(height=10, color="transparent"),
                    *controls
                ], spacing=10), padding=ft.Padding.only(left=18, right=18, top=14, bottom=18))
            ], spacing=0),
            bgcolor="#FFFFFF", border_radius=12, border=ft.Border.all(1, "#E2E8F0"),
            shadow=ft.BoxShadow(blur_radius=20, spread_radius=0, color="#0A000000", offset=ft.Offset(0, 4))
        )

    def toggle_edad_mode(self, e):
        if self.edad_mode_value[0] == "edad":
            self.edad_mode_value[0] = "fecha"
            self.in_seg_edad.visible = False
            self.in_seg_fecha_nac.visible = True
            e.control.content.controls[1].value = "Usar Edad Directa"
            e.control.content.controls[0].name = ft.Icons.NUMBERS
        else:
            self.edad_mode_value[0] = "edad"
            self.in_seg_edad.visible = True
            self.in_seg_fecha_nac.visible = False
            e.control.content.controls[1].value = "Usar Fecha Nacimiento"
            e.control.content.controls[0].name = ft.Icons.CALENDAR_TODAY
        self.main_page.update()

    def calcular_edad_desde_fecha(self, e):
        """Calcular edad desde fecha de nacimiento con auto-formateo"""
        fecha_str = self.in_seg_fecha_nac.value or ""

        # Auto-formateo: agregar / automáticamente
        solo_numeros = ''.join(c for c in fecha_str if c.isdigit())

        # Formatear según la longitud
        if len(solo_numeros) <= 2:
            fecha_formateada = solo_numeros
        elif len(solo_numeros) <= 4:
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:]
        elif len(solo_numeros) <= 8:
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:4] + '/' + solo_numeros[4:8]
        else:
            solo_numeros = solo_numeros[:8]
            fecha_formateada = solo_numeros[:2] + '/' + solo_numeros[2:4] + '/' + solo_numeros[4:8]

        if fecha_formateada != fecha_str:
            self.in_seg_fecha_nac.value = fecha_formateada
            self.in_seg_fecha_nac.update()

        if len(solo_numeros) < 8:
            self.edad_calculada_display.visible = False
            if self.page: self.edad_calculada_display.update()
            return

        try:
            partes = fecha_formateada.split("/")
            if len(partes) != 3: raise ValueError("Formato inválido")
            dia, mes, anio = int(partes[0]), int(partes[1]), int(partes[2])
            fecha_nac = datetime(anio, mes, dia)
            hoy = datetime.now()
            edad = hoy.year - fecha_nac.year
            if (hoy.month, hoy.day) < (fecha_nac.month, fecha_nac.day):
                edad -= 1
            self.edad_calculada_display.content.controls[2].value = f"{edad} años"
            self.edad_calculada_display.visible = True
            self.edad_calculada_display.update()
        except Exception as ex:
            self.edad_calculada_display.visible = False
            if self.page: self.edad_calculada_display.update()

    def _apply_custom_styles(self):
        if hasattr(self.in_seg_monto, 'input_box'):
            self.in_seg_monto.input_box.border = ft.Border.all(1.5, PURPLE_MIST)
            self.in_seg_monto.input_box.bgcolor = "white"

    def limpiar_seguro(self, e):
        self.in_seg_monto.value = ""
        self.in_seg_edad.value = ""
        self.in_seg_fecha_nac.value = ""
        self.seg_financiera_selected["value"] = "CONSUBANCO"
        self.seg_tipo_selected["value"] = "VEJEZ"
        self.seg_plazo_selected["value"] = "60"
        self.toggle_seg_nuevo.value = False
        self.toggle_seg_ret.value = False
        self.toggle_seg_nuevo.layout_content()
        self.toggle_seg_ret.layout_content()
        self.kpi_seg_prima_text.value = "$0"
        self.kpi_seg_mensual_text.value = "$0"
        self.seg_results_container.visible = False
        self.seg_empty_state.visible = True
        self.main_page.update()

    async def run_seguro(self, e):
        btn = e.control
        original_content = btn.content
        btn.content = ft.Row([
            ft.ProgressRing(width=20, height=20, stroke_width=2, color="white"),
            ft.Text("Analizando...", size=16, weight="bold", color="white")
        ], alignment=ft.MainAxisAlignment.CENTER, spacing=10)
        btn.update()

        await asyncio.sleep(0.6)

        try:
            financiera = self.seg_financiera_selected["value"]
            tipo_pension = self.seg_tipo_selected["value"]
            plazo = int(self.seg_plazo_selected["value"])
            es_nuevo = self.toggle_seg_nuevo.value
            es_retencion = self.toggle_seg_ret.value

            try:
                if self.edad_mode_value[0] == "edad":
                    edad = int(self.in_seg_edad.value) if self.in_seg_edad.value else 0
                else:
                    fecha_str = self.in_seg_fecha_nac.value
                    if not fecha_str: raise ValueError("Ingresa fecha de nacimiento")
                    partes = fecha_str.split("/")
                    if len(partes) != 3: raise ValueError("Formato DD/MM/AAAA inválido")
                    dia, mes, anio = int(partes[0]), int(partes[1]), int(partes[2])
                    fecha_nac = datetime(anio, mes, dia)
                    hoy = datetime.now()
                    edad = hoy.year - fecha_nac.year
                    if (hoy.month, hoy.day) < (fecha_nac.month, fecha_nac.day):
                        edad -= 1
            except ValueError as ve:
                if self.sound_manager: self.sound_manager.play("wrong")
                print(f"Error edad: {ve}")
                return
            except:
                if self.sound_manager: self.sound_manager.play("wrong")
                return

            monto = self.in_seg_monto.raw_value
            if monto <= 0:
                if self.sound_manager: self.sound_manager.play("wrong")
                await shake_control(self.in_seg_monto, self.main_page)
                return

            if edad <= 0 or edad > 120:
                if self.sound_manager: self.sound_manager.play("wrong")
                if self.edad_mode_value[0] == "edad":
                    await shake_control(self.in_seg_edad, self.main_page)
                else:
                    await shake_control(self.in_seg_fecha_nac, self.main_page)
                return

            if self.sound_manager: self.sound_manager.play("pop")

            status, color, costo_1, costo_total = self.motor.calcular_seguro(
                financiera, edad, monto, plazo, tipo_pension, es_nuevo, es_retencion
            )

            self.kpi_seg_prima_text.value = to_money(costo_total)
            self.kpi_seg_mensual_text.value = to_money(costo_1)

            self.status_seg_card.bgcolor = color
            self.status_seg_card.content.controls[0].controls[1].controls[1].value = status

            icon_name = ft.Icons.VERIFIED_USER
            if "NO" in status or "RECHAZADO" in status: icon_name = ft.Icons.GPP_BAD
            elif "AUTORIZACION" in status: icon_name = ft.Icons.ADMIN_PANEL_SETTINGS
            elif "APLICA" in status: icon_name = ft.Icons.SECURITY
            self.status_seg_card.content.controls[0].controls[0].content.name = icon_name

            factor_aprox = costo_total / (monto * (plazo/12)) if (monto > 0 and plazo > 0) else 0
            self.txt_factor_detalle.value = f"{factor_aprox:.4f}"
            self.txt_edad_detalle.value = f"{edad} años"
            self.txt_fin_detalle.value = f"{financiera}"
            self.txt_costo_detalle.value = to_money(costo_1)
            self.txt_razon_detalle.value = status

            self.seg_empty_state.visible = False
            self.seg_results_container.visible = True
            self.seg_results_container.opacity = 1
            self.seg_results_container.scale = 1.0

            if self.sound_manager: self.sound_manager.play("cash")
            self.main_page.update()

            if self.history_manager:
                 self.history_manager.add_calculation({
                     "tipo": "Seguro",
                     "cliente": f"{financiera} - {tipo_pension}",
                     "mejor_opcion": {
                         "monto": costo_total,
                         "Status": status,
                         "Prima": costo_1
                     }
                 })

        except Exception as ex:
            print(f"Error run_seguro: {ex}")

        finally:
            btn.content = original_content
            btn.update()
