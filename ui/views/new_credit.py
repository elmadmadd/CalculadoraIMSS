import flet as ft
import time
import asyncio
import logging
from ui.theme import THEME
from ui.components.enterprise_input import EnterpriseInput
from ui.components.card import FintechCard
from ui.utils import shake_control, render_cards_progressively
from core.engine import MotorFinanciero
from core.config import PRODUCTO_CONFIG
from ui.components.comparator import create_selectable_offer_card
from ui.components.skeleton import create_skeleton_offer_card

logger = logging.getLogger(__name__)

# ==================== PALETA GOLD LUXURY (REFINADA) ====================
GOLD_PRIMARY = "#C8A86C"  # Dorado Metálico Suave (Toggle Base)
GOLD_LIGHT   = "#F5EFE6"  # Blanco Cremoso (Fondos)
GOLD_CREAM   = "#EBDCC1"  # Beige Arena (Acentos sutiles)
GOLD_DARK    = "#947843"  # Bronce Antiguo (Texto, Iconos)
GOLD_DEEP    = "#785F30"  # Marrón Dorado Profundo (Alto contraste)
GOLD_ACCENT  = "#A68A53"  # Dorado Oscuro (Active/Hover)
GOLD_ACTION  = "#E68900"  # Naranja Ámbar Vibrante (Botón Principal)
COLOR_THEME_NUEVO = GOLD_PRIMARY

SUNSET_MANGO = "#D55123"   # Dark Mango
SUNSET_APRICOT = "#E47A24" # Apricot

class NewCreditView(ft.Container):
    def __init__(self, page, config_manager, gestor_ops, history_manager=None, sound_manager=None, offer_comparator=None):
        super().__init__(expand=True)
        self.main_page = page
        self.config_manager = config_manager
        self.gestor_ops = gestor_ops
        self.history_manager = history_manager
        self.sound_manager = sound_manager
        self.offer_comparator = offer_comparator
        self.motor = MotorFinanciero(config_manager)

        self.orden_actual = {"criterio": "liquidez"}

        self.setup_ui()

    def setup_ui(self):
        # Input
        self.input_nuevo = EnterpriseInput(
            label="Monto Requerido / Capacidad",
            prefix=ft.Text("$"),
            is_money=True,
            on_submit=lambda e: asyncio.create_task(self.run_nuevo(e))
        )

        # Apply Gold Styles to Input
        if hasattr(self.input_nuevo, 'input_box'):
            self.input_nuevo.input_box.border = ft.Border.all(1.5, GOLD_CREAM)
            self.input_nuevo.input_box.bgcolor = "white"
            self.input_nuevo.input_box.focused_border_color = GOLD_PRIMARY

        # Radio Group (Hidden logic)
        self.radio_nuevo = ft.RadioGroup(content=ft.Container(), value="CAPACIDAD")

        # Switch Retención
        self.cb_retencion = ft.Switch(
            value=False,
            active_color=GOLD_PRIMARY,
            active_track_color=f"{GOLD_PRIMARY}60",
            inactive_thumb_color="#9CA3AF",
            inactive_track_color="#E5E7EB",
            on_change=lambda e: self.update_toggles_ui()
        )
        self.retencion_toggle = ft.Container(
            content=ft.Row([
                self.cb_retencion,
                ft.Text("Tasa Preferencial", size=14, weight="w500", color="#374151")
            ], spacing=10),
            padding=ft.Padding.symmetric(horizontal=12, vertical=8),
            bgcolor="#F9FAFB",
            border_radius=10
        )

        # Toggle Morphing Button
        self.toggle_icon = ft.Icon(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, color="white", size=26)
        self.toggle_text_main = ft.Text("Por Capacidad", size=16, weight="bold", color="white")
        self.toggle_text_sub = ft.Text("Cambiar a Monto", size=11, color="white70")

        self.toggle_box = ft.Container(
            content=ft.Row([
                ft.Container(
                    content=self.toggle_icon,
                    width=48, height=48,
                    bgcolor="white24",
                    border_radius=12,
                    alignment=ft.Alignment(0, 0)
                ),
                ft.Column([
                    self.toggle_text_main,
                    self.toggle_text_sub
                ], spacing=2, alignment=ft.MainAxisAlignment.CENTER)
            ], alignment=ft.MainAxisAlignment.START, spacing=16),
            padding=ft.Padding.all(16),
            bgcolor=GOLD_ACCENT,
            border_radius=16,
            animate_scale=ft.Animation(400, "elasticOut"),
            animate=ft.Animation(200, "easeOut"),
            shadow=ft.BoxShadow(blur_radius=20, color=f"{GOLD_ACCENT}40", offset=ft.Offset(0,10)),
            border=ft.Border.all(1, "white12")
        )

        self.tipo_calc_toggle = ft.GestureDetector(
            content=self.toggle_box,
            on_tap_down=self.handle_tap_down,
            on_tap_up=self.handle_tap_up,
            on_tap_cancel=self.handle_tap_cancel,
            mouse_cursor=ft.MouseCursor.CLICK
        )

        self.lbl_input_titulo = ft.Text(
            "CAPACIDAD DE PAGO",
            size=14,
            color=GOLD_DEEP,
            weight="bold",
            font_family="Inter"
        )

        # Button Calculate
        self.btn_icon = ft.Icon(ft.Icons.CALCULATE_OUTLINED, color="white", size=24)
        self.btn_text = ft.Text("CALCULAR OFERTAS", size=16, weight="bold", color="white", font_family="Inter")
        self.btn_spinner = ft.ProgressRing(width=20, height=20, stroke_width=3, color="white", visible=False)

        self.btn_calcular_gold = ft.Container(
            content=ft.Row([
                self.btn_icon,
                self.btn_text,
                self.btn_spinner
            ], alignment=ft.MainAxisAlignment.CENTER, spacing=10),
            padding=ft.Padding.symmetric(vertical=15),
            gradient=ft.LinearGradient(
                colors=[GOLD_PRIMARY, GOLD_DEEP],
                begin=ft.Alignment(-1, -1),
                end=ft.Alignment(1, 1)
            ),
            border_radius=12,
            shadow=ft.BoxShadow(
                blur_radius=12,
                color=f"{GOLD_DEEP}66",
                offset=ft.Offset(0, 4)
            ),
            on_click=self.run_nuevo,
            animate_scale=ft.Animation(100, "easeOut"),
            ink=True
        )

        # Results Grid
        self.grid_nuevo = ft.ResponsiveRow(spacing=20, run_spacing=20)

        # Filters
        self.btn_filtro_liquidez = self._create_filter_btn("liquidez", "Mayor Liquidez", True)
        self.btn_filtro_pago = self._create_filter_btn("pago", "Menor Pago")
        self.btn_filtro_plazo = self._create_filter_btn("plazo", "Menor Plazo")

        self.filtros_row = ft.Row([
            ft.Icon(ft.Icons.SORT_ROUNDED, size=16, color=THEME["colors"]["text_tertiary"]),
            ft.Text("Ordenar:", size=12, color=THEME["colors"]["text_tertiary"], weight="bold"),
            self.btn_filtro_liquidez,
            self.btn_filtro_pago,
            self.btn_filtro_plazo
        ], spacing=8, wrap=True)

        self.lbl_contador_ofertas = ft.Text("0", size=12, weight="bold", color="white")
        self.badge_contador = ft.Container(
            content=self.lbl_contador_ofertas,
            bgcolor=COLOR_THEME_NUEVO,
            padding=ft.Padding.symmetric(horizontal=8, vertical=4),
            border_radius=12,
            visible=False
        )

        # Layout
        self.bgcolor = GOLD_LIGHT
        self.padding = ft.Padding.symmetric(horizontal=20, vertical=24)
        self.alignment = ft.Alignment(-1, -1)

        self.content = ft.Column([
            ft.ResponsiveRow([
                # Left Panel
                ft.Container(
                    col={"xs": 12, "sm": 12, "md": 5, "lg": 4, "xl": 3},
                    content=ft.Container(
                        bgcolor="#FFFFFF",
                        padding=ft.Padding.symmetric(horizontal=20, vertical=24),
                        border_radius=16,
                        border=ft.Border.all(1, "#E5E7EB"),
                        shadow=ft.BoxShadow(blur_radius=20, color="#10000000", offset=ft.Offset(0, 4)),
                        content=ft.Column([
                            ft.Container(
                                content=ft.Row([
                                    ft.Container(
                                        content=ft.Icon(ft.Icons.CREDIT_SCORE_ROUNDED, color="white", size=22),
                                        padding=10,
                                        bgcolor=GOLD_PRIMARY,
                                        border_radius=10,
                                        shadow=ft.BoxShadow(blur_radius=8, color=f"{GOLD_PRIMARY}50", offset=ft.Offset(0, 2))
                                    ),
                                    ft.Column([
                                        ft.Text("Simulador", size=18, weight="bold", color="#1F2937"),
                                        ft.Text("Crédito Nuevo", size=11, color="#6B7280")
                                    ], spacing=0)
                                ], spacing=12),
                                padding=ft.Padding.only(bottom=20)
                            ),
                            ft.Container(height=1, bgcolor="#F3F4F6", margin=ft.Margin.only(bottom=16)),
                            self._create_seccion_input("TIPO DE CÁLCULO", ft.Icons.SWAP_HORIZ_ROUNDED, self.tipo_calc_toggle),
                            ft.Container(height=16),
                            self._create_seccion_input("MONTO O CAPACIDAD", ft.Icons.ATTACH_MONEY_ROUNDED, self.input_nuevo),
                            ft.Container(height=16),
                            self._create_seccion_input("OPCIONES", ft.Icons.TUNE_ROUNDED, self.retencion_toggle, "Clientes nuevos con monto mínimo o exclientes sin crédito activo"),
                            ft.Container(height=24),
                            self.btn_calcular_gold,
                            ft.Container(
                                content=ft.TextButton(
                                    "Limpiar campos",
                                    icon=ft.Icons.REFRESH_ROUNDED,
                                    icon_color="#9CA3AF",
                                    style=ft.ButtonStyle(color="#6B7280", overlay_color="#F3F4F6"),
                                    on_click=self.limpiar_nuevo
                                ),
                                alignment=ft.Alignment(0, 0),
                                padding=ft.Padding.only(top=8)
                            )
                        ], spacing=0, scroll=ft.ScrollMode.AUTO)
                    )
                ),
                # Right Panel
                ft.Container(
                    col={"xs": 12, "sm": 12, "md": 7, "lg": 8, "xl": 9},
                    bgcolor="transparent",
                    content=ft.Column([
                        ft.Container(
                            content=ft.Row([
                                ft.Row([
                                    ft.Container(
                                        content=ft.Icon(ft.Icons.VIEW_LIST_ROUNDED, color=COLOR_THEME_NUEVO, size=20),
                                        padding=8,
                                        bgcolor=f"{COLOR_THEME_NUEVO}15",
                                        border_radius=8
                                    ),
                                    ft.Text("Ofertas Disponibles", size=18, weight="w600", color=THEME["colors"]["text_primary"]),
                                    self.badge_contador
                                ], spacing=10),
                            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                            padding=ft.Padding.only(bottom=12)
                        ),
                        ft.Container(content=self.filtros_row, padding=ft.Padding.only(bottom=16)),
                        self.grid_nuevo,
                    ], scroll=ft.ScrollMode.AUTO, expand=True),
                    expand=True,
                    padding=ft.Padding.only(left=10)
                )
            ], spacing=16, expand=True, vertical_alignment=ft.CrossAxisAlignment.START)
        ], expand=True)

        self.update_morphing_visuals()

    def _create_seccion_input(self, titulo, icono, contenido, descripcion=None):
        elementos = [
            ft.Row([
                ft.Icon(icono, size=16, color=GOLD_DARK),
                ft.Text(titulo, size=11, weight="w600", color=GOLD_DARK)
            ], spacing=6),
            ft.Container(height=8),
            contenido
        ]
        if descripcion:
            elementos.append(ft.Container(content=ft.Text(descripcion, size=10, color="#9CA3AF", italic=True), padding=ft.Padding.only(top=6)))
        return ft.Container(content=ft.Column(elementos, spacing=0), padding=ft.Padding.symmetric(vertical=4))

    def _create_filter_btn(self, data, text, visible=False):
        return ft.Container(
            data=data,
            content=ft.Row([ft.Text(text, size=12, weight="bold")], spacing=5),
            padding=ft.Padding.symmetric(horizontal=12, vertical=8),
            bgcolor=COLOR_THEME_NUEVO if visible else "#F3F4F6",
            border_radius=8,
            on_click=lambda e: self.aplicar_orden(data),
            visible=visible
        )

    def change_tipo_calc(self, val):
        self.radio_nuevo.value = val
        self.lbl_input_titulo.value = "MONTO SOLICITADO" if val == "MONTO" else "CAPACIDAD DE PAGO"
        if hasattr(self.lbl_input_titulo, 'page'): self.lbl_input_titulo.update()

        # EnterpriseInput specific update if needed (label update not supported directly usually, check class)
        # self.input_nuevo.tf.label = ... (EnterpriseInput logic)

        self.update_morphing_visuals()

    def update_toggles_ui(self):
        self.update_morphing_visuals()

    def update_morphing_visuals(self):
        is_monto = self.radio_nuevo.value == "MONTO"

        self.toggle_icon.name = ft.Icons.MONETIZATION_ON_ROUNDED if is_monto else ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED
        self.toggle_text_main.value = "Por Monto" if is_monto else "Por Capacidad"
        self.toggle_text_sub.value = "Cambiar a Capacidad" if is_monto else "Cambiar a Monto"

        color_activo = GOLD_DEEP if is_monto else GOLD_ACCENT
        self.toggle_box.bgcolor = color_activo
        self.toggle_box.shadow.color = f"{color_activo}40"

        try:
            if self.toggle_box.page: self.toggle_box.update()
        except: pass

    def handle_tap_down(self, e):
        self.toggle_box.scale = 0.96
        self.toggle_box.update()

    def handle_tap_up(self, e):
        new_val = "CAPACIDAD" if self.radio_nuevo.value == "MONTO" else "MONTO"
        self.change_tipo_calc(new_val)
        self.toggle_box.scale = 1.0
        self.update_morphing_visuals()
        self.toggle_box.update()

    def handle_tap_cancel(self, e):
        self.toggle_box.scale = 1.0
        self.toggle_box.update()

    def aplicar_orden(self, criterio):
        self.orden_actual["criterio"] = criterio
        # ... (Implement sorting logic similar to original) ...
        # Simplified:
        for btn in [self.btn_filtro_liquidez, self.btn_filtro_pago, self.btn_filtro_plazo]:
            es_activo = btn.data == criterio
            btn.bgcolor = COLOR_THEME_NUEVO if es_activo else "#F3F4F6"
            btn.content.controls[0].color = "white" if es_activo else THEME["colors"]["text_secondary"]
            if btn.page: btn.update()

        # Reorder grid
        # ... logic to reorder children of self.grid_nuevo ...

    def limpiar_nuevo(self, e):
        self.input_nuevo.value = ""
        self.cb_retencion.value = False
        self.radio_nuevo.value = "CAPACIDAD"
        self.grid_nuevo.controls.clear()
        self.update_toggles_ui()
        self.input_nuevo.update()
        self.grid_nuevo.update()
        if self.cb_retencion.page: self.cb_retencion.update()

    def msg(self, t, c=THEME["colors"]["success"]):
        self.main_page.snack_bar = ft.SnackBar(ft.Text(t, color="white"), bgcolor=c)
        self.main_page.snack_bar.open = True
        self.main_page.update()

    async def run_nuevo(self, e):
        start_time = time.time()

        self.btn_icon.visible = False
        self.btn_text.value = "CALCULANDO..."
        self.btn_spinner.visible = True
        self.btn_calcular_gold.disabled = True
        self.btn_calcular_gold.update()

        try:
            v = self.input_nuevo.raw_value
            if not v or v <= 0:
                raise ValueError("Monto inválido")

            # Skeletons
            self.grid_nuevo.controls.clear()
            for _ in range(6):
                self.grid_nuevo.controls.append(
                    ft.Container(content=create_skeleton_offer_card(), col={"xs": 12, "md": 6, "xl": 4})
                )
            self.grid_nuevo.update()

            # ... Calculation Logic (Simplified adaptation from main.py) ...
            bm = str(self.radio_nuevo.value).lower() == "monto"
            tr = self.cb_retencion.value

            ops = []
            for p in PRODUCTO_CONFIG:
                # ... loop products and calculate ...
                # Mocking logic for structure
                keys_plazos = self.motor.retencion.get(p, {}).keys() if tr else self.motor.tradicional.get(p, {}).keys()
                plazos = sorted([int(k) for k in keys_plazos], reverse=True)
                for pl in plazos:
                     # ...
                     res = self.motor.calcular(p, v, pl, bm, tr, rounding_step=100.0)
                     if res:
                         ops.append({**res, "producto": p, "plazo": pl, "pago_visual": res['pago'], "tipo_tasa": "RETENCION" if tr else "TRADICIONAL"})

            # Render
            self.grid_nuevo.controls.clear()

            if not ops:
                self.grid_nuevo.controls.append(ft.Text("No hay ofertas"))
            else:
                self.lbl_contador_ofertas.value = str(len(ops))
                self.badge_contador.visible = True

                def create_card(item):
                    o, i = item
                    return FintechCard(o, i==0, "NUEVO", None, None, None, None, self.main_page, delay_ms=i*50)

                await render_cards_progressively(self.grid_nuevo, [(o,i) for i,o in enumerate(ops)], create_card)

            if self.sound_manager: self.sound_manager.play("cash")

        except Exception as ex:
            if self.sound_manager: self.sound_manager.play("wrong")
            await shake_control(self.input_nuevo, self.main_page)
            self.msg(f"Error: {ex}", THEME["colors"]["error"])
        finally:
            self.btn_icon.visible = True
            self.btn_text.value = "CALCULAR OFERTAS"
            self.btn_spinner.visible = False
            self.btn_calcular_gold.disabled = False
            self.btn_calcular_gold.update()
