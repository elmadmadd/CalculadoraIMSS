package com.example.consucalcu
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.consucalcu.academy.AcademyScreen
import com.example.consucalcu.features.DigitalTicket
import com.example.consucalcu.features.TicketGenerator
import com.example.consucalcu.manager.HapticManager
import com.example.consucalcu.manager.LottieCache
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.model.*
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.ui.screens.*
import com.example.consucalcu.data.PreferenceRepository
import com.example.consucalcu.viewmodel.CalculatorViewModel
import com.example.consucalcu.viewmodel.CalculatorViewModelFactory
import com.example.consucalcu.viewmodel.CalculatorUiState

// ========== RESOURCE MANAGERS SYSTEM ==========
val LocalHapticManager = compositionLocalOf<HapticManager?> { null }
val LocalSoundManager = compositionLocalOf<SoundManager?> { null }

class SettingsAwareHapticFeedback(
    private val delegate: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val isHapticEnabled: () -> Boolean,
) : androidx.compose.ui.hapticfeedback.HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        if (isHapticEnabled()) {
            delegate.performHapticFeedback(hapticFeedbackType)
        }
    }
}

class MainActivity : ComponentActivity() {
    @android.annotation.SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val hapticManager = remember { HapticManager(context) }
            val soundManager = remember { SoundManager(context, hapticManager) }
            val repository = remember { PreferenceRepository(context) }
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModelFactory(repository),
            )
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isHapticEnabled, state.isSoundEnabled) {
                hapticManager.isHapticEnabled = state.isHapticEnabled
                soundManager.isSoundEnabled = state.isSoundEnabled
            }

            DisposableEffect(Unit) {
                onDispose {
                    soundManager.release()
                }
            }

            val standardHaptic = LocalHapticFeedback.current
            val customHapticFeedback = remember(standardHaptic, state.isHapticEnabled) {
                SettingsAwareHapticFeedback(standardHaptic) { state.isHapticEnabled }
            }

            LaunchedEffect(Unit) {
                LottieCache.preload(
                    context,
                    listOf(
                        R.raw.lottie_fire,
                        R.raw.lottie_levelup,
                    ),
                )
            }

            // Centralized Theme Management
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }

            CompositionLocalProvider(
                LocalHapticManager provides hapticManager,
                LocalSoundManager provides soundManager,
                LocalHapticFeedback provides customHapticFeedback
            ) {
                com.example.consucalcu.ui.theme.ConsucalcuTheme(darkTheme = isDarkTheme) {
                    var showSplash by rememberSaveable { mutableStateOf(value = true) }

                    // La app se compone debajo del splash: así carga sus datos durante
                    // la animación y el desvanecido final la revela, sin destello blanco.
                    Box(modifier = Modifier.fillMaxSize()) {
                        ConsuCalcuApp(
                            viewModel = viewModel,
                            isDark = isDarkTheme,
                        ) { isDarkTheme = !isDarkTheme }
                        if (showSplash) {
                            SplashScreen { showSplash = false }
                        }
                    }
                }
            }
        }
    }
}

enum class NavigationTab(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    REFINANCE("refinance", R.string.nav_refinance, Icons.Rounded.Loop),
    NEW_CREDIT("new_credit", R.string.nav_quotation, Icons.Rounded.AttachMoney),
    INSURANCE("insurance", R.string.nav_simulator, Icons.Rounded.Savings),
    ACADEMY("academy", R.string.nav_academy, Icons.Rounded.School);

    companion object {
        fun fromRoute(route: String?): NavigationTab {
            return entries.firstOrNull { it.route == route } ?: REFINANCE
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val soundManager = LocalSoundManager.current


    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "refinance"

    val currentTabIndex = NavigationTab.fromRoute(currentRoute).ordinal
    var academyImmersiveMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != NavigationTab.ACADEMY.route) {
            academyImmersiveMode = false
        }
    }

    LaunchedEffect(currentTabIndex) {
        if (state.selectedTab != currentTabIndex) {
            viewModel.setTab(currentTabIndex)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadData() }
    LaunchedEffect(state) { viewModel.saveData() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CalculatorEffect.PlaySound -> {
                    soundManager?.play(effect.soundName)
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

    val navigateToRoute: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
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

    // Con el teclado abierto la barra inferior solo estorba: se oculta.
    val imeVisible = WindowInsets.isImeVisible

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!academyImmersiveMode) Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp),
                )
            },
            bottomBar = {
                if (!academyImmersiveMode && !imeVisible) Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 720.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        NavigationTab.entries.forEach { tab ->
                            val selected = currentTabIndex == tab.ordinal
                            NavigationBarItem(
                            selected = selected,
                            onClick = { navigateToRoute(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.labelRes),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = ConsuOrange,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            )
                        }
                    }

                    SporadicNavBarMascot(
                        enabled = !academyImmersiveMode,
                        onAppearance = { soundManager?.play("mascot_appear") },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(padding)
                        // Así el imePadding() de cada pantalla descuenta el
                        // espacio ya reservado y no deja un hueco sobre el teclado.
                        .consumeWindowInsets(padding)
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                ) {
                    fun routeToIndex(route: String?) = NavigationTab.fromRoute(route).ordinal

                    val navSlideSpec = spring<IntOffset>(
                        dampingRatio = 0.9f,
                        stiffness = 420f
                    )
                    val scaleTweenSpec = tween<Float>(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "refinance",
                        enterTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            val direction = if (to > from) 1 else -1
                            slideInHorizontally(
                                initialOffsetX = { width -> direction * (width / 9) },
                                animationSpec = navSlideSpec
                            ) + fadeIn(tween(220, delayMillis = 35)) + scaleIn(
                                initialScale = 0.992f,
                                animationSpec = scaleTweenSpec
                            )
                        },
                        exitTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            val direction = if (to > from) -1 else 1
                            slideOutHorizontally(
                                targetOffsetX = { width -> direction * (width / 14) },
                                animationSpec = navSlideSpec
                            ) + fadeOut(tween(170)) + scaleOut(
                                targetScale = 0.995f,
                                animationSpec = scaleTweenSpec
                            )
                        },
                        popEnterTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            val direction = if (to > from) 1 else -1
                            slideInHorizontally(
                                initialOffsetX = { width -> direction * (width / 9) },
                                animationSpec = navSlideSpec
                            ) + fadeIn(tween(220, delayMillis = 35)) + scaleIn(
                                initialScale = 0.992f,
                                animationSpec = scaleTweenSpec
                            )
                        },
                        popExitTransition = {
                            val from = routeToIndex(initialState.destination.route)
                            val to = routeToIndex(targetState.destination.route)
                            val direction = if (to > from) -1 else 1
                            slideOutHorizontally(
                                targetOffsetX = { width -> direction * (width / 14) },
                                animationSpec = navSlideSpec
                            ) + fadeOut(tween(170)) + scaleOut(
                                targetScale = 0.995f,
                                animationSpec = scaleTweenSpec
                            )
                        }
                    ) {
                        composable("refinance") {
                            RefinanceScreen(viewModel, state, isDark)
                        }
                        composable("new_credit") {
                            NewCreditScreen(viewModel, state, isDark)
                        }
                        composable("insurance") {
                            InsuranceScreen(viewModel, state)
                        }
                        composable("academy") {
                            AcademyScreen(
                                onImmersiveModeChange = { academyImmersiveMode = it }
                            )
                        }
                    }
                }
            }
        }

        if (!academyImmersiveMode) {
            DynamicIslandHeader(
                state = state,
                viewModel = viewModel,
                isDark = isDark,
                onToggleTheme = onToggleTheme,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 4.dp)
                    .zIndex(10f),
            )
        }

        state.selectedOfferForLayout?.let { selectedOffer ->
            if (state.showOfferPreview) {
                OfferPreviewModal(
                    offer = selectedOffer,
                    viewModel = viewModel,
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
                        soundManager?.play("ui_success")
                        clipboard.setText(AnnotatedString(layout))
                        viewModel.dismissForm()
                        viewModel.triggerSuccess()
                    },
                    onGenerateTicket = { viewModel.generateTicketBackground() }
                )
            }
        }

        if (state.showTicketPreview && (state.activeTicketData != null)) {
            state.activeTicketData?.let { ticketData ->
                Dialog(onDismissRequest = { viewModel.dismissTicketPreview() }) {
                     Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                     ) {
                         DigitalTicket(
                             data = ticketData,
                             modifier = Modifier.padding(ConsuSpacing.lg)
                         )
                         Spacer(Modifier.height(16.dp))
                         JuicyButton(
                             text = stringResource(R.string.action_close),
                             color = Color.White,
                             shadowColor = Color.Gray,
                             textColor = Color.Black,
                             height = 48.dp,
                             onClick = { viewModel.dismissTicketPreview() }
                         )
                     }
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
                         soundManager?.play("ui_success")
                         navigateToRoute(
                             if (draft.type == "Refinanciamiento") {
                                 NavigationTab.REFINANCE.route
                             } else {
                                 NavigationTab.NEW_CREDIT.route
                             }
                         )
                    },
                    onClear = {
                        viewModel.clearHistory()
                        soundManager?.play("delete")
                    },
                    onDelete = { draft ->
                        viewModel.deleteDraft(draft)
                        soundManager?.play("delete")
                    }
                )
            }
        }

            if (state.mandatoryConfigNeeded) {
            MandatoryConfigScreen(viewModel)
        } else {
            if (state.showConfigDialog) {
                ConfigDialog(viewModel)
            }

            if (state.showOffersComparator) {
                val offers = if (state.selectedTab == 0) state.offersCNCA else state.offersNew
                if (offers.isNotEmpty()) {
                    OffersComparator(
                        offers = offers,
                        onDismiss = {
                            soundManager?.play("pop")
                            viewModel.toggleOffersComparator(false)
                        },
                        onSelectOffer = { offer ->
                            soundManager?.play("ui_success")
                            viewModel.onSelectOffer(offer)
                            viewModel.toggleOffersComparator(false)
                        }
                    )
                }
            }

            if (state.showAmortizationSimulator && (state.selectedOfferForAmortization != null)) {
                AmortizationSimulator(
                    offer = state.selectedOfferForAmortization!!,
                    amortizationTable = state.amortizationTable,
                    onDismiss = {
                        soundManager?.play("pop")
                        viewModel.dismissAmortizationSimulator()
                    }
                )
            }
        }

        if (state.showSuccessAnim) {
            SuccessOverlay { viewModel.hideSuccess() }
        }

        if (state.showSupportModal && !state.mandatoryConfigNeeded) {
            VoluntarySupportModal(
                onDismiss = { viewModel.dismissSupportModal() }
            )
        }
    }
}



@Composable
fun DynamicIslandHeader(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var islandState by remember { mutableStateOf(IslandState.COLLAPSED) }
    val configuration = LocalConfiguration.current
    val settingsWidth = (configuration.screenWidthDp.dp - 32.dp)
        .coerceAtLeast(240.dp)
        .coerceAtMost(400.dp)
    val settingsHeight = (configuration.screenHeightDp.dp - 120.dp)
        .coerceAtLeast(280.dp)
        .coerceAtMost(440.dp)

    BackHandler(enabled = islandState != IslandState.COLLAPSED) {
        islandState = IslandState.COLLAPSED
    }

    val width by animateDpAsState(
        targetValue = when (islandState) {
            IslandState.COLLAPSED -> 132.dp
            IslandState.PREVIEW -> 280.dp
            IslandState.SETTINGS -> settingsWidth
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "islandWidth"
    )

    val height by animateDpAsState(
        targetValue = when (islandState) {
            IslandState.COLLAPSED -> 32.dp
            IslandState.PREVIEW -> 56.dp
            IslandState.SETTINGS -> settingsHeight
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "islandHeight"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when (islandState) {
            IslandState.COLLAPSED -> 16.dp
            IslandState.PREVIEW -> 28.dp
            IslandState.SETTINGS -> 24.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "islandCorner"
    )

    val soundManager = LocalSoundManager.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        if (islandState == IslandState.SETTINGS) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.24f))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Cerrar ajustes",
                    ) {
                        soundManager?.play("island_collapse")
                        islandState = IslandState.COLLAPSED
                    },
            )
        }

        Surface(
            color = ConsuNavy,
            shape = RoundedCornerShape(cornerRadius),
            modifier = Modifier
                .width(width)
                .height(height)
                .clickable(
                    enabled = islandState != IslandState.SETTINGS,
                    role = Role.Button,
                    onClickLabel = if (islandState == IslandState.COLLAPSED) "Expandir ConsuCalcu" else "Contraer ConsuCalcu",
                ) {
                    val nextState = if (islandState == IslandState.COLLAPSED) {
                        IslandState.PREVIEW
                    } else IslandState.COLLAPSED
                    soundManager?.play(
                        if (nextState == IslandState.PREVIEW) "island_expand" else "island_collapse"
                    )
                    islandState = nextState
                },
            shadowElevation = 2.dp,
        ) {
        AnimatedContent(
            targetState = islandState,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                fadeOut(animationSpec = tween(90))
            },
            label = "islandContent"
        ) { targetState ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (targetState) {
                    IslandState.COLLAPSED -> {
                        Text(
                            text = "ConsuCalcu",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IslandState.PREVIEW -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ConsuCalcu",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Propuestas de Crédito",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            val firstName = state.userProfile.name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: "U"
                            val initial = firstName.take(1).uppercase()

                            Box(
                                modifier = Modifier
                                    // 48dp touch target, 36dp visual circle (padding insets the drawn shape)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Abrir ajustes del asesor",
                                    ) {
                                        soundManager?.play("island_full_expand")
                                        islandState = IslandState.SETTINGS
                                    }
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    IslandState.SETTINGS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header de ajustes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Ajustes de Asesor",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = state.userProfile.name.ifEmpty { "Asesor Firmado" },
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        // 48dp touch target, 32dp visual circle
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            role = Role.Button,
                                            onClickLabel = "Cerrar ajustes",
                                        ) {
                                            soundManager?.play("island_collapse")
                                            islandState = IslandState.COLLAPSED
                                        }
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Ajustes de opciones
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            soundManager?.play("island_collapse")
                                            islandState = IslandState.COLLAPSED
                                            viewModel.toggleHistory()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "Historial de Cotizaciones",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            soundManager?.play("island_collapse")
                                            islandState = IslandState.COLLAPSED
                                            viewModel.toggleConfigDialog(true)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "Configuración de Convenio",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(ConsuRadius.sm))
                                        .toggleable(
                                            value = isDark,
                                            role = Role.Switch,
                                            onValueChange = { onToggleTheme() },
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "Modo Oscuro",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Switch(
                                        checked = isDark,
                                        onCheckedChange = null,
                                        colors = consuSettingsSwitchColors(),
                                        modifier = Modifier.scale(0.9f)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(ConsuRadius.sm))
                                        .toggleable(
                                            value = state.isSoundEnabled,
                                            role = Role.Switch,
                                            onValueChange = viewModel::toggleSound,
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (state.isSoundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "Efectos de Sonido",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Switch(
                                        checked = state.isSoundEnabled,
                                        onCheckedChange = null,
                                        colors = consuSettingsSwitchColors(),
                                        modifier = Modifier.scale(0.9f)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(ConsuRadius.sm))
                                        .toggleable(
                                            value = state.isHapticEnabled,
                                            role = Role.Switch,
                                            onValueChange = viewModel::toggleHaptic,
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Vibration,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "Vibración Háptica",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Switch(
                                        checked = state.isHapticEnabled,
                                        onCheckedChange = null,
                                        colors = consuSettingsSwitchColors(),
                                        modifier = Modifier.scale(0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun consuSettingsSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = ConsuOrange,
    checkedBorderColor = ConsuOrange,
    uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
    uncheckedTrackColor = Color.White.copy(alpha = 0.14f),
    uncheckedBorderColor = Color.White.copy(alpha = 0.24f),
    disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
    disabledCheckedTrackColor = ConsuOrange.copy(alpha = 0.35f)
)

enum class IslandState {
    COLLAPSED,
    PREVIEW,
    SETTINGS
}
