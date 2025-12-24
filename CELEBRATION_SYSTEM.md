# Sistema de Celebración Inmersiva - ConsuCalcu

## 📋 Resumen de Implementación

Se ha implementado un sistema completo de celebración inmersiva con overlays de pantalla completa para dos eventos críticos:

### 🔥 1. Incremento de Racha Diaria (Streak Overlay)

**Trigger:** Se activa automáticamente cuando el usuario abre la app en días consecutivos.

**Características:**
- Overlay de pantalla completa con fondo oscuro semitransparente (85% opacidad)
- Animación Lottie de fuego (`lottie_fire.json`) en loop infinito
- Tipografía gigante (36sp) en color naranja vibrante
- Mensaje motivacional: "¡Estás on fire! 🔥 Vuelve mañana para mantenerla."
- Botón de acción naranja "¡VAMOS!" con elevación 3D
- Sonido "magic" al cerrar

**Lógica:**
- Detecta si el usuario entró ayer → incrementa racha
- Solo muestra overlay si racha > 1 (no en el primer día)
- Guarda la fecha y racha en SharedPreferences

---

### 🏆 2. Subida de Nivel (Level Up Overlay)

**Trigger:** Se activa cuando el usuario gana suficiente XP para subir de nivel (cada 100 XP por nivel).

**Características:**
- Overlay con gradiente radial (azul profundo → negro)
- Animación Lottie de trofeo/explosión (`lottie_levelup.json`)
- Tipografía dorada (38sp) para el nivel alcanzado
- Sistema de títulos desbloqueables:
  - **Nivel 1:** "Novato"
  - **Nivel 5:** "Experto Financiero"
  - **Nivel 10:** "Lobo de Wall St"
  - **Nivel 20:** "Leyenda"
- Tarjeta de recompensa premium con borde dorado (cuando hay título)
- Botón "RECLAMAR" dorado (con título) o "¡CONTINUAR!" verde (sin título)
- Sonido "cash" al cerrar

**Lógica:**
- Verifica si `xp >= nivel * 100`
- Determina el título según el nivel alcanzado
- Dispara el overlay con el título correspondiente

---

## 🎨 Diseño Premium

### Colores Utilizados:
- **Racha:** Naranja vibrante (`JuicyOrange`)
- **Nivel:** Dorado (#FFD700) con fondo oscuro (#2C2C2C)
- **Fondos:** Semitransparentes para mantener contexto

### Tipografía:
- **Headlines:** 36-38sp, FontWeight.Black, letterSpacing 2sp
- **Body:** 18sp, FontWeight.Medium, lineHeight 26sp
- **Labels:** 14sp, FontWeight.Bold

### Animaciones:
- Lottie en loop infinito para mantener energía visual
- Tamaños: 200dp (racha), 220dp (nivel)
- Elevación de botones: 8dp para efecto 3D

---

## 🔧 Cambios Técnicos

### 1. CalculatorUiState
```kotlin
// Nuevos campos agregados:
val showStreakOverlay: Boolean = false
val showLevelUpOverlay: Boolean = false
val unlockedTitle: String? = null
```

### 2. CalculatorViewModel

**Métodos nuevos:**
- `dismissStreak()` - Cierra overlay de racha
- `dismissLevelUp()` - Cierra overlay de nivel y limpia título

**Métodos modificados:**
- `loadData()` - Detecta incremento de racha y dispara overlay
- `addXp()` - Implementa sistema de títulos y dispara overlay de nivel

### 3. Nuevos Composables

**StreakOverlay(streak: Int, onDismiss: () -> Unit)**
- Overlay inmersivo para racha diaria
- Fondo negro 85% opacidad
- Animación de fuego central

**LevelUpOverlay(level: Int, reward: String?, onDismiss: () -> Unit)**
- Overlay inmersivo para subida de nivel
- Gradiente radial azul-negro
- Tarjeta de recompensa condicional

### 4. Integración en ConsuCalcuApp
```kotlin
// Agregado al final del Box principal:
if (state.showStreakOverlay) {
    StreakOverlay(state.streak) {
        soundManager.play("magic")
        viewModel.dismissStreak()
    }
}

if (state.showLevelUpOverlay) {
    LevelUpOverlay(state.userProfile.level, state.unlockedTitle) {
        soundManager.play("cash")
        viewModel.dismissLevelUp()
    }
}
```

---

## 📦 Requisitos Previos

El usuario debe agregar los siguientes archivos Lottie en `res/raw/`:

1. **lottie_fire.json** - Animación de fuego/llamas para racha
2. **lottie_levelup.json** - Animación de trofeo/explosión para nivel

### Recomendaciones de Animaciones:
- **Fuego:** Buscar "fire animation" en LottieFiles
- **Level Up:** Buscar "trophy", "level up", "achievement" en LottieFiles

---

## 🎮 Experiencia de Usuario

### Flujo de Racha:
1. Usuario abre la app un día después de su última visita
2. `loadData()` detecta día consecutivo → incrementa racha
3. Si racha > 1 → `showStreakOverlay = true`
4. Overlay aparece sobre todo el contenido
5. Usuario ve animación de fuego y mensaje motivacional
6. Presiona "¡VAMOS!" → sonido "magic" + overlay se cierra

### Flujo de Nivel:
1. Usuario completa una acción que da XP (ej: calcular oferta)
2. `addXp()` verifica si alcanzó 100 XP
3. Nivel aumenta → determina si hay título nuevo
4. `showLevelUpOverlay = true` con título (si aplica)
5. Overlay aparece con animación de trofeo
6. Si hay título → muestra tarjeta dorada de recompensa
7. Usuario presiona "RECLAMAR" → sonido "cash" + overlay se cierra

---

## 🚀 Mejoras Futuras Sugeridas

1. **Haptic Feedback:** Agregar vibración al mostrar overlays
2. **Confetti:** Disparar confetti junto con los overlays
3. **Persistencia de Títulos:** Guardar títulos desbloqueados en SharedPreferences
4. **Galería de Logros:** Pantalla para ver todos los títulos obtenidos
5. **Más Títulos:** Agregar títulos intermedios (niveles 3, 7, 15, etc.)
6. **Rachas Especiales:** Celebraciones únicas para rachas de 7, 30, 100 días
7. **Animaciones de Entrada:** Fade in + scale para los overlays
8. **Sonidos Personalizados:** Diferentes sonidos según el nivel alcanzado

---

## ✅ Estado de Implementación

- [x] Estado actualizado con banderas de overlays
- [x] Lógica de detección de racha en `loadData()`
- [x] Sistema de títulos en `addXp()`
- [x] Métodos de cierre de overlays
- [x] Componente `StreakOverlay`
- [x] Componente `LevelUpOverlay`
- [x] Integración en `ConsuCalcuApp`
- [ ] Archivos Lottie agregados por el usuario
- [ ] Testing en dispositivo real

---

**Nota:** El usuario debe agregar los archivos `lottie_fire.json` y `lottie_levelup.json` en `app/src/main/res/raw/` para que las animaciones funcionen correctamente.
