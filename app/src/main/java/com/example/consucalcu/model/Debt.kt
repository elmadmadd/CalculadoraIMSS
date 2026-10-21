package com.example.consucalcu.model

data class Debt(
    val id: Long = System.currentTimeMillis(),
    val product: String = "CSB",
    val balance: String = "",
    val payment: String = "",
    val rate: String = "",
    val originalTerm: String = "60",
    val paymentsMade: String = ""
)
