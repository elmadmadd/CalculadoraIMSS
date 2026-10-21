package com.example.consucalcu.utils

import androidx.compose.ui.graphics.Color
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

// =================================================================================================
// 1. HELPERS & EXTENSIONS
// =================================================================================================
/**
 * Mezcla dos colores con alpha blending
 */
fun Color.blendWith(other: Color, ratio: Float = 0.5f): Color {
    val clampedRatio = ratio.coerceIn(0f, 1f)
    return Color(
        red = this.red * (1 - clampedRatio) + other.red * clampedRatio,
        green = this.green * (1 - clampedRatio) + other.green * clampedRatio,
        blue = this.blue * (1 - clampedRatio) + other.blue * clampedRatio,
        alpha = this.alpha * (1 - clampedRatio) + other.alpha * clampedRatio
    )
}

val BD_ZERO = BigDecimal.ZERO
val BD_500 = BigDecimal("500")

fun String.toBD(): BigDecimal = this.replace(",", "").replace("$", "").trim().toBigDecimalOrNull() ?: BD_ZERO
fun Double.toBD(): BigDecimal = BigDecimal.valueOf(this)
fun BigDecimal.toCurrency(): String = DecimalFormat("#,###.00").format(this)

// =================================================================================================
// 1.1 NSS VALIDATION UTILITY
// =================================================================================================
/**
 * Valida un Número de Seguridad Social (NSS) mexicano usando el algoritmo Luhn modificado.
 * @return true si el NSS es válido, false en caso contrario.
 */
fun String.isValidNSS(): Boolean {
    if (this.length != 11) return false
    if (!this.all { it.isDigit() }) return false

    val digits = this.map { it.toString().toInt() }
    var sum = 0

    // Algoritmo Luhn (usado por IMSS)
    for (i in 0..9) {
        var digit = digits[i]
        if (i % 2 == 0) digit *= 2
        if (digit > 9) digit -= 9
        sum += digit
    }

    val checkDigit = (10 - (sum % 10)) % 10
    return checkDigit == digits[10]
}

// =================================================================================================
// 1.2 AMORTIZATION CALCULATOR
// =================================================================================================
data class AmortizationRow(
    val month: Int,
    val payment: BigDecimal,
    val interest: BigDecimal,
    val iva: BigDecimal,
    val principal: BigDecimal,
    val balance: BigDecimal
)

/**
 * Genera la tabla de amortización asegurando que se respete el pago mensual fijo.
 * Calcula implícitamente la tasa de interés que cuadra exactamente la operación.
 * @param amount Monto del préstamo
 * @param term Plazo en meses
 * @param fixedPayment Pago mensual fijo determinado por el cotizador
 * @return Lista de filas de amortización
 */
fun calculateAmortizationTable(
    amount: BigDecimal,
    term: Int,
    fixedPayment: BigDecimal,
    periodsPerYear: Int = 12
): List<AmortizationRow> {
    if (amount <= BigDecimal.ZERO || term <= 0 || fixedPayment <= BigDecimal.ZERO) return emptyList()

    // 1. Algoritmo de Bisección (Binary Search) para encontrar la tasa de interés interna exacta
    // Buscamos una tasa 'r' (sin IVA) tal que al aplicar (Interés + IVA + Capital) el saldo llegue a 0.
    var low = 0.0
    var high = 1.0 // 100% de interés mensual como cota superior
    var periodicRate = 0.0
    val targetError = 0.01 // Precisión deseada de 1 centavo

    for (i in 0..100) {
        periodicRate = (low + high) / 2.0
        var tempBalance = amount.toDouble()

        for (m in 1..term) {
            val interest = tempBalance * periodicRate
            val iva = interest * 0.16
            val principal = fixedPayment.toDouble() - interest - iva
            tempBalance -= principal
        }

        if (Math.abs(tempBalance) <= targetError) {
            break
        } else if (tempBalance > 0) {
            // Tasa es muy alta (genera mucho interés y se abona poco capital)
            high = periodicRate
        } else {
            // Tasa es muy baja (se abona demasiado capital y el saldo queda negativo)
            low = periodicRate
        }
    }

    // 2. Construir la tabla usando la tasa exacta calculada
    val finalPeriodicRate = BigDecimal(periodicRate.toString()).setScale(10, RoundingMode.HALF_UP)

    val rows = mutableListOf<AmortizationRow>()
    var balance = amount

    for (month in 1..term) {
        val interest = balance.multiply(finalPeriodicRate).setScale(2, RoundingMode.HALF_UP)
        val iva = interest.multiply(BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP)

        var payment = fixedPayment
        var principal = payment.subtract(interest).subtract(iva)

        // Ajuste fino en el último mes para cuadrar a 0.00 exactos
        if (month == term) {
            principal = balance
            payment = principal.add(interest).add(iva).setScale(2, RoundingMode.HALF_UP)
        } else if (principal > balance) {
            principal = balance
            payment = principal.add(interest).add(iva).setScale(2, RoundingMode.HALF_UP)
        }

        balance = balance.subtract(principal).max(BigDecimal.ZERO)

        rows.add(
            AmortizationRow(
                month = month,
                payment = payment,
                interest = interest,
                iva = iva,
                principal = principal,
                balance = balance
            )
        )
    }

    return rows
}
