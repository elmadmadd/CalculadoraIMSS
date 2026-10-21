package com.example.consucalcu

import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.utils.calculateAmortizationTable
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PortalComparisonRegressionTest {
    @Test
    fun baseProductsMatchUserPaymentsAndTotalsAtSixtyMonths() {
        // qwdqdssdas.pdf (2026-09-08), también corroborado por los JSON
        // de $40,000 y $205,000. Identificación por CAT, no solo por marca.
        val amounts = listOf("254600", "30000", "40000", "205000")
        val cases = mapOf(
            "OPC" to listOf(
                "9248.55" to "554913.00", "1089.77" to "65386.20",
                "1453.03" to "87181.80", "7446.79" to "446807.40"
            ),
            "CSB" to listOf(
                "8778.06" to "526683.60", "1034.34" to "62060.40",
                "1379.11" to "82746.60", "7067.96" to "424077.60"
            )
        )
        cases.forEach { (product, expected) ->
            amounts.zip(expected).forEach { (amount, reference) ->
                val result = FinancialEngine.calculate(
                    product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
                )
                assertNotNull("$product: $amount", result)
                val payment = result!!.first.payment
                assertEquals("Cuota $product: $amount", BigDecimal(reference.first), payment)
                assertEquals("Total $product: $amount", BigDecimal(reference.second), payment * BigDecimal("60"))
            }
        }
    }

    // Fuente: ConsuCalcu_Banco_Comparaciones_2026-09-08.pdf.
    // Identificar por marca Y CAT: OPC 36.42 = MAX; CSB 32.94 = ESP.
    // Las filas de Consupago/Más Nómina no calibran OPC o CSB base.
    @Test
    fun portalPaymentsMatchAtSixtyMonths() {
        val amounts = listOf("205000", "123456", "153400", "100000")
        val expected = mapOf(
            "OPC MAX" to listOf("6817.74", "4105.81", "5101.66", "3325.73"),
            "CSB ESP" to listOf("6483.80", "3904.70", "4851.78", "3162.83")
        )
        expected.forEach { (product, payments) ->
            amounts.zip(payments).forEach { (amount, payment) ->
                val result = FinancialEngine.calculate(
                    product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
                )
                assertNotNull("$product: $amount", result)
                assertEquals("$product: $amount", BigDecimal(payment), result!!.first.payment)
            }
        }
    }

    @Test
    fun portalAmountsProduceTheReferencePayrollDeduction() {
        mapOf("OPC MAX" to "352083.8", "CSB ESP" to "370217.5").forEach { (product, amount) ->
            val result = FinancialEngine.calculate(
                product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
            )!!
            assertEquals(BigDecimal("11709.34"), result.first.payment)
        }
    }

    @Test
    fun capacityModeRespectsTheAppAmountStepAndDoesNotExceedCapacity() {
        // La app ofrece múltiplos de $500; el PDF conserva montos con décimas.
        mapOf("OPC MAX" to "352000", "CSB ESP" to "370000").forEach { (product, amount) ->
            val capacity = BigDecimal("11709.34")
            val result = FinancialEngine.calculate(
                product, capacity, 60, false, false, BigDecimal("500")
            )!!
            assertEquals(BigDecimal(amount), result.first.amount)
            assertTrue(result.first.payment <= capacity)
            val next = FinancialEngine.calculate(
                product, result.first.amount + BigDecimal("500"), 60, true, false, BigDecimal.ZERO
            )!!
            assertTrue(next.first.payment > capacity)
        }
    }

    @Test
    fun opcMaxQuarterMillionMatchesPortalWithoutSpecialCase() {
        // El factor calibrado reproduce la cuota del portal en ambos modos.
        val byAmount = FinancialEngine.calculate(
            "OPC MAX", BigDecimal("250000"), 60, true, false, BigDecimal.ZERO
        )!!
        assertEquals(BigDecimal("8314.31"), byAmount.first.payment)

        val byCapacity = FinancialEngine.calculate(
            "OPC MAX", BigDecimal("8314.32"), 60, false, false, BigDecimal("500")
        )!!
        assertEquals(BigDecimal("250000"), byCapacity.first.amount)
        assertEquals(BigDecimal("8314.31"), byCapacity.first.payment)
    }

    @Test
    fun csbVipMatchesOfficialFactorGuide() {
        // Guía 2026-10-21: factor 0.0309541837 a 60 meses (CAT 31.5 %).
        // 13 simulaciones oficiales más el caso de $350,000 del Excel.
        mapOf(
            "170000" to "5262.21",
            "180000" to "5571.75",
            "253000" to "7831.41",
            "341400" to "10567.76",
            "157154.85" to "4864.60",
            "150000" to "4643.13",
            "185500" to "5742.00",
            "164500" to "5091.96",
            "410500" to "12706.69",
            "208181.23" to "6444.08",
            "202500" to "6268.22",
            "384000" to "11886.41",
            "516500" to "15987.84",
            "350000" to "10833.96"
        ).forEach { (amount, payment) ->
            val result = FinancialEngine.calculate(
                "CSB VIP", BigDecimal(amount), 60, true, false, BigDecimal.ZERO
            )!!
            assertEquals("CSB VIP: $amount", BigDecimal(payment), result.first.payment)
        }

        val byCapacity = FinancialEngine.calculate(
            "CSB VIP", BigDecimal("6000"), 60, false, false, BigDecimal("500")
        )!!
        assertEquals(BigDecimal("193500"), byCapacity.first.amount)
        assertTrue(byCapacity.first.payment <= BigDecimal("6000"))
    }

    @Test
    fun csbVipAmortizationFirstRowMatchesOfficialSimulation() {
        // monto, cuota, interés, IVA, abono a capital y saldo del mes 1.
        listOf(
            listOf("157154.85", "4864.60", "3127.12", "500.34", "1237.14", "155917.71"),
            listOf("170000", "5262.21", "3382.72", "541.24", "1338.25", "168661.75")
        ).forEach { row ->
            val amount = row[0]
            val firstRow = calculateAmortizationTable(
                amount = BigDecimal(amount),
                term = 60,
                fixedPayment = BigDecimal(row[1])
            ).first()
            assertEquals("Interés $amount", BigDecimal(row[2]), firstRow.interest)
            assertEquals("IVA $amount", BigDecimal(row[3]), firstRow.iva)
            assertEquals("Capital $amount", BigDecimal(row[4]), firstRow.principal)
            assertEquals("Saldo $amount", BigDecimal(row[5]), firstRow.balance)
        }
    }
}
