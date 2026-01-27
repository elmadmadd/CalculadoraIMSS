import flet as ft
import asyncio
from ui.theme import THEME, BRAND_COLORS
from ui.utils import animate_rolling_currency
from utils.formatting import to_money
from core.performance import performance_manager
from ui.effects import TransitionEffects
from utils.image import GeneradorImagen

try:
    from ui.effects import TransitionEffects
    HAVE_TRANSITIONS = True
except ImportError:
    HAVE_TRANSITIONS = False

class FintechCard(ft.Container):
    """
    Componente UI 2.0: Tarjeta de alto rendimiento con Motion Design.
    Muestra la Liquidez como dato principal y el Monto Bruto como secundario.

    MEJORAS 2026:
    - Integración con sistema de efectos de transición (transition_effects.py)
    - Efectos dinámicos según tipo: GLOW (mejor opción), SMOOTH (retención), ELEVATION (tradicional)
    - Sombras y escalas adaptativas según el contexto de la tarjeta
    """
    def __init__(self, data, is_best, tipo, on_layout, on_detail, on_compare, on_amortization, page=None, delay_ms=0):
        # Guardar callback principal explícitamente
        self.on_layout_fn = on_layout

        super().__init__(
            animate_opacity=THEME["animations"]["smooth"],
            animate_offset=THEME["animations"]["bouncy"],
            animate_scale=THEME["animations"]["standard"],
            animate=THEME["animations"]["fast"],
            opacity=0,
            offset=ft.Offset(0, 0.2),
            scale=0.95,
            # on_click removido - se manejará en _handle_click
            border_radius=18,
            padding=0,
            margin=ft.Margin.only(bottom=16),
        )
        self.data = data
        self.is_best = is_best
        self.tipo = tipo
        self._initial_page = page # Guardar referencia sin conflicto con self.page
        self.delay_ms = delay_ms

        # Callbacks secundarios
        self.on_detail = on_detail
        self.on_compare = on_compare
        self.on_amortization = on_amortization

        # --- CONFIGURACIÓN DE EFECTOS ---
        if HAVE_TRANSITIONS:
            # Todas las tarjetas usan el mismo efecto (sin distinción especial para is_best)
            if data.get('tipo_tasa') == 'RETENCION':
                self.transition_effect = TransitionEffects.SMOOTH
            else:
                self.transition_effect = TransitionEffects.ELEVATION
        else:
            self.transition_effect = None

        self.colors = self._get_semantic_colors()

        # Aplicar colores y bordes
        if self.colors.get("gradient"):
            self.gradient = self.colors["gradient"]
        else:
            self.bgcolor = self.colors["bg"]

        if self.colors.get("border"):
            self.border = self.colors["border"]

        self.shadow = self._get_shadow(is_hover=False)

        # Estado de animación
        self._is_hovering = False
        self._is_pressing = False

        # Contenido con animaciones rediseñadas
        self.content = self._build_content()
        self.on_hover = self._handle_hover
        self.on_click = self._handle_click

    def _handle_hover(self, e):
        """Animación de hover sutil y elegante"""
        mouse_entered = str(e.data).lower() == "true"

        if mouse_entered and not self._is_pressing:
            self._is_hovering = True
            # Elevación suave
            self.scale = 1.015  # Más sutil que antes
            self.shadow = ft.BoxShadow(
                blur_radius=20,
                spread_radius=2,
                color="#20000000",
                offset=ft.Offset(0, 8)
            )

            # Borde sutil para mejor opción
            if self.is_best:
                self.border = ft.Border.all(2, THEME["colors"]["accent"])
        else:
            self._is_hovering = False
            if not self._is_pressing:
                # Restaurar estado normal
                self.scale = 1.0
                self.shadow = self._get_shadow(is_hover=False)

                if self.is_best:
                    self.border = ft.Border.all(1, "#E0E0E0")
                else:
                    self.border = None

        try:
            self.update()
        except:
            pass

    async def _handle_click(self, e):
        """Manejo de click directo sin animación (Optimizado)"""
        if self.on_layout_fn:
            try:
                # Ejecución directa e inmediata
                self.on_layout_fn(self.data)
            except Exception as ex:
                print(f"❌ Error al ejecutar acción de tarjeta: {ex}")
                # Intentar recuperar si falló visualmente
                try: self.page.update()
                except: pass
        else:
            print("❌ Error: Callback on_layout_fn no definido")

    def did_mount(self):
        """Animación de entrada y números girando (adaptativa según rendimiento)"""
        async def sequence():
            # Aplicar multiplicador de delay según rendimiento
            adjusted_delay = self.delay_ms * performance_manager.get_delay_multiplier()

            if adjusted_delay > 0:
                await asyncio.sleep(adjusted_delay / 1000)

            # Verificar que el control sigue en la página antes de actualizar
            if not self.page:
                return

            self.opacity = 1
            self.offset = ft.Offset(0, 0)

            try:
                self.update()
            except AssertionError:
                # Control ya no está en la página, ignorar
                return

            # Animamos los números hacia el target calculado (Liquidez)
            if self.page:
                if not performance_manager.should_reduce_animations():
                    await asyncio.gather(
                        animate_rolling_currency(self.lbl_monto, 0, self.target_monto, duration=0.8),
                        animate_rolling_currency(self.lbl_pago, 0, self.data.get('pago', 0), duration=0.8)
                    )
                else:
                    # Modo Rendimiento: Asignación directa sin animación
                    self.lbl_monto.value = to_money(self.target_monto)
                    self.lbl_pago.value = to_money(self.data.get('pago', 0))
                    self.lbl_monto.update()
                    self.lbl_pago.update()

        if self.page:
            self.page.run_task(sequence)

    def _get_semantic_colors(self):
        is_dark = self._initial_page.theme_mode == ft.ThemeMode.DARK if self._initial_page else False
        is_retencion = self.data.get('tipo_tasa') == 'RETENCION'

        # Paleta Lime & Navy (Ajustada)
        NAVY_GRADIENT_START = "#0F4C81" # Classic Blue
        NAVY_GRADIENT_END = "#1B365D"   # Traditional Navy
        LIME_ACCENT = "#84CC16"         # Lime 500

        if self.is_best:
            # MEJOR OPCIÓN: Premium Light (Gold Accent)
            return {
                "bg": "#FFFFFF",
                "gradient": None,
                "text_main": "#0F172A",
                "text_sec": "#64748B",
                "accent": "#D97706",
                "badge_bg": ft.LinearGradient(
                    colors=["#F59E0B", "#D97706"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                ),
                "badge_text": "white",
                "divider": "#F1F5F9",
                "border": ft.Border.all(1, "#FDE68A") # Gold suave (Amber 200) 1px
            }
        else:
            # OPCIONES NORMALES
            if is_retencion:
                accent_color = "#10B981"  # Emerald
                badge_gradient = ft.LinearGradient(
                    colors=["#10B981", "#059669"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                )
                border_col = "#E5E7EB"
            else:
                accent_color = "#3B82F6"  # Blue
                badge_gradient = ft.LinearGradient(
                    colors=["#3B82F6", "#2563EB"],
                    begin=ft.Alignment.CENTER_LEFT,
                    end=ft.Alignment.CENTER_RIGHT
                )
                border_col = "#E5E7EB"

            return {
                "bg": "#FFFFFF",
                "gradient": None,
                "text_main": "#0F172A",
                "text_sec": "#64748B",
                "accent": accent_color,
                "badge_bg": badge_gradient,
                "badge_text": "white",
                "divider": "#E5E7EB",
                "border": ft.Border.all(1, border_col) # Gray border
            }

    def _get_shadow(self, is_hover):
        # Sombra uniforme para todas las tarjetas (sin distinción por is_best)
        color = "#15000000"
        if is_hover:
            return ft.BoxShadow(blur_radius=25, color=color, offset=ft.Offset(0, 10))
        return ft.BoxShadow(blur_radius=15, color=color, offset=ft.Offset(0, 4))

    def _build_content(self):
        lbl_tipo = "RETENCIÓN" if self.data.get('tipo_tasa') == 'RETENCION' else "TRADICIONAL"

        # Colores del tema (Armonía)
        try:
            c_primary = THEME["colors"]["text_primary"]
        except:
             c_primary = "#1F2937"

        c_accent = THEME["colors"]["accent"]
        c_text_main = THEME["colors"]["text_primary"]
        c_text_sec = THEME["colors"]["text_secondary"]
        c_divider = THEME["colors"]["divider"]

        # Badge flotante (Más sutil)
        # ELIMINADO: Usuario solicitó quitar el badge verde de arriba por ser redundante.
        badge = None
        # if self.is_best:
        #    badge = ft.Container(...)

        # COLORES POR MARCA (definidos temprano para usar en todo el card)
        brand_colors_early = {
            "CONSUPAGO": "#D97706",
            "CONSUBANCO": "#0369A1",
            "MASNOMINA": "#475569",  # Más claro para mejor legibilidad
            "OPCIPRES": "#2563EB"
        }
        producto_upper_early = self.data['producto'].upper()
        brand_color = "#1F2937"  # Default
        for key, color in brand_colors_early.items():
            if key in producto_upper_early:
                brand_color = color
                break

        # Cálculo de montos
        monto_bruto = float(self.data.get('monto', 0))
        val_principal = monto_bruto
        texto_titulo = "MONTO A RECIBIR"

        if self.tipo == "CNCA":
            deuda_total = float(self.data.get('deuda_a_liquidar', 0))
            if deuda_total > 0:
                val_liquidez = monto_bruto - deuda_total
                if val_liquidez <= 0:
                    val_liquidez = float(self.data.get('remanente', 0))
                val_principal = val_liquidez
                texto_titulo = "REMANENTE CLIENTE"
            else:
                val_principal = float(self.data.get('remanente', 0)) or monto_bruto
                texto_titulo = "REMANENTE CLIENTE"

        self.target_monto = val_principal

        # Monto Principal (Hero) - COLOR DE MARCA
        self.lbl_monto = ft.Text(
            "$0.00",
            size=30,  # Un poco más grande
            weight="w800",
            color=brand_color,  # Usar color de marca
            font_family="Outfit",
            text_align=ft.TextAlign.CENTER,
            selectable=True
        )


        # Definir lbl_pago aquí para uso posterior
        self.lbl_pago = ft.Text(f"${self.data.get('pago_visual', 0):,.2f}", size=13, weight="bold", color=c_text_main)

        # Estilos de Marca
        brand_key = self.data['producto'].upper()
        # Mapeo simple si el nombre no coincide exactamente (ej. espacios)
        if "CONSUBANCO" in brand_key: brand_key = "CONSUBANCO"
        elif "CONSUPAGO" in brand_key: brand_key = "CONSUPAGO"
        elif "MASNOMINA" in brand_key: brand_key = "MASNOMINA"
        elif "OPCIPRES" in brand_key: brand_key = "OPCIPRES"

        brand_style = BRAND_COLORS.get(brand_key, BRAND_COLORS["DEFAULT"])

        # Header (Producto + Tipo)
        # Generar badges dinámicos para asesores
        badges_row = []

        # Badge: Mejor Oferta (is_best)
        if self.is_best:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.STAR_ROUNDED, size=12, color="#F59E0B"),
                        ft.Text("MEJOR OFERTA", size=9, weight="bold", color="#F59E0B")
                    ], spacing=3),
                    bgcolor="#FEF3C7",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#F59E0B")
                )
            )

        # Badge: Menor Pago (si es el menor pago de todas)
        # Este se agregará dinámicamente desde run_nuevo si es necesario

        # Badge: Plazo Corto (si plazo <= 36 meses)
        if self.data.get('plazo', 60) <= 36:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.FLASH_ON_ROUNDED, size=12, color="#10B981"),
                        ft.Text("PLAZO CORTO", size=9, weight="bold", color="#10B981")
                    ], spacing=3),
                    bgcolor="#D1FAE5",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#10B981")
                )
            )

        # Badge: Retención/SIPRE
        if self.data.get('tipo_tasa', '').upper() in ['RETENCIÓN', 'SIPRE', 'RETENCION']:
            badges_row.append(
                ft.Container(
                    content=ft.Row([
                        ft.Icon(ft.Icons.VERIFIED_ROUNDED, size=12, color="#8B5CF6"),
                        ft.Text("TASA PREFERENCIAL", size=9, weight="bold", color="#8B5CF6")
                    ], spacing=3),
                    bgcolor="#EDE9FE",
                    padding=ft.Padding.symmetric(horizontal=6, vertical=3),
                    border_radius=6,
                    border=ft.Border.all(1, "#8B5CF6")
                )
            )

        # Mapeo de logos de instituciones
        logo_map = {
            "CONSUPAGO": "csp.png",
            "CONSUBANCO": "csb.png",
            "MASNOMINA": "msn.png",
            "OPCIPRES": "opc.png"
        }

        # COLORES POR MARCA (refinados para mejor contraste)
        brand_colors = {
            "CONSUPAGO": {"primary": "#D97706", "light": "#FEF7ED", "border": "#F59E0B"},
            "CONSUBANCO": {"primary": "#0369A1", "light": "#F0F9FF", "border": "#0EA5E9"},
            "MASNOMINA": {"primary": "#475569", "light": "#F8FAFC", "border": "#64748B"},  # Más claro
            "OPCIPRES": {"primary": "#2563EB", "light": "#EFF6FF", "border": "#3B82F6"}
        }

        # Determinar colores de la marca
        producto_upper = self.data['producto'].upper()
        brand_key = "CONSUPAGO"  # Default
        for key in brand_colors:
            if key in producto_upper:
                brand_key = key
                break

        brand = brand_colors[brand_key]

        # Determinar logo a usar
        logo_path = None
        for key in logo_map:
            if key in producto_upper:
                logo_path = logo_map[key]
                break

        # === LOGO PREMIUM CON SOMBRA SUTIL ===
        if logo_path:
            logo_widget = ft.Container(
                content=ft.Image(
                    src=logo_path,
                    width=40,
                    height=40,
                    fit="contain"
                ),
                width=52,
                height=52,
                bgcolor="white",
                border_radius=26,
                alignment=ft.Alignment(0, 0),
                border=ft.Border.all(2.5, brand["border"]),
                shadow=ft.BoxShadow(
                    blur_radius=8,
                    color="#15000000",
                    offset=ft.Offset(0, 2)
                )
            )
        else:
            logo_widget = ft.Container(
                content=ft.Icon(ft.Icons.ACCOUNT_BALANCE_ROUNDED, size=26, color=brand["primary"]),
                width=52,
                height=52,
                bgcolor="white",
                border_radius=26,
                alignment=ft.Alignment(0, 0),
                border=ft.Border.all(2.5, brand["border"]),
                shadow=ft.BoxShadow(
                    blur_radius=8,
                    color="#15000000",
                    offset=ft.Offset(0, 2)
                )
            )

        # Badge de retención
        ret_badge = ft.Container(
            content=ft.Text("PREFERENCIAL", size=8, weight="bold", color="white"),
            bgcolor="#10B981",
            padding=ft.Padding.symmetric(horizontal=6, vertical=2),
            border_radius=4,
            visible=lbl_tipo == "RETENCIÓN"
        )

        # === HEADER CON GRADIENTE PARA MEJOR OPCIÓN ===
        header_bg = None
        if self.is_best:
            header_bg = ft.LinearGradient(
                begin=ft.Alignment(-1, 0),
                end=ft.Alignment(1, 0),
                colors=["#FFFBEB", "#FEF3C7"]
            )

        header = ft.Container(
            content=ft.Row([
                # Logo con sombra
                logo_widget,
                # Nombre + Plazo como badge
                ft.Column([
                    ft.Text(self.data['producto'], size=16, weight="w700", color=brand["primary"]),
                    ft.Row([
                        # Plazo como badge pill
                        ft.Container(
                            content=ft.Text(f"{self.data.get('plazo', 0)} meses", size=10, weight="w600", color=c_text_sec),
                            bgcolor="#F3F4F6",
                            padding=ft.Padding.symmetric(horizontal=8, vertical=3),
                            border_radius=10
                        ),
                        ret_badge
                    ], spacing=6)
                ], spacing=4, expand=True),
            ], spacing=14, vertical_alignment=ft.CrossAxisAlignment.CENTER),
            padding=ft.Padding.only(left=16, right=16, top=16, bottom=14),
            gradient=header_bg,
            border_radius=ft.BorderRadius.only(top_left=14, top_right=14) if self.is_best else None
        )

        # === HERO SECTION MINIMALISTA ===
        hero_section = ft.Container(
            content=ft.Column([
                # Monto principal (grande y centrado)
                self.lbl_monto,

                # Información secundaria (condicional según tipo)
                ft.Column([
                    # Monto bruto - SOLO para CNCA (refinanciamiento)
                    ft.Text(
                        f"Monto bruto: ${monto_bruto:,.2f}",
                        size=11,
                        color=c_text_sec,
                        weight="w500",
                        visible=self.tipo == "CNCA"  # Solo en refinanciamiento
                    ),
                    # Descuento mensual - siempre visible
                    ft.Text(
                        f"Descuento mensual: ${self.data.get('pago_visual', 0):,.2f}",
                        size=11,
                        color=c_text_sec
                    )
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=2)
            ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=6),
            padding=ft.Padding.symmetric(horizontal=16, vertical=16),
            alignment=ft.Alignment(0, 0)
        )

        # === FOOTER (SIEMPRE PRESENTE PARA ALTURA UNIFORME) ===
        # Contenido del footer solo si es mejor opción
        footer_content = None
        if self.is_best:
            footer_content = ft.Row([
                ft.Icon(ft.Icons.STAR_ROUNDED, size=14, color="#D97706"),
                ft.Text("Mejor opción", size=11, weight="w600", color="#D97706")
            ], spacing=4, alignment=ft.MainAxisAlignment.CENTER)

        footer = ft.Container(
            content=footer_content,
            padding=ft.Padding.symmetric(horizontal=16, vertical=8),
            bgcolor="#FEF3C7" if self.is_best else "transparent",
            height=32  # Altura fija para todas las tarjetas
        )

        # Estructura final (siempre incluye footer)
        main_content = ft.Column([
            header,
            hero_section,
            footer
        ], spacing=0)

        # Estilos premium según mejor opción
        border_color = "#F59E0B" if self.is_best else "#E5E7EB"
        border_width = 2.5 if self.is_best else 1
        bg_color = "#FFFEF7" if self.is_best else "white"

        # Sombra premium
        card_shadow = ft.BoxShadow(
            blur_radius=16 if self.is_best else 12,
            spread_radius=0,
            color="#20000000" if self.is_best else "#12000000",
            offset=ft.Offset(0, 4)
        )

        return ft.Container(
            content=main_content,
            bgcolor=bg_color,
            border=ft.Border.all(border_width, border_color),
            border_radius=16,
            shadow=card_shadow,
            on_click=lambda e: self.on_detail(self.data) if self.on_detail else None,
            ink=True,
            animate_scale=ft.Animation(200, "easeOut"),
            height=230  # Altura fija para todas las tarjetas
        )

    def _stat_block(self, label, value):
        return ft.Column([
            ft.Text(label, size=9, weight="bold", color=self.colors["text_sec"]),
            ft.Text(value, size=13, weight="bold", color=self.colors["text_main"])
        ], spacing=2)

    def _icon_action(self, icon, tooltip, on_click):
        return ft.IconButton(
            icon=icon,
            icon_size=18,  # Reducido para pantallas pequeñas
            icon_color=self.colors["text_sec"],
            tooltip=tooltip,
            on_click=on_click,
            style=ft.ButtonStyle(overlay_color=ft.Colors.with_opacity(0.1, self.colors["accent"]))
        )

    def on_export_image(self, e):
        """Genera la imagen y muestra una previsualización antes de confirmar"""

        # Acceso seguro a globales - We assume global 'loading' might not be available here,
        # so we rely on page overlay or loading state if passed.
        # But for now I'll just keep the logic

        try:
            # Generar la imagen temporalmente
            ok, res_path = GeneradorImagen.crear_card_img(self.data)

            if ok:
                # Mostrar diálogo de previsualización
                preview_img = ft.Image(
                    src=res_path,
                    width=400,
                    fit="contain",
                    border_radius=12,
                )

                def copiar_clipboard(e):
                    # Simular copia
                    if e.page:
                        e.page.set_clipboard(res_path)
                        snack = ft.SnackBar(ft.Text(f"Ruta copiada: {res_path}"), bgcolor=THEME["colors"]["success"])
                        e.page.overlay.append(snack)
                        snack.open = True
                        e.page.update()
                        dlg_preview.open = False
                        self.page.update()

                dlg_preview = ft.AlertDialog(
                    title=ft.Text("Vista Previa de Captura", size=16, weight="bold"),
                    content=ft.Column([
                        preview_img,
                        ft.Text(f"Guardado en: {res_path}", size=10, italic=True, color="grey")
                    ], tight=True, horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                    actions=[
                        ft.TextButton("Copiar Ruta", icon=ft.Icons.COPY, on_click=copiar_clipboard),
                        ft.TextButton("Listo", on_click=lambda e: [setattr(dlg_preview, "open", False), self.page.update()] if e.page else None),
                    ],
                    actions_alignment=ft.MainAxisAlignment.END,
                    shape=ft.RoundedRectangleBorder(radius=16)
                )
                if self.page:
                    self.page.dialog = dlg_preview
                    dlg_preview.open = True
                    self.page.update()
            else:
                print(f"Error generando imagen: {res_path}")

        except Exception as ex:
            print(f"Error generando imagen: {ex}")
