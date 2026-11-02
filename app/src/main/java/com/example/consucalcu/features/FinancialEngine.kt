package com.example.consucalcu.features

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.model.*
import com.example.consucalcu.utils.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object FinancialEngine {
    data class CalculationResult(val amount: BigDecimal, val payment: BigDecimal)

    private val MC = MathContext(20, RoundingMode.HALF_UP)
    private val BD_1000 = BigDecimal("1000")
    private val BD_60 = BigDecimal("60")

    private fun roundSipre(value: BigDecimal, step: BigDecimal): BigDecimal {
        if (step.compareTo(BigDecimal.ZERO) == 0) return value
        return value.divide(step, 0, RoundingMode.FLOOR).multiply(step)
    }

    fun getBrand(prodKey: String): String {
        val normalized = prodKey.trim().uppercase()
        return when {
            normalized == "CONSUBANCO IMSS BIENESTAR" -> "CONSUBANCO"
            normalized.startsWith("CSB") || normalized.startsWith("CONSUBANCO") -> "CSB"
            normalized.startsWith("OPC") || normalized.startsWith("CONSUPAGO") ||
                normalized.startsWith("CSP") || normalized.startsWith("MASNOMINA") ||
                normalized.startsWith("MÁS NÓMINA") || normalized.startsWith("MSN") -> "OPC"
            else -> normalized
        }
    }

    /** Resolves current names and migrates product names stored by previous app versions. */
    fun resolveProductKey(prodKey: String): String? {
        val normalized = prodKey.trim().uppercase()
        FinancialConfig.PRODUCTS.keys.firstOrNull { it.uppercase() == normalized }?.let { return it }
        return when (normalized) {
            "CONSUBANCO", "CONSUBANCO TRADICIONAL" -> "CSB"
            "CONSUBANCO ESPECIAL" -> "CSB ESP"
            "CONSUBANCO VIP" -> "CSB VIP"
            "CONSUPAGO ESPECIAL", "CSP ESP", "CSP ESPECIAL" -> "OPC MAX"
            "OPCIPRES ESPECIAL", "OPC ESPECIAL" -> "OPC ESP"
            "CONSUPAGO", "CONSUPAGO TRADICIONAL", "CSP", "CSP TRADICIONAL",
            "OPCIPRES", "OPCIPRES TRADICIONAL", "MASNOMINA", "MÁS NÓMINA", "MSN" -> "OPC"
            else -> null
        }?.takeIf { it in FinancialConfig.PRODUCTS }
    }

    fun hasValidFactor(prodKey: String, term: Int, useRetention: Boolean = false): Boolean {
        val resolved = resolveProductKey(prodKey) ?: return false
        val config = FinancialConfig.PRODUCTS[resolved] ?: return false
        val factor = if (useRetention) {
            config.retention?.factors?.get(term) ?: config.factorsTrad[term]
        } else {
            config.factorsTrad[term]
        }
        return factor != null && factor > BigDecimal.ZERO
    }

    fun calculate(
        prodKey: String,
        value: BigDecimal,
        term: Int,
        isByAmount: Boolean,
        useRetention: Boolean,
        roundingStep: BigDecimal
    ): Pair<CalculationResult, String>? {
        val resolvedProductKey = resolveProductKey(prodKey) ?: return null
        val config = FinancialConfig.PRODUCTS[resolvedProductKey] ?: return null

        val minMontoReal = config.minTermMap[term] ?: config.minGlobal

        var factor = config.factorsTrad[term]
        var tipoTasa = "TRADICIONAL"
        var isSipreGlobal = false

        var activeRoundingStep = if(roundingStep == BigDecimal.ZERO) config.minInc else roundingStep
        if (activeRoundingStep == BigDecimal.ZERO) activeRoundingStep = BigDecimal("100")

        if (useRetention && term == 60) {
            val globalFactor = config.retention?.factors?.get(60)
            if (globalFactor != null) {
                factor = globalFactor
                tipoTasa = "RETENCION"
                isSipreGlobal = true
            }
        } else if (useRetention) {
             val f = config.retention?.factors?.get(term)
             if (f != null) {
                 factor = f
                 tipoTasa = "RETENCION"
             }
        }

        if (factor == null || factor == BigDecimal.ZERO) return null

        if (isSipreGlobal) {
            var currentVal = value
            var workingByAmount = isByAmount

            for (i in 0 until 50) {
                val montoBase: BigDecimal

                if (workingByAmount) {
                     montoBase = roundSipre(currentVal, activeRoundingStep)
                } else {
                     val montoEst = currentVal.multiply(BD_60, MC).divide(factor, MC)
                     montoBase = roundSipre(montoEst, activeRoundingStep)
                }

                if (montoBase < minMontoReal) return null

                val totalTeorico = montoBase.multiply(factor, MC)
                val pagoMensual = totalTeorico.divide(BD_60, MC).setScale(2, RoundingMode.HALF_UP)

                if (!workingByAmount && pagoMensual > value) {
                    val nuevoMonto = montoBase.subtract(activeRoundingStep)
                    if (nuevoMonto < minMontoReal) return null
                    currentVal = nuevoMonto
                    workingByAmount = true
                    continue
                }

                return Pair(CalculationResult(montoBase, pagoMensual), tipoTasa)
            }
            return null
        } else {
            val montoFinal: BigDecimal
            val pagoFinal: BigDecimal

            if (isByAmount) {
                montoFinal = value // Use input value directly without rounding to match SIPRE exactly
                val bruto = montoFinal.divide(BD_1000, MC).multiply(factor, MC)
                pagoFinal = bruto.setScale(2, RoundingMode.HALF_UP)
            } else {
                val bruto = value.divide(factor, MC).multiply(BD_1000, MC)
                montoFinal = roundSipre(bruto, activeRoundingStep)
                val check = montoFinal.divide(BD_1000, MC).multiply(factor, MC)
                pagoFinal = check.setScale(2, RoundingMode.HALF_UP)
            }

            if (montoFinal < minMontoReal) return null
            return Pair(CalculationResult(montoFinal, pagoFinal), tipoTasa)
        }
    }

    fun getNormalizedRate(rateInput: String): Double {
        val clean = rateInput.replace("%", "").trim()
        val mapped = when (clean) {
            "43.96" -> 2.66
            "43.77" -> 2.65
            "43.57" -> 2.64
            "43.38" -> 2.63
            "43.19" -> 2.62
            "42.99" -> 2.61
            "43.81" -> 2.65
            "43.71" -> 2.65
            "43.62" -> 2.64
            "43.23" -> 2.62
            "42.65" -> 2.59
            "42.62" -> 2.57
            "42.26" -> 2.57
            "41.88" -> 2.55
            "40.92" -> 2.50
            "39.60" -> 2.43
            "39.78" -> 2.44
            "39.03" -> 2.40
            "36.42" -> 2.26
            "35.31" -> 2.20
            "34.21" -> 2.14
            "32.94" -> 2.07
            "31.50" -> 1.99
            else -> null
        }
        if (mapped != null) return mapped

        val parsed = clean.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite() || parsed <= 0.0) return Double.NaN

        // If parsed is a CAT directly (any value > 10.0), let's find the closest match or do a simple conversion.
        if (parsed > 10.0) {
            val list = listOf(
                Pair(43.96, 2.66),
                Pair(43.77, 2.65),
                Pair(43.57, 2.64),
                Pair(43.38, 2.63),
                Pair(43.19, 2.62),
                Pair(42.99, 2.61),
                Pair(43.81, 2.65),
                Pair(43.71, 2.65),
                Pair(43.62, 2.64),
                Pair(43.23, 2.62),
                Pair(42.65, 2.59),
                Pair(42.62, 2.57),
                Pair(42.26, 2.57),
                Pair(41.88, 2.55),
                Pair(40.92, 2.50),
                Pair(39.60, 2.43),
                Pair(39.78, 2.44),
                Pair(39.03, 2.40),
                Pair(36.42, 2.26),
                Pair(35.31, 2.20),
                Pair(34.21, 2.14),
                Pair(32.94, 2.07),
                Pair(31.50, 1.99)
            )
            var closestRate = 0.0
            var minDiff = 999999.0
            for ((catVal, rateVal) in list) {
                val diff = Math.abs(catVal - parsed)
                if (diff < minDiff) {
                    minDiff = diff
                    closestRate = rateVal
                }
            }
            if (minDiff < 1.5) { // Only map if it is close enough to a known CAT
                return closestRate
            }
            return Double.NaN
        }
        return parsed
    }

    fun checkNormative(
        prodKey: String,
        debts: List<Debt>,
        offer: LoanOffer,
        config: FinancialConfig.ProductData
    ): Pair<Boolean, List<String>> {
        if (debts.isEmpty()) return Pair(true, listOf("Crédito Nuevo"))

        val resolvedProduct = resolveProductKey(prodKey)
            ?: return Pair(false, listOf("Rechazo: Cotizador no reconocido."))

        // OPC base no tiene restricciones porque es el producto con la tasa más alta.
        if (resolvedProduct == "OPC") {
            return Pair(true, listOf("OPC sin restricciones por ser el producto con la tasa más alta"))
        }

        // Con varios créditos, la regla de tasa/plazo se evalúa únicamente contra
        // el de mayor saldo. El incremento de descuento sí usa la suma de pagos.
        val d = debts.maxWithOrNull(
            compareBy<Debt> { it.balance.toBD() }
                .thenBy { getNormalizedRate(it.rate).takeIf(Double::isFinite) ?: Double.POSITIVE_INFINITY }
        )
            ?: return Pair(false, listOf("Rechazo: No hay una deuda válida para evaluar."))
        val dRate = getNormalizedRate(d.rate)
        if (!dRate.isFinite() || dRate <= 0.0) {
            return Pair(false, listOf("Rechazo: La tasa o CAT de la deuda de mayor monto no es válida."))
        }

        val proposedRate = config.tasaRefForTerm(offer.term)
        if (!proposedRate.isFinite() || proposedRate <= 0.0) {
            return Pair(false, listOf("Rechazo: La tasa del nuevo cotizador no es válida."))
        }

        val dOrigTerm = d.originalTerm.toIntOrNull() ?: 0
        val referenceSuffix = if (debts.size > 1) " (crédito de mayor monto)" else ""
        val rateReduction = dRate - proposedRate

        if (rateReduction < -0.000001) {
            return Pair(true, listOf("Sube tasa$referenceSuffix"))
        }
        if (kotlin.math.abs(rateReduction) <= 0.000001) {
            return Pair(true, listOf("Mantiene tasa$referenceSuffix"))
        }
        if (rateReduction <= 0.25 + 0.000001) {
            return Pair(true, listOf("Baja la tasa hasta 25 pb$referenceSuffix"))
        }
        if (rateReduction <= 0.50 + 0.000001 && dOrigTerm > 0 && offer.term >= dOrigTerm + 6) {
            return Pair(true, listOf("Baja la tasa hasta 50 pb y aumenta el plazo 6 meses o más$referenceSuffix"))
        }

        val totalCurrentPayments = debts.sumOf { it.payment.toBD() }
        if (debts.any { it.payment.toBD() <= BigDecimal.ZERO }) {
            return Pair(false, listOf("Rechazo: Capture todos los descuentos para validar el incremento de $500."))
        }
        val paymentIncrease = offer.monthlyPayment.subtract(totalCurrentPayments)
        if (rateReduction <= 0.50 + 0.000001 && paymentIncrease >= BigDecimal("500")) {
            return Pair(true, listOf("Baja la tasa hasta 50 pb e incrementa el descuento $500 o más sobre la suma de descuentos"))
        }

        return Pair(
            false,
            listOf("No cumple CNCA: la baja de tasa requiere máximo 25 pb, o hasta 50 pb con +6 meses de plazo o +$500 de descuento.")
        )
    }

    fun isValidTermAndAmount(prodKey: String, term: Int, amount: BigDecimal): Boolean {
        val config = FinancialConfig.PRODUCTS[prodKey] ?: return false
        val limit = config.minTermMap[term] ?: config.minGlobal
        return amount >= limit
    }
}
