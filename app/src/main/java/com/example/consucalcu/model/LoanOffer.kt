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
    val rejectionReason: String? = null
) : Parcelable
