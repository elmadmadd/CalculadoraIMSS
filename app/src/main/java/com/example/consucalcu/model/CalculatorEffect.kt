package com.example.consucalcu.model

sealed interface CalculatorEffect {
    data class PlaySound(val soundName: String) : CalculatorEffect
    data class TriggerHaptics(val pattern: LongArray) : CalculatorEffect
    data class GenerateTicket(
        val offer: LoanOffer,
        val clientData: ClientData,
        val userName: String
    ) : CalculatorEffect
}
