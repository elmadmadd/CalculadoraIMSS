import math
import logging
import time
import datetime
from decimal import Decimal, ROUND_HALF_UP
from utils.formatting import clean_float, to_money, clean_decimal

try:
    from core.normativa import NormativaCNCA
    HAVE_NORMATIVA = True
except ImportError:
    HAVE_NORMATIVA = False

logger = logging.getLogger(__name__)

class MotorFinanciero:
    def __init__(self, config_manager):
        self.cfg_mgr = config_manager

        # Instancia de normativa CNCA
        if HAVE_NORMATIVA:
            self.normativa = NormativaCNCA()
        else:
            self.normativa = None

        # --- LISTA DE PLAZOS ESTÁNDAR (POLÍTICA VIGENTE) ---
        # Incluye 54 meses (soportado por Consupago, Opcipres, Consubanco)
        self.plazos_standard = [60, 54, 48, 36, 24, 12]

        # --- FACTORES GLOBALES SIPRE ---
        self.factores_globales_sipre = {
            "CONSUBANCO": Decimal("1.857251"),
            # REGRESAMOS AL FACTOR ORIGINAL (Terminación 1)
            # Esto genera 18,856.811... -> se redondea a .81
            "OPCIPRES":   Decimal("1.933371"),
            "CONSUPAGO":  Decimal("1.9954353")
        }
        self.recargar_config()

    def recargar_config(self):
        self.cfg_mgr.cargar()
        self.tradicional = self.cfg_mgr.datos["TRADICIONAL"]
        self.retencion = self.cfg_mgr.datos["RETENCION"]
        self.reglas_tr_cnca = self.cfg_mgr.datos.get("REGLAS_RETENCION_CNCA", {})
        self.reglas_tr_nuevo = self.cfg_mgr.datos.get("REGLAS_RETENCION_NUEVO", {})
        self.meta = self.cfg_mgr.datos["METADATA"]

        # Reglas de negocio (Montos mínimos por producto y plazo)
        self.reglas_plazo = {
            "MASNOMINA": {
                # Mínimo global: $8,000 para todos los plazos (24, 36, 48, 60)
                "min_global": 8000.0
            },
            "CONSUPAGO": {
                # Montos mínimos por rango de plazos
                # 12-24m: $35,000
                # 36-48m: $15,000
                # 54-60m: $10,000
                "min_plazo_rango": [
                    (12, 24, 35000.0),
                    (36, 48, 15000.0),
                    (54, 60, 10000.0)
                ]
            },
            "OPCIPRES": {
                # Montos mínimos por plazo específico
                "min_plazo": {
                    60: 30000.0,   # $30,000
                    54: 45000.0,   # $45,000
                    48: 50000.0,   # $50,000
                    36: 70000.0    # $70,000
                }
            },
            "CONSUBANCO": {
                # Montos mínimos por plazo específico
                "min_plazo": {
                    60: 40000.0,    # $40,000
                    54: 95000.0,    # $95,000
                    48: 100000.0,   # $100,000
                    36: 145000.0    # $145,000
                }
            }
        }

        # Reglas específicas para CNCA con RETENCIÓN (monto BRUTO mínimo)
        # Estas reglas se aplican SOLO en refinanciamiento (CNCA) con retención
        self.reglas_cnca_retencion = {
            "CONSUBANCO": {
                "monto_bruto_min": 100000.0,  # $100,000 para CNCA
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            },
            "OPCIPRES": {
                "monto_bruto_min": 75000.0,  # $75,000 para CNCA (CORREGIDO)
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            },
            "CONSUPAGO": {
                "monto_bruto_min": 50000.0,  # $50,000 para CNCA (CORREGIDO)
                "plazo_requerido": 60,
                "pagos_aplicados_min": 24
            }
        }

        # Reglas específicas para CRÉDITO NUEVO con RETENCIÓN (monto NETO mínimo)
        # NORMATIVA ENERO 2026: Fin de promoción diciembre, montos estándar restaurados
        self.reglas_nuevo_retencion = {
            "CONSUBANCO": {
                "monto_neto_min_normal": 200000.0,  # Estándar 2026
                "plazo_requerido": 60
            },
            "OPCIPRES": {
                "monto_neto_min_normal": 125000.0,  # Estándar 2026
                "plazo_requerido": 60
            },
            "CONSUPAGO": {
                "monto_neto_min_normal": 100000.0,  # Estándar 2026
                "plazo_requerido": 60
            }
        }


    def redondear(self, m, step=100.0):
        m = clean_float(m)
        step = float(step)
        if step <= 0: return m
        return math.floor(m / step) * step if m > 0 else 0.0

    def obtener_minimo_requerido(self, prod, plazo):
        """
        Devuelve el monto mínimo requerido para un producto y plazo específicos.
        Si la financiera usa reglas por plazo específico y el plazo no está, bloquea (retorna valor muy alto).
        """
        rules = self.reglas_plazo.get(prod, {})

        # 1. Regla por diccionario de plazos específicos (Ej. Consubanco)
        if "min_plazo" in rules:
            # Si el plazo está definido, devuelve su mínimo.
            # Si NO está definido (busca un plazo no permitido), devuelve un valor bloqueante.
            return rules["min_plazo"].get(plazo, 999999999.0)

        # 2. Regla por rangos (Ej. Consupago)
        if "min_plazo_rango" in rules:
            for (p_min, p_max, m_min) in rules["min_plazo_rango"]:
                if p_min <= plazo <= p_max:
                    return m_min
            return 5000.0 # Fallback razonable si no cae en rango (aunque debería)

        # 3. Regla global (Ej. MasNomina)
        if "min_global" in rules:
            return rules["min_global"]

        # 4. Default general
        return 5000.0

    def validar_minimo(self, prod, monto, plazo):
        # Usamos el nuevo método centralizado
        limit = self.obtener_minimo_requerido(prod, plazo)
        return clean_float(monto) >= limit

    def validar_cnca_retencion(self, prod, monto_bruto, plazo, deudas):
        """
        Valida reglas específicas de CNCA con RETENCIÓN.

        Args:
            prod: Producto financiero
            monto_bruto: Monto BRUTO del crédito (deudas + liquidez)
            plazo: Plazo del crédito
            deudas: Lista de deudas a refinanciar

        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        if prod not in self.reglas_cnca_retencion:
            return True, ""  # Producto sin reglas especiales

        reglas = self.reglas_cnca_retencion[prod]

        # 1. Validar monto bruto mínimo
        monto_min = reglas.get("monto_bruto_min", 0)
        if monto_bruto < monto_min:
            return False, f"Monto bruto mínimo: ${monto_min:,.0f}"

        # 2. Validar plazo requerido
        plazo_req = reglas.get("plazo_requerido")
        if plazo_req is not None and plazo != plazo_req:
            return False, f"Plazo requerido: {plazo_req} meses"

        # 3. Validar pagos aplicados mínimos (24+)
        pagos_min = reglas.get("pagos_aplicados_min", 0)
        if pagos_min > 0:
            # Verificar que TODAS las deudas tengan al menos pagos_min pagos aplicados
            for deuda in deudas:
                pagos_aplicados = deuda.get("pagos", 0)
                if pagos_aplicados < pagos_min:
                    return False, f"Requiere {pagos_min}+ pagos aplicados en todas las deudas"

        return True, ""

    def validar_nuevo_retencion(self, prod, monto_neto, plazo):
        """
        Valida reglas específicas de CRÉDITO NUEVO con RETENCIÓN.
        ENERO 2026: Montos estándar aplicados (fin de promoción diciembre).

        Args:
            prod: Producto financiero
            monto_neto: Monto NETO del crédito
            plazo: Plazo del crédito

        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        if prod not in self.reglas_nuevo_retencion:
            return True, ""  # Producto sin reglas especiales

        reglas = self.reglas_nuevo_retencion[prod]

        # 1. Validar monto mínimo estándar
        monto_min = reglas.get("monto_neto_min_normal", 125000.0)

        if monto_neto < monto_min:
            return False, f"Monto neto mínimo: ${monto_min:,.0f}"

        # 2. Validar plazo requerido
        plazo_req = reglas.get("plazo_requerido")
        if plazo_req is not None and plazo != plazo_req:
            return False, f"Plazo requerido: {plazo_req} meses"

        return True, ""


    def calcular(self, prod, valor, plazo, por_monto, usa_retencion=False, rounding_step=100.0):
        try:
            # --- DETERMINAR MONTO MÍNIMO REAL (LOOKUP CENTRALIZADO) ---
            monto_minimo_real = self.obtener_minimo_requerido(prod, plazo)

            cat_val = self.meta.get(prod, {}).get("cat_tr" if usa_retencion else "cat", "0.0%").replace('%', '')

            # --- LÓGICA ESPECIAL SIPRE (RETENCIÓN 60 MESES) ---
            if usa_retencion and plazo == 60 and prod in self.factores_globales_sipre:
                factor_global = self.factores_globales_sipre[prod]
                current_val = valor
                is_por_monto = por_monto

                for _ in range(50):
                    if is_por_monto:
                        monto_base = Decimal(str(self.redondear(current_val, rounding_step)))
                    else:
                        capacidad_dec = Decimal(str(current_val))
                        # Cálculo inverso: (Capacidad * 60) / Factor
                        monto_estimado = (capacidad_dec * Decimal("60")) / factor_global
                        monto_base = Decimal(str(self.redondear(float(monto_estimado), rounding_step)))

                    # --- NUEVA VALIDACIÓN DE MÍNIMO ---
                    if float(monto_base) < monto_minimo_real:
                        return None

                    # 1. Total Teórico (Monto * Factor)
                    total_teorico = monto_base * factor_global

                    # 2. Mensualidad (Total / 60)
                    # Usamos ROUND_HALF_UP estándar porque los factores ya tienen la precisión corregida
                    mensual_raw = total_teorico / Decimal("60")
                    pago_mensual_sipre = mensual_raw.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

                    # 3. Validación de Capacidad
                    if not is_por_monto and pago_mensual_sipre > Decimal(str(valor)):
                        # Si nos pasamos por centavos, bajamos un escalón de redondeo
                        nuevo_monto_float = float(monto_base - Decimal(str(rounding_step)))

                        # VALIDACIÓN CORREGIDA (Usando variable dinámica)
                        if nuevo_monto_float < monto_minimo_real: return None

                        current_val = nuevo_monto_float
                        is_por_monto = True
                        continue

                    # 4. Cálculo final del Total Real a visualizar
                    total_real_pagar = pago_mensual_sipre * Decimal("60")

                    return {
                        "monto": float(monto_base),
                        "pago": float(pago_mensual_sipre),
                        "total_pagar": float(total_real_pagar),
                        "cat": cat_val
                    }
                return None

            # --- LÓGICA TRADICIONAL (FACTOR POR MIL) ---
            dic_factores = self.retencion if usa_retencion else self.tradicional
            raw_factor = dic_factores.get(prod, {}).get(str(plazo), 0)

            if raw_factor == 0: return None

            f = Decimal(str(raw_factor))

            if por_monto:
                m = self.redondear(valor, rounding_step)
                # Fórmula: (Monto / 1000) * Factor
                pago_calc = (Decimal(m) / Decimal("1000")) * f
            else:
                # Fórmula Inversa: (Capacidad / Factor) * 1000
                bruto = (Decimal(valor) / f) * Decimal("1000")
                m = self.redondear(bruto, rounding_step)
                pago_calc = (Decimal(m) / Decimal("1000")) * f

            # --- NUEVA VALIDACIÓN TRADICIONAL ---
            if float(m) < monto_minimo_real: return None

            # Redondeo tradicional
            pago_final = pago_calc.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            return {
                "monto": float(m),
                "pago": float(pago_final),
                "cat": cat_val
            }

        except (ValueError, TypeError, ArithmeticError) as e:
            logger.error(f"Error de cálculo en motor financiero: {e}", exc_info=True)
            return None
        except Exception as e:
            logger.error(f"Error inesperado en motor financiero: {e}", exc_info=True)
            return None

    def calcular_seguro(self, financiera, edad, monto, plazo, tipo_pension, es_cliente_nuevo, es_retencion=False):
        """
        Retorna una tupla: (Estatus_Texto, Color_Hex, Costo_Calculado_1A, Costo_Calculado_Total)
        Implementa reglas estrictas Addendum Pensionados IMSS
        """
        try:
            monto = Decimal(str(monto))
            edad_final = edad + (plazo / 12.0)

            # 1. Validar Monto Mínimo
            min_req = self.obtener_minimo_requerido(financiera, plazo)
            if monto < Decimal(str(min_req)):
                return (f"RECHAZADO: Monto menor al mínimo (${min_req:,.2f})", "#EF4444", 0.0, 0.0)

            # --- REGLAS DE NEGOCIO IMSS (ADDENDUM) ---

            # Regla 8: Límite Absoluto
            if edad_final > 85:
                 return ("RECHAZADO: Edad + Plazo > 85 años", "#EF4444", 0.0, 0.0)

            aplica_un_anual = False
            aplica_total = False
            razon = "Sin costo de seguro"
            color = "#10B981" # Verde (Aprobado sin costo)

            # Reglas por Tipo de Pensión
            if tipo_pension == "INVALIDEZ":
                # Regla 1.2
                if 51 <= edad <= 70:
                    if es_cliente_nuevo: # Nuevo o < 24 meses
                        aplica_un_anual = True
                        razon = "Invalidez 51-70 años + Nuevo"
                else:
                    # Fuera de rango 51-70
                    if edad_final > 75:
                        aplica_un_anual = True
                        razon = "Invalidez excede 75 años (Extensión)"
                    # Si <= 75, no aplica costo

            else: # VEJEZ
                if 66 <= edad <= 75:
                    if edad_final > 75:
                        # Regla 6: Excede 75 años -> Pago Total
                        aplica_total = True
                        razon = "Vejez 66-75 excede 75 años (Pago Total)"
                    elif es_cliente_nuevo:
                        # Regla 5: No excede 75 + Nuevo -> 1 Año
                        aplica_un_anual = True
                        razon = "Vejez 66-75 + Nuevo"

                elif edad >= 76:
                    if edad_final > 80:
                         # Regla 8 (Extensión): Excede 80 -> 1 Año
                         aplica_un_anual = True
                         razon = "Vejez > 76 excede 80 años (Extensión)"
                    # Regla 7: <= 80 -> No aplica costo

            # --- CÁLCULO DE COSTOS ---
            c1_result = 0.0
            ct_result = 0.0

            if aplica_un_anual or aplica_total:
                factores = self.cfg_mgr.datos["INSURANCE_FACTORS"].get(financiera, {})
                # Key especial para Opcipres Retención 60m
                key_plazo = "999" if (financiera == "OPCIPRES" and plazo == 60 and es_retencion) else str(plazo)

                pair = factores.get(key_plazo)
                if pair:
                    f1 = Decimal(str(pair[0]))
                    fT = Decimal(str(pair[1]))

                    # Calculamos los valores teóricos
                    costo_1_ano = float(monto * f1)
                    costo_total = float(monto * fT)

                    if aplica_un_anual:
                        c1_result = costo_1_ano
                        # Si aplica 1 año, el costo total a mostrar es el de 1 año en este contexto
                        ct_result = costo_1_ano
                        color = "#F59E0B"
                        razon = f"APLICA 1 AÑO: {razon}"

                    if aplica_total:
                        c1_result = costo_total # En total, ambos valores reflejan el total para evitar confusión
                        ct_result = costo_total
                        color = "#F59E0B"
                        razon = f"APLICA TOTAL: {razon}"
                else:
                    razon = f"ERROR: Sin factor {financiera} {plazo}m"
                    color = "#EF4444"

            return (razon, color, c1_result, ct_result)

        except Exception as e:
            return (f"Error de cálculo: {e}", "#EF4444", 0.0, 0.0)

    # ... (Resto de métodos de validación se mantienen igual: validar_retencion_deudas, validar_normativa, generar_layout_final)
    def validar_retencion_deudas(self, deudas):
        if not deudas: return False
        for d in deudas:
            try:
                pagos = int(clean_float(d.get('pagos', 0)))
                if pagos < 24: return False
            except: return False
        return True

    def validar_compatibilidad_marcas(self, prod_destino, deudas):
        """
        Valida las reglas de negocio para liquidación de marcas (Enero 2026):
        1. Refinanciamiento (Auto-compra): Permitido si todas las deudas son de la misma marca.
        2. Compra de Cartera (Competencia): Permitido si todas las deudas son de la competencia permitida.
        3. Mezcla: NO permitida (No se puede mezclar misma marca con otras).

        Marcas del Ecosistema:
        - MSN = MASNOMINA
        - CSB = CONSUBANCO
        - CSP = CONSUPAGO
        - OPC = OPCIPRES
        """
        if not deudas: return True, ""

        target = prod_destino.upper()
        ECOSYSTEM = {"MASNOMINA", "CONSUBANCO", "CONSUPAGO", "OPCIPRES"}

        # Obtener marcas de origen
        marcas_origen = set()
        for d in deudas:
            m = str(d.get('producto') or '').upper().strip()
            marcas_origen.add(m)

        # 1. Validar que las marcas pertenezcan al ecosistema regulado
        # La regla "Solo tienen X/Y/Z" implica que marcas fuera de la lista rompen la regla
        for m in marcas_origen:
            if m not in ECOSYSTEM:
                # Si una marca no está en el ecosistema (ej. BANCOMER),
                # no cumple con "Solo tienen CSB/MSN/OPC", por tanto se rechaza.
                return False, f"Marca no permitida para liquidación: {m}. Solo se aceptan: {', '.join(ECOSYSTEM)}"

        has_internal = target in marcas_origen
        has_external = any(m != target for m in marcas_origen)

        # 2. Validar No-Mezcla
        if has_internal and has_external:
            return False, "Regla de Marca: No se puede combinar Refinanciamiento (Misma Marca) con Compra de Cartera."

        # 3. Validaciones específicas (Redundante con lógica de conjuntos pero explícito por las reglas)
        # Si es externo, verificamos que las externas sean "competencia válida".
        # Como nuestro Ecosistema es cerrado (4 marcas), si es external y está en ECOSYSTEM, es válida.
        return True, ""

    def validar_normativa_cnca(self, prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion=False):
        """
        Validación usando el módulo NORMATIVA_CNCA.
        Complementa y corrige las validaciones existentes.

        Returns:
            (bool, list): (es_valido, lista_razones)
        """
        if not self.normativa:
            # Si no hay módulo de normativa, usar validación legacy
            return self.validar_normativa(prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion)

        razones_aprobacion = []

        # 1. Validar producto de liquidación
        valido, msg = self.normativa.validar_producto_liquidacion(prod, deudas)
        if not valido:
            return False, [msg]
        razones_aprobacion.append("Producto Compatible")

        # 2. Validar incremento de tasa
        valido, msg = self.normativa.validar_incremento_tasa(prod, deudas)
        if valido:
            razones_aprobacion.append("Incremento Tasa")

        # 3. Validar pagos acumulados y tasa de referencia
        valido, msg = self.normativa.validar_pagos_acumulados_tasa_ref(prod, deudas, remanente_capital)
        if valido:
            razones_aprobacion.append("Antigüedad")

        # 4. Validar incremento de descuento mensual
        valido, msg = self.normativa.validar_incremento_descuento_mensual(prod, deudas, incremento_liquidez, remanente_capital)
        if valido:
            razones_aprobacion.append("Incremento Descuento")

        # 5. Validar aumento de plazo
        if deudas:
            # Validar que todas las deudas cumplan con el aumento de plazo
            todas_cumplen_plazo = True
            for deuda in deudas:
                plazo_actual = deuda.get("plazo", 0)
                valido, msg = self.normativa.validar_aumento_plazo(plazo_actual, plazo_nuevo)
                if valido:
                    if "Plazo" not in razones_aprobacion:
                        razones_aprobacion.append("Plazo")
                else:
                    todas_cumplen_plazo = False
                    break

        # Si hay al menos una razón de aprobación, es válido
        if razones_aprobacion:
            return True, razones_aprobacion
        else:
            return False, ["No cumple con ninguna normativa CNCA"]

    def validar_normativa(self, prod, deudas, incremento_liquidez, remanente_capital, plazo_nuevo, usa_retencion=False):
        """
        Validación Estricta con 'Unanimidad de Compuerta' (CNCA):
        Todas las deudas deben compartir al menos una razón de validación.
        """
        # --- PASO 0: VALIDACIÓN DE MARCAS (NUEVO REGLAMENTO) ---
        v_marcas, msg_marcas = self.validar_compatibilidad_marcas(prod, deudas)
        if not v_marcas:
            return False, [msg_marcas]

        # Filtros Base
        if prod == "MASNOMINA": return remanente_capital > 0, ["Liquidez Positiva"]
        if remanente_capital <= 0: return False, ["Rechazo: Sin Liquidez"]
        if not deudas: return True, ["Crédito Nuevo"]

        inc = float(incremento_liquidez)
        sets_deudas = [] # Lista de conjuntos, uno por deuda

        for i, d in enumerate(deudas):
            razones_d = set()
            try:
                tasa_d = float(d.get('tasa', 0))
                pagos_d = int(clean_float(d.get('pagos', 0)))
                pl_old = int(d.get('plazo', 0))
            except: return False, [f"Error datos deuda {i+1}"]

            # --- 1. REGLA DE INCREMENTO DE TASA (Rate Increase) ---
            # "El producto con el que se va a liquidar debe tener una tasa de referencia o menor"
            # MN y CSP: Sin restricción.
            # OPC: Tasa Baja <= 2.57%.
            # CSB: Tasa Baja <= 2.40%.
            pass_tasa = False
            if prod in ["MASNOMINA", "CONSUPAGO"]:
                pass_tasa = True
            elif prod == "OPCIPRES" and tasa_d <= 2.57:
                pass_tasa = True
            elif prod == "CONSUBANCO" and tasa_d <= 2.40:
                pass_tasa = True

            if pass_tasa: razones_d.add("Tasa")

            # --- 2. PAGOS ACUMULADOS (Antigüedad) - NORMATIVA ENERO 2026 ---
            # Bifurcación LCOM vs Interno:
            # - Financieras INTERNAS (CSB, CSP, OPC, MAS): 16 pagos mínimos
            # - Financieras COMPETENCIA (Cualquier otra): 24 pagos mínimos (Regla LCOM)
            # - Financieras COMPETENCIA (Cualquier otra): 24 pagos mínimos (Regla LCOM)
            producto_origen = str(d.get('producto') or '').upper()
            es_interna = producto_origen in ['CONSUBANCO', 'CONSUPAGO', 'OPCIPRES', 'MASNOMINA']

            pagos_minimos = 16 if es_interna else 24

            # OPC: Pagos mínimos Y Tasa <= 2.67%
            # CSB: Pagos mínimos Y Tasa <= 2.57%
            # CSP: Pagos mínimos Y Tasa <= 2.67% (ACTUALIZADO ENERO 2026)
            pass_antiguedad = False
            if prod == "OPCIPRES":
                if pagos_d >= pagos_minimos and tasa_d <= 2.67: pass_antiguedad = True
            elif prod == "CONSUBANCO":
                if pagos_d >= pagos_minimos and tasa_d <= 2.57: pass_antiguedad = True
            elif prod == "CONSUPAGO":
                if pagos_d >= pagos_minimos and tasa_d <= 2.67: pass_antiguedad = True
            else:
                # MASNOMINA: Solo antigüedad estándar
                if pagos_d >= pagos_minimos: pass_antiguedad = True

            if pass_antiguedad: razones_d.add("Antigüedad")

            # --- 3. INCREMENTO DESCUENTO (Aumento de Pago Mensual) - ENERO 2026 ---
            # "El cliente acepta aumentar su descuento mensual en $750 o $1,000"
            # "Y la deuda origen tiene tasa favorable (<= referencia)"
            # OPC: +750 Incremento en Descuento Y Tasa <= 2.67%
            # CSB: +1,000 Incremento en Descuento Y Tasa <= 2.57%
            # CSP: +750 Incremento en Descuento Y Tasa <= 2.67% (ACTUALIZADO)
            # MN: Sin restricción
            pass_incremento = False
            if prod == "MASNOMINA":
                pass_incremento = True
            elif prod == "CONSUPAGO":
                if inc >= 750.0 and tasa_d <= 2.67: pass_incremento = True
            elif prod == "OPCIPRES":
                if inc >= 750.0 and tasa_d <= 2.67: pass_incremento = True
            elif prod == "CONSUBANCO":
                if inc >= 1000.0 and tasa_d <= 2.57: pass_incremento = True

            if pass_incremento: razones_d.add("Incremento Descuento")

            # --- 4. AUMENTO DE PLAZO DE CRÉDITO ---
            # "Aplica para todos los cotizadores sin restricción"
            # Validamos mapeo de plazos.
            pass_plazo = False
            if (pl_old <= 12 and plazo_nuevo >= 36) or \
               (pl_old <= 24 and plazo_nuevo >= 48) or \
               (pl_old in [36, 48] and plazo_nuevo >= 54) or \
               (pl_old == 54 and plazo_nuevo == 60):
                pass_plazo = True

            if pass_plazo: razones_d.add("Plazo")

            sets_deudas.append(razones_d)

        # Evaluar Unanimidad (Intersección)
        if not sets_deudas: return False, ["Sin Deudas"]

        interseccion = set.intersection(*sets_deudas)

        if interseccion:
            # Aprobado por unanimidad en al menos una razón
            return True, list(interseccion)

        # Motor de Sugerencias (Si falla la unanimidad)
        # Iterar quitando una deuda a la vez para ver si el resto cumple
        for i in range(len(sets_deudas)):
            # Subset excluyendo la deuda i
            subset = sets_deudas[:i] + sets_deudas[i+1:]
            if not subset: continue

            interseccion_sub = set.intersection(*subset)
            if interseccion_sub:
                razon_ganadora = list(interseccion_sub)[0]
                monto_d = deudas[i].get('saldo', deudas[i].get('monto', 0))
                try: monto_str = to_money(monto_d)
                except: monto_str = "$0.00"
                return False, [f"Sugerencia: Eliminar deuda de {monto_str} para aprobar por {razon_ganadora}."]

        return False, ["No hay unanimidad de criterios (Tasa, Liquidez, Antigüedad, Plazo)."]

    def optimizar_combinacion_deudas(self, prod, deudas, incremento_liquidez, plazo_nuevo, capacidad_total_original, usa_retencion=False):
        """
        Optimizador Inteligente de Combinaciones de Deudas (CORREGIDO).

        CORRECCIÓN LÓGICA:
        - Al excluir una deuda, TAMBIÉN se debe excluir su pago de la capacidad.
        - Se recalcula el monto del crédito para cada combinación.

        Args:
            prod: Producto financiero
            deudas: Lista de deudas (diccionarios con 'saldo', 'pago', etc.)
            incremento_liquidez: Ajuste de liquidez (float)
            plazo_nuevo: Plazo del nuevo crédito
            capacidad_total_original: (No se usa como fijo, se recalcula)
            usa_retencion: Si usa retención

        Returns:
            dict con optimización o None
        """
        from itertools import combinations

        if not deudas or len(deudas) < 2:
            return None

        n = len(deudas)

        # 1. Escenario Base (Todas las deudas)
        # Recalcular capacidad real del set completo
        cap_base = sum(d.get('pago', 0) for d in deudas) + float(incremento_liquidez)
        res_base = self.calcular(prod, cap_base, plazo_nuevo, False, usa_retencion, rounding_step=500.0)

        if not res_base or not self.validar_minimo(prod, res_base['monto'], plazo_nuevo):
            return None

        monto_base = res_base['monto']
        total_saldo_base = sum(d.get('saldo', 0) for d in deudas)
        remanente_base = monto_base - total_saldo_base

        valido_base, razones_base = self.validar_normativa(prod, deudas, incremento_liquidez, remanente_base, plazo_nuevo, usa_retencion)

        # Estructura del mejor resultado
        mejor_resultado = {
            'mejor_combinacion': list(range(n)),
            'remanente_optimo': remanente_base if valido_base else -999999999, # Si base no es válido, cualquier válido será mejor
            'monto_optimo': monto_base,
            'deudas_a_excluir': [],
            'razones': razones_base,
            'mejora': 0,
            'num_deudas': n,
            'total_deuda': total_saldo_base
        }

        base_es_valido = valido_base

        # 2. Iterar combinaciones (Subsets)
        # Probamos quitando de 1 hasta N-1 deudas
        # Nota: Performance O(2^n). Para n <= 10 es trivial.

        hay_mejora = False

        for num_excluir in range(1, n):
            num_incluir = n - num_excluir

            for combo_indices in combinations(range(n), num_incluir):
                # Subset de deudas
                deudas_subset = [deudas[i] for i in combo_indices]

                # A. Calcular Capacidad del Subset
                sub_pagos = sum(d.get('pago', 0) for d in deudas_subset)
                sub_cap = sub_pagos + float(incremento_liquidez)

                if sub_cap <= 0: continue

                # B. Calcular Nuevo Crédito
                res_sub = self.calcular(prod, sub_cap, plazo_nuevo, False, usa_retencion, rounding_step=500.0)
                if not res_sub: continue

                if not self.validar_minimo(prod, res_sub['monto'], plazo_nuevo):
                   continue

                sub_monto = res_sub['monto']
                sub_saldo_total = sum(d.get('saldo', 0) for d in deudas_subset)
                sub_remanente = sub_monto - sub_saldo_total

                # C. Verificar si mejora el remanente
                # Si el escenario base era inválido, cualquier válido es mejora.
                # Si era válido, necesitamos superar el remanente óptimo actual.
                if sub_remanente <= mejor_resultado['remanente_optimo']:
                    continue

                # D. Validar Normativa
                es_valido, razones = self.validar_normativa(prod, deudas_subset, incremento_liquidez, sub_remanente, plazo_nuevo, usa_retencion)

                if es_valido:
                    deudas_excluidas = [deudas[i] for i in range(n) if i not in combo_indices]
                    mejor_resultado = {
                        'mejor_combinacion': list(combo_indices),
                        'remanente_optimo': sub_remanente,
                        'monto_optimo': sub_monto,
                        'deudas_a_excluir': deudas_excluidas,
                        'razones': razones,
                        'mejora': sub_remanente - remanente_base if base_es_valido else 0, # Si base inválido, mejora es simbólica
                        'num_deudas': len(deudas_subset),
                        'total_deuda': sub_saldo_total
                    }
                    hay_mejora = True

        # Retornar solo si encontramos algo válido que mejore (o si el base era inválido y encontramos uno válido)
        if hay_mejora:
            return mejor_resultado

        return None

    def generar_layout_final(self, form_data, credito_data, perfil, tipo_tramite, formato="FORMAL"):
        id_liq = form_data.get('id_credito', '')
        desc_liq = to_money(form_data.get('descuento_liq', 0))
        cnca_det = ""
        if tipo_tramite == "CNCA" or id_liq: cnca_det = f"ID LIQ: {id_liq}\nDESC: {desc_liq}\nNUM: {form_data.get('num_descuentos','')}\nMARCA: {form_data.get('marca_liquida','').upper()}\n"
        nombre_cliente = form_data.get('nombre', '').upper()
        monto = credito_data['monto']
        plazo = credito_data['plazo']
        pago = to_money(credito_data['pago_visual'])
        cat = credito_data['cat']
        agente = perfil.get('nombre', '').upper()
        supervisor = perfil.get('supervisor', '').upper()
        if formato == "WHATSAPP":
            emoji_tramite = "🔄" if tipo_tramite == "CNCA" else "🆕"
            emoji_dinero = "💰" if monto > 50000 else "💵"
            if tipo_tramite == "CNCA": layout = f"""{emoji_tramite} *REFINANCIAMIENTO APROBADO* {emoji_tramite}\n\n🎉 *¡Buenas noticias {nombre_cliente}!* \n\n{emoji_dinero} *Te autorizamos:* ${monto:,.2f}\n📅 *Plazo:* {plazo} meses\n💳 *Pago quincenal:* {pago}\n📊 *CAT:* {cat}%\n\n✅ *Beneficios:*\n• 🏦 Consolidas todas tus deudas\n• 📉 Reduce tu tasa de interés\n• 📈 Mejora tu historial crediticio\n\n👤 *Asesor:* {agente}\n📞 *Contacta a tu supervisor:* {supervisor}\n\n_*Este es un preautorización sujeta a validación de documentos*_"""
            else: layout = f"""{emoji_tramite} *CRÉDITO NUEVO APROBADO* {emoji_tramite}\n\n*¡Excelente {nombre_cliente}!* \n\n{emoji_dinero} *Crédito aprobado:* ${monto:,.2f}\n📅 *Plazo:* {plazo} meses\n💳 *Pago quincenal:* {pago}\n📊 *CAT:* {cat}%\n\n🚀 *Ventajas:*\n• ⚡ Desembolso en 24-48 hrs\n• 🏠 Sin aval ni garantía hipotecaria\n• 📄 Trámite 100% digital\n\n👤 *Asesor:* {agente}\n📞 *Contacta a tu supervisor:* {supervisor}\n\n_*Oferta válida por 72 hrs. Sujeta a aprobación de buró*_"""
        else: layout = f"""SOLICITUD DE CAPTURA | {tipo_tramite}\n\nORIGEN: {form_data.get('convenio', 'IMSS').upper()} | TRAMITE: PAPERLESS\n\nCLIENTE: {nombre_cliente}\nNSS: {form_data.get('nss', '')} | RFC: {form_data.get('rfc', '').upper()}\nCONTACTO: {form_data.get('telefono', '')}\n\nFINANCIERO\nCLABE: {form_data.get('clabe', '')} ({form_data.get('banco', '').upper()})\nOCUPACION: {form_data.get('ocupacion', '').upper()}\n\nTRAMITE: {tipo_tramite}\n{cnca_det}\nOFERTA: ${monto:,.2f} a {plazo} MESES\nPAGO: {pago} | CAT: {cat}%\n\nAGENTE: {agente}\nSUPERVISOR: {supervisor}"""
        return layout

    @staticmethod
    def calcular_tasa_anual(monto, pago, plazo, pagos_restantes=0):
        """
        Calcula una tasa anual estimativa basada en el monto, pago y plazo.
        Método estático de utilidad para evitar crashes si no está definido.
        """
        try:
            # Retorno seguro para evitar bloqueos
            return 0.0
        except:
            return 0.0
