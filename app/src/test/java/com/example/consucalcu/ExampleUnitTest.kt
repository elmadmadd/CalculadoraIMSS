package com.example.consucalcu

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.model.Debt
import com.example.consucalcu.model.LoanOffer
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun imssUsesOnlyTheSixNewQuoters() {
        assertEquals(
            linkedSetOf("OPC", "OPC MAX", "OPC ESP", "CSB", "CSB ESP", "CSB VIP"),
            FinancialConfig.IMSS_PRODUCT_KEYS
        )

        FinancialConfig.IMSS_PRODUCT_KEYS.forEach { product ->
            assertNotNull("Missing configuration for $product", FinancialConfig.PRODUCTS[product])
        }
    }

    @Test
    fun opcRateMinimumAndFactorTablesMatchTheNewQuoter() {
        val opc = FinancialConfig.PRODUCTS.getValue("OPC")

        assertEquals(
            mapOf(
                60 to BigDecimal("36.32581"),
                54 to BigDecimal("37.89928"),
                48 to BigDecimal("39.92836"),
                36 to BigDecimal("46.21135"),
                24 to BigDecimal("59.50058"),
                12 to BigDecimal("100.95868")
            ),
            opc.factorsTrad
        )
        assertEquals(
            mapOf(
                60 to BigDecimal("5000"),
                54 to BigDecimal("5000"),
                48 to BigDecimal("10000"),
                36 to BigDecimal("10000"),
                24 to BigDecimal("10000"),
                12 to BigDecimal("25000")
            ),
            opc.minTermMap
        )
        assertEquals(
            mapOf(60 to 2.61, 54 to 2.62, 48 to 2.63, 36 to 2.64, 24 to 2.65, 12 to 2.66),
            opc.tasaRefByTerm
        )
        assertEquals(
            mapOf(
                60 to "42.99%", 54 to "43.19%", 48 to "43.38%",
                36 to "43.57%", 24 to "43.77%", 12 to "43.96%"
            ),
            opc.catTradByTerm
        )
    }

    @Test
    fun csbRateMinimumAndFactorTablesMatchTheNewQuoter() {
        val csb = FinancialConfig.PRODUCTS.getValue("CSB")

        assertEquals(
            mapOf(
                60 to BigDecimal("34.47785"),
                54 to BigDecimal("36.27268"),
                48 to BigDecimal("38.26465"),
                36 to BigDecimal("45.12292"),
                24 to BigDecimal("58.78059"),
                12 to BigDecimal("100.34938")
            ),
            csb.factorsTrad
        )
        assertEquals(
            mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("35000"),
                48 to BigDecimal("35000"),
                36 to BigDecimal("40000"),
                24 to BigDecimal("40000"),
                12 to BigDecimal("45000")
            ),
            csb.minTermMap
        )
        assertEquals(
            mapOf(60 to 2.40, 54 to 2.43, 48 to 2.43, 36 to 2.50, 24 to 2.55, 12 to 2.57),
            csb.tasaRefByTerm
        )
    }

    @Test
    fun premiumProductsKeepTheInheritedSixtyMonthConditions() {
        val expected = mapOf(
            "OPC MAX" to PremiumConditions("33.257255", "2.26", "50000"),
            "OPC ESP" to PremiumConditions("32.22284", "2.14", "75000"),
            "CSB ESP" to PremiumConditions("31.62828", "2.07", "100000"),
            "CSB VIP" to PremiumConditions("30.9541845", "1.99", "150000")
        )

        expected.forEach { (product, conditions) ->
            val config = FinancialConfig.PRODUCTS.getValue(product)
            assertEquals("Unexpected terms for $product", setOf(60), config.factorsTrad.keys)
            assertEquals("Unexpected factor for $product", BigDecimal(conditions.factor), config.factorsTrad[60])
            assertEquals("Unexpected monthly rate for $product", conditions.rate.toDouble(), config.tasaRefForTerm(60), 0.0)
            assertEquals("Unexpected minimum for $product", BigDecimal(conditions.minimum), config.minTermMap[60])
        }
    }

    @Test
    fun everyNewQuoterCalculatesAtItsMinimumUsingItsConfiguredFactor() {
        FinancialConfig.IMSS_PRODUCT_KEYS.forEach { product ->
            val config = FinancialConfig.PRODUCTS.getValue(product)
            config.factorsTrad.forEach { (term, factor) ->
                val minimum = config.minTermMap.getValue(term)
                val result = FinancialEngine.calculate(
                    prodKey = product,
                    value = minimum,
                    term = term,
                    isByAmount = true,
                    useRetention = false,
                    roundingStep = BigDecimal("500")
                )

                assertNotNull("Calculation failed for $product at $term months", result)
                assertEquals(minimum, result!!.first.amount)
                assertEquals(
                    minimum.divide(BigDecimal("1000")).multiply(factor).setScale(2, RoundingMode.HALF_UP),
                    result.first.payment
                )
                assertTrue(
                    "The exact minimum was rejected for $product at $term months",
                    FinancialEngine.isValidTermAndAmount(product, term, minimum)
                )
                assertFalse(
                    "An amount below the minimum was accepted for $product at $term months",
                    FinancialEngine.isValidTermAndAmount(product, term, minimum.subtract(BigDecimal.ONE))
                )
            }
        }
    }

    @Test
    fun legacyProductNamesResolveToTheirReplacement() {
        val replacements = mapOf(
            "CONSUPAGO TRADICIONAL" to "OPC",
            "MASNOMINA" to "OPC",
            "CONSUPAGO ESPECIAL" to "OPC MAX",
            "OPCIPRES ESPECIAL" to "OPC ESP",
            "CONSUBANCO TRADICIONAL" to "CSB",
            "CONSUBANCO ESPECIAL" to "CSB ESP",
            "CONSUBANCO VIP" to "CSB VIP"
        )

        replacements.forEach { (legacyName, replacement) ->
            assertEquals(replacement, FinancialEngine.resolveProductKey(legacyName))
        }
    }

    @Test
    fun officialOpcCatsNormalizeToTheirPublishedMonthlyRates() {
        assertEquals(2.61, FinancialEngine.getNormalizedRate("42.99%"), 0.0)
        assertEquals(2.62, FinancialEngine.getNormalizedRate("43.19%"), 0.0)
        assertEquals(2.63, FinancialEngine.getNormalizedRate("43.38%"), 0.0)
        assertEquals(2.65, FinancialEngine.getNormalizedRate("43.77%"), 0.0)
    }

    @Test
    fun opcHasNoCncaRestrictions() {
        val invalidDebt = Debt(balance = "100000", payment = "1000", rate = "not-a-rate")
        val result = checkCnca(
            product = "OPC",
            debts = listOf(invalidDebt),
            debtRateIndependentOffer = offer("OPC", payment = "100")
        )

        assertTrue(result.first)
        assertEquals(listOf("OPC sin restricciones por ser el producto con la tasa más alta"), result.second)
    }

    @Test
    fun cncaAllowsRaisingAndKeepingTheRate() {
        val raisesRate = checkCsb(debt(rate = "2.20"))
        val keepsRate = checkCsb(debt(rate = "2.40"))

        assertTrue(raisesRate.first)
        assertEquals(listOf("Sube tasa"), raisesRate.second)
        assertTrue(keepsRate.first)
        assertEquals(listOf("Mantiene tasa"), keepsRate.second)
    }

    @Test
    fun cncaAllowsADecreaseOfAtMostTwentyFiveBasisPoints() {
        val atLimit = checkCsb(debt(rate = "2.65"))
        val overLimit = checkCsb(debt(rate = "2.651"))

        assertTrue(atLimit.first)
        assertEquals(listOf("Baja la tasa hasta 25 pb"), atLimit.second)
        assertFalse(overLimit.first)
    }

    @Test
    fun cncaAllowsUpToFiftyBasisPointsWhenTermIncreasesBySixMonths() {
        val sixMoreMonths = checkCsb(debt(rate = "2.90", originalTerm = "54"), offerTerm = 60)
        val onlyFiveMoreMonths = checkCsb(debt(rate = "2.90", originalTerm = "55"), offerTerm = 60)
        val moreThanFiftyPoints = checkCsb(debt(rate = "2.901", originalTerm = "54"), offerTerm = 60)

        assertTrue(sixMoreMonths.first)
        assertEquals(
            listOf("Baja la tasa hasta 50 pb y aumenta el plazo 6 meses o más"),
            sixMoreMonths.second
        )
        assertFalse(onlyFiveMoreMonths.first)
        assertFalse(moreThanFiftyPoints.first)
    }

    @Test
    fun cncaAllowsUpToFiftyBasisPointsWithFiveHundredMoreInPayment() {
        val currentDebt = debt(rate = "2.90", payment = "700", originalTerm = "60")
        val atLimit = checkCsb(currentDebt, offerPayment = "1200")
        val belowLimit = checkCsb(currentDebt, offerPayment = "1199.99")

        assertTrue(atLimit.first)
        assertEquals(
            listOf("Baja la tasa hasta 50 pb e incrementa el descuento \$500 o más sobre la suma de descuentos"),
            atLimit.second
        )
        assertFalse(belowLimit.first)
    }

    @Test
    fun cncaUsesLargestBalanceForRateAndTermRules() {
        val debts = listOf(
            debt(balance = "100000", rate = "2.65", payment = "400"),
            debt(balance = "99999", rate = "3.20", payment = "300")
        )
        val result = checkCnca("CSB", debts, offer("CSB", payment = "100"))

        assertTrue(result.first)
        assertEquals(listOf("Baja la tasa hasta 25 pb (crédito de mayor monto)"), result.second)
    }

    @Test
    fun cncaSumsPaymentsWhenSeveralCreditsWillBeSettled() {
        val debts = listOf(
            debt(balance = "100000", rate = "2.90", payment = "400", originalTerm = "60"),
            debt(balance = "50000", rate = "3.20", payment = "350", originalTerm = "60")
        )

        val exactlyFiveHundredMore = checkCnca("CSB", debts, offer("CSB", payment = "1250"))
        val oneCentShort = checkCnca("CSB", debts, offer("CSB", payment = "1249.99"))

        assertTrue(exactlyFiveHundredMore.first)
        assertEquals(
            listOf("Baja la tasa hasta 50 pb e incrementa el descuento \$500 o más sobre la suma de descuentos"),
            exactlyFiveHundredMore.second
        )
        assertFalse(oneCentShort.first)
    }

    private fun checkCsb(
        debt: Debt,
        offerTerm: Int = 60,
        offerPayment: String = "100"
    ): Pair<Boolean, List<String>> = checkCnca(
        product = "CSB",
        debts = listOf(debt),
        debtRateIndependentOffer = offer("CSB", payment = offerPayment, term = offerTerm)
    )

    private fun checkCnca(
        product: String,
        debts: List<Debt>,
        debtRateIndependentOffer: LoanOffer
    ): Pair<Boolean, List<String>> = FinancialEngine.checkNormative(
        prodKey = product,
        debts = debts,
        offer = debtRateIndependentOffer,
        config = FinancialConfig.PRODUCTS.getValue(product)
    )

    private fun debt(
        balance: String = "100000",
        rate: String,
        payment: String = "700",
        originalTerm: String = "60"
    ) = Debt(
        product = "CSB",
        balance = balance,
        payment = payment,
        rate = rate,
        originalTerm = originalTerm
    )

    private fun offer(product: String, payment: String, term: Int = 60) = LoanOffer(
        product = product,
        amount = BigDecimal("200000"),
        monthlyPayment = BigDecimal(payment),
        term = term
    )

    private data class PremiumConditions(
        val factor: String,
        val rate: String,
        val minimum: String
    )
}
