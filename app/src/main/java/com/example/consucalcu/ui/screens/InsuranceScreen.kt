package com.example.consucalcu.ui.screens

import com.example.consucalcu.utils.*
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import java.math.BigDecimal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState() // Estado del scroll
    val isDark = isSystemInDarkTheme()
    
    // Auto-scroll cuando hay resultado
    LaunchedEffect(state.insResultStatus) {
        if (state.insResultStatus.isNotEmpty()) {
            delay(500) // Esperar a que la UI se dibuje
            listState.animateScrollToItem(index = 5) // Índice aproximado de la tarjeta de resultado
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedResultItem(index = 0) {
                JuicyGradientCard(
                    brush = if (isDark) {
                        Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF3A3A3C)))
                    },
                    shadowColor = Color(0xFF0A0A0A)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "CALCULADORA DE SEGURO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "IMSS Pensionados",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            AnimatedResultItem(index = 1) {
                JuicyCard(
                    color = MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "DATOS DEL CRÉDITO",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(20.dp))
                        
                        
                        // 1. Selector de Financiera con Colores
                        JuicyProviderDropdown(
                            selectedValue = state.insProvider,
                            onSelectionChange = {
                                viewModel.updateInsProvider(it)
                                soundManager.play("pop")
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // 2. Selector de Plazo con Icono y Estilo
                        JuicyTermDropdown(
                            selectedValue = state.insTerm,
                            onSelectionChange = {
                                viewModel.updateInsTerm(it)
                                soundManager.play("pop")
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        JuicyInput(
                            label = "MONTO DEL CRÉDITO",
                            value = state.insAmount,
                            onValChange = { viewModel.updateInsAmount(it) },
                            type = KeyboardType.Number,
                            leadingIcon = Icons.Rounded.AttachMoney,
                            isCurrency = true,
                            isError = state.inputError,
                            imeAction = ImeAction.Next
                        )
                    }
                }
            }
        }
        
        item {
            AnimatedResultItem(index = 2) {
                JuicyCard(
                    color = MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(Modifier.padding(20.dp).animateContentSize()) {
                        Text(
                            "DATOS DEL CLIENTE",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(20.dp))
                        
                        // Selector Tipo de Pensión (FilterChips)
                        Text(
                            "TIPO DE PENSIÓN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Invalidez", "Vejez", "Otro").forEach { type ->
                                FilterChip(
                                    selected = state.insPensionType == type,
                                    onClick = {
                                        viewModel.updateInsPensionType(type)
                                        soundManager.play("pop")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    label = { Text(type) },
                                    leadingIcon = if (state.insPensionType == type) {
                                        { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JuicyBlue,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Input Edad con Validación Visual
                        val ageInt = state.insAge.toIntOrNull() ?: 0
                        val isAgeWarning = ageInt > 75
                        val isAgeError = ageInt > 85
                        
                        JuicyInput(
                            label = if (isAgeError) "EDAD (EXCEDE LÍMITE)" else "EDAD DEL CLIENTE",
                            value = state.insAge,
                            onValChange = { viewModel.updateInsAge(it) },
                            type = KeyboardType.Number,
                            leadingIcon = if (isAgeError) Icons.Rounded.Close else Icons.Rounded.Person,
                            isCurrency = false,
                            isError = isAgeError,
                            imeAction = if (!state.insIsNewClient) ImeAction.Next else ImeAction.Done
                        )
                        
                        Spacer(Modifier.height(16.dp))

                        // Checkbox Es Cliente Nuevo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = state.insIsNewClient,
                                    onValueChange = { 
                                        viewModel.toggleInsNewClient(it)
                                        soundManager.play("pop")
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.insIsNewClient,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = JuicyBlue)
                            )
                            Text(
                                "Es Cliente Nuevo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Input Antigüedad (Condicional)
                        AnimatedVisibility(visible = !state.insIsNewClient) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                JuicyInput(
                                    label = "ANTIGÜEDAD (MESES)",
                                    value = state.insAntiquity,
                                    onValChange = { viewModel.updateInsAntiquity(it) },
                                    type = KeyboardType.Number,
                                    leadingIcon = Icons.Rounded.DateRange,
                                    isCurrency = false,
                                    imeAction = ImeAction.Done
                                )
                            }
                        }
                        
                        // Texto de ayuda para advertencia
                        if (isAgeWarning && !isAgeError) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = JuicyOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "⚠️ Precaución: Edades avanzadas pueden requerir pago total o rechazo",
                                    color = JuicyOrange,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            AnimatedResultItem(index = 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        JuicyButton(
                            text = "CALCULAR",
                            icon = Icons.Outlined.Savings,
                            color = if (isDark) Color.White else Color(0xFF1C1C1E),
                            shadowColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFC7C7CC),
                            textColor = if (isDark) Color.Black else Color.White,
                            height = 56.dp,
                            isLoading = state.isLoading,
                            soundManager = soundManager,
                            onClick = {
                                soundManager.play("cash")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.calculateInsurance()
                            }
                        )
                    }
                    JuicyIconButton(
                        icon = Icons.Rounded.DeleteSweep,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 56.dp,
                        soundManager = soundManager
                    ) {
                        soundManager.play("pop")
                        viewModel.clearInsuranceData()
                    }
                }
            }
        }
        
        // Result Card - Diseño Simplificado y Claro
        if (state.insResultStatus.isNotEmpty()) {
            item {
                AnimatedResultItem(index = 0) {
                    val isRejected = state.insResultStatus.contains("RECHAZADO")
                    val isFree = state.insResultStatus.contains("NO APLICA")
                    val hasCost = state.insCost1Year > BD_ZERO || state.insCostTotal > BD_ZERO
                    
                    JuicyCard(
                        color = MaterialTheme.colorScheme.surface,
                        shadowColor = state.insResultColor
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            // ========== DICTAMEN PRINCIPAL ==========
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Icono de estado
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            state.insResultColor.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isRejected -> Icons.Rounded.Close
                                            isFree -> Icons.Rounded.CheckCircle
                                            hasCost -> Icons.Rounded.AttachMoney
                                            else -> Icons.AutoMirrored.Rounded.HelpOutline
                                        },
                                        contentDescription = null,
                                        tint = state.insResultColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Spacer(Modifier.width(16.dp))
                                
                                // Texto del dictamen
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "RESULTADO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            isRejected -> "RECHAZADO"
                                            isFree -> "SIN COSTO"
                                            hasCost -> "CON COSTO"
                                            else -> "PROCESADO"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = state.insResultColor,
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            
                            // ========== COSTO (SI APLICA) ==========
                            if (hasCost) {
                                Spacer(Modifier.height(24.dp))
                                
                                // Tarjeta de costo destacada
                                Surface(
                                    color = state.insResultColor,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "COSTO DEL SEGURO",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.2.sp
                                        )
                                        
                                        Spacer(Modifier.height(8.dp))
                                        
                                        val finalCost = if (state.insCostTotal > BD_ZERO) 
                                            state.insCostTotal 
                                        else 
                                            state.insCost1Year
                                        
                                        Text(
                                            text = "$${finalCost.toCurrency()}",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 36.sp,
                                            letterSpacing = (-1).sp
                                        )
                                        
                                        // Indicador de tipo de pago
                                        if (state.insCostTotal > BD_ZERO) {
                                            Spacer(Modifier.height(4.dp))
                                            Surface(
                                                color = Color.White.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = "PAGO TOTAL PLAZO",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "Costo 1er Año",
                                                color = Color.White.copy(alpha = 0.8f),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 12.sp
                                            )
                                        }
                                        
                                        // PAGO MENSUAL ESTIMADO
                                        if (state.insMonthlyPayment > BD_ZERO) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                            Spacer(Modifier.height(12.dp))
                                            
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "MENSUALIDAD APROXIMADA",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "$${state.insMonthlyPayment.toCurrency()}",
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // ========== DETALLES EXPANDIBLES ==========
                            var showDetails by remember { mutableStateOf(false) }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Botón para mostrar/ocultar detalles
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        showDetails = !showDetails
                                        soundManager.play("pop")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (showDetails) "Ocultar análisis" else "Ver análisis detallado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = if (showDetails) 
                                            Icons.Rounded.Close 
                                        else 
                                            Icons.Rounded.AddCircleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            // Contenido expandible
                            AnimatedVisibility(
                                visible = showDetails,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Análisis del Dictamen",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = state.insResultStatus,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                lineHeight = 20.sp
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
        
        item { Spacer(Modifier.height(120.dp)) }
    }
}
