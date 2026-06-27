package com.example.consucalcu.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientData(
    val name: String = "",
    val nss: String = "",
    val curp: String = "",
    val rfc: String = "",
    val email: String = "",
    val phone: String = "",
    val clabe: String = "",
    val bank: String = "",
    val age: String = "",
    val pensionType: String = "",
    val agreement: String = "IMSS",
    val disease: String = "NINGUNA",
    val diseaseDate: String = "",
    
    // Tipo de trámite
    val transactionType: String = "NUEVO", // NUEVO, CNCA, 2ª DISP, LCOM
    val creditId: String = "", // ID del crédito a liquidar
    val discountAmount: String = "", // Descuento
    val numDiscounts: String = "", // Número de descuentos
    val liquidatingBrand: String = "", // Marca que liquida
    
    // Datos del crédito nuevo (se llenarán automáticamente desde la oferta)
    val isCommissionable: String = "SÍ",
    
    // Referencias
    val ref1Name: String = "",
    val ref1Phone: String = "",
    val ref2Name: String = "",
    val ref2Phone: String = ""
) : Parcelable
