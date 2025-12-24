package com.example.consucalcu.rewards.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.rewards.*

/**
 * REWARDS GALLERY SCREEN
 * 
 * Pantalla principal de recompensas que muestra todas las categorías:
 * - Temas
 * - Efectos de partículas
 * - Avatares
 * - Insignias
 * - Estilos de tarjetas
 */

@Composable
fun RewardsGalleryScreen(
    currentLevel: Int,
    rewardsState: RewardsState,
    onThemeClick: () -> Unit,
    onParticleClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onCardStyleClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏆 Recompensas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Nivel $currentLevel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lista de categorías
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Temas
            item {
                RewardCategoryCard(
                    title = "Temas de Color",
                    emoji = "🎨",
                    unlockedCount = ThemeCatalog.getUnlockedThemes(currentLevel).size,
                    totalCount = ThemeCatalog.ALL_THEMES.size,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF0A84FF), Color(0xFF00D4FF))
                    ),
                    onClick = onThemeClick
                )
            }
            
            // Efectos de partículas
            item {
                RewardCategoryCard(
                    title = "Efectos de Partículas",
                    emoji = "✨",
                    unlockedCount = ParticleCatalog.getUnlockedParticles(currentLevel).size,
                    totalCount = ParticleCatalog.ALL_PARTICLES.size,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF6D00), Color(0xFFFFD700))
                    ),
                    onClick = onParticleClick
                )
            }
            
            // Avatares
            item {
                RewardCategoryCard(
                    title = "Avatares",
                    emoji = "👤",
                    unlockedCount = AvatarCatalog.getUnlockedAvatars(currentLevel).size,
                    totalCount = AvatarCatalog.ALL_AVATARS.size,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF7C4DFF), Color(0xFFE040FB))
                    ),
                    onClick = onAvatarClick
                )
            }
            
            // Estilos de tarjetas
            item {
                RewardCategoryCard(
                    title = "Estilos de Tarjetas",
                    emoji = "💳",
                    unlockedCount = CardStyleCatalog.getUnlockedCardStyles(currentLevel).size,
                    totalCount = CardStyleCatalog.ALL_CARD_STYLES.size,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E676), Color(0xFF00BFA5))
                    ),
                    onClick = onCardStyleClick
                )
            }
            
            // Insignias (solo visualización)
            item {
                BadgesSection(
                    currentLevel = currentLevel,
                    unlockedBadges = rewardsState.unlocked.badges
                )
            }
        }
    }
}

@Composable
private fun RewardCategoryCard(
    title: String,
    emoji: String,
    unlockedCount: Int,
    totalCount: Int,
    gradient: Brush,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(gradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Emoji
                    Text(
                        text = emoji,
                        fontSize = 48.sp
                    )
                    
                    // Info
                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "$unlockedCount/$totalCount desbloqueados",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Flecha
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Ver más",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun BadgesSection(
    currentLevel: Int,
    unlockedBadges: Set<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏅",
                fontSize = 32.sp
            )
            
            Column {
                Text(
                    text = "Insignias",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${unlockedBadges.size}/${BadgeCatalog.ALL_BADGES.size} coleccionadas",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Grid de badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgeCatalog.ALL_BADGES.forEach { badge ->
                val isUnlocked = badge.unlockLevel <= currentLevel
                
                BadgeItem(
                    badge = badge,
                    isUnlocked = isUnlocked,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: Badge,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) {
                        when (badge.rarity) {
                            BadgeRarity.COMMON -> Brush.linearGradient(
                                colors = listOf(Color(0xFFCD7F32), Color(0xFFB87333))
                            )
                            BadgeRarity.RARE -> Brush.linearGradient(
                                colors = listOf(Color(0xFFC0C0C0), Color(0xFFB0B0B0))
                            )
                            BadgeRarity.EPIC -> Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300))
                            )
                            BadgeRarity.LEGENDARY -> Brush.linearGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            )
                        }
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2D3E5F), Color(0xFF1A2F3F))
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isUnlocked) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) badge.emoji else "🔒",
                fontSize = 28.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (isUnlocked) badge.name else "Nivel ${badge.unlockLevel}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
