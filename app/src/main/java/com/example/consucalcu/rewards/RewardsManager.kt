package com.example.consucalcu.rewards

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * REWARDS SYSTEM MANAGER
 * 
 * Sistema central de gestión de recompensas.
 * Maneja el desbloqueo, activación y persistencia de todas las recompensas.
 */

class RewardsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("rewards_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    private val _rewardsState = MutableStateFlow(loadRewardsState())
    val rewardsState: StateFlow<RewardsState> = _rewardsState.asStateFlow()
    
    // =================================================================================================
    // INITIALIZATION
    // =================================================================================================
    
    init {
        // Asegurar que el tema por defecto esté desbloqueado
        if (_rewardsState.value.unlocked.themes.isEmpty()) {
            unlockReward(RewardType.THEME, "default")
        }
    }
    
    // =================================================================================================
    // UNLOCK LOGIC
    // =================================================================================================
    
    /**
     * Verifica y desbloquea recompensas basadas en el nivel actual del usuario
     */
    fun checkAndUnlockRewards(currentLevel: Int): List<String> {
        val newUnlocks = mutableListOf<String>()
        
        // Verificar temas
        ThemeCatalog.ALL_THEMES.forEach { theme ->
            if (theme.unlockLevel == currentLevel && 
                !_rewardsState.value.unlocked.themes.contains(theme.id)) {
                unlockReward(RewardType.THEME, theme.id)
                newUnlocks.add("Tema: ${theme.name}")
            }
        }
        
        // Verificar efectos de partículas
        ParticleCatalog.ALL_PARTICLES.forEach { particle ->
            if (particle.unlockLevel == currentLevel && 
                !_rewardsState.value.unlocked.particles.contains(particle.id)) {
                unlockReward(RewardType.PARTICLE, particle.id)
                newUnlocks.add("Efecto: ${particle.name}")
            }
        }
        
        // Verificar avatares
        AvatarCatalog.ALL_AVATARS.forEach { avatar ->
            if (avatar.unlockLevel == currentLevel && 
                !_rewardsState.value.unlocked.avatars.contains(avatar.id)) {
                unlockReward(RewardType.AVATAR, avatar.id)
                newUnlocks.add("Avatar: ${avatar.name}")
            }
        }
        
        // Verificar badges
        BadgeCatalog.ALL_BADGES.forEach { badge ->
            if (badge.unlockLevel == currentLevel && 
                !_rewardsState.value.unlocked.badges.contains(badge.id)) {
                unlockReward(RewardType.BADGE, badge.id)
                newUnlocks.add("Insignia: ${badge.name}")
            }
        }
        
        // Verificar estilos de tarjetas
        CardStyleCatalog.ALL_CARD_STYLES.forEach { cardStyle ->
            if (cardStyle.unlockLevel == currentLevel && 
                !_rewardsState.value.unlocked.cardStyles.contains(cardStyle.id)) {
                unlockReward(RewardType.CARD_STYLE, cardStyle.id)
                newUnlocks.add("Tarjeta: ${cardStyle.name}")
            }
        }
        
        // Actualizar lista de nuevos desbloqueos
        if (newUnlocks.isNotEmpty()) {
            _rewardsState.update { it.copy(newUnlocks = newUnlocks) }
        }
        
        return newUnlocks
    }
    
    /**
     * Desbloquea una recompensa específica
     */
    private fun unlockReward(type: RewardType, id: String) {
        _rewardsState.update { currentState ->
            val updatedUnlocked = when (type) {
                RewardType.THEME -> currentState.unlocked.copy(
                    themes = currentState.unlocked.themes + id
                )
                RewardType.PARTICLE -> currentState.unlocked.copy(
                    particles = currentState.unlocked.particles + id
                )
                RewardType.AVATAR -> currentState.unlocked.copy(
                    avatars = currentState.unlocked.avatars + id
                )
                RewardType.BADGE -> currentState.unlocked.copy(
                    badges = currentState.unlocked.badges + id
                )
                RewardType.CARD_STYLE -> currentState.unlocked.copy(
                    cardStyles = currentState.unlocked.cardStyles + id
                )
                RewardType.SOUND -> currentState.unlocked.copy(
                    sounds = currentState.unlocked.sounds + id
                )
            }
            
            val newState = currentState.copy(unlocked = updatedUnlocked)
            saveRewardsState(newState)
            newState
        }
    }
    
    // =================================================================================================
    // ACTIVATION LOGIC
    // =================================================================================================
    
    /**
     * Activa un tema específico
     */
    fun activateTheme(themeId: String) {
        if (!_rewardsState.value.unlocked.themes.contains(themeId)) return
        
        _rewardsState.update { currentState ->
            val newState = currentState.copy(
                active = currentState.active.copy(currentTheme = themeId)
            )
            saveRewardsState(newState)
            newState
        }
    }
    
    /**
     * Activa un efecto de partículas
     */
    fun activateParticle(particleId: String) {
        if (!_rewardsState.value.unlocked.particles.contains(particleId)) return
        
        _rewardsState.update { currentState ->
            val newState = currentState.copy(
                active = currentState.active.copy(currentParticle = particleId)
            )
            saveRewardsState(newState)
            newState
        }
    }
    
    /**
     * Activa un avatar
     */
    fun activateAvatar(avatarId: String) {
        if (!_rewardsState.value.unlocked.avatars.contains(avatarId)) return
        
        _rewardsState.update { currentState ->
            val newState = currentState.copy(
                active = currentState.active.copy(currentAvatar = avatarId)
            )
            saveRewardsState(newState)
            newState
        }
    }
    
    /**
     * Activa un estilo de tarjeta
     */
    fun activateCardStyle(cardStyleId: String) {
        if (!_rewardsState.value.unlocked.cardStyles.contains(cardStyleId)) return
        
        _rewardsState.update { currentState ->
            val newState = currentState.copy(
                active = currentState.active.copy(currentCardStyle = cardStyleId)
            )
            saveRewardsState(newState)
            newState
        }
    }
    
    /**
     * Activa un paquete de sonidos
     */
    fun activateSound(soundId: String) {
        if (!_rewardsState.value.unlocked.sounds.contains(soundId)) return
        
        _rewardsState.update { currentState ->
            val newState = currentState.copy(
                active = currentState.active.copy(currentSound = soundId)
            )
            saveRewardsState(newState)
            newState
        }
    }
    
    // =================================================================================================
    // GETTERS
    // =================================================================================================
    
    /**
     * Obtiene el tema actualmente activo
     */
    fun getActiveTheme(): AppTheme {
        val themeId = _rewardsState.value.active.currentTheme
        return ThemeCatalog.getThemeById(themeId) ?: ThemeCatalog.ALL_THEMES.first()
    }
    
    /**
     * Obtiene el efecto de partículas activo
     */
    fun getActiveParticle(): ParticleEffect? {
        val particleId = _rewardsState.value.active.currentParticle
        if (particleId == "none") return null
        return ParticleCatalog.getParticleById(particleId)
    }
    
    /**
     * Obtiene el avatar activo
     */
    fun getActiveAvatar(): Avatar {
        val avatarId = _rewardsState.value.active.currentAvatar
        return AvatarCatalog.getAvatarById(avatarId) ?: AvatarCatalog.ALL_AVATARS.first()
    }
    
    /**
     * Obtiene el estilo de tarjeta activo
     */
    fun getActiveCardStyle(): CardStyle {
        val cardStyleId = _rewardsState.value.active.currentCardStyle
        return CardStyleCatalog.getCardStyleById(cardStyleId) ?: CardStyleCatalog.ALL_CARD_STYLES.first()
    }
    
    /**
     * Limpia la lista de nuevos desbloqueos
     */
    fun clearNewUnlocks() {
        _rewardsState.update { it.copy(newUnlocks = emptyList()) }
    }
    
    // =================================================================================================
    // PERSISTENCE
    // =================================================================================================
    
    private fun saveRewardsState(state: RewardsState) {
        prefs.edit().apply {
            putString("unlocked_themes", gson.toJson(state.unlocked.themes.toList()))
            putString("unlocked_particles", gson.toJson(state.unlocked.particles.toList()))
            putString("unlocked_avatars", gson.toJson(state.unlocked.avatars.toList()))
            putString("unlocked_sounds", gson.toJson(state.unlocked.sounds.toList()))
            putString("unlocked_badges", gson.toJson(state.unlocked.badges.toList()))
            putString("unlocked_card_styles", gson.toJson(state.unlocked.cardStyles.toList()))
            
            putString("active_theme", state.active.currentTheme)
            putString("active_particle", state.active.currentParticle)
            putString("active_avatar", state.active.currentAvatar)
            putString("active_sound", state.active.currentSound)
            putString("active_card_style", state.active.currentCardStyle)
            
            apply()
        }
    }
    
    private fun loadRewardsState(): RewardsState {
        val unlockedThemes = loadStringSet("unlocked_themes", setOf("default"))
        val unlockedParticles = loadStringSet("unlocked_particles", setOf("none"))
        val unlockedAvatars = loadStringSet("unlocked_avatars", setOf("basic"))
        val unlockedSounds = loadStringSet("unlocked_sounds", setOf("default"))
        val unlockedBadges = loadStringSet("unlocked_badges", emptySet())
        val unlockedCardStyles = loadStringSet("unlocked_card_styles", setOf("flat"))
        
        return RewardsState(
            unlocked = UnlockedRewards(
                themes = unlockedThemes,
                particles = unlockedParticles,
                avatars = unlockedAvatars,
                sounds = unlockedSounds,
                badges = unlockedBadges,
                cardStyles = unlockedCardStyles
            ),
            active = ActiveRewards(
                currentTheme = prefs.getString("active_theme", "default") ?: "default",
                currentParticle = prefs.getString("active_particle", "none") ?: "none",
                currentAvatar = prefs.getString("active_avatar", "basic") ?: "basic",
                currentSound = prefs.getString("active_sound", "default") ?: "default",
                currentCardStyle = prefs.getString("active_card_style", "flat") ?: "flat"
            )
        )
    }
    
    private fun loadStringSet(key: String, default: Set<String>): Set<String> {
        val json = prefs.getString(key, null) ?: return default
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type).toSet()
        } catch (e: Exception) {
            default
        }
    }
}

// =================================================================================================
// REWARD TYPES
// =================================================================================================

enum class RewardType {
    THEME,
    PARTICLE,
    AVATAR,
    BADGE,
    CARD_STYLE,
    SOUND
}
