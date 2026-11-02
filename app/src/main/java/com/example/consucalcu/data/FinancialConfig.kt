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
        val minTermMap: Map<Int, BigDecimal> = emptyMap(),
        val catTradByTerm: Map<Int, String> = emptyMap(),
        val tasaRefByTerm: Map<Int, Double> = emptyMap()
    ) {
        fun catTradForTerm(term: Int): String = catTradByTerm[term] ?: meta.catTrad
        fun tasaRefForTerm(term: Int): Double = tasaRefByTerm[term] ?: meta.tasaRef
        /** Tasa para mostrar, siempre con dos decimales: 2.40 %, no 2.4 %. */
        fun tasaRefLabel(term: Int): String =
            String.format(java.util.Locale.US, "%.2f%%", tasaRefForTerm(term))
    }

    data class AgreementConfig(
        val id: String,
        val label: String,
        val description: String,
        val paymentFrequency: String,
        val terms: List<Int>,
        val minAmount: BigDecimal,
        val maxAmount: BigDecimal,
        val amountStep: BigDecimal,
        val productKeys: Set<String>,
        val supportsConsolidation: Boolean = true,
        val enabled: Boolean = true
    ) {
        val isBiweekly: Boolean
            get() = paymentFrequency == PAYMENT_FREQUENCY_BIWEEKLY

        /**
         * Por monto se acepta cualquier cantidad desde el mínimo del convenio.
         * [amountStep] solo aplica al calcular por descuento, y [maxAmount] solo a
         * los cotizadores base: MAX, ESP y VIP no tienen monto máximo.
         */
        fun acceptsRequestedAmount(amount: BigDecimal): Boolean = amount >= minAmount
    }

    const val AGREEMENT_IMSS = "imss"
    const val AGREEMENT_IMSS_BIENESTAR = "imss_bienestar"
    const val PAYMENT_FREQUENCY_MONTHLY = "MENSUAL"
    const val PAYMENT_FREQUENCY_BIWEEKLY = "QUINCENAL"
    // Las variantes comparten marca; los consumidores usan este catálogo único.
    val IMSS_PRODUCTS_BY_BRAND = linkedMapOf(
        "OPC" to listOf("OPC", "OPC MAX", "OPC ESP"),
        "CSB" to listOf("CSB", "CSB ESP", "CSB VIP")
    )
    val IMSS_PRODUCT_KEYS = IMSS_PRODUCTS_BY_BRAND.values.flatten().toCollection(linkedSetOf())

    fun isPremiumProduct(productKey: String): Boolean =
        productKey.endsWith(" MAX") || productKey.endsWith(" ESP") || productKey.endsWith(" VIP")

    val PRODUCTS = mapOf(
        // Condiciones 2026-11-02. Los factores de OPC y CSB salen del CAT de cada
        // plazo (i = (1 + CAT)^(1/12) - 1 y anualidad a n meses) y están verificados
        // con simulaciones oficiales. Véase docs/calibracion-opc-csb-2026-09-08.md.
        "OPC" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.7774805"),
                54 to BigDecimal("38.2522771"),
                48 to BigDecimal("40.1857737"),
                36 to BigDecimal("46.3724411"),
                24 to BigDecimal("59.5762581"),
                12 to BigDecimal("100.9601550")
            ),
            meta = ProductMeta(2.66, "43.96%", "43.96%"),
            minGlobal = BigDecimal("5000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                60 to BigDecimal("5000"),
                54 to BigDecimal("5000"),
                48 to BigDecimal("10000"),
                36 to BigDecimal("10000"),
                24 to BigDecimal("10000"),
                12 to BigDecimal("25000")
            ),
            catTradByTerm = mapOf(
                60 to "43.96%", 54 to "43.96%", 48 to "43.96%",
                36 to "43.96%", 24 to "43.96%", 12 to "43.96%"
            ),
            tasaRefByTerm = mapOf(
                60 to 2.66, 54 to 2.66, 48 to 2.66,
                36 to 2.66, 24 to 2.66, 12 to 2.66
            )
        ),
        // Hereda las condiciones financieras de CSP Especial.
        // Condiciones 2026-11-02: CAT 37.16 %, unos 2.30 % mensual sin IVA.
        // Factor derivado del CAT (i = 1.3716^(1/12) - 1, anualidad a 60 meses);
        // reproduce las simulaciones oficiales. Sustituye a CAT 36.42 %.
        "OPC MAX" to ProductData(
            factorsTrad = mapOf(60 to BigDecimal("33.6034742")),
            meta = ProductMeta(2.30, "37.16%", "37.16%"),
            minGlobal = BigDecimal("50000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("50000"))
        ),
        // Condiciones 2026-11-02: CAT 35.31 %, equivalente a 2.20 % mensual sin
        // IVA. Sustituye a CAT 34.21 %; el factor sale de la plataforma de origen.
        "OPC ESP" to ProductData(
            factorsTrad = mapOf(60 to BigDecimal("32.737765")),
            meta = ProductMeta(2.20, "35.31%", "35.31%"),
            minGlobal = BigDecimal("75000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("75000"))
        ),
        "CSB" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("34.8749909"),
                54 to BigDecimal("36.4021171"),
                48 to BigDecimal("38.8488146"),
                36 to BigDecimal("45.1213316"),
                24 to BigDecimal("58.9263198"),
                12 to BigDecimal("100.3478859")
            ),
            meta = ProductMeta(2.45, "39.88%", "39.88%"),
            minGlobal = BigDecimal("30000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("35000"),
                48 to BigDecimal("35000"),
                36 to BigDecimal("40000"),
                24 to BigDecimal("40000"),
                12 to BigDecimal("45000")
            ),
            catTradByTerm = mapOf(
                60 to "39.88%", 54 to "39.88%", 48 to "40.92%",
                36 to "40.92%", 24 to "42.26%", 12 to "42.26%"
            ),
            tasaRefByTerm = mapOf(
                60 to 2.45, 54 to 2.45, 48 to 2.50,
                36 to 2.50, 24 to 2.57, 12 to 2.57
            )
        ),
        // Condiciones 2026-11-02: CAT 33.48 %, unos 2.10 % mensual sin IVA.
        // Factor derivado del CAT (i = 1.3348^(1/12) - 1, anualidad a 60 meses);
        // reproduce las simulaciones oficiales. Sustituye a CAT 32.94 %.
        "CSB ESP" to ProductData(
            factorsTrad = mapOf(60 to BigDecimal("31.8810807")),
            meta = ProductMeta(2.10, "33.48%", "33.48%"),
            minGlobal = BigDecimal("100000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("100000"))
        ),
        "CSB VIP" to ProductData(
            // Factor de la plataforma de origen (2026-11-02): 0.030954185 por
            // peso, CAT 31.5 %. Reproduce el Excel 2026-09-08 y las 13
            // simulaciones oficiales, igual que 0.0309541837 derivado del CAT.
            factorsTrad = mapOf(60 to BigDecimal("30.954185")),
            meta = ProductMeta(1.99, "31.50%", "31.50%"),
            minGlobal = BigDecimal("150000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("150000"))
        ),
        // Convenio 4789: CSB IMSS Bienestar / CSB_IMSSBIENESTAR_EFEC_EPAG_QN_V001.
        // Los factores son pago quincenal por cada $1,000 financiados y fueron
        // calculados directamente de la tabla oficial del cotizador.
        "CONSUBANCO IMSS BIENESTAR" to ProductData(
            factorsTrad = mapOf(
                48 to BigDecimal("24.90667"),
                42 to BigDecimal("26.01917"),
                36 to BigDecimal("27.83246"),
                30 to BigDecimal("30.20626"),
                24 to BigDecimal("34.16667"),
                18 to BigDecimal("40.71585"),
                12 to BigDecimal("54.27686")
            ),
            retention = RetentionRule(emptyMap(), BigDecimal("9999999")),
            meta = ProductMeta(45.00, "56.2%", "56.2%"),
            minGlobal = BigDecimal("5000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                48 to BigDecimal("5000"),
                42 to BigDecimal("5000"),
                36 to BigDecimal("5000"),
                30 to BigDecimal("5000"),
                24 to BigDecimal("5000"),
                18 to BigDecimal("5000"),
                12 to BigDecimal("5000")
            ),
            catTradByTerm = mapOf(
                48 to "56.2%",
                42 to "56.2%",
                36 to "57.1%",
                30 to "57.1%",
                24 to "58.0%",
                18 to "58.0%",
                12 to "58.0%"
            ),
            tasaRefByTerm = mapOf(
                48 to 45.00,
                42 to 45.00,
                36 to 45.60,
                30 to 45.60,
                24 to 46.20,
                18 to 46.20,
                12 to 46.20
            )
        )
    )
    val STANDARD_TERMS = listOf("12", "18", "24", "36", "48", "54", "60")

    val AGREEMENTS: Map<String, AgreementConfig> = mapOf(
        AGREEMENT_IMSS to AgreementConfig(
            id = AGREEMENT_IMSS,
            label = "IMSS",
            description = "Convenios actuales",
            paymentFrequency = PAYMENT_FREQUENCY_MONTHLY,
            terms = STANDARD_TERMS.map(String::toInt),
            minAmount = BigDecimal("5000"),
            maxAmount = BigDecimal("1000000"),
            amountStep = BigDecimal("100"),
            productKeys = IMSS_PRODUCT_KEYS
        ),
        AGREEMENT_IMSS_BIENESTAR to AgreementConfig(
            id = AGREEMENT_IMSS_BIENESTAR,
            label = "IMSS Bienestar",
            description = "Pago quincenal · Convenio 4789",
            paymentFrequency = PAYMENT_FREQUENCY_BIWEEKLY,
            terms = listOf(12, 18, 24, 30, 36, 42, 48),
            minAmount = BigDecimal("5000"),
            maxAmount = BigDecimal("1000000"),
            amountStep = BigDecimal("500"),
            productKeys = setOf("CONSUBANCO IMSS BIENESTAR"),
            supportsConsolidation = false,
            enabled = false
        )
    )

    val ACTIVE_AGREEMENTS: List<AgreementConfig>
        get() = AGREEMENTS.values.filter { it.enabled }

    fun agreementFor(id: String, includeDisabled: Boolean = false): AgreementConfig {
        val agreement = AGREEMENTS[id]
        return if (agreement != null && (agreement.enabled || includeDisabled)) {
            agreement
        } else {
            AGREEMENTS.getValue(AGREEMENT_IMSS)
        }
    }

    val INSURANCE_FACTORS = mapOf(
        "CSB" to mapOf(
            12 to Pair(BigDecimal("0.01242424"), BigDecimal("0.01242424")),
            24 to Pair(BigDecimal("0.01791518"), BigDecimal("0.02524355")),
            36 to Pair(BigDecimal("0.01957399"), BigDecimal("0.03893088")),
            48 to Pair(BigDecimal("0.02036369"), BigDecimal("0.05366801")),
            54 to Pair(BigDecimal("0.02060137"), BigDecimal("0.06129260")),
            60 to Pair(BigDecimal("0.02080281"), BigDecimal("0.06932477"))
        ),
        "CSB_VIP" to mapOf(
            60 to Pair(BigDecimal("0.02056227"), BigDecimal("0.06676468"))
        ),
        "CSB_ESP" to mapOf(
            60 to Pair(BigDecimal("0.02334237"), BigDecimal("0.07618676"))
        ),
        // OPC base conserva el esquema de seguro de CSP, que cubre los seis plazos.
        "OPC" to mapOf(
            12 to Pair(BigDecimal("0.01242424"), BigDecimal("0.01242424")),
            24 to Pair(BigDecimal("0.01791518"), BigDecimal("0.02524355")),
            36 to Pair(BigDecimal("0.01966443"), BigDecimal("0.03929658")),
            48 to Pair(BigDecimal("0.02048595"), BigDecimal("0.05452240")),
            54 to Pair(BigDecimal("0.02074001"), BigDecimal("0.06251714")),
            60 to Pair(BigDecimal("0.02093617"), BigDecimal("0.07078555"))
        ),
        "OPC_MAX" to mapOf(
            60 to Pair(BigDecimal("0.02346923"), BigDecimal("0.07753356"))
        ),
        "OPC_ESP" to mapOf(
            60 to Pair(BigDecimal("0.02338957"), BigDecimal("0.07668513"))
        )
    )
}
