import flet as ft
import asyncio
from ui.theme import THEME

class JuicySidebar(ft.Container):
    """Sidebar flotante con efecto Glassmorphism y Responsividad"""
    def __init__(self, page, on_nav_change, perfil=None, responsive_manager=None):
        super().__init__()
        self._page_ref = page
        self.on_nav_change = on_nav_change
        self.rm = responsive_manager
        self.selected_index = 0
        self.is_collapsed = False  # Estado de colapso

        # Estilo Base - Glassmorphism
        # Ancho fijo Slim (expandido) o mini (colapsado)
        self.width = 260

        self.padding = 20
        self.bgcolor = "#F2FFFFFF" if page.theme_mode == ft.ThemeMode.LIGHT else "#F20F172A"
        self.blur = ft.Blur(10, 10, ft.BlurTileMode.MIRROR)
        self.border_radius = ft.BorderRadius.only(top_right=20, bottom_right=20)
        self.shadow = ft.BoxShadow(blur_radius=20, color="#26000000", offset=ft.Offset(5, 0))

        # Animación suave para cambio de tamaño
        self.animate = ft.Animation(300, ft.AnimationCurve.EASE_OUT_CUBIC)

        self.menu_items = [
            {"icon": ft.Icons.PIE_CHART_ROUNDED, "label": "Refinanciar", "color": "#10B981", "bg": "#ECFDF5"},  # Emerald
            {"icon": ft.Icons.ADD_CARD_ROUNDED, "label": "Crédito Nuevo", "color": "#F59E0B", "bg": "#FFFBEB"}, # Amber/Gold
            {"icon": ft.Icons.VERIFIED_USER_ROUNDED, "label": "Seguros", "color": "#8B5CF6", "bg": "#F5F3FF"},     # Violet
            {"icon": ft.Icons.PEOPLE_ROUNDED, "label": "Agenda", "color": "#3B82F6", "bg": "#EFF6FF"},   # Blue (Agenda)
        ]

        # Widget de Perfil Simple
        if perfil is None:
            perfil = {}
        nombre_asesor = perfil.get("nombre", "Asesor")[:15]

        # WIDGET DE PERFIL (SIMPLIFICADO) - Referencia para ocultar/mostrar
        self.profile_widget = ft.Container(
            content=ft.Row([
                # Avatar Simple
                ft.Container(
                    content=ft.Icon(ft.Icons.PERSON_ROUNDED, color="white", size=24),
                    alignment=ft.Alignment(0, 0),
                    width=48, height=48,
                    bgcolor=THEME["colors"]["accent"],
                    border_radius=24,
                    shadow=ft.BoxShadow(
                        blur_radius=8,
                        color="#40000000",
                        offset=ft.Offset(0, 4)
                    )
                ),
                # Info del usuario
                ft.Column([
                    ft.Text(
                        nombre_asesor,
                        size=14,
                        weight="bold",
                        color=THEME["colors"]["text_primary"]
                    ),
                    ft.Text(
                        "Asesor Financiero",
                        size=11,
                        color=THEME["colors"]["text_secondary"]
                    ),
                ], spacing=2, expand=True)
            ], alignment=ft.MainAxisAlignment.START, spacing=12),
            padding=ft.Padding.all(14),
            bgcolor=THEME["colors"]["card_bg"],
            border=ft.Border.all(1, THEME["colors"]["divider"]),
            border_radius=16,
            shadow=ft.BoxShadow(
                blur_radius=4,
                color="#10000000",
                offset=ft.Offset(0, 2)
            )
        )

        self.menu_column = ft.Column([self._build_btn(i, item) for i, item in enumerate(self.menu_items)], spacing=8)

        # Indicador animado para el item seleccionado
        self.animated_indicator = ft.Container(
            width=6,  # Ancho más visible
            height=48,
            bgcolor=self.menu_items[0]["color"],  # Color del primer item
            border_radius=ft.BorderRadius.only(top_right=4, bottom_right=4),
            offset=ft.Offset(0, 0),
            animate_offset=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT_CUBIC),
            animate=ft.Animation(400, ft.AnimationCurve.EASE_IN_OUT_CUBIC),  # Anima bgcolor también
            disabled=True  # No captura eventos de mouse
        )

        # Botón Toggle para colapsar/expandir (con icono chevron)
        self.toggle_btn = ft.IconButton(
            icon=ft.Icons.CHEVRON_LEFT_ROUNDED,
            icon_color=THEME["colors"]["text_secondary"],
            icon_size=20,
            tooltip="Contraer sidebar",
            on_click=self._toggle_collapse
        )

        # Logo circular con gradiente dinámico (cambia según pestaña activa)
        self.logo_container = ft.Container(
            content=ft.Icon(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, color="white", size=28),
            width=56,
            height=56,
            gradient=ft.LinearGradient(
                begin=ft.Alignment(-1, -1),
                end=ft.Alignment(1, 1),
                colors=["#10B981", "#059669"]  # Verde inicial (Refinanciar)
            ),
            border_radius=28,
            alignment=ft.Alignment(0, 0),
            shadow=ft.BoxShadow(
                blur_radius=12,
                color="#3310B981",
                offset=ft.Offset(0, 3)
            ),
            animate=ft.Animation(300, "easeOut")  # Animar cambios de gradiente
        )

        # Wrapper para mantener forma circular al contraer sidebar
        self.logo_wrapper = ft.Container(
            content=self.logo_container,
            width=56,
            height=56,
            alignment=ft.Alignment(0, 0)
        )


        # Textos que se ocultarán al colapsar
        self.app_title = ft.Text(
            "ConsuCalcu",
            size=20,
            weight="w900",
            font_family="Outfit",
            color=THEME["colors"]["text_primary"]
        )
        self.app_subtitle = ft.Text(
            "Calculadora IMSS",
            size=10,
            color=THEME["colors"]["text_tertiary"],
            italic=True
        )
        self.nav_header = ft.Text(
            "NAVEGACIÓN",
            size=10,
            weight="bold",
            color=THEME["colors"]["text_tertiary"],
            text_align=ft.TextAlign.LEFT
        )
        self.config_header = ft.Text(
            "CONFIGURACIÓN",
            size=10,
            weight="bold",
            color=THEME["colors"]["text_tertiary"],
            text_align=ft.TextAlign.LEFT
        )
        # Badge de versión con fondo
        self.version_badge = ft.Container(
            content=ft.Row([
                ft.Text("v2.7", size=10, weight="bold", color="#6366F1"),
                ft.Icon(ft.Icons.VERIFIED_ROUNDED, size=12, color=THEME["colors"]["success"])
            ], spacing=4, tight=True),
            padding=ft.Padding.symmetric(horizontal=10, vertical=4),
            bgcolor="#EEF2FF",  # Indigo 50
            border_radius=12,
            border=ft.Border.all(1, "#C7D2FE")  # Indigo 200
        )


        # Iconos de configuración con hover mejorado
        self.icon_perf = ft.IconButton(
            icon=ft.Icons.SPEED_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Rendimiento",
            on_click=self._open_performance,
            animate_scale=ft.Animation(150, "easeOut")
        )

        self.icon_sync = ft.IconButton(
            icon=ft.Icons.SYNC_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Buscar Actualizaciones",
            on_click=self._check_update,
            animate_scale=ft.Animation(150, "easeOut")
        )

        self.icon_settings = ft.IconButton(
            icon=ft.Icons.SETTINGS_ROUNDED,
            icon_color="#9CA3AF",
            icon_size=20,
            tooltip="Configuración",
            on_click=self._open_config,
            animate_scale=ft.Animation(150, "easeOut")
        )

        # Asignar hover handlers
        self.icon_perf.on_hover = lambda e: self._hover_config_icon(e, self.icon_perf)
        self.icon_sync.on_hover = lambda e: self._hover_config_icon(e, self.icon_sync)
        self.icon_settings.on_hover = lambda e: self._hover_config_icon(e, self.icon_settings)

        # Fila de iconos de configuración
        self.settings_icons_row = ft.Row(
            [self.icon_perf, self.icon_sync, self.icon_settings],
            alignment=ft.MainAxisAlignment.CENTER,
            spacing=0
        )

        self.content = ft.Column([
            # ===== HEADER SECTION =====
            ft.Container(
                content=ft.Column([
                    # Botón Toggle siempre visible en la esquina superior
                    ft.Row([
                        ft.Container(expand=True),
                        self.toggle_btn
                    ], alignment=ft.MainAxisAlignment.END),

                    ft.Container(height=8),

                    # Logo compacto dinámico
                    self.logo_wrapper,

                    # Títulos (se ocultan al colapsar)
                    self.app_title,
                    self.app_subtitle
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=8),
                padding=ft.Padding.only(bottom=16, top=8)
            ),

            ft.Divider(height=1, color=THEME["colors"]["divider"]),

            # ===== PROFILE SECTION =====


            # ===== NAVIGATION SECTION =====
            ft.Container(
                content=ft.Column([
                    self.nav_header,
                    ft.Container(height=8),
                    # Layout Horizontal: Indicador | Botones
                    ft.Row([
                        # Carril del indicador (Izquierda)
                        ft.Container(
                            width=6,
                            content=ft.Stack([self.animated_indicator], height=300),
                            alignment=ft.Alignment(-1, -1)
                        ),
                        # Columna de botones (Derecha)
                        ft.Container(content=self.menu_column, expand=True)
                    ], spacing=0, vertical_alignment=ft.CrossAxisAlignment.START),
                ], spacing=0),
                padding=ft.Padding.only(bottom=12)
            ),

            ft.Container(expand=True), # Spacer flexible

            # ===== SETTINGS SECTION =====
            ft.Divider(height=1, color=THEME["colors"]["divider"]),
            ft.Container(
                content=ft.Column([
                    self.config_header,
                    # Fila de Acciones Rápidas (Flat Design) - Referencia para ocultar
                    self.settings_icons_row,
                ], spacing=0),
                padding=ft.Padding.symmetric(vertical=16)
            ),

            # ===== FOOTER =====
            ft.Container(
                content=self.version_badge,
                padding=ft.Padding.only(bottom=12),
                alignment=ft.Alignment(0, 0)
            )
        ], expand=True)

    def _toggle_collapse(self, e):
        """Alternar entre sidebar expandida y colapsada con animación fluida"""
        # Debounce: Evitar clics múltiples mientras anima
        if self.toggle_btn.disabled: return
        self.toggle_btn.disabled = True
        self.toggle_btn.update()

        self.is_collapsed = not self.is_collapsed

        # 1. Configurar width principal (animado automáticamente)
        if self.is_collapsed:
            self.width = 80
            self.padding = 16  # Aumentado de 12 a 16
            self.toggle_btn.icon = ft.Icons.CHEVRON_RIGHT_ROUNDED
            self.toggle_btn.tooltip = "Expandir sidebar"

            # Ocultar headers secundarios inmediatamente para evitar overflow visual durante la transición
            self.app_title.visible = False
            self.app_subtitle.visible = False
            self.nav_header.visible = False
            self.config_header.visible = False
            self.version_badge.visible = False
            self.settings_icons_row.visible = False
        else:
            self.width = 260
            self.padding = 20
            self.toggle_btn.icon = ft.Icons.CHEVRON_LEFT_ROUNDED
            self.toggle_btn.tooltip = "Contraer sidebar"

            # Mostrar headers (podría retardarse un poco si se quisiera exquisitez)
            self.app_title.visible = True
            self.app_subtitle.visible = True
            self.nav_header.visible = True
            self.config_header.visible = True
            self.version_badge.visible = True
            self.settings_icons_row.visible = True

        # 2. Actualizar botones IN-PLACE (Sin reconstruir) - Esto mantiene la animación suave
        for i, btn in enumerate(self.menu_column.controls):
            content_row = btn.content

            # Contenedor de Texto (Hijo 1 del Row)
            text_container = content_row.controls[1]

            # Animamos propiedades
            text_container.width = 140 if not self.is_collapsed else 0
            text_container.opacity = 1 if not self.is_collapsed else 0

            # Ajustamos padding y spacing
            current_padding = 12 if not self.is_collapsed else 8
            btn.padding = ft.Padding.symmetric(horizontal=current_padding, vertical=12)
            content_row.spacing = 12 if not self.is_collapsed else 0

            # Actualizar Tooltip dinámicamente
            # Necesitamos el label del item correspondiente
            if i < len(self.menu_items):
                item_label = self.menu_items[i]["label"]
                btn.tooltip = item_label if self.is_collapsed else None

        self.update()

        # Rehabilitar botón después de la animación (Async)
        async def enable_btn():
            await asyncio.sleep(0.35)
            self.toggle_btn.disabled = False
            try:
                self.toggle_btn.update()
            except: pass

        if hasattr(self.page, 'run_task'):
             self.page.run_task(enable_btn)

    def _build_btn(self, index, item):
        is_selected = self.selected_index == index

        # Colores
        accent_color = item.get("color", "#10B981")
        accent_bg = item.get("bg", "#ECFDF5")

        if is_selected:
            bg_color = accent_bg
            text_color = accent_color
            icon_color = accent_color
            icon_size = 24
            font_weight = "bold"
        else:
            bg_color = "transparent"
            text_color = "#6B7280"
            icon_color = "#9CA3AF"
            icon_size = 22
            font_weight = "w500"

        # Estructura Híbrida (Icono Fijo + Texto Animado)
        btn_content = ft.Row([
            # 1. Icono (Siempre visible, centrado en su tile)
            ft.Container(
                content=ft.Icon(item["icon"], color=icon_color, size=icon_size),
                width=32,
                alignment=ft.Alignment(0, 0),
            ),
            # 2. Texto (Animable width/opacity)
            ft.Container(
                content=ft.Text(
                    item["label"],
                    size=14,
                    color=text_color,
                    weight=font_weight,
                    no_wrap=True # Vital para que no salte línea al encogerse
                ),
                width=140 if not self.is_collapsed else 0,
                opacity=1 if not self.is_collapsed else 0,
                animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT_CUBIC), # Animación suave 60fps
                animate_opacity=ft.Animation(150, ft.AnimationCurve.EASE_IN_OUT),
                clip_behavior=ft.ClipBehavior.HARD_EDGE # Recorte limpio
            )
        ], spacing=12 if not self.is_collapsed else 0, alignment=ft.MainAxisAlignment.START)

        # Wrapper para manejar evento async con argumento
        async def btn_click(e):
            await self._handle_click(index)

        btn = ft.Container(
            content=btn_content,
            padding=ft.Padding.symmetric(horizontal=12 if not self.is_collapsed else 8, vertical=12),
            height=48,
            bgcolor=bg_color,
            border_radius=ft.BorderRadius.only(top_right=8, bottom_right=8),
            on_click=btn_click,
            animate=ft.Animation(200, ft.AnimationCurve.EASE_OUT), # Ultra rápido y lineal para borrar rastro inmediato
            tooltip=item["label"] if self.is_collapsed else None
        )

        # Hover Effect Simple (Corregido para respetar selección actual)
        def on_hover(e):
             # Verificar estado actual dinámicamente (no usar variable capturada)
             if self.selected_index != index:
                 # Color hover sutil (gris muy claro en light, gris oscuro en dark)
                 hover_color = "#F9FAFB" if self.page.theme_mode == ft.ThemeMode.LIGHT else "#374151"
                 e.control.bgcolor = hover_color if e.data == "true" else "transparent"
                 e.control.update()

        btn.on_hover = on_hover
        return btn

    async def _handle_click(self, index):
        self.selected_index = index

        # 1. UI PRIMERO: Actualizar indicador y botones
        step_offset = 56 / 48
        indicator_offset = index * step_offset
        self.animated_indicator.offset = ft.Offset(0, indicator_offset)
        self.animated_indicator.bgcolor = self.menu_items[index]["color"]
        self.animated_indicator.update()

        # 2. Actualizar color del logo según pestaña activa
        color_map = {
            0: (["#10B981", "#059669"], "#3310B981"),  # Verde (Refinanciar)
            1: (["#F59E0B", "#D97706"], "#33F59E0B"),  # Naranja/Dorado (Crédito Nuevo)
            2: (["#8B5CF6", "#7C3AED"], "#338B5CF6"),  # Púrpura (Seguros)
            3: (["#3B82F6", "#2563EB"], "#333B82F6")   # Azul (Seguimiento)
        }

        new_colors, shadow_color = color_map.get(index, (["#10B981", "#059669"], "#3310B981"))
        self.logo_container.gradient.colors = new_colors
        self.logo_container.shadow.color = shadow_color
        self.logo_container.update()

        # Actualizar colores de botones (In-Place)
        for i, btn in enumerate(self.menu_column.controls):
            item = self.menu_items[i]
            is_selected = i == index

            accent_color = item.get("color", "#10B981")
            accent_bg = item.get("bg", "#ECFDF5")

            if is_selected:
                # Entrada: Suave
                btn.animate = ft.Animation(200, "easeOut")
                btn.bgcolor = accent_bg
            else:
                # Salida: Instantánea
                btn.animate = ft.Animation(1, "linear")
                btn.bgcolor = "transparent"

            content_row = btn.content
            content_row.controls[0].content.color = accent_color if is_selected else "#9CA3AF"

            txt_ctrl = content_row.controls[1].content
            txt_ctrl.color = accent_color if is_selected else "#6B7280"
            txt_ctrl.weight = "bold" if is_selected else "w500"

        self.menu_column.update()

        # 2. PEQUEÑA PAUSA (Aumentada) para permitir que el renderizado visual termine suavemente
        await asyncio.sleep(0.05)

        # 3. CAMBIO DE VISTA (Lógica pesada)
        try:
            res = self.on_nav_change(index)
            if hasattr(res, '__await__'):
                await res
        except Exception as ex:
            print(f"Error navigating: {ex}")

    def _toggle_theme(self, e):
        # Cambiar tema
        self.page.theme_mode = (
            ft.ThemeMode.DARK
            if self.page.theme_mode == ft.ThemeMode.LIGHT
            else ft.ThemeMode.LIGHT
        )

        # Actualizar icono del botón
        e.control.icon = (
            ft.Icons.DARK_MODE_ROUNDED
            if self.page.theme_mode == ft.ThemeMode.LIGHT
            else ft.Icons.LIGHT_MODE_ROUNDED
        )

        # Actualizar color de fondo del sidebar
        self.bgcolor = (
            "#FFFFFF"
            if self.page.theme_mode == ft.ThemeMode.LIGHT
            else "#1F2937"  # Gray 800 para dark mode
        )

        # Actualizar toda la página
        self.update()
        self.page.update()

    def _check_update(self, e):
        # Esta función será conectada desde main
        if hasattr(self.page, '_verificar_update_click'):
            self.page._verificar_update_click(e)

    def _open_config(self, e):
        # Esta función será conectada desde main
        if hasattr(self.page, '_config_dlg'):
            self.page._config_dlg(e)
        else:
            print("[ERROR] _config_dlg not found on page")

    def _open_performance(self, e):
        # Esta función será conectada desde main
        if hasattr(self.page, '_performance_dlg'):
            self.page._performance_dlg(e)
        else:
            print("[ERROR] _performance_dlg not found on page")

    def _hover_config_icon(self, e, icon_btn):
        """Maneja el efecto hover de los iconos de configuración"""
        if e.data == "true":
            icon_btn.icon_color = THEME["colors"]["accent"]
            icon_btn.scale = 1.1
        else:
            icon_btn.icon_color = "#9CA3AF"
            icon_btn.scale = 1.0
        icon_btn.update()

    def resize(self):
        """Actualiza dimensiones y estado visual basadas en ResponsiveManager"""
        if self.rm:
            target_width = self.rm.get_size("layout", "sidebar_width")

            # Solo actualizar si el ancho objetivo es diferente
            if self.width != target_width:
                self.width = target_width

                # Sincronizar estado is_collapsed con el nuevo ancho
                should_be_collapsed = target_width < 150

                # Si el estado visual no coincide con el ancho, forzar actualización visual
                if self.is_collapsed != should_be_collapsed:
                    self.is_collapsed = should_be_collapsed

                    # Actualizar visibilidad de headers
                    visible = not self.is_collapsed
                    self.profile_widget.visible = visible
                    self.app_title.visible = visible
                    self.app_subtitle.visible = visible
                    self.nav_header.visible = visible
                    self.config_header.visible = visible
                    self.version_badge.visible = visible
                    self.settings_icons_row.visible = visible

                    # Actualizar botón toggle
                    self.toggle_btn.icon = ft.Icons.CHEVRON_LEFT_ROUNDED if visible else ft.Icons.CHEVRON_RIGHT_ROUNDED
                    self.padding = 20 if visible else 16  # Corregido de 12 a 16

                    # Actualizar items del menú
                    for i, btn in enumerate(self.menu_column.controls):
                        content_row = btn.content
                        if len(content_row.controls) > 1:
                            text_container = content_row.controls[1]
                            text_container.width = 140 if visible else 0
                            text_container.opacity = 1 if visible else 0
                            content_row.spacing = 12 if visible else 0

                            if i < len(self.menu_items):
                                btn.tooltip = self.menu_items[i]["label"] if not visible else None

                self.update()
