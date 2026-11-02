package com.example.consucalcu

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.model.LoanOffer
import com.example.consucalcu.utils.calculateAmortizationTable
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class PortalComparisonRegressionTest {
    @Test
    fun baseProductsMatchOfficialSimulationsAtSixtyMonths() {
        // Simulaciones oficiales 2026-11-02: monto, descuento y total a 60 meses.
        // CSB (Consubanco) con CAT 39.88 % y OPC (Opcipres) con CAT 43.96 %.
        val cases = mapOf(
            "CSB" to listOf(
                Triple("100000", "3487.50", "209250.00"),
                Triple("199999", "6974.96", "418497.60"),
                Triple("233333", "8137.49", "488249.40"),
                Triple("277751", "9686.56", "581193.60"),
                Triple("296254", "10331.86", "619911.60")
            ),
            "OPC" to listOf(
                Triple("100000", "3677.75", "220665.00"),
                Triple("199999", "7355.46", "441327.60"),
                Triple("233333", "8581.40", "514884.00"),
                Triple("277751", "10214.98", "612898.80"),
                Triple("296254", "10895.48", "653728.80")
            )
        )
        cases.forEach { (product, rows) ->
            rows.forEach { (amount, payment, total) ->
                val result = FinancialEngine.calculate(
                    product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
                )!!
                assertEquals("Cuota $product: $amount", BigDecimal(payment), result.first.payment)
                assertEquals("Total $product: $amount", BigDecimal(total), result.first.payment * BigDecimal("60"))
            }
        }
    }

    @Test
    fun capacityModeRespectsTheAppAmountStepAndDoesNotExceedCapacity() {
        // Por descuento la app ofrece múltiplos de $100.
        mapOf("OPC MAX" to "348400", "CSB ESP" to "367200").forEach { (product, amount) ->
            val capacity = BigDecimal("11709.34")
            val result = FinancialEngine.calculate(
                product, capacity, 60, false, false, BigDecimal("100")
            )!!
            assertEquals(BigDecimal(amount), result.first.amount)
            assertTrue(result.first.payment <= capacity)
            val next = FinancialEngine.calculate(
                product, result.first.amount + BigDecimal("100"), 60, true, false, BigDecimal.ZERO
            )!!
            assertTrue(next.first.payment > capacity)
        }
    }

    @Test
    fun csbVipMatchesOfficialFactorGuide() {
        // Guía 2026-10-21: 13 simulaciones a 60 meses (CAT 31.5 %).
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
            "CSB VIP", BigDecimal("6000"), 60, false, false, BigDecimal("100")
        )!!
        assertEquals(BigDecimal("193800"), byCapacity.first.amount)
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

    @Test
    fun premiumSixtyMonthMatchesOfficialSimulations2026_11_02() {
        // Simulaciones oficiales: monto, descuento mensual y total a 60 meses.
        // CSB ESP con CAT 33.48 % y OPC MAX (Opcipres) con CAT 37.16 %.
        val cases = mapOf(
            "CSB ESP" to listOf(
                Triple("100000", "3188.11", "191286.60"),
                Triple("199999", "6376.18", "382570.80"),
                Triple("333333", "10627.02", "637621.20"),
                Triple("456789", "14562.93", "873775.80")
            ),
            "OPC MAX" to listOf(
                Triple("100000", "3360.35", "201621.00"),
                Triple("199999", "6720.66", "403239.60"),
                Triple("333333", "11201.15", "672069.00"),
                Triple("456789", "15349.70", "920982.00")
            )
        )
        cases.forEach { (product, rows) ->
            rows.forEach { (amount, payment, total) ->
                val result = FinancialEngine.calculate(
                    product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
                )!!
                assertEquals("Cuota $product: $amount", BigDecimal(payment), result.first.payment)
                assertEquals("Total $product: $amount", BigDecimal(total), result.first.payment * BigDecimal("60"))
            }
        }
    }

    @Test
    fun csbVipAndOpcEspMatchOriginPlatformMatrix() {
        // resumen_de_motor_de_calculo.md (2026-11-02): capital, cuota y total a
        // 60 meses. El total siempre es la cuota redondeada por 60.
        val cases = listOf(
            listOf("CSB VIP", "74833", "2316.39", "138983.40"),
            listOf("CSB VIP", "123456.78", "3821.50", "229290.00"),
            listOf("CSB VIP", "199999", "6190.81", "371448.60"),
            listOf("OPC ESP", "15550.50", "509.09", "30545.40"),
            listOf("OPC ESP", "88888.88", "2910.02", "174601.20"),
            listOf("OPC ESP", "123456.78", "4041.70", "242502.00")
        )
        cases.forEach { (product, amount, payment, total) ->
            val config = FinancialConfig.PRODUCTS.getValue(product)
            val computed = BigDecimal(amount).multiply(config.factorsTrad.getValue(60))
                .divide(BigDecimal("1000")).setScale(2, RoundingMode.HALF_UP)
            assertEquals("Cuota $product: $amount", BigDecimal(payment), computed)
            assertEquals("Total $product: $amount", BigDecimal(total), computed * BigDecimal("60"))

            // Los capitales bajo el mínimo vigente calibran, pero no se ofrecen.
            val result = FinancialEngine.calculate(
                product, BigDecimal(amount), 60, true, false, BigDecimal.ZERO
            )
            if (BigDecimal(amount) < (config.minTermMap[60] ?: config.minGlobal)) {
                assertNull("$product: $amount bajo el mínimo", result)
            } else {
                assertEquals("Motor $product: $amount", BigDecimal(payment), result!!.first.payment)
            }
        }
    }

    @Test
    fun opcEspNewCatMapsToItsMonthlyRate() {
        assertEquals(2.20, FinancialEngine.getNormalizedRate("35.31%"), 0.0)
        assertEquals(2.10, FinancialEngine.getNormalizedRate("33.48%"), 0.0)
        assertEquals(2.45, FinancialEngine.getNormalizedRate("39.88%"), 0.0)
        assertEquals(2.30, FinancialEngine.getNormalizedRate("37.16%"), 0.0)
        // Los créditos vigentes con el CAT anterior se siguen reconociendo.
        assertEquals(2.14, FinancialEngine.getNormalizedRate("34.21"), 0.0)
    }

    @Test
    fun offerTotalIsRoundedPaymentTimesPeriods() {
        val offer = LoanOffer("CSB VIP", BigDecimal("199999"), BigDecimal("6190.81"), 60)
        assertEquals(BigDecimal("371448.60"), offer.totalToPay)
    }

    @Test
    fun baseProductsMatchOfficialSimulationsAtShorterTerms() {
        // Simulaciones oficiales 2026-11-02: producto, plazo, monto, descuento y total.
        val cases = listOf(
            listOf("CSB", "54", "270886", "9860.82", "532484.28"),
            listOf("CSB", "54", "282054", "10267.36", "554437.44"),
            listOf("CSB", "54", "287001", "10447.44", "564161.76"),
            listOf("CSB", "54", "288000", "10483.81", "566125.74"),
            listOf("OPC", "54", "270886", "10362.01", "559548.54"),
            listOf("OPC", "54", "282054", "10789.21", "582617.34"),
            listOf("OPC", "54", "287001", "10978.44", "592835.76"),
            listOf("OPC", "54", "288000", "11016.66", "594899.64"),
            listOf("CSB", "48", "244692", "9505.99", "456287.52"),
            listOf("CSB", "48", "256559", "9967.01", "478416.48"),
            listOf("CSB", "48", "263143", "10222.79", "490693.92"),
            listOf("CSB", "48", "274000", "10644.58", "510939.84"),
            listOf("OPC", "48", "244692", "9833.14", "471990.72"),
            listOf("OPC", "48", "256559", "10310.02", "494880.96"),
            listOf("OPC", "48", "263143", "10574.61", "507581.28"),
            listOf("OPC", "48", "274000", "11010.90", "528523.20"),
            listOf("CSB", "36", "210524", "9499.12", "341968.32"),
            listOf("CSB", "36", "229945", "10375.42", "373515.12"),
            listOf("CSB", "36", "236313", "10662.76", "383859.36"),
            listOf("CSB", "36", "237000", "10693.76", "384975.36"),
            listOf("OPC", "36", "210524", "9762.51", "351450.36"),
            listOf("OPC", "36", "229945", "10663.11", "383871.96"),
            listOf("OPC", "36", "236313", "10958.41", "394502.76"),
            listOf("OPC", "36", "237000", "10990.27", "395649.72"),
            listOf("CSB", "24", "154838", "9124.03", "218976.72"),
            listOf("CSB", "24", "181989", "10723.94", "257374.56"),
            listOf("CSB", "24", "183253", "10798.42", "259162.08"),
            listOf("CSB", "24", "185000", "10901.37", "261632.88"),
            listOf("OPC", "24", "154838", "9224.67", "221392.08"),
            listOf("OPC", "24", "181989", "10842.22", "260213.28"),
            listOf("OPC", "24", "183253", "10917.53", "262020.72"),
            listOf("OPC", "24", "185000", "11021.61", "264518.64"),
            listOf("CSB", "12", "93261", "9358.54", "112302.48"),
            listOf("CSB", "12", "106401", "10677.12", "128125.44"),
            listOf("CSB", "12", "106611", "10698.19", "128378.28"),
            listOf("CSB", "12", "109000", "10937.92", "131255.04"),
            listOf("OPC", "12", "106401", "10742.26", "128907.12"),
            listOf("OPC", "12", "106611", "10763.46", "129161.52"),
            listOf("OPC", "12", "109000", "11004.66", "132055.92")
        )
        cases.forEach { (product, termText, amount, payment, total) ->
            val term = termText.toInt()
            val result = FinancialEngine.calculate(
                product, BigDecimal(amount), term, true, false, BigDecimal.ZERO
            )!!
            assertEquals("Cuota $product $term: $amount", BigDecimal(payment), result.first.payment)
            assertEquals("Total $product $term: $amount", BigDecimal(total), result.first.payment * BigDecimal(term))
        }
    }
}
