package com.example.consucalcu

import androidx.compose.ui.graphics.Color

// =================================================================================================
// NEW DESIGN SYSTEM COLORS (CONSUCALC NAVY & ORANGE)
// =================================================================================================
val ConsuNavy = Color(0xFF0B1F44)      // Azul Marino Profundo (Primary)
val ConsuNavyLight = Color(0xFF1B3564) // Azul Marino Claro para Degradados
val ConsuOrange = Color(0xFFF95B00)    // Naranja Vibrante (Accent)
val ConsuBgLight = Color(0xFFF5F7FB)   // Fondo Modo Claro
val ConsuChipBg = Color(0xFFF0F3F8)    // Fondo de Chips deseleccionados y botones secundarios

// AZUL PREMIUM - Mapeado a ConsuNavy
val JuicyBlue = ConsuNavy
val JuicyBlueShadow = ConsuNavyLight

// VERDE ESMERALDA - Mapeado a Azul Claro
val JuicyGreen = ConsuNavyLight
val JuicyGreenShadow = ConsuNavy

// NARANJA CÁLIDO - Mapeado a ConsuOrange
val JuicyOrange = ConsuOrange
val JuicyOrangeShadow = Color(0xFFD84A00)
val JuicyOrangeGlow = ConsuOrange.copy(alpha = 0.3f)

// ROJO CORAL - Mapeado a un rojo premium de error
val JuicyRed = Color(0xFFBE123C)
val JuicyRedShadow = Color(0xFF9F1239)
val JuicyRedGlow = Color(0xFFFDA4AF)

// PÚRPURA REFINADO - Mapeado a Navy
val JuicyPurple = ConsuNavy
val JuicyPurpleShadow = ConsuNavyLight
val JuicyPurpleGlow = ConsuOrange.copy(alpha = 0.3f)

// COLORES SECUNDARIOS
val JuicyCyan = ConsuNavyLight
val JuicyPink = ConsuOrange
val JuicyYellow = ConsuOrange
val JuicyLime = ConsuNavyLight

// ===== PALETA MONOCROMÁTICA PREMIUM =====
val BgLight = ConsuBgLight
val BgDark = Color(0xFF0A0F1D)        // Negro/Navy muy profundo para modo oscuro

val SurfaceLight = Color.White
val SurfaceDark = Color(0xFF101B3A)   // Navy oscuro para tarjetas en modo oscuro

val TextDark = Color(0xFF0B1F44)      // Azul marino profundo para texto en modo claro
val TextLight = Color(0xFFF4F4F5)     // Blanco suave Zinc-100

val BorderLight = Color(0xFFE2E8F0)   // Gris claro
val BorderDark = Color(0xFF1E293B)    // Gris oscuro

// DORADO PREMIUM (acento principal de la app) -> Mapeado a Naranja ConsuCalc
val GoldAccent = ConsuOrange
val GoldAccentLight = Color(0xFFFFB07F)   // Naranja claro
val GoldAccentDark = Color(0xFFD84A00)    // Naranja oscuro

// COLORES DE ESTADO (funcionales, no identidad visual)
val SuccessGreen = Color(0xFF10B981)  // Esmeralda limpio
val WarningOrange = Color(0xFFF59E0B) // Ámbar limpio
val ErrorRed = Color(0xFFEF4444)      // Rojo limpio