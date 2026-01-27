    # --- HELPER: Skeleton Loading Card ---
    def create_skeleton_card():
        """Crea una tarjeta skeleton animada para carga (Efecto Shimmer Simulado)"""
        shim_col = "#E2E8F0" # Slate 200 - Color base del skeleton
        bg_col = "#FFFFFF"   # Fondo blanco para la card
        
        # Animación de opacidad simple para simular 'respiración'
        return ft.Container(
            padding=20,
            bgcolor=bg_col,
            border_radius=16,
            border=ft.Border.all(1, "#E5E7EB"),
            content=ft.Column([
                # Header sim (Icon + Title)
                ft.Row([
                    ft.Container(width=45, height=45, border_radius=12, bgcolor=shim_col),
                    ft.Column([
                        ft.Container(width=140, height=18, border_radius=4, bgcolor=shim_col),
                        ft.Container(width=90, height=12, border_radius=4, bgcolor=shim_col),
                    ], spacing=6)
                ], spacing=15),
                
                ft.Container(height=20),
                
                # Stats Row 1
                ft.Row([
                    ft.Container(width=100, height=14, border_radius=4, bgcolor=shim_col),
                    ft.Container(width=120, height=24, border_radius=6, bgcolor=shim_col),
                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                
                ft.Container(height=12),
                
                # Stats Row 2
                ft.Row([
                    ft.Container(width=90, height=14, border_radius=4, bgcolor=shim_col),
                    ft.Container(width=100, height=20, border_radius=6, bgcolor=shim_col),
                ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
                
                ft.Divider(height=25, color="#F1F5F9"),
                
                # Footer Actions
                ft.Row([
                    ft.Container(expand=True, height=36, border_radius=8, bgcolor=shim_col),
                    ft.Container(width=10),
                    ft.Container(expand=True, height=36, border_radius=8, bgcolor=shim_col),
                ])
            ]),
            animate_opacity=ft.Animation(800, "easeInOut"),
            opacity=0.6, # Opacidad base
        )
