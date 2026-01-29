
import math

# Configurar precisión decimal alta para cálculos internos (si se requiere)
# Para propósitos generales de float estándar de Python, esto no afecta directamente a menos que usemos Decimal,
# pero mantenemos la lógica original.

class CalculadoraFinanciera:
    @staticmethod
    def calcular_tasa_anual(saldo, pago, plazo_o_pagos, pagos_restantes=None):
        """
        Calcula tasa anual estimada usando Newton-Raphson.
        Si pagos_restantes no se proporciona, usa plazo_o_pagos como pagos.
        """
        try:
            saldo = float(saldo)
            pago = float(pago)
            pagos_restantes = int(pagos_restantes) if pagos_restantes is not None else int(plazo_o_pagos)
            
            if saldo <= 0 or pago <= 0 or pagos_restantes <= 0:
                print("Valores inválidos para cálculo de tasa")
                return 0.0

            # Verificación básica de viabilidad
            monto_total_pagos = pago * pagos_restantes
            if monto_total_pagos <= saldo:
                print("El total de pagos es menor o igual al saldo. Tasa negativa o cero.")
                return 0.0

            # Intentar Newton-Raphson primero
            # f(r) = P * (1 - (1+r)^-n) / r - PV = 0
            # P = pago quincenal, n = pagos restantes, PV = saldo actual
            # Buscamos r (tasa quincenal)
            
            # Estimación inicial sensata: Tasa Anual 30% -> Quincenal ~1.25%
            r = 0.0125 
            max_iter = 50
            tol = 1e-6

            def f(r):
                if r == 0: return pago * pagos_restantes - saldo
                return pago * (1 - (1 + r) ** -pagos_restantes) / r - saldo

            def f_prime(r):
                if r == 0: return 0 # Indefinido
                term1 = (1 + r) ** (-pagos_restantes - 1)
                num = pago * (pagos_restantes * r * term1 - (1 - (1 + r) ** -pagos_restantes))
                den = r * r
                return num / den

            for i in range(max_iter):
                y = f(r)
                if abs(y) < tol:
                    # Convertir tasa quincenal efectiva a anual nominal (aprox * 24)
                    # O anual efectiva. Bancos suelen usar nominal para contratos simples.
                    return r * 24 * 100
                
                deriv = f_prime(r)
                if deriv == 0:
                    break
                
                r_new = r - y / deriv
                if r_new <= -1: # Tasa no puede ser <= -100%
                    r_new = 1e-5
                
                r = r_new
            
            # Si Newton falla, intentar bisección como fallback robusto
            # Rango razonable: 0% a 200% anual -> 0 a ~0.1 quincenal
            low = 0.0
            high = 0.5 # 50% quincenal es gigante
            
            for i in range(100):
                mid = (low + high) / 2
                if mid == 0: val = pago * pagos_restantes - saldo
                else: val = pago * (1 - (1 + mid) ** -pagos_restantes) / mid - saldo
                
                if abs(val) < tol:
                    return mid * 24 * 100
                
                if val > 0: # Necesitamos tasa más alta para reducir el valor presente a saldo
                    low = mid
                else:
                    high = mid
            
            return (low + high) / 2 * 24 * 100

        except Exception as e:
            print(f"Error calculando tasa: {e}")
            return 0.0

    def verificar_formula_consistencia(self, saldo, pago, pagos_restantes, tasa_calculada):
        """
        Verifica si la tasa calculada regenera el saldo con precisión aceptable.
        """
        try:
            if tasa_calculada <= 0: return False
            r_quincenal = (tasa_calculada / 100) / 24
            if r_quincenal == 0: return False

            present_value = pago * (1 - (1 + r_quincenal) ** -pagos_restantes) / r_quincenal
            diff = abs(present_value - saldo)
            
            # Tolerancia de $100 pesos o 1%
            return diff < 100 or (diff / saldo) < 0.01
        except:
            return False

    def generar_tabla_amortizacion(self, monto: float, tasa_anual: float, plazo: int, pago_periodico: float):
        """Genera datos para tabla de amortización."""
        tabla = []
        saldo = monto
        tasa_periodo = (tasa_anual / 100) / 24
        
        # Ajuste de pago para cerrar en 0 exacto o usar pago real
        # Si usamos pago proporcionado, el último periodo tendrá ajuste
        
        total_interes = 0
        total_iva = 0
        total_capital = 0  # Suma de abonos a capital
        
        for i in range(1, plazo + 1):
            periodo = i
            
            # Interés de este periodo sobre saldo insoluto
            interes = saldo * tasa_periodo
            iva = interes * 0.16
            
            # Abono a capital
            capital = pago_periodico - interes - iva
            
            # Manejo de último pago o capital negativo
            if i == plazo:
                # Ajuste final para saldar
                # Puede que el pago final sea diferente si queremos cerrar exacto
                # Pero en tabla informativa mostramos la proyección con pago cte
                if saldo > pago_periodico:
                   # Caso atípico: deuda no se paga
                   pass
                else:
                    # Último pago real para cerrar
                    # capital = saldo
                    # pago_real = capital + interes + iva
                    pass
            
            saldo_final = saldo - capital
            
            # Evitar saldos negativos por redondeo en display, aunque matemáticamente correctos
            if saldo_final < 0: saldo_final = 0
            
            if capital < 0: 
                # Amortización negativa (pago no cubre intereses)
                pass

            item = {
                "no": periodo,
                "saldo_inicial": saldo,
                "pago": pago_periodico,
                "interes": interes,
                "iva": iva,
                "capital": capital,
                "saldo_final": saldo_final
            }
            tabla.append(item)
            
            total_interes += interes
            total_iva += iva
            total_capital += capital
            
            saldo = saldo_final
            if saldo <= 0.01:
                break
                
        return tabla, {"interes": total_interes, "iva": total_iva, "capital": total_capital}
