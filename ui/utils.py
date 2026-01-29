import asyncio
import re
import flet as ft
from utils.formatting import clean_float

async def shake_control(control, page):
    """
    Aplica animación premium de 'pulse + glow' para indicar error.
    Usa escala suave, sombra roja brillante y transición de borde elegante.
    """
    try:
        # Guardar valores originales
        original_border = control.border_color if hasattr(control, 'border_color') else None
        original_shadow = control.shadow if hasattr(control, 'shadow') else None
        original_scale = control.scale if hasattr(control, 'scale') else 1.0

        # Configurar animaciones suaves
        if hasattr(control, 'animate_scale'):
            control.animate_scale = ft.Animation(300, "easeOutCubic")
        if hasattr(control, 'animate_shadow'):
            control.animate_shadow = ft.Animation(300, "easeOut")

        # Efecto de Glow (resplandor rojo) + Pulse
        if hasattr(control, 'shadow'):
            control.shadow = ft.BoxShadow(
                spread_radius=4,
                blur_radius=20,
                color="#EF444440",  # Rojo con transparencia
                offset=ft.Offset(0, 0)
            )

        # Cambiar borde a rojo suave
        if hasattr(control, 'border_color'):
            control.border_color = "#EF4444"

        # Primera pulsación (agrandar)
        if hasattr(control, 'scale'):
            control.scale = 1.05

        control.update()
        await asyncio.sleep(0.15)

        # Segunda pulsación (encoger)
        if hasattr(control, 'scale'):
            control.scale = 0.98
        control.update()
        await asyncio.sleep(0.15)

        # Volver a tamaño normal
        if hasattr(control, 'scale'):
            control.scale = 1.0
        control.update()

        # Mantener el glow por 1.5 segundos
        await asyncio.sleep(1.5)

        # Restaurar valores originales con transición suave
        if hasattr(control, 'border_color'):
            control.border_color = original_border
        if hasattr(control, 'shadow'):
            control.shadow = original_shadow
        if hasattr(control, 'scale'):
            control.scale = original_scale

        control.update()

    except Exception as e:
        print(f"Error en shake_control: {e}")

async def animate_rolling_currency(control, start, end, duration=1.0):
    """
    Versión OPTIMIZADA: Asignación directa sin animación rolling.
    Elimina consumo de CPU/GPU innecesario.
    """
    try:
        e_val = clean_float(end)
        control.value = f"${e_val:,.2f}"
        if control.page: control.update()
    except:
        pass

    # Dummy wait para mantener compatibilidad si se espera await
    await asyncio.sleep(0)

def format_currency_live(value: str) -> str:
    """
    Formatea un string como moneda mientras el usuario escribe.
    """
    try:
        # Remover todo excepto dígitos y punto decimal
        cleaned = re.sub(r'[^\d.]', '', value)
        if not cleaned or cleaned == '.':
            return ""

        # Manejar múltiples puntos decimales
        parts = cleaned.split('.')
        if len(parts) > 2:
            cleaned = parts[0] + '.' + ''.join(parts[1:])

        # Convertir a float y formatear
        num = float(cleaned)
        return f"{num:,.2f}"
    except:
        return value

def validate_field(value: str, field_type: str = "number", min_val: float = None, max_val: float = None) -> tuple[bool, str]:
    """
    Valida un campo de entrada en tiempo real.
    """
    if not value or value.strip() == "":
        return False, "Campo requerido"

    if field_type in ["number", "currency"]:
        try:
            num = clean_float(value)
            if min_val is not None and num < min_val:
                return False, f"Mínimo: ${min_val:,.2f}" if field_type == "currency" else f"Mínimo: {min_val}"
            if max_val is not None and num > max_val:
                return False, f"Máximo: ${max_val:,.2f}" if field_type == "currency" else f"Máximo: {max_val}"
            return True, ""
        except:
            return False, "Valor inválido"

    elif field_type == "rfc":
        # RFC: 4 letras + 6 dígitos + 3 caracteres alfanuméricos
        if not re.match(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$', value.upper()):
            return False, "RFC inválido (ej: XAXX010101000)"
        return True, ""

    elif field_type == "curp":
        # CURP: 18 caracteres
        if not re.match(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[0-9A-Z]\d$', value.upper()):
            return False, "CURP inválido (18 caracteres)"
        return True, ""

    elif field_type == "nss":
        # NSS: 11 dígitos
        cleaned = re.sub(r'\D', '', value)
        if len(cleaned) != 11:
            return False, "NSS debe tener 11 dígitos"
        return True, ""

    return True, ""

async def render_cards_progressively(
    container: ft.Control,
    items: list,
    builder_func: callable,
    batch_size: int = 6,  # Cards visibles en pantalla típica
    delay_ms: int = 15
):
    """
    Renderiza cards con lazy loading y animación inicial.

    MEJORA UX: Solo las primeras cards visibles (~6) se animan.
    El resto se carga inmediatamente al hacer scroll.
    """
    container.controls.clear()

    visible_count = min(batch_size, len(items))  # Máximo 6 visibles iniciales

    # FASE 1: Crear las cards visibles (con animación)
    visible_cards = []
    for i, item in enumerate(items[:visible_count]):
        try:
            card = builder_func(item)
            card.opacity = 0
            card.scale = 0.96
            card.offset = ft.Offset(0, 0.1)
            visible_cards.append(card)
            container.controls.append(card)
        except Exception as e:
            print(f"Error creating card: {e}")
            continue

    # FASE 2: Crear el resto de cards (sin animación, visibles directamente)
    for item in items[visible_count:]:
        try:
            card = builder_func(item)
            card.opacity = 1  # Ya visibles
            card.scale = 1
            card.offset = ft.Offset(0, 0)
            container.controls.append(card)
        except Exception as e:
            print(f"Error creating card: {e}")
            continue

    # Mostrar contenedor
    try:
        container.update()
    except:
        return

    # FASE 3: Animar solo las cards visibles en cascada
    await asyncio.sleep(0.03)  # Breve pausa

    stagger_delay = 0.05  # 50ms entre cada card visible
    for idx, card in enumerate(visible_cards):
        asyncio.create_task(_animate_card_entrance(card, idx * stagger_delay))

    await asyncio.sleep(0.08)
    try:
        container.update()
    except:
        pass

async def _animate_card_entrance(card, delay: float):
    """Anima la entrada de una card con fade + scale + slide."""
    try:
        await asyncio.sleep(delay)
        card.opacity = 1
        card.scale = 1
        card.offset = ft.Offset(0, 0)
        if hasattr(card, 'update') and card.page:
            card.update()
    except:
        pass
