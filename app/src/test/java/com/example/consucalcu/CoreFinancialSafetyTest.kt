package com.example.consucalcu

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.data.PreferenceJsonCodec
import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.model.Debt
import com.example.consucalcu.model.LoanOffer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CoreFinancialSafetyTest {

    @Test
    fun unsupportedProductTermNeverProducesZeroPaymentFallback() {
        assertFalse(FinancialEngine.hasValidFactor("CSB", 18))
        assertNull(
            FinancialEngine.calculate(
                prodKey = "CSB",
                value = BigDecimal("100000"),
                term = 18,
                isByAmount = true,
                useRetention = false,
                roundingStep = BigDecimal.ZERO
            )
        )
    }

    @Test
    fun legacyBrandAliasResolvesOnlyToAConcreteSupportedProduct() {
        assertEquals("CSB", FinancialEngine.resolveProductKey("CONSUBANCO"))
        assertTrue(FinancialEngine.hasValidFactor("CONSUBANCO", 60))
        assertNull(FinancialEngine.resolveProductKey("PRODUCTO DESCONOCIDO"))
    }

    @Test
    fun invalidRateIsNotConvertedToZeroAndCannotPassNormativeRules() {
        assertTrue(FinancialEngine.getNormalizedRate("").isNaN())
        assertTrue(FinancialEngine.getNormalizedRate("0").isNaN())
        assertTrue(FinancialEngine.getNormalizedRate("CAT desconocido").isNaN())
        assertTrue(FinancialEngine.getNormalizedRate("99.99%").isNaN())

        val config = FinancialConfig.PRODUCTS.getValue("CSB")
        val debt = Debt(balance = "10000", rate = "")
        val offer = LoanOffer(
            product = "CSB",
            amount = BigDecimal("15000"),
            monthlyPayment = BigDecimal("1500"),
            term = 60
        )

        val result = FinancialEngine.checkNormative(
            "CSB",
            listOf(debt),
            offer,
            config
        )

        assertFalse(result.first)
        assertTrue(result.second.single().contains("tasa o CAT", ignoreCase = true))
    }

    @Test
    fun malformedPreferenceJsonFailsClosedInsteadOfThrowing() {
        val type = object : TypeToken<List<Debt>>() {}.type
        assertNull(PreferenceJsonCodec.fromJsonOrNull<List<Debt>>(Gson(), "{not-json", type))
        assertNull(PreferenceJsonCodec.decodeDebts(Gson(), "{not-json"))
        assertNull(PreferenceJsonCodec.decodeClient(Gson(), "[not-an-object]"))
    }

    @Test
    fun oldPreferenceJsonIsMigratedWithSafeDefaults() {
        val debts = PreferenceJsonCodec.decodeDebts(Gson(), """[{"balance":"12000"}]""")
        assertEquals(1, debts?.size)
        assertEquals("CSB", debts?.single()?.product)
        assertEquals("60", debts?.single()?.originalTerm)

        val client = PreferenceJsonCodec.decodeClient(Gson(), """{"name":"Ana"}""")
        assertEquals("Ana", client?.name)
        assertEquals("IMSS", client?.agreement)
        assertEquals("NUEVO", client?.transactionType)
    }

    @Test
    fun requestedAmountMustRespectAgreementRangeAndStep() {
        val imss = FinancialConfig.agreementFor(FinancialConfig.AGREEMENT_IMSS)
        assertTrue(imss.acceptsRequestedAmount(BigDecimal("50000")))
        assertTrue(imss.acceptsRequestedAmount(BigDecimal("12300")))
        assertTrue(imss.acceptsRequestedAmount(BigDecimal("1000000")))
        assertFalse(imss.acceptsRequestedAmount(BigDecimal("12345")))
        assertFalse(imss.acceptsRequestedAmount(BigDecimal("12300.50")))
        assertFalse(imss.acceptsRequestedAmount(BigDecimal("4900")))
        assertFalse(imss.acceptsRequestedAmount(BigDecimal("1000100")))
    }

    @Test
    fun premiumCapacityCanExceedAgreementMaximumSoOffersMustBeCapped() {
        // El motor no conoce el tope del convenio; el ViewModel debe filtrar.
        val imss = FinancialConfig.agreementFor(FinancialConfig.AGREEMENT_IMSS)
        val vip = FinancialEngine.calculate(
            "CSB VIP", BigDecimal("40000"), 60, false, false, BigDecimal("500")
        )!!
        assertEquals(BigDecimal("1292000"), vip.first.amount)
        assertTrue(vip.first.amount > imss.maxAmount)
    }
}
