"""
TRANSITION EFFECTS MODULE
Inspirado en Palettify.js - Sistema de animaciones y transiciones suaves para Flet.

Características:
- Efectos predefinidos listos para usar
- Sistema de callbacks personalizable
- Animaciones fluidas y optimizadas
- Fácil integración con componentes existentes

Autor: Sistema de Calculadora IMSS
Fecha: Enero 2026
"""

import flet as ft
import asyncio
from typing import Callable, Optional, Dict, Any
from dataclasses import dataclass


# ==========================================
# CONFIGURACIÓN DE EFECTOS PREDEFINIDOS
# ==========================================

@dataclass
class TransitionConfig:
    """Configuración de una transición."""
    duration: int = 300  # Duración en milisegundos
    curve: str = ft.AnimationCurve.EASE_OUT
    scale_factor: float = 1.05
    shadow_blur: int = 20
    shadow_color: str = "#40000000"
    opacity_change: float = 0.0  # 0 = sin cambio


class TransitionEffects:
    """
    Colección de efectos de transición predefinidos.
    Similar a palettifyStyles en Palettify.js
    """
    
    # Efecto 1: Elevación suave (Material Design)
    ELEVATION = TransitionConfig(
        duration=250,
        curve=ft.AnimationCurve.EASE_OUT,
        scale_factor=1.02,
        shadow_blur=24,
        shadow_color="#33000000"
    )
    
    # Efecto 2: Glow brillante (Neomorphism)
    GLOW = TransitionConfig(
        duration=300,
        curve=ft.AnimationCurve.EASE_IN_OUT,
        scale_factor=1.05,
        shadow_blur=30,
        shadow_color="#603B82F6"  # Azul con alpha
    )
    
    # Efecto 3: Bounce suave
    BOUNCE = TransitionConfig(
        duration=400,
        curve=ft.AnimationCurve.BOUNCE_OUT,
        scale_factor=1.08,
        shadow_blur=15,
        shadow_color="#40000000"
    )
    
    # Efecto 4: Fade elegante
    FADE = TransitionConfig(
        duration=350,
        curve=ft.AnimationCurve.EASE_IN_OUT,
        scale_factor=1.0,  # Sin escala
        shadow_blur=0,
        opacity_change=0.2  # Cambio de opacidad
    )
    
    # Efecto 5: Zoom dramático
    ZOOM = TransitionConfig(
        duration=200,
        curve=ft.AnimationCurve.EASE_OUT,
        scale_factor=1.1,
        shadow_blur=35,
        shadow_color="#50000000"
    )
    
    # Efecto 6: Slide sutil
    SLIDE = TransitionConfig(
        duration=280,
        curve=ft.AnimationCurve.EASE_OUT,
        scale_factor=1.03,
        shadow_blur=18,
        shadow_color="#30000000"
    )
    
    # Efecto 7: Pulse (latido)
    PULSE = TransitionConfig(
        duration=150,
        curve=ft.AnimationCurve.LINEAR,
        scale_factor=1.06,
        shadow_blur=25,
        shadow_color="#4010B981"  # Verde esmeralda
    )
    
    # Efecto 8: Smooth (ultra suave)
    SMOOTH = TransitionConfig(
        duration=400,
        curve=ft.AnimationCurve.DECELERATE,
        scale_factor=1.04,
        shadow_blur=20,
        shadow_color="#35000000"
    )


# ==========================================
# CONTENEDOR INTERACTIVO CON TRANSICIONES
# ==========================================

class AnimatedContainer(ft.Container):
    """
    Contenedor con efectos de transición automáticos.
    
    Uso básico:
        card = AnimatedContainer(
            content=ft.Text("Hover me!"),
            effect=TransitionEffects.GLOW,
            on_enter=lambda: print("Mouse entered!"),
            on_leave=lambda: print("Mouse left!")
        )
    
    Callbacks disponibles (estilo Palettify):
        - before_enter_callback: Se ejecuta antes de la animación de entrada
        - after_enter_callback: Se ejecuta después de la animación de entrada
        - before_leave_callback: Se ejecuta antes de la animación de salida
        - after_leave_callback: Se ejecuta después de la animación de salida
        - on_ready_callback: Se ejecuta cuando el componente está listo
    """
    
    def __init__(
        self,
        content,
        effect: TransitionConfig = TransitionEffects.ELEVATION,
        # Callbacks estilo Palettify
        before_enter_callback: Optional[Callable] = None,
        after_enter_callback: Optional[Callable] = None,
        before_leave_callback: Optional[Callable] = None,
        after_leave_callback: Optional[Callable] = None,
        on_ready_callback: Optional[Callable] = None,
        # Configuración adicional
        disabled: bool = False,
        custom_enter_effect: Optional[Dict[str, Any]] = None,
        custom_leave_effect: Optional[Dict[str, Any]] = None,
        **kwargs
    ):
        # Configurar animaciones
        super().__init__(
            content=content,
            animate=ft.Animation(effect.duration, effect.curve),
            animate_scale=ft.Animation(effect.duration, effect.curve),
            animate_opacity=ft.Animation(effect.duration, effect.curve) if effect.opacity_change > 0 else None,
            **kwargs
        )
        
        # Almacenar configuración
        self.effect = effect
        self.disabled = disabled
        self.custom_enter = custom_enter_effect or {}
        self.custom_leave = custom_leave_effect or {}
        
        # Callbacks
        self._before_enter = before_enter_callback
        self._after_enter = after_enter_callback
        self._before_leave = before_leave_callback
        self._after_leave = after_leave_callback
        self._on_ready = on_ready_callback
        
        # Estado original (para restaurar)
        self._original_scale = kwargs.get('scale', 1.0)
        self._original_shadow = kwargs.get('shadow')
        self._original_opacity = kwargs.get('opacity', 1.0)
        
        # Configurar evento hover
        if not disabled:
            self.on_hover = self._handle_hover
    
    def did_mount(self):
        """Callback cuando el componente está montado (equivalente a onReadyCallback)."""
        if self._on_ready:
            try:
                self._on_ready(self)
            except Exception as e:
                print(f"Error en on_ready_callback: {e}")
    
    async def _handle_hover(self, e):
        """Maneja los eventos de hover con callbacks."""
        if self.disabled:
            return
        
        is_entering = e.data == "true"
        
        if is_entering:
            await self._handle_enter(e)
        else:
            await self._handle_leave(e)
    
    async def _handle_enter(self, e):
        """Maneja la entrada del mouse (hover in)."""
        # beforeEnterCallback
        if self._before_enter:
            try:
                result = self._before_enter(self, e)
                if asyncio.iscoroutine(result):
                    await result
            except Exception as ex:
                print(f"Error en before_enter_callback: {ex}")
        
        # Aplicar efectos de entrada
        self._apply_enter_effects()
        
        # afterEnterCallback
        if self._after_enter:
            try:
                result = self._after_enter(self, e)
                if asyncio.iscoroutine(result):
                    await result
            except Exception as ex:
                print(f"Error en after_enter_callback: {ex}")
    
    async def _handle_leave(self, e):
        """Maneja la salida del mouse (hover out)."""
        # beforeLeaveCallback
        if self._before_leave:
            try:
                result = self._before_leave(self, e)
                if asyncio.iscoroutine(result):
                    await result
            except Exception as ex:
                print(f"Error en before_leave_callback: {ex}")
        
        # Aplicar efectos de salida
        self._apply_leave_effects()
        
        # afterLeaveCallback
        if self._after_leave:
            try:
                result = self._after_leave(self, e)
                if asyncio.iscoroutine(result):
                    await result
            except Exception as ex:
                print(f"Error en after_leave_callback: {ex}")
    
    def _apply_enter_effects(self):
        """Aplica los efectos visuales de entrada."""
        # Escala
        scale = self.custom_enter.get('scale', self.effect.scale_factor)
        self.scale = scale
        
        # Sombra
        if self.effect.shadow_blur > 0:
            shadow_blur = self.custom_enter.get('shadow_blur', self.effect.shadow_blur)
            shadow_color = self.custom_enter.get('shadow_color', self.effect.shadow_color)
            self.shadow = ft.BoxShadow(
                blur_radius=shadow_blur,
                spread_radius=2,
                color=shadow_color,
                offset=ft.Offset(0, 4)
            )
        
        # Opacidad
        if self.effect.opacity_change > 0:
            opacity = self.custom_enter.get('opacity', self._original_opacity - self.effect.opacity_change)
            self.opacity = max(0.0, min(1.0, opacity))
        
        # Actualizar
        if self.page:
            self.update()
    
    def _apply_leave_effects(self):
        """Aplica los efectos visuales de salida (restaura estado original)."""
        # Restaurar escala
        self.scale = self.custom_leave.get('scale', self._original_scale)
        
        # Restaurar sombra
        self.shadow = self.custom_leave.get('shadow', self._original_shadow)
        
        # Restaurar opacidad
        self.opacity = self.custom_leave.get('opacity', self._original_opacity)
        
        # Actualizar
        if self.page:
            self.update()
    
    def set_effect(self, new_effect: TransitionConfig):
        """Cambia el efecto de transición dinámicamente."""
        self.effect = new_effect
        self.animate = ft.Animation(new_effect.duration, new_effect.curve)
        self.animate_scale = ft.Animation(new_effect.duration, new_effect.curve)
        if new_effect.opacity_change > 0:
            self.animate_opacity = ft.Animation(new_effect.duration, new_effect.curve)
        if self.page:
            self.update()
    
    def enable(self):
        """Habilita las animaciones."""
        self.disabled = False
        self.on_hover = self._handle_hover
        if self.page:
            self.update()
    
    def disable(self):
        """Deshabilita las animaciones."""
        self.disabled = True
        self.on_hover = None
        # Restaurar estado original
        self._apply_leave_effects()


# ==========================================
# BOTÓN ANIMADO CON EFECTOS
# ==========================================

class AnimatedButton(ft.Button):
    """
    Botón con efectos de transición integrados.
    
    Uso:
        btn = AnimatedButton(
            text="Click me!",
            effect=TransitionEffects.PULSE,
            on_click=lambda e: print("Clicked!")
        )
    """
    
    def __init__(
        self,
        text: str,
        on_click: Optional[Callable] = None,
        effect: TransitionConfig = TransitionEffects.ELEVATION,
        icon: Optional[str] = None,
        **kwargs
    ):
        super().__init__(
            text=text,
            icon=icon,
            on_click=self._handle_click,
            animate_scale=ft.Animation(effect.duration, effect.curve),
            **kwargs
        )
        
        self.effect = effect
        self._user_on_click = on_click
        self._is_pressed = False
        
        # Configurar hover
        self.on_hover = self._handle_hover
    
    def _handle_hover(self, e):
        """Efecto hover."""
        if e.data == "true" and not self._is_pressed:
            self.scale = 1.05
        else:
            self.scale = 1.0
        self.update()
    
    async def _handle_click(self, e):
        """Efecto de click con animación."""
        # Animación de press
        self._is_pressed = True
        self.scale = 0.95
        self.update()
        
        await asyncio.sleep(self.effect.duration / 2000)  # Mitad de la duración
        
        # Restaurar
        self.scale = 1.0
        self._is_pressed = False
        self.update()
        
        # Ejecutar callback del usuario
        if self._user_on_click:
            result = self._user_on_click(e)
            if asyncio.iscoroutine(result):
                await result


# ==========================================
# UTILIDADES DE TRANSICIÓN
# ==========================================

class TransitionUtils:
    """Utilidades para crear transiciones personalizadas."""
    
    @staticmethod
    def create_custom_effect(
        duration: int = 300,
        curve: str = ft.AnimationCurve.EASE_OUT,
        scale: float = 1.05,
        shadow_blur: int = 20,
        shadow_color: str = "#40000000",
        opacity_change: float = 0.0
    ) -> TransitionConfig:
        """
        Crea un efecto de transición personalizado.
        
        Args:
            duration: Duración en milisegundos
            curve: Curva de animación (ft.AnimationCurve)
            scale: Factor de escala al hacer hover
            shadow_blur: Radio de desenfoque de la sombra
            shadow_color: Color de la sombra (hex con alpha)
            opacity_change: Cambio de opacidad (0.0 = sin cambio)
        
        Returns:
            TransitionConfig configurado
        """
        return TransitionConfig(
            duration=duration,
            curve=curve,
            scale_factor=scale,
            shadow_blur=shadow_blur,
            shadow_color=shadow_color,
            opacity_change=opacity_change
        )
    
    @staticmethod
    async def animate_sequence(controls: list, delay_ms: int = 100):
        """
        Anima una secuencia de controles con delay entre cada uno.
        
        Args:
            controls: Lista de controles AnimatedContainer
            delay_ms: Delay entre animaciones en milisegundos
        """
        for control in controls:
            if hasattr(control, '_apply_enter_effects'):
                control._apply_enter_effects()
            await asyncio.sleep(delay_ms / 1000)
    
    @staticmethod
    def get_all_effects() -> Dict[str, TransitionConfig]:
        """Retorna un diccionario con todos los efectos disponibles."""
        return {
            "ELEVATION": TransitionEffects.ELEVATION,
            "GLOW": TransitionEffects.GLOW,
            "BOUNCE": TransitionEffects.BOUNCE,
            "FADE": TransitionEffects.FADE,
            "ZOOM": TransitionEffects.ZOOM,
            "SLIDE": TransitionEffects.SLIDE,
            "PULSE": TransitionEffects.PULSE,
            "SMOOTH": TransitionEffects.SMOOTH,
        }


# ==========================================
# EJEMPLO DE USO
# ==========================================

def create_demo_card(effect_name: str, effect: TransitionConfig) -> AnimatedContainer:
    """Crea una tarjeta de demostración para un efecto."""
    return AnimatedContainer(
        content=ft.Column([
            ft.Text(effect_name, size=16, weight="bold", color="white"),
            ft.Text(f"Duration: {effect.duration}ms", size=12, color="#CBD5E1"),
            ft.Text(f"Scale: {effect.scale_factor}x", size=12, color="#CBD5E1"),
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=5),
        effect=effect,
        width=200,
        height=120,
        bgcolor="#1E293B",
        border_radius=12,
        padding=20,
        alignment=ft.Alignment(0, 0),
        # Callbacks de ejemplo
        before_enter_callback=lambda self, e: print(f"🎯 Entrando a {effect_name}"),
        after_leave_callback=lambda self, e: print(f"👋 Saliendo de {effect_name}"),
    )
