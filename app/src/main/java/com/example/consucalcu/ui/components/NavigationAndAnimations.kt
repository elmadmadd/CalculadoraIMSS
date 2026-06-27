package com.example.consucalcu.ui.components

import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.consucalcu.LocalHapticManager
import com.example.consucalcu.ConsuNavy
import com.example.consucalcu.ConsuOrange
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun AnimatedResultItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L) // Un poco más lento el stagger para apreciarlo
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { 80 },
            animationSpec = spring(
                dampingRatio = 0.75f, // Ligeramente más bouncy
                stiffness = Spring.StiffnessLow
            )
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.9f)
    ) {
        content()
    }
}

@Composable
fun AnimatedDebtItem(
    content: @Composable () -> Unit
) {
    // Escala + fade para la inserción de filas de deuda
    // Animación más rápida para mantener responsivo el formulario
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(180)
        ),
        exit = fadeOut(tween(120))
    ) {
        content()
    }
}

@Composable
fun AnimatedTabContent(
    tabKey: Int,
    content: @Composable () -> Unit
) {
    // Uses alpha + subtle translationY only — the NavHost slide handles horizontal movement.
    // Scale is removed to avoid dual-axis scaling conflicts during transition
    AnimatedContent(
        targetState = tabKey,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(220, delayMillis = 80)
            ) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = tween(250)
            ) togetherWith fadeOut(
                animationSpec = tween(150)
            )
        },
        label = "tabContent"
    ) { _ ->
        content()
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticManager = LocalHapticManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val selectedIconBg = if (isSystemInDarkTheme()) Color.White else ConsuNavy
    val selectedIconTint = if (isSystemInDarkTheme()) ConsuNavy else Color.White
    val selectedTextColor = ConsuOrange
    val unselectedColor = Color(0xFF94A3B8)
    
    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            selected -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navItemScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!selected) hapticManager?.medium()
                    onClick()
                }
            )
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        // Orange top indicator line
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (selected) ConsuOrange else Color.Transparent)
        )
        
        Spacer(Modifier.height(6.dp))
        
        // Icon container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) selectedIconBg else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = if (selected) selectedIconTint else unselectedColor
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        // Label Text
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedTextColor else unselectedColor
        )
    }
}
