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
import com.example.consucalcu.model.TipoPension
import com.example.consucalcu.model.UserProfile
import com.example.consucalcu.model.InsuranceAmortizationRow
import com.example.consucalcu.utils.*
import com.example.consucalcu.ui.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.MathContext

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
    val creditAgreementId: String = FinancialConfig.AGREEMENT_IMSS,
    val isByAmount: Boolean = false,
    val offersNew: List<LoanOffer> = emptyList(),
    val hasQuotedNewCredit: Boolean = false,
    val showClientForm: Boolean = false,
    val showConfigDialog: Boolean = false,
    val showSupportModal: Boolean = false,
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
    val insProvider: String = "CSB",
    val insIsNewClient: Boolean = false,
    val insAntiquity: String = "", // Added antiquity field
    val insResultStatus: String = "",
    val insResultColor: Color = Color.Transparent,
    val insCost1Year: BigDecimal = BD_ZERO,
    val insCostTotal: BigDecimal = BD_ZERO,
    val insMonthlyPayment: BigDecimal = BD_ZERO, // Added PMT field
    val insGender: String = "Hombre",
    val insClientType: String = "Nuevo",
    val insDictamen: String = "No Aplica",
    val insPaymentsSustained: String = "",
    val insAmortizationTable: List<InsuranceAmortizationRow> = emptyList(),
    val insDictamenJson: String = "",

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
    val isHapticEnabled: Boolean = true,

    // Tasa y CAT en las tarjetas de ofertas (el promotor decide si se ven)
    val showRatesOnCards: Boolean = false
)

// =================================================================================================
// 11. VIEWMODEL LOGIC
// =================================================================================================
// Pausa breve para que la carga se perciba; el cálculo en sí es instantáneo.
private const val QUOTE_FEEDBACK_MS = 350L

class CalculatorViewModel(private val repository: PreferenceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()
    private var saveJob: Job? = null
    private var calculationJob: Job? = null

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
        val creditAgreementId = repository.getCreditAgreementId()
        val isSound = repository.getIsSoundEnabled()
        val isHaptic = repository.getIsHapticEnabled()

        _uiState.update {
            it.copy(
                userProfile = p,
                debts = debtsList,
                clientData = client,
                capacityCNCA = cap,
                creditAgreementId = FinancialConfig.agreementFor(creditAgreementId).id,

                insAge = repository.getInsuranceAge(),
                insAmount = repository.getInsuranceAmount(),
                insTerm = repository.getInsuranceTerm(),
                insProvider = repository.getInsuranceProvider(),
                insPensionType = repository.getInsurancePensionType(),
                insIsNewClient = repository.getInsuranceIsNew(),
                insAntiquity = repository.getInsuranceAntiquity(),

                isSoundEnabled = isSound,
                isHapticEnabled = isHaptic,
                showRatesOnCards = repository.getShowRatesOnCards(),

                mandatoryConfigNeeded = p.name.isBlank() || p.username.isBlank() || p.password.isBlank() || p.supervisor.isBlank()
            )
        }
    }

    fun saveData() {
        saveJob?.cancel()
        val snapshot = _uiState.value
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            repository.saveData(
                debts = snapshot.debts,
                client = snapshot.clientData,
                capacityCNCA = snapshot.capacityCNCA,
                insAge = snapshot.insAge,
                insAmount = snapshot.insAmount,
                insTerm = snapshot.insTerm,
                insProvider = snapshot.insProvider,
                insPensionType = snapshot.insPensionType,
                insIsNew = snapshot.insIsNewClient,
                insAntiquity = snapshot.insAntiquity,
                creditAgreementId = snapshot.creditAgreementId,
                isSoundEnabled = snapshot.isSoundEnabled,
                isHapticEnabled = snapshot.isHapticEnabled,
                showRatesOnCards = snapshot.showRatesOnCards
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

    fun toggleShowRatesOnCards(enabled: Boolean) {
        _uiState.update { it.copy(showRatesOnCards = enabled) }
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

    fun selectCreditAgreement(id: String) {
        val agreement = FinancialConfig.agreementFor(id)
        _uiState.update {
            it.copy(
                creditAgreementId = agreement.id,
                offersNew = emptyList(),
                hasQuotedNewCredit = false,
                showOffersComparator = false,
                showOfferPreview = false,
                selectedOfferForLayout = null,
                inputError = false
            )
        }
    }

    fun toggleCalculationMode(byAmount: Boolean) {
        // El mismo número significa algo distinto en cada modo (monto o
        // capacidad): al cambiar de modo se limpian captura y resultados.
        _uiState.update {
            if (it.isByAmount == byAmount) it
            else it.copy(
                isByAmount = byAmount,
                newCreditInput = "",
                offersNew = emptyList(),
                hasQuotedNewCredit = false
            )
        }
    }

    fun clearNewCreditData() {
        _uiState.update { it.copy(newCreditInput = "", offersNew = emptyList(), hasQuotedNewCredit = false) }
    }

    fun toggleConfigDialog(show: Boolean) {
        _uiState.update { it.copy(showConfigDialog = show) }
    }

    fun toggleRetention(enabled: Boolean) {
        _uiState.update { it.copy(useRetention = enabled) }
    }

    fun updateClientData(data: ClientData) {
        // Mejorar UX: Autocompletar RFC y extraer fecha si se ingresa CURP válida
        var updatedData = data
        if (data.curp.length == 18 && GeneralUtils.isValidCURP(data.curp)) {
            val upperCurp = data.curp.uppercase()
            val rfcPrefix = upperCurp.take(10)
            if (data.rfc.isBlank() || data.rfc.startsWith(rfcPrefix.take(4))) {
                updatedData = updatedData.copy(rfc = rfcPrefix)
            }
        }
        _uiState.update { it.copy(clientData = updatedData) }
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

    fun dismissSupportModal() {
        _uiState.update { it.copy(showSupportModal = false) }
    }

    private fun recordCalculationForSupport() {
        val calculationCount = repository.incrementSupportCalculationCount()
        if (calculationCount >= 10) showSupportInvitation()
    }

    fun recordPdfGeneratedForSupport() {
        val pdfCount = repository.incrementSupportPdfCount()
        if (pdfCount >= 5) showSupportInvitation()
    }

    private fun showSupportInvitation() {
        repository.resetSupportCounters()
        _uiState.update { it.copy(showSupportModal = true) }
    }

    fun calculateRefinance() {
        saveToHistory("Refinanciamiento")
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            val current = _uiState.value
            val totalDebts = current.debts.sumOf { it.balance.toBD() }
            val cap = current.capacityCNCA.toBD()

            if (totalDebts <= BD_ZERO && cap == BD_ZERO) {
                 _uiState.update { it.copy(isLoading = false, inputError = true) }
                 _effects.emit(CalculatorEffect.PlaySound("ui_error"))
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }

            _uiState.update { it.copy(isLoading = true, offersCNCA = emptyList()) }
            delay(QUOTE_FEEDBACK_MS)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val capVal = current.capacityCNCA.toBD()

                val debtBalance = current.debts.sumOf { it.balance.toBD() }
                val debts = current.debts.filter { it.balance.toBD() > BD_ZERO }
                val results = mutableListOf<LoanOffer>()

                val totalPagosDeudas = current.debts.sumOf { it.payment.toBD() }
                val capacidadTotal = totalPagosDeudas.add(capVal)

                if (capacidadTotal > BD_ZERO && debts.isNotEmpty()) {
                    // La nueva normativa evalúa cada plazo disponible del cotizador.
                    // El aumento de 6 meses se valida después contra el crédito de mayor saldo.
                    val cncaProducts = FinancialConfig.PRODUCTS.filterKeys {
                        it in FinancialConfig.IMSS_PRODUCT_KEYS
                    }

                    cncaProducts.forEach { (prodKey, config) ->
                        config.factorsTrad.keys.sorted().forEach { term ->
                            val calcData = FinancialEngine.calculate(
                                prodKey,
                                capacidadTotal,
                                term,
                                false,
                                false,
                                FinancialConfig.agreementFor(FinancialConfig.AGREEMENT_IMSS).amountStep
                            )
                            if (calcData != null) {
                                val (res, _) = calcData
                                val offer = LoanOffer(
                                    prodKey,
                                    res.amount,
                                    res.payment,
                                    term,
                                    cat = config.catTradForTerm(term),
                                    tipoTasa = config.tasaRefLabel(term)
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
                // Ordenar por remanente conservando la variante exacta del nuevo cotizador.
                results.sortedByDescending { it.mainValue }
                    .mapIndexed { i, offer ->
                        offer.copy(isBest = i == 0)
                    }
            }
            if (calculatedOffers.isNotEmpty()) {
                _effects.emit(CalculatorEffect.PlaySound("ui_success"))
            } else {
                // Sin ofertas disponibles
                _effects.emit(CalculatorEffect.PlaySound("ui_error"))
            }
            _uiState.update { it.copy(isLoading = false, offersCNCA = calculatedOffers) }
            recordCalculationForSupport()
        }
    }

    fun calculateNewCredit() {
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            val current = _uiState.value
            val agreement = FinancialConfig.agreementFor(current.creditAgreementId)
            val inputValue = current.newCreditInput.toBD()
            val invalidRequestedAmount = current.isByAmount &&
                !agreement.acceptsRequestedAmount(inputValue)

            if (inputValue <= BD_ZERO || invalidRequestedAmount) {
                 _uiState.update { it.copy(isLoading = false, inputError = true) }
                 _effects.emit(CalculatorEffect.PlaySound("ui_error"))
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }
            _uiState.update { it.copy(isLoading = true, offersNew = emptyList()) }
            delay(QUOTE_FEEDBACK_MS)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val inputVal = current.newCreditInput.toBD()
                val results = mutableListOf<LoanOffer>()
                if (inputVal > BD_ZERO) {
                    val selectedAgreement = FinancialConfig.agreementFor(current.creditAgreementId)
                    selectedAgreement.productKeys.forEach { prodKey ->
                        val config = FinancialConfig.PRODUCTS[prodKey] ?: return@forEach
                        val isPremiumVariant = FinancialConfig.isPremiumProduct(prodKey)

                        if (isPremiumVariant) {
                            // MAX, ESP y VIP: solo 60 meses, sin fallback.
                            val calc = FinancialEngine.calculate(prodKey, inputVal, 60, current.isByAmount, false, selectedAgreement.amountStep)
                            if (calc != null) {
                                val (res, _) = calc
                                val minRequired = config.minTermMap[60] ?: config.minGlobal
                                if (res.amount >= minRequired && FinancialEngine.isValidTermAndAmount(prodKey, 60, res.amount)) {
                                    results.add(
                                        LoanOffer(
                                            prodKey, res.amount, res.payment, 60,
                                            mainValue = res.amount,
                                            mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.catTradForTerm(60),
                                            tipoTasa = config.tasaRefLabel(60),
                                            agreementId = selectedAgreement.id,
                                            agreementLabel = selectedAgreement.label,
                                            paymentFrequency = selectedAgreement.paymentFrequency
                                        )
                                    )
                                }
                            }
                        } else {
                            // MODO TRADICIONAL: calcular todos los plazos disponibles
                            config.factorsTrad.keys.sorted().forEach { term ->
                                val calcTrad = FinancialEngine.calculate(prodKey, inputVal, term, current.isByAmount, false, selectedAgreement.amountStep)
                                if (calcTrad != null) {
                                    val (res, _) = calcTrad
                                    val minRequired = config.minTermMap[term] ?: config.minGlobal
                                    if (res.amount >= minRequired &&
                                        res.amount >= config.minGlobal &&
                                        res.amount <= selectedAgreement.maxAmount &&
                                        FinancialEngine.isValidTermAndAmount(prodKey, term, res.amount)
                                    ) {
                                        val displayProduct = if (selectedAgreement.id == FinancialConfig.AGREEMENT_IMSS_BIENESTAR) {
                                            "CONSUBANCO"
                                        } else {
                                            prodKey
                                        }
                                        results.add(LoanOffer(displayProduct, res.amount, res.payment, term,
                                            mainValue = res.amount, mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.catTradForTerm(term),
                                            tipoTasa = config.tasaRefLabel(term),
                                            agreementId = selectedAgreement.id,
                                            agreementLabel = selectedAgreement.label,
                                            paymentFrequency = selectedAgreement.paymentFrequency))
                                    }
                                }
                            }
                        }
                    }
                }
                // Crédito nuevo no usa retención: la mejor opción es la primera tras ordenar.
                val sorted = results.sortedWith(
                    compareByDescending<LoanOffer> { it.amount }.thenBy { it.monthlyPayment }
                ).mapIndexed { i, offer ->
                    offer.copy(isBest = i == 0)
                }
                sorted
            }
            if (calculatedOffers.isNotEmpty()) {
                _effects.emit(CalculatorEffect.PlaySound("ui_success"))
            } else {
                // Sin ofertas disponibles
                _effects.emit(CalculatorEffect.PlaySound("ui_error"))
            }
            _uiState.update { it.copy(isLoading = false, offersNew = calculatedOffers, hasQuotedNewCredit = true) }
            recordCalculationForSupport()
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
-MONTO: $${o.amount.toCurrency()}
-PLAZO: ${o.termDisplay}
-${o.paymentLabel}: ${'$'}${o.monthlyPayment.toCurrency()}
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
        val validTerms = FinancialConfig.PRODUCTS[provider]?.factorsTrad?.keys.orEmpty()
        _uiState.update {
            it.copy(
                insProvider = provider,
                insTerm = it.insTerm.takeIf(validTerms::contains)
                    ?: 60.takeIf(validTerms::contains)
                    ?: validTerms.minOrNull()
                    ?: it.insTerm
            )
        }
    }



    fun updateInsAntiquity(v: String) {
        _uiState.update { it.copy(insAntiquity = v) }
    }

    fun updateInsGender(gender: String) {
        _uiState.update { it.copy(insGender = gender) }
    }

    fun updateInsClientType(clientType: String) {
        _uiState.update { it.copy(insClientType = clientType) }
    }

    fun updateInsDictamen(dictamen: String) {
        _uiState.update { it.copy(insDictamen = dictamen) }
    }

    fun updateInsPaymentsSustained(payments: String) {
        _uiState.update { it.copy(insPaymentsSustained = payments) }
    }

    fun calculateInsurance() {
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            val current = _uiState.value
            val age = current.insAge.toIntOrNull()
            val amount = current.insAmount.toBD()

            // Validación de inputs
            if (age == null || age <= 0 || amount <= BD_ZERO) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inputError = true,
                        insResultStatus = "Error: Ingrese edad y monto válidos",
                        insResultColor = ErrorRed
                    )
                }
                _effects.emit(CalculatorEffect.PlaySound("ui_error"))
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }

            val productKey = FinancialEngine.resolveProductKey(current.insProvider)
            if (productKey == null || !FinancialEngine.hasValidFactor(productKey, current.insTerm, current.useRetention)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inputError = true,
                        insResultStatus = "RECHAZADO: El producto no tiene un factor de cálculo para ${current.insTerm} meses.",
                        insResultColor = ErrorRed,
                        insCost1Year = BD_ZERO,
                        insCostTotal = BD_ZERO,
                        insMonthlyPayment = BD_ZERO,
                        insAmortizationTable = emptyList(),
                        insDictamenJson = ""
                    )
                }
                _effects.emit(CalculatorEffect.PlaySound("ui_error"))
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }

            // --- VALIDACIÓN DE MONTO MÍNIMO ---
            val configProducto = FinancialConfig.PRODUCTS.getValue(productKey)
            val minRequerido = configProducto.minTermMap[current.insTerm] ?: configProducto.minGlobal

            if (amount < minRequerido) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inputError = true,
                        insResultStatus = "RECHAZADO: Monto menor al mínimo.\nRequiere $${minRequerido.toCurrency()} para ${current.insTerm} meses.",
                        insResultColor = ErrorRed,
                        insCost1Year = BD_ZERO,
                        insCostTotal = BD_ZERO
                    )
                }
                _effects.emit(CalculatorEffect.PlaySound("ui_error"))
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            delay(QUOTE_FEEDBACK_MS)

            val result = withContext(Dispatchers.Default) {
                val term = current.insTerm

                val tipoPension = when(current.insPensionType) {
                    "Jubilación" -> TipoPension.JUBILACION
                    "Vejez" -> TipoPension.VEJEZ
                    "Cesantía" -> TipoPension.CESANTIA
                    "Viudez" -> TipoPension.VIUDEZ
                    "Invalidez" -> TipoPension.INVALIDEZ
                    "Orfandad" -> TipoPension.ORFANDAD
                    "Riesgo de Trabajo" -> TipoPension.RIESGO_TRABAJO
                    "Ascendencia" -> TipoPension.ASCENDENCIA
                    else -> TipoPension.OTRA
                }

                val dictamenResult = NormativaIMSS.evaluarDictamenSeguro(
                    producto = productKey,
                    montoSolicitado = amount.toDouble(),
                    plazo = term,
                    sexo = current.insGender,
                    edad = age,
                    tipoPension = tipoPension,
                    tipoCliente = current.insClientType,
                    dictamen = current.insDictamen,
                    pagosSostenidos = current.insPaymentsSustained.toIntOrNull() ?: 0,
                    antiguedadMeses = current.insAntiquity.toIntOrNull() ?: 0
                )

                if (!dictamenResult.aprobado) {
                    val jsonStr = """
                        {
                          "aprobado": false,
                          "producto": "${dictamenResult.producto}",
                          "plazo": ${dictamenResult.plazo},
                          "tasaMensual": ${dictamenResult.tasaMensual},
                          "factorSeguro": ${dictamenResult.factorSeguro},
                          "tipoSeguro": "",
                          "montoSeguro": 0.00,
                          "motivo": "${dictamenResult.motivo}"
                        }
                    """.trimIndent()
                    return@withContext InsCalcResult(
                        approved = false,
                        status = "RECHAZADO\n${dictamenResult.motivo}",
                        color = ErrorRed,
                        cost1Year = BD_ZERO,
                        costTotal = BD_ZERO,
                        monthlyPayment = BD_ZERO,
                        table = emptyList(),
                        jsonStr = jsonStr
                    )
                }

                // Si está aprobado: calcular amortización
                // Never synthesize a zero payment when a factor is absent. The selected product
                // and term were checked above, but calculate remains the source of truth.
                val calcRes = FinancialEngine.calculate(
                    productKey,
                    amount,
                    term,
                    true,
                    current.useRetention,
                    BD_ZERO
                ) ?: return@withContext InsCalcResult(
                    approved = false,
                    status = "RECHAZADO\nNo existe un factor válido para el producto y plazo seleccionados.",
                    color = ErrorRed,
                    cost1Year = BD_ZERO,
                    costTotal = BD_ZERO,
                    monthlyPayment = BD_ZERO,
                    table = emptyList(),
                    jsonStr = ""
                )
                val pagoMensual = calcRes.first.payment

                val schedule = NormativaIMSS.calcularTablaSeguro(productKey, amount, term)
                val rows = schedule.rows
                val cost1Year = if (dictamenResult.tipoSeguro == "PRIMER_ANIO") schedule.firstYearCost else BD_ZERO
                val costTotal = if (dictamenResult.tipoSeguro == "PLAZO_COMPLETO") schedule.fullTermCost else BD_ZERO

                val seguroText = if (dictamenResult.tipoSeguro == "PRIMER_ANIO") "1 AÑO" else "PLAZO COMPLETO"
                val displayTotal = if (dictamenResult.tipoSeguro == "PRIMER_ANIO") cost1Year else costTotal
                val statusText = "APROBADO\nSeguro: $${displayTotal.toCurrency()} ($seguroText)\nTasa: ${dictamenResult.tasaMensual}%\nFactor: ${dictamenResult.factorSeguro}\n\n${dictamenResult.motivo}"
                val color = if (dictamenResult.tipoSeguro == "PRIMER_ANIO") WarningOrange else JuicyOrange

                val formattedMontoSeguro = displayTotal.toDouble()
                val jsonStr = """
                    {
                      "aprobado": true,
                      "producto": "${dictamenResult.producto}",
                      "plazo": ${dictamenResult.plazo},
                      "tasaMensual": ${dictamenResult.tasaMensual},
                      "factorSeguro": ${dictamenResult.factorSeguro},
                      "tipoSeguro": "${dictamenResult.tipoSeguro}",
                      "montoSeguro": $formattedMontoSeguro,
                      "motivo": "${dictamenResult.motivo}"
                    }
                """.trimIndent()

                InsCalcResult(
                    approved = true,
                    status = statusText,
                    color = color,
                    cost1Year = cost1Year,
                    costTotal = costTotal,
                    monthlyPayment = pagoMensual,
                    table = rows,
                    jsonStr = jsonStr
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    insResultStatus = result.status,
                    insResultColor = result.color,
                    insCost1Year = result.cost1Year,
                    insCostTotal = result.costTotal,
                    insMonthlyPayment = result.monthlyPayment,
                    insAmortizationTable = result.table,
                    insDictamenJson = result.jsonStr
                )
            }

            _effects.emit(
                CalculatorEffect.PlaySound(if (result.approved) "ui_success" else "ui_error")
            )
            recordCalculationForSupport()
        }
    }

    fun copyDictamenToClipboard(context: Context) {
        val json = _uiState.value.insDictamenJson
        if (json.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Dictamen Seguro", json)
            clipboard.setPrimaryClip(clip)
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
                insCostTotal = BD_ZERO,
                insMonthlyPayment = BD_ZERO,
                insAmortizationTable = emptyList(),
                insDictamenJson = ""
            )
        }
    }

    private data class InsCalcResult(
        val approved: Boolean,
        val status: String,
        val color: Color,
        val cost1Year: BigDecimal,
        val costTotal: BigDecimal,
        val monthlyPayment: BigDecimal,
        val table: List<InsuranceAmortizationRow>,
        val jsonStr: String
    )

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
            savings = savings,
            paymentLabel = offer.paymentLabel,
            termDisplay = offer.termDisplay
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
            term = offer.periodCount,
            fixedPayment = offer.monthlyPayment,
            periodsPerYear = offer.periodsPerYear
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
