# DIÁLOGO DE CONFIGURACIÓN DE RENDIMIENTO
# Este código debe agregarse en la función main()

def show_performance_dialog(e):
    """Muestra el diálogo de configuración de rendimiento"""
    
    # Estado actual
    current_mode = performance_manager.get_status_text()
    is_auto = performance_manager.user_override is None
    is_reduced = performance_manager.should_reduce_animations()
    
    # Info del hardware (si está disponible)
    hw_info = ""
    if HAVE_PSUTIL:
        try:
            cpu_count = psutil.cpu_count(logical=False) or "N/A"
            ram_gb = psutil.virtual_memory().total / (1024**3)
            cpu_freq = psutil.cpu_freq()
            freq_text = f"{cpu_freq.current/1000:.2f} GHz" if cpu_freq else "N/A"
            
            hw_info = f"""
📊 Hardware Detectado:
• CPU: {cpu_count} núcleos @ {freq_text}
• RAM: {ram_gb:.1f} GB
"""
        except:
            hw_info = "No se pudo detectar el hardware"
    else:
        hw_info = "psutil no disponible - Instala con: pip install psutil"
    
    # Radio buttons para selección de modo
    mode_radio = ft.RadioGroup(
        value="auto" if is_auto else ("reduced" if is_reduced else "full"),
        content=ft.Column([
            ft.Radio(
                value="auto",
                label="Automático (Recomendado)",
                label_style=ft.TextStyle(size=14)
            ),
            ft.Radio(
                value="reduced",
                label="Reducido (Mejor rendimiento)",
                label_style=ft.TextStyle(size=14)
            ),
            ft.Radio(
                value="full",
                label="Completo (Todas las animaciones)",
                label_style=ft.TextStyle(size=14)
            )
        ], spacing=10)
    )
    
    def save_preference(e):
        selected = mode_radio.value
        if selected == "auto":
            performance_manager.reset_to_auto()
        elif selected == "reduced":
            performance_manager.set_user_preference(True)
        else:  # full
            performance_manager.set_user_preference(False)
        
        page.close(dlg)
        msg(f"Modo de rendimiento: {performance_manager.get_status_text()}", THEME["colors"]["success"])
    
    dlg = ft.AlertDialog(
        modal=True,
        title=ft.Row([
            ft.Icon(ft.Icons.SPEED_ROUNDED, color=THEME["colors"]["accent"], size=28),
            ft.Text("Configuración de Rendimiento", weight="bold")
        ], spacing=10),
        content=ft.Container(
            content=ft.Column([
                ft.Text(
                    "Ajusta el nivel de animaciones según tu equipo:",
                    size=13,
                    color=THEME["colors"]["text_secondary"]
                ),
                ft.Divider(height=20),
                ft.Container(
                    content=ft.Text(hw_info, size=12, color=THEME["colors"]["text_tertiary"]),
                    bgcolor=THEME["colors"]["card_bg"],
                    padding=12,
                    border_radius=8
                ),
                ft.Divider(height=20),
                ft.Text("Modo de Animaciones:", weight="bold", size=14),
                mode_radio,
                ft.Divider(height=10),
                ft.Container(
                    content=ft.Column([
                        ft.Text("ℹ️ Información:", weight="bold", size=12),
                        ft.Text(
                            "• Automático: Detecta tu hardware y ajusta las animaciones\n"
                            "• Reducido: Animaciones más rápidas y menos delays\n"
                            "• Completo: Todas las animaciones y efectos visuales",
                            size=11,
                            color=THEME["colors"]["text_tertiary"]
                        )
                    ], spacing=5),
                    bgcolor="#F0F9FF" if page.theme_mode == ft.ThemeMode.LIGHT else "#1E293B",
                    padding=10,
                    border_radius=8
                )
            ], spacing=15, tight=True),
            width=500
        ),
        actions=[
            ft.TextButton("Cancelar", on_click=lambda e: page.close(dlg)),
            EnterpriseButton(
                "GUARDAR",
                on_click=save_preference,
                icon=ft.Icons.CHECK_ROUNDED,
                bgcolor=THEME["colors"]["success"]
            )
        ]
    )
    
    page.open(dlg)
