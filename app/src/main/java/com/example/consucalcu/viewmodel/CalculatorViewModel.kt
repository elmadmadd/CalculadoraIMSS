package com.example.consucalcu.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.consucalcu.*
import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.data.PreferenceRepository
import com.example.consucalcu.data.NormativaIMSS
import com.example.consucalcu.features.FinancialEngine
import com.example.consucalcu.features.TicketData
import com.example.consucalcu.model.CalculatorEffect
import com.example.consucalcu.model.ClientData
import com.example.consucalcu.model.Debt
import com.example.consucalcu.model.LoanOffer
import com.example.consucalcu.model.ResultadoSeguro
import com.example.consucalcu.model.TipoPension
import com.example.consucalcu.model.UserProfile
import com.example.consucalcu.utils.*
import com.example.consucalcu.ui.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

// =================================================================================================
// 10. VIEWMODEL STATE
// =================================================================================================
data class CalculationDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val debts: List<Debt>, // Snapshot
    val capacity: String,
    val type: String = "Refinanciamiento"
)

data class CalculatorUiState(
    val selectedTab: Int = 0,
    val debts: List<Debt> = listOf(Debt()),
    val history: List<CalculationDraft> = emptyList(), // Historial
    val showHistory: Boolean = false, // Modal Historial
    val capacityCNCA: String = "",
    val offersCNCA: List<LoanOffer> = emptyList(),
    val newCreditInput: String = "",
    val isByAmount: Boolean = false,
    val offersNew: List<LoanOffer> = emptyList(),
    val showClientForm: Boolean = false,
    val showConfigDialog: Boolean = false,
    val showSuccessAnim: Boolean = false,
    val showOfferPreview: Boolean = false,
    val inputError: Boolean = false,
    val selectedOfferForLayout: LoanOffer? = null,
    val clientData: ClientData = ClientData(),
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val useRetention: Boolean = false,
    val mandatoryConfigNeeded: Boolean = false,
    
    // Ticket Generation State
    val isGeneratingTicket: Boolean = false,
    
    // New Feature: Shareable Ticket
    val showTicketPreview: Boolean = false,
    val activeTicketData: TicketData? = null,
    
    // Insurance Module State
    val insPensionType: String = "Invalidez",
    val insAge: String = "",
    val insAmount: String = "",
    val insTerm: Int = 60,
    val insProvider: String = "CONSUBANCO",
    val insIsNewClient: Boolean = false,
    val insAntiquity: String = "", // Added antiquity field
    val insResultStatus: String = "",
    val insResultColor: Color = Color.Transparent,
    val insCost1Year: BigDecimal = BD_ZERO,
    val insCostTotal: BigDecimal = BD_ZERO,
    val insMonthlyPayment: BigDecimal = BD_ZERO, // Added PMT field
    
    // ========== NEW FEATURES STATE ==========
    // Comparador de Ofertas
    val showOffersComparator: Boolean = false,
    
    // Simulador de Amortización
    val showAmortizationSimulator: Boolean = false,
    val selectedOfferForAmortization: LoanOffer? = null,
    val amortizationTable: List<AmortizationRow> = emptyList(),

    // Filtro de resultados premium
    val showPremiumOffers: Boolean = true,

    // Ajustes globales de sonido y vibración
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true
)

// =================================================================================================
// 11. VIEWMODEL LOGIC
// =================================================================================================
class CalculatorViewModel(private val repository: PreferenceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()
    private var saveJob: Job? = null
    
    private val _effects = MutableSharedFlow<CalculatorEffect>()
    val effects = _effects.asSharedFlow()
    
    fun setGeneratingTicket(generating: Boolean) {
        _uiState.update { it.copy(isGeneratingTicket = generating) }
    }

    fun loadData() {
        val p = repository.loadProfile()
        val debtsList = repository.loadDebts()
        val client = repository.loadClient()
        val cap = repository.getCapacityCNCA()
        val isSound = repository.getIsSoundEnabled()
        val isHaptic = repository.getIsHapticEnabled()

        _uiState.update {
            it.copy(
                userProfile = p,
                debts = debtsList,
                clientData = client,
                capacityCNCA = cap,
                
                insAge = repository.getInsuranceAge(),
                insAmount = repository.getInsuranceAmount(),
                insTerm = repository.getInsuranceTerm(),
                insProvider = repository.getInsuranceProvider(),
                insPensionType = repository.getInsurancePensionType(),
                insIsNewClient = repository.getInsuranceIsNew(),
                insAntiquity = repository.getInsuranceAntiquity(),
                
                isSoundEnabled = isSound,
                isHapticEnabled = isHaptic,
                
                mandatoryConfigNeeded = p.name.isBlank() || p.username.isBlank() || p.password.isBlank() || p.supervisor.isBlank()
            )
        }
    }

    fun saveData() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            val s = _uiState.value
            repository.saveData(
                debts = s.debts,
                client = s.clientData,
                capacityCNCA = s.capacityCNCA,
                insAge = s.insAge,
                insAmount = s.insAmount,
                insTerm = s.insTerm,
                insProvider = s.insProvider,
                insPensionType = s.insPensionType,
                insIsNew = s.insIsNewClient,
                insAntiquity = s.insAntiquity,
                isSoundEnabled = s.isSoundEnabled,
                isHapticEnabled = s.isHapticEnabled
            )
        }
    }

    fun saveProfile(p: UserProfile) {
        repository.saveProfile(p)
        _uiState.update {
            it.copy(
                userProfile = p,
                showConfigDialog = false,
                mandatoryConfigNeeded = false
            )
        }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun toggleHaptic(enabled: Boolean) {
        _uiState.update { it.copy(isHapticEnabled = enabled) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun addDebtRow() {
        _uiState.update { it.copy(debts = it.debts + Debt(id = System.nanoTime())) }
    }

    fun removeDebtRow(debt: Debt) {
        _uiState.update { it.copy(debts = it.debts - debt) }
    }

    fun updateDebt(id: Long, newDebt: Debt) {
        _uiState.update { s -> s.copy(debts = s.debts.map { if (it.id == id) newDebt else it }) }
    }

    fun updateCapacityCNCA(v: String) {
        _uiState.update { it.copy(capacityCNCA = v) }
    }

    fun clearRefinanceData() {
        _uiState.update {
            it.copy(
                debts = listOf(Debt(id = System.nanoTime())),
                capacityCNCA = "",
                offersCNCA = emptyList()
            )
        }
    }

    fun updateNewCreditInput(v: String) {
        _uiState.update { it.copy(newCreditInput = v) }
    }

    fun toggleCalculationMode(byAmount: Boolean) {
        _uiState.update { it.copy(isByAmount = byAmount) }
    }

    fun clearNewCreditData() {
        _uiState.update { it.copy(newCreditInput = "", offersNew = emptyList()) }
    }

    fun toggleConfigDialog(show: Boolean) {
        _uiState.update { it.copy(showConfigDialog = show) }
    }

    fun updateClientData(data: ClientData) {
        _uiState.update { it.copy(clientData = data) }
    }

    fun onSelectOffer(offer: LoanOffer) {
        _uiState.update { it.copy(selectedOfferForLayout = offer, showOfferPreview = true) }
    }

    fun dismissPreview() {
        _uiState.update { it.copy(showOfferPreview = false) }
    }

    fun continueToClientForm() {
        _uiState.update { it.copy(showOfferPreview = false, showClientForm = true) }
    }

    fun dismissForm() {
        _uiState.update { it.copy(showClientForm = false) }
    }

    fun triggerSuccess() {
        _uiState.update { it.copy(showSuccessAnim = true) }
    }

    fun hideSuccess() {
        _uiState.update { it.copy(showSuccessAnim = false) }
    }

    fun calculateRefinance() {
        saveToHistory("Refinanciamiento")
        viewModelScope.launch {
            val current = _uiState.value
            val totalDebts = current.debts.sumOf { it.balance.toBD() }
            val cap = current.capacityCNCA.toBD()
            
            if (totalDebts <= BD_ZERO && cap == BD_ZERO) {
                 _uiState.update { it.copy(inputError = true) }
                 _effects.emit(CalculatorEffect.PlaySound("wrong"))
                 _effects.emit(CalculatorEffect.TriggerHaptics(HapticPatterns.ERROR)) // HapticPatterns.ERROR
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }

            _uiState.update { it.copy(isLoading = true, offersCNCA = emptyList()) }
            delay(1500)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value
                val capVal = state.capacityCNCA.toBD()
                
                val debtBalance = state.debts.sumOf { it.balance.toBD() }
                val debts = state.debts.filter { it.balance.toBD() > BD_ZERO }
                val results = mutableListOf<LoanOffer>()
                
                val totalPagosDeudas = state.debts.sumOf { it.payment.toBD() }
                val capacidadTotal = totalPagosDeudas.add(capVal)

                if (capacidadTotal > BD_ZERO && debts.isNotEmpty()) {
                    // Detectar plazos originales de las deudas
                    val originalTerms = debts.mapNotNull { it.originalTerm.toIntOrNull() }.distinct()
                    
                    // Determinar qué plazos calcular según las reglas de extensión de plazo
                    val termsToCalculate = mutableSetOf<Int>()
                    originalTerms.forEach { origTerm ->
                        when (origTerm) {
                            12         -> termsToCalculate.addAll(listOf(36, 48, 54, 60))
                            24         -> termsToCalculate.addAll(listOf(48, 54, 60))
                            36, 48     -> termsToCalculate.addAll(listOf(54, 60))
                            54         -> termsToCalculate.add(60)
                            else       -> termsToCalculate.add(60)
                        }
                    }
                    if (termsToCalculate.isEmpty()) termsToCalculate.add(60)

                    // CNCA: Solo aplica a products TRADICIONALES
                    // ESPECIAL y VIP son solo para créditos nuevos
                    val cncaProducts = FinancialConfig.PRODUCTS.filter { (k, _) ->
                        !k.endsWith("ESPECIAL") && !k.endsWith("VIP")
                    }

                    cncaProducts.forEach { (prodKey, config) ->
                        termsToCalculate.forEach { term ->
                            var retencionAplicada = false
                            
                            // Validar 24 pagos mínimos para Retención en CNCA
                            val minPagosRetencion = 24
                            val minPagosActuales = debts.minOfOrNull { it.paymentsMade.toIntOrNull() ?: 0 } ?: 0
                            val cumplePagos = minPagosActuales >= minPagosRetencion

                            if (cumplePagos) {
                                val calcRet = FinancialEngine.calculate(
                                    prodKey,
                                    capacidadTotal,
                                    term,
                                    false,
                                    true, // useRetention = true
                                    BD_500
                                )
                                if (calcRet != null && calcRet.second == "RETENCION") {
                                    val (res, tipo) = calcRet
                                    val offerRet = LoanOffer(
                                        prodKey,
                                        res.amount,
                                        res.payment,
                                        term,
                                        cat = config.meta.catRet,
                                        tipoTasa = tipo
                                    )
                                    // Usar MIN_RET_CNCA para refinanciamiento
                                    val minRetCNCA = FinancialEngine.MIN_RET_CNCA[prodKey] ?: BigDecimal.ZERO
                                    if (offerRet.amount >= minRetCNCA) {
                                        val (viable, reasons) = FinancialEngine.checkNormative(
                                            prodKey,
                                            debts,
                                            offerRet,
                                            config
                                        )
                                        if (viable && offerRet.amount >= debtBalance && FinancialEngine.isValidTermAndAmount(prodKey, term, offerRet.amount)) {
                                            val remnant = offerRet.amount.subtract(debtBalance)
                                            val finalReasons = reasons.toMutableList().apply {
                                                add("Tasa Preferencial (24+ pagos)")
                                            }
                                            results.add(
                                                offerRet.copy(
                                                    mainValue = remnant,
                                                    mainLabel = "REMANENTE",
                                                    reasons = finalReasons,
                                                    isBest = true
                                                )
                                            )
                                            retencionAplicada = true
                                        }
                                    }
                                }
                            }
                            
                            // FALLBACK: Si no aplicó retención, calcular tradicional
                            if (!retencionAplicada) {
                                val calcData = FinancialEngine.calculate(
                                    prodKey,
                                    capacidadTotal,
                                    term,
                                    false,
                                    false, // useRetention = false
                                    BD_500
                                )
                                if (calcData != null) {
                                    val (res, tipo) = calcData
                                    val offer = LoanOffer(
                                        prodKey,
                                        res.amount,
                                        res.payment,
                                        term,
                                        cat = config.meta.catTrad,
                                        tipoTasa = tipo
                                    )
                                    if (offer.amount >= config.minGlobal) {
                                        val (viable, reasons) = FinancialEngine.checkNormative(
                                            prodKey,
                                            debts,
                                            offer,
                                            config
                                        )
                                        if (viable && offer.amount >= debtBalance && FinancialEngine.isValidTermAndAmount(prodKey, term, offer.amount)) {
                                            val remnant = offer.amount.subtract(debtBalance)
                                            results.add(
                                                offer.copy(
                                                    mainValue = remnant,
                                                    mainLabel = "REMANENTE",
                                                    reasons = reasons
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Ordenar y marcar ofertas de retención como "mejor opción"
                results.sortedByDescending { it.mainValue }
                    .mapIndexed { i, offer ->
                        offer.copy(isBest = offer.tipoTasa == "RETENCION" || i == 0)
                    }
            }
            if (calculatedOffers.isNotEmpty()) {
                _effects.emit(CalculatorEffect.PlaySound("cash"))
                _effects.emit(CalculatorEffect.TriggerHaptics(HapticPatterns.SUCCESS)) // HapticPatterns.SUCCESS
            } else {
                // Sin ofertas disponibles
                _effects.emit(CalculatorEffect.PlaySound("wrong"))
                _effects.emit(CalculatorEffect.TriggerHaptics(longArrayOf(0, 50, 40, 50))) // HapticPatterns.ERROR
            }
            _uiState.update { it.copy(isLoading = false, offersCNCA = calculatedOffers) }
        }
    }

    fun calculateNewCredit() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.newCreditInput.toBD() <= BD_ZERO) {
                 _uiState.update { it.copy(inputError = true) }
                 _effects.emit(CalculatorEffect.PlaySound("wrong"))
                 _effects.emit(CalculatorEffect.TriggerHaptics(HapticPatterns.ERROR)) // HapticPatterns.ERROR
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }
            _uiState.update { it.copy(isLoading = true, offersNew = emptyList()) }
            delay(1500)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value
                val inputVal = state.newCreditInput.toBD()
                val results = mutableListOf<LoanOffer>()
                if (inputVal > BD_ZERO) {
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        val isPremiumVariant = prodKey.endsWith("ESPECIAL") || prodKey.endsWith("VIP")

                        if (isPremiumVariant) {
                            // ESPECIAL y VIP: solo 60 meses, sin retención, sin fallback
                            val step = if (state.isByAmount) BigDecimal("100") else BigDecimal("500")
                            val calc = FinancialEngine.calculate(prodKey, inputVal, 60, state.isByAmount, false, step)
                            if (calc != null) {
                                val (res, _) = calc
                                val minRequired = config.minTermMap[60] ?: config.minGlobal
                                if (res.amount >= minRequired && FinancialEngine.isValidTermAndAmount(prodKey, 60, res.amount)) {
                                    results.add(
                                        LoanOffer(
                                            prodKey, res.amount, res.payment, 60,
                                            mainValue = res.amount,
                                            mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.meta.catTrad,
                                            tipoTasa = prodKey.substringAfterLast(" ")
                                        )
                                    )
                                }
                            }
                        } else {
                            // MODO TRADICIONAL: calcular todos los plazos disponibles
                            config.factorsTrad.keys.forEach { term ->
                                val step = if (state.isByAmount) BigDecimal("100") else BigDecimal("500")
                                val calcTrad = FinancialEngine.calculate(prodKey, inputVal, term, state.isByAmount, false, step)
                                if (calcTrad != null) {
                                    val (res, tipo) = calcTrad
                                    val minRequired = config.minTermMap[term] ?: config.minGlobal
                                    if (res.amount >= minRequired && res.amount >= config.minGlobal && FinancialEngine.isValidTermAndAmount(prodKey, term, res.amount)) {
                                        results.add(LoanOffer(prodKey, res.amount, res.payment, term,
                                            mainValue = res.amount, mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.meta.catTrad, tipoTasa = tipo))
                                    }
                                }
                            }
                        }
                    }
                }
                // Ordenar y marcar ofertas de retención como "mejor opción"
                results.sortedWith(
                    compareByDescending<LoanOffer> { it.amount }.thenBy { it.monthlyPayment }
                ).mapIndexed { i, offer ->
                    offer.copy(isBest = offer.tipoTasa == "RETENCION" || i == 0)
                }
            }
            if (calculatedOffers.isNotEmpty()) {
                _effects.emit(CalculatorEffect.PlaySound("cash"))
                _effects.emit(CalculatorEffect.TriggerHaptics(HapticPatterns.SUCCESS)) // HapticPatterns.SUCCESS
            } else {
                // Sin ofertas disponibles
                _effects.emit(CalculatorEffect.PlaySound("wrong"))
                _effects.emit(CalculatorEffect.TriggerHaptics(longArrayOf(0, 50, 40, 50))) // HapticPatterns.ERROR
            }
            _uiState.update { it.copy(isLoading = false, offersNew = calculatedOffers) }
        }
    }

    fun generateLayout(): String {
        val s = _uiState.value
        val d = s.clientData
        val p = s.userProfile
        val o = s.selectedOfferForLayout ?: return ""
        
        // Determinar tipo de trámite basado en la oferta
        val isNuevo = d.transactionType == "NUEVO"
        val isCNCA = d.transactionType == "CNCA"
        val is2Disp = d.transactionType == "2ª DISP"
        val isLCOM = d.transactionType == "LCOM"
        
        return """
SOLICITUD DE CAPTURA

USUARIO: ${p.username}
CONTRASEÑA: ${p.password}

CONVENIO: ${d.agreement}
TIPO DE ORIGINACIÓN: ${o.product}

DATOS DE CLIENTE
-NOMBRE: ${d.name.uppercase()}
-NSS: ${d.nss}
-CURP: ${d.curp.uppercase()}
-RFC (CON HOMOCLAVE): ${d.rfc.uppercase()}
-CORREO: ${d.email}
-TELEFONO: ${d.phone}
-CLABE Y BANCO: ${d.clabe} - ${d.bank.uppercase()}
-EDAD: ${d.age}
-PENSIÓN: ${d.pensionType}

CUESTIONARIO MEDICO
-ENFERMEDAD: ${d.disease}
-FECHA DE INICIO DE PADECIMIENTO: ${d.diseaseDate}

TIPO DE TRÁMITE
-NUEVO(${if (isNuevo) "X" else " "})	CNCA(${if (isCNCA) "X" else " "})	2ª DISP(${if (is2Disp) "X" else " "})	LCOM(${if (isLCOM) "X" else " "})
-ID (CRÉDITO A LIQUIDAR): ${d.creditId}
-DESCUENTO: ${d.discountAmount}
-NÚM DE DESCUENTOS: ${d.numDiscounts}
-MARCA QUE LIQUIDA: ${d.liquidatingBrand}

DATOS DEL CRÉDITO NUEVO
-MONTO: ${'$'}${o.amount.toCurrency()}
-PLAZO: ${o.term} meses
-DESCUENTO: ${'$'}${o.monthlyPayment.toCurrency()}
-COTIZADOR: ${o.tipoTasa}
-COMISIONABLE: ${d.isCommissionable}

REFERENCIAS
1ª ${d.ref1Name} - ${d.ref1Phone}
2ª ${d.ref2Name} - ${d.ref2Phone}

PROMOTOR (NOMBRE COMPLETO): ${p.name.uppercase()}
SUPERVISOR: ${p.supervisor.uppercase()}

CAT: ${o.cat}
        """.trimIndent()
    }

    fun generateTicketBackground() {
        val o = _uiState.value.selectedOfferForLayout ?: return
        val c = _uiState.value.clientData
        val u = _uiState.value.userProfile
        
        viewModelScope.launch {
            _effects.emit(CalculatorEffect.GenerateTicket(o, c, u.name))
        }
    }
    
    // Insurance Module Functions
    fun updateInsPensionType(type: String) {
        _uiState.update { it.copy(insPensionType = type) }
    }
    
    fun updateInsAge(age: String) {
        _uiState.update { it.copy(insAge = age) }
    }
    
    fun updateInsAmount(amount: String) {
        _uiState.update { it.copy(insAmount = amount) }
    }
    
    fun updateInsTerm(term: Int) {
        _uiState.update { it.copy(insTerm = term) }
    }
    
    fun updateInsProvider(provider: String) {
        _uiState.update { it.copy(insProvider = provider) }
    }
    
    fun toggleInsNewClient(isNew: Boolean) {
        _uiState.update { it.copy(insIsNewClient = isNew) }
    }
    
    fun updateInsAntiquity(v: String) {
        _uiState.update { it.copy(insAntiquity = v) }
    }
    
    fun calculateInsurance() {
        viewModelScope.launch {
            val current = _uiState.value
            val age = current.insAge.toIntOrNull()
            val amount = current.insAmount.toBD()
            
            // Validación de inputs
            if (age == null || age <= 0 || amount <= BD_ZERO) {
                _uiState.update { 
                    it.copy(
                        inputError = true,
                        insResultStatus = "Error: Ingrese edad y monto válidos",
                        insResultColor = ErrorRed
                    ) 
                }
                _effects.emit(CalculatorEffect.PlaySound("wrong"))
                _effects.emit(CalculatorEffect.TriggerHaptics(longArrayOf(0, 50, 40, 50))) // HapticPatterns.ERROR
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }
            
            // --- VALIDACIÓN DE MONTO MÍNIMO ---
            val configProducto = FinancialConfig.PRODUCTS[current.insProvider]
            val minRequerido = configProducto?.minTermMap?.get(current.insTerm) 
                               ?: configProducto?.minGlobal 
                               ?: BD_ZERO

            if (amount < minRequerido) {
                _uiState.update { 
                    it.copy(
                        inputError = true,
                        insResultStatus = "RECHAZADO: Monto menor al mínimo.\nRequiere $${minRequerido.toCurrency()} para ${current.insTerm} meses.",
                        insResultColor = ErrorRed,
                        insCost1Year = BD_ZERO,
                        insCostTotal = BD_ZERO
                    ) 
                }
                _effects.emit(CalculatorEffect.PlaySound("wrong"))
                _effects.emit(CalculatorEffect.TriggerHaptics(longArrayOf(0, 50, 40, 50))) // HapticPatterns.ERROR
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            delay(800)
            
            val result = withContext(Dispatchers.Default) {
                val state = _uiState.value
                val pensionTypeStr = state.insPensionType
                val term = state.insTerm
                val provider = state.insProvider
                val isNewClient = state.insIsNewClient
                val antiquity = state.insAntiquity.toIntOrNull() ?: 0
                
                val tipoPension = when(pensionTypeStr) {
                    "Invalidez" -> TipoPension.INVALIDEZ
                    "Vejez" -> TipoPension.VEJEZ
                    else -> TipoPension.OTRO
                }
                
                val resultadoRegla = NormativaIMSS.calcularReglaSeguro(
                    edad = age,
                    plazoMeses = term,
                    tipoPension = tipoPension,
                    esNuevo = isNewClient,
                    antiguedadMeses = antiquity
                )
                
                var status = "NO APLICA SEGURO"
                var color = SuccessGreen
                var applyOneYear = false
                var applyTotal = false
                
                when(resultadoRegla) {
                    ResultadoSeguro.RECHAZO -> {
                        status = "RECHAZADO: Edad Total Excede Límite (80 años)"
                        color = ErrorRed
                    }
                    ResultadoSeguro.UN_ANIO -> {
                        status = "APLICA 1 AÑO DE SEGURO"
                        color = WarningOrange
                        applyOneYear = true
                    }
                    ResultadoSeguro.PLAZO_COMPLETO -> {
                        status = "APLICA SEGURO PLAZO COMPLETO"
                        color = JuicyOrange
                        applyTotal = true
                    }
                    ResultadoSeguro.NINGUNO -> {
                        status = "NO APLICA SEGURO (Aprobado)"
                        color = SuccessGreen
                    }
                }
                
                var cost1Year = BD_ZERO
                var costTotal = BD_ZERO
                var monthlyPayment = BD_ZERO
                
                if (applyOneYear || applyTotal) {
                    val baseProvider = provider.substringBefore(" ")
                    val factors = FinancialConfig.INSURANCE_FACTORS[baseProvider]
                    
                    val termKey = if (provider.startsWith("OPCIPRES") && term == 60 && (provider.endsWith("ESPECIAL") || provider.endsWith("VIP"))) {
                        999
                    } else {
                        term
                    }
                    
                    val factorPair = factors?.get(termKey)
                    
                    if (factorPair != null) {
                        val (factor1Year, factorTotal) = factorPair
                        
                        val calculated1Year = amount.multiply(factor1Year, MathContext(20, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                        val calculatedTotal = amount.multiply(factorTotal, MathContext(20, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                        
                        if (applyOneYear) {
                            cost1Year = calculated1Year
                            status += "\nCosto: $${cost1Year.toCurrency()}"
                        } else if (applyTotal) {
                            costTotal = calculatedTotal
                            status += "\nCosto Total: $${costTotal.toCurrency()}"
                        }
                        
                        val totalAmountWithIns = amount.add(if(applyOneYear) cost1Year else costTotal)
                        
                        val paymentFactor = FinancialConfig.PRODUCTS[provider]?.factorsTrad?.get(term)
                        if (paymentFactor != null) {
                            monthlyPayment = totalAmountWithIns.multiply(paymentFactor)
                                .divide(BigDecimal(1000), 2, RoundingMode.HALF_UP)
                        }
                    } else {
                        status = "ERROR: Sin factor para $provider ($term m)"
                        color = ErrorRed
                    }
                }
                
                Triple(status, color, Triple(cost1Year, costTotal, monthlyPayment))
            }
            
            val status = result.first
            val color = result.second
            val costResult = result.third
            val cost1Year = costResult.first
            val costTotal = costResult.second
            val monthlyPayment = costResult.third
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    insResultStatus = status,
                    insResultColor = color,
                    insCost1Year = cost1Year,
                    insCostTotal = costTotal,
                    insMonthlyPayment = monthlyPayment
                ) 
            }
            
            _effects.emit(CalculatorEffect.PlaySound("cash"))
            _effects.emit(CalculatorEffect.TriggerHaptics(longArrayOf(0, 10, 50, 20))) // HapticPatterns.SUCCESS
        }
    }
    
    fun clearInsuranceData() {
        _uiState.update { 
            it.copy(
                insAge = "",
                insAmount = "",
                insResultStatus = "",
                insResultColor = Color.Transparent,
                insCost1Year = BD_ZERO,
                insCostTotal = BD_ZERO
            ) 
        }
    }
    
    // =================================================================================================
    // HISTORY LOGIC
    // =================================================================================================
    fun saveToHistory(type: String) {
        val state = _uiState.value
        if (state.debts.isEmpty() && state.capacityCNCA.isBlank()) return
        
        val draft = CalculationDraft(
            debts = state.debts.map { it.copy() },
            capacity = state.capacityCNCA,
            type = type
        )
        val newHistory = (listOf(draft) + state.history).take(10)
        _uiState.update { it.copy(history = newHistory) }
    }

    fun deleteDraft(draft: CalculationDraft) {
        _uiState.update { 
            it.copy(history = it.history - draft) 
        }
    }

    fun clearHistory() {
        _uiState.update { 
            it.copy(history = emptyList()) 
        }
    }
    
    fun restoreHistory(draft: CalculationDraft) {
        _uiState.update { 
            it.copy(
                debts = draft.debts,
                capacityCNCA = draft.capacity,
                showHistory = false,
                offersCNCA = emptyList(),
                selectedTab = if(draft.type == "Refinanciamiento") 0 else 1
            )
        }
    }
    
    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    // =================================================================================================
    // TICKET SHARING LOGIC
    // =================================================================================================
    fun togglePremiumOffers() {
        _uiState.update { it.copy(showPremiumOffers = !it.showPremiumOffers) }
    }

    fun prepareTicketSharing(context: Context, offer: LoanOffer) {
        val state = _uiState.value
        val debts = state.debts
        val totalCurrentPayment = debts.sumOf { it.payment.toBD() }
        
        val savings = if (totalCurrentPayment > BigDecimal.ZERO && state.selectedTab == 0) {
            val s = totalCurrentPayment.subtract(offer.monthlyPayment).max(BigDecimal.ZERO)
            "$${s.toCurrency()}"
        } else {
             "N/A"
        }
        
        val prevPaymentStr = if (totalCurrentPayment > BigDecimal.ZERO && state.selectedTab == 0) {
            "${debts.size} Deudas"
        } else {
            "Crédito Nuevo"
        }

        val data = TicketData(
            clientName = state.clientData.name.ifBlank { "Cliente Preferente" },
            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            product = offer.product,
            amount = "$${offer.mainValue.toCurrency()}",
            payment = "$${offer.monthlyPayment.toCurrency()}",
            term = offer.term,
            previousPayment = prevPaymentStr,
            savings = savings
        )
        
        _uiState.update { it.copy(showTicketPreview = true, activeTicketData = data) }
    }
    
    fun dismissTicketPreview() {
        _uiState.update { it.copy(showTicketPreview = false, activeTicketData = null) }
    }
    
    // =================================================================================================
    // NEW FEATURES FUNCTIONS
    // =================================================================================================
    
    fun toggleOffersComparator(show: Boolean) {
        _uiState.update { it.copy(showOffersComparator = show) }
    }
    
    fun showAmortizationFor(offer: LoanOffer) {
        val table = calculateAmortizationTable(
            amount = offer.amount,
            term = offer.term,
            fixedPayment = offer.monthlyPayment
        )
        
        _uiState.update {
            it.copy(
                showAmortizationSimulator = true,
                selectedOfferForAmortization = offer,
                amortizationTable = table
            )
        }
    }
    
    fun dismissAmortizationSimulator() {
        _uiState.update {
            it.copy(
                showAmortizationSimulator = false,
                selectedOfferForAmortization = null,
                amortizationTable = emptyList()
            )
        }
    }
}

class CalculatorViewModelFactory(private val repository: PreferenceRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
