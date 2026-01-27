import flet as ft
from core.agenda_manager import AgendaManager
from ui.theme import THEME

class AgendaView(ft.Stack):
    def __init__(self, page, perfil, sound_manager=None):
        super().__init__(expand=True)
        self.page = page
        self.perfil = perfil
        self.sound_manager = sound_manager
        self.agenda_manager = AgendaManager()

        self.clientes_seleccionados = set()
        self.paginacion_limite = 20
        self.paginacion_offset = 0
        self.cargando_mas_clientes = False
        self.fin_de_lista = False

        self.setup_ui()
        self.cargar_clientes()

    def setup_ui(self):
        # KPI Cards
        self.kpi_total_clientes = ft.Text("0", size=32, weight="w800", color="#1E293B")
        self.kpi_prospectos = ft.Text("0", size=32, weight="w800", color="#1E293B")
        self.kpi_en_proceso = ft.Text("0", size=32, weight="w800", color="#1E293B")
        self.kpi_aprobados = ft.Text("0", size=32, weight="w800", color="#1E293B")

        # Formulario
        self.txt_datos_crudos = ft.TextField(
            label="Pegar datos del cliente aquí", multiline=True, min_lines=8, max_lines=12,
            hint_text="Ejemplo:\nTEL1: ...\nNOMBRE: ...", text_size=13
        )
        self.dd_estatus_cliente = ft.Dropdown(
            label="Estatus", options=[ft.dropdown.Option(x) for x in ["Prospecto", "Contactado", "En Proceso", "Aprobado", "Rechazado", "Inactivo"]],
            value="Prospecto"
        )
        self.dd_grupo_cliente = ft.Dropdown(label="Grupo / Convenio", options=[], expand=True)

        # Filtros
        self.txt_buscar_cliente = ft.TextField(hint_text="Buscar...", prefix_icon=ft.Icons.SEARCH_ROUNDED, col={"xs": 12, "md": 8}, on_change=lambda e: self.cargar_clientes())
        self.dd_filtro_estatus = ft.Dropdown(
            label="Filtrar", options=[ft.dropdown.Option("Todos")] + [ft.dropdown.Option(x) for x in ["Prospecto", "En Proceso", "Aprobado"]],
            value="Todos", col={"xs": 12, "md": 4}, on_change=lambda e: self.cargar_clientes()
        )

        self.lista_clientes = ft.Column(spacing=12, scroll=ft.ScrollMode.AUTO)
        self.lista_clientes.on_scroll = self.on_scroll_lista

        # Layout
        self.content = ft.Container(
            padding=30,
            content=ft.Column([
                ft.Container(
                    content=ft.Row([
                        ft.Row([ft.Icon(ft.Icons.PEOPLE_ROUNDED, size=32, color="#3B82F6"), ft.Text("Agenda de Clientes", size=32, weight="w900", color=THEME["colors"]["text_primary"])], spacing=12),
                        ft.Row([
                            ft.IconButton(icon=ft.Icons.DELETE_SWEEP_ROUNDED, icon_color="#EF4444", on_click=self.eliminar_seleccionados),
                            ft.IconButton(icon=ft.Icons.REFRESH_ROUNDED, icon_color="#475569", on_click=lambda e: self.cargar_clientes())
                        ], spacing=12)
                    ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                    margin=ft.Margin(0, 0, 0, 30)
                ),
                ft.ResponsiveRow([
                    self._create_kpi_card("Total", self.kpi_total_clientes, ft.Icons.PEOPLE_ROUNDED, ["#3B82F6", "#DBEAFE"]),
                    self._create_kpi_card("Prospectos", self.kpi_prospectos, ft.Icons.PERSON_ADD_ROUNDED, ["#F59E0B", "#FEF3C7"]),
                    self._create_kpi_card("En Proceso", self.kpi_en_proceso, ft.Icons.AUTORENEW_ROUNDED, ["#8B5CF6", "#EDE9FE"]),
                    self._create_kpi_card("Aprobados", self.kpi_aprobados, ft.Icons.CHECK_CIRCLE_ROUNDED, ["#10B981", "#D1FAE5"]),
                ], spacing=16),
                ft.Divider(height=20, color="transparent"),
                ft.ResponsiveRow([
                    ft.Container(col={"xs": 12, "md": 4}, content=ft.Container(
                        content=ft.Column([
                            ft.Text("Agregar Cliente", size=18, weight="bold"),
                            ft.Divider(),
                            self.txt_datos_crudos, self.dd_estatus_cliente, self.dd_grupo_cliente,
                            ft.Button("Agregar Cliente", on_click=self.agregar_cliente_desde_datos, bgcolor="#3B82F6", color="white")
                        ], spacing=16), padding=20, bgcolor="white", border_radius=12, border=ft.Border.all(1, "#E2E8F0")
                    )),
                    ft.Container(col={"xs": 12, "md": 8}, content=ft.Column([
                        ft.ResponsiveRow([self.txt_buscar_cliente, self.dd_filtro_estatus], spacing=12),
                        ft.Container(height=16),
                        ft.Container(content=self.lista_clientes, expand=True)
                    ], spacing=0, expand=True), expand=True)
                ], spacing=20, expand=True)
            ], scroll=ft.ScrollMode.AUTO, expand=True),
            expand=True
        )

        self.actualizar_dropdown_grupos()

    def _create_kpi_card(self, title, value_control, icon, color_theme):
        main, bg = color_theme
        return ft.Container(
            content=ft.Column([
                ft.Container(content=ft.Icon(icon, size=24, color=main), bgcolor=bg, padding=10, border_radius=12),
                ft.Column([value_control, ft.Text(title, size=13, color="#64748B", weight="w500")], spacing=2)
            ], spacing=12),
            padding=18, border_radius=16, bgcolor="white", border=ft.Border.all(1, "#E2E8F0"),
            shadow=ft.BoxShadow(blur_radius=10, color="#08000000", offset=ft.Offset(0, 4)),
            col={"xs": 12, "sm": 6, "md": 4, "lg": 2}
        )

    def actualizar_dropdown_grupos(self):
        grupos = self.agenda_manager.obtener_grupos()
        self.dd_grupo_cliente.options = [ft.dropdown.Option(g) for g in grupos]
        if self.dd_grupo_cliente.page: self.dd_grupo_cliente.update()

    def agregar_cliente_desde_datos(self, e):
        if not self.txt_datos_crudos.value: return
        datos = self.agenda_manager.parsear_datos_crudos(self.txt_datos_crudos.value)
        datos.update({'estatus': self.dd_estatus_cliente.value, 'grupo': self.dd_grupo_cliente.value, 'asesor': self.perfil.get('nombre', 'Asesor')})
        if not datos.get('nombre'): return

        try:
            self.agenda_manager.agregar_cliente(datos)
            if self.sound_manager: self.sound_manager.play("cash")
            self.txt_datos_crudos.value = ""
            self.txt_datos_crudos.update()
            self.cargar_clientes()
        except Exception as ex:
            print(f"Error: {ex}")

    def cargar_clientes(self, cargar_mas=False):
        if not cargar_mas:
            self.paginacion_offset = 0
            self.lista_clientes.controls.clear()

        filtro = self.txt_buscar_cliente.value
        estatus = self.dd_filtro_estatus.value if self.dd_filtro_estatus.value != "Todos" else None

        clientes = self.agenda_manager.obtener_clientes(filtro=filtro, estatus=estatus, limite=self.paginacion_limite, offset=self.paginacion_offset)

        for c in clientes:
            self.lista_clientes.controls.append(self.crear_tarjeta_cliente(c))

        self.paginacion_offset += self.paginacion_limite
        self.lista_clientes.update()
        self.actualizar_kpis()

    def actualizar_kpis(self):
        stats = self.agenda_manager.obtener_stats()
        self.kpi_total_clientes.value = str(stats.get('total', 0))
        self.kpi_prospectos.value = str(stats.get('prospectos', 0))
        self.kpi_en_proceso.value = str(stats.get('en_proceso', 0))
        self.kpi_aprobados.value = str(stats.get('aprobados', 0))
        if self.kpi_total_clientes.page:
            self.kpi_total_clientes.update()
            self.kpi_prospectos.update()
            self.kpi_en_proceso.update()
            self.kpi_aprobados.update()

    def toggle_seleccion_cliente(self, e, cid):
        if e.control.value:
            self.clientes_seleccionados.add(cid)
        else:
            self.clientes_seleccionados.discard(cid)

    def crear_tarjeta_cliente(self, cliente):
        return ft.Container(
            content=ft.Column([
                ft.Row([
                    ft.Checkbox(value=cliente['id'] in self.clientes_seleccionados, on_change=lambda e: self.toggle_seleccion_cliente(e, cliente['id'])),
                    ft.Text(cliente.get('nombre', 'Sin nombre'), weight="bold"),
                    ft.Container(content=ft.Text(cliente.get('estatus', 'Prospecto'), size=10), bgcolor="#F3F4F6", padding=5, border_radius=5)
                ], alignment=ft.MainAxisAlignment.START),
                ft.Text(f"Tel: {cliente.get('telefono', '-')}", size=12, color="grey")
            ]),
            padding=10, bgcolor="white", border=ft.Border.all(1, "#E5E7EB"), border_radius=8
        )

    def eliminar_seleccionados(self, e):
        if not self.clientes_seleccionados: return

        def confirmar(e):
            try:
                for cid in self.clientes_seleccionados:
                    self.agenda_manager.eliminar_cliente(cid)
                if self.sound_manager: self.sound_manager.play("pop")
                self.clientes_seleccionados.clear()
                dlg.open = False
                self.cargar_clientes()
                self.page.update()
            except Exception as ex:
                print(f"Error eliminar: {ex}")

        dlg = ft.AlertDialog(
            title=ft.Text("Confirmar"),
            content=ft.Text(f"¿Eliminar {len(self.clientes_seleccionados)} clientes?"),
            actions=[
                ft.TextButton("Cancelar", on_click=lambda e: [setattr(dlg, "open", False), self.page.update()]),
                ft.TextButton("Eliminar", on_click=confirmar)
            ]
        )
        self.page.dialog = dlg
        dlg.open = True
        self.page.update()

    def on_scroll_lista(self, e):
        if e.pixels >= e.max_scroll_extent - 100:
            self.cargar_clientes(cargar_mas=True)
