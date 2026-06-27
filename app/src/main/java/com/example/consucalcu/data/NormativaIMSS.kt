package com.example.consucalcu.data

import com.example.consucalcu.model.TipoPension
import com.example.consucalcu.model.ResultadoSeguro

object NormativaIMSS {
    fun calcularReglaSeguro(
        edad: Int,
        plazoMeses: Int,
        tipoPension: TipoPension,
        esNuevo: Boolean,
        antiguedadMeses: Int
    ): ResultadoSeguro {
        val plazoAnios = plazoMeses / 12.0
        val edadFinal = edad + plazoAnios

        // Validación Global Actualizada:
        // 1. Si edadFinal > 85, es RECHAZO absouto.
        if (edadFinal > 85) return ResultadoSeguro.RECHAZO
        
        // 2. Si edadFinal está entre 80.01 y 85 (TODO TIPO DE PENSIÓN):
        // "Se puede considerar una edad máxima de 85 años... si el cliente realiza el pago del seguro por el primer año"
        if (edadFinal > 80) return ResultadoSeguro.UN_ANIO

        return when (tipoPension) {
            TipoPension.INVALIDEZ -> {
                // Caso INVALIDEZ
                if (edad in 51..70) {
                    // Si edad entre 51-70: Cobrar UN_ANIO si es nuevo o antigüedad <= 24 meses
                    if (esNuevo || antiguedadMeses <= 24) ResultadoSeguro.UN_ANIO else ResultadoSeguro.NINGUNO
                } else {
                    // Si edad fuera de rango pero (edad + plazo/12) > 75: Cobrar UN_ANIO
                    if (edadFinal > 75) ResultadoSeguro.UN_ANIO else ResultadoSeguro.NINGUNO
                }
            }
            TipoPension.VEJEZ -> {
                // Caso VEJEZ
                if (edad in 66..75) {
                    if (edadFinal > 75) {
                        // Si (edad + plazo/12) > 75: Cobrar PLAZO_COMPLETO
                        ResultadoSeguro.PLAZO_COMPLETO
                    } else if (esNuevo || antiguedadMeses <= 24) {
                        // Si no excede 75 y (es nuevo o antigüedad <= 24): Cobrar UN_ANIO
                        ResultadoSeguro.UN_ANIO
                    } else {
                        // Si no excede 75 y antigüedad > 24: NINGUNO
                        ResultadoSeguro.NINGUNO
                    }
                } else if (edad >= 76) {
                    // Si edad >= 76: NINGUNO (siempre que no viole la regla global de 80 años)
                    ResultadoSeguro.NINGUNO
                } else {
                    ResultadoSeguro.NINGUNO
                }
            }
            TipoPension.OTRO -> ResultadoSeguro.NINGUNO
        }
    }
}
