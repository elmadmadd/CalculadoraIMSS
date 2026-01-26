
import flet as ft
import time
import platform
import logging

try:
    import psutil
    HAVE_PSUTIL = True
except ImportError:
    HAVE_PSUTIL = False

class PerformanceManager:
    """
    Gestiona el modo de rendimiento de la aplicación.
    Detecta automáticamente el hardware y ajusta las animaciones.
    """
    def __init__(self):
        self.performance_mode = self._detect_hardware()
        self.user_override = None # Puede ser True (Reduced) o False (Full)
        
    def _detect_hardware(self):
        """
        Detecta el hardware y sugiere el modo de rendimiento.
        Returns: True si debe usar modo reducido, False si puede usar animaciones completas
        """
        try:
            if not HAVE_PSUTIL:
                # Si no hay psutil, asumimos modo seguro (reducido) en Windows/Linux genérico
                # En Mac solemos tener buen rendimiento gráfico
                return platform.system() != "Darwin"
                
            # Criterios para modo reducido:
            # 1. Menos de 4 núcleos lógicos
            cpu_count = psutil.cpu_count()
            if cpu_count and cpu_count < 4:
                return True
                
            # 2. Menos de 8GB de RAM
            mem = psutil.virtual_memory()
            if mem.total < 8 * (1024**3): # 8 GB
                return True
                
            return False # Hardware suficiente
        except:
            return True # Ante la duda, modo rendimiento
            
    def should_reduce_animations(self) -> bool:
        """Retorna True si se deben reducir las animaciones"""
        if self.user_override is not None:
            return self.user_override
        return self.performance_mode
        
    def set_user_preference(self, reduced: bool):
        """Permite al usuario forzar un modo específico"""
        self.user_override = reduced
        
    def reset_to_auto(self):
        """Vuelve a la detección automática"""
        self.user_override = None
        
    def get_animation_duration(self, default_ms: int) -> int:
        """Retorna la duración de animación ajustada según el modo"""
        if self.should_reduce_animations():
            return int(default_ms * 0.5) # 50% más rápido
        return default_ms

    def get_delay_multiplier(self) -> float:
        """Retorna el multiplicador para delays entre animaciones"""
        if self.should_reduce_animations():
            return 0.3 # 70% menos espera
        return 1.0
        
    def get_status_text(self) -> str:
        """Retorna texto descriptivo del estado actual"""
        is_reduced = self.should_reduce_animations()
        mode_str = "Reducido" if is_reduced else "Completo"
        source = "(Manual)" if self.user_override is not None else "(Auto)"
        return f"{mode_str} {source}"

# Instancia global
performance_manager = PerformanceManager()
