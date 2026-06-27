package com.example.consucalcu

import androidx.compose.ui.graphics.Color

// =================================================================================================
// CONSUCALC DESIGN SYSTEM — SINGLE SOURCE OF TRUTH FOR ALL COLORS
// =================================================================================================

// ── PRIMARY BRAND ────────────────────────────────────────────────────────────
val ConsuNavy       = Color(0xFF0B1F44)   // Azul Marino Profundo — primary identity
val ConsuNavyLight  = Color(0xFF1B3564)   // Azul Marino Claro — gradients, secondary dark surfaces
val ConsuNavyDark   = Color(0xFF081833)   // Azul Marino Ultra Profundo — deepest backgrounds

val ConsuOrange     = Color(0xFFF95B00)   // Naranja Vibrante — accent, CTAs, selected states
val ConsuOrangeDark = Color(0xFFD84A00)   // Naranja Oscuro — pressed states, shadows
val ConsuOrangeLight= Color(0xFFFFB07F)   // Naranja Claro — highlights, glows
val ConsuOrangeGlow = Color(0x33F95B00)   // Naranja 20% — glow effects

// ── BACKGROUNDS ──────────────────────────────────────────────────────────────
val ConsuBgLight    = Color(0xFFF5F7FB)   // Fondo modo claro — scaffold background
val ConsuBgDark     = Color(0xFF0A0F1D)   // Fondo modo oscuro — scaffold background

// ── SURFACES (cards, chips, containers) ──────────────────────────────────────
val ConsuSurfaceLight = Color.White              // Tarjetas modo claro
val ConsuSurfaceDark  = Color(0xFF101B3A)        // Tarjetas modo oscuro
val ConsuSurfaceVarLight = Color(0xFFF2F4F8)     // Surface variant claro (settings rows, etc.)
val ConsuSurfaceVarDark  = Color(0xFF162040)      // Surface variant oscuro
val ConsuChipBg     = Color(0xFFF0F3F8)          // Chips, botones secundarios modo claro

// ── TEXT ─────────────────────────────────────────────────────────────────────
val ConsuTextPrimary    = Color(0xFF0B1F44)  // Texto principal modo claro (= ConsuNavy)
val ConsuTextLight      = Color(0xFFF4F4F5)  // Texto principal modo oscuro
val ConsuTextSecondary  = Color(0xFF64748B)  // Texto secundario / placeholders / hints
val ConsuTextMuted      = Color(0xFF94A3B8)  // Texto muy tenue (disabled, captions)

// ── BORDERS & DIVIDERS ───────────────────────────────────────────────────────
val ConsuDividerLight = Color(0xFFE2E8F0)  // Bordes modo claro
val ConsuDividerDark  = Color(0xFF1E293B)  // Bordes modo oscuro

// ── STATE COLORS (functional, not brand identity) ────────────────────────────
val ConsuSuccess  = Color(0xFF10B981)  // Esmeralda — éxito, habilitado
val ConsuWarning  = Color(0xFFF59E0B)  // Ámbar — advertencia
val ConsuError    = Color(0xFFEF4444)  // Rojo — error, destructivo

// ── SKELETON LOADING ─────────────────────────────────────────────────────────
val ConsuSkeletonLight     = Color(0xFFE2E8F0)  // Base skeleton modo claro
val ConsuSkeletonHighlight = Color(0xFFF0F3F8)  // Highlight skeleton modo claro
val ConsuSkeletonDark      = Color(0xFF1E293B)  // Base skeleton modo oscuro
val ConsuSkeletonDarkHi    = Color(0xFF263354)  // Highlight skeleton modo oscuro

// =================================================================================================
// LEGACY ALIASES — kept for backward compatibility, will be removed progressively
// =================================================================================================
val JuicyBlue = ConsuNavy
val JuicyBlueShadow = ConsuNavyLight
val JuicyGreen = ConsuNavyLight
val JuicyGreenShadow = ConsuNavy
val JuicyOrange = ConsuOrange
val JuicyOrangeShadow = ConsuOrangeDark
val JuicyOrangeGlow = ConsuOrangeGlow
val JuicyRed = Color(0xFFBE123C)
val JuicyRedShadow = Color(0xFF9F1239)
val JuicyRedGlow = Color(0xFFFDA4AF)
val JuicyPurple = ConsuNavy
val JuicyPurpleShadow = ConsuNavyLight
val JuicyPurpleGlow = ConsuOrangeGlow
val JuicyCyan = ConsuNavyLight
val JuicyPink = ConsuOrange
val JuicyYellow = ConsuOrange
val JuicyLime = ConsuNavyLight

// Legacy names pointing to new tokens
val BgLight = ConsuBgLight
val BgDark = ConsuBgDark
val SurfaceLight = ConsuSurfaceLight
val SurfaceDark = ConsuSurfaceDark
val TextDark = ConsuTextPrimary
val TextLight = ConsuTextLight
val BorderLight = ConsuDividerLight
val BorderDark = ConsuDividerDark
val GoldAccent = ConsuOrange
val GoldAccentLight = ConsuOrangeLight
val GoldAccentDark = ConsuOrangeDark
val SuccessGreen = ConsuSuccess
val WarningOrange = ConsuWarning
val ErrorRed = ConsuError