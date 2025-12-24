# 🎨 Sistema de Recompensas - ConsuCalcu

## 🌟 Descripción General

Sistema modular de recompensas estéticas que se desbloquean progresivamente conforme el usuario sube de nivel. Diseñado para aumentar el engagement y hacer la experiencia más gratificante sin afectar la funcionalidad financiera core.

---

## 📦 Componentes del Sistema

### 1. **Temas de Color** 🎨
Paletas de colores completas que transforman toda la interfaz:

| Nivel | Tema | Emoji | Tipo |
|-------|------|-------|------|
| 1 | Azul Clásico | 🔵 | Básico |
| 3 | Ocean Breeze | 🌊 | Estándar |
| 5 | Fire Phoenix | 🔥 | Estándar |
| 7 | Sakura Dream | 🌸 | Estándar |
| 9 | Emerald Forest | 🌲 | Estándar |
| 10 | Diamond Elite | 💎 | **Premium** |
| 12 | Midnight Blue | 🌙 | Estándar |
| 15 | Cosmic Purple | 🌌 | **Premium** |
| 20 | Gold Legend | 🏆 | **Premium** |

**Características:**
- Gradientes personalizados
- Colores primarios, secundarios y de acento
- Sombras y brillos coordinados
- Fondos y superficies temáticos

### 2. **Efectos de Partículas** ✨
Animaciones visuales que se activan en eventos importantes:

| Nivel | Efecto | Emoji | Partículas |
|-------|--------|-------|------------|
| 1 | Sin Efecto | ⚪ | 0 |
| 2 | Estrellas | ⭐ | 30 |
| 4 | Monedas | 💰 | 40 |
| 6 | Fuegos Artificiales | 🎆 | 60 |
| 8 | Estrellas Fugaces | 🌟 | 25 |
| 12 | Diamantes | 💎 | 35 |
| 18 | Llamas Doradas | 🔥 | 50 |

**Uso:**
- Cálculos exitosos
- Generación de tickets
- Subida de nivel
- Logros especiales

### 3. **Avatares** 👤
Iconos de perfil que representan el progreso del usuario:

| Nivel | Avatar | Emoji | Descripción |
|-------|--------|-------|-------------|
| 1 | Usuario | 👤 | Avatar básico |
| 3 | Ejecutivo | 💼 | Profesional de negocios |
| 5 | Lobo de Wall Street | 🦁 | Maestro de finanzas |
| 7 | Estratega | 🎯 | Planificador experto |
| 10 | Rey de las Finanzas | 👑 | Dominio total |
| 15 | Visionario | 🚀 | Más allá del horizonte |
| 20 | Leyenda Dorada | 🏆 | Estatus legendario |

**Características:**
- Gradientes únicos por avatar
- Se muestra en el perfil
- Refleja el nivel de maestría

### 4. **Insignias** 🏅
Logros coleccionables con sistema de rareza:

| Nivel | Insignia | Emoji | Rareza |
|-------|----------|-------|--------|
| 2 | Primer Paso | 🥉 | Común (Bronce) |
| 5 | Calculador Experto | 🥈 | Raro (Plata) |
| 10 | Maestro Financiero | 🥇 | Épico (Oro) |
| 15 | Elite Diamond | 💎 | Legendario (Diamante) |
| 20 | Leyenda Inmortal | 👑 | Legendario (Arcoíris) |

**Sistema de Rareza:**
- 🥉 **Común**: Gradiente bronce
- 🥈 **Raro**: Gradiente plata
- 🥇 **Épico**: Gradiente oro
- 💎 **Legendario**: Gradiente multicolor

### 5. **Estilos de Tarjetas** 💳
Diseños premium para mostrar resultados de cálculos:

| Nivel | Estilo | Emoji | Efectos |
|-------|--------|-------|---------|
| 1 | Plana | 📄 | Diseño básico |
| 5 | Cristal | 💳 | Glassmorphism |
| 10 | Cristalina | 💎 | Glassmorphism + Shimmer |
| 15 | Dorada | 🏆 | Shimmer + Glow |
| 20 | Holográfica | 🌟 | Todos los efectos |

**Efectos Disponibles:**
- **Glassmorphism**: Efecto de vidrio esmerilado
- **Shimmer**: Brillo animado
- **Glow**: Resplandor exterior
- **Border**: Bordes personalizados

---

## 🏗️ Arquitectura

```
rewards/
├── RewardsData.kt          # Modelos de datos base
├── ThemeCatalog.kt         # 9 temas con paletas completas
├── RewardsCatalog.kt       # Catálogos de partículas, avatares, badges, estilos
├── RewardsManager.kt       # Lógica central y persistencia
└── ui/
    ├── RewardsGallery.kt   # Pantalla principal de colección
    ├── ThemeSelector.kt    # Selector de temas
    └── RewardUnlockOverlay.kt # Overlay de desbloqueo
```

### Flujo de Datos

```
Usuario sube de nivel
        ↓
CalculatorViewModel.addXp()
        ↓
RewardsManager.checkAndUnlockRewards()
        ↓
Actualiza RewardsState (Flow)
        ↓
UI se actualiza automáticamente
        ↓
Muestra RewardUnlockOverlay
```

---

## 🔧 API Pública

### RewardsManager

```kotlin
class RewardsManager(context: Context) {
    // Estado reactivo
    val rewardsState: StateFlow<RewardsState>
    
    // Desbloqueo
    fun checkAndUnlockRewards(currentLevel: Int): List<String>
    
    // Activación
    fun activateTheme(themeId: String)
    fun activateParticle(particleId: String)
    fun activateAvatar(avatarId: String)
    fun activateCardStyle(cardStyleId: String)
    
    // Getters
    fun getActiveTheme(): AppTheme
    fun getActiveParticle(): ParticleEffect?
    fun getActiveAvatar(): Avatar
    fun getActiveCardStyle(): CardStyle
    
    // Utilidades
    fun clearNewUnlocks()
}
```

### ThemeCatalog

```kotlin
object ThemeCatalog {
    val ALL_THEMES: List<AppTheme>
    
    fun getThemeById(id: String): AppTheme?
    fun getUnlockedThemes(currentLevel: Int): List<AppTheme>
    fun getNextThemeToUnlock(currentLevel: Int): AppTheme?
}
```

---

## 💾 Persistencia

Todos los datos se guardan automáticamente en **SharedPreferences**:

```
rewards_prefs
├── unlocked_themes: Set<String>
├── unlocked_particles: Set<String>
├── unlocked_avatars: Set<String>
├── unlocked_badges: Set<String>
├── unlocked_card_styles: Set<String>
├── active_theme: String
├── active_particle: String
├── active_avatar: String
└── active_card_style: String
```

**Formato**: JSON serializado con Gson

---

## 🎯 Casos de Uso

### 1. Usuario Sube de Nivel 3

```kotlin
// En CalculatorViewModel
private fun addXp(ctx: Context) {
    // ... lógica de XP ...
    
    if (nx >= nl * 100) {
        nl++
        
        // Verificar recompensas
        val newUnlocks = rewardsManager.checkAndUnlockRewards(nl)
        // newUnlocks = ["Tema: Ocean Breeze", "Avatar: Ejecutivo"]
        
        // Mostrar overlay
        _uiState.update {
            it.copy(showRewardUnlockOverlay = true)
        }
    }
}
```

### 2. Usuario Cambia de Tema

```kotlin
// En ThemeSelectorScreen
ThemeCard(
    theme = oceanBreeze,
    isUnlocked = true,
    isActive = false,
    onClick = {
        viewModel.activateTheme("ocean_breeze")
        // El tema se aplica automáticamente en toda la app
    }
)
```

### 3. Aplicar Tema Activo

```kotlin
@Composable
fun ConsuCalcuApp() {
    val rewardsState by viewModel.rewardsState.collectAsState()
    val activeTheme = ThemeCatalog.getThemeById(rewardsState.active.currentTheme)
    
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = activeTheme.colors.primary,
            secondary = activeTheme.colors.secondary,
            // ...
        )
    ) {
        // UI aquí
    }
}
```

---

## 🎨 Ejemplos Visuales

### Tema Fire Phoenix
```kotlin
primary = Color(0xFFFF6D00)        // Naranja vibrante
secondary = Color(0xFFFF1744)      // Rojo intenso
accent = Color(0xFFFFD600)         // Amarillo dorado
gradient = [#FF6D00 → #FF1744 → #FFD600]
```

### Tema Cosmic Purple
```kotlin
primary = Color(0xFF7C4DFF)        // Púrpura profundo
secondary = Color(0xFFE040FB)      // Magenta brillante
accent = Color(0xFF00E5FF)         // Cian neón
gradient = [#7C4DFF → #E040FB → #00E5FF]
```

---

## 🚀 Extensibilidad

### Agregar Nuevo Tema

```kotlin
// En ThemeCatalog.kt
val NeonCyberpunkColors = ThemeColors(
    primary = Color(0xFFFF00FF),
    primaryShadow = Color(0xFFCC00CC),
    primaryGlow = Color(0xFFFF66FF),
    secondary = Color(0xFF00FFFF),
    secondaryShadow = Color(0xFF00CCCC),
    secondaryGlow = Color(0xFF66FFFF),
    accent = Color(0xFFFFFF00),
    background = Color(0xFF000000),
    surface = Color(0xFF1A001A),
    gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF))
    )
)

// Agregar a ALL_THEMES
AppTheme(
    id = "neon_cyberpunk",
    name = "Neon Cyberpunk",
    description = "Futuro distópico neón",
    unlockLevel = 25,
    emoji = "🌃",
    colors = NeonCyberpunkColors,
    isPremium = true
)
```

### Agregar Nuevo Efecto de Partículas

```kotlin
// En RewardsCatalog.kt
ParticleEffect(
    id = "rainbow_burst",
    name = "Explosión Arcoíris",
    description = "Explosión multicolor espectacular",
    unlockLevel = 25,
    emoji = "🌈",
    colors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF7F00),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF0000FF),
        Color(0xFF4B0082),
        Color(0xFF9400D3)
    ),
    particleCount = 100
)
```

---

## 📊 Métricas de Engagement

El sistema de recompensas está diseñado para:

- ✅ **Aumentar retención**: Usuarios vuelven para desbloquear más
- ✅ **Incrementar uso**: Más cálculos = más XP = más recompensas
- ✅ **Personalización**: Cada usuario tiene su estilo único
- ✅ **Gamificación**: Sensación de progreso constante
- ✅ **Compartir**: Usuarios presumen sus logros

---

## 🎓 Mejores Prácticas

1. **No bloquear funcionalidad**: Las recompensas son solo estéticas
2. **Feedback inmediato**: Mostrar overlay al desbloquear
3. **Progresión clara**: El usuario sabe qué viene después
4. **Variedad**: Diferentes tipos de recompensas mantienen interés
5. **Premium sin pago**: Temas premium se desbloquean con esfuerzo

---

## 🐛 Testing

```kotlin
// Test de desbloqueo
@Test
fun `nivel 5 desbloquea Fire Phoenix y Lobo de Wall Street`() {
    val manager = RewardsManager(context)
    val unlocks = manager.checkAndUnlockRewards(5)
    
    assertTrue(unlocks.contains("Tema: Fire Phoenix"))
    assertTrue(unlocks.contains("Avatar: Lobo de Wall Street"))
}

// Test de persistencia
@Test
fun `tema activo se persiste correctamente`() {
    val manager = RewardsManager(context)
    manager.activateTheme("ocean_breeze")
    
    val newManager = RewardsManager(context)
    assertEquals("ocean_breeze", newManager.getActiveTheme().id)
}
```

---

## 📝 Changelog

### v1.0.0 (2025-12-22)
- ✨ Sistema inicial de recompensas
- 🎨 9 temas de color
- ✨ 7 efectos de partículas
- 👤 7 avatares
- 🏅 5 insignias
- 💳 5 estilos de tarjetas
- 💾 Persistencia con SharedPreferences
- 🎯 UI completa (Galería, Selector, Overlay)

---

## 🤝 Contribuir

Para agregar nuevas recompensas:

1. Define los datos en el catálogo correspondiente
2. Asegúrate de asignar un nivel de desbloqueo
3. Crea assets visuales si es necesario (Lottie, imágenes)
4. Actualiza la documentación
5. Agrega tests

---

## 📄 Licencia

Parte del proyecto ConsuCalcu - Todos los derechos reservados

---

## 🎉 Conclusión

Este sistema de recompensas transforma ConsuCalcu de una calculadora funcional a una **experiencia gamificada adictiva**. Los usuarios no solo calculan préstamos, sino que **coleccionan, personalizan y progresan** en su viaje financiero.

**¡Que comience la caza de recompensas!** 🏆✨
