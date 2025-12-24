# 📝 SNIPPETS DE CÓDIGO - INTEGRACIÓN RÁPIDA

Este documento contiene fragmentos de código listos para copiar y pegar en tu `MainActivity.kt`.

---

## 1️⃣ Agregar RewardsManager al ViewModel

```kotlin
// En CalculatorViewModel
class CalculatorViewModel(private val context: Context) : ViewModel() {
    
    // ========== AGREGAR ESTAS LÍNEAS ==========
    private val rewardsManager = RewardsManager(context)
    val rewardsState = rewardsManager.rewardsState
    // ==========================================
    
    // ... resto del código existente ...
}
```

---

## 2️⃣ Modificar función addXp

```kotlin
// REEMPLAZAR la función addXp existente con esta versión mejorada:

private fun addXp(ctx: Context) {
    val p = _uiState.value.userProfile
    var nx = p.xp + 10
    var nl = p.level
    
    if (nx >= nl * 100) {
        nl++
        nx = 0
        
        // ========== NUEVO: Verificar recompensas ==========
        val newUnlocks = rewardsManager.checkAndUnlockRewards(nl)
        // ==================================================
        
        val unlockedTitle = when (nl) {
            1 -> "Novato"
            5 -> "Experto Financiero"
            10 -> "Lobo de Wall St"
            20 -> "Leyenda"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                showLevelUpOverlay = true,
                unlockedTitle = unlockedTitle,
                // ========== NUEVO: Mostrar overlay de recompensas ==========
                showRewardUnlockOverlay = newUnlocks.isNotEmpty()
                // ===========================================================
            )
        }
    }
    
    saveProfile(ctx, p.copy(xp = nx, level = nl))
}
```

---

## 3️⃣ Actualizar UIState

```kotlin
// AGREGAR este campo a tu data class UIState:

data class UIState(
    val userProfile: UserProfile = UserProfile(),
    val mandatoryConfigNeeded: Boolean = false,
    val showTutorial: Boolean = false,
    val showLevelUpOverlay: Boolean = false,
    val unlockedTitle: String? = null,
    
    // ========== NUEVO ==========
    val showRewardUnlockOverlay: Boolean = false,
    val showRewardsGallery: Boolean = false,
    val showThemeSelector: Boolean = false
    // ===========================
)
```

---

## 4️⃣ Agregar funciones de gestión de recompensas al ViewModel

```kotlin
// AGREGAR estas funciones al CalculatorViewModel:

// ========== FUNCIONES DE RECOMPENSAS ==========

fun activateTheme(themeId: String) {
    rewardsManager.activateTheme(themeId)
}

fun activateParticle(particleId: String) {
    rewardsManager.activateParticle(particleId)
}

fun activateAvatar(avatarId: String) {
    rewardsManager.activateAvatar(avatarId)
}

fun activateCardStyle(cardStyleId: String) {
    rewardsManager.activateCardStyle(cardStyleId)
}

fun dismissRewardUnlock() {
    rewardsManager.clearNewUnlocks()
    _uiState.update { it.copy(showRewardUnlockOverlay = false) }
}

fun openRewardsGallery() {
    _uiState.update { it.copy(showRewardsGallery = true) }
}

fun closeRewardsGallery() {
    _uiState.update { it.copy(showRewardsGallery = false) }
}

fun openThemeSelector() {
    _uiState.update { it.copy(showThemeSelector = true) }
}

fun closeThemeSelector() {
    _uiState.update { it.copy(showThemeSelector = false) }
}

// ==============================================
```

---

## 5️⃣ Agregar imports necesarios

```kotlin
// AGREGAR estos imports al inicio de MainActivity.kt:

// ========== IMPORTS DE RECOMPENSAS ==========
import com.example.consucalcu.rewards.*
import com.example.consucalcu.rewards.ui.*
// ============================================
```

---

## 6️⃣ Agregar Overlay de Recompensas en ConsuCalcuApp

```kotlin
// AGREGAR dentro de ConsuCalcuApp, después del LevelUpOverlay existente:

@Composable
fun ConsuCalcuApp(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModelFactory(LocalContext.current)
    )
    val state by viewModel.uiState.collectAsState()
    
    // ========== NUEVO: Observar estado de recompensas ==========
    val rewardsState by viewModel.rewardsState.collectAsState()
    // ===========================================================
    
    Box(modifier = Modifier.fillMaxSize()) {
        // ... tu UI principal ...
        
        // Overlay de nivel (existente)
        if (state.showLevelUpOverlay) {
            LevelUpOverlay(state.userProfile.level, state.unlockedTitle) {
                soundManager?.play("magic")
                viewModel.dismissLevelUp()
            }
        }
        
        // ========== NUEVO: Overlay de recompensas ==========
        if (state.showRewardUnlockOverlay && rewardsState.newUnlocks.isNotEmpty()) {
            RewardUnlockOverlay(
                newUnlocks = rewardsState.newUnlocks,
                onDismiss = {
                    soundManager?.play("magic")
                    viewModel.dismissRewardUnlock()
                }
            )
        }
        // ===================================================
    }
}
```

---

## 7️⃣ Aplicar Tema Activo Dinámicamente

```kotlin
// MODIFICAR tu ConsuCalcuApp para aplicar el tema activo:

@Composable
fun ConsuCalcuApp(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModelFactory(LocalContext.current)
    )
    val state by viewModel.uiState.collectAsState()
    val rewardsState by viewModel.rewardsState.collectAsState()
    
    // ========== NUEVO: Obtener tema activo ==========
    val activeTheme = remember(rewardsState.active.currentTheme) {
        ThemeCatalog.getThemeById(rewardsState.active.currentTheme)
            ?: ThemeCatalog.ALL_THEMES.first()
    }
    
    // Crear ColorScheme dinámico
    val dynamicColors = if (isDark) {
        darkColorScheme(
            primary = activeTheme.colors.primary,
            onPrimary = Color.White,
            secondary = activeTheme.colors.secondary,
            onSecondary = Color.White,
            tertiary = activeTheme.colors.accent,
            onTertiary = Color.White,
            background = activeTheme.colors.background,
            onBackground = TextLight,
            surface = activeTheme.colors.surface,
            onSurface = TextLight,
            outline = BorderDark
        )
    } else {
        lightColorScheme(
            primary = activeTheme.colors.primary,
            onPrimary = Color.White,
            secondary = activeTheme.colors.secondary,
            tertiary = activeTheme.colors.accent
        )
    }
    // ================================================
    
    MaterialTheme(
        // ========== CAMBIAR ESTO ==========
        colorScheme = dynamicColors,  // En lugar de: if (isDark) DarkColorScheme else LightColorScheme
        // ==================================
        typography = JuicyTypography
    ) {
        // ... contenido ...
    }
}
```

---

## 8️⃣ Agregar Botón de Recompensas en el Menú

```kotlin
// AGREGAR en tu ProfileScreen o menú principal:

@Composable
fun ProfileScreen(/* ... */) {
    val state by viewModel.uiState.collectAsState()
    val rewardsState by viewModel.rewardsState.collectAsState()
    
    Column(/* ... */) {
        // ... otros elementos del menú ...
        
        // ========== NUEVO: Botón de Recompensas ==========
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.openRewardsGallery() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆",
                    fontSize = 24.sp
                )
                Text(
                    text = "Mis Recompensas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${rewardsState.unlocked.themes.size + 
                            rewardsState.unlocked.particles.size + 
                            rewardsState.unlocked.avatars.size + 
                            rewardsState.unlocked.badges.size + 
                            rewardsState.unlocked.cardStyles.size}/33",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        // =================================================
    }
    
    // ========== NUEVO: Modal de Recompensas ==========
    if (state.showRewardsGallery) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRewardsGallery() },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            RewardsGalleryScreen(
                currentLevel = state.userProfile.level,
                rewardsState = rewardsState,
                onThemeClick = { viewModel.openThemeSelector() },
                onParticleClick = { /* TODO: Abrir selector de partículas */ },
                onAvatarClick = { /* TODO: Abrir selector de avatares */ },
                onCardStyleClick = { /* TODO: Abrir selector de estilos */ },
                onClose = { viewModel.closeRewardsGallery() }
            )
        }
    }
    
    // Modal de Selector de Temas
    if (state.showThemeSelector) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeThemeSelector() },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ThemeSelectorScreen(
                currentLevel = state.userProfile.level,
                rewardsState = rewardsState,
                onThemeSelected = { themeId ->
                    viewModel.activateTheme(themeId)
                    soundManager?.play("pop")
                },
                onClose = { viewModel.closeThemeSelector() }
            )
        }
    }
    // =================================================
}
```

---

## 9️⃣ Mostrar Avatar Activo en Perfil

```kotlin
// AGREGAR en tu ProfileScreen para mostrar el avatar:

@Composable
fun ProfileHeader(
    profile: UserProfile,
    rewardsState: RewardsState
) {
    // ========== NUEVO: Obtener avatar activo ==========
    val activeAvatar = remember(rewardsState.active.currentAvatar) {
        AvatarCatalog.getAvatarById(rewardsState.active.currentAvatar)
            ?: AvatarCatalog.ALL_AVATARS.first()
    }
    // ==================================================
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== NUEVO: Avatar con gradiente ==========
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(activeAvatar.gradient)
                .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activeAvatar.emoji,
                fontSize = 40.sp
            )
        }
        // =================================================
        
        Column {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            
            // ========== NUEVO: Mostrar nombre del avatar ==========
            Text(
                text = activeAvatar.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            // ======================================================
            
            Text(
                text = "Nivel ${profile.level}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

## 🔟 Aplicar Efectos de Partículas en Cálculos

```kotlin
// MODIFICAR tu función de cálculo exitoso:

fun onCalculateSuccess(result: LoanOffer) {
    // ... lógica existente ...
    
    // ========== NUEVO: Aplicar efecto de partículas activo ==========
    val activeParticle = rewardsManager.getActiveParticle()
    
    if (activeParticle != null && activeParticle.id != "none") {
        // Mostrar konfetti con los colores del efecto activo
        _uiState.update {
            it.copy(
                showKonfetti = true,
                konfettiColors = activeParticle.colors,
                konfettiParticleCount = activeParticle.particleCount
            )
        }
    } else {
        // Konfetti por defecto
        _uiState.update { it.copy(showKonfetti = true) }
    }
    // ================================================================
    
    soundManager?.play("cash")
    addXp(context)
}
```

---

## 1️⃣1️⃣ Aplicar Estilo de Tarjeta Activo

```kotlin
// MODIFICAR tu composable de tarjeta de resultados:

@Composable
fun ResultCard(
    offer: LoanOffer,
    rewardsState: RewardsState,
    modifier: Modifier = Modifier
) {
    // ========== NUEVO: Obtener estilo activo ==========
    val activeCardStyle = remember(rewardsState.active.currentCardStyle) {
        CardStyleCatalog.getCardStyleById(rewardsState.active.currentCardStyle)
            ?: CardStyleCatalog.ALL_CARD_STYLES.first()
    }
    // ==================================================
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            // ========== NUEVO: Aplicar shimmer si está habilitado ==========
            .then(
                if (activeCardStyle.hasShimmer) {
                    Modifier.shimmerEffect()
                } else {
                    Modifier
                }
            ),
            // ===============================================================
        shape = RoundedCornerShape(20.dp),
        color = if (activeCardStyle.hasGlassmorphism) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        // ========== NUEVO: Aplicar glow si está habilitado ==========
        shadowElevation = if (activeCardStyle.hasGlow) 16.dp else 4.dp,
        // ============================================================
        border = BorderStroke(
            width = activeCardStyle.borderWidth.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        // ... contenido de la tarjeta ...
    }
}
```

---

## 🎯 Checklist de Integración

Marca cada paso conforme lo completes:

- [ ] 1. Agregar `RewardsManager` al `ViewModel`
- [ ] 2. Modificar función `addXp`
- [ ] 3. Actualizar `UIState`
- [ ] 4. Agregar funciones de gestión
- [ ] 5. Agregar imports
- [ ] 6. Agregar overlay de recompensas
- [ ] 7. Aplicar tema activo
- [ ] 8. Agregar botón de recompensas
- [ ] 9. Mostrar avatar activo
- [ ] 10. Aplicar efectos de partículas
- [ ] 11. Aplicar estilo de tarjeta

---

## 🐛 Solución de Problemas Comunes

### Error: "Unresolved reference: RewardsManager"

**Solución:**
```kotlin
// Verifica que tengas este import:
import com.example.consucalcu.rewards.RewardsManager
```

### Error: "Cannot access 'rewardsState'"

**Solución:**
```kotlin
// Asegúrate de exponer el StateFlow en el ViewModel:
val rewardsState = rewardsManager.rewardsState
```

### Los temas no se aplican

**Solución:**
```kotlin
// Verifica que estés usando el ColorScheme dinámico:
MaterialTheme(
    colorScheme = dynamicColors,  // NO usar DarkColorScheme directamente
    typography = JuicyTypography
)
```

### Las recompensas no se desbloquean

**Solución:**
```kotlin
// Verifica que llames a checkAndUnlockRewards en addXp:
val newUnlocks = rewardsManager.checkAndUnlockRewards(nl)
```

---

## 🚀 ¡Listo para Integrar!

Copia y pega estos snippets en orden. El sistema está diseñado para ser **plug-and-play**.

**Tiempo estimado de integración: 1-2 horas**

---

*Última actualización: 2025-12-22*
