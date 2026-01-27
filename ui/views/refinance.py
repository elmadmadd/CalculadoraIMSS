import flet as ft
import asyncio
import logging
from ui.theme import THEME
from ui.layouts.refinance_layout import create_refinance_layout
from ui.components.financial_widgets import ValidationGatesWidget
from ui.components.debt_row import DebtRow
from ui.components.card import FintechCard
from ui.utils import shake_control, render_cards_progressively
from core.engine import MotorFinanciero
from utils.formatting import to_money
from ui.components.comparator import create_offer_comparator, create_selectable_offer_card

logger = logging.getLogger(__name__)

class RefinanceView(ft.Container):
    def __init__(self, page, config_manager, gestor_ops, history_manager=None, sound_manager=None, offer_comparator=None):
        super().__init__(expand=True)
        self.page = page
        self.config_manager = config_manager
        self.gestor_ops = gestor_ops
        self.history_manager = history_manager
        self.sound_manager = sound_manager
        self.offer_comparator = offer_comparator

        self.motor = MotorFinanciero(config_manager)

        # State
        self.payment_capacity = {"value": 0.0}
        self.ultima_operacion_id = None

        # UI Components
        self.col_deudas = ft.Column(spacing=2, scroll=ft.ScrollMode.AUTO)
        self.grid_cnca = ft.ResponsiveRow(spacing=20, run_spacing=20)

        self.empty_state_cnca = ft.Container(
            content=ft.Column([
                ft.Container(
                    content=ft.Icon(ft.Icons.ROCKET_LAUNCH_OUTLINED, size=50, color=THEME["colors"]["text_tertiary"]),
                    padding=25, bgcolor=THEME["colors"]["background"], border_radius=100,
                    border=ft.Border.all(1, THEME["colors"]["divider"])
                ),
                ft.Text("Esperando Datos", size=16, weight="bold", color=THEME["colors"]["text_secondary"]),
                ft.Text(
                    "Ingresa tus pasivos y presiona Calcular\npara ver las mejores ofertas.",
                    size=12, color=THEME["colors"]["text_tertiary"], text_align=ft.TextAlign.CENTER
                )
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=10),
            alignment=ft.Alignment(0, 0),
            padding=40,
            opacity=0.8,
            height=300
        )

        self.results_container_cnca = ft.Container(
            content=self.grid_cnca,
            border_radius=12,
            visible=False
        )

        self.live_kpi_deuda = ft.Text("$0.00", size=22, weight="bold", color="#1F2937")
        self.live_kpi_liquidez = ft.Text("$0.00", size=22, weight="bold", color="#1F2937")

        self.liquidity_input = ft.TextField(
            label="Disponible Adicional",
            prefix=ft.Text("$"),
            text_align=ft.TextAlign.RIGHT,
            keyboard_type=ft.KeyboardType.NUMBER,
            border_color=THEME["colors"]["border"],
            focused_border_color=THEME["colors"]["accent"],
            bgcolor=THEME["colors"]["input_bg"],
            text_size=14,
            height=45,
            content_padding=10,
            hint_text="0.00 (+ o -)",
            on_change=self.on_liquidity_change
        )

        self.validation_gates_widget = ValidationGatesWidget()

        # Build Layout
        self.content = create_refinance_layout(
            col_deudas=self.col_deudas,
            liquidity_input=self.liquidity_input,
            validation_gates_widget=self.validation_gates_widget,
            on_calculate_callback=self.run_cnca,
            grid_cnca=self.grid_cnca,
            empty_state_cnca=self.empty_state_cnca,
            results_container_cnca=self.results_container_cnca,
            live_kpi_deuda=self.live_kpi_deuda,
            live_kpi_liquidez=self.live_kpi_liquidez,
            add_debt_callback=self.add_row,
            theme_colors=THEME["colors"],
            on_clear_callback=self.limpiar_todo
        )

    def on_liquidity_change(self, e):
        try:
            val_str = e.control.value.replace("$", "").replace(",", "")
            val = float(val_str) if val_str else 0.0
            self.payment_capacity["value"] = val
            self.upd_sum()
        except ValueError:
            pass

    def check_cnca_empty_state(self):
        has_results = len(self.grid_cnca.controls) > 0
        self.empty_state_cnca.visible = not has_results
        self.results_container_cnca.visible = has_results

    def limpiar_todo(self, e):
        self.col_deudas.controls.clear()
        self.liquidity_input.value = "0"
        try: self.validation_gates_widget._reset_gates()
        except: pass
        self.grid_cnca.controls.clear()
        self.check_cnca_empty_state()
        try:
            self.live_kpi_deuda.value = "$0.00"
            self.live_kpi_liquidez.value = "$0.00"
            self.live_kpi_deuda.update()
            self.live_kpi_liquidez.update()
        except: pass
        self.col_deudas.update()
        self.liquidity_input.update()
        if self.validation_gates_widget.page: self.validation_gates_widget.update()
        self.page.update()
        self.upd_sum()

    def msg(self, t, c=THEME["colors"]["success"]):
        self.page.snack_bar = ft.SnackBar(ft.Text(t, color="white"), bgcolor=c)
        self.page.snack_bar.open = True
        self.page.update()

    def upd_sum(self, e=None):
        try:
            credits_list = []
            total_pago = 0.0
            total_saldo = 0.0

            for d in self.col_deudas.controls:
                try:
                    data = d.get_data()
                    if data:
                        pago = float(data.get('pago', 0) or 0)
                        saldo = float(data.get('saldo', 0) or 0)
                        total_pago += pago
                        total_saldo += saldo

                        data_clean = data.copy()
                        data_clean['pago'] = pago
                        data_clean['saldo'] = saldo
                        data_clean['pagos'] = int(float(data.get('pagos', 0) or 0))
                        data_clean['_es_retencion'] = data_clean['pagos'] >= 24
                        data_clean['_es_tradicional'] = data_clean['pagos'] >= 16
                        credits_list.append(data_clean)
                except:
                    continue

            self.live_kpi_deuda.value = to_money(total_saldo)
            self.live_kpi_liquidez.value = to_money(total_pago)
            try:
                self.live_kpi_deuda.update()
                self.live_kpi_liquidez.update()
            except: pass

            if credits_list:
                try:
                    self.validation_gates_widget.validate_credits(
                        credits_list,
                        producto="CONSUPAGO",
                        incremento_liquidez=self.payment_capacity["value"],
                        plazo_nuevo=60
                    )
                except: pass
            else:
                try: self.validation_gates_widget._reset_gates()
                except: pass

        except Exception as ex:
            print(f"Error en upd_sum: {ex}")

    async def add_row(self, e=None):
        idx = len(self.col_deudas.controls) + 1

        def remove_wrapper(idx_unused):
            async def remove_task():
                row.animate_opacity = THEME["animations"]["fade"]
                row.animate_scale = THEME["animations"]["fade"]
                row.animate_offset = ft.Animation(350, ft.AnimationCurve.EASE_IN)
                row.opacity = 0
                row.scale = 0.8
                row.offset = ft.Offset(-0.3, 0)
                row.update()
                await asyncio.sleep(0.4)
                try:
                    if row in self.col_deudas.controls:
                        self.col_deudas.controls.remove(row)
                        self.col_deudas.update()
                        self.upd_sum()
                except Exception as e:
                    print(f"Error removing row: {e}")
            self.page.run_task(remove_task)

        row = DebtRow(idx,
                     remove_wrapper,
                     self.upd_sum,
                     idx % 2 == 0)

        row.opacity = 0
        row.scale = 0.95
        row.offset = ft.Offset(0.1, 0)

        self.col_deudas.controls.append(row)
        self.col_deudas.update()

        await asyncio.sleep(0.05)
        row.opacity = 1
        row.scale = 1
        row.offset = ft.Offset(0, 0)
        row.update()

        self.upd_sum()

    async def run_cnca(self, e):
        try:
            # Feedback inicial
            if self.sound_manager: self.sound_manager.play("pop")

            # Recolectar datos
            ds = []
            for d in self.col_deudas.controls:
                data = d.get_data()
                if data:
                    ds.append(data)

            if not ds:
                await shake_control(self.col_deudas, self.page)
                self.msg("Agrega al menos una deuda", THEME["colors"]["error"])
                return

            aj = float(self.payment_capacity["value"])
            tot_pago = sum(float(x['pago']) for x in ds)
            tot_deuda = sum(float(x['saldo']) for x in ds)
            cap = tot_pago + aj

            # --- START PROCESS ---
            self.grid_cnca.controls.clear()
            self.check_cnca_empty_state()

            # Calcular ofertas
            ops = []
            productos = ["CONSUBANCO", "CONSUPAGO", "MASNOMINA", "OPCIPRES"]
            plazos = [60, 54, 48, 36, 24] # Prioridad a plazos largos

            # Usar MotorFinanciero
            # (Aquí simplifico la lógica de iteración, asumiendo que MotorFinanciero hace el trabajo pesado
            # pero el código original iteraba manualmente. Copiaré la lógica de iteración básica)

            for prod in productos:
                for pl in plazos:
                    # Intento Retención
                    if self.config_manager.datos["RETENCION"].get(prod):
                        res = self.motor.calcular(prod, cap, pl, False, True)
                        if res and res['monto'] >= tot_deuda:
                            remanente = res['monto'] - tot_deuda

                            # Validar normativa
                            valido, razones = self.motor.validar_normativa_cnca(prod, ds, aj, remanente, pl, True)

                            if valido:
                                res.update({
                                    'producto': prod, 'plazo': pl, 'tipo_tasa': 'RETENCION',
                                    'remanente': remanente, 'pago_visual': res['pago'],
                                    'razones': razones
                                })
                                ops.append(res)
                                continue # Si hay retención, no buscar tradicional para este plazo/prod

                    # Intento Tradicional
                    res = self.motor.calcular(prod, cap, pl, False, False)
                    if res and res['monto'] >= tot_deuda:
                        remanente = res['monto'] - tot_deuda
                        valido, razones = self.motor.validar_normativa_cnca(prod, ds, aj, remanente, pl, False)
                        if valido:
                            res.update({
                                'producto': prod, 'plazo': pl, 'tipo_tasa': 'TRADICIONAL',
                                'remanente': remanente, 'pago_visual': res['pago'],
                                'razones': razones
                            })
                            ops.append(res)

            # Ordenar por remanente
            ops.sort(key=lambda x: x['remanente'], reverse=True)

            # Renderizar resultados
            if not ops:
                if self.sound_manager: self.sound_manager.play("wrong")
                self.grid_cnca.controls.append(ft.Container(
                    content=ft.Column([
                        ft.Icon(ft.Icons.SEARCH_OFF_ROUNDED, size=50, color=THEME["colors"]["text_tertiary"]),
                        ft.Text("Sin opciones viables", color=THEME["colors"]["text_secondary"], weight="bold"),
                        ft.Text("Intenta ajustar la liquidez.", size=12, color=THEME["colors"]["text_tertiary"])
                    ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=5),
                    alignment=ft.Alignment(0, 0), opacity=0.7
                ))
                await shake_control(self.grid_cnca, self.page)
                self.check_cnca_empty_state()
            else:
                if self.sound_manager: self.sound_manager.play("cash")
                self.results_container_cnca.visible = True
                self.empty_state_cnca.visible = False

                # Progressive Rendering wrapper
                def create_cnca_card(item):
                    o, i = item
                    is_best = (i == 0)
                    delay = i * 100

                    c = FintechCard(
                        data=o, is_best=is_best, tipo="CNCA",
                        on_layout=lambda d: print("Detalle"), # Placeholder
                        on_detail=lambda d: print("Detalle"),
                        on_compare=lambda d: print("Comparar"),
                        on_amortization=lambda d: print("Amortización"),
                        page=self.page, delay_ms=delay
                    )

                    grid_config = {"xs": 12, "md": 6, "xl": 4}
                    c.col = grid_config

                    if self.offer_comparator:
                        selectable_card = create_selectable_offer_card(o, self.offer_comparator, c, self.page)
                        if hasattr(selectable_card, 'col'):
                            selectable_card.col = grid_config
                        return selectable_card
                    else:
                        return c

                items_with_index = [(o, i) for i, o in enumerate(ops)]

                await render_cards_progressively(
                    container=self.grid_cnca,
                    items=items_with_index,
                    builder_func=create_cnca_card,
                    batch_size=8,
                    delay_ms=15
                )

                # Save history
                if self.history_manager:
                    self.history_manager.add_calculation({
                        'tipo': 'CNCA',
                        'cliente': 'Consulta CNCA',
                        'deudas': ds,
                        'resultados': ops[:5],
                        'mejor_opcion': ops[0]
                    })

            self.page.update()

        except Exception as ex:
            import traceback
            traceback.print_exc()
            self.msg(f"Error: {str(ex)[:100]}", THEME["colors"]["error"])
