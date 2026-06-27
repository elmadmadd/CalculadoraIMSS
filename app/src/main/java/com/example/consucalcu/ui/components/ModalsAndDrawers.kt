package com.example.consucalcu.ui.components

import com.example.consucalcu.R
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.consucalcu.*
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.model.*
import com.example.consucalcu.utils.*
import com.example.consucalcu.viewmodel.*
import java.math.BigDecimal
import java.math.RoundingMode

// ==========================================
// 1. DETAIL ROW & DASHED DIVIDER HELPERS
// ==========================================
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontSize = 20.sp
        )
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray.copy(alpha = 0.3f),
    thickness: Dp = 1.dp,
    dashIntervals: FloatArray = floatArrayOf(15f, 15f)
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .padding(vertical = 16.dp),
        onDraw = {
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = thickness.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(dashIntervals, 0f)
            )
        }
    )
}

// ==========================================
// 2. CLIENT FORM CONTENT
// ==========================================
@Composable
fun ClientFormContent(
    viewModel: CalculatorViewModel,
    onGenerate: (String) -> Unit,
    onGenerateTicket: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val d = state.clientData

    Column(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        Text(
            "Formulario de Captura",
            style = JuicyTypography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "Completa todos los campos para generar el layout o ticket",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            item {
                FormSectionCard("IDENTIFICACIÓN", Icons.Rounded.Badge) {
                    JuicyInput(
                        "Nombre Completo",
                        d.name,
                        { viewModel.updateClientData(d.copy(name = it)) },
                        KeyboardType.Text,
                        Icons.Rounded.Person
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) {
                                    JuicyInput(
                                        "NSS", d.nss,
                                        { if (it.length <= 11) viewModel.updateClientData(d.copy(nss = it)) },
                                        KeyboardType.Number, Icons.Rounded.Numbers
                                    )
                                }
                                if (d.nss.isNotEmpty()) {
                                    Spacer(Modifier.width(8.dp))
                                    NSSValidationIndicator(d.nss)
                                }
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "CURP", d.curp,
                                { if (it.length <= 18) viewModel.updateClientData(d.copy(curp = it)) },
                                keyboardTypeFromString(d.curp) ?: KeyboardType.Text, Icons.Rounded.Badge
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "RFC (con homoclave)", d.rfc,
                                { if (it.length <= 13) viewModel.updateClientData(d.copy(rfc = it)) },
                                KeyboardType.Text, Icons.Rounded.Badge
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Edad", d.age,
                                { if (it.length <= 3) viewModel.updateClientData(d.copy(age = it)) },
                                KeyboardType.Number, Icons.Rounded.CalendarMonth
                            )
                        }
                    }
                }
            }

            item {
                FormSectionCard("CONTACTO Y BANCO", Icons.Rounded.Phone) {
                    JuicyInput(
                        "Correo Electrónico", d.email,
                        { viewModel.updateClientData(d.copy(email = it)) },
                        KeyboardType.Email, Icons.Rounded.Person
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Teléfono", d.phone,
                                { if (it.length <= 10) viewModel.updateClientData(d.copy(phone = it)) },
                                KeyboardType.Phone, Icons.Rounded.Phone
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Tipo de Pensión", d.pensionType,
                                { viewModel.updateClientData(d.copy(pensionType = it)) },
                                KeyboardType.Text, Icons.Rounded.AccountBalance
                            )
                        }
                    }
                    JuicyInput(
                        "CLABE Interbancaria", d.clabe,
                        { if (it.length <= 18) viewModel.updateClientData(d.copy(clabe = it)) },
                        KeyboardType.Number, Icons.Rounded.CreditCard
                    )
                    JuicyInput(
                        "Banco", d.bank,
                        { viewModel.updateClientData(d.copy(bank = it)) },
                        KeyboardType.Text, Icons.Rounded.AccountBalance
                    )
                }
            }

            item {
                FormSectionCard("DATOS MÉDICOS", Icons.Rounded.Info) {
                    JuicyInput(
                        "Enfermedad", d.disease,
                        { viewModel.updateClientData(d.copy(disease = it)) },
                        KeyboardType.Text, Icons.Rounded.LocalFireDepartment
                    )
                    JuicyInput(
                        "Fecha de Inicio de Padecimiento", d.diseaseDate,
                        { viewModel.updateClientData(d.copy(diseaseDate = it)) },
                        KeyboardType.Text, Icons.Rounded.CalendarMonth
                    )
                }
            }

            item {
                FormSectionCard("TIPO DE TRÁMITE", Icons.Rounded.Description) {
                    TransactionTypeSelector(
                        selected = d.transactionType,
                        onSelect = { viewModel.updateClientData(d.copy(transactionType = it)) }
                    )
                    
                    if (d.transactionType != "NUEVO") {
                        JuicyInput(
                            "ID (Crédito a Liquidar)", d.creditId,
                            { viewModel.updateClientData(d.copy(creditId = it)) },
                            KeyboardType.Text, Icons.Rounded.Numbers
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                JuicyInput(
                                    "Descuento Mensual", d.discountAmount,
                                    { viewModel.updateClientData(d.copy(discountAmount = it)) },
                                    KeyboardType.Text, Icons.Rounded.AttachMoney
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                JuicyInput(
                                    "Núm. Descuentos", d.numDiscounts,
                                    { viewModel.updateClientData(d.copy(numDiscounts = it)) },
                                    KeyboardType.Number, Icons.Rounded.Numbers
                                )
                            }
                        }
                        JuicyInput(
                            "Marca que Liquida", d.liquidatingBrand,
                            { viewModel.updateClientData(d.copy(liquidatingBrand = it)) },
                            KeyboardType.Text, Icons.Rounded.Business
                        )
                    }
                }
            }

            item {
                FormSectionCard("REFERENCIAS", Icons.Rounded.Person) {
                    Text(
                        "1ª Referencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    JuicyInput(
                        "Nombre Completo", d.ref1Name,
                        { viewModel.updateClientData(d.copy(ref1Name = it)) },
                        KeyboardType.Text, Icons.Rounded.Person
                    )
                    JuicyInput(
                        "Teléfono", d.ref1Phone,
                        { if (it.length <= 10) viewModel.updateClientData(d.copy(ref1Phone = it)) },
                        KeyboardType.Phone, Icons.Rounded.Phone
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))

                    Text(
                        "2ª Referencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    JuicyInput(
                        "Nombre Completo", d.ref2Name,
                        { viewModel.updateClientData(d.copy(ref2Name = it)) },
                        KeyboardType.Text, Icons.Rounded.Person
                    )
                    JuicyInput(
                        "Teléfono", d.ref2Phone,
                        { if (it.length <= 10) viewModel.updateClientData(d.copy(ref2Phone = it)) },
                        KeyboardType.Phone, Icons.Rounded.Phone,
                        imeAction = ImeAction.Done
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        JuicyButton(
            text = "COPIAR TEXTO AL PORTAPAPELES",
            color = JuicyGreen,
            shadowColor = JuicyGreenShadow,
            textColor = Color.White,
            height = 60.dp,
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            onClick = { onGenerate(viewModel.generateLayout()) }
        )
        
        Spacer(Modifier.height(12.dp))
        
        JuicyButton(
            text = "COMPARTIR TICKET DORADO",
            color = Color(0xFF1F2937),
            shadowColor = Color(0xFF111827),
            textColor = Color(0xFFFFD700),
            height = 60.dp,
            icon = Icons.Rounded.Star,
            iconColor = Color(0xFFFFD700),
            isLoading = state.isGeneratingTicket,
            onClick = onGenerateTicket
        )
    }
}

private fun keyboardTypeFromString(s: String) = KeyboardType.Text

// ==========================================
// 3. OFFER PREVIEW MODAL
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferPreviewModal(
    offer: LoanOffer,
    viewModel: CalculatorViewModel,
    soundManager: SoundManager,
    context: Context,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (offer.isBest) {
                    Surface(
                        color = JuicyOrange,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "MEJOR OPCIÓN",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Text(
                    text = offer.product,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = offer.tipoTasa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JuicyBlue,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = if(offer.amount > offer.mainValue) "REMANENTE" else "MONTO A RECIBIR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                var heroScaleTarget by remember { mutableFloatStateOf(0.8f) }
                var heroAlphaTarget by remember { mutableFloatStateOf(0f) }
                
                LaunchedEffect(Unit) {
                    heroScaleTarget = 1f
                    heroAlphaTarget = 1f
                }
                
                val heroScale by animateFloatAsState(
                    targetValue = heroScaleTarget,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "heroScale"
                )
                
                val heroAlpha by animateFloatAsState(
                    targetValue = heroAlphaTarget,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "heroAlpha"
                )
                
                val productColors = remember(offer.product, offer.tipoTasa) {
                    ProductBranding.getColors(
                        offer.product, 
                        offer.tipoTasa == "RETENCION"
                    )
                }
                
                Text(
                    text = "$${offer.mainValue.toCurrency()}",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 56.sp,
                        letterSpacing = (-2).sp
                    ),
                    color = productColors.primary,
                    modifier = Modifier.graphicsLayer {
                        scaleX = heroScale
                        scaleY = heroScale
                        alpha = heroAlpha
                    }
                )
                
                if (offer.amount > offer.mainValue) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Monto total: $${offer.amount.toCurrency()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        DetailRow(
                            icon = Icons.Rounded.AttachMoney,
                            label = "MENSUALIDAD",
                            value = "$${offer.monthlyPayment.toCurrency()}",
                            valueColor = JuicyOrange
                        )
                        
                        DashedDivider()
                        
                        DetailRow(
                            icon = Icons.Rounded.CalendarMonth,
                            label = "PLAZO",
                            value = "${offer.term} Meses",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        DashedDivider()
                        
                        DetailRow(
                            icon = Icons.AutoMirrored.Rounded.TrendingDown,
                            label = "CAT",
                            value = offer.cat,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                val isRefinance = state.selectedTab == 0
                if (isRefinance) {
                    val currentPayment = remember(state.debts) { 
                        state.debts.sumOf { it.payment.toBD() } 
                    }
                    
                    if (currentPayment > BigDecimal.ZERO) {
                        Spacer(Modifier.height(24.dp))
                        BeforeAfterChart(
                            currentPayment = currentPayment,
                            newPayment = offer.monthlyPayment
                        )
                    }
                }

                if (offer.reasons.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "RAZONES DE APROBACIÓN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    offer.reasons.forEach { reason ->
                        Surface(
                            color = JuicyGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEmotions,
                                    contentDescription = null,
                                    tint = JuicyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    reason,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(Modifier.weight(0.3f)) {
                         JuicyButton(
                            text = "",
                            icon = Icons.Rounded.Share,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowColor = MaterialTheme.colorScheme.outline.copy(alpha=0.15f),
                            textColor = MaterialTheme.colorScheme.onSurface,
                            height = 60.dp,
                            soundManager = soundManager
                         ) {
                             soundManager.play("pop")
                             viewModel.prepareTicketSharing(context, offer)
                         }
                    }

                    Box(Modifier.weight(1f)) {
                         JuicyButton(
                            text = "CONTINUAR",
                            color = productColors.primary,
                            shadowColor = productColors.shadow,
                            textColor = Color.White,
                            height = 60.dp,
                            soundManager = soundManager,
                            onClick = onContinue
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ==========================================
// 4. CONFIG DIALOG & CONFIG FORM CONTENT
// ==========================================
@Composable
fun ConfigDialog(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    Dialog(onDismissRequest = { viewModel.toggleConfigDialog(false) }) {
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = MaterialTheme.colorScheme.outline
        ) {
            ConfigFormContent(viewModel, ctx, soundManager)
        }
    }
}

@Composable
fun ConfigFormContent(
    viewModel: CalculatorViewModel,
    ctx: Context,
    soundManager: SoundManager,
    isMandatory: Boolean = false,
    onSaved: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var tempName by remember { mutableStateOf(state.userProfile.name) }
    var tempUser by remember { mutableStateOf(state.userProfile.username) }
    var tempPwd by remember { mutableStateOf(state.userProfile.password) }
    var tempSup by remember { mutableStateOf(state.userProfile.supervisor) }

    val isValid = tempName.isNotBlank() && tempUser.isNotBlank() &&
                  tempPwd.isNotBlank() && tempSup.isNotBlank()

    Column(
        Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isMandatory) {
            Image(
                painter = painterResource(id = R.drawable.avatar_mascot),
                contentDescription = "Buddy",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(JuicyOrange, CircleShape)
                    .border(3.dp, JuicyOrange, CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "¡Bienvenido Asesor!",
                style = MaterialTheme.typography.headlineMedium,
                color = JuicyBlue,
                fontWeight = FontWeight.Black
            )
            Text(
                "Para comenzar, necesito configurar tu perfil.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        } else {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = JuicyBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "CONFIGURACIÓN",
                style = MaterialTheme.typography.titleLarge,
                color = JuicyBlue,
                fontWeight = FontWeight.Black
            )
            Text(
                "Datos del Asesor",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))
        }

        JuicyInput(
            "Nombre Completo",
            tempName,
            { tempName = it },
            KeyboardType.Text,
            Icons.Rounded.Person
        )

        Spacer(Modifier.height(12.dp))

        JuicyInput(
            "Usuario (BP)",
            tempUser,
            { tempUser = it },
            KeyboardType.Text,
            Icons.Rounded.Badge
        )

        Spacer(Modifier.height(12.dp))

        JuicyInput(
            "Contraseña del Sistema",
            tempPwd,
            { tempPwd = it },
            KeyboardType.Text,
            Icons.Rounded.Business
        )

        Spacer(Modifier.height(12.dp))

        JuicyInput(
            "Nombre del Supervisor",
            tempSup,
            { tempSup = it },
            KeyboardType.Text,
            Icons.Rounded.Person,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Estos datos se usarán automáticamente en el layout de captura",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        JuicyButton(
            text = if (isMandatory) "¡COMENZAR AHORA!" else "GUARDAR CONFIGURACIÓN",
            color = if (isValid) JuicyGreen else Color.Gray,
            shadowColor = if (isValid) JuicyGreenShadow else Color.DarkGray,
            textColor = Color.White,
            icon = if (isMandatory) Icons.Rounded.PlayArrow else Icons.Rounded.EmojiEvents,
            soundManager = soundManager
        ) {
            if (isValid) {
                viewModel.saveProfile(
                    UserProfile(tempName, tempUser, tempPwd, tempSup)
                )
                onSaved()
            }
        }
    }
}

// ==========================================
// 5. OFFERS COMPARATOR
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersComparator(
    offers: List<LoanOffer>,
    onDismiss: () -> Unit,
    onSelectOffer: (LoanOffer) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Comparador de Ofertas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Producto", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Monto", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Mensualidad", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Plazo", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
            
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                itemsIndexed(offers) { index, offer ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectOffer(offer) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (offer.isBest) JuicyGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (offer.isBest) BorderStroke(2.dp, JuicyGreen) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1.2f)) {
                                Text(offer.product, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                if (offer.isBest) {
                                    Text("⭐ Mejor Opción", fontSize = 9.sp, color = JuicyGreen)
                                }
                            }
                            Text("$${offer.amount.toCurrency()}", Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                            Text("$${offer.monthlyPayment.toCurrency()}", Modifier.weight(1.2f), fontSize = 13.sp, textAlign = TextAlign.End)
                            Text("${offer.term}m", Modifier.weight(0.8f), fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            JuicyButton(
                text = "CERRAR",
                color = MaterialTheme.colorScheme.secondary,
                shadowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                textColor = Color.White,
                onClick = onDismiss
            )
        }
    }
}

// ==========================================
// 6. AMORTIZATION SIMULATOR
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationSimulator(
    offer: LoanOffer,
    amortizationTable: List<AmortizationRow>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Tabla de Amortización",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${offer.product} - ${offer.term} meses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JuicyBlue.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monto", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$${offer.amount.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyBlue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Mensualidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$${offer.monthlyPayment.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total a Pagar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        val totalPaid = amortizationTable.sumOf { it.payment }
                        Text("$${totalPaid.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyOrange)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mes", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("Interés", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("IVA", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("Capital", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("Saldo", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                    }
                }
                
                itemsIndexed(amortizationTable) { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${row.month}", Modifier.weight(0.5f), fontSize = 11.sp)
                        Text("$${row.interest.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyRed.copy(alpha = 0.7f))
                        Text("$${row.iva.toCurrency()}", Modifier.weight(0.7f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyOrange.copy(alpha = 0.7f))
                        Text("$${row.principal.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyGreen.copy(alpha = 0.7f))
                        Text("$${row.balance.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            JuicyButton(
                text = "CERRAR",
                color = MaterialTheme.colorScheme.secondary,
                shadowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                textColor = Color.White,
                onClick = onDismiss
            )
        }
    }
}

// ==========================================
// 7. HISTORY SHEET UI
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<CalculationDraft>,
    onRestore: (CalculationDraft) -> Unit,
    onClear: () -> Unit,
    onDelete: (CalculationDraft) -> Unit
) {
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
           Modifier.fillMaxWidth(), 
           horizontalArrangement = Arrangement.SpaceBetween,
           verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Historial Reciente", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Limpiar", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        if (history.isEmpty()) {
            Column(
                Modifier.fillMaxWidth(), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "📭", 
                    fontSize = 48.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No hay borradores guardados",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history, key = { it.timestamp }) { draft ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(draft)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        modifier = Modifier.animateItem(),
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 
                                MaterialTheme.colorScheme.errorContainer 
                            else Color.Transparent

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    ) {
                        JuicyCard(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                            shadowColor = MaterialTheme.colorScheme.outline.copy(alpha=0.3f),
                            modifier = Modifier.clickable { onRestore(draft) }
                        ) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        draft.type, 
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${draft.debts.size} deudas • Cap: $${draft.capacity}", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(draft.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(draft.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.4f)
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

// ==========================================
// 8. LIQUIDATION CALCULATOR & HELPERS
// ==========================================
fun calculateMonthlyRate(capital: Double, payment: Double, months: Int): Double {
    if (payment * months <= capital) return 0.0
    var lower = 0.000001
    var upper = 1.0
    var rate = 0.0
    for (i in 0 until 100) {
        rate = (lower + upper) / 2.0
        val presentValue = payment * (1 - Math.pow(1 + rate, -months.toDouble())) / rate
        if (presentValue.compareTo(capital) > 0) {
            lower = rate
        } else {
            upper = rate
        }
        if (kotlin.math.abs(upper - lower) < 1e-8) break
    }
    return rate
}

data class LiquidationResultData(
    val isValid: Boolean = false,
    val iMensual: Double = 0.0,
    val iAnual: Double = 0.0,
    val cat: Double = 0.0,
    val saldoCorte: Double = 0.0,
    val saldoHoy: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidationCalculatorModal(
    onDismiss: () -> Unit,
    soundManager: SoundManager? = null
) {
    var capitalInput by remember { mutableStateOf("") }
    var paymentInput by remember { mutableStateOf("") }
    var monthsInput by remember { mutableStateOf("") }
    var paymentsMadeInput by remember { mutableStateOf("") }
    var daysStr by remember { mutableStateOf("0") }
    var isQuincenal by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    val formatCurrency = { amount: Double ->
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "MX")).format(amount)
    }

    val calculationResult = remember(capitalInput, paymentInput, monthsInput, paymentsMadeInput, daysStr, isQuincenal) {
        try {
            val p = capitalInput.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
            val r = paymentInput.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
            val n = monthsInput.toIntOrNull() ?: 0
            val hechos = paymentsMadeInput.toIntOrNull() ?: 0
            val dias = daysStr.toIntOrNull() ?: 0

            if (p > 0 && r > 0 && n > 0 && hechos >= 0 && n > hechos) {
                val iPeriodo = calculateMonthlyRate(p, r, n)
                val periodsPerYear = if (isQuincenal) 24.0 else 12.0
                
                val iMensual = if (isQuincenal) iPeriodo * 2 else iPeriodo
                val iAnual = iMensual * 12
                val cat = (Math.pow(1 + iPeriodo, periodsPerYear) - 1) * 100

                val remaining = n - hechos
                val saldoCorte = if (iPeriodo > 0) r * (1 - Math.pow(1 + iPeriodo, -remaining.toDouble())) / iPeriodo else r * remaining.toDouble()

                val daysInPeriod = if (isQuincenal) 15.2 else 30.4
                val iDiaria = iPeriodo / daysInPeriod
                val interesGenerado = saldoCorte * iDiaria * dias
                val saldoHoy = saldoCorte + interesGenerado

                LiquidationResultData(
                    isValid = true,
                    iMensual = iMensual,
                    iAnual = iAnual,
                    cat = cat,
                    saldoCorte = saldoCorte,
                    saldoHoy = saldoHoy
                )
            } else {
                LiquidationResultData(isValid = false)
            }
        } catch (e: Exception) {
            LiquidationResultData(isValid = false)
        }
    }

    LaunchedEffect(calculationResult.isValid, calculationResult.saldoHoy) {
        if (calculationResult.isValid) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Calculadora de Liquidación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Calcula instantáneamente el monto de liquidación reconstruyendo las condiciones exactas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            AnimatedVisibility(
                visible = calculationResult.isValid,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                JuicyGradientCard(
                    brush = Brush.linearGradient(listOf(Color(0xFF141E30), Color(0xFF243B55))),
                    shadowColor = Color(0xFF141E30).copy(alpha=0.3f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MONTO TOTAL A LIQUIDAR HOY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formatCurrency(calculationResult.saldoHoy),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Al corte mensual", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.6f))
                                Text(formatCurrency(calculationResult.saldoCorte), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tasa (Mensual)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.6f))
                                Text(String.format(java.util.Locale.US, "%.2f %%", calculationResult.iMensual * 100), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATOS DEL PRÉSTAMO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { isQuincenal = false },
                            color = if (!isQuincenal) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Text("MEN", fontSize = 10.sp, modifier = Modifier.padding(horizontal=8.dp, vertical=4.dp), color = if(!isQuincenal) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            onClick = { isQuincenal = true },
                            color = if (isQuincenal) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Text("QUI", fontSize = 10.sp, modifier = Modifier.padding(horizontal=8.dp, vertical=4.dp), color = if(isQuincenal) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactInput(
                        value = capitalInput,
                        label = "Capital Originado",
                        isCurrency = true,
                        inputType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    ) { capitalInput = it }
                    CompactInput(
                        value = paymentInput,
                        label = if(isQuincenal) "Descuento Qui." else "Descuento Men.",
                        isCurrency = true,
                        inputType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    ) { paymentInput = it }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactInput(
                        value = monthsInput,
                        label = if(isQuincenal) "Total Quincenas" else "Total Meses",
                        inputType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    ) { monthsInput = it }
                    CompactInput(
                        value = paymentsMadeInput,
                        label = "Pagos Pagados",
                        inputType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    ) { paymentsMadeInput = it }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "INTERESES A HOY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                CompactInput(
                    value = daysStr,
                    label = "Días desde el último pago (corte)",
                    inputType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                ) { daysStr = it }

                if (capitalInput.contains("35,000") && paymentInput.contains("3,561")) {
                    Text(
                        text = "💡 Tip: En cartas de Consupago, usa '17' días para igualar el ejemplo exacto.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("NUEVO", "CNCA", "2ª DISP", "LCOM")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(option) },
                color = if (isSelected) JuicyPurple else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    2.dp,
                    if (isSelected) JuicyPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isSelected) Icons.Rounded.EmojiEvents else Icons.Rounded.Loop,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        option,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessOverlay(onDismiss: () -> Unit) {
    var offsetY by remember { mutableStateOf(-120f) }
    var alpha by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(0.8f) }
    
    LaunchedEffect(Unit) {
        // Animación de entrada estilo Telegram Pop
        launch {
            animate(
                -120f,
                0f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { value, _ -> offsetY = value }
        }
        
        launch {
            animate(0f, 1f, animationSpec = tween(400)) { value, _ -> alpha = value }
        }

        launch {
            animate(
                0.8f,
                1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = Spring.StiffnessMedium
                )
            ) { value, _ -> scale = value }
        }
        
        delay(2400)
        
        // Salida suave
        launch {
            animate(0f, -40f, animationSpec = tween(400)) { value, _ -> offsetY = value }
        }
        animate(1f, 0f, animationSpec = tween(300)) { value, _ -> alpha = value }
        
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .offset(y = offsetY.dp)
                .scale(scale)
                .graphicsLayer { this.alpha = alpha },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            JuicyGreen.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = JuicyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        "¡LISTO!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Información copiada al portapapeles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
