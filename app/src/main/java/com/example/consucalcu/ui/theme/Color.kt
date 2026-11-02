package com.example.consucalcu

import androidx.compose.ui.graphics.Color

// =================================================================================================
// CONSUCALC DESIGN SYSTEM — SINGLE SOURCE OF TRUTH FOR ALL COLORS (NAVY & ORANGE)
// Paleta de referencia 2026-10-21: fondo #0D203E, tarjetas #193358, acento #EC511B.
// =================================================================================================

// ── PRIMARY BRAND (NAVY) ─────────────────────────────────────────────────────
val ConsuNavy       = Color(0xFF0D203E)   // Azul marino de marca — fondo de referencia
val ConsuNavyLight  = Color(0xFF193358)   // Azul de tarjetas y contenedores
val ConsuNavyDark   = Color(0xFF071428)   // Azul casi negro — degradados

val ConsuOrange     = Color(0xFFEC511B)   // Naranja de marca — acento y acción
val ConsuOrangeDark = Color(0xFFC9400F)   // Naranja oscuro — estados presionados
val ConsuOrangeLight= Color(0xFFF9A47F)   // Naranja claro — resaltados
val ConsuOrangeGlow = Color(0x22EC511B)   // Naranja 13 % — brillos sutiles

// ── BACKGROUNDS ──────────────────────────────────────────────────────────────
val ConsuBgLight    = Color(0xFFF4F7FB)   // Blanco azulado
val ConsuBgDark     = Color(0xFF0A1B35)   // Azul marino un tono bajo la isla, para que se distinga

// ── SURFACES (cards, chips, containers) ──────────────────────────────────────
val ConsuSurfaceLight = Color.White              // Tarjetas en modo claro
val ConsuSurfaceDark  = Color(0xFF193358)        // Tarjetas en modo oscuro
val ConsuSurfaceVarLight = Color(0xFFEDF2F8)     // Campos y contenedores claros
val ConsuSurfaceVarDark  = Color(0xFF213F69)      // Campos y contenedores oscuros
val ConsuChipBg     = Color(0xFFE3EAF3)          // Chips claros

// ── TEXT ─────────────────────────────────────────────────────────────────────
val ConsuTextPrimary    = Color(0xFF0D203E)  // Texto principal en claro
val ConsuTextLight      = Color(0xFFF4F7FB)  // Texto principal en oscuro
val ConsuTextSecondary  = Color(0xFF4A5D7A)  // Texto secundario en claro
val ConsuTextMuted      = Color(0xFF6B7C96)  // Texto atenuado

// ── BORDERS & DIVIDERS ───────────────────────────────────────────────────────
val ConsuDividerLight = Color(0xFFDDE5EF)
val ConsuDividerDark  = Color(0xFF24416B)

// ── STATE COLORS ─────────────────────────────────────────────────────────────
val ConsuSuccess  = Color(0xFF10B981)  // Emerald 500
val ConsuSuccessDark = Color(0xFF047857)  // Emerald 700 — fondo con texto blanco
val ConsuWarning  = Color(0xFFF59E0B)  // Amber 500
val ConsuError    = Color(0xFFEF4444)  // Red 500

// ── SKELETON LOADING ─────────────────────────────────────────────────────────
val ConsuSkeletonLight     = Color(0xFFDDE5EF)
val ConsuSkeletonHighlight = Color(0xFFEDF2F8)
val ConsuSkeletonDark      = Color(0xFF193358)
val ConsuSkeletonDarkHi    = Color(0xFF26446F)

// =================================================================================================
// LEGACY ALIASES — kept for backward compatibility, mapped to the navy palette
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