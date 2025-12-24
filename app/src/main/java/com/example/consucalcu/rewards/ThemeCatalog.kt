package com.example.consucalcu.rewards

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * THEME CATALOG
 * 
 * Catálogo completo de temas desbloqueables con paletas de colores premium.
 * Cada tema se desbloquea al alcanzar un nivel específico.
 */

object ThemeCatalog {
    
    // =================================================================================================
    // THEME COLORS DEFINITIONS
    // =================================================================================================
    
    private val DefaultColors = ThemeColors(
        primary = Color(0xFF0A84FF),
        primaryShadow = Color(0xFF0055CC),
        primaryGlow = Color(0xFF64B5F6),
        secondary = Color(0xFF00E676),
        secondaryShadow = Color(0xFF00C853),
        secondaryGlow = Color(0xFF69F0AE),
        accent = Color(0xFF7C4DFF),
        background = Color(0xFF0A0E27),
        surface = Color(0xFF1A1F3A),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF0A84FF), Color(0xFF00D4FF))
        )
    )
    
    private val OceanBreezeColors = ThemeColors(
        primary = Color(0xFF006994),
        primaryShadow = Color(0xFF003D5C),
        primaryGlow = Color(0xFF4FC3F7),
        secondary = Color(0xFF00BCD4),
        secondaryShadow = Color(0xFF0097A7),
        secondaryGlow = Color(0xFF80DEEA),
        accent = Color(0xFF26C6DA),
        background = Color(0xFF0A1929),
        surface = Color(0xFF1A2F3F),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF006994), Color(0xFF00BCD4), Color(0xFF4FC3F7))
        )
    )
    
    private val FirePhoenixColors = ThemeColors(
        primary = Color(0xFFFF6D00),
        primaryShadow = Color(0xFFE65100),
        primaryGlow = Color(0xFFFF9E40),
        secondary = Color(0xFFFF1744),
        secondaryShadow = Color(0xFFD50000),
        secondaryGlow = Color(0xFFFF5252),
        accent = Color(0xFFFFD600),
        background = Color(0xFF1A0A00),
        surface = Color(0xFF2D1A0F),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFF6D00), Color(0xFFFF1744), Color(0xFFFFD600))
        )
    )
    
    private val SakuraDreamColors = ThemeColors(
        primary = Color(0xFFE91E63),
        primaryShadow = Color(0xFFC2185B),
        primaryGlow = Color(0xFFF48FB1),
        secondary = Color(0xFF9C27B0),
        secondaryShadow = Color(0xFF7B1FA2),
        secondaryGlow = Color(0xFFCE93D8),
        accent = Color(0xFFFF4081),
        background = Color(0xFF1A0A1A),
        surface = Color(0xFF2D1A2D),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF4081))
        )
    )
    
    private val DiamondEliteColors = ThemeColors(
        primary = Color(0xFFB0BEC5),
        primaryShadow = Color(0xFF78909C),
        primaryGlow = Color(0xFFECEFF1),
        secondary = Color(0xFF00E5FF),
        secondaryShadow = Color(0xFF00B8D4),
        secondaryGlow = Color(0xFF84FFFF),
        accent = Color(0xFFE0F7FA),
        background = Color(0xFF0D1117),
        surface = Color(0xFF1C2128),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFB0BEC5), Color(0xFF00E5FF), Color(0xFFECEFF1))
        )
    )
    
    private val CosmicPurpleColors = ThemeColors(
        primary = Color(0xFF7C4DFF),
        primaryShadow = Color(0xFF651FFF),
        primaryGlow = Color(0xFFB388FF),
        secondary = Color(0xFFE040FB),
        secondaryShadow = Color(0xFFAA00FF),
        secondaryGlow = Color(0xFFEA80FC),
        accent = Color(0xFF00E5FF),
        background = Color(0xFF0A0A1A),
        surface = Color(0xFF1A1A2E),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF7C4DFF), Color(0xFFE040FB), Color(0xFF00E5FF))
        )
    )
    
    private val GoldLegendColors = ThemeColors(
        primary = Color(0xFFFFD700),
        primaryShadow = Color(0xFFB8860B),
        primaryGlow = Color(0xFFFFF176),
        secondary = Color(0xFFFFB300),
        secondaryShadow = Color(0xFFFF8F00),
        secondaryGlow = Color(0xFFFFD54F),
        accent = Color(0xFFFFEA00),
        background = Color(0xFF0A0A0A),
        surface = Color(0xFF1A1A1A),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFFFF176))
        )
    )
    
    private val EmeraldForestColors = ThemeColors(
        primary = Color(0xFF00C853),
        primaryShadow = Color(0xFF009624),
        primaryGlow = Color(0xFF69F0AE),
        secondary = Color(0xFF00E676),
        secondaryShadow = Color(0xFF00C853),
        secondaryGlow = Color(0xFFB9F6CA),
        accent = Color(0xFF76FF03),
        background = Color(0xFF0A1A0A),
        surface = Color(0xFF1A2D1A),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF00C853), Color(0xFF00E676), Color(0xFF76FF03))
        )
    )
    
    private val MidnightBlueColors = ThemeColors(
        primary = Color(0xFF1565C0),
        primaryShadow = Color(0xFF0D47A1),
        primaryGlow = Color(0xFF42A5F5),
        secondary = Color(0xFF1E88E5),
        secondaryShadow = Color(0xFF1565C0),
        secondaryGlow = Color(0xFF64B5F6),
        accent = Color(0xFF2196F3),
        background = Color(0xFF000A1A),
        surface = Color(0xFF0F1A2D),
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF42A5F5))
        )
    )
    
    // =================================================================================================
    // THEME CATALOG
    // =================================================================================================
    
    val ALL_THEMES = listOf(
        AppTheme(
            id = "default",
            name = "Azul Clásico",
            description = "El tema original de ConsuCalcu",
            unlockLevel = 1,
            emoji = "🔵",
            colors = DefaultColors,
            isPremium = false
        ),
        AppTheme(
            id = "ocean_breeze",
            name = "Ocean Breeze",
            description = "Profundidades del océano con toques turquesa",
            unlockLevel = 3,
            emoji = "🌊",
            colors = OceanBreezeColors,
            isPremium = false
        ),
        AppTheme(
            id = "fire_phoenix",
            name = "Fire Phoenix",
            description = "Llamas ardientes con energía explosiva",
            unlockLevel = 5,
            emoji = "🔥",
            colors = FirePhoenixColors,
            isPremium = false
        ),
        AppTheme(
            id = "sakura_dream",
            name = "Sakura Dream",
            description = "Flores de cerezo en un sueño mágico",
            unlockLevel = 7,
            emoji = "🌸",
            colors = SakuraDreamColors,
            isPremium = false
        ),
        AppTheme(
            id = "emerald_forest",
            name = "Emerald Forest",
            description = "Bosque esmeralda lleno de vida",
            unlockLevel = 9,
            emoji = "🌲",
            colors = EmeraldForestColors,
            isPremium = false
        ),
        AppTheme(
            id = "diamond_elite",
            name = "Diamond Elite",
            description = "Elegancia plateada con toques de diamante",
            unlockLevel = 10,
            emoji = "💎",
            colors = DiamondEliteColors,
            isPremium = true
        ),
        AppTheme(
            id = "midnight_blue",
            name = "Midnight Blue",
            description = "Azul medianoche profundo y misterioso",
            unlockLevel = 12,
            emoji = "🌙",
            colors = MidnightBlueColors,
            isPremium = false
        ),
        AppTheme(
            id = "cosmic_purple",
            name = "Cosmic Purple",
            description = "Galaxias púrpuras del espacio profundo",
            unlockLevel = 15,
            emoji = "🌌",
            colors = CosmicPurpleColors,
            isPremium = true
        ),
        AppTheme(
            id = "gold_legend",
            name = "Gold Legend",
            description = "Oro puro para verdaderas leyendas",
            unlockLevel = 20,
            emoji = "🏆",
            colors = GoldLegendColors,
            isPremium = true
        )
    )
    
    /**
     * Obtiene un tema por su ID
     */
    fun getThemeById(id: String): AppTheme? {
        return ALL_THEMES.find { it.id == id }
    }
    
    /**
     * Obtiene todos los temas desbloqueados hasta un nivel específico
     */
    fun getUnlockedThemes(currentLevel: Int): List<AppTheme> {
        return ALL_THEMES.filter { it.unlockLevel <= currentLevel }
    }
    
    /**
     * Obtiene el siguiente tema a desbloquear
     */
    fun getNextThemeToUnlock(currentLevel: Int): AppTheme? {
        return ALL_THEMES
            .filter { it.unlockLevel > currentLevel }
            .minByOrNull { it.unlockLevel }
    }
}
