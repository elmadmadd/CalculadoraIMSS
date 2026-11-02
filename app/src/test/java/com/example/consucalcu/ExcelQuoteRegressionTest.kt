package com.example.consucalcu

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.features.FinancialEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(Parameterized::class)
class ExcelQuoteRegressionTest(
    private val product: String,
    private val amountText: String,
    private val paymentText: String,
    private val totalText: String
) {
    // Fuente: ConsuCalcu_Banco_Comparaciones_2026-09-08 (2).xlsx,
    // columnas A-F y K, 32 casos únicos por entidad + CAT + monto + plazo.
    @Test
    fun factorMatchesExcelAndEnginePreservesEligibility() {
        val config = FinancialConfig.PRODUCTS.getValue(product)
        val amount = BigDecimal(amountText)
        val expected = BigDecimal(paymentText)
        val computed = amount.multiply(config.factorsTrad.getValue(60))
            .divide(BigDecimal("1000")).setScale(2, RoundingMode.HALF_UP)
        assertEquals(expected, computed)
        assertEquals(BigDecimal(totalText), computed.multiply(BigDecimal("60")))

        // El Excel incluye CSB VIP por $100,000: sirve para calibrar, pero
        // no autoriza reducir el mínimo vigente de $150,000 en la app.
        for (retention in listOf(false, true)) {
            val result = FinancialEngine.calculate(product, amount, 60, true, retention, BigDecimal("500"))
            if (amount < (config.minTermMap[60] ?: config.minGlobal)) {
                assertNull(result)
            } else {
                assertNotNull(result)
                assertEquals(expected, result!!.first.payment)
                val inverse = FinancialEngine.calculate(product, expected, 60, false, retention, BigDecimal("500"))
                if (inverse != null) {
                    assertTrue(inverse.first.payment <= expected)
                    assertEquals(0, inverse.first.amount.remainder(BigDecimal("500")).compareTo(BigDecimal.ZERO))
                    val forward = FinancialEngine.calculate(product, inverse.first.amount, 60, true, retention, BigDecimal("500"))!!
                    assertEquals(inverse.first.payment, forward.first.payment)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}: monto {1}, cuota {2}")
        fun cases(): Collection<Array<String>> = listOf(
            arrayOf("CSB VIP", "100000", "3095.42", "185725.20"),
            arrayOf("CSB VIP", "350000", "10833.96", "650037.60"),
            arrayOf("CSB VIP", "300000", "9286.26", "557175.60"),
            arrayOf("CSB VIP", "176900", "5475.80", "328548.00"),
            arrayOf("CSB VIP", "245600", "7602.35", "456141.00"),
            arrayOf("CSB VIP", "235000", "7274.23", "436453.80"),
            arrayOf("CSB VIP", "150000", "4643.13", "278587.80")
        )
    }
}
