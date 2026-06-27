package com.example.consucalcu.ui.screens

import com.example.consucalcu.R
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.model.*
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.viewmodel.*

/** Datos del asesor recopilados en el onboarding */
private data class OnboardingData(
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val supervisor: String = ""
)

@Composable
fun MandatoryConfigScreen(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    val isDark = isSystemInDarkTheme()
    val hapticManager = LocalHapticManager.current

    // ── Estado de pasos (0, 1, 2) ──
    var step by remember { mutableIntStateOf(0) }
    var data by remember { mutableStateOf(OnboardingData()) }

    // Validaciones por paso
    val step0Valid = data.name.isNotBlank()
    val step1Valid = data.username.isNotBlank() && data.password.isNotBlank()
    val step2Valid = data.supervisor.isNotBlank()
    val currentStepValid = when (step) {
        0 -> step0Valid
        1 -> step1Valid
        2 -> step2Valid
        else -> false
    }

    // ── Mascot escala según el paso ──
    val mascotScale by animateFloatAsState(
        targetValue = if (step == 0) 1f else 0.72f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "mascotScale"
    )

    val bgBase = if (isDark) BgDark else BgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor deslizable para la parte superior (evita problemas de tamaño al abrir el teclado)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(56.dp))

                // ── Dots de progreso ──
                OnboardingStepDots(currentStep = step, totalSteps = 3)

                Spacer(Modifier.height(32.dp))

                // ── Mascot / Hero ──
                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = mascotScale; scaleY = mascotScale }
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_mascot),
                        contentDescription = "Buddy",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Contenido por paso con Crossfade ──
                androidx.compose.animation.Crossfade(
                    targetState = step,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    animationSpec = tween(300),
                    label = "stepContent"
                ) { targetStep ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (targetStep) {
                            // ── PASO 1: Bienvenida + Nombre ──
                            0 -> {
                                OnboardingStepHeader(
                                    title = "¡Bienvenido, Asesor!",
                                    subtitle = "Empecemos conociéndonos.\n¿Cuál es tu nombre completo?",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE COMPLETO",
                                    value = data.name,
                                    onValueChange = { data = data.copy(name = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. María García López",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 2: Credenciales ──
                            1 -> {
                                OnboardingStepHeader(
                                    title = "Tus Credenciales",
                                    subtitle = "Se usan para generar el\nlayout de captura automático.",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "USUARIO (BP)",
                                    value = data.username,
                                    onValueChange = { data = data.copy(username = it) },
                                    icon = Icons.Rounded.Badge,
                                    placeholder = "Ej. MGARCIA01",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingInputField(
                                    label = "CONTRASEÑA DEL SISTEMA",
                                    value = data.password,
                                    onValueChange = { data = data.copy(password = it) },
                                    icon = Icons.Rounded.Lock,
                                    placeholder = "Tu contraseña de acceso",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 3: Supervisor + Confirmar ──
                            2 -> {
                                OnboardingStepHeader(
                                    title = "Último Detalle",
                                    subtitle = "¿Quién es tu supervisor?\nAparecerá en tus reportes.",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE DEL SUPERVISOR",
                                    value = data.supervisor,
                                    onValueChange = { data = data.copy(supervisor = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. Carlos Ramírez",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingProfileSummary(data = data, isDark = isDark)
                            }
                        }
                    }
                }
            }

            // ── Botones de navegación ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón ATRÁS
                if (step > 0) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f)
                                else Color.Black.copy(alpha = 0.06f)
                            )
                            .bouncyClick {
                                hapticManager?.light()
                                soundManager.play("pop")
                                if (step > 0) {
                                    step--
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowRight,
                            contentDescription = "Atrás",
                            tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.6f),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { scaleX = -1f }
                        )
                    }
                }

                // Botón SIGUIENTE / COMENZAR
                val isLastStep = step == 2
                val btnColor = if (currentStepValid) {
                    if (isLastStep) GoldAccent else JuicyBlue
                } else {
                    if (isDark) ConsuSurfaceDark else ConsuChipBg
                }
                val btnShadow = if (currentStepValid) {
                    if (isLastStep) GoldAccentDark else JuicyBlueShadow
                } else {
                    if (isDark) ConsuNavy else ConsuDividerLight
                }
                val btnTextColor = if (currentStepValid) Color.White
                else if (isDark) Color.White.copy(0.25f) else Color.Black.copy(0.25f)

                JuicyButton(
                    text = when {
                        !currentStepValid -> if (isLastStep) "COMPLETA EL CAMPO" else "INGRESA TU DATO"
                        isLastStep -> "🌟  ¡COMENZAR AHORA!"
                        else -> "SIGUIENTE  →"
                    },
                    color = btnColor,
                    shadowColor = btnShadow,
                    textColor = btnTextColor,
                    soundManager = soundManager,
                    modifier = Modifier.weight(1f)
                ) {
                    val isValidDynamic = when (step) {
                        0 -> data.name.isNotBlank()
                        1 -> data.username.isNotBlank() && data.password.isNotBlank()
                        2 -> data.supervisor.isNotBlank()
                        else -> false
                    }
                    if (isValidDynamic) {
                        if (step == 2) {
                            soundManager.play("magic")
                            hapticManager?.success()
                            viewModel.saveProfile(
                                UserProfile(data.name, data.username, data.password, data.supervisor)
                            )
                        } else if (step < 2) {
                            hapticManager?.medium()
                            soundManager.play("pop")
                            step++
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingStepDots(currentStep: Int, totalSteps: Int) {
    val isDark = isSystemInDarkTheme()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { i ->
            val isActive = i == currentStep
            val isPast = i < currentStep
            val dotWidth by animateFloatAsState(
                targetValue = if (isActive) 28f else 8f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "dotWidth$i"
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (isActive || isPast) 1f else 0.3f,
                label = "dotAlpha$i"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> GoldAccent.copy(alpha = dotAlpha)
                            isPast -> GoldAccent.copy(alpha = 0.6f)
                            else -> if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun OnboardingStepHeader(title: String, subtitle: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else ConsuNavy,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.55f) else ConsuNavy.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun OnboardingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    isDark: Boolean,
    imeAction: ImeAction = ImeAction.Next
) {
    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }
    val isValid = value.isNotBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            isValid -> SuccessGreen
            isFocused -> GoldAccent
            else -> if (isDark) Color.White.copy(0.12f) else Color.Black.copy(0.10f)
        },
        animationSpec = tween(250),
        label = "borderColor"
    )
    val bgColor = if (isDark) ConsuNavyDark else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.4f),
                letterSpacing = 1.2.sp
            )
            if (isValid) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .onFocusChanged {
                    if (it.isFocused != isFocused) {
                        isFocused = it.isFocused
                        if (it.isFocused) hapticManager?.light()
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(16.dp))
            Icon(
                icon,
                contentDescription = null,
                tint = if (isValid) SuccessGreen
                       else if (isFocused) GoldAccent
                       else if (isDark) Color.White.copy(0.35f) else Color.Black.copy(0.3f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = {
                    if (it != value) hapticManager?.tick()
                    onValueChange(it)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color.White else ConsuNavy
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = imeAction
                ),
                singleLine = true,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = if (isDark) Color.White.copy(0.22f) else Color.Black.copy(0.22f),
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                            )
                        }
                        inner()
                    }
                }
            )
            AnimatedVisibility(visible = isValid) {
                Box(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (!isValid) Spacer(Modifier.width(14.dp))
        }
    }
}

@Composable
private fun OnboardingProfileSummary(data: OnboardingData, isDark: Boolean) {
    val cardBg = if (isDark) ConsuNavyDark else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "RESUMEN DE TU PERFIL",
                style = MaterialTheme.typography.labelMedium,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ProfileSummaryRow(Icons.Rounded.Person, "Nombre", data.name, isDark)
            ProfileSummaryRow(Icons.Rounded.Badge, "Usuario", data.username, isDark)
            ProfileSummaryRow(Icons.Rounded.Lock, "Contraseña", "••••••", isDark)
            ProfileSummaryRow(Icons.Rounded.Person, "Supervisor", data.supervisor.ifBlank { "—" }, isDark)
        }
    }
}

@Composable
private fun ProfileSummaryRow(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = GoldAccent.copy(alpha = 0.7f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Color.White.copy(0.45f) else Color.Black.copy(0.45f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White.copy(0.85f) else ConsuNavy,
            maxLines = 1
        )
    }
}
