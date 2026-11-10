package com.example.consucalcu

import com.example.consucalcu.data.NormativaIMSS
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class InsuranceCalculationTest {

    // Cálculo de Seguro CSB V.18 (02.11.2026) y OPC V.10 (09.11.2026): producto,
    // plazo, monto de la hoja y sus celdas de cobro del primer año y del plazo total.
    @Test
    fun scheduleMatchesEverySheetOfTheOfficialWorkbooks() {
        val sheets = listOf(
            listOf("CSB", "12", "45000", "557.71", "557.71"),
            listOf("CSB", "24", "40000", "714.67", "1005.77"),
            listOf("CSB", "36", "40000", "782.95", "1557.21"),
            listOf("CSB", "48", "35000", "714.00", "1887.16"),
            listOf("CSB", "54", "35000", "721.97", "2153.29"),
            listOf("CSB", "60", "30000", "624.85", "2088.04"),
            listOf("CSB ESP", "60", "100000", "2062.84", "6745.90"),
            listOf("CSB VIP", "60", "2000004", "41124.62", "133529.62"),
            listOf("OPC", "12", "25000", "310.51", "310.51"),
            listOf("OPC", "24", "10000", "179.10", "252.33"),
            listOf("OPC", "36", "10000", "196.69", "393.54"),
            listOf("OPC", "48", "10000", "204.83", "545.09"),
            listOf("OPC", "54", "5000", "103.73", "312.59"),
            listOf("OPC", "60", "5000", "104.68", "353.96"),
            listOf("OPC ESP", "60", "75000", "1551.56", "5106.41"),
            listOf("OPC MAX", "60", "5000", "103.72", "343.56")
        )

        sheets.forEach { (product, term, amount, firstYear, fullTerm) ->
            val schedule = NormativaIMSS.calcularTablaSeguro(product, BigDecimal(amount), term.toInt())
            assertEquals("$product $term meses", term.toInt(), schedule.rows.size)
            assertEquals("1er año $product $term", BigDecimal(firstYear), schedule.firstYearCost)
            assertEquals("Total $product $term", BigDecimal(fullTerm), schedule.fullTermCost)
        }
    }

    @Test
    fun retentionAddsFiftyFourMonthsToSpecialProducts() {
        // Cálculo de Seguro CSB - OPC Retención V.1 (09.11.2026), hojas de 54 meses.
        listOf(
            listOf("CSB ESP", "100000", "2042.10", "5973.55"),
            listOf("CSB VIP", "100000", "2035.31", "5915.79"),
            listOf("OPC MAX", "5000", "102.71", "303.90"),
            listOf("OPC ESP", "5000", "102.42", "301.29")
        ).forEach { (product, amount, firstYear, fullTerm) ->
            val schedule = NormativaIMSS.calcularTablaSeguro(product, BigDecimal(amount), 54)
            assertEquals("1er año $product 54", BigDecimal(firstYear), schedule.firstYearCost)
            assertEquals("Total $product 54", BigDecimal(fullTerm), schedule.fullTermCost)
            assertEquals(listOf(54, 60), NormativaIMSS.plazosSeguro(product))
            assertEquals(true, NormativaIMSS.esRetencion(product, 54))
        }
        assertEquals(false, NormativaIMSS.esRetencion("CSB", 54))
        assertEquals(listOf(12, 24, 36, 48, 54, 60), NormativaIMSS.plazosSeguro("OPC"))
    }

    @Test
    fun firstRowMatchesTheWorkbookRow() {
        val row = NormativaIMSS.calcularTablaSeguro("CSB", BigDecimal("45000"), 12).rows.first()

        assertEquals(BigDecimal("1156.50"), row.interest)
        assertEquals(BigDecimal("185.04"), row.iva)
        assertEquals(BigDecimal("3174.18"), row.principal)
        assertEquals(BigDecimal("41825.82"), row.finalBalance)
        assertEquals(BigDecimal("46341.54"), row.insurableValue)
        assertEquals(BigDecimal("81.43"), row.monthlyInsurance)
    }

    @Test
    fun everyProductUsesTheSameInsuranceFactor() {
        listOf("OPC", "OPC MAX", "OPC ESP", "CSB", "CSB ESP", "CSB VIP").forEach {
            assertEquals(it, 1.7571, NormativaIMSS.getFactorSeguro(it), 0.0)
        }
    }
}
