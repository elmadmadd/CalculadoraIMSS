package com.example.consucalcu.ui.screens
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import com.example.consucalcu.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.LocalSoundManager
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

private val OnboardingDataSaver = listSaver<OnboardingData, String>(
    // Never place the credential in SavedState, which the OS may persist.
    save = { listOf(it.name, it.username, it.supervisor) },
    restore = { values ->
        OnboardingData(
            name = values.getOrElse(0) { "" },
            username = values.getOrElse(1) { "" },
            password = "",
            supervisor = values.getOrElse(2) { "" }
        )
    }
)

@Composable
fun MandatoryConfigScreen(viewModel: CalculatorViewModel) {
    val soundManager = LocalSoundManager.current
    val hapticManager = LocalHapticManager.current

    // ── Estado de pasos (0, 1, 2) ──
    var step by rememberSaveable { mutableIntStateOf(0) }
    var data by rememberSaveable(stateSaver = OnboardingDataSaver) {
        mutableStateOf(OnboardingData())
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    OnboardingGreetingMascot(
                        playGreeting = step == 0,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
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
                                    subtitle = "Empecemos conociéndonos.\n¿Cuál es tu nombre completo?"
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE COMPLETO",
                                    value = data.name,
                                    onValueChange = { data = data.copy(name = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. María García López",
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 2: Credenciales ──
                            1 -> {
                                OnboardingStepHeader(
                                    title = "Tus Credenciales",
                                    subtitle = "Se usan para generar el\nlayout de captura automático."
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "USUARIO (BP)",
                                    value = data.username,
                                    onValueChange = { data = data.copy(username = it) },
                                    icon = Icons.Rounded.Badge,
                                    placeholder = "Ej. MGARCIA01"
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingInputField(
                                    label = "CONTRASEÑA DEL SISTEMA",
                                    value = data.password,
                                    onValueChange = { data = data.copy(password = it) },
                                    icon = Icons.Rounded.Lock,
                                    placeholder = "Tu contraseña de acceso",
                                    isPassword = true,
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 3: Supervisor + Confirmar ──
                            2 -> {
                                OnboardingStepHeader(
                                    title = "Último Detalle",
                                    subtitle = "¿Quién es tu supervisor?\nAparecerá en tus reportes."
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE DEL SUPERVISOR",
                                    value = data.supervisor,
                                    onValueChange = { data = data.copy(supervisor = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. Carlos Ramírez",
                                    imeAction = ImeAction.Done
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingProfileSummary(data = data)
                            }
                        }
                    }
                }
            }

            // ── Botones de navegación ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón ATRÁS
                if (step > 0) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .bouncyClick {
                                hapticManager?.light()
                                soundManager?.play("pop")
                                if (step > 0) {
                                    step--
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { scaleX = -1f }
                        )
                    }
                }

                // Botón SIGUIENTE / COMENZAR
                val isLastStep = step == 2
                val btnColor = if (currentStepValid) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                val btnTextColor = if (currentStepValid) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
                }

                JuicyButton(
                    text = when {
                        !currentStepValid -> if (isLastStep) "COMPLETA EL CAMPO" else "INGRESA TU DATO"
                        isLastStep -> "🌟  ¡COMENZAR AHORA!"
                        else -> "SIGUIENTE  →"
                    },
                    color = btnColor,
                    shadowColor = Color.Transparent,
                    textColor = btnTextColor,
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
                            soundManager?.play("ui_success")
                            viewModel.saveProfile(
                                UserProfile(data.name, data.username, data.password, data.supervisor)
                            )
                        } else if (step < 2) {
                            hapticManager?.medium()
                            soundManager?.play("pop")
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
    val activeColor = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)) {
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
                            isActive -> activeColor.copy(alpha = dotAlpha)
                            isPast -> activeColor.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun OnboardingStepHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val isValid = value.isNotBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            isValid -> ConsuSuccess
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(250),
        label = "borderColor"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.2.sp
            )
            if (isValid) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = ConsuSuccess,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .clip(RoundedCornerShape(ConsuRadius.md))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, RoundedCornerShape(ConsuRadius.md))
                .semantics { contentDescription = label }
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
                tint = if (isValid) ConsuSuccess
                       else if (isFocused) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
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
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                    imeAction = imeAction
                ),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                singleLine = true,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                            )
                        }
                        inner()
                    }
                }
            )
            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
            } else AnimatedVisibility(visible = isValid) {
                Box(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ConsuSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = ConsuSuccess,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (!isPassword && !isValid) Spacer(Modifier.width(14.dp))
        }
    }
}

@Composable
private fun OnboardingProfileSummary(data: OnboardingData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ConsuRadius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(ConsuRadius.md))
            .padding(ConsuSpacing.lg)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "RESUMEN DE TU PERFIL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ProfileSummaryRow(Icons.Rounded.Person, "Nombre", data.name)
            ProfileSummaryRow(Icons.Rounded.Badge, "Usuario", data.username)
            ProfileSummaryRow(Icons.Rounded.Lock, "Contraseña", "••••••")
            ProfileSummaryRow(Icons.Rounded.Person, "Supervisor", data.supervisor.ifBlank { "—" })
        }
    }
}

@Composable
private fun ProfileSummaryRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
