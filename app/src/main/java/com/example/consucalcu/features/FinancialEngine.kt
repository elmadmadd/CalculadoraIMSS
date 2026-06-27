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

    val MIN_RET_NUEVO = mapOf(
        "CONSUBANCO" to BigDecimal("200000"),
        "OPCIPRES" to BigDecimal("125000"),
        "CONSUPAGO" to BigDecimal("100000"),
        "MASNOMINA" to BigDecimal("999999999")
    )

    val MIN_RET_CNCA = mapOf(
        "CONSUBANCO TRADICIONAL" to BigDecimal("100000"),
        "OPCIPRES TRADICIONAL" to BigDecimal("75000"),
        "CONSUPAGO TRADICIONAL" to BigDecimal("50000"),
        "MASNOMINA" to BigDecimal("999999999")
    )

    fun calculate(
        prodKey: String,
        value: BigDecimal,
        term: Int,
        isByAmount: Boolean,
        useRetention: Boolean,
        roundingStep: BigDecimal
    ): Pair<CalculationResult, String>? {
        val config = FinancialConfig.PRODUCTS[prodKey] ?: return null
        
        var limit = config.minTermMap[term]
        if (limit == null && config.minTermMap.isNotEmpty()) {
             limit = BigDecimal("999999999")
        }
        if (limit == null || (config.minTermMap.isEmpty() && limit == null)) {
            limit = config.minGlobal
        }

        val minMontoReal = limit ?: BigDecimal.ZERO

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
                montoFinal = roundSipre(value, activeRoundingStep)
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

    fun checkNormative(
        prodKey: String,
        debts: List<Debt>,
        offer: LoanOffer,
        config: FinancialConfig.ProductData
    ): Pair<Boolean, List<String>> {
        val reasons = mutableListOf<String>()
        val totalDebts = debts.sumOf { it.balance.toBD() }
        val remnant = offer.amount.subtract(totalDebts)
        val increase = remnant
        
        if (prodKey == "MASNOMINA") {
             return if (remnant > BigDecimal.ZERO) Pair(true, listOf("Liquidez Positiva")) 
                    else Pair(false, emptyList())
        }
        
        if (remnant <= BigDecimal.ZERO) return Pair(false, listOf("Rechazo: Sin Liquidez"))
        if (debts.isEmpty()) return Pair(true, listOf("Crédito Nuevo"))

        val total = debts.size
        
        var cTasa = 0
        var cLiq = 0
        var cAnt = 0
        var cPlazo = 0
        
        val plazoNuevo = offer.term
        
        for (d in debts) {
            val dRate = d.rate.toDoubleOrNull() ?: 0.0
            val dPaid = d.paymentsMade.toIntOrNull() ?: 0
            val dTerm = d.originalTerm.toIntOrNull() ?: 0
            
            if (prodKey == "OPCIPRES" && dRate <= 2.57) cTasa++
            else if (prodKey == "CONSUBANCO" && dRate <= 2.40) cTasa++
            else if (prodKey == "CONSUPAGO" && dRate <= 2.69) cTasa++

            var liqOk = false
            if (prodKey == "CONSUPAGO" && increase >= BigDecimal("500")) liqOk = true
            if (prodKey == "OPCIPRES" && increase >= BigDecimal("750") && dRate <= 2.69) liqOk = true
            if (prodKey == "CONSUBANCO" && increase >= BigDecimal("1000") && dRate <= 2.57) liqOk = true
            if (liqOk) cLiq++

            var antOk = false
            if (prodKey == "CONSUPAGO" && dPaid >= 16) antOk = true
            if (prodKey == "OPCIPRES" && dPaid >= 16 && dRate <= 2.69) antOk = true
            if (prodKey == "CONSUBANCO" && dPaid >= 16 && dRate <= 2.57) antOk = true
            if (antOk) cAnt++

            var plazoOk = false
            if (dTerm <= 12 && plazoNuevo >= 36) plazoOk = true
            if (dTerm <= 24 && plazoNuevo >= 48) plazoOk = true
            if ((dTerm == 36 || dTerm == 48) && plazoNuevo >= 54) plazoOk = true
            if (dTerm == 54 && plazoNuevo == 60) plazoOk = true
            if (plazoOk) cPlazo++
        }

        if (cTasa == total) reasons.add("Mejora Tasa (Global)")
        if (cLiq == total) reasons.add("Incremento Liquidez (Global)")
        if (cAnt == total) reasons.add("Antigüedad 16+ (Global)")
        if (cPlazo == total) reasons.add("Extensión Plazo (Global)")

        return if (reasons.isNotEmpty()) Pair(true, reasons) else Pair(false, listOf("No cumple normativa completa."))
    }

    fun isValidTermAndAmount(prodKey: String, term: Int, amount: BigDecimal): Boolean {
        val config = FinancialConfig.PRODUCTS[prodKey] ?: return false
        val limit = config.minTermMap[term] ?: config.minGlobal
        return amount >= limit
    }
}
