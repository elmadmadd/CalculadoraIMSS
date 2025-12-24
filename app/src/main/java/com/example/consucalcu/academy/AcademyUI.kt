package com.example.consucalcu.academy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Importar desde MainActivity
import com.example.consucalcu.JuicyOrange
import com.example.consucalcu.JuicyYellow
import com.example.consucalcu.SoundManager

import kotlinx.coroutines.delay

/**
 * ACADEMY UI - Interfaz visual de la Academia
 * Pantalla principal con camino de aprendizaje tipo Duolingo
 */

@Composable
fun AcademyScreen(
    soundManager: SoundManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AcademyViewModel = remember { AcademyViewModel(context) }
    
    val progress by viewModel.progress.collectAsState()
    val modules by viewModel.modules.collectAsState()
    val currentBattle by viewModel.currentBattle.collectAsState()
    
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    var showResourceDialog by remember { mutableStateOf(false) }
    var selectedResource by remember { mutableStateOf<Lesson?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Pantalla principal con camino de aprendizaje
        if (currentBattle == null && selectedLesson == null) {
            AcademyMainScreen(
                progress = progress,
                modules = modules,
                onLessonClick = { lesson ->
                    when (lesson.type) {
                        LessonType.QUIZ -> selectedLesson = lesson
                        LessonType.BATTLE -> {
                            viewModel.startBattle(lesson.id)
                            selectedLesson = lesson
                        }
                        LessonType.RESOURCE -> {
                            selectedResource = lesson
                            showResourceDialog = true
                        }
                        LessonType.PRACTICE -> selectedLesson = lesson
                    }
                    soundManager?.play("pop")
                },
                soundManager = soundManager
            )
        }
        
        // Pantalla de batalla RPG
        if (currentBattle != null && selectedLesson != null) {
            BattleScreen(
                lesson = selectedLesson!!,
                battleState = currentBattle!!,
                onAnswer = { isCorrect ->
                    viewModel.answerBattleQuestion(selectedLesson!!.id, isCorrect)
                    soundManager?.play(if (isCorrect) "success" else "error")
                },
                onExit = {
                    viewModel.endBattle()
                    selectedLesson = null
                    soundManager?.play("pop")
                }
            )
        }
        
        // Pantalla de cuestionario normal
        if (currentBattle == null && selectedLesson != null && selectedLesson!!.type != LessonType.RESOURCE) {
            QuizScreen(
                lesson = selectedLesson!!,
                onComplete = { correctAnswers, totalQuestions ->
                    viewModel.completeLesson(selectedLesson!!.id, correctAnswers, totalQuestions)
                    selectedLesson = null
                    soundManager?.play("success")
                },
                onExit = {
                    selectedLesson = null
                    soundManager?.play("pop")
                }
            )
        }
        
        // Diálogo de recursos
        if (showResourceDialog && selectedResource != null) {
            // Marcar como completado automáticamente al abrir (si no estaba completado)
            LaunchedEffect(selectedResource!!.id) {
                if (!selectedResource!!.isCompleted) {
                    viewModel.completeLesson(selectedResource!!.id, 1, 1) // 100% completado
                }
            }
            
            ResourceDialog(
                resource = selectedResource!!,
                onDismiss = {
                    showResourceDialog = false
                    selectedResource = null
                    soundManager?.play("pop")
                }
            )
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
    onLessonClick: (Lesson) -> Unit,
    soundManager: SoundManager?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con stats del usuario
        item {
            AcademyHeader(progress = progress)
        }
        
        // Módulos con camino de lecciones
        items(modules) { module ->
            ModuleSection(
                module = module,
                onLessonClick = onLessonClick,
                soundManager = soundManager
            )
        }
        
        // Spacer final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Header con estadísticas del usuario
 */
@Composable
private fun AcademyHeader(progress: AcademyProgress) {
    // Animación de gradiente
    val infiniteTransition = rememberInfiniteTransition(label = "headerGradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.primary
                    ),
                    start = Offset(gradientOffset * 1000, 0f),
                    end = Offset((gradientOffset + 1) * 1000, 1000f)
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nivel y XP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nivel ${progress.currentLevel}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${progress.currentXP} / ${progress.xpToNextLevel} XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Racha con animación de escala
            val streakScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "streakScale"
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.scale(streakScale)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Racha",
                    tint = JuicyOrange,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "${progress.streak}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Barra de progreso XP con animación
        LinearProgressIndicator(
            progress = { progress.currentXP.toFloat() / progress.xpToNextLevel.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

/**
 * Sección de módulo con camino de lecciones
 */
@Composable
private fun ModuleSection(
    module: Module,
    onLessonClick: (Lesson) -> Unit,
    soundManager: SoundManager?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header del módulo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 2.dp,
                    color = if (module.isUnlocked) module.color else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.icon,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(module.color.copy(alpha = 0.2f))
                        .padding(8.dp)
                )
                
                Column {
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
            }
            
            if (!module.isUnlocked) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Bloqueado",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        // Camino de lecciones (tipo Duolingo)
        if (module.isUnlocked) {
            LearningPath(
                lessons = module.lessons,
                moduleColor = module.color,
                onLessonClick = onLessonClick,
                soundManager = soundManager
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
    onLessonClick: (Lesson) -> Unit,
    soundManager: SoundManager?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lessons.forEachIndexed { index, lesson ->
            // Animación de entrada escalonada
            var isVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                delay(index * 80L) // Delay escalonado
                isVisible = true
            }
            
            val alpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "alpha"
            )
            
            val translationY by animateFloatAsState(
                targetValue = if (isVisible) 0f else 40f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "translationY"
            )
            
            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    this.translationY = translationY
                }
            ) {
                Column {
                    // Línea conectora (excepto para la primera lección)
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(
                                    if (lesson.isCompleted || !lesson.isLocked) moduleColor.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                .align(if (index % 2 == 0) Alignment.Start else Alignment.End)
                        )
                    }
                    
                    // Nodo de lección
                    LessonNode(
                        lesson = lesson,
                        moduleColor = moduleColor,
                        onClick = { onLessonClick(lesson) },
                        soundManager = soundManager,
                        alignment = if (index % 2 == 0) Alignment.Start else Alignment.End
                    )
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
    soundManager: SoundManager?,
    alignment: Alignment.Horizontal
) {
    // Animación de escala
    val scale by animateFloatAsState(
        targetValue = if (lesson.isLocked) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    // Animación de flotación para lecciones disponibles (no bloqueadas y no completadas)
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (!lesson.isLocked && !lesson.isCompleted) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignment == Alignment.Start) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .offset(y = (-floatOffset).dp)
                .size(80.dp)
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
                    width = if (!lesson.isLocked && !lesson.isCompleted) 4.dp else 3.dp,
                    color = if (lesson.isLocked) MaterialTheme.colorScheme.outline
                    else moduleColor,
                    shape = CircleShape
                )
                .clickable(enabled = !lesson.isLocked) { onClick() }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (lesson.type) {
                        LessonType.QUIZ -> Icons.Rounded.HelpOutline
                        LessonType.BATTLE -> Icons.Rounded.EmojiEvents
                        LessonType.RESOURCE -> Icons.Rounded.ReceiptLong
                        LessonType.PRACTICE -> Icons.Rounded.Loop
                    },
                    contentDescription = lesson.type.name,
                    tint = if (lesson.isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                    else if (lesson.isCompleted) Color.White
                    else moduleColor,
                    modifier = Modifier.size(28.dp)
                )
                
                if (lesson.isCompleted && lesson.stars > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(lesson.stars) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Estrella",
                                tint = JuicyYellow,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
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
    onDismiss: () -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) { /* Prevent dismiss on content click */ }
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = resource.resourceContent,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }
                
                // Botón copiar
                Button(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resource.resourceContent))
                        android.widget.Toast.makeText(context, "Copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar al Portapapeles")
                }
            }
        }
    }
}
