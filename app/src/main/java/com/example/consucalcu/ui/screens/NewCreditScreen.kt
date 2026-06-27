package com.example.consucalcu.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.model.*
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreditScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val isDark = isSystemInDarkTheme()
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== HEADER TEMÁTICO NAVY/ORANGE ==========
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
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
                                ) {
                                    Text("$", color = ConsuOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "CRÉDITO NUEVO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Cotizador multi-producto",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f),
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

        // ========== CARD: DATOS DE COTIZACIÓN ==========
        item {
            AnimatedResultItem(index = 1) {
                JuicyCard(
                    color = MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(Modifier.padding(20.dp).animateContentSize()) {
                        Column {
                            Text(
                                "DATOS DE COTIZACIÓN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(ConsuOrange)
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        // Toggle: Por capacidad / Por monto
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF1E293B) else ConsuChipBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val activeCap = !state.isByAmount
                            val activeMonto = state.isByAmount
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activeCap) (if (isDark) Color.White else ConsuNavy) else Color.Transparent)
                                    .clickable {
                                        soundManager.play("pop")
                                        viewModel.toggleCalculationMode(false)
                                    }
                            ) {
                                Text(
                                    text = "Por capacidad",
                                    color = if (activeCap) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White else ConsuNavy),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activeMonto) (if (isDark) Color.White else ConsuNavy) else Color.Transparent)
                                    .clickable {
                                        soundManager.play("pop")
                                        viewModel.toggleCalculationMode(true)
                                    }
                            ) {
                                Text(
                                    text = "Por monto",
                                    color = if (activeMonto) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White else ConsuNavy),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (state.isByAmount) {
                            JuicySliderInput(
                                label = "¿CUÁNTO NECESITAS?",
                                value = state.newCreditInput,
                                range = 5000f..200000f,
                                steps = 38,
                                onValueChange = { viewModel.updateNewCreditInput(it) },
                                isError = state.inputError
                            )
                        } else {
                            JuicyInput(
                                label = "¿CUÁNTO PUEDES PAGAR?",
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // COTIZAR Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White else ConsuNavy)
                            .bouncyClick(soundManager = soundManager) {
                                viewModel.calculateNewCredit()
                            }
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = if (isDark) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Calculate,
                                        contentDescription = null,
                                        tint = if (isDark) ConsuNavy else ConsuOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "COTIZAR",
                                        color = if (isDark) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isDark) ConsuNavy else ConsuOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // LIMPIAR Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF1E293B) else ConsuChipBg)
                            .bouncyClick(soundManager = soundManager) {
                                viewModel.clearNewCreditData()
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Limpiar",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "LIMPIAR",
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // ========== COMPARAR OFERTAS ==========
        item {
            AnimatedVisibility(
                visible = state.offersNew.isNotEmpty() && !state.isLoading,
                enter = fadeIn(tween(400)) + expandVertically(tween(400, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                JuicyButton(
                    text = "COMPARAR OFERTAS",
                    color = GoldAccentDark,
                    shadowColor = Color(0xFF8B6914),
                    textColor = Color.White,
                    icon = Icons.AutoMirrored.Rounded.CompareArrows,
                    soundManager = soundManager
                ) {
                    viewModel.toggleOffersComparator(true)
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
                            soundManager.play("pop")
                            viewModel.onSelectOffer(offer)
                        },
                        onShowAmortization = { offer ->
                            soundManager.play("magic")
                            viewModel.showAmortizationFor(offer)
                        },
                        emptyContent = {
                            EmptyStateIllustration(
                                title = "Sin ofertas para ti",
                                message = "Ajusta la capacidad de pago o plazo",
                                emoji = "🔍"
                            )
                        }
                    )
                }
            }
        }
        // Spacer para isla flotante
        item { Spacer(Modifier.height(120.dp)) }
    }
}
