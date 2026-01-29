import flet as ft
import asyncio
import logging
from core.config import GestorConfiguracion
from core.data import GestorOperaciones, GestorPerfil, GestorSesion
from core.system import SyncManager
from core.performance import performance_manager
from ui.theme import THEME, ThemeManager
from ui.responsive import ResponsiveManager
from ui.sidebar import JuicySidebar
from ui.state import LoadingState
from ui.audio import SoundManager
from ui.views.refinance import RefinanceView
from ui.views.new_credit import NewCreditView
from ui.views.insurance import InsuranceView
from ui.views.agenda import AgendaView
from ui.components.comparator import create_offer_comparator
from core.history import CalculationHistory

# Configuración de Logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

async def main(page: ft.Page):
    # 1. Configuración Inicial
    page.title = "ConsuCalcu Pro - Sistema Financiero"
    page.theme_mode = ft.ThemeMode.LIGHT
    page.padding = 0
    page.spacing = 0
    page.window.width = 1200
    page.window.height = 800
    page.window.min_width = 800
    page.window.min_height = 600

    # Fuentes
    page.fonts = {
        "Outfit": "fonts/Outfit-Regular.ttf", # Ensure this path exists or is correct relative to assets
        "Roboto Mono": "Roboto Mono"
    }
    
    # 2. Gestores Core
    config_manager = GestorConfiguracion()
    gestor_ops = GestorOperaciones()
    gestor_perfil = GestorPerfil()
    gestor_sesion = GestorSesion()
    
    # 3. Gestores UI/System
    responsive_manager = ResponsiveManager(page)
    sound_manager = SoundManager(page)
    history_manager = CalculationHistory()

    # Perfil
    perfil_data = gestor_perfil.cargar()
    if not perfil_data:
        perfil_data = {"nombre": "Usuario", "supervisor": "General"}

    # 4. Componentes UI Globales
    loading_overlay = ft.Container(
        content=ft.ProgressRing(),
        alignment=ft.Alignment(0,0),
        bgcolor="#80000000",
        visible=False
    )
    page.overlay.append(loading_overlay)
    
    loading_state = LoadingState(loading_overlay) # Simple wrapper
    
    offer_comparator = create_offer_comparator(page, THEME)

    # 5. Vistas
    refinance_view = RefinanceView(page, config_manager, gestor_ops, history_manager, sound_manager, offer_comparator)
    new_credit_view = NewCreditView(page, config_manager, gestor_ops, history_manager, sound_manager, offer_comparator)
    insurance_view = InsuranceView(page, config_manager, sound_manager, history_manager)
    agenda_view = AgendaView(page, perfil_data, sound_manager)
    
    views = [refinance_view, new_credit_view, insurance_view, agenda_view]
    
    # 6. Navegación
    content_area = ft.AnimatedSwitcher(
        content=refinance_view,
        transition=ft.AnimatedSwitcherTransition.FADE,
        duration=300,
        reverse_duration=200,
        switch_in_curve=ft.AnimationCurve.EASE_OUT,
        switch_out_curve=ft.AnimationCurve.EASE_IN,
        expand=True
    )
    
    async def on_nav_change(index):
        content_area.content = views[index]
        content_area.update()
        # Optional: Trigger specific load logic for views if needed
        if index == 3: # Agenda
            agenda_view.cargar_clientes()

    sidebar = JuicySidebar(page, on_nav_change, perfil_data, responsive_manager)
    
    # 7. Layout Principal
    page.add(
        ft.Row([
            sidebar,
            ft.Container(content=content_area, expand=True, bgcolor=THEME["colors"]["light"]["background"])
        ], expand=True, spacing=0)
    )
    
    # 8. Tareas Iniciales
    async def initial_setup():
        responsive_manager.update_dimensions()
        # Sync logic
        await SyncManager.sync_remote()
        config_manager.actualizar_memoria(SyncManager.load_local())
    
    page.on_resize = lambda e: [responsive_manager.update_dimensions(), sidebar.resize()]
    
    await initial_setup()
    page.update()

if __name__ == "__main__":
    ft.app(target=main, assets_dir="assets")
