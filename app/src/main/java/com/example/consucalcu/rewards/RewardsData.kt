package com.example.consucalcu.rewards

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * REWARDS SYSTEM - DATA MODELS
 * 
 * Define todos los tipos de recompensas desbloqueables y sus configuraciones.
 * Sistema progresivo basado en niveles del usuario.
 */

// =================================================================================================
// THEME DEFINITIONS
// =================================================================================================

/**
 * Representa un tema de color desbloqueable
 */
data class AppTheme(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val colors: ThemeColors,
    val isPremium: Boolean = false
)

/**
 * Paleta de colores para un tema
 */
data class ThemeColors(
    val primary: Color,
    val primaryShadow: Color,
    val primaryGlow: Color,
    val secondary: Color,
    val secondaryShadow: Color,
    val secondaryGlow: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val gradient: Brush
)

// =================================================================================================
// PARTICLE EFFECTS
// =================================================================================================

/**
 * Efectos de partículas desbloqueables
 */
data class ParticleEffect(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val colors: List<Color>,
    val particleCount: Int = 50
)

// =================================================================================================
// AVATARS
// =================================================================================================

/**
 * Avatares/Iconos de perfil desbloqueables
 */
data class Avatar(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val gradient: Brush
)

// =================================================================================================
// BADGES
// =================================================================================================

/**
 * Insignias/Logros coleccionables
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val rarity: BadgeRarity
)

enum class BadgeRarity {
    COMMON,    // Bronce
    RARE,      // Plata
    EPIC,      // Oro
    LEGENDARY  // Diamante
}

// =================================================================================================
// CARD STYLES
// =================================================================================================

/**
 * Estilos de tarjetas para resultados
 */
data class CardStyle(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val hasGlassmorphism: Boolean = false,
    val hasShimmer: Boolean = false,
    val hasGlow: Boolean = false,
    val borderWidth: Float = 1f
)

// =================================================================================================
// SOUND PACKS
// =================================================================================================

/**
 * Paquetes de sonidos premium
 */
data class SoundPack(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val emoji: String,
    val sounds: Map<String, String> // key: evento, value: nombre del archivo
)

// =================================================================================================
// UNIFIED REWARDS MODEL
// =================================================================================================

/**
 * Modelo unificado de recompensas desbloqueadas por el usuario
 */
data class UnlockedRewards(
    val themes: Set<String> = setOf("default"),
    val particles: Set<String> = setOf("none"),
    val avatars: Set<String> = setOf("basic"),
    val sounds: Set<String> = setOf("default"),
    val badges: Set<String> = emptySet(),
    val cardStyles: Set<String> = setOf("flat")
)

/**
 * Recompensas actualmente seleccionadas por el usuario
 */
data class ActiveRewards(
    val currentTheme: String = "default",
    val currentParticle: String = "none",
    val currentAvatar: String = "basic",
    val currentSound: String = "default",
    val currentCardStyle: String = "flat"
)

/**
 * Estado completo del sistema de recompensas
 */
data class RewardsState(
    val unlocked: UnlockedRewards = UnlockedRewards(),
    val active: ActiveRewards = ActiveRewards(),
    val newUnlocks: List<String> = emptyList() // IDs de recompensas recién desbloqueadas
)
