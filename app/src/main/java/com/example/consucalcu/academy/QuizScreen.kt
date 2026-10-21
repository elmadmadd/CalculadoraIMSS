package com.example.consucalcu.academy
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importar colores desde MainActivity
import com.example.consucalcu.JuicyGreen
import com.example.consucalcu.JuicyRed
import com.example.consucalcu.JuicyYellow
import com.example.consucalcu.JuicyOrange

import kotlinx.coroutines.delay
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.draw.drawWithContent

/**
 * QUIZ SCREEN - Pantalla de cuestionario normal
 * Sistema de preguntas estilo Duolingo
 */
@Composable
fun QuizScreen(
    lesson: Lesson,
    onComplete: (Int, Int) -> Unit,
    onExit: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var quizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = if (currentQuestionIndex < lesson.questions.size) {
        lesson.questions[currentQuestionIndex]
    } else null

    val progress = (currentQuestionIndex + 1).toFloat() / lesson.questions.size.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Column(verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Salir"
                        )
                    }

                    Text(
                        text = "${currentQuestionIndex + 1} / ${lesson.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Barra de progreso
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(ConsuRadius.xs)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Haptic
            val haptic = LocalHapticFeedback.current

            // Contenido
            if (!quizCompleted && currentQuestion != null) {
                val scrollState = rememberScrollState()

                LaunchedEffect(currentQuestionIndex) {
                    scrollState.scrollTo(0)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(top = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    QuizQuestionCard(
                        question = currentQuestion,
                        selectedAnswer = selectedAnswer,
                        showFeedback = showFeedback,
                        isCorrect = isCorrect,
                        onAnswerSelected = { answer ->
                            if (!showFeedback) {
                                // Haptic al seleccionar
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedAnswer = answer
                                isCorrect = answer == currentQuestion.correctAnswer
                                showFeedback = true
                                if (isCorrect) correctAnswers++
                            }
                        },
                        onContinue = {
                            if (currentQuestionIndex < lesson.questions.size - 1) {
                                currentQuestionIndex++
                                selectedAnswer = null
                                showFeedback = false
                            } else {
                                quizCompleted = true
                            }
                        }
                    )
                }
            }

            // Resultado final
            if (quizCompleted) {
                val stars = StarRating.calculateStars(correctAnswers, lesson.questions.size)
                val passed = stars > 0

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 20.dp, bottom = 16.dp)
                ) {
                    QuizResult(
                        correctAnswers = correctAnswers,
                        totalQuestions = lesson.questions.size,
                        xpEarned = lesson.xpReward,
                        stars = stars,
                        onContinue = {
                            onComplete(correctAnswers, lesson.questions.size)
                        },
                        onRetry = if (!passed) {
                            {
                                currentQuestionIndex = 0
                                correctAnswers = 0
                                selectedAnswer = null
                                showFeedback = false
                                isCorrect = false
                                quizCompleted = false
                            }
                        } else null
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de pregunta del cuestionario
 */
@Composable
private fun QuizQuestionCard(
    question: Question,
    selectedAnswer: String?,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.xl)
    ) {
        // Tipo de pregunta badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(ConsuRadius.xl))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (question.type) {
                    QuestionType.MULTIPLE_CHOICE -> Icons.Rounded.CheckCircle
                    QuestionType.TRUE_FALSE -> Icons.AutoMirrored.Rounded.HelpOutline
                    QuestionType.FILL_BLANK -> Icons.Rounded.Edit
                    QuestionType.MATCH_PAIRS -> Icons.Rounded.Link
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = when (question.type) {
                    QuestionType.MULTIPLE_CHOICE -> "Opción Múltiple"
                    QuestionType.TRUE_FALSE -> "Verdadero/Falso"
                    QuestionType.FILL_BLANK -> "Completar"
                    QuestionType.MATCH_PAIRS -> "Emparejar"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        // Pregunta
        Text(
            text = question.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opciones con animación de entrada escalonada
        Column(verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)) {
            question.options.forEachIndexed { index, option ->
                var isVisible by remember { mutableStateOf(false) }

                LaunchedEffect(question.question) {
                    delay(index * 100L)
                    isVisible = true
                }

                val alpha by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "optionAlpha"
                )

                val translationX by animateFloatAsState(
                    targetValue = if (isVisible) 0f else 50f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "optionTranslationX"
                )

                Box(
                    modifier = Modifier.graphicsLayer {
                        this.alpha = alpha
                        this.translationX = translationX
                    }
                ) {
                    QuizAnswerOption(
                        text = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = showFeedback && option == question.correctAnswer,
                        isWrong = showFeedback && selectedAnswer == option && !isCorrect,
                        enabled = !showFeedback,
                        onClick = { onAnswerSelected(option) }
                    )
                }
            }
        }

        // Feedback y botón continuar
        AnimatedVisibility(
            visible = showFeedback,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ConsuRadius.lg))
                    .background(
                        if (isCorrect) JuicyGreen.copy(alpha = 0.1f)
                        else JuicyRed.copy(alpha = 0.1f)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCorrect) JuicyGreen else JuicyRed,
                        shape = RoundedCornerShape(ConsuRadius.lg)
                    )
                    .padding(ConsuSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = if (isCorrect) JuicyGreen else JuicyRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isCorrect) "¡Excelente!" else "No es correcto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) JuicyGreen else JuicyRed
                    )
                }

                if (question.explanation.isNotEmpty()) {
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrect) JuicyGreen else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(ConsuRadius.md)
                ) {
                    Text(
                        text = "Continuar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Opción de respuesta del cuestionario
 */
@Composable
private fun QuizAnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> JuicyGreen.copy(alpha = 0.15f)
        isWrong -> JuicyRed.copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCorrect -> JuicyGreen
        isWrong -> JuicyRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected && !isCorrect && !isWrong) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ConsuRadius.lg))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(ConsuRadius.lg)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(ConsuSpacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (isCorrect || isWrong) {
                Icon(
                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = if (isCorrect) JuicyGreen else JuicyRed,
                    modifier = Modifier.size(28.dp)
                )
            } else if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Pantalla de resultado del cuestionario
 */
@Composable
private fun QuizResult(
    correctAnswers: Int,
    totalQuestions: Int,
    xpEarned: Int,
    stars: Int,
    onContinue: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val percentage = (correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
    val passed = stars > 0

    var showAnimation by remember { mutableStateOf(false) }

    // Transición infinita para el efecto holográfico (Foil)
    val infiniteTransition = rememberInfiniteTransition(label = "foil")
    val foilOffset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "foilOffset"
    )

    // Haptic al terminar
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(300)
        showAnimation = true
        if (passed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(200)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Si tienes 3 estrellas, es una carta premium
    val isPremiumFoil = passed && stars == 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ConsuRadius.xxl))
            .background(
                if (passed) Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ) else Brush.linearGradient(
                    colors = listOf(Color(0xFF8B0000), JuicyRed) // Rojo más oscuro
                )
            )
            .then(
                // EFECTO HOLOGRÁFICO FOIL
                if (isPremiumFoil) {
                    Modifier.drawWithContent {
                        drawContent()
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color(0xFFFFFFEE).copy(alpha = 0.5f), // Brillo holográfico
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(foilOffset, 0f),
                            end = Offset(foilOffset + 400f, size.height)
                        )
                        drawRect(brush = brush)
                    }
                } else Modifier
            )
            .padding(ConsuSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.xl)
    ) {
        // Emoji animado
        AnimatedVisibility(
            visible = showAnimation,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Text(
                text = if (passed) "🎉" else "😔",
                fontSize = 56.sp
            )
        }

        // Título
        Text(
            text = if (passed) "¡Lección Completada!" else "Necesitas practicar más",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        if (passed) {
            // Estrellas
            Row(
                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                repeat(3) { index ->
                    AnimatedVisibility(
                        visible = showAnimation,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100
                            )
                        )
                    ) {
                        Icon(
                            imageVector = if (index < stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "Estrella",
                            tint = if (index < stars) JuicyYellow else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // Estadísticas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ConsuRadius.lg))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(ConsuSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
        ) {
            StatRow(
                label = "Respuestas correctas",
                value = "$correctAnswers / $totalQuestions"
            )
            StatRow(
                label = "Porcentaje",
                value = "$percentage%"
            )
            if (passed) {
                StatRow(
                    label = "XP ganado",
                    value = "+$xpEarned XP",
                    valueColor = JuicyYellow
                )
            }
        }

        // Botones de acción
        if (passed) {
            // Botón continuar (aprobado)
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(ConsuRadius.md)
            ) {
                Text(
                    text = "Continuar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            // Botón reintentar (reprobado)
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = JuicyOrange
                    ),
                    shape = RoundedCornerShape(ConsuRadius.md)
                ) {
                    Icon(
                        Icons.Rounded.Loop,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Volver a intentar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Botón salir (siempre visible al reprobar)
            TextButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Salir",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Fila de estadística
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
