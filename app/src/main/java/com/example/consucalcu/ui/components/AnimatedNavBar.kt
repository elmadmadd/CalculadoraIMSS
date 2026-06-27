package com.example.consucalcu.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    ballColor: Color = ConsuOrange,
    iconInBallColor: Color = Color.White,
    cornerRadius: Dp = 28.dp,
    ballSize: Dp = 56.dp,
    indentWidth: Dp = 86.dp,
    indentHeight: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    var itemCentersXPx by remember { mutableStateOf(listOf<Float>()) }

    val targetXPx by remember(selectedIndex, itemCentersXPx) {
        derivedStateOf {
            if (itemCentersXPx.isNotEmpty() && selectedIndex < itemCentersXPx.size) {
                itemCentersXPx[selectedIndex]
            } else {
                0f
            }
        }
    }

    // Buttery smooth spring — low stiffness for slow glide, high damping to avoid bounce
    val animatedX by animateFloatAsState(
        targetValue = targetXPx,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessLow
        ),
        label = "navX"
    )

    // Subtle scale "pop" when the ball arrives at a new tab
    val ballScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ballPop"
    )
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

    val indentShape = IndentRectShape(
        indentShapeData = IndentShapeData(
            xIndent = animatedX,
            yIndent = with(density) { indentHeight.toPx() },
            width = with(density) { indentWidth.toPx() },
            cornerRadius = with(density) { cornerRadius.toPx() }
        )
    )

    val barHeightDp = 72.dp
    val ballSizePx = with(density) { ballSize.toPx() }

    Box(
        modifier = modifier
            .height(barHeightDp)
            .fillMaxWidth()
    ) {
        // 1. Bar background with shadow and indent shape clip
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 12.dp,
                    shape = indentShape,
                    clip = false
                )
                .background(containerColor, shape = indentShape)
        )

        // 2. Floating ball with the SELECTED ICON inside it
        Box(
            modifier = Modifier
                .offset {
                    androidx.compose.ui.unit.IntOffset(
                        x = (animatedX - ballSizePx / 2f).toInt(),
                        y = (-ballSizePx / 2.2f).toInt()
                    )
                }
                .scale(animatedBallScale)
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
            // Quick crossfade for the icon swap
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
                    modifier = Modifier.size(24.dp),
                    tint = iconInBallColor
                )
            }
        }

        // 3. Custom Layout that measures items and reports their center-X
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = content,
            measurePolicy = MeasurePolicy { measurables, constraints ->
                val itemWidth = constraints.maxWidth / measurables.size
                val placeables = measurables.map {
                    it.measure(constraints.copy(minWidth = itemWidth, maxWidth = itemWidth))
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val centers = mutableListOf<Float>()
                    var x = 0
                    placeables.forEach { placeable ->
                        placeable.placeRelative(x, 0)
                        centers.add(x + placeable.width / 2f)
                        x += placeable.width
                    }
                    itemCentersXPx = centers
                }
            }
        )
    }
}

// =================================================================================================
// 3. INDIVIDUAL NAV BAR ITEM
// =================================================================================================

@Composable
fun AnimatedNavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unselectedColor: Color = Color.Gray
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                if (!selected) {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // AnimatedVisibility: slides down + fades in when deselected,
        // slides up + fades out when selected (absorbed into the ball)
        AnimatedVisibility(
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
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Medium,
                    color = unselectedColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
