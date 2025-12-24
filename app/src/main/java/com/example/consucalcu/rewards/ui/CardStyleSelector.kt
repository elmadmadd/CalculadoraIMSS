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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.rewards.*

/**
 * CARD STYLE SELECTOR SCREEN
 * 
 * Pantalla para seleccionar estilos de tarjetas desbloqueados.
 */

@Composable
fun CardStyleSelectorScreen(
    currentLevel: Int,
    rewardsState: RewardsState,
    onCardStyleSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedStyles = CardStyleCatalog.getUnlockedCardStyles(currentLevel)
    val activeStyleId = rewardsState.active.currentCardStyle
    
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
            Text(
                text = "💳 Estilos de Tarjetas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Nivel $currentLevel • ${unlockedStyles.size}/${CardStyleCatalog.ALL_CARD_STYLES.size} desbloqueados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lista de estilos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(CardStyleCatalog.ALL_CARD_STYLES) { style ->
                val isUnlocked = style.unlockLevel <= currentLevel
                val isActive = style.id == activeStyleId
                
                CardStyleItem(
                    cardStyle = style,
                    isUnlocked = isUnlocked,
                    isActive = isActive,
                    onClick = {
                        if (isUnlocked) {
                            onCardStyleSelected(style.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CardStyleItem(
    cardStyle: CardStyle,
    isUnlocked: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isUnlocked
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        shadowElevation = if (cardStyle.hasGlow && isUnlocked) 8.dp else 2.dp,
        border = BorderStroke(
            width = if (isActive) 3.dp else cardStyle.borderWidth.dp,
            color = if (isActive) Color(0xFFFFD700) 
                   else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (cardStyle.hasGlassmorphism && isUnlocked) {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Emoji
                    Text(
                        text = if (isUnlocked) cardStyle.emoji else "🔒",
                        fontSize = 40.sp
                    )
                    
                    Column {
                        Text(
                            text = cardStyle.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Características
                        if (isUnlocked) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (cardStyle.hasGlassmorphism) {
                                    FeatureChip("Cristal")
                                }
                                if (cardStyle.hasShimmer) {
                                    FeatureChip("Brillo")
                                }
                                if (cardStyle.hasGlow) {
                                    FeatureChip("Resplandor")
                                }
                            }
                        } else {
                            Text(
                                text = "Nivel ${cardStyle.unlockLevel}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Indicador de activo
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Activo",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
private fun FeatureChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
