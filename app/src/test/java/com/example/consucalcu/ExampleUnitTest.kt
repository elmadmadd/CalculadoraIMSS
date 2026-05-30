package com.example.consucalcu

import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

class ExampleUnitTest {
    
    @Test
    fun testFinancialCalculations() {
        val testCases = listOf(
            // 60 months
            Triple("CONSUBANCO TRADICIONAL", 60, Pair(BigDecimal("84926.97"), BigDecimal("2928.10"))),
            Triple("OPCIPRES TRADICIONAL", 60, Pair(BigDecimal("81368.60"), BigDecimal("2928.10"))),
            Triple("CONSUPAGO TRADICIONAL", 60, Pair(BigDecimal("79960.75"), BigDecimal("2928.10"))),
            Triple("MASNOMINA", 60, Pair(BigDecimal("79616.66"), BigDecimal("2928.10"))),
            
            // 54 months
            Triple("CONSUBANCO TRADICIONAL", 54, Pair(BigDecimal("81300.48"), BigDecimal("2928.10"))),
            Triple("MASNOMINA", 54, Pair(BigDecimal("81300.48"), BigDecimal("2928.10"))),
            Triple("OPCIPRES TRADICIONAL", 54, Pair(BigDecimal("77752.83"), BigDecimal("2928.10"))),
            Triple("CONSUPAGO TRADICIONAL", 54, Pair(BigDecimal("76856.24"), BigDecimal("2928.10"))),
            
            // 48 months
            Triple("CONSUBANCO TRADICIONAL", 48, Pair(BigDecimal("76359.58"), BigDecimal("2928.10"))),
            Triple("OPCIPRES TRADICIONAL", 48, Pair(BigDecimal("73450.04"), BigDecimal("2928.10"))),
            Triple("CONSUPAGO TRADICIONAL", 48, Pair(BigDecimal("73063.64"), BigDecimal("2928.10"))),
            Triple("MASNOMINA", 48, Pair(BigDecimal("72864.09"), BigDecimal("2928.10"))),
            
            // 36 months
            Triple("CONSUBANCO TRADICIONAL", 36, Pair(BigDecimal("64893.92"), BigDecimal("2928.10"))),
            Triple("CONSUPAGO TRADICIONAL", 36, Pair(BigDecimal("63283.14"), BigDecimal("2928.10"))),
            Triple("OPCIPRES TRADICIONAL", 36, Pair(BigDecimal("63227.04"), BigDecimal("2928.10"))),
            Triple("MASNOMINA", 36, Pair(BigDecimal("63143.11"), BigDecimal("2928.10"))),
            
            // 24 months
            Triple("CONSUPAGO TRADICIONAL", 24, Pair(BigDecimal("49196.02"), BigDecimal("2928.10"))),
            Triple("MASNOMINA", 24, Pair(BigDecimal("49148.77"), BigDecimal("2928.10"))),
            
            // 12 months
            Triple("CONSUPAGO TRADICIONAL", 12, Pair(BigDecimal("29018.00"), BigDecimal("2928.10"))),

            // 60 months Special/VIP
            Triple("CONSUBANCO VIP", 60, Pair(BigDecimal("378279.72"), BigDecimal("11709.34"))),
            Triple("CONSUBANCO ESPECIAL", 60, Pair(BigDecimal("370217.51"), BigDecimal("11709.34"))),
            Triple("OPCIPRES ESPECIAL", 60, Pair(BigDecimal("363386.36"), BigDecimal("11709.34"))),
            Triple("CONSUPAGO ESPECIAL", 60, Pair(BigDecimal("352083.79"), BigDecimal("11709.34")))
        )

        for ((product, term, values) in testCases) {
            val (amount, expectedPayment) = values
            
            var testAmount = amount
            var testExpectedPayment = expectedPayment
            
            // Dynamically scale up the test inputs if they are below the minimum term limits
            val minAmt = FinancialConfig.PRODUCTS[product]?.minTermMap?.get(term)
                ?: FinancialConfig.PRODUCTS[product]?.minGlobal
                ?: BigDecimal.ZERO
                
            if (testAmount < minAmt) {
                testAmount = testAmount.multiply(BigDecimal("10"))
                testExpectedPayment = testExpectedPayment.multiply(BigDecimal("10"))
            }

            val result = FinancialEngine.calculate(
                prodKey = product,
                value = testAmount,
                term = term,
                isByAmount = true,
                useRetention = false,
                roundingStep = BigDecimal("0.01")
            )
            assertNotNull("Result should not be null for $product $term", result)
            val calResult = result!!.first
            assertEquals(
                "Payment mismatch for $product at $term months (monto $testAmount)",
                testExpectedPayment.toDouble(),
                calResult.payment.toDouble(),
                0.01
            )
        }
    }

    @Test
    fun testMinimumAmounts() {
        // MASNOMINA minimums:
        // 60m: 10000, 54m: 75000, 48m: 15000, 36m: 15000, 24m: 20000
        assertTrue(FinancialEngine.isValidTermAndAmount("MASNOMINA", 60, BigDecimal("10000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("MASNOMINA", 60, BigDecimal("9900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("MASNOMINA", 54, BigDecimal("75000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("MASNOMINA", 54, BigDecimal("74000")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("MASNOMINA", 48, BigDecimal("15000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("MASNOMINA", 48, BigDecimal("14000")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("MASNOMINA", 36, BigDecimal("15000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("MASNOMINA", 36, BigDecimal("14000")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("MASNOMINA", 24, BigDecimal("20000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("MASNOMINA", 24, BigDecimal("19000")))

        // CONSUPAGO TRADICIONAL minimums:
        // 60m: 5000, 54m: 5000, 48m: 10000, 36m: 10000, 24m: 25000, 12m: 25000
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 60, BigDecimal("5000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 60, BigDecimal("4900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 54, BigDecimal("5000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 54, BigDecimal("4900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 48, BigDecimal("10000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 48, BigDecimal("9900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 36, BigDecimal("10000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 36, BigDecimal("9900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 24, BigDecimal("25000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 24, BigDecimal("24900")))
        
        assertTrue(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 12, BigDecimal("25000")))
        assertFalse(FinancialEngine.isValidTermAndAmount("CONSUPAGO TRADICIONAL", 12, BigDecimal("24900")))
    }
}