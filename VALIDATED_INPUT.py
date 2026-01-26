"""
VALIDATED INPUT COMPONENT
=========================
Input con validación en tiempo real y feedback visual.
Compatible con Flet sin UserControl.
"""

import flet as ft
import re

def create_validated_input(
    label: str,
    validation_type: str = "text",
    required: bool = True,
    min_value: float = None,
    max_value: float = None,
    min_length: int = None,
    max_length: int = None,
    custom_validator = None,
    on_validation_change = None,
    width: int = None,
    value: str = "",
    **kwargs
):
    """
    Crear input con validación en tiempo real.
    
    Args:
        label: Etiqueta del campo
        validation_type: Tipo de validación (text, number, money, email, nss, rfc, curp)
        required: Si el campo es requerido
        min_value: Valor mínimo (para números)
        max_value: Valor máximo (para números)
        min_length: Longitud mínima
        max_length: Longitud máxima
        custom_validator: Función de validación personalizada
        on_validation_change: Callback cuando cambia la validación
        width: Ancho del input
        value: Valor inicial
    
    Returns:
        dict con 'control' (el input) y 'validate' (función de validación)
    """
    
    is_valid = not required
    error_message = ""
    
    # Crear el TextField
    text_field = ft.TextField(
        label=label,
        value=value,
        border_radius=12,
        width=width,
        **kwargs
    )
    
    # Ícono de validación
    validation_icon = ft.Icon(
        name=ft.Icons.CHECK_CIRCLE,
        color="green",
        size=20,
        visible=False
    )
    
    def validate():
        """Ejecutar validación"""
        nonlocal is_valid, error_message
        
        val = text_field.value or ""
        
        # 1. Validar campo requerido
        if required and not val.strip():
            is_valid = False
            error_message = "Este campo es requerido"
            _update_ui()
            return False
        
        # Si está vacío y no es requerido, es válido
        if not val.strip() and not required:
            is_valid = True
            error_message = ""
            _update_ui()
            return True
        
        # 2. Validar por tipo
        if validation_type == "number" or validation_type == "money":
            if not _validate_number(val):
                return False
        elif validation_type == "email":
            if not _validate_email(val):
                return False
        elif validation_type == "nss":
            if not _validate_nss(val):
                return False
        elif validation_type == "rfc":
            if not _validate_rfc(val):
                return False
        elif validation_type == "curp":
            if not _validate_curp(val):
                return False
        
        # 3. Validar longitud
        if min_length and len(val) < min_length:
            is_valid = False
            error_message = f"Mínimo {min_length} caracteres"
            _update_ui()
            return False
        
        if max_length and len(val) > max_length:
            is_valid = False
            error_message = f"Máximo {max_length} caracteres"
            _update_ui()
            return False
        
        # 4. Validador personalizado
        if custom_validator:
            valid, msg = custom_validator(val)
            if not valid:
                is_valid = False
                error_message = msg
                _update_ui()
                return False
        
        # Si llegamos aquí, es válido
        is_valid = True
        error_message = ""
        _update_ui()
        return True
    
    def _validate_number(value):
        nonlocal is_valid, error_message
        try:
            num = float(value.replace(",", "").replace("$", ""))
            
            if min_value is not None and num < min_value:
                is_valid = False
                error_message = f"Mínimo: {min_value}"
                _update_ui()
                return False
            
            if max_value is not None and num > max_value:
                is_valid = False
                error_message = f"Máximo: {max_value}"
                _update_ui()
                return False
            
            return True
        except ValueError:
            is_valid = False
            error_message = "Debe ser un número válido"
            _update_ui()
            return False
    
    def _validate_email(value):
        nonlocal is_valid, error_message
        pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(pattern, value):
            is_valid = False
            error_message = "Email inválido"
            _update_ui()
            return False
        return True
    
    def _validate_nss(value):
        nonlocal is_valid, error_message
        if not re.match(r'^\d{11}$', value):
            is_valid = False
            error_message = "NSS debe tener 11 dígitos"
            _update_ui()
            return False
        return True
    
    def _validate_rfc(value):
        nonlocal is_valid, error_message
        if not re.match(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$', value.upper()):
            is_valid = False
            error_message = "RFC inválido"
            _update_ui()
            return False
        return True
    
    def _validate_curp(value):
        nonlocal is_valid, error_message
        if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', value.upper()):
            is_valid = False
            error_message = "CURP inválido"
            _update_ui()
            return False
        return True
    
    def _update_ui():
        """Actualizar UI según validación"""
        if is_valid:
            validation_icon.name = ft.Icons.CHECK_CIRCLE
            validation_icon.color = "green"
            validation_icon.visible = True
            text_field.border_color = "green"
            text_field.error_text = None
        else:
            validation_icon.name = ft.Icons.ERROR
            validation_icon.color = "red"
            validation_icon.visible = True
            text_field.border_color = "red"
            text_field.error_text = error_message
        
        if on_validation_change:
            on_validation_change(is_valid)
        
        # Intentar actualizar si está en la página
        try:
            if hasattr(text_field, 'update'):
                text_field.update()
            if hasattr(validation_icon, 'update'):
                validation_icon.update()
        except:
            pass
    
    # Conectar eventos
    def on_change(e):
        validate()
    
    def on_blur(e):
        validate()
    
    text_field.on_change = on_change
    text_field.on_blur = on_blur
    
    # Crear container con input e ícono
    container = ft.Row([
        ft.Container(
            content=text_field,
            expand=True
        ),
        ft.Container(
            content=validation_icon,
            width=30,
            alignment=ft.Alignment(0, 0)
        )
    ], spacing=5)
    
    # Retornar objeto con control y métodos
    return {
        'control': container,
        'text_field': text_field,
        'validate': validate,
        'is_valid': lambda: is_valid,
        'value': lambda: text_field.value,
        'set_value': lambda v: setattr(text_field, 'value', v)
    }
