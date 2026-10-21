package com.example.consucalcu.data

import com.example.consucalcu.model.TipoPension
import com.example.consucalcu.model.InsuranceDictamenResult
import java.math.BigDecimal

object NormativaIMSS {

    fun evaluarDictamenSeguro(
        producto: String,
        montoSolicitado: Double,
        plazo: Int,
        sexo: String,
        edad: Int,
        tipoPension: TipoPension,
        tipoCliente: String,
        dictamen: String,
        pagosSostenidos: Int,
        antiguedadMeses: Int
    ): InsuranceDictamenResult {
        // Validaciones iniciales: Nunca continuar si falta información
        if (producto.isBlank()) {
            return InsuranceDictamenResult(false, "", plazo, 0.0, 0.0, "", 0.0, "Rechazo: Falta especificar el producto.")
        }
        if (montoSolicitado <= 0) {
            return InsuranceDictamenResult(false, producto, plazo, 0.0, 0.0, "", 0.0, "Rechazo: El monto solicitado debe ser mayor a 0.")
        }
        if (plazo <= 0) {
            return InsuranceDictamenResult(false, producto, plazo, 0.0, 0.0, "", 0.0, "Rechazo: El plazo debe ser mayor a 0.")
        }
        if (edad <= 0) {
            return InsuranceDictamenResult(false, producto, plazo, 0.0, 0.0, "", 0.0, "Rechazo: La edad del cliente debe ser mayor a 0.")
        }
        if (sexo.isBlank()) {
            return InsuranceDictamenResult(false, producto, plazo, 0.0, 0.0, "", 0.0, "Rechazo: Falta especificar el sexo del cliente.")
        }
        if (tipoCliente.isBlank()) {
            return InsuranceDictamenResult(false, producto, plazo, 0.0, 0.0, "", 0.0, "Rechazo: Falta especificar el tipo de cliente.")
        }

        // Paso 1: Determinar si el Addendum aplica
        val isMale = sexo.equals("Hombre", ignoreCase = true)
        val aplicaAddendum = isMale && (
            (tipoPension == TipoPension.INVALIDEZ && edad in 51..70) ||
            (tipoPension == TipoPension.VEJEZ && edad in 66..75)
        )

        var tipoSeguro = "PRIMER_ANIO"
        var aprobado = true
        var motivo = ""

        if (!aplicaAddendum) {
            motivo = "Cliente fuera del rango del Addendum. El seguro aplica únicamente por el primer año por defecto."
        } else {
            // Paso 2: Si aplica Addendum, evaluar tipo de cliente
            val cleanCliente = tipoCliente.lowercase().trim()
            when {
                cleanCliente.contains("nuevo") || cleanCliente.contains("segunda") || cleanCliente.contains("disposición") -> {
                    // Nuevo o Segunda Disposición: Debe existir dictamen
                    if (dictamen.isBlank() || dictamen.equals("No Aplica", ignoreCase = true)) {
                        aprobado = false
                        motivo = "Rechazo: Falta el resultado del dictamen para cliente nuevo dentro del Addendum."
                    } else {
                        motivo = "Cliente dentro del Addendum con dictamen aprobado ($dictamen). Seguro del primer año administrativo."
                    }
                }
                cleanCliente.contains("renovación") || cleanCliente.contains("renovacion") -> {
                    // Renovación: Debe cumplir 3 pagos completos
                    if (pagosSostenidos < 3) {
                        aprobado = false
                        motivo = "Rechazo: Cliente en renovación dentro del Addendum requiere al menos 3 pagos sostenidos (vía nómina)."
                    } else {
                        motivo = "Cliente en renovación dentro del Addendum con $pagosSostenidos pagos sostenidos. Seguro del primer año."
                    }
                }
                cleanCliente.contains("compra") || cleanCliente.contains("deuda") -> {
                    // Compra de deuda: Debe cumplir 12 pagos
                    if (pagosSostenidos < 12) {
                        aprobado = false
                        motivo = "Rechazo: Compra de deuda dentro del Addendum requiere al menos 12 pagos sostenidos."
                    } else {
                        motivo = "Compra de deuda dentro del Addendum con $pagosSostenidos pagos sostenidos. Seguro del primer año."
                    }
                }
                else -> {
                    motivo = "Tipo de cliente desconocido. Seguro asignado por el primer año."
                }
            }
        }

        // Paso 3: Edad Máxima
        if (aprobado) {
            val plazoAnios = plazo / 12.0
            val edadFinal = edad + plazoAnios

            if (edadFinal > 85.0) {
                aprobado = false
                motivo = "Rechazo: La edad final acumulada ($edadFinal años) excede el límite máximo de 85 años."
            } else if (edadFinal > 80.0) {
                if (tipoSeguro != "PRIMER_ANIO") {
                    aprobado = false
                    motivo = "Rechazo: La edad final acumulada ($edadFinal años) excede 80 años y solo se permite hasta 85 años si el seguro se cobra únicamente por el primer año."
                } else {
                    motivo = "Edad final acumulada ($edadFinal años) mayor a 80 años. Se aprueba cobrando seguro únicamente por el primer año."
                }
            } else {
                if ((tipoPension == TipoPension.INVALIDEZ || tipoPension == TipoPension.VEJEZ) && edadFinal > 75.0) {
                    tipoSeguro = "PLAZO_COMPLETO"
                    motivo = "Edad final acumulada ($edadFinal años) mayor a 75 años. Se aprueba cobrando el seguro por TODO EL PLAZO."
                }
            }
        }

        // Paso 4: Tasa de Interés Mensual
        val tasaMensual = getTasaMensual(producto, plazo)

        // Paso 5: Factor de Seguro
        val factorSeguro = getFactorSeguro(producto)

        return InsuranceDictamenResult(
            aprobado = aprobado,
            producto = producto,
            plazo = plazo,
            tasaMensual = tasaMensual,
            factorSeguro = factorSeguro,
            tipoSeguro = if (aprobado) tipoSeguro else "",
            montoSeguro = 0.0, // Se calculará dinámicamente con la amortización
            motivo = motivo
        )
    }

    fun getTasaMensual(producto: String, plazo: Int): Double {
        val prodClean = producto.uppercase().trim()
        return when {
            prodClean == "OPC MAX" -> 2.26
            prodClean == "OPC ESP" -> 2.14
            prodClean == "OPC" -> when (plazo) {
                12 -> 2.66
                24 -> 2.65
                36 -> 2.64
                48 -> 2.63
                54 -> 2.62
                60 -> 2.61
                else -> Double.NaN
            }
            prodClean == "CSB VIP" -> 1.99
            prodClean == "CSB ESP" -> 2.07
            prodClean == "CSB" -> when (plazo) {
                12 -> 2.57
                24 -> 2.55
                36 -> 2.50
                48 -> 2.43
                54 -> 2.43
                60 -> 2.40
                else -> Double.NaN
            }
            else -> Double.NaN
        }
    }

    fun getFactorSeguro(producto: String): Double {
        val prodClean = producto.uppercase().trim()
        return when {
            prodClean == "OPC MAX" || prodClean == "OPC ESP" -> 1.99
            prodClean == "OPC" -> 1.7571
            prodClean == "CSB ESP" -> 1.99
            prodClean == "CSB" || prodClean == "CSB VIP" -> 1.7571
            else -> Double.NaN
        }
    }
}
