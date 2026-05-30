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
        val primary = Color(0xFF3B5B47)      // Verde bosque apagado (Sage/Forest)
        val primaryDark = Color(0xFF2B4234)
        val accent = Color(0xFF5B7F68)
        val shadow = Color(0xFF2B4234).copy(alpha = 0.2f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF1E3025))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== CONSUPAGO ==========
    object Consupago {
        val primary = Color(0xFFB05E49)      // Terracota apagado (Rust)
        val primaryDark = Color(0xFF8C4736)
        val accent = Color(0xFFCC8472)
        val shadow = Color(0xFF8C4736).copy(alpha = 0.2f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF5C2D22))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== OPCIPRES ==========
    object Opcipres {
        val primary = Color(0xFF9E815E)      // Ocre / Bronce viejo
        val primaryDark = Color(0xFF7A6348)
        val accent = Color(0xFFC0A482)
        val shadow = Color(0xFF7A6348).copy(alpha = 0.2f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF54422F))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== MASNOMINA ==========
    object Masnomina {
        val primary = Color(0xFF8B3A3A)      // Burdeos / Vino apagado
        val primaryDark = Color(0xFF6B2A2A)
        val accent = Color(0xFFAB5E5E)
        val shadow = Color(0xFF6B2A2A).copy(alpha = 0.2f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(primary, primaryDark, Color(0xFF4F2020))
            )
        
        val gradientVertical: Brush
            get() = Brush.verticalGradient(
                colors = listOf(accent, primary, primaryDark)
            )
    }

    // ========== TASA PREFERENCIAL (Override para ofertas especiales) ==========
    object Preferencial {
        val primary = Color(0xFF5E4E6B)      // Berenjena / Lavanda profundo
        val primaryDark = Color(0xFF453850)
        val accent = Color(0xFF827092)
        val gold = Color(0xFF8E8A7F)         // Acento champagne
        val shadow = Color(0xFF453850).copy(alpha = 0.2f)
        
        val gradient: Brush
            get() = Brush.horizontalGradient(
                colors = listOf(Color(0xFF705F7E), primary, primaryDark)
            )
        
        val gradientPremium: Brush
            get() = Brush.linearGradient(
                colors = listOf(
                    accent,
                    Color(0xFF705F7E),
                    primary,
                    primaryDark
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
