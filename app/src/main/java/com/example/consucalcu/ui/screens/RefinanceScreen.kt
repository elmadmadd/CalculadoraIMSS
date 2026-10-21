package com.example.consucalcu.ui.screens
import com.example.consucalcu.data.FinancialConfig

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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.ui.platform.LocalContext
import com.example.consucalcu.R
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.*
import com.example.consucalcu.ui.theme.*
import androidx.compose.material.icons.filled.ArrowDropDown
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
    isDark: Boolean
) {
    val soundManager = LocalSoundManager.current
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
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.xl)
    ) {
        // ── 2. BANNER PRINCIPAL ──
        item {
            AnimatedResultItem(index = 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.refinance_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.refinance_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
                    )
                }
            }
        }

        // ── 3. SECCIÓN DEUDAS ──
        item {
            AnimatedResultItem(index = 2) {
                Column {
                    Text(
                        stringResource(R.string.debts_to_liquidate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else ConsuNavy
                    )

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ConsuRadius.xxl),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(ConsuSpacing.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
                            ) {
                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.addDebtRow()
                                    },
                                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                    shape = RoundedCornerShape(ConsuRadius.md),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                                        Text(
                                            stringResource(R.string.add_debt),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showLiquidationModal = true
                                    },
                                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                    shape = RoundedCornerShape(ConsuRadius.md),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
                                    ) {
                                        Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(22.dp))
                                        Text(
                                            stringResource(R.string.calc_liq),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            if (state.debts.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                state.debts.forEachIndexed { index, debt ->
                                    DebtItemDesign(index, debt, viewModel, haptic)
                                    if (index < state.debts.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        )
                                    }
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
                    Column {
                        Text(
                            stringResource(R.string.payment_capacity),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else ConsuNavy
                        )
                        Text(
                            stringResource(R.string.payment_capacity_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ConsuRadius.xxl),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(ConsuSpacing.xl)) {
                            Text(
                                stringResource(R.string.liquidity_adjustment),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(ConsuRadius.lg),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.AddCircle, null, tint = if (isDark) Color.White else ConsuNavy)

                                    BasicTextField(
                                        value = state.capacityCNCA,
                                        onValueChange = { viewModel.updateCapacityCNCA(it) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                        textStyle = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (isDark) Color.White else ConsuNavy
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        singleLine = true
                                    )

                                    Text("MXN", fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.5f) else ConsuNavy.copy(alpha = 0.5f))
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, null, tint = JuicyOrange, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.liquidity_adjustment_desc),
                                    style = MaterialTheme.typography.bodySmall,
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
                    horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .bouncyClick {
                                if (state.debts.any { it.balance.toBD() > BD_ZERO }) {
                                    viewModel.calculateRefinance()
                                }
                            },
                        shape = RoundedCornerShape(ConsuRadius.lg),
                        color = if (state.debts.any { it.balance.toBD() > BD_ZERO })
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Calculate,
                                null,
                                tint = if (state.debts.any { it.balance.toBD() > BD_ZERO })
                                          MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.action_calculate),
                                    color = if (state.debts.any { it.balance.toBD() > BD_ZERO })
                                              MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    stringResource(R.string.calculate_desc),
                                    color = if (state.debts.any { it.balance.toBD() > BD_ZERO })
                                              MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                null,
                                tint = if (state.debts.any { it.balance.toBD() > BD_ZERO })
                                          ConsuOrangeLight
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.width(80.dp).fillMaxHeight()
                            .bouncyClick { viewModel.clearRefinanceData() },
                        shape = RoundedCornerShape(ConsuRadius.lg),
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else ConsuSurfaceVarLight
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, tint = JuicyOrange)
                            Text(stringResource(R.string.action_clear), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = if (isDark) Color.White else ConsuNavy)
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
                } else {
                    GroupedResultsSection(
                        offers = state.offersCNCA,
                        showPremium = state.showPremiumOffers,
                        onTogglePremium = { viewModel.togglePremiumOffers() },
                        onSelectOffer = { offer ->
                            soundManager?.play("pop")
                            viewModel.onSelectOffer(offer)
                        },
                        onShowAmortization = { offer ->
                            soundManager?.play("ui_success")
                            viewModel.showAmortizationFor(offer)
                        },
                        emptyContent = {
                            EmptyStateIllustration(
                                title = stringResource(R.string.no_offers_title),
                                message = stringResource(R.string.no_offers_desc),
                                emoji = "🔍"
                            )
                        }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }

    if (showLiquidationModal) {
        LiquidationCalculatorModal(
            onDismiss = { showLiquidationModal = false }
        )
    }
}

@Composable
fun DebtItemDesign(
    index: Int,
    debt: Debt,
    viewModel: CalculatorViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val soundManager = LocalSoundManager.current
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) {
            soundManager?.play("pop")
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.removeDebtRow(debt)
            true
        } else false
    })

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(ConsuRadius.lg)).background(ErrorRed),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.padding(end = 24.dp))
            }
        }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(ConsuRadius.lg),
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "#${index + 1}",
                        fontWeight = FontWeight.Black,
                        color = JuicyOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Box(modifier = Modifier.weight(1.5f)) {
                        CompactBrandDropdown(
                            selectedBrand = debt.product.ifEmpty { "CSB" },
                            onBrandChange = { newBrand ->
                                viewModel.updateDebt(debt.id, debt.copy(product = newBrand))
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        CompactTermDropdown(
                            selectedTerm = debt.originalTerm.ifEmpty { "60" },
                            onTermChange = { newTerm ->
                                viewModel.updateDebt(debt.id, debt.copy(originalTerm = newTerm))
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)) {
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
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)) {
                    CompactInput(
                        value = debt.rate,
                        label = "TASA / CAT (%)",
                        modifier = Modifier.weight(1f),
                        inputType = KeyboardType.Decimal,
                        isCurrency = false
                    ) { viewModel.updateDebt(debt.id, debt.copy(rate = it)) }
                    CompactInput(
                        value = debt.paymentsMade,
                        label = "NÚM. PAGOS",
                        modifier = Modifier.weight(1f),
                        inputType = KeyboardType.Number,
                        isCurrency = false
                    ) { viewModel.updateDebt(debt.id, debt.copy(paymentsMade = it)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactBrandDropdown(
    selectedBrand: String,
    onBrandChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val brands = FinancialConfig.IMSS_PRODUCTS_BY_BRAND.keys.toList()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(ConsuRadius.dropdown),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (selectedBrand) {
                                    "CSB" -> ConsuNavy
                                    "OPC" -> ConsuSuccess
                                    else         -> Color.Gray
                                }
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedBrand,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            brands.forEach { brand ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (brand) {
                                            "CSB" -> ConsuNavy
                                            "OPC" -> ConsuSuccess
                                            else         -> Color.Gray
                                        }
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(brand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    onClick = {
                        onBrandChange(brand)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTermDropdown(
    selectedTerm: String,
    onTermChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val terms = listOf("12", "18", "24", "36", "48", "54", "60")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(ConsuRadius.dropdown),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedTerm M",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            terms.forEach { term ->
                DropdownMenuItem(
                    text = {
                        Text(text = "$term meses", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    },
                    onClick = {
                        onTermChange(term)
                        expanded = false
                    }
                )
            }
        }
    }
}
