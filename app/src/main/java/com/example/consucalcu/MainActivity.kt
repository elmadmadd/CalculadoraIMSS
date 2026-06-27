package com.example.consucalcu

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.consucalcu.academy.AcademyScreen
import com.example.consucalcu.features.DigitalTicket
import com.example.consucalcu.features.TicketGenerator
import com.example.consucalcu.manager.HapticManager
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.model.*
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.ui.screens.*
import com.example.consucalcu.data.PreferenceRepository
import com.example.consucalcu.viewmodel.CalculatorViewModel
import com.example.consucalcu.viewmodel.CalculatorViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== HAPTIC FEEDBACK SYSTEM ==========
val LocalHapticManager = compositionLocalOf<HapticManager?> { null }

class SettingsAwareHapticFeedback(
    private val delegate: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val isHapticEnabled: () -> Boolean
) : androidx.compose.ui.hapticfeedback.HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: androidx.compose.ui.hapticfeedback.HapticFeedbackType) {
        if (isHapticEnabled()) {
            delegate.performHapticFeedback(hapticFeedbackType)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val hapticManager = remember { HapticManager(context) }
            val repository = remember { PreferenceRepository(context) }
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModelFactory(repository)
            )
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isHapticEnabled) {
                hapticManager.isHapticEnabled = state.isHapticEnabled
            }

            val standardHaptic = LocalHapticFeedback.current
            val customHapticFeedback = remember(standardHaptic, state.isHapticEnabled) {
                SettingsAwareHapticFeedback(standardHaptic) { state.isHapticEnabled }
            }
            
            LaunchedEffect(Unit) {
               LottieCache.preload(context, listOf(
                   R.raw.lottie_fire, 
                   R.raw.lottie_levelup
               ))
            }

            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            CompositionLocalProvider(
                LocalHapticManager provides hapticManager,
                LocalHapticFeedback provides customHapticFeedback
            ) {
                val dynamicColors = if (isDarkTheme) {
                    darkColorScheme(
                        primary = Color.White,
                        onPrimary = Color.Black,
                        primaryContainer = Color(0xFF2C2C2E),
                        onPrimaryContainer = Color.White,
                        secondary = GoldAccent,
                        onSecondary = Color.Black,
                        secondaryContainer = Color(0xFF3A3A3C),
                        onSecondaryContainer = Color.White,
                        tertiary = GoldAccentLight,
                        onTertiary = Color.Black,
                        background = BgDark,
                        onBackground = TextLight,
                        surface = SurfaceDark,
                        onSurface = TextLight,
                        surfaceVariant = Color(0xFF2C2C2E),
                        onSurfaceVariant = TextLight.copy(alpha = 0.7f),
                        outline = BorderDark,
                        error = ErrorRed,
                        onError = Color.White
                    )
                } else {
                    lightColorScheme(
                        primary = Color(0xFF1C1C1E),
                        onPrimary = Color.White,
                        primaryContainer = Color(0xFFE5E5EA),
                        onPrimaryContainer = TextDark,
                        secondary = GoldAccentDark,
                        onSecondary = Color.White,
                        secondaryContainer = Color(0xFFF2F2F7),
                        onSecondaryContainer = TextDark,
                        tertiary = GoldAccent,
                        onTertiary = Color.White,
                        background = BgLight,
                        onBackground = TextDark,
                        surface = SurfaceLight,
                        onSurface = TextDark,
                        surfaceVariant = Color(0xFFF2F2F7),
                        onSurfaceVariant = TextDark.copy(alpha = 0.7f),
                        outline = BorderLight,
                        error = ErrorRed,
                        onError = Color.White
                    )
                }

                MaterialTheme(
                    colorScheme = dynamicColors,
                    typography = JuicyTypography
                ) {
                    ConsuCalcuApp(
                        viewModel = viewModel,
                        isDark = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}

val JuicyTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

enum class NavigationTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    REFINANCE("refinance", "Refinanciar", Icons.Rounded.Loop),
    NEW_CREDIT("new_credit", "Cotizador", Icons.Rounded.AttachMoney),
    INSURANCE("insurance", "Simulador", Icons.Rounded.Savings),
    ACADEMY("academy", "Educación", Icons.Rounded.School);

    companion object {
        fun fromRoute(route: String?): NavigationTab {
            return entries.firstOrNull { it.route == route } ?: REFINANCE
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsuCalcuApp(
    viewModel: CalculatorViewModel,
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(ctx) }
    LaunchedEffect(state.isSoundEnabled) {
        soundManager.isSoundEnabled = state.isSoundEnabled
    }

    var showMenu by remember { mutableStateOf(false) }
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "refinance"

    val currentTabIndex = NavigationTab.fromRoute(currentRoute).ordinal

    val pillLeading = remember { Animatable(0f) }
    val pillTrailing = remember { Animatable(1f) }
    val prevTabIndex = remember { mutableIntStateOf(0) }

    LaunchedEffect(currentTabIndex) {
        val prev = prevTabIndex.intValue
        val curr = currentTabIndex
        prevTabIndex.intValue = curr

        val fastSpec = spring<Float>(
            dampingRatio = 0.82f,
            stiffness = 300f
        )
        val slowSpec = spring<Float>(
            dampingRatio = 0.85f,
            stiffness = 120f
        )

        if (curr > prev) {
            launch { pillTrailing.animateTo(curr + 1f, fastSpec) }
            launch { pillLeading.animateTo(curr.toFloat(), slowSpec) }
        } else if (curr < prev) {
            launch { pillLeading.animateTo(curr.toFloat(), fastSpec) }
            launch { pillTrailing.animateTo(curr + 1f, slowSpec) }
        } else {
            launch { pillLeading.snapTo(curr.toFloat()) }
            launch { pillTrailing.snapTo(curr + 1f) }
        }
    }

    LaunchedEffect(currentTabIndex) {
        if (state.selectedTab != currentTabIndex) {
            viewModel.setTab(currentTabIndex)
        }
    }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }
    LaunchedEffect(Unit) { viewModel.loadData(); delay(50) }
    LaunchedEffect(state) { viewModel.saveData() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CalculatorEffect.PlaySound -> {
                    soundManager.play(effect.soundName)
                }
                is CalculatorEffect.TriggerHaptics -> {
                    if (state.isHapticEnabled) {
                        triggerHapticTexture(ctx, effect.pattern)
                    }
                }
                is CalculatorEffect.GenerateTicket -> {
                    viewModel.setGeneratingTicket(true)
                    try {
                        TicketGenerator.generateAndShare(ctx, effect.offer, effect.clientData, effect.userName)
                    } finally {
                        viewModel.setGeneratingTicket(false)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) Color.White else ConsuNavy, fontWeight = FontWeight.Bold)) {
                                    append("Consu")
                                }
                                withStyle(style = SpanStyle(color = ConsuOrange, fontWeight = FontWeight.Bold)) {
                                    append("Calc")
                                }
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    actions = {
                        val firstName = state.userProfile.name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: "U"
                        val initial = firstName.take(1).uppercase()
                        
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) GoldAccent.copy(alpha = 0.15f)
                                        else Color(0xFF1C1C1E).copy(alpha = 0.08f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isDark) GoldAccent.copy(alpha = 0.4f)
                                        else Color(0xFF1C1C1E).copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    initial,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) GoldAccent else Color(0xFF1C1C1E)
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                )

                if (showMenu) {
                    ModalBottomSheet(
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        dragHandle = {
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, bottom = 4.dp)
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                            )
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val userFirstName = state.userProfile.name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: "U"
                                val userInitial = userFirstName.take(1).uppercase()
                                
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDark) GoldAccent.copy(alpha = 0.12f)
                                            else Color(0xFF1C1C1E).copy(alpha = 0.06f)
                                        )
                                        .border(
                                            2.dp,
                                            if (isDark) GoldAccent.copy(alpha = 0.3f)
                                            else Color(0xFF1C1C1E).copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        userInitial,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) GoldAccent else Color(0xFF1C1C1E)
                                    )
                                }
                                
                                Spacer(Modifier.width(14.dp))
                                
                                Column {
                                    Text(
                                        userFirstName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Asesor Financiero",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            Surface(
                                onClick = {
                                    showMenu = false
                                    soundManager.play("pop")
                                    viewModel.toggleHistory()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Historial",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Cotizaciones guardadas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleConfigDialog(true)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Configuración",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Datos de usuario y convenio",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            Surface(
                                onClick = { onToggleTheme() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isDark) Color(0xFF2C2C2E)
                                                else Color(0xFFE5E5EA)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isDark) GoldAccent else Color(0xFFFF9500)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        if (isDark) "Modo Oscuro" else "Modo Claro",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Switch(
                                        checked = isDark,
                                        onCheckedChange = { onToggleTheme() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = GoldAccent,
                                            checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                            uncheckedThumbColor = Color(0xFF8E8E93),
                                            uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                            uncheckedBorderColor = Color.Transparent,
                                            checkedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            
                            Surface(
                                onClick = { viewModel.toggleSound(!state.isSoundEnabled) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isDark) Color(0xFF2C2C2E)
                                                else Color(0xFFE5E5EA)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (state.isSoundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (state.isSoundEnabled) (if (isDark) GoldAccent else Color(0xFF34C759)) else Color(0xFF8E8E93)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        "Efectos de Sonido",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Switch(
                                        checked = state.isSoundEnabled,
                                        onCheckedChange = { viewModel.toggleSound(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = GoldAccent,
                                            checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                            uncheckedThumbColor = Color(0xFF8E8E93),
                                            uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                            uncheckedBorderColor = Color.Transparent,
                                            checkedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            
                            Surface(
                                onClick = { viewModel.toggleHaptic(!state.isHapticEnabled) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isDark) Color(0xFF2C2C2E)
                                                else Color(0xFFE5E5EA)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Vibration,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (state.isHapticEnabled) (if (isDark) GoldAccent else Color(0xFF5856D6)) else Color(0xFF8E8E93)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        "Vibración Háptica",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Switch(
                                        checked = state.isHapticEnabled,
                                        onCheckedChange = { viewModel.toggleHaptic(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = GoldAccent,
                                            checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                            uncheckedThumbColor = Color(0xFF8E8E93),
                                            uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                            uncheckedBorderColor = Color.Transparent,
                                            checkedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val onTabClick: (String) -> Unit = { route ->
                        if (navController.currentDestination?.route != route) {
                            soundManager.play("pop")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    AnimatedNavigationBar(
                        selectedIndex = currentTabIndex,
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth(),
                        containerColor = if (isDark) SurfaceDark else Color.White,
                        ballColor = ConsuOrange,
                        cornerRadius = 28.dp
                    ) {
                        NavigationTab.entries.forEach { tab ->
                            AnimatedNavBarItem(
                                icon = tab.icon,
                                label = tab.label,
                                selected = currentTabIndex == tab.ordinal,
                                onClick = { onTabClick(tab.route) },
                                unselectedColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = 0.dp
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                ) {
                    fun routeToIndex(route: String?) = NavigationTab.fromRoute(route).ordinal

                    val navTweenSpec = tween<IntOffset>(
                        durationMillis = 460,
                        easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
                    )
                    val scaleTweenSpec = tween<Float>(
                        durationMillis = 460,
                        easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "refinance",
                        enterTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            if (to > from) {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = navTweenSpec
                                ) + fadeIn(tween(300))
                            } else {
                                fadeIn(tween(400)) + scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = scaleTweenSpec
                                )
                            }
                        },
                        exitTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            if (to > from) {
                                fadeOut(tween(300)) + scaleOut(
                                    targetScale = 0.94f,
                                    animationSpec = scaleTweenSpec
                                )
                            } else {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = navTweenSpec
                                ) + fadeOut(tween(300))
                            }
                        },
                        popEnterTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            if (to > from) {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = navTweenSpec
                                ) + fadeIn(tween(300))
                            } else {
                                fadeIn(tween(400)) + scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = scaleTweenSpec
                                )
                            }
                        },
                        popExitTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            if (to > from) {
                                fadeOut(tween(300)) + scaleOut(
                                    targetScale = 0.94f,
                                    animationSpec = scaleTweenSpec
                                )
                            } else {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = navTweenSpec
                                ) + fadeOut(tween(300))
                            }
                        }
                    ) {
                        composable("refinance") {
                            RefinanceScreen(viewModel, state, soundManager, ctx)
                        }
                        composable("new_credit") {
                            NewCreditScreen(viewModel, state, soundManager, ctx)
                        }
                        composable("insurance") {
                            InsuranceScreen(viewModel, state, soundManager, ctx)
                        }
                        composable("academy") {
                            AcademyScreen(soundManager = soundManager)
                        }
                    }
                }
            }
        }

        state.selectedOfferForLayout?.let { selectedOffer ->
            if (state.showOfferPreview) {
                OfferPreviewModal(
                    offer = selectedOffer,
                    viewModel = viewModel,
                    soundManager = soundManager,
                    context = ctx,
                    onDismiss = { viewModel.dismissPreview() },
                    onContinue = { viewModel.continueToClientForm() }
                )
            }
        }

        if (state.showClientForm) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissForm() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
            ) {
                ClientFormContent(
                    viewModel,
                    onGenerate = { layout ->
                        soundManager.play("magic")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboard.setText(AnnotatedString(layout))
                        viewModel.dismissForm()
                        viewModel.triggerSuccess()
                    },
                    onGenerateTicket = { viewModel.generateTicketBackground() }
                )
            }
        }
        
        if (state.showTicketPreview && state.activeTicketData != null) {
            Dialog(onDismissRequest = { viewModel.dismissTicketPreview() }) {
                 Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                 ) {
                     DigitalTicket(
                         data = state.activeTicketData!!,
                         modifier = Modifier.padding(16.dp)
                     )
                     Spacer(Modifier.height(16.dp))
                     JuicyButton(
                         text = "CERRAR",
                         color = Color.White,
                         shadowColor = Color.Gray,
                         textColor = Color.Black,
                         height = 48.dp, 
                         soundManager = soundManager,
                         onClick = { viewModel.dismissTicketPreview() }
                     )
                 }
            }
        }

        if (state.showHistory) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleHistory() },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                HistorySheet(
                    history = state.history,
                    onRestore = { draft -> 
                         viewModel.restoreHistory(draft)
                         soundManager.play("magic")
                    },
                    onClear = { 
                        viewModel.clearHistory()
                        soundManager.play("delete")
                    },
                    onDelete = { draft ->
                        viewModel.deleteDraft(draft)
                        soundManager.play("delete")
                    }
                )
            }
        }

        if (state.mandatoryConfigNeeded) {
            MandatoryConfigScreen(viewModel, ctx, soundManager)
        } else {
            if (state.showConfigDialog) {
                ConfigDialog(viewModel, ctx, soundManager)
            }

            if (state.showOffersComparator) {
                val offers = if (state.selectedTab == 0) state.offersCNCA else state.offersNew
                if (offers.isNotEmpty()) {
                    OffersComparator(
                        offers = offers,
                        onDismiss = {
                            soundManager.play("pop")
                            viewModel.toggleOffersComparator(false)
                        },
                        onSelectOffer = { offer ->
                            soundManager.play("magic")
                            viewModel.onSelectOffer(offer)
                            viewModel.toggleOffersComparator(false)
                        }
                    )
                }
            }
            
            if (state.showAmortizationSimulator && state.selectedOfferForAmortization != null) {
                AmortizationSimulator(
                    offer = state.selectedOfferForAmortization!!,
                    amortizationTable = state.amortizationTable,
                    onDismiss = {
                        soundManager.play("pop")
                        viewModel.dismissAmortizationSimulator()
                    }
                )
            }
        }

        if (state.showSuccessAnim) {
            SuccessOverlay { viewModel.hideSuccess() }
        }
    }
}
