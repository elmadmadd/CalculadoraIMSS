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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.rewards.*

/**
 * PARTICLE SELECTOR SCREEN
 * 
 * Pantalla para seleccionar efectos de partículas desbloqueados.
 */

@Composable
fun ParticleSelectorScreen(
    currentLevel: Int,
    rewardsState: RewardsState,
    onParticleSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Safe access to catalog
    val allParticles = remember {
        try {
            ParticleCatalog.ALL_PARTICLES
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val unlockedParticles = remember(currentLevel) {
        try {
            ParticleCatalog.getUnlockedParticles(currentLevel)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val activeParticleId = rewardsState.active.currentParticle
    
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
                text = "✨ Efectos de Partículas",
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
            text = "Nivel $currentLevel • ${unlockedParticles.size}/${allParticles.size} desbloqueados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Grid de efectos
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(allParticles) { particle ->
                val isUnlocked = particle.unlockLevel <= currentLevel
                val isActive = particle.id == activeParticleId
                
                ParticleCard(
                    particle = particle,
                    isUnlocked = isUnlocked,
                    isActive = isActive,
                    onClick = {
                        if (isUnlocked) {
                            onParticleSelected(particle.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ParticleCard(
    particle: ParticleEffect,
    isUnlocked: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isUnlocked) {
                    // Asegurar que siempre haya al menos 2 colores para el gradiente
                    val gradientColors = when {
                        particle.colors.size >= 2 -> particle.colors.take(2)
                        particle.colors.size == 1 -> listOf(particle.colors[0], particle.colors[0].copy(alpha = 0.7f))
                        else -> listOf(Color(0xFF2D3E5F), Color(0xFF1A2F3F))
                    }
                    Brush.linearGradient(colors = gradientColors)
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2D3E5F),
                            Color(0xFF1A2F3F)
                        )
                    )
                }
            )
            .border(
                width = if (isActive) 3.dp else 1.dp,
                color = if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isUnlocked
            ) {
                isPressed = true
                onClick()
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji o icono de bloqueo
            Text(
                text = if (isUnlocked) particle.emoji else "🔒",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Nombre del efecto
            Text(
                text = particle.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Nivel requerido o estado activo
            if (!isUnlocked) {
                Text(
                    text = "Nivel ${particle.unlockLevel}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            } else if (isActive) {
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "ACTIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "${particle.particleCount} partículas",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        // Checkmark si está activo
        AnimatedVisibility(
            visible = isActive,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Activo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
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
