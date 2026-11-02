package com.example.consucalcu

import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.model.LoanOffer
import com.example.consucalcu.utils.calculateAmortizationTable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ImssBienestarCalculationTest {

    @Test
    fun agreementMetadataMatchesOfficialCotizador() {
        val agreement = FinancialConfig.agreementFor(FinancialConfig.AGREEMENT_IMSS_BIENESTAR, includeDisabled = true)

        assertFalse(agreement.enabled)
        assertEquals(listOf(12, 18, 24, 30, 36, 42, 48), agreement.terms)
        assertEquals(BigDecimal("5000"), agreement.minAmount)
        assertEquals(BigDecimal("1000000"), agreement.maxAmount)
        assertEquals(FinancialConfig.PAYMENT_FREQUENCY_BIWEEKLY, agreement.paymentFrequency)
        assertFalse(agreement.supportsConsolidation)
    }

    @Test
    fun imssBienestarAgreementIsDisabledByDefault() {
        assertEquals(FinancialConfig.AGREEMENT_IMSS, FinancialConfig.agreementFor(FinancialConfig.AGREEMENT_IMSS_BIENESTAR).id)
        assertFalse(FinancialConfig.ACTIVE_AGREEMENTS.any { it.id == FinancialConfig.AGREEMENT_IMSS_BIENESTAR })
        assertEquals(listOf(FinancialConfig.AGREEMENT_IMSS), FinancialConfig.ACTIVE_AGREEMENTS.map { it.id })
    }

    @Test
    fun fiveThousandPesosMatchesOfficialQuincenalPayments() {
        val expectedByTerm = mapOf(
            48 to BigDecimal("124.53"),
            42 to BigDecimal("130.10"),
            36 to BigDecimal("139.16"),
            30 to BigDecimal("151.03"),
            24 to BigDecimal("170.83"),
            18 to BigDecimal("203.58"),
            12 to BigDecimal("271.38")
        )

        expectedByTerm.forEach { (term, expectedPayment) ->
            val result = FinancialEngine.calculate(
                prodKey = "CONSUBANCO IMSS BIENESTAR",
                value = BigDecimal("5000"),
                term = term,
                isByAmount = true,
                useRetention = false,
                roundingStep = BigDecimal("500")
            )

            assertTrue("No result for $term months", result != null)
            assertEquals(expectedPayment.toDouble(), result!!.first.payment.toDouble(), 0.01)
        }
    }

    @Test
    fun biweeklyOfferUsesBiweeklyLabelsAndAmortizationPeriods() {
        val offer = LoanOffer(
            product = "CONSUBANCO",
            amount = BigDecimal("5000"),
            monthlyPayment = BigDecimal("124.53"),
            term = 48,
            agreementId = FinancialConfig.AGREEMENT_IMSS_BIENESTAR,
            agreementLabel = "IMSS Bienestar",
            paymentFrequency = FinancialConfig.PAYMENT_FREQUENCY_BIWEEKLY
        )

        val table = calculateAmortizationTable(
            amount = offer.amount,
            term = offer.periodCount,
            fixedPayment = offer.monthlyPayment,
            periodsPerYear = offer.periodsPerYear
        )

        assertEquals("PAGO QUINCENAL", offer.paymentLabel)
        assertEquals("48 meses · 96 quincenas", offer.termDisplay)
        assertEquals(96, table.size)
        assertEquals(BigDecimal.ZERO.setScale(2), table.last().balance)
    }
}
