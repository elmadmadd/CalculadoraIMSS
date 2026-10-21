package com.example.consucalcu

import androidx.compose.ui.graphics.Color

// =================================================================================================
// CONSUCALC DESIGN SYSTEM — SINGLE SOURCE OF TRUTH FOR ALL COLORS (SLATE & ORANGE HARMONY)
// =================================================================================================

// ── PRIMARY BRAND (SLATE & NAVY) ─────────────────────────────────────────────
val ConsuNavy       = Color(0xFF0F172A)   // Slate 900 — Deep Indigo/Navy
val ConsuNavyLight  = Color(0xFF1E293B)   // Slate 800 — Dark Slate Grey
val ConsuNavyDark   = Color(0xFF020617)   // Slate 950 — Almost Black Navy

val ConsuOrange     = Color(0xFFEA580C)   // Naranja Premium — Vibrant corporate accent
val ConsuOrangeDark = Color(0xFFC2410C)   // Naranja Oscuro — Pressed states
val ConsuOrangeLight= Color(0xFFFDBA74)   // Naranja Claro (Orange 300) — Highlights
val ConsuOrangeGlow = Color(0x22EA580C)   // Naranja 13% — Subtle glows

// ── BACKGROUNDS ──────────────────────────────────────────────────────────────
val ConsuBgLight    = Color(0xFFF8FAFC)   // Slate 50 — Soft Slate White
val ConsuBgDark     = Color(0xFF090D16)   // Custom Slate Black — Ultra-dark background

// ── SURFACES (cards, chips, containers) ──────────────────────────────────────
val ConsuSurfaceLight = Color.White              // Pure White for light cards
val ConsuSurfaceDark  = Color(0xFF131B2E)        // Deep Navy-Slate for dark cards
val ConsuSurfaceVarLight = Color(0xFFF1F5F9)     // Slate 100 for light inputs/containers
val ConsuSurfaceVarDark  = Color(0xFF1E293B)      // Slate 800 for dark inputs/containers
val ConsuChipBg     = Color(0xFFE2E8F0)          // Slate 200 for light chips

// ── TEXT ─────────────────────────────────────────────────────────────────────
val ConsuTextPrimary    = Color(0xFF0F172A)  // Slate 900 for primary light text
val ConsuTextLight      = Color(0xFFF8FAFC)  // Slate 50 for primary dark text
val ConsuTextSecondary  = Color(0xFF475569)  // Slate 600 for secondary light text
val ConsuTextMuted      = Color(0xFF64748B)  // Slate 500 for muted text

// ── BORDERS & DIVIDERS ───────────────────────────────────────────────────────
val ConsuDividerLight = Color(0xFFE2E8F0)  // Slate 200 for light dividers
val ConsuDividerDark  = Color(0xFF1E293B)  // Slate 800 for dark dividers

// ── STATE COLORS ─────────────────────────────────────────────────────────────
val ConsuSuccess  = Color(0xFF10B981)  // Emerald 500
val ConsuWarning  = Color(0xFFF59E0B)  // Amber 500
val ConsuError    = Color(0xFFEF4444)  // Red 500

// ── SKELETON LOADING ─────────────────────────────────────────────────────────
val ConsuSkeletonLight     = Color(0xFFE2E8F0)
val ConsuSkeletonHighlight = Color(0xFFF1F5F9)
val ConsuSkeletonDark      = Color(0xFF1E293B)
val ConsuSkeletonDarkHi    = Color(0xFF334155)

// =================================================================================================
// LEGACY ALIASES — kept for backward compatibility, mapped to new Slate palette
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