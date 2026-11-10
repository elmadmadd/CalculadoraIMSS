package com.example.consucalcu.model

import java.math.BigDecimal

enum class TipoPension {
    JUBILACION, VEJEZ, CESANTIA, VIUDEZ, INVALIDEZ, ORFANDAD, RIESGO_TRABAJO, ASCENDENCIA, OTRA
}

enum class ResultadoSeguro { NINGUNO, UN_ANIO, PLAZO_COMPLETO, RECHAZO }

data class InsuranceAmortizationRow(
    val month: Int,
    val initialBalance: BigDecimal,
    val interest: BigDecimal,
    val iva: BigDecimal,
    val principal: BigDecimal,
    val finalBalance: BigDecimal,
    val insurableValue: BigDecimal,
    val monthlyInsurance: BigDecimal
)

data class InsuranceSchedule(
    /** Pago de la hoja del seguro; no es el descuento del cotizador. */
    val payment: BigDecimal,
    val rows: List<InsuranceAmortizationRow>,
    val firstYearCost: BigDecimal,
    val fullTermCost: BigDecimal
)

data class InsuranceDictamenResult(
    val aprobado: Boolean,
    val producto: String,
    val plazo: Int,
    val tasaMensual: Double,
    val factorSeguro: Double,
    val tipoSeguro: String,
    val montoSeguro: Double,
    val motivo: String
)
