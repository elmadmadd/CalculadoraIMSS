package com.example.consucalcu.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class LoanOffer(
    val product: String,
    val amount: BigDecimal,
    val monthlyPayment: BigDecimal,
    val term: Int,
    val mainValue: BigDecimal = BigDecimal.ZERO,
    val mainLabel: String = "",
    val reasons: List<String> = emptyList(),
    val isBest: Boolean = false,
    val cat: String = "0.0%",
    val tipoTasa: String = "TRADICIONAL",
    val rejectionReason: String? = null,
    val agreementId: String = "imss",
    val agreementLabel: String = "",
    val paymentFrequency: String = "MENSUAL"
) : Parcelable {
    val paymentLabel: String
        get() = if (paymentFrequency == "QUINCENAL") "PAGO QUINCENAL" else "MENSUALIDAD"

    val shortPaymentLabel: String
        get() = if (paymentFrequency == "QUINCENAL") "QUINCENAL" else "MENSUAL"

    val paymentNoun: String
        get() = if (paymentFrequency == "QUINCENAL") "Quincenal" else "Mensual"

    val periodLabel: String
        get() = if (paymentFrequency == "QUINCENAL") "Quincenas" else "Meses"

    val periodCount: Int
        get() = if (paymentFrequency == "QUINCENAL") term * 2 else term

    val periodsPerYear: Int
        get() = if (paymentFrequency == "QUINCENAL") 24 else 12

    /** Total del crédito: siempre la cuota ya redondeada por el número de pagos. */
    val totalToPay: BigDecimal
        get() = monthlyPayment.multiply(BigDecimal(periodCount))

    val termDisplay: String
        get() = if (paymentFrequency == "QUINCENAL") {
            "$term meses · $periodCount quincenas"
        } else {
            "$term meses"
        }
}
