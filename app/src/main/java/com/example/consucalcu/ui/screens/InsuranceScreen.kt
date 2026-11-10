package com.example.consucalcu.ui.screens
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import com.example.consucalcu.utils.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.R
import com.example.consucalcu.*
import com.example.consucalcu.model.*
import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.data.NormativaIMSS
import com.example.consucalcu.ui.components.*
import com.example.consucalcu.viewmodel.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
) {
    val soundManager = LocalSoundManager.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // Al terminar de calcular, lleva al promotor al resultado.
    var wasLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (wasLoading && !state.isLoading && state.insResultStatus.isNotEmpty()) {
            listState.animateScrollToItem(index = 4)
        }
        wasLoading = state.isLoading
    }

    val focusManager = LocalFocusManager.current
    val calculate = {
        focusManager.clearFocus()
        viewModel.calculateInsurance()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)
    ) {
        item {
            AnimatedResultItem(index = 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ConsuSpacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.insurance_calculator_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.imss_pensioners),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
                    )
                }
            }
        }

        item {
            AnimatedResultItem(index = 1) {
                JuicyCard(
                    color = MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(Modifier.padding(ConsuSpacing.lg)) {
                        Text(
                            stringResource(R.string.credit_data),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(ConsuSpacing.md))

                        JuicyProviderDropdown(
                            selectedValue = state.insProvider,
                            onSelectionChange = {
                                viewModel.updateInsProvider(it)
                                soundManager?.play("pop")
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        JuicyTermDropdown(
                            selectedValue = state.insTerm,
                            options = NormativaIMSS.plazosSeguro(state.insProvider),
                            labelFor = { term ->
                                if (NormativaIMSS.esRetencion(state.insProvider, term)) {
                                    "$term meses · Retención"
                                } else {
                                    "$term meses"
                                }
                            },
                            onSelectionChange = {
                                viewModel.updateInsTerm(it)
                                soundManager?.play("pop")
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        JuicyInput(
                            label = stringResource(R.string.credit_amount_label),
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
                    Column(Modifier.padding(ConsuSpacing.lg).animateContentSize()) {
                        Text(
                            stringResource(R.string.client_data_header),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(20.dp))



                        // 2. Tipo de Pensión Dropdown
                        Text(
                            "TIPO DE PENSIÓN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        var pensionExpanded by remember { mutableStateOf(false) }
                        val pensionTypes = listOf(
                            "Jubilación", "Vejez", "Cesantía", "Viudez", "Invalidez", "Orfandad", "Riesgo de Trabajo", "Ascendencia", "Otra"
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = pensionExpanded,
                                onExpandedChange = { pensionExpanded = !pensionExpanded }
                            ) {
                                Surface(
                                    onClick = { pensionExpanded = true },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, true),
                                    shape = RoundedCornerShape(ConsuRadius.lg),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(state.insPensionType, color = MaterialTheme.colorScheme.onSurface)
                                        Icon(
                                            if (pensionExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                                ExposedDropdownMenu(
                                    expanded = pensionExpanded,
                                    onDismissRequest = { pensionExpanded = false }
                                ) {
                                    pensionTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = {
                                                viewModel.updateInsPensionType(type)
                                                pensionExpanded = false
                                                soundManager?.play("pop")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 3. Edad Input
                        val ageInt = state.insAge.toIntOrNull() ?: 0
                        val isAgeWarning = ageInt > 75
                        val isAgeError = ageInt > 85

                        JuicyInput(
                            label = if (isAgeError) stringResource(R.string.age_exceeds_limit) else stringResource(R.string.client_age),
                            value = state.insAge,
                            onValChange = { viewModel.updateInsAge(it) },
                            type = KeyboardType.Number,
                            leadingIcon = if (isAgeError) Icons.Rounded.Close else Icons.Rounded.Person,
                            isCurrency = false,
                            isError = isAgeError,
                            imeAction = ImeAction.Done,
                            onImeDone = calculate
                        )




                        if (isAgeWarning && !isAgeError) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = ConsuWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "⚠️ Precaución: Edades avanzadas pueden requerir pago total o rechazo",
                                    color = ConsuWarning,
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
                Row(horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)) {
                    Box(Modifier.weight(1f)) {
                        JuicyButton(
                            text = stringResource(R.string.action_calculate),
                            icon = Icons.Rounded.Calculate,
                            color = MaterialTheme.colorScheme.primary,
                            shadowColor = Color.Transparent,
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            height = 56.dp,
                            isLoading = state.isLoading,
                            onClick = {
                                soundManager?.play("pop")
                                calculate()
                            }
                        )
                    }
                    JuicyIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        contentDescription = "Limpiar datos del seguro",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowColor = Color.Transparent,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 56.dp
                    ) {
                        soundManager?.play("pop")
                        viewModel.clearInsuranceData()
                    }
                }
            }
        }

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
                        Column(Modifier.padding(ConsuSpacing.xxl)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
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

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.results_label),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodySmall,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            isRejected -> stringResource(R.string.status_rejected)
                                            isFree -> stringResource(R.string.status_free)
                                            hasCost -> stringResource(R.string.status_with_cost)
                                            else -> "PROCESADO"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = state.insResultColor,
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            if (hasCost) {
                                Spacer(Modifier.height(24.dp))

                                Surface(
                                    color = ConsuNavy,
                                    shape = RoundedCornerShape(ConsuRadius.lg),
                                    border = BorderStroke(1.dp, state.insResultColor.copy(alpha = 0.7f)),
                                    shadowElevation = 1.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(ConsuSpacing.xl),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "MONTO DE DOMICILIACIÓN",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            letterSpacing = 1.2.sp,
                                            textAlign = TextAlign.Center
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
                                            letterSpacing = (-1).sp,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(Modifier.height(6.dp))
                                        Surface(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            val badgeText = if (state.insCostTotal > BD_ZERO)
                                                "PLAZO COMPLETO - PAGO ÚNICO"
                                            else
                                                "PRIMER AÑO - PAGO ÚNICO"
                                            Text(
                                                text = badgeText,
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            var showDetails by remember { mutableStateOf(false) }

                            Spacer(Modifier.height(20.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(ConsuRadius.md),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDetails = !showDetails
                                        soundManager?.play("pop")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(ConsuSpacing.lg),
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
                                            text = if (showDetails) stringResource(R.string.hide_analysis) else stringResource(R.string.view_detailed_analysis),
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
                                        shape = RoundedCornerShape(ConsuRadius.md)
                                    ) {
                                        Column(Modifier.padding(ConsuSpacing.lg)) {
                                            Text(
                                                text = stringResource(R.string.analysis_of_ruling),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = state.insResultStatus,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                lineHeight = 20.sp
                                            )

                                            if (state.insDictamenJson.isNotEmpty()) {
                                                Spacer(Modifier.height(16.dp))
                                                val context = androidx.compose.ui.platform.LocalContext.current
                                                val soundManager = LocalSoundManager.current

                                                JuicyButton(
                                                    text = "Copiar Dictamen JSON",
                                                    icon = Icons.Rounded.ContentCopy,
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shadowColor = Color.Transparent,
                                                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    height = 40.dp,
                                                    onClick = {
                                                        viewModel.copyDictamenToClipboard(context)
                                                        soundManager?.play("copy_confirm")
                                                    }
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

    }
}
