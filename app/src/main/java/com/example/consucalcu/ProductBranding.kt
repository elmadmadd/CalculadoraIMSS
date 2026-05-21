package com.example.consucalcu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Sistema de identidad visual por producto financiero.
 * Cada financiera tiene su propio esquema de colores para diferenciación visual.
 */
object ProductBranding {

    // ========== CONSUBANCO ==========
    object Consubanco {
        val primary = Color(0xFF16A34A)      // Verde esmeralda (Green)
        val primaryDark = Color(0xFF15803D)
        val accent = Color(0xFF4ADE80)
        val shadow = Color(0xFF15803D).copy(alpha = 0.4f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF14532D))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== CONSUPAGO ==========
    object Consupago {
        val primary = Color(0xFFEA580C)      // Naranja fuerte (Orange)
        val primaryDark = Color(0xFFC2410C)
        val accent = Color(0xFFFB923C)
        val shadow = Color(0xFFC2410C).copy(alpha = 0.4f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF9A3412))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== OPCIPRES ==========
    object Opcipres {
        val primary = Color(0xFFD97706)      // Amarillo / Ámbar dorado (Yellow)
        val primaryDark = Color(0xFFB45309)
        val accent = Color(0xFFFBBF24)
        val shadow = Color(0xFFB45309).copy(alpha = 0.4f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF78350F))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== MASNOMINA ==========
    object Masnomina {
        val primary = Color(0xFFDC2626)      // Rojo (Red)
        val primaryDark = Color(0xFFB91C1C)
        val accent = Color(0xFFF87171)
        val shadow = Color(0xFFB91C1C).copy(alpha = 0.4f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF7F1D1D))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== TASA PREFERENCIAL (Override para ofertas especiales) ==========
    object Preferencial {
        val primary = Color(0xFF7B1FA2)      // Morado premium
        val primaryDark = Color(0xFF4A0072)
        val accent = Color(0xFFE040FB)
        val gold = Color(0xFFFFD700)
        val shadow = Color(0xFF7B1FA2).copy(alpha = 0.5f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(Color(0xFF9C27B0), primary, primaryDark)
            )
        
        val gradientPremium: Brush
            get() = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE040FB),
                    Color(0xFF9C27B0),
                    Color(0xFF7B1FA2),
                    Color(0xFF4A0072)
                )
            )
    }

    /**
     * Obtiene el esquema de colores para un producto específico.
     * 🎨 ENHANCED: Incluye colores adicionales para efectos visuales premium
     */
    data class ProductColors(
        val primary: Color,
        val primaryDark: Color,
        val accent: Color,
        val shadow: Color,
        val gradient: Brush,
        val gradientVertical: Brush,
        // ✨ NUEVO: Colores adicionales para efectos premium
        val glow: Color = accent.copy(alpha = 0.5f),
        val surface: Color = primary.copy(alpha = 0.1f),
        val onPrimary: Color = Color.White,
        val shimmer: Color = accent.copy(alpha = 0.3f)
    )

    fun getColors(productKey: String, isPreferencial: Boolean = false): ProductColors {
        return if (isPreferencial) {
                ProductColors(
                    primary = Preferencial.primary,
                    primaryDark = Preferencial.primaryDark,
                    accent = Preferencial.accent,
                    shadow = Preferencial.shadow,
                    gradient = Preferencial.gradient,
                    gradientVertical = Preferencial.gradientPremium
                )
            } else {
                when (productKey.uppercase()) {
                    "CONSUBANCO" -> ProductColors(
                        primary = Consubanco.primary,
                        primaryDark = Consubanco.primaryDark,
                        accent = Consubanco.accent,
                        shadow = Consubanco.shadow,
                        gradient = Consubanco.gradient,
                        gradientVertical = Consubanco.gradientVertical
                    )
                    "CONSUPAGO" -> ProductColors(
                        primary = Consupago.primary,
                        primaryDark = Consupago.primaryDark,
                        accent = Consupago.accent,
                        shadow = Consupago.shadow,
                        gradient = Consupago.gradient,
                        gradientVertical = Consupago.gradientVertical
                    )
                    "OPCIPRES" -> ProductColors(
                        primary = Opcipres.primary,
                        primaryDark = Opcipres.primaryDark,
                        accent = Opcipres.accent,
                        shadow = Opcipres.shadow,
                        gradient = Opcipres.gradient,
                        gradientVertical = Opcipres.gradientVertical
                    )
                    "MASNOMINA" -> ProductColors(
                        primary = Masnomina.primary,
                        primaryDark = Masnomina.primaryDark,
                        accent = Masnomina.accent,
                        shadow = Masnomina.shadow,
                        gradient = Masnomina.gradient,
                        gradientVertical = Masnomina.gradientVertical
                    )
                    else -> ProductColors(
                        primary = Consubanco.primary,
                        primaryDark = Consubanco.primaryDark,
                        accent = Consubanco.accent,
                        shadow = Consubanco.shadow,
                        gradient = Consubanco.gradient,
                        gradientVertical = Consubanco.gradientVertical
                    )
                }
            }
    }

    /**
     * Obtiene el icono/emoji representativo del producto.
     */
    fun getProductEmoji(productKey: String): String {
        return when (productKey.uppercase()) {
            "CONSUBANCO" -> "🏦"
            "CONSUPAGO" -> "💳"
            "OPCIPRES" -> "🎯"
            "MASNOMINA" -> "💼"
            else -> "📊"
        }
    }
}
