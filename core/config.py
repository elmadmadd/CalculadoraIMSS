from core.system import SyncManager
from pathlib import Path

class GestorConfiguracion:
    def __init__(self):
        # No definimos ruta de archivo para evitar lecturas accidentales
        # --- FACTORES HARDCODED (Fuente Única de Verdad - Fallback) ---
        self.defaults = {
            "TRADICIONAL": {
                "MASNOMINA": {
                    # CAT: 45.48%, Tasa Mensual: 2.73%
                    # Plazos: 24, 36, 48, 60 meses
                    # Mínimo: $8,000
                    "60": 37.06127,
                    "48": 40.45340,
                    "36": 46.62260,
                    "24": 59.80870,
                },
                "CONSUPAGO": {
                    # CAT: 44.98%, Tasas variables por plazo
                    # 12-24m: 2.73% (min $35,000)
                    # 36-48m: 2.72% (min $15,000)
                    # 54-60m: 2.69% (min $10,000)
                    "60": 36.88917,   # 2.69%
                    "54": 38.35000,   # 2.69%
                    "48": 40.37447,   # 2.72%
                    "36": 46.54880,   # 2.72%
                    "24": 59.77820,   # 2.73%
                    "12": 101.15000   # 2.73%
                },
                "OPCIPRES": {
                    # CAT: 42.26%, Tasas variables por plazo
                    # 36m: 2.67% (min $70,000)
                    # 48m: 2.67% (min $50,000)
                    # 54m: 2.62% (min $45,000)
                    # 60m: 2.57-2.59% (min $30,000)
                    "60": 35.98562,   # 2.57-2.59%
                    "54": 37.65909,   # 2.62%
                    "48": 39.86520,   # 2.67%
                    "36": 46.47090,   # 2.67%
                },
                "CONSUBANCO": {
                    # CAT: 39.03%, Tasas variables por plazo
                    # 36m: 2.50% (min $145,000)
                    # 48m: 2.50% (min $100,000)
                    # 54m: 2.44% (min $95,000)
                    # 60m: 2.40% (min $40,000)
                    "60": 34.47785,   # 2.40%
                    "54": 36.50000,   # 2.44%
                    "48": 38.34620,   # 2.50%
                    "36": 45.48000,   # 2.50%
                },
            },
            "RETENCION": {
            # CALIBRACIÓN DICIEMBRE 2025 (Solo 60 meses)
            "CONSUBANCO": {"60": 30.95418}, # Validado con $620k
            "CONSUPAGO":  {"60": 33.25726}, # Validado con $500k
            "OPCIPRES":   {"60": 32.22283}, # Validado con $600k
            },
            "REGLAS_RETENCION_CNCA": {
                "CONSUBANCO": 100000.0, "OPCIPRES": 75000.0, "CONSUPAGO": 50000.0, "MASNOMINA": 999999999.0
            },
            "REGLAS_RETENCION_NUEVO": {
                # REGLAS ESTÁNDAR 2026
                "CONSUBANCO": 200000.0,
                "OPCIPRES": 125000.0,
                "CONSUPAGO": 100000.0,
                "MASNOMINA": 999999999.0
            },
            "METADATA": {
            # Actualización de CATs Retención
            "CONSUBANCO": {"tasa_ref": "2.50%", "cat": "39.03%", "cat_tr": "31.50%", "codigo": "CSB"},
            "CONSUPAGO":  {"tasa_ref": "2.73%", "cat": "44.20%", "cat_tr": "36.42%", "codigo": "CSP"}, # Nuevo CAT Ret
            "OPCIPRES":   {"tasa_ref": "2.67%", "cat": "42.26%", "cat_tr": "34.21%", "codigo": "OPC"}, # Nuevo CAT Ret
            "MASNOMINA":  {"tasa_ref": "2.73%", "cat": "44.57%", "cat_tr": "46.50%", "codigo": "MN"}
        },
            "INSURANCE_FACTORS": {
                # Estructura: Plazo: (Factor_1_Año, Factor_Total)
                "CONSUBANCO": {
                    "36": (0.020390, 0.040555), "48": (0.021213, 0.055907),
                    "54": (0.021461, 0.063849), "60": (0.021671, 0.072217)
                },
                "CONSUPAGO": {
                    "12": (0.012956, 0.012956), "24": (0.018685, 0.026342),
                    "36": (0.020507, 0.041028), "48": (0.021362, 0.056948),
                    "54": (0.021623, 0.065283), "60": (0.021827, 0.073927)
                },
                "OPCIPRES": {
                    "36": (0.020501, 0.041001), "48": (0.021325, 0.056689),
                    "54": (0.021576, 0.064861), "60": (0.021770, 0.073298),
                    "999": (0.023390, 0.076685) # Caso especial Retención 60m
                },
                "MASNOMINA": {
                    "24": (0.018687, 0.026347), "36": (0.020513, 0.041051),
                    "48": (0.021367, 0.056986), "60": (0.021837, 0.074045)
                }
            }

        }

        # ESTRATEGIA OFFLINE-FIRST:
        # 1. Cargar defaults hardcoded (Red de seguridad)
        self.datos = self.defaults

        # 2. Intentar cargar cache persistente (Datos offline)
        cached_data = SyncManager.load_local()
        if cached_data:
            print("✓ Configuración local cargada")
            self.datos = cached_data

    def actualizar_memoria(self, nuevos_datos):
        """Actualiza la configuración en caliente tras sync remoto"""
        if nuevos_datos:
            self.datos = nuevos_datos
            print("✓ Configuración en memoria actualizada (Hot Reload)")

    def cargar(self):
        return self.datos

    def guardar(self, datos):
        pass # Deshabilitado para evitar corrupción

    def actualizar_factor(self, tipo, financiera, plazo, nuevo_factor):
        pass

    def restaurar_fabrica(self):
        return True

CONSTANTES = {
    "RUTAS": {
        "DOCS": Path.home() / "Documents",
        "PERFIL": Path.home() / "Documents" / "perfil_asesor_secure.json",
        "CONFIG": Path.home() / "Documents" / "consucalcu_config.json",
        "REPORTES": Path.home() / "Documents" / "ReportesDiarios",
        "SESION": Path.home() / "Documents" / "datos_sesion_temp.json"
    }
}

PRODUCTO_CONFIG = ["MASNOMINA", "CONSUBANCO", "CONSUPAGO", "OPCIPRES"]
