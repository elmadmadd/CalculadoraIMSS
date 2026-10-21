package com.example.consucalcu.academy
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.example.consucalcu.ui.components.JuicyCard
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager

import kotlinx.coroutines.delay

/**
 * ACADEMY UI - Interfaz visual de la Academia
 * Pantalla principal con camino de aprendizaje tipo Duolingo
 */

@Composable
fun AcademyScreen(
    modifier: Modifier = Modifier,
    onImmersiveModeChange: (Boolean) -> Unit = {}
) {
    val soundManager = LocalSoundManager.current
    val viewModel: AcademyViewModel = viewModel()

    val progress by viewModel.progress.collectAsState()
    val modules by viewModel.modules.collectAsState()
    val currentBattle by viewModel.currentBattle.collectAsState()

    var selectedLessonId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedResourceId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedLesson = selectedLessonId?.let(AcademyContent::getLessonById)
    val selectedResource = selectedResourceId?.let(AcademyContent::getLessonById)

    // Si Android recrea el proceso durante una batalla, reconstruir un estado
    // jugable a partir del identificador guardado por Compose.
    LaunchedEffect(selectedLessonId) {
        if (selectedLesson?.type == LessonType.BATTLE && currentBattle == null) {
            viewModel.startBattle(selectedLesson.id)
        }
    }

    LaunchedEffect(selectedLesson, currentBattle) {
        onImmersiveModeChange(selectedLesson != null || currentBattle != null)
    }

    DisposableEffect(Unit) {
        onDispose { onImmersiveModeChange(false) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Pantalla principal con camino de aprendizaje
        if (currentBattle == null && selectedLesson == null) {
            AcademyMainScreen(
                progress = progress,
                modules = modules,
                onLessonClick = { lesson ->
                    when (lesson.type) {
                        LessonType.QUIZ -> selectedLessonId = lesson.id
                        LessonType.BATTLE -> {
                            viewModel.startBattle(lesson.id)
                            selectedLessonId = lesson.id
                        }
                        LessonType.RESOURCE -> {
                            selectedResourceId = lesson.id
                        }
                        LessonType.PRACTICE -> selectedLessonId = lesson.id
                    }
                    soundManager?.play("pop")
                }
            )
        }

        // Pantalla de batalla RPG
        val activeBattle = currentBattle
        val battleLesson = selectedLesson
        if (activeBattle != null && battleLesson != null) {
            BattleScreen(
                lesson = battleLesson,
                battleState = activeBattle,
                onAnswer = { isCorrect ->
                    viewModel.answerBattleQuestion(battleLesson.id, isCorrect)
                    soundManager?.play(if (isCorrect) "ui_success" else "ui_error")
                },
                onExit = {
                    viewModel.endBattle()
                    selectedLessonId = null
                    soundManager?.play("pop")
                }
            )
        }

        // Pantalla de cuestionario normal
        if (currentBattle == null && selectedLesson?.type == LessonType.QUIZ) {
            val activeLesson = selectedLesson
            QuizScreen(
                lesson = activeLesson,
                onComplete = { correctAnswers, totalQuestions ->
                    viewModel.completeLesson(activeLesson.id, correctAnswers, totalQuestions)
                    selectedLessonId = null
                    soundManager?.play("academy_complete")
                },
                onExit = {
                    selectedLessonId = null
                    soundManager?.play("pop")
                }
            )
        }

        // Diálogo de recursos
        selectedResource?.let { activeResource ->
            ResourceDialog(
                resource = activeResource,
                isCompleted = activeResource.id in progress.completedLessons,
                onMarkComplete = {
                    viewModel.completeLesson(activeResource.id, 1, 1)
                    selectedResourceId = null
                    soundManager?.play("academy_complete")
                },
                onDismiss = {
                    selectedResourceId = null
                    soundManager?.play("pop")
                }
            )
        }

        // Pantalla de Práctica (Credit Match Game)
        if (currentBattle == null && selectedLesson?.type == LessonType.PRACTICE) {
            val activeLesson = selectedLesson
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CreditMatchGame(
                    onCaseSolved = { /* Feedback visual local en el componente */ },
                    onComplete = { correctAnswers, totalCases ->
                        viewModel.completeLesson(activeLesson.id, correctAnswers, totalCases)
                        selectedLessonId = null
                        soundManager?.play("academy_complete")
                    },
                    onClose = {
                        selectedLessonId = null
                        soundManager?.play("pop")
                    }
                )
            }
        }
    }
}

/**
 * Pantalla principal con el camino de aprendizaje
 */
@Composable
private fun AcademyMainScreen(
    progress: AcademyProgress,
    modules: List<Module>,
    onLessonClick: (Lesson) -> Unit
) {
    val soundManager = LocalSoundManager.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)
    ) {
        // Header con stats del usuario
        item {
            AcademyHeader(progress = progress)
        }

        // Módulos con camino de lecciones
        items(modules) { module ->
            ModuleSection(
                module = module,
                onLessonClick = onLessonClick
            )
        }

    }
}

/**
 * Header con estadísticas del usuario
 */
@Composable
private fun AcademyHeader(progress: AcademyProgress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "Tu aprendizaje",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Continúa donde te quedaste",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
            )
        }
        Text(
            text = "${progress.completedLessons.size} completadas",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Sección de módulo con camino de lecciones
 */
@Composable
private fun ModuleSection(
    module: Module,
    onLessonClick: (Lesson) -> Unit
) {
    // Progreso animado del módulo
    val moduleProgress = if (module.totalLessons > 0) {
        module.completedLessons.toFloat() / module.totalLessons.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = moduleProgress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "moduleProgress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
    ) {
        // Header del módulo
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = if (module.isUnlocked) module.color.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
        ) {
            Column(Modifier.padding(ConsuSpacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (module.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${module.completedLessons}/${module.totalLessons} lecciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    if (!module.isUnlocked) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    } else if (moduleProgress >= 1f) {
                        // Badge de completado
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(module.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Barra de progreso del módulo
                if (module.isUnlocked) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(ConsuRadius.track)),
                        color = module.color,
                        trackColor = module.color.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Camino de lecciones (tipo Duolingo)
        if (module.isUnlocked) {
            LearningPath(
                lessons = module.lessons,
                moduleColor = module.color,
                onLessonClick = onLessonClick
            )
        }
    }
}

/**
 * Camino de aprendizaje con lecciones conectadas
 */
@Composable
private fun LearningPath(
    lessons: List<Lesson>,
    moduleColor: Color,
    onLessonClick: (Lesson) -> Unit
) {
    val soundManager = LocalSoundManager.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
    ) {
        lessons.forEachIndexed { index, lesson ->
            val available = !lesson.isLocked
            Surface(
                onClick = {
                    if (available) {
                        soundManager?.play("pop")
                        onLessonClick(lesson)
                    } else {
                        soundManager?.play("ui_error")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ConsuRadius.lg),
                color = if (available) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(
                    1.dp,
                    if (available) moduleColor.copy(alpha = 0.28f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                tonalElevation = if (available) 1.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(ConsuSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    lesson.isCompleted -> moduleColor
                                    available -> moduleColor.copy(alpha = 0.14f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                lesson.isLocked -> Icons.Rounded.Lock
                                lesson.isCompleted -> Icons.Rounded.Check
                                lesson.type == LessonType.QUIZ -> Icons.Rounded.Quiz
                                lesson.type == LessonType.BATTLE -> Icons.Rounded.EmojiEvents
                                lesson.type == LessonType.RESOURCE -> Icons.Rounded.Description
                                else -> Icons.Rounded.PlayArrow
                            },
                            contentDescription = null,
                            tint = when {
                                lesson.isCompleted -> Color.White
                                available -> moduleColor
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                lesson.isCompleted -> "Completada"
                                lesson.isLocked -> "Nivel ${lesson.requiredLevel} requerido"
                                else -> "Comenzar lección"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (available) moduleColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (available) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Abrir lección",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Nodo individual de lección en el camino
 */
@Composable
private fun LessonNode(
    lesson: Lesson,
    moduleColor: Color,
    onClick: () -> Unit,
    alignment: Alignment.Horizontal
) {
    val soundManager = LocalSoundManager.current
    // Estado para tooltip de nodo bloqueado
    var showLockedTooltip by remember { mutableStateOf(false) }

    // Animación de escala
    val scale by animateFloatAsState(
        targetValue = if (lesson.isLocked) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Animación de flotación más sutil (4dp en vez de 8dp)
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (!lesson.isLocked && !lesson.isCompleted) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // Animación de pulso para lecciones disponibles
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (!lesson.isLocked && !lesson.isCompleted) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Glow sutil para lecciones disponibles
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (!lesson.isLocked && !lesson.isCompleted) 0.4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Auto-dismiss del tooltip
    LaunchedEffect(showLockedTooltip) {
        if (showLockedTooltip) {
            delay(2000)
            showLockedTooltip = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignment == Alignment.Start) Arrangement.Start else Arrangement.End
    ) {
        // Contenedor con nodo + título
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .offset(y = (-floatOffset).dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .then(
                        if (lesson.isLocked) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        } else if (lesson.isCompleted) {
                            Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(moduleColor, moduleColor.copy(alpha = 0.7f))
                                )
                            )
                        } else {
                            // Lección disponible - con efecto de pulso
                            Modifier
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            moduleColor.copy(alpha = pulseAlpha),
                                            moduleColor.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        }
                    )
                    .border(
                        width = if (!lesson.isLocked && !lesson.isCompleted) 3.dp else 2.dp,
                        color = if (lesson.isLocked) MaterialTheme.colorScheme.outline
                        else moduleColor,
                        shape = CircleShape
                    )
                    .clickable(enabled = true) {
                        if (lesson.isLocked) {
                            showLockedTooltip = true
                            soundManager?.play("ui_error")
                        } else {
                            onClick()
                        }
                    }
                    .padding(ConsuSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (lesson.isLocked) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = when (lesson.type) {
                                LessonType.QUIZ -> Icons.AutoMirrored.Rounded.HelpOutline
                                LessonType.BATTLE -> Icons.Rounded.EmojiEvents
                                LessonType.RESOURCE -> Icons.AutoMirrored.Rounded.ReceiptLong
                                LessonType.PRACTICE -> Icons.Rounded.Loop
                            },
                            contentDescription = lesson.type.name,
                            tint = if (lesson.isCompleted) Color.White else moduleColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (lesson.isCompleted && lesson.stars > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            repeat(lesson.stars) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Estrella",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Título de la lección (NUEVO)
            Spacer(Modifier.height(6.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (lesson.isCompleted) FontWeight.Bold else FontWeight.Medium,
                color = if (lesson.isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                       else if (lesson.isCompleted) moduleColor
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )

            // Tooltip de bloqueo (NUEVO)
            AnimatedVisibility(
                visible = showLockedTooltip,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(ConsuRadius.sm))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔒 Nivel ${lesson.requiredLevel} requerido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para mostrar recursos educativos
 */
@Composable
private fun ResourceDialog(
    resource: Lesson,
    isCompleted: Boolean,
    onMarkComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(ConsuRadius.xxl))
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { /* Consumir el toque dentro del contenido */ })
                    }
                    .padding(ConsuSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                HorizontalDivider()

                // Contenido del recurso
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
                ) {
                    item {
                        Text(
                            text = resource.resourceContent,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("Recurso de Academia", resource.resourceContent)
                        )
                        android.widget.Toast.makeText(context, "Copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar al Portapapeles")
                }

                Button(
                    onClick = onMarkComplete,
                    enabled = !isCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCompleted) "Recurso completado" else "Marcar como revisado")
                }
            }
        }
    }
}
