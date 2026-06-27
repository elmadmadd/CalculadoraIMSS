package com.example.consucalcu.data

import java.math.BigDecimal

object FinancialConfig {
    data class ProductMeta(val tasaRef: Double, val catTrad: String, val catRet: String)
    data class RetentionRule(val factors: Map<Int, BigDecimal>, val minAmount: BigDecimal)
    data class ProductData(
        val factorsTrad: Map<Int, BigDecimal>,
        val retention: RetentionRule? = null,
        val meta: ProductMeta,
        val minGlobal: BigDecimal,
        val minInc: BigDecimal,
        val minTermMap: Map<Int, BigDecimal> = emptyMap()
    )

    val PRODUCTS = mapOf(
        "CONSUBANCO TRADICIONAL" to ProductData(
            mapOf(
                60 to BigDecimal("34.47786"),
                54 to BigDecimal("36.01578"),
                48 to BigDecimal("38.34620"),
                36 to BigDecimal("45.12133")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("30.95418")),
                BigDecimal("100000.0")
            ),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("50000"),
            BigDecimal("1000"),
            mapOf(
                60 to BigDecimal("50000"),
                54 to BigDecimal("75000"),
                48 to BigDecimal("100000"),
                36 to BigDecimal("100000")
            )
        ),
        "CONSUBANCO ESPECIAL" to ProductData(
            mapOf(60 to BigDecimal("31.62827")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.50, "32.94%", "31.50%"),
            BigDecimal("100000"),
            BigDecimal("1000"),
            mapOf(60 to BigDecimal("100000"))
        ),
        "CONSUBANCO VIP" to ProductData(
            mapOf(60 to BigDecimal("30.95419")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("150000"),
            BigDecimal("1000"),
            mapOf(60 to BigDecimal("150000"))
        ),
        "CONSUPAGO TRADICIONAL" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.61922"),
                54 to BigDecimal("38.09840"),
                48 to BigDecimal("40.07602"),
                36 to BigDecimal("46.26983"),
                24 to BigDecimal("59.51904"),
                12 to BigDecimal("100.90633")
            ),
            retention = RetentionRule(
                 mapOf(60 to BigDecimal("33.25726")),
                 BigDecimal("50000.0")
            ),
            meta = ProductMeta(2.73, "43.62%", "36.42%"),
            minGlobal = BigDecimal("5000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                60 to BigDecimal("5000"), 54 to BigDecimal("5000"),
                48 to BigDecimal("10000"), 36 to BigDecimal("10000"),
                24 to BigDecimal("25000"), 12 to BigDecimal("25000")
            )
        ),
        "CONSUPAGO ESPECIAL" to ProductData(
            factorsTrad = mapOf(60 to BigDecimal("33.25726")),
            retention = RetentionRule(emptyMap(), BigDecimal("9999999")),
            meta = ProductMeta(2.73, "43.62%", "36.42%"),
            minGlobal = BigDecimal("50000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("50000"))
        ),
        "OPCIPRES TRADICIONAL" to ProductData(
            mapOf(
                60 to BigDecimal("35.98563"),
                54 to BigDecimal("37.65908"),
                48 to BigDecimal("39.86519"),
                36 to BigDecimal("46.31088")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("32.22283")),
                BigDecimal("75000.0")
            ),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("30000"),
            BigDecimal("750"),
            mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("45000"),
                48 to BigDecimal("50000"),
                36 to BigDecimal("70000")
            )
        ),
        "OPCIPRES ESPECIAL" to ProductData(
            mapOf(60 to BigDecimal("32.22283")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("75000"),
            BigDecimal("750"),
            mapOf(60 to BigDecimal("75000"))
        ),
        "MASNOMINA" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.77748"),
                54 to BigDecimal("36.01578"),
                48 to BigDecimal("40.18578"),
                36 to BigDecimal("46.37244"),
                24 to BigDecimal("59.57626")
            ),
            retention = RetentionRule(emptyMap(), BigDecimal("999999999.0")),
            meta = ProductMeta(2.73, "43.96%", "46.50%"),
            minGlobal = BigDecimal("10000"),
            minInc = BigDecimal("0"),
            minTermMap = mapOf(
                60 to BigDecimal("10000"),
                54 to BigDecimal("75000"),
                48 to BigDecimal("15000"),
                36 to BigDecimal("15000"),
                24 to BigDecimal("20000")
            )
        )
    )
    val STANDARD_TERMS = listOf("12", "18", "24", "36", "48", "54", "60")
    
    val INSURANCE_FACTORS = mapOf(
        "CONSUBANCO" to mapOf(
            36 to Pair(BigDecimal("0.020390"), BigDecimal("0.040555")),
            48 to Pair(BigDecimal("0.021213"), BigDecimal("0.055907")),
            54 to Pair(BigDecimal("0.021461"), BigDecimal("0.063849")),
            60 to Pair(BigDecimal("0.021671"), BigDecimal("0.072217"))
        ),
        "CONSUPAGO" to mapOf(
            12 to Pair(BigDecimal("0.012956"), BigDecimal("0.012956")),
            24 to Pair(BigDecimal("0.018685"), BigDecimal("0.026342")),
            36 to Pair(BigDecimal("0.020507"), BigDecimal("0.041028")),
            48 to Pair(BigDecimal("0.021362"), BigDecimal("0.056948")),
            54 to Pair(BigDecimal("0.021623"), BigDecimal("0.065283")),
            60 to Pair(BigDecimal("0.021827"), BigDecimal("0.073927"))
        ),
        "OPCIPRES" to mapOf(
            36 to Pair(BigDecimal("0.020501"), BigDecimal("0.041001")),
            48 to Pair(BigDecimal("0.021325"), BigDecimal("0.056689")),
            54 to Pair(BigDecimal("0.021576"), BigDecimal("0.064861")),
            60 to Pair(BigDecimal("0.021770"), BigDecimal("0.073298")),
            999 to Pair(BigDecimal("0.023390"), BigDecimal("0.076685"))
        ),
        "MASNOMINA" to mapOf(
            24 to Pair(BigDecimal("0.018687"), BigDecimal("0.026347")),
            36 to Pair(BigDecimal("0.020513"), BigDecimal("0.041051")),
            48 to Pair(BigDecimal("0.021367"), BigDecimal("0.056986")),
            60 to Pair(BigDecimal("0.021837"), BigDecimal("0.074045"))
        )
    )
}
