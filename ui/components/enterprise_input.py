import flet as ft
import re
from ui.theme import THEME
from ui.utils import format_currency_live

class EnterpriseInput(ft.Column):
    """
    JuicyInput MEJORADO: Componente de entrada estilizado con UX visual premium.

    MEJORAS INTEGRADAS:
    - Validaciones en tiempo real (NSS/RFC/CURP)
    - Formateo automático de moneda
    - Feedback visual inmediato (bordes verdes/rojos)
    - Animaciones de foco suaves
    """
    def __init__(self, label, prefix_icon=None, prefix_text=None, suffix_text=None, prefix=None, suffix=None, expand=False, width=None, value=None,
                 password=False, is_money=False, multiline=False, max_length=None,
                 tipo_validacion=None, keyboard_type=None, on_submit=None, on_change=None):
        super().__init__(expand=expand, width=width, spacing=2)
        self.tipo_validacion = tipo_validacion
        self.max_length = max_length
        self.is_valid = True
        self.error_message = ""
        self._ext_ch = on_change
        self.is_money = is_money

        # Mensaje de Error
        self.error_text = ft.Text("", size=11, color=THEME["colors"]["error"], weight="bold", visible=False, text_align=ft.TextAlign.RIGHT)

        # Lógica de Teclado
        final_keyboard = keyboard_type if keyboard_type else ("number" if is_money or tipo_validacion == "NSS" else "text")

        # Resolve prefix/suffix
        final_prefix = prefix if prefix else (ft.Text(prefix_text) if prefix_text else None)
        final_suffix = suffix if suffix else (ft.Text(suffix_text) if suffix_text else None)

        # TextField Interno (Minimalista, el estilo lo da el contenedor)
        self.tf = ft.TextField(
            value=str(value) if value is not None else "",
            label=label,
            prefix_icon=prefix_icon,
            prefix=final_prefix,
            suffix=final_suffix,
            password=password,
            can_reveal_password=password,
            text_size=15,
            border="none",          # Sin borde propio
            bgcolor="transparent",  # Fondo transparente
            color="#111827",        # Gray 900 (Strict Light)
            keyboard_type=final_keyboard,
            text_align=ft.TextAlign.RIGHT if is_money else "left",
            multiline=multiline,
            min_lines=2 if multiline else 1,
            content_padding=ft.Padding.only(top=10, bottom=10, right=10, left=0 if prefix_icon else 10),
            height=None if multiline else 45,
            label_style=ft.TextStyle(size=14, color="#6B7280"), # Gray 500
            on_change=self._on_change_wrapper,
            on_focus=self._on_focus_wrapper,
            on_blur=self._on_blur_wrapper,
            on_submit=on_submit,
            selection_color="#10B981", # Emerald 500
            cursor_color="#10B981"     # Emerald 500
        )

        # Contenedor Visual (La "Caja" Premium)
        self.input_box = ft.Container(
            content=self.tf,
            border_radius=12,
            border=ft.Border.all(1, "#E5E7EB"), # Gray 200 (Default)
            bgcolor="#FFFFFF", # White
            padding=ft.Padding.symmetric(horizontal=10),
            # ANIMACIONES PREMIUM
            animate=THEME["animations"]["fast"],
            animate_scale=ft.Animation(150, ft.AnimationCurve.EASE_OUT),
            shadow=None,
            on_click=lambda e: self.tf.focus()
        )

        self.controls = [self.input_box, self.error_text]

    @property
    def value(self):
        return self.tf.value

    @value.setter
    def value(self, val):
        self.tf.value = val

    @property
    def raw_value(self):
        """Devuelve el valor numérico limpio si es moneda, o el string directo"""
        val = self.tf.value
        if self.is_money:
            try:
                return float(str(val).replace("$", "").replace(",", ""))
            except:
                return 0.0
        return val

    @staticmethod
    def _luhn_check(nss):
        if not nss.isdigit() or len(nss) != 11: return False
        digits = [int(d) for d in nss]
        checksum = 0
        for i, d in enumerate(reversed(digits)):
            if i % 2 == 1:
                d *= 2
                if d > 9: d -= 9
            checksum += d
        return checksum % 10 == 0

    def _set_error(self, msg):
        self.is_valid = False
        self.error_message = msg
        self.error_text.value = msg
        self.error_text.visible = True
        self.input_box.border = ft.Border.all(2, "#EF4444")  # Red 500
        # Shake effect wrapper logic if needed, simple red border for now
        if self.page:
            self.error_text.update()
            self.input_box.update()

    def _set_success(self):
        """Novo: Feedback visual de éxito (borde verde)"""
        self.is_valid = True
        self.error_message = ""
        self.error_text.visible = False
        self.input_box.border = ft.Border.all(2, "#10B981")  # Emerald 500
        if self.page:
            self.error_text.update()
            self.error_text.update()
            self.input_box.update()

    def _clear_error(self):
        self.is_valid = True
        self.error_message = ""
        self.error_text.visible = False
        self.input_box.border = ft.Border.all(1, THEME["colors"]["divider"])
        if self.page:
            self.error_text.update()
            self.input_box.update()

    def _on_change_wrapper(self, e):
        val = e.control.value

        # Limitar longitud
        if self.max_length and len(val) > self.max_length:
            e.control.value = val[:self.max_length]
            e.control.update()
            return

        # NOTA: Formateo de moneda desactivado en on_change para evitar que el cursor se mueva
        # El formateo se hace en on_blur (al perder el foco)

        # Validación en tiempo real (solo feedback visual, sin formateo)
        if val:
            is_valid, error_msg = self._validate_field(val)
            if is_valid:
                self._set_success()
            elif error_msg:
                self._set_error(error_msg)
        else:
            self._clear_error()

        if self._ext_ch: self._ext_ch(e)

    def _validate_field(self, val):
        """Validación mejorada en tiempo real"""
        if not val:
            return True, ""

        if self.tipo_validacion == "NSS":
            if len(val) < 11:
                return False, "NSS incompleto (11 dígitos)"
            elif len(val) == 11:
                if not self._luhn_check(val):
                    return False, "NSS inválido"
                return True, ""

        elif self.tipo_validacion == "RFC":
            # RFC: 4 letras + 6 dígitos + 3 alfanuméricos = 13 caracteres
            if len(val) < 13:
                return False, "RFC incompleto"
            elif len(val) == 13:
                if not re.match(r'^[A-ZÑ&]{4}\d{6}[A-Z0-9]{3}$', val.upper()):
                    return False, "RFC inválido"
                return True, ""

        elif self.tipo_validacion == "CURP":
            # CURP: 18 caracteres
            if len(val) < 18:
                return False, "CURP incompleto (18 caracteres)"
            elif len(val) == 18:
                if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', val.upper()):
                    return False, "CURP inválido"
                return True, ""

        elif self.tipo_validacion == "TEXTO":
            # Validar que no esté vacío y tenga al menos 3 caracteres
            if len(val) < 3:
                return False, "Mínimo 3 caracteres"
            return True, ""

        return True, ""

    def _on_focus_wrapper(self, e):
        # Focus Animation: Scale Up & Green Glow
        self.input_box.border = ft.Border.all(2, "#10B981") # Emerald 500
        self.input_box.shadow = ft.BoxShadow(
            blur_radius=12,
            spread_radius=0,
            color="#10B98133", # Emerald 20% opacity
            offset=ft.Offset(0, 4)
        )
        self.input_box.bgcolor = "#ECFDF5" # Emerald 50
        self.input_box.scale = 1.01 # Modest scale up
        self.input_box.update()

    def _on_blur_wrapper(self, e):
        val = e.control.value

        # Formateo final de moneda
        if self.is_money and val:
            try:
                formatted = format_currency_live(val)
                e.control.value = formatted
                e.control.update()
            except:
                pass

        # Validación final
        if val:
            is_valid, error_msg = self._validate_field(val)
            if is_valid:
                self._set_success()
            elif error_msg:
                self._set_error(error_msg)
            else:
                self._clear_error()
        else:
            # Restaurar estado normal si no hay error
            if not self.error_text.visible:
                self.input_box.border = ft.Border.all(1, "#E5E7EB") # Gray 200
            self.input_box.shadow = None
            self.input_box.bgcolor = "#FFFFFF" # White
            self.input_box.scale = 1.0 # Restore scale
            self.input_box.update()
