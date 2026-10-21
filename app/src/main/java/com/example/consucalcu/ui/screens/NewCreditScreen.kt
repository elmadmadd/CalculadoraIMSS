package com.example.consucalcu.ui.screens
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.consucalcu.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.model.*
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.utils.*
import com.example.consucalcu.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreditScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    isDark: Boolean
) {
    val soundManager = LocalSoundManager.current
    val agreement = FinancialConfig.agreementFor(state.creditAgreementId)
    val agreementOptions = FinancialConfig.ACTIVE_AGREEMENTS
    val paymentInputLabel = if (agreement.isBiweekly) {
        "¿CUÁNTO PUEDES PAGAR POR QUINCENA?"
    } else {
        stringResource(R.string.how_much_can_pay)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)
    ) {
        // ========== HEADER TEMÁTICO NAVY/ORANGE ==========
        item {
            AnimatedResultItem(index = 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_credit_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.new_credit_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
                    )
                }
            }
        }

        if (agreementOptions.size > 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "CONVENIO DE CÁLCULO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ConsuRadius.lg))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        agreementOptions.forEach { option ->
                            val selected = option.id == agreement.id
                            Surface(
                                onClick = {
                                    soundManager?.play("pop")
                                    viewModel.selectCreditAgreement(option.id)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(ConsuRadius.md),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = option.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = agreement.description +
                            (if (agreement.isBiweekly) " · 12-48 meses · mínimo $5,000" else "") +
                            (if (!agreement.supportsConsolidation) " · sin consolidación" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // ========== CARD: DATOS DE COTIZACIÓN ==========
        item {
            AnimatedResultItem(index = 1) {
                JuicyCard(
                    color = MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(Modifier.padding(ConsuSpacing.xl).animateContentSize()) {
                        Text(
                            stringResource(R.string.quotation_data),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))

                        // Toggle: Por capacidad / Por monto
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(ConsuRadius.lg))
                                .background(if (isDark) ConsuDividerDark else ConsuChipBg)
                                .padding(ConsuSpacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.xs)
                        ) {
                            val activeCap = !state.isByAmount
                            val activeMonto = state.isByAmount

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(ConsuRadius.md))
                                    .background(if (activeCap) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        soundManager?.play("pop")
                                        viewModel.toggleCalculationMode(false)
                                    }
                            ) {
                                Text(
                                    text = stringResource(R.string.by_capacity),
                                    color = if (activeCap) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(ConsuRadius.md))
                                    .background(if (activeMonto) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        soundManager?.play("pop")
                                        viewModel.toggleCalculationMode(true)
                                    }
                            ) {
                                Text(
                                    text = stringResource(R.string.by_amount),
                                    color = if (activeMonto) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (state.isByAmount) {
                            JuicyInput(
                                label = stringResource(R.string.how_much_needed),
                                value = state.newCreditInput,
                                onValChange = { viewModel.updateNewCreditInput(it) },
                                type = KeyboardType.Number,
                                leadingIcon = Icons.Rounded.AttachMoney,
                                isCurrency = true,
                                isError = state.inputError,
                                imeAction = ImeAction.Done
                            )
                        } else {
                            JuicyInput(
                                label = paymentInputLabel,
                                value = state.newCreditInput,
                                onValChange = { viewModel.updateNewCreditInput(it) },
                                type = KeyboardType.Number,
                                leadingIcon = Icons.Outlined.Savings,
                                isCurrency = true,
                                isError = state.inputError,
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                }
            }
        }

        // ========== BOTONES: COTIZAR + LIMPIAR ==========
        item {
            AnimatedResultItem(index = 2) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
                ) {
                    // COTIZAR Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(ConsuRadius.lg))
                            .background(MaterialTheme.colorScheme.primary)
                            .bouncyClick {
                                viewModel.calculateNewCredit()
                            }
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.action_quote),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { viewModel.clearNewCreditData() },
                        modifier = Modifier
                            .size(56.dp),
                        shape = RoundedCornerShape(ConsuRadius.lg),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = stringResource(R.string.action_clear),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // ========== RESULTADOS AGRUPADOS ==========
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
                        offers = state.offersNew,
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
                        },
                        onCompare = { viewModel.toggleOffersComparator(true) },
                        showRates = state.showRatesOnCards,
                        onToggleRates = { viewModel.toggleShowRatesOnCards(!state.showRatesOnCards) }
                    )
                }
            }
        }
    }
}
