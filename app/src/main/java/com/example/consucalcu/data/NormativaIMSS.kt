package com.example.consucalcu.data

import com.example.consucalcu.model.InsuranceAmortizationRow
import com.example.consucalcu.model.InsuranceDictamenResult
import com.example.consucalcu.model.InsuranceSchedule
import com.example.consucalcu.model.TipoPension
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

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

    // Cálculo de Seguro CSB V.18 y OPC V.09 (02.11.2026). Tasas sin IVA exactas:
    // la tasa de la tarjeta es este valor redondeado.
    fun getTasaMensual(producto: String, plazo: Int): Double {
        val prodClean = producto.uppercase().trim()
        return when {
            prodClean == "OPC MAX" -> 2.30
            prodClean == "OPC ESP" -> 2.20
            prodClean == "OPC" -> when (plazo) {
                12, 24, 36, 48, 54, 60 -> 2.6578
                else -> Double.NaN
            }
            prodClean == "CSB VIP" -> 1.99
            prodClean == "CSB ESP" -> 2.10
            prodClean == "CSB" -> when (plazo) {
                12, 24 -> 2.57
                36, 48 -> 2.50
                54, 60 -> 2.445
                else -> Double.NaN
            }
            else -> Double.NaN
        }
    }

    fun getFactorSeguro(producto: String): Double {
        val prodClean = producto.uppercase().trim()
        return when (prodClean) {
            "OPC", "OPC MAX", "OPC ESP", "CSB", "CSB ESP", "CSB VIP" -> 1.7571
            else -> Double.NaN
        }
    }

    /**
     * Reproduce la hoja del producto: PMT con la tasa con IVA y una tabla donde el
     * seguro mensual es el valor asegurable (saldo + interés + IVA) por factor / 1000.
     * Usa su propio PMT, que no es el descuento del cotizador.
     *
     * Dos hojas se apartan del resto y se replican tal cual: OPC a 36 meses redondea
     * el PMT a pesos, y CSB VIP no redondea ningún valor intermedio.
     */
    fun calcularTablaSeguro(producto: String, monto: BigDecimal, plazo: Int): InsuranceSchedule {
        val prodClean = producto.uppercase().trim()
        val sinRedondeo = prodClean == "CSB VIP"
        val centavos: (BigDecimal) -> BigDecimal = { valor ->
            if (sinRedondeo) valor.round(MC) else valor.setScale(2, RoundingMode.HALF_UP)
        }

        val tasa = BigDecimal(getTasaMensual(producto, plazo).toString()).divide(BD_100, MC)
        val factor = BigDecimal(getFactorSeguro(producto).toString())
        val tasaConIva = tasa.multiply(UNO_MAS_IVA, MC)
        val descuento = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(BigDecimal.ONE.add(tasaConIva).pow(plazo, MC), MC),
            MC
        )
        val pmtExacto = monto.multiply(tasaConIva, MC).divide(descuento, MC)
        val pmt = if (prodClean == "OPC" && plazo == 36) {
            pmtExacto.setScale(0, RoundingMode.HALF_UP)
        } else {
            centavos(pmtExacto)
        }

        var saldo = centavos(monto)
        val rows = (1..plazo).map { mes ->
            val interes = centavos(saldo.multiply(tasa, MC))
            val iva = centavos(interes.multiply(IVA, MC))
            val capital = centavos(pmt.subtract(interes).subtract(iva))
            val saldoFinal = centavos(saldo.subtract(capital))
            val valorAsegurable = centavos(saldo.add(interes).add(iva))
            val seguro = centavos(valorAsegurable.multiply(factor, MC).divide(BD_1000, MC))
            InsuranceAmortizationRow(mes, saldo, interes, iva, capital, saldoFinal, valorAsegurable, seguro)
                .also { saldo = saldoFinal }
        }

        fun suma(filas: List<InsuranceAmortizationRow>) =
            filas.sumOf { it.monthlyInsurance }.setScale(2, RoundingMode.HALF_UP)

        return InsuranceSchedule(rows, suma(rows.take(12)), suma(rows))
    }

    private val MC = MathContext(34, RoundingMode.HALF_UP)
    private val BD_100 = BigDecimal("100")
    private val BD_1000 = BigDecimal("1000")
    private val IVA = BigDecimal("0.16")
    private val UNO_MAS_IVA = BigDecimal("1.16")
}
