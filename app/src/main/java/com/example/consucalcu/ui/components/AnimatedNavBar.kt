package com.example.consucalcu.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.ConsuOrange
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.abs

// =================================================================================================
// 1. DATA MODELS & SHAPE
// =================================================================================================

data class IndentShapeData(
    val xIndent: Float = 0f,
    val yIndent: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val cornerRadius: Float = 0f
)

class IndentRectShape(
    private val indentShapeData: IndentShapeData,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        return Outline.Generic(
            Path().addRoundRectWithIndent(size, indentShapeData)
        )
    }
}

fun Path.addRoundRectWithIndent(
    size: Size,
    indentShapeData: IndentShapeData,
): Path {
    val width = size.width
    val height = size.height
    val cornerRadius = indentShapeData.cornerRadius
    val indentWidth = indentShapeData.width
    val indentHeight = indentShapeData.yIndent
    val xOffset = indentShapeData.xIndent - (indentWidth / 2)

    return apply {
        reset()
        // Start top-left
        moveTo(0f, cornerRadius)

        // Left side
        arcTo(
            rect = Rect(Offset(0f, 0f), Size(cornerRadius * 2, cornerRadius * 2)),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Top side - Indent
        lineTo(xOffset, 0f)

        // BEZIER INDENT
        val maxX = 110f
        val maxY = 34f

        fun translate(x: Float, y: Float): Offset {
            return Offset(
                x = ((x / maxX) * indentWidth) + xOffset,
                y = ((y / maxY) * indentHeight)
            )
        }

        val middle = translate(x = 55f, y = 34f)

        val control1 = translate(x = 23f, y = 0f)
        val control2 = translate(x = 39f, y = 34f)
        val control3 = translate(x = 71f, y = 34f)
        val control4 = translate(x = 87f, y = 0f)

        cubicTo(control1.x, control1.y, control2.x, control2.y, middle.x, middle.y)
        cubicTo(control3.x, control3.y, control4.x, control4.y, (xOffset + indentWidth), 0f)

        lineTo(width - cornerRadius, 0f)

        // Top-right corner
        arcTo(
            rect = Rect(Offset(width - cornerRadius * 2, 0f), Size(cornerRadius * 2, cornerRadius * 2)),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Right side
        lineTo(width, height - cornerRadius)

        // Bottom-right corner
        arcTo(
            rect = Rect(Offset(width - cornerRadius * 2, height - cornerRadius * 2), Size(cornerRadius * 2, cornerRadius * 2)),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Bottom side
        lineTo(cornerRadius, height)

        // Bottom-left corner
        arcTo(
            rect = Rect(Offset(0f, height - cornerRadius * 2), Size(cornerRadius * 2, cornerRadius * 2)),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        close()
    }
}

// =================================================================================================
// 2. ANIMATED NAV BAR CONTAINER
// =================================================================================================

@Composable
fun AnimatedNavigationBar(
    selectedIndex: Int,
    selectedIcon: ImageVector,
    itemCount: Int, // Added to fix calculation bug
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    ballColor: Color = ConsuOrange,
    iconInBallColor: Color = Color.White,
    cornerRadius: Dp = 28.dp,
    ballSize: Dp = 56.dp,
    indentWidth: Dp = 72.dp,
    indentHeight: Dp = 24.dp,
    content: @Composable RowScope.() -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()

        val targetXPx by remember(selectedIndex, itemCount, totalWidthPx) {
            derivedStateOf {
                if (itemCount > 0 && totalWidthPx > 0) {
                    val itemWidth = totalWidthPx / itemCount
                    (selectedIndex + 0.5f) * itemWidth
                } else {
                    0f
                }
            }
        }

    // Velocity-aware spring drive — using an Animatable directly (instead of animateFloatAsState)
    // lets us read the live velocity each frame to drive squash & stretch below.
    val ballX = remember { Animatable(targetXPx) }
    LaunchedEffect(targetXPx) {
        ballX.animateTo(
            targetValue = targetXPx,
            animationSpec = spring(
                dampingRatio = 0.82f, // Slightly less damping for a livelier feel
                stiffness = Spring.StiffnessLow
            )
        )
    }
    val animatedX = ballX.value
    // How much the ball stretches horizontally / squashes vertically while it's travelling
    val travelStretch = (abs(ballX.velocity) / 5000f).coerceIn(0f, 0.32f)


    // Trigger the pop by keying on selectedIndex
    var ballPopTrigger by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(selectedIndex) {
        ballPopTrigger = 0.92f  // shrink slightly
        kotlinx.coroutines.delay(50)
        ballPopTrigger = 1.05f  // overshoot
        kotlinx.coroutines.delay(120)
        ballPopTrigger = 1f     // settle
    }
    val animatedBallScale by animateFloatAsState(
        targetValue = ballPopTrigger,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ballPopScale"
    )

    // Icon "flick" — a quick spring-back rotation whenever the selected icon changes
    val iconRotation = remember { Animatable(0f) }
    LaunchedEffect(selectedIcon) {
        iconRotation.snapTo(-150f)
        iconRotation.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        )
    }

    // Breathing halo behind the ball, with a brighter flash on tab change
    val glowPulse by rememberInfiniteTransition(label = "glowPulse").animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulseAlpha"
    )
    val glowBoost = remember { Animatable(0f) }
    LaunchedEffect(selectedIndex) {
        glowBoost.snapTo(1f)
        glowBoost.animateTo(0f, animationSpec = tween(650, easing = FastOutSlowInEasing))
    }

    val clampedX = animatedX.coerceIn(
        with(density) { cornerRadius.toPx() + indentWidth.toPx() / 2f },
        totalWidthPx - with(density) { cornerRadius.toPx() + indentWidth.toPx() / 2f }
    )

    val indentShape = IndentRectShape(
        indentShapeData = IndentShapeData(
            xIndent = clampedX,
            yIndent = with(density) { indentHeight.toPx() },
            width = with(density) { indentWidth.toPx() },
            cornerRadius = with(density) { cornerRadius.toPx() }
        )
    )

    val ballSizePx = with(density) { ballSize.toPx() }
    val ballCenterYPx = -ballSizePx / 2.2f + ballSizePx / 2f
    val glowSizePx = ballSizePx * 2f
    val glowAlpha = (glowPulse * 0.5f + glowBoost.value * 0.5f).coerceIn(0f, 1f)
    val glowScale = 1f + glowBoost.value * 0.25f

    // 1. Bar background with shadow and indent shape clip
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp, // Reduced for elegance
                shape = indentShape,
                clip = false
            )
            .background(containerColor, shape = indentShape)
    )

    // 2. Pulsing glow halo behind the ball
    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    x = (clampedX - glowSizePx / 2f).toInt(),
                    y = (ballCenterYPx - glowSizePx / 2f).toInt()
                )
            }
            .size(with(density) { glowSizePx.toDp() })
            .graphicsLayer {
                scaleX = glowScale
                scaleY = glowScale
                alpha = glowAlpha
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(ballColor.copy(alpha = 0.55f), ballColor.copy(alpha = 0f))
                ),
                shape = CircleShape
            )
    )

    // 3. Floating ball with the SELECTED ICON inside it
    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    x = (clampedX - ballSizePx / 2f).toInt(),
                    y = (-ballSizePx / 2.2f).toInt()
                )
            }
                .graphicsLayer {
                    // Combine the arrival "pop" with velocity-driven squash & stretch
                    scaleX = animatedBallScale * (1f + travelStretch)
                    scaleY = animatedBallScale * (1f - travelStretch * 0.55f)
                }
                .size(ballSize)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = ballColor.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(ballColor),
            contentAlignment = Alignment.Center
        ) {
            // Crossfade for the icon swap, with a quick flick rotation for extra flourish
            Crossfade(
                targetState = selectedIcon,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "ballIconCrossfade"
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = iconRotation.value },
                    tint = iconInBallColor
                )
            }
        }

        // 4. Simple Row for items
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

// =================================================================================================
// 3. INDIVIDUAL NAV BAR ITEM
// =================================================================================================

@Composable
fun RowScope.AnimatedNavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unselectedColor: Color = Color.Gray
) {
    val hapticManager = com.example.consucalcu.LocalHapticManager.current

    Box(
        modifier = modifier
            .weight(1f) // Added weight to ensure equal distribution
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                if (!selected) {
                    hapticManager?.medium()
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Disambiguate AnimatedVisibility if needed, usually just top-level
        androidx.compose.animation.AnimatedVisibility(
            visible = !selected,
            enter = slideInVertically(
                initialOffsetY = { -it / 2 },  // subtle slide from above
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = 150,  // wait for ball to start moving
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 350,
                    delayMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(
                    durationMillis = 350,
                    delayMillis = 150,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it / 2 },  // subtle slide upward
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutLinearInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutLinearInEasing
                )
            ) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutLinearInEasing
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = unselectedColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = unselectedColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
