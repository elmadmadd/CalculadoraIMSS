# ==========================================
# COMPONENTES UI MEJORADOS (UX)
# ==========================================
# Este archivo contiene componentes mejorados listos para integrar en main.py

import flet as ft
import re
from typing import Callable, Optional

class SmartTextField(ft.TextField):
    """
    TextField mejorado con:
    - Formateo automático de moneda en vivo
    - Validación en tiempo real
    - Feedback visual (bordes verdes/rojos)
    - Mensajes de error contextuales
    
    Uso:
        # Campo de moneda con validación de mínimo
        monto_field = SmartTextField(
            label="Monto",
            hint_text="$0.00",
            field_type="currency",
            min_val=10000,
            auto_format=True
        )
        
        # Campo de RFC con validación automática
        rfc_field = SmartTextField(
            label="RFC",
            hint_text="XAXX010101000",
            field_type="rfc"
        )
    """
    def __init__(
        self,
        label: str = "",
        hint_text: str = "",
        field_type: str = "text",
        min_val: float = None,
        max_val: float = None,
        auto_format: bool = True,
        on_change_callback: Optional[Callable] = None,
        **kwargs
    ):
        """
        Args:
            label: Etiqueta del campo
            hint_text: Texto de ayuda
            field_type: Tipo ("text", "currency", "number", "rfc", "curp", "nss")
            min_val: Valor mínimo (para números/moneda)
            max_val: Valor máximo (para números/moneda)
            auto_format: Si True, formatea automáticamente
            on_change_callback: Callback adicional al cambiar
        """
        self.field_type = field_type
        self.min_val = min_val
        self.max_val = max_val
        self.auto_format = auto_format
        self.on_change_callback = on_change_callback
        self._is_valid = True
        
        # Configurar teclado y filtros según tipo
        keyboard_type = "text"
        input_filter = None
        
        if field_type in ["currency", "number"]:
            keyboard_type = "number"
            # Permitir solo números y un punto decimal
            input_filter = ft.InputFilter(regex_string=r"[0-9.]", replacement_string="")
        
        super().__init__(
            label=label,
            hint_text=hint_text,
            keyboard_type=keyboard_type,
            input_filter=input_filter,
            on_change=self._handle_change,
            on_blur=self._handle_blur,
            on_focus=self._handle_focus, # Nuevo handler de foco
            border_radius=8,
            **kwargs
        )
    
    def _handle_focus(self, e):
        """Maneja cuando el campo recibe el foco."""
        # Si el valor es 0 o vacío, limpiar para facilitar escritura
        try:
            val_clean = self.value.replace("$", "").replace(",", "").strip()
            if val_clean == "0" or val_clean == "0.00" or val_clean == "0.0":
                self.value = ""
                self.update()
        except:
            pass
            
    def _handle_change(self, e):
        """Maneja cambios en tiempo real."""
        val = e.control.value
        
        # 0. Si está vacío, restaurar a 0 (si es numérico)
        if (val is None or len(val) == 0) and self.field_type in ["currency", "number"]:
            e.control.value = "0"
            e.control.update()
            self.value = "0" # Sincronizar self.value
            if self.on_change_callback:
                self.on_change_callback(e)
            return
        
        # Si no es numérico y está vacío, permitirlo y salir
        if not val:
            if self.on_change_callback:
                self.on_change_callback(e)
            return

        # 1. Evitar ceros a la izquierda (ej: 0500 -> 500)
        if self.field_type in ["currency", "number"]:
            if val.startswith("0") and len(val) > 1 and not val.startswith("0."):
                val = val.lstrip("0")
                e.control.value = val 
                e.control.update()
                self.value = val

        # 2. Formateo automático moneda
        if self.auto_format and self.field_type == "currency":
            # Solo si no está terminando en punto (escribiendo decimales)
            if not val.endswith('.') and len(val) > 1:
                try:
                    formatted = self._format_currency_live(val)
                    if formatted != val:
                        e.control.value = formatted
                        e.control.update()
                        self.value = formatted
                except:
                    pass
        
        # Sincronizar self.value final
        self.value = e.control.value
        
        # Validar en tiempo real
        self._validate()
        
        # Llamar callback adicional
        if self.on_change_callback:
            self.on_change_callback(e)
    
    def _handle_blur(self, e):
        """Maneja cuando el campo pierde el foco."""
        if not self.value:
            return

        # Formateo final
        if self.auto_format and self.field_type == "currency":
            self.value = self._format_currency_live(self.value)
            self.update()
        
        # Validación final
        self._validate()
    
    def _format_currency_live(self, value: str) -> str:
        """Formatea como moneda."""
        try:
            cleaned = re.sub(r'[^\d.]', '', value)
            if not cleaned or cleaned == '.':
                return ""
            parts = cleaned.split('.')
            if len(parts) > 2:
                cleaned = parts[0] + '.' + ''.join(parts[1:])
            num = float(cleaned)
            return f"{num:,.2f}"
        except:
            return value
    
    def _clean_float(self, v):
        """Limpia y convierte a float."""
        try:
            return float(str(v).replace("$","").replace(",","").strip())
        except:
            return 0.0
    
    def _validate(self):
        """Valida el campo y actualiza el feedback visual."""
        if not self.value or self.value.strip() == "":
            self._is_valid = True
            self.border_color = None
            self.error_text = None
            self.update()
            return
        
        is_valid, error_msg = self._validate_field(
            self.value,
            self.field_type,
            self.min_val,
            self.max_val
        )
        
        self._is_valid = is_valid
        
        # CYBERPUNK FINANCIAL 2026: Bordes Lime para validación
        if is_valid:
            self.border_color = "#84CC16"  # Lime - Terminal hacker
            self.error_text = None
        else:
            self.border_color = "#EF4444"  # Rojo
            self.error_text = error_msg
        
        self.update()
    
    def _validate_field(self, value: str, field_type: str, min_val: float, max_val: float):
        """Valida un campo."""
        if not value or value.strip() == "":
            return False, "Campo requerido"
        
        if field_type in ["number", "currency"]:
            try:
                num = self._clean_float(value)
                if min_val is not None and num < min_val:
                    return False, f"Mínimo: ${min_val:,.2f}" if field_type == "currency" else f"Mínimo: {min_val}"
                if max_val is not None and num > max_val:
                    return False, f"Máximo: ${max_val:,.2f}" if field_type == "currency" else f"Máximo: {max_val}"
                return True, ""
            except:
                return False, "Valor inválido"
        
        elif field_type == "rfc":
            if not re.match(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$', value.upper()):
                return False, "RFC inválido (ej: XAXX010101000)"
            return True, ""
        
        elif field_type == "curp":
            if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', value.upper()):
                return False, "CURP inválido (18 caracteres)"
            return True, ""
        
        elif field_type == "nss":
            cleaned = re.sub(r'\D', '', value)
            if len(cleaned) != 11:
                return False, "NSS debe tener 11 dígitos"
            return True, ""
        
        return True, ""
    
    def is_valid(self) -> bool:
        """Retorna si el campo es válido."""
        return self._is_valid
    
    def get_value(self):
        """Retorna el valor limpio (sin formato)."""
        if self.field_type in ["currency", "number"]:
            return self._clean_float(self.value)
        return self.value


# ==========================================
# EJEMPLO DE USO
# ==========================================

def ejemplo_uso():
    """
    Ejemplo de cómo usar SmartTextField en tu aplicación.
    """
    
    # Campo de monto con validación de mínimo $10,000
    monto_field = SmartTextField(
        label="Monto del Crédito",
        hint_text="$0.00",
        field_type="currency",
        min_val=10000,
        max_val=1000000,
        auto_format=True,
        width=200
    )
    
    # Campo de capacidad de pago
    capacidad_field = SmartTextField(
        label="Capacidad de Pago",
        hint_text="$0.00",
        field_type="currency",
        min_val=500,
        auto_format=True,
        width=200
    )
    
    # Campo de RFC con validación automática
    rfc_field = SmartTextField(
        label="RFC",
        hint_text="XAXX010101000",
        field_type="rfc",
        width=200
    )
    
    # Campo de CURP
    curp_field = SmartTextField(
        label="CURP",
        hint_text="18 caracteres",
        field_type="curp",
        width=250
    )
    
    # Campo de NSS
    nss_field = SmartTextField(
        label="NSS",
        hint_text="11 dígitos",
        field_type="nss",
        width=200
    )
    
    # Botón que valida todos los campos
    def validar_formulario(e):
        campos = [monto_field, capacidad_field, rfc_field, curp_field, nss_field]
        
        if all(campo.is_valid() for campo in campos):
            print("✅ Todos los campos son válidos")
            print(f"Monto: ${monto_field.get_value():,.2f}")
            print(f"Capacidad: ${capacidad_field.get_value():,.2f}")
            print(f"RFC: {rfc_field.get_value()}")
        else:
            print("❌ Hay campos inválidos")
    
    btn_validar = ft.Button(
        "Validar Formulario",
        on_click=validar_formulario
    )
    
    return ft.Column([
        ft.Text("Formulario con Validación en Tiempo Real", size=20, weight="bold"),
        monto_field,
        capacidad_field,
        rfc_field,
        curp_field,
        nss_field,
        btn_validar
    ], spacing=15)


# ==========================================
# INTEGRACIÓN EN MAIN.PY
# ==========================================

"""
PASOS PARA INTEGRAR:

1. Copiar la clase SmartTextField al archivo main.py (después de las utilidades de UX)

2. Reemplazar TextFields existentes:

   # ANTES:
   monto_field = ft.TextField(
       label="Monto",
       hint_text="$0.00",
       keyboard_type=ft.KeyboardType.NUMBER
   )
   
   # DESPUÉS:
   monto_field = SmartTextField(
       label="Monto",
       hint_text="$0.00",
       field_type="currency",
       min_val=10000,
       auto_format=True
   )

3. Usar get_value() para obtener valores limpios:

   # ANTES:
   monto = clean_float(monto_field.value)
   
   # DESPUÉS:
   monto = monto_field.get_value()  # Ya retorna float limpio

4. Validar antes de procesar:

   if monto_field.is_valid():
       # Procesar...
   else:
       mostrar_mensaje(page, "Corrige los errores", "red")
"""
