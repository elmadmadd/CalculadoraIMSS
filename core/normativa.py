"""
NORMATIVA_CNCA.py
Validaciones de normativa CNCA para refinanciamiento y crédito nuevo.
Implementa todas las reglas de negocio para tasas tradicionales y de retención.
"""

class NormativaCNCA:
    """
    Clase que centraliza todas las validaciones de normativa CNCA.
    """
    
    # Mapeo de códigos de producto
    PRODUCTOS = {
        "MASNOMINA": "MSN",
        "CONSUPAGO": "CSP",
        "OPCIPRES": "OPC",
        "CONSUBANCO": "CSB"
    }
    
    # Tasas de referencia mensuales (%)
    TASAS_REFERENCIA = {
        "OPCIPRES": 2.67,
        "CONSUBANCO": 2.57,  # Para incremento de tasa
        "CONSUBANCO_BAJA": 2.40  # Para dar de baja
    }
    
    def _to_float(self, val):
        """Convierte valor a float de forma segura."""
        try:
            return float(val)
        except (ValueError, TypeError):
            return 0.0

    def __init__(self):
        pass
    
    # ==========================================
    # VALIDACIONES TASA TRADICIONAL - CNCA
    # ==========================================
    
    def validar_incremento_tasa(self, producto_nuevo, deudas):
        """
        Valida si se puede hacer incremento de tasa según producto.
        
        Args:
            producto_nuevo: Producto con el que se va a refinanciar
            deudas: Lista de deudas a dar de baja
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        # MASNOMINA y CONSUPAGO: sin tasa de referencia, sin restricciones
        if producto_nuevo in ["MASNOMINA", "CONSUPAGO"]:
            return True, ""
        
        # OPCIPRES: cartera a dar de baja debe tener tasa <= 2.57% mensual
        if producto_nuevo == "OPCIPRES":
            for deuda in deudas:
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                if tasa_mensual > 2.57:
                    return False, f"OPCIPRES requiere que todas las deudas tengan tasa ≤ 2.57% mensual"
            return True, ""
        
        # CONSUBANCO: cartera a dar de baja debe tener tasa <= 2.40% mensual
        if producto_nuevo == "CONSUBANCO":
            for deuda in deudas:
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                if tasa_mensual > 2.40:
                    return False, f"CONSUBANCO requiere que todas las deudas tengan tasa ≤ 2.40% mensual"
            return True, ""
        
        return True, ""
    
    def validar_pagos_acumulados_tasa_ref(self, producto_nuevo, deudas, remanente):
        """
        Valida pagos acumulados y tasa de referencia según producto.
        
        Args:
            producto_nuevo: Producto con el que se va a refinanciar
            deudas: Lista de deudas a dar de baja
            remanente: Remanente positivo disponible
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        remanente = self._to_float(remanente)
        
        # CONSUPAGO y MASNOMINA: sin restricciones si hay remanente positivo
        if producto_nuevo in ["CONSUPAGO", "MASNOMINA"]:
            if remanente > 0:
                return True, ""
            else:
                return False, "Requiere remanente positivo"
        
        # OPCIPRES: 16+ pagos acumulados, tasa <= 2.67% mensual
        if producto_nuevo == "OPCIPRES":
            for deuda in deudas:
                pagos = self._to_float(deuda.get("pagos_aplicados") or deuda.get("pagos", 0))
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                
                if pagos < 16:
                    return False, "OPCIPRES requiere 16+ pagos acumulados en todas las deudas"
                if tasa_mensual > 2.67:
                    return False, "OPCIPRES requiere tasa ≤ 2.67% mensual en todas las deudas"
            
            if remanente <= 0:
                return False, "Requiere remanente positivo"
            
            return True, ""
        
        # CONSUBANCO: 16+ pagos acumulados, tasa <= 2.57% mensual
        if producto_nuevo == "CONSUBANCO":
            for deuda in deudas:
                pagos = self._to_float(deuda.get("pagos_aplicados") or deuda.get("pagos", 0))
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                
                if pagos < 16:
                    return False, "CONSUBANCO requiere 16+ pagos acumulados en todas las deudas"
                if tasa_mensual > 2.57:
                    return False, "CONSUBANCO requiere tasa ≤ 2.57% mensual en todas las deudas"
            
            if remanente <= 0:
                return False, "Requiere remanente positivo"
            
            return True, ""
        
        return True, ""
    
    def validar_incremento_descuento_mensual(self, producto_nuevo, deudas, incremento_pago, remanente):
        """
        Valida incremento de descuento mensual según producto.
        
        Args:
            producto_nuevo: Producto con el que se va a refinanciar
            deudas: Lista de deudas a dar de baja
            incremento_pago: Incremento en el pago mensual
            remanente: Remanente disponible
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        remanente = self._to_float(remanente)
        incremento_pago = self._to_float(incremento_pago)

        # CONSUPAGO y MASNOMINA: sin restricción si hay remanente
        if producto_nuevo in ["CONSUPAGO", "MASNOMINA"]:
            if remanente > 0:
                return True, ""
            else:
                return False, "Requiere remanente positivo"
        
        # OPCIPRES: $750 de incremento, tasa <= 2.67% mensual
        if producto_nuevo == "OPCIPRES":
            if incremento_pago < 750:
                return False, "OPCIPRES requiere incremento mínimo de $750 en el pago"
            
            for deuda in deudas:
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                if tasa_mensual > 2.67:
                    return False, "OPCIPRES requiere tasa ≤ 2.67% mensual en todas las deudas"
            
            return True, ""
        
        # CONSUBANCO: $1000 de incremento, tasa <= 2.57% mensual
        if producto_nuevo == "CONSUBANCO":
            if incremento_pago < 1000:
                return False, "CONSUBANCO requiere incremento mínimo de $1,000 en el pago"
            
            for deuda in deudas:
                tasa_mensual = self._to_float(deuda.get("tasa_mensual") or deuda.get("tasa", 0))
                if tasa_mensual > 2.57:
                    return False, "CONSUBANCO requiere tasa ≤ 2.57% mensual en todas las deudas"
            
            return True, ""
        
        return True, ""
    
    def validar_aumento_plazo(self, plazo_actual, plazo_nuevo):
        """
        Valida aumento de plazo de crédito.
        
        Args:
            plazo_actual: Plazo actual de la deuda a dar de baja
            plazo_nuevo: Plazo del nuevo crédito
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        reglas = {12: 36, 24: 48, 36: 54, 48: 54, 54: 60}
        plazo_actual = int(self._to_float(plazo_actual))
        plazo_nuevo = int(self._to_float(plazo_nuevo))
        
        min_req = reglas.get(plazo_actual)
        
        if min_req is None:
            # Plazo actual no está en las reglas
            return True, ""
        
        if plazo_nuevo < min_req:
            return False, f"Plazo actual {plazo_actual}m requiere nuevo plazo {min_req}m+"
        
        return True, ""
    
    # ==========================================
    # VALIDACIONES TASA DE RETENCIÓN - CNCA
    # ==========================================
    
    def validar_retencion_cnca(self, producto, monto_bruto, deudas):
        """
        Valida reglas de retención para CNCA (refinanciamiento).
        Solo aplica para plazo 60 meses.
        
        Args:
            producto: Producto financiero
            monto_bruto: Monto bruto del crédito (deudas + liquidez)
            deudas: Lista de deudas a refinanciar
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        monto_bruto = self._to_float(monto_bruto)
        reglas = {
            "CONSUBANCO": {"pagos_min": 24, "monto_bruto_min": 100000},
            "CONSUPAGO": {"pagos_min": 24, "monto_bruto_min": 50000},
            "OPCIPRES": {"pagos_min": 24, "monto_bruto_min": 75000}
        }
        
        if producto not in reglas:
            return False, f"Producto {producto} no tiene reglas de retención CNCA"
        
        regla = reglas[producto]
        
        # Validar monto bruto mínimo
        if monto_bruto < regla["monto_bruto_min"]:
            return False, f"Monto bruto mínimo: ${regla['monto_bruto_min']:,.0f}"
        
        # Validar pagos acumulados en TODAS las deudas
        for deuda in deudas:
            pagos = self._to_float(deuda.get("pagos_aplicados") or deuda.get("pagos", 0))
            if pagos < regla["pagos_min"]:
                return False, f"Requiere {regla['pagos_min']}+ pagos acumulados en todas las deudas"
        
        return True, ""
    
    def validar_retencion_nuevo(self, producto, monto_neto, plazo):
        """
        Valida reglas de retención para crédito NUEVO.
        Solo aplica para plazo 60 meses.
        
        Args:
            producto: Producto financiero
            monto_neto: Monto neto del crédito
            plazo: Plazo del crédito
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        # Solo aplica para 60 meses
        if plazo != 60:
            return False, "Tasa de retención solo aplica para plazo de 60 meses"
        
        reglas = {
            "CONSUBANCO": 200000,
            "CONSUPAGO": 100000,
            "OPCIPRES": 125000
        }
        
        if producto not in reglas:
            return False, f"Producto {producto} no tiene reglas de retención para crédito nuevo"
        
        monto_min = reglas[producto]
        
        if monto_neto < monto_min:
            return False, f"Monto mínimo para retención: ${monto_min:,.0f}"
        
        return True, ""
    
    # ==========================================
    # REGLAS DE LIQUIDACIÓN DE PRODUCTOS
    # ==========================================
    
    def obtener_productos_permitidos_liquidacion(self, deudas):
        """
        Determina qué productos se pueden usar para liquidar una combinación de deudas.
        
        Args:
            deudas: Lista de deudas con su producto
            
        Returns:
            list: Lista de productos permitidos para liquidar
        """
        # Extraer códigos de productos únicos
        productos_deuda = set()
        for deuda in deudas:
            prod = deuda.get("producto", "")
            codigo = self.PRODUCTOS.get(prod, prod)
            productos_deuda.add(codigo)
        
        productos_permitidos = []
        
        # Regla 1: MSN se puede liquidar con MSN
        if productos_deuda == {"MSN"}:
            productos_permitidos.append("MASNOMINA")
        
        # Regla 2: Si la combinación solo tiene CSB/OPC/CSP se puede liquidar con MSN
        if productos_deuda.issubset({"CSB", "OPC", "CSP"}):
            productos_permitidos.append("MASNOMINA")
        
        # Regla 3: CSB/MSN/OPC se puede liquidar con CSP
        if productos_deuda.issubset({"CSB", "MSN", "OPC"}):
            productos_permitidos.append("CONSUPAGO")
        
        # Regla 4: Si todos los créditos son CSP se puede liquidar con CSP
        if productos_deuda == {"CSP"}:
            productos_permitidos.append("CONSUPAGO")
        
        # Regla 5: MSN/CSB/CSP se puede liquidar con OPC
        if productos_deuda.issubset({"MSN", "CSB", "CSP"}):
            productos_permitidos.append("OPCIPRES")
        
        # Regla 6: Si todos los créditos son OPC se puede liquidar con OPC
        if productos_deuda == {"OPC"}:
            productos_permitidos.append("OPCIPRES")
        
        # Regla 7: MSN/OPC/CSP se puede liquidar con CSB
        if productos_deuda.issubset({"MSN", "OPC", "CSP"}):
            productos_permitidos.append("CONSUBANCO")
        
        # Regla 8: Si todos los créditos son CSB se puede liquidar con CSB
        if productos_deuda == {"CSB"}:
            productos_permitidos.append("CONSUBANCO")
        
        # Eliminar duplicados manteniendo orden
        return list(dict.fromkeys(productos_permitidos))
    
    def validar_producto_liquidacion(self, producto_nuevo, deudas):
        """
        Valida si un producto específico puede liquidar las deudas dadas.
        
        Args:
            producto_nuevo: Producto con el que se quiere liquidar
            deudas: Lista de deudas a liquidar
            
        Returns:
            (bool, str): (es_valido, mensaje_error)
        """
        productos_permitidos = self.obtener_productos_permitidos_liquidacion(deudas)
        
        if producto_nuevo in productos_permitidos:
            return True, ""
        else:
            return False, f"El producto {producto_nuevo} no puede liquidar esta combinación de deudas. Productos permitidos: {', '.join(productos_permitidos)}"
