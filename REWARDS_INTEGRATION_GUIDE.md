# 🎨 SISTEMA DE RECOMPENSAS - GUÍA DE INTEGRACIÓN

## 📋 Resumen

Este documento explica cómo integrar el nuevo sistema de recompensas modular en tu `MainActivity.kt`.

---

## 🗂️ Estructura de Archivos Creados

```
app/src/main/java/com/example/consucalcu/rewards/
├── RewardsData.kt              # Modelos de datos
├── ThemeCatalog.kt             # Catálogo de 9 temas
├── RewardsCatalog.kt           # Catálogos de partículas, avatares, badges, etc.
├── RewardsManager.kt           # Lógica central del sistema
└── ui/
    ├── RewardsGallery.kt       # Pantalla principal de recompensas
    ├── ThemeSelector.kt        # Selector de temas
    └── RewardUnlockOverlay.kt  # Overlay de desbloqueo
```

---

## 🎯 Características Implementadas

### 1. **Temas de Color** (9 temas)
- 🔵 Azul Clásico (Nivel 1)
- 🌊 Ocean Breeze (Nivel 3)
- 🔥 Fire Phoenix (Nivel 5)
- 🌸 Sakura Dream (Nivel 7)
- 🌲 Emerald Forest (Nivel 9)
- 💎 Diamond Elite (Nivel 10) ⭐ PREMIUM
- 🌙 Midnight Blue (Nivel 12)
- 🌌 Cosmic Purple (Nivel 15) ⭐ PREMIUM
- 🏆 Gold Legend (Nivel 20) ⭐ PREMIUM

### 2. **Efectos de Partículas** (7 efectos)
- ⚪ Sin Efecto (Nivel 1)
- ⭐ Estrellas (Nivel 2)
- 💰 Monedas (Nivel 4)
- 🎆 Fuegos Artificiales (Nivel 6)
- 🌟 Estrellas Fugaces (Nivel 8)
- 💎 Diamantes (Nivel 12)
- 🔥 Llamas Doradas (Nivel 18)

### 3. **Avatares** (7 avatares)
- 👤 Usuario (Nivel 1)
- 💼 Ejecutivo (Nivel 3)
- 🦁 Lobo de Wall Street (Nivel 5)
- 🎯 Estratega (Nivel 7)
- 👑 Rey de las Finanzas (Nivel 10)
- 🚀 Visionario (Nivel 15)
- 🏆 Leyenda Dorada (Nivel 20)

### 4. **Insignias** (5 badges)
- 🥉 Primer Paso (Nivel 2) - COMÚN
- 🥈 Calculador Experto (Nivel 5) - RARO
- 🥇 Maestro Financiero (Nivel 10) - ÉPICO
- 💎 Elite Diamond (Nivel 15) - LEGENDARIO
- 👑 Leyenda Inmortal (Nivel 20) - LEGENDARIO

### 5. **Estilos de Tarjetas** (5 estilos)
- 📄 Plana (Nivel 1)
- 💳 Cristal (Nivel 5) - Glassmorphism
- 💎 Cristalina (Nivel 10) - Glassmorphism + Shimmer
- 🏆 Dorada (Nivel 15) - Shimmer + Glow
- 🌟 Holográfica (Nivel 20) - Todo incluido

---

## 🔧 Pasos de Integración

### **Paso 1: Agregar RewardsManager al ViewModel**

En tu `CalculatorViewModel`, agrega:

```kotlin
class CalculatorViewModel(private val context: Context) : ViewModel() {
    
    // Agregar RewardsManager
    private val rewardsManager = RewardsManager(context)
    val rewardsState = rewardsManager.rewardsState
    
    // Modificar la función addXp para incluir recompensas
    private fun addXp(ctx: Context) {
        val p = _uiState.value.userProfile
        var nx = p.xp + 10
        var nl = p.level
        
        if (nx >= nl * 100) {
            nl++
            nx = 0
            
            // NUEVO: Verificar recompensas desbloqueadas
            val newUnlocks = rewardsManager.checkAndUnlockRewards(nl)
            
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
                    // NUEVO: Mostrar overlay de recompensas si hay desbloqueos
                    showRewardUnlockOverlay = newUnlocks.isNotEmpty()
                )
            }
        }
        
        saveProfile(ctx, p.copy(xp = nx, level = nl))
    }
    
    // NUEVO: Funciones para gestionar recompensas
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
}
```

### **Paso 2: Actualizar UIState**

Agrega el campo para el overlay de recompensas:

```kotlin
data class UIState(
    // ... campos existentes ...
    val showLevelUpOverlay: Boolean = false,
    val showRewardUnlockOverlay: Boolean = false, // NUEVO
    val unlockedTitle: String? = null
)
```

### **Paso 3: Agregar Overlay en ConsuCalcuApp**

En tu composable principal, agrega el overlay de recompensas:

```kotlin
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Tu UI principal aquí...
        
        // Overlay de nivel (existente)
        if (state.showLevelUpOverlay) {
            LevelUpOverlay(state.userProfile.level, state.unlockedTitle) {
                soundManager?.play("magic")
                viewModel.dismissLevelUp()
            }
        }
        
        // NUEVO: Overlay de recompensas
        if (state.showRewardUnlockOverlay && rewardsState.newUnlocks.isNotEmpty()) {
            RewardUnlockOverlay(
                newUnlocks = rewardsState.newUnlocks,
                onDismiss = {
                    soundManager?.play("magic")
                    viewModel.dismissRewardUnlock()
                }
            )
        }
    }
}
```

### **Paso 4: Agregar Botón de Recompensas en el Menú**

En tu pantalla de perfil o menú principal, agrega un botón para acceder a las recompensas:

```kotlin
// En ProfileScreen o donde tengas el menú
var showRewardsGallery by remember { mutableStateOf(false) }

// Botón para abrir recompensas
Button(
    onClick = { showRewardsGallery = true },
    modifier = Modifier.fillMaxWidth()
) {
    Icon(Icons.Rounded.EmojiEvents, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text("🏆 Mis Recompensas")
}

// Modal de recompensas
if (showRewardsGallery) {
    ModalBottomSheet(
        onDismissRequest = { showRewardsGallery = false }
    ) {
        RewardsGalleryScreen(
            currentLevel = state.userProfile.level,
            rewardsState = rewardsState,
            onThemeClick = { /* Abrir selector de temas */ },
            onParticleClick = { /* Abrir selector de partículas */ },
            onAvatarClick = { /* Abrir selector de avatares */ },
            onCardStyleClick = { /* Abrir selector de estilos */ },
            onClose = { showRewardsGallery = false }
        )
    }
}
```

### **Paso 5: Aplicar Tema Activo**

Para aplicar el tema seleccionado, modifica tu `MaterialTheme`:

```kotlin
@Composable
fun ConsuCalcuApp(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModelFactory(LocalContext.current)
    )
    val rewardsState by viewModel.rewardsState.collectAsState()
    
    // Obtener tema activo
    val activeTheme = ThemeCatalog.getThemeById(rewardsState.active.currentTheme)
        ?: ThemeCatalog.ALL_THEMES.first()
    
    // Crear ColorScheme dinámico basado en el tema activo
    val dynamicColors = if (isDark) {
        darkColorScheme(
            primary = activeTheme.colors.primary,
            secondary = activeTheme.colors.secondary,
            tertiary = activeTheme.colors.accent,
            background = activeTheme.colors.background,
            surface = activeTheme.colors.surface
        )
    } else {
        lightColorScheme(
            primary = activeTheme.colors.primary,
            secondary = activeTheme.colors.secondary,
            tertiary = activeTheme.colors.accent
        )
    }
    
    MaterialTheme(
        colorScheme = dynamicColors,
        typography = JuicyTypography
    ) {
        // Tu contenido aquí...
    }
}
```

---

## 🎨 Uso de Efectos de Partículas

Para usar los efectos de partículas en tus cálculos:

```kotlin
// En tu función de cálculo exitoso
val activeParticle = rewardsManager.getActiveParticle()

if (activeParticle != null) {
    // Mostrar konfetti con los colores del efecto activo
    showKonfetti(activeParticle.colors, activeParticle.particleCount)
}
```

---

## 🎯 Próximos Pasos Opcionales

1. **Sonidos Premium**: Agregar paquetes de sonidos desbloqueables
2. **Animaciones de Fondo**: Implementar fondos animados
3. **Logros Especiales**: Agregar logros por acciones específicas (ej: "Calculó 100 préstamos")
4. **Sistema de Racha**: Recompensas por usar la app X días seguidos
5. **Compartir Logros**: Permitir compartir badges en redes sociales

---

## 📝 Notas Importantes

- ✅ **Persistencia**: El sistema guarda automáticamente todo en SharedPreferences
- ✅ **Retrocompatibilidad**: No afecta usuarios existentes, todos empiezan con recompensas básicas
- ✅ **Performance**: Usa Kotlin Flow para actualizaciones reactivas eficientes
- ✅ **Modular**: Fácil de extender con nuevas categorías de recompensas

---

## 🐛 Troubleshooting

### Error: "Unresolved reference: RewardsManager"
**Solución**: Asegúrate de que todos los archivos estén en el paquete correcto:
```kotlin
package com.example.consucalcu.rewards
```

### Los temas no se aplican
**Solución**: Verifica que estés usando `rewardsState.active.currentTheme` en tu MaterialTheme

### Las recompensas no se desbloquean
**Solución**: Asegúrate de llamar `rewardsManager.checkAndUnlockRewards(nl)` en la función `addXp`

---

## 🎉 ¡Listo!

Tu sistema de recompensas está completo y listo para usar. Los usuarios ahora tendrán:
- 9 temas únicos para desbloquear
- 7 efectos de partículas
- 7 avatares personalizables
- 5 insignias coleccionables
- 5 estilos de tarjetas premium

¡Esto hará que tu app sea mucho más adictiva y gratificante! 🚀
