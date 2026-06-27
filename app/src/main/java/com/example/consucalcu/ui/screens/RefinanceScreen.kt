package com.example.consucalcu.ui.screens

import com.example.consucalcu.data.FinancialConfig
import androidx.compose.material.icons.rounded.TrendingDown
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.consucalcu.ui.ViabilityTrafficLight
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.utils.*
import com.example.consucalcu.viewmodel.*
import java.math.BigDecimal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefinanceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val totalDebt = state.debts.sumOf { it.balance.toBD() }
    val totalPayment = state.debts.sumOf { it.payment.toBD() }
    val listState = rememberLazyListState()
    var showLiquidationModal by remember { mutableStateOf(false) }

    // Auto-scroll al agregar deuda
    val debtCount = state.debts.size
    LaunchedEffect(debtCount) {
        if (debtCount > 1) {
            delay(300)
            listState.animateScrollToItem(2) // Scroll a la card de deudas
        }
    }

    // Animated counters para el header
    var animatedDebt by remember { mutableStateOf(0f) }
    var animatedPayment by remember { mutableStateOf(0f) }
    LaunchedEffect(totalDebt) {
        animate(
            initialValue = animatedDebt,
            targetValue = totalDebt.toFloat(),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) { value, _ -> animatedDebt = value }
    }
    LaunchedEffect(totalPayment) {
        animate(
            initialValue = animatedPayment,
            targetValue = totalPayment.toFloat(),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) { value, _ -> animatedPayment = value }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BgDark else Color(0xFFF8F9FA))
            .imePadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── 2. BANNER PRINCIPAL ──
        item {
            AnimatedResultItem(index = 0) {
                JuicyCard(
                    color = if (isDark) SurfaceDark else ConsuNavy,
                    shadowColor = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFF1B3564))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Loop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "REFINANCIAMIENTO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Consolidación de deudas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = ConsuOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = ConsuOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // ── 3. SECCIÓN DEUDAS ──
        item {
            AnimatedResultItem(index = 2) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF334155) else Color(0xFF1A237E)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "DEUDAS A LIQUIDAR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1A237E),
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Card Añadir Deuda
                                Surface(
                                    modifier = Modifier.weight(1f).height(120.dp)
                                        .bouncyClick(soundManager) { 
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.addDebtRow() 
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                ) {
                                    Box {
                                        Icon(
                                            Icons.Rounded.Description, null,
                                            tint = (if (isDark) Color.White else Color.Black).copy(alpha = 0.05f),
                                            modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 10.dp, y = 10.dp)
                                        )
                                        Column(Modifier.padding(16.dp)) {
                                            Surface(
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                color = if (isDark) Color(0xFF334155) else Color(0xFF1A237E)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Text("AÑADIR\nDEUDA", fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 16.sp)
                                            Text("Registra tus deudas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                    }
                                }

                                // Card Calc Liq
                                Surface(
                                    modifier = Modifier.weight(1f).height(120.dp)
                                        .bouncyClick(soundManager) { 
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showLiquidationModal = true 
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                ) {
                                    Box {
                                        Icon(
                                            Icons.Rounded.AccountBalance, null,
                                            tint = (if (isDark) Color.White else Color.Black).copy(alpha = 0.05f),
                                            modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 10.dp, y = 10.dp)
                                        )
                                        Column(Modifier.padding(16.dp)) {
                                            Surface(
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                color = if (isDark) Color(0xFF334155) else Color(0xFF1A237E)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Rounded.Calculate, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Text("CALC.\nLIQ.", fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 16.sp)
                                            Text("Calcula cuánto puedes liquidar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }

                            // Renderizar Deudas si existen
                            if (state.debts.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                state.debts.forEachIndexed { index, debt ->
                                    DebtItemDesign(index, debt, viewModel, soundManager, haptic)
                                    if (index < state.debts.size - 1) Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 4. CAPACIDAD DE PAGO ──
        item {
            AnimatedResultItem(index = 3) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF334155) else Color(0xFF1A237E)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Assessment, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CAPACIDAD DE PAGO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Define cuánto puedes destinar para pagar tus deudas.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "AJUSTE DE LIQUIDEZ (+/-)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFF1A237E))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.AddCircle, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                                    
                                    BasicTextField(
                                        value = state.capacityCNCA,
                                        onValueChange = { viewModel.updateCapacityCNCA(it) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                        textStyle = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (isDark) Color.White else Color(0xFF1A237E)
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        singleLine = true
                                    )
                                    
                                    Text("MXN", fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF1A237E).copy(alpha = 0.5f))
                                }
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, null, tint = JuicyOrange, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Ajuste este valor para ver diferentes escenarios.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 5. BOTONES DE ACCIÓN ──
        item {
            AnimatedResultItem(index = 4) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .bouncyClick(soundManager) { viewModel.calculateRefinance() },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF334155) else Color(0xFF1A237E)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Calculate, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("CALCULAR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text("Ver mi plan de liquidación", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = JuicyOrange)
                        }
                    }

                    Surface(
                        modifier = Modifier.width(80.dp).fillMaxHeight()
                            .bouncyClick(soundManager) { viewModel.clearRefinanceData() },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, tint = JuicyOrange)
                            Text("LIMPIAR", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                    }
                }
            }
        }

        item {
            AnimatedContent(
                targetState = state.isLoading,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "resultsTransition"
            ) { loading ->
                if (loading) {
                    ResultCardSkeletonList(count = 3)
                } else if (state.offersCNCA.isNotEmpty()) {
                    GroupedResultsSection(
                        offers = state.offersCNCA,
                        showPremium = state.showPremiumOffers,
                        onTogglePremium = { viewModel.togglePremiumOffers() },
                        onSelectOffer = { offer ->
                            soundManager.play("pop")
                            viewModel.onSelectOffer(offer)
                        },
                        onShowAmortization = { offer ->
                            soundManager.play("magic")
                            viewModel.showAmortizationFor(offer)
                        },
                        emptyContent = {}
                    )
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }

    if (showLiquidationModal) {
        LiquidationCalculatorModal(
            onDismiss = { showLiquidationModal = false },
            soundManager = soundManager
        )
    }
}

@Composable
fun DebtItemDesign(
    index: Int,
    debt: Debt,
    viewModel: CalculatorViewModel,
    soundManager: SoundManager,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) {
            soundManager.play("pop")
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.removeDebtRow(debt)
            true
        } else false
    })

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(ErrorRed),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.padding(end = 24.dp))
            }
        }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${index + 1}",
                        fontWeight = FontWeight.Black,
                        color = JuicyOrange,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(debt.product, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactInput(
                        value = debt.balance,
                        label = "ADEUDA",
                        modifier = Modifier.weight(1f),
                        inputType = KeyboardType.Decimal,
                        isCurrency = true
                    ) { viewModel.updateDebt(debt.id, debt.copy(balance = it)) }
                    CompactInput(
                        value = debt.payment,
                        label = "PAGO",
                        modifier = Modifier.weight(1f),
                        inputType = KeyboardType.Decimal,
                        isCurrency = true
                    ) { viewModel.updateDebt(debt.id, debt.copy(payment = it)) }
                }
            }
        }
    }
}
