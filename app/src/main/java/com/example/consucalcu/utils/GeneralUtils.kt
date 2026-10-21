package com.example.consucalcu.utils



/**
 * Utilidades generales de validación y transformación de datos.
 */
object GeneralUtils {

    /**
     * Valida una CURP mexicana básica (formato: 4 letras, 6 números, 6 letras/números, 2 números).
     */
    fun isValidCURP(curp: String): Boolean {
        val regex = "^[A-Z]{4}[0-9]{6}[A-Z]{6}[0-9]{2}$".toRegex()
        return regex.matches(curp.uppercase())
    }

    /**
     * Valida un RFC mexicano con homoclave (13 caracteres para personas físicas).
     */
    fun isValidRFC(rfc: String): Boolean {
        val regex = "^[A-Z&]{4}[0-9]{6}[A-Z0-9]{3}$".toRegex()
        return regex.matches(rfc.uppercase())
    }

    /**
     * Valida una CLABE Interbancaria (18 dígitos).
     */
    fun isValidCLABE(clabe: String): Boolean {
        return (clabe.length == 18) && clabe.all { it.isDigit() }
    }

    /**
     * Valida un correo electrónico.
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Extrae la fecha de nacimiento de una CURP y devuelve un String "dd/mm/aaaa".
     */
    fun extractBirthDateFromCURP(curp: String): String? {
        if (curp.length < 10) return null
        val datePart = curp.substring(4, 10)
        val yearShort = datePart.substring(0, 2)
        val month = datePart.substring(2, 4)
        val day = datePart.substring(4, 6)

        // Asumir siglo basado en el año (00-24 -> 2000, 25-99 -> 1900)
        val year = if (yearShort.toInt() <= 24) "20$yearShort" else "19$yearShort"
        return "$day/$month/$year"
    }
}
