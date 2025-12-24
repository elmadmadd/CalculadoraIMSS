package com.example.consucalcu.rewards

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * PARTICLE EFFECTS CATALOG
 * 
 * Catálogo de efectos de partículas desbloqueables.
 * Cada efecto tiene colores y configuraciones únicas.
 */

object ParticleCatalog {
    
    val ALL_PARTICLES = listOf(
        ParticleEffect(
            id = "none",
            name = "Sin Efecto",
            description = "Sin animaciones de partículas",
            unlockLevel = 1,
            emoji = "⚪",
            colors = listOf(Color.Transparent)
        ),
        ParticleEffect(
            id = "stars",
            name = "Estrellas",
            description = "Estrellas brillantes cayendo suavemente",
            unlockLevel = 2,
            emoji = "⭐",
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFF176),
                Color(0xFFFFEA00)
            ),
            particleCount = 30
        ),
        ParticleEffect(
            id = "coins",
            name = "Monedas",
            description = "Lluvia de monedas doradas",
            unlockLevel = 4,
            emoji = "💰",
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFB300),
                Color(0xFFFF8F00)
            ),
            particleCount = 40
        ),
        ParticleEffect(
            id = "fireworks",
            name = "Fuegos Artificiales",
            description = "Explosión de colores festivos",
            unlockLevel = 6,
            emoji = "🎆",
            colors = listOf(
                Color(0xFFFF1744),
                Color(0xFF00E676),
                Color(0xFF0A84FF),
                Color(0xFFFFD700),
                Color(0xFFE040FB)
            ),
            particleCount = 60
        ),
        ParticleEffect(
            id = "shooting_stars",
            name = "Estrellas Fugaces",
            description = "Estrellas cruzando el cielo",
            unlockLevel = 8,
            emoji = "🌟",
            colors = listOf(
                Color(0xFF00E5FF),
                Color(0xFF64B5F6),
                Color(0xFFB388FF)
            ),
            particleCount = 25
        ),
        ParticleEffect(
            id = "diamonds",
            name = "Diamantes",
            description = "Diamantes brillantes flotando",
            unlockLevel = 12,
            emoji = "💎",
            colors = listOf(
                Color(0xFFB0BEC5),
                Color(0xFF00E5FF),
                Color(0xFFECEFF1)
            ),
            particleCount = 35
        ),
        ParticleEffect(
            id = "golden_flames",
            name = "Llamas Doradas",
            description = "Fuego dorado ascendente",
            unlockLevel = 18,
            emoji = "🔥",
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFF6D00),
                Color(0xFFFFEA00)
            ),
            particleCount = 50
        )
    )
    
    fun getParticleById(id: String): ParticleEffect? {
        return ALL_PARTICLES.find { it.id == id }
    }
    
    fun getUnlockedParticles(currentLevel: Int): List<ParticleEffect> {
        return ALL_PARTICLES.filter { it.unlockLevel <= currentLevel }
    }
}

/**
 * AVATAR CATALOG
 * 
 * Catálogo de avatares/iconos de perfil desbloqueables.
 */

object AvatarCatalog {
    
    val ALL_AVATARS = listOf(
        Avatar(
            id = "basic",
            name = "Usuario",
            description = "Avatar básico de usuario",
            unlockLevel = 1,
            emoji = "👤",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF78909C), Color(0xFFB0BEC5))
            )
        ),
        Avatar(
            id = "executive",
            name = "Ejecutivo",
            description = "Profesional de negocios",
            unlockLevel = 3,
            emoji = "💼",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
            )
        ),
        Avatar(
            id = "wolf",
            name = "Lobo de Wall Street",
            description = "Maestro de las finanzas",
            unlockLevel = 5,
            emoji = "🦁",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFF6D00), Color(0xFFFFB300))
            )
        ),
        Avatar(
            id = "strategist",
            name = "Estratega",
            description = "Planificador experto",
            unlockLevel = 7,
            emoji = "🎯",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF00C853), Color(0xFF69F0AE))
            )
        ),
        Avatar(
            id = "king",
            name = "Rey de las Finanzas",
            description = "Dominio total del mercado",
            unlockLevel = 10,
            emoji = "👑",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD700), Color(0xFFFFF176))
            )
        ),
        Avatar(
            id = "visionary",
            name = "Visionario",
            description = "Ve más allá del horizonte",
            unlockLevel = 15,
            emoji = "🚀",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF))
            )
        ),
        Avatar(
            id = "legend",
            name = "Leyenda Dorada",
            description = "Estatus legendario alcanzado",
            unlockLevel = 20,
            emoji = "🏆",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFFFF176))
            )
        )
    )
    
    fun getAvatarById(id: String): Avatar? {
        return ALL_AVATARS.find { it.id == id }
    }
    
    fun getUnlockedAvatars(currentLevel: Int): List<Avatar> {
        return ALL_AVATARS.filter { it.unlockLevel <= currentLevel }
    }
}

/**
 * BADGE CATALOG
 * 
 * Catálogo de insignias/logros coleccionables.
 */

object BadgeCatalog {
    
    val ALL_BADGES = listOf(
        Badge(
            id = "first_step",
            name = "Primer Paso",
            description = "Comenzaste tu viaje financiero",
            unlockLevel = 2,
            emoji = "🥉",
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "calculator_expert",
            name = "Calculador Experto",
            description = "Dominas las calculaciones básicas",
            unlockLevel = 5,
            emoji = "🥈",
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "financial_master",
            name = "Maestro Financiero",
            description = "Conocimiento financiero avanzado",
            unlockLevel = 10,
            emoji = "🥇",
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "elite_diamond",
            name = "Elite Diamond",
            description = "Alcanzaste el nivel élite",
            unlockLevel = 15,
            emoji = "💎",
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            id = "immortal_legend",
            name = "Leyenda Inmortal",
            description = "Estatus legendario permanente",
            unlockLevel = 20,
            emoji = "👑",
            rarity = BadgeRarity.LEGENDARY
        )
    )
    
    fun getBadgeById(id: String): Badge? {
        return ALL_BADGES.find { it.id == id }
    }
    
    fun getUnlockedBadges(currentLevel: Int): List<Badge> {
        return ALL_BADGES.filter { it.unlockLevel <= currentLevel }
    }
}

/**
 * CARD STYLE CATALOG
 * 
 * Catálogo de estilos de tarjetas para resultados.
 */

object CardStyleCatalog {
    
    val ALL_CARD_STYLES = listOf(
        CardStyle(
            id = "flat",
            name = "Plana",
            description = "Diseño minimalista y limpio",
            unlockLevel = 1,
            emoji = "📄",
            hasGlassmorphism = false,
            hasShimmer = false,
            hasGlow = false,
            borderWidth = 1f
        ),
        CardStyle(
            id = "glassmorphism",
            name = "Cristal",
            description = "Efecto de vidrio esmerilado",
            unlockLevel = 5,
            emoji = "💳",
            hasGlassmorphism = true,
            hasShimmer = false,
            hasGlow = false,
            borderWidth = 1.5f
        ),
        CardStyle(
            id = "crystal",
            name = "Cristalina",
            description = "Reflejos y transparencias",
            unlockLevel = 10,
            emoji = "💎",
            hasGlassmorphism = true,
            hasShimmer = true,
            hasGlow = false,
            borderWidth = 2f
        ),
        CardStyle(
            id = "golden",
            name = "Dorada",
            description = "Brillo dorado premium",
            unlockLevel = 15,
            emoji = "🏆",
            hasGlassmorphism = false,
            hasShimmer = true,
            hasGlow = true,
            borderWidth = 2.5f
        ),
        CardStyle(
            id = "holographic",
            name = "Holográfica",
            description = "Efecto holográfico animado",
            unlockLevel = 20,
            emoji = "🌟",
            hasGlassmorphism = true,
            hasShimmer = true,
            hasGlow = true,
            borderWidth = 3f
        )
    )
    
    fun getCardStyleById(id: String): CardStyle? {
        return ALL_CARD_STYLES.find { it.id == id }
    }
    
    fun getUnlockedCardStyles(currentLevel: Int): List<CardStyle> {
        return ALL_CARD_STYLES.filter { it.unlockLevel <= currentLevel }
    }
}
