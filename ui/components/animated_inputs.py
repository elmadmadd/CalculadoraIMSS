"""
ANIMATED_INPUTS.py
Sistema de Inputs Premium con Animaciones Fluidas
Fintech UX 2026 - Material Design 3 Enhanced

Características:
- Floating labels animados
- Bordes que crecen en focus
- Iconos de validación con fade-in
- Ripple effect en click
- Smooth color transitions
- Feedback háptico visual
"""

import flet as ft
from typing import Optional, Callable
import re

class AnimatedTextField(ft.Container):
    """
    TextField premium con animaciones fluidas y validación visual.
    
    Animaciones incluidas:
    - Label flotante (float up on focus)
    - Borde que crece (border expansion)
    - Icono de validación (fade in/out)
    - Color transitions (300ms ease-out)
    - Scale feedback en error
    """
    
    def __init__(
        self,
        label: str = "",
        hint_text: str = "",
        prefix_icon: Optional[str] = None,
        field_type: str = "text",  # text, email, currency, number
        min_val: Optional[float] = None,
        max_val: Optional[float] = None,
        required: bool = False,
        on_change: Optional[Callable] = None,
        width: Optional[int] = None,
        **kwargs
    ):
        self.label_text = label
        self.hint_text = hint_text
        self.prefix_icon = prefix_icon
        self.field_type = field_type
        self.min_val = min_val
        self.max_val = max_val
        self.required = required
        self.on_change_callback = on_change
        self._is_focused = False
        self._is_valid = None  # None = no validado, True = válido, False = inválido
        self._value = ""
        
        # Colores del sistema
        self.colors = {
            "border_default": "#E5E7EB",  # Gray 200
            "border_focus": "#10B981",    # Emerald 500
            "border_error": "#EF4444",    # Red 500
            "label_default": "#6B7280",   # Gray 500
            "label_focus": "#10B981",     # Emerald 500
            "label_error": "#EF4444",     # Red 500
            "bg_default": "#FFFFFF",      # White
            "bg_focus": "#ECFDF5",        # Emerald 50
            "text": "#111827",            # Gray 900
        }
        
        # Crear label flotante
        self.floating_label = ft.Text(
            value=label,
            size=16,
            color=self.colors["label_default"],
            weight=ft.FontWeight.W_400,
            animate_size=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_position=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_opacity=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
        )
        
        # Crear input field
        self.text_field = ft.TextField(
            value="",
            hint_text=hint_text,
            border=ft.InputBorder.NONE,
            text_size=16,
            color=self.colors["text"],
            cursor_color=self.colors["border_focus"],
            on_focus=self._on_focus,
            on_blur=self._on_blur,
            on_change=self._on_change,
            content_padding=ft.Padding.only(left=12 if not prefix_icon else 44, right=44, top=24, bottom=8),
        )

        if field_type == "currency":
            self.text_field.keyboard_type = ft.KeyboardType.NUMBER
        
        # Multiline
        self.multiline = kwargs.get("multiline", False)
        self.text_field.multiline = self.multiline
        if self.multiline:
            self.text_field.min_lines = 3
    
    @property
    def value(self):
        """Para compatibilidad con EnterpriseInput"""
        return self.text_field.value
        
    @value.setter
    def value(self, val):
        self.text_field.value = str(val if val is not None else "")
        if val: # Si tiene valor inicial, levantar label
             self.floating_label.size = 12
             self.floating_label.color = self.colors["label_focus"]
             # En un Stack, la posición se anima, necesitamos flag o llamar update
        
    @property
    def raw_value(self):
        """Retorna valor numérico para currency/number"""
        return self.get_value()
        
        # Icono de prefijo (opcional)
        self.prefix_icon_widget = None
        if prefix_icon:
            self.prefix_icon_widget = ft.Icon(
                name=prefix_icon,
                size=20,
                color=self.colors["label_default"],
                animate_opacity=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            )
        
        # Icono de validación (sufijo)
        self.validation_icon = ft.Icon(
            name=ft.Icons.CHECK_CIRCLE_ROUNDED,
            size=20,
            color=self.colors["border_focus"],
            opacity=0,
            animate_opacity=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
        )
        
        # Mensaje de error
        self.error_text = ft.Text(
            value="",
            size=12,
            color=self.colors["label_error"],
            opacity=0,
            animate_opacity=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
        )
        
        # Contenedor del input con animaciones
        input_container = ft.Stack(
            controls=[
                # Campo de texto
                self.text_field,
                
                # Label flotante (posicionado absolutamente)
                ft.Container(
                    content=self.floating_label,
                    left=12 if not prefix_icon else 44,
                    top=16,
                    animate_position=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
                ),
                
                # Icono de prefijo
                ft.Container(
                    content=self.prefix_icon_widget,
                    left=12,
                    top=16,
                ) if prefix_icon else ft.Container(),
                
                # Icono de validación
                ft.Container(
                    content=self.validation_icon,
                    right=12,
                    top=16,
                ),
            ],
            height=56,
        )
        
        # Contenedor principal con borde animado
        super().__init__(
            content=ft.Column([
                input_container,
                ft.Container(height=4),  # Espaciado
                self.error_text,
            ], spacing=0, tight=True),
            bgcolor=self.colors["bg_default"],
            border=ft.Border.all(1, self.colors["border_default"]),
            border_radius=12,
            width=width,
            animate=ft.Animation(300, ft.AnimationCurve.EASE_OUT),
            animate_scale=ft.Animation(200, ft.AnimationCurve.EASE_OUT),
            **kwargs
        )
    
    def _on_focus(self, e):
        """Animaciones al recibir focus"""
        self._is_focused = True
        
        # Animar label hacia arriba
        self.floating_label.size = 12
        self.floating_label.color = self.colors["label_focus"]
        
        # Cambiar color de borde
        self.border = ft.Border.all(2, self.colors["border_focus"])
        self.bgcolor = self.colors["bg_focus"]
        
        # Animar icono de prefijo
        if self.prefix_icon_widget:
            self.prefix_icon_widget.color = self.colors["border_focus"]
        
        self.update()
    
    def _on_blur(self, e):
        """Animaciones al perder focus"""
        self._is_focused = False
        
        # Si está vacío, bajar label
        if not self.text_field.value:
            self.floating_label.size = 16
            self.floating_label.color = self.colors["label_default"]
        
        # Validar campo
        self._validate()
        
        # Restaurar colores si es válido
        if self._is_valid or self._is_valid is None:
            self.border = ft.Border.all(1, self.colors["border_default"])
            self.bgcolor = self.colors["bg_default"]
            
            if self.prefix_icon_widget:
                self.prefix_icon_widget.color = self.colors["label_default"]
        
        self.update()
    
    def _on_change(self, e):
        """Maneja cambios en el texto"""
        self._value = e.control.value
        
        # Validar en tiempo real si ya se validó antes
        if self._is_valid is not None:
            self._validate()
        
        # Callback externo
        if self.on_change_callback:
            self.on_change_callback(e)
    
    def _validate(self):
        """Valida el campo y muestra feedback visual"""
        value = self.text_field.value
        
        # Campo vacío
        if not value:
            if self.required:
                self._set_error("Este campo es obligatorio")
                return False
            else:
                self._clear_validation()
                return True
        
        # Validación por tipo
        is_valid, error_msg = self._validate_by_type(value)
        
        if is_valid:
            self._set_success()
        else:
            self._set_error(error_msg)
        
        return is_valid
    
    def _validate_by_type(self, value: str):
        """Valida según el tipo de campo"""
        if self.field_type == "email":
            if not re.match(r'^[\w\.-]+@[\w\.-]+\.\w+$', value):
                return False, "Email inválido"
        
        elif self.field_type in ["currency", "number"]:
            try:
                num = float(value.replace(",", "").replace("$", ""))
                if self.min_val is not None and num < self.min_val:
                    return False, f"Mínimo: ${self.min_val:,.2f}" if self.field_type == "currency" else f"Mínimo: {self.min_val}"
                if self.max_val is not None and num > self.max_val:
                    return False, f"Máximo: ${self.max_val:,.2f}" if self.field_type == "currency" else f"Máximo: {self.max_val}"
            except:
                return False, "Valor numérico inválido"
        
        return True, ""
    
    def _set_success(self):
        """Muestra estado de éxito"""
        self._is_valid = True
        
        # Borde verde
        self.border = ft.Border.all(2, self.colors["border_focus"])
        
        # Label verde
        self.floating_label.color = self.colors["label_focus"]
        
        # Mostrar icono de check
        self.validation_icon.name = ft.Icons.CHECK_CIRCLE_ROUNDED
        self.validation_icon.color = self.colors["border_focus"]
        self.validation_icon.opacity = 1
        
        # Ocultar error
        self.error_text.opacity = 0
        
        # Escala normal
        self.scale = 1.0
        
        self.update()
    
    def _set_error(self, message: str):
        """Muestra estado de error"""
        self._is_valid = False
        
        # Borde rojo
        self.border = ft.Border.all(2, self.colors["border_error"])
        
        # Label rojo
        self.floating_label.color = self.colors["label_error"]
        
        # Mostrar icono de error
        self.validation_icon.name = ft.Icons.ERROR_ROUNDED
        self.validation_icon.color = self.colors["border_error"]
        self.validation_icon.opacity = 1
        
        # Mostrar mensaje de error
        self.error_text.value = message
        self.error_text.opacity = 1
        
        # Shake effect (escala ligeramente)
        self.scale = 0.98
        
        self.update()
        
        # Restaurar escala
        import asyncio
        async def restore_scale():
            await asyncio.sleep(0.1)
            self.scale = 1.0
            self.update()
        
        asyncio.create_task(restore_scale())
    
    def _clear_validation(self):
        """Limpia el estado de validación"""
        self._is_valid = None
        
        # Ocultar icono
        self.validation_icon.opacity = 0
        
        # Ocultar error
        self.error_text.opacity = 0
        
        self.update()
    
    def get_value(self):
        """Retorna el valor del campo"""
        if self.field_type in ["currency", "number"]:
            try:
                return float(self.text_field.value.replace(",", "").replace("$", ""))
            except:
                return 0.0
        return self.text_field.value
    
    def is_valid(self) -> bool:
        """Retorna si el campo es válido"""
        return self._validate()


# ==========================================
# EJEMPLO DE USO
# ==========================================

def ejemplo_animated_inputs(page: ft.Page):
    """Ejemplo de uso de AnimatedTextField"""
    
    # Input de email
    email_input = AnimatedTextField(
        label="Correo Electrónico",
        hint_text="ejemplo@correo.com",
        prefix_icon=ft.Icons.EMAIL_ROUNDED,
        field_type="email",
        required=True,
        width=400,
    )
    
    # Input de monto
    monto_input = AnimatedTextField(
        label="Monto del Crédito",
        hint_text="$0.00",
        prefix_icon=ft.Icons.ATTACH_MONEY_ROUNDED,
        field_type="currency",
        min_val=10000,
        max_val=500000,
        required=True,
        width=400,
    )
    
    # Input de texto simple
    nombre_input = AnimatedTextField(
        label="Nombre Completo",
        hint_text="Ingresa tu nombre",
        prefix_icon=ft.Icons.PERSON_ROUNDED,
        required=True,
        width=400,
    )
    
    # Botón de validación
    def validar(e):
        if email_input.is_valid() and monto_input.is_valid() and nombre_input.is_valid():
            page.snack_bar = ft.SnackBar(
                content=ft.Text("✅ Todos los campos son válidos"),
                bgcolor="#10B981"
            )
            page.snack_bar.open = True
            page.update()
    
    btn_validar = ft.Button(
        "Validar Formulario",
        on_click=validar,
        bgcolor="#10B981",
        color="white",
    )
    
    return ft.Column([
        ft.Text("Inputs Animados Premium", size=24, weight="bold"),
        ft.Container(height=20),
        email_input,
        ft.Container(height=20),
        monto_input,
        ft.Container(height=20),
        nombre_input,
        ft.Container(height=30),
        btn_validar,
    ], spacing=0, scroll=ft.ScrollMode.AUTO)
