package com.example.consucalcu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.consucalcu.ConsuOrange
import kotlin.math.PI
import kotlin.math.sin

// =================================================================================================
// 1. DATA MODELS & SHAPE
// =================================================================================================

data class IndentShapeData(
    val xIndent: Float = 0f,
    val yIndent: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val ballOffset: Float = 0f,
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

        val start = translate(x = 0f, y = 0f)
        val middle = translate(x = 55f, y = 34f)
        val end = translate(x = 110f, y = 0f)

        val control1 = translate(x = 23f, y = 0f)
        val control2 = translate(x = 39f, y = 34f)
        val control3 = translate(x = 71f, y = 34f)
        val control4 = translate(x = 87f, y = 0f)

        cubicTo(control1.x, control1.y, control2.x, control2.y, middle.x, middle.y)
        cubicTo(control3.x, control3.y, control4.x, control4.y, end.x, end.y)

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
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    ballColor: Color = ConsuOrange,
    cornerRadius: Dp = 24.dp,
    ballSize: Dp = 48.dp,
    indentWidth: Dp = 80.dp,
    indentHeight: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    var itemPositions by remember { mutableStateOf(listOf<Offset>()) }
    
    val selectedItemOffset by remember(selectedIndex, itemPositions) {
        derivedStateOf {
            if (itemPositions.isNotEmpty() && selectedIndex < itemPositions.size) {
                itemPositions[selectedIndex]
            } else {
                Offset.Zero
            }
        }
    }

    // Animation for indent movement
    val animatedX by animateFloatAsState(
        targetValue = selectedItemOffset.x,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "indentX"
    )

    val indentShape = IndentRectShape(
        indentShapeData = IndentShapeData(
            xIndent = animatedX,
            yIndent = with(density) { indentHeight.toPx() },
            width = with(density) { indentWidth.toPx() },
            cornerRadius = with(density) { cornerRadius.toPx() }
        )
    )

    Box(modifier = modifier) {
        // Main Bar Surface with animated Indent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    shadowElevation = 10f
                    shape = indentShape
                    clip = true
                }
                .background(containerColor)
        )

        // The Floating Ball
        Box(
            modifier = Modifier
                .offset {
                    androidx.compose.ui.unit.IntOffset(
                        x = (animatedX - with(density) { ballSize.toPx() / 2 }).toInt(),
                        y = with(density) { - (ballSize.toPx() / 3) }.toInt() // Lifted half way
                    )
                }
                .size(ballSize)
                .clip(CircleShape)
                .background(ballColor)
                .graphicsLayer {
                    // Small pop animation on index change
                    val scale = 1f // Could add more complex logic here
                },
            contentAlignment = Alignment.Center
        ) {
            // Optional: current icon could be mirrored here inside the ball
        }

        // Layout for Items
        Layout(
            content = content,
            measurePolicy = MeasurePolicy { measurables, constraints ->
                val itemWidth = constraints.maxWidth / measurables.size
                val placeables = measurables.map { it.measure(constraints.copy(minWidth = itemWidth, maxWidth = itemWidth)) }
                
                layout(constraints.maxWidth, constraints.maxHeight) {
                    val positions = mutableListOf<Offset>()
                    var xPosition = 0
                    placeables.forEach { placeable ->
                        placeable.placeRelative(xPosition, 0)
                        positions.add(Offset(xPosition + placeable.width / 2f, 0f))
                        xPosition += placeable.width
                    }
                    itemPositions = positions
                }
            }
        )
    }
}
