package com.example.consucalcu.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.example.consucalcu.model.ClientData
import com.example.consucalcu.model.Debt
import com.example.consucalcu.model.UserProfile
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.security.KeyStore
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PreferenceRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadProfile(): UserProfile {
        return UserProfile(
            prefs.getString("name", "") ?: "",
            prefs.getString("username", "") ?: "",
            loadPassword(),
            prefs.getString("supervisor", "") ?: ""
        )
    }

    fun saveProfile(p: UserProfile) {
        val encryptedPassword = p.password.takeIf { it.isNotBlank() }?.let(::encryptPassword)
        prefs.edit {
            putString("name", p.name)
            putString("username", p.username)
            putString("supervisor", p.supervisor)
            // Remove the legacy clear-text value even if the keystore becomes unavailable.
            remove(LEGACY_PASSWORD_KEY)
            if (encryptedPassword == null) remove(ENCRYPTED_PASSWORD_KEY)
            else putString(ENCRYPTED_PASSWORD_KEY, encryptedPassword)
        }
    }

    fun loadDebts(): List<Debt> {
        val json = loadProtectedJson(ENCRYPTED_DEBTS_KEY, LEGACY_DEBTS_KEY)
        if (json != null) {
            val parsed = PreferenceJsonCodec.decodeDebts(gson, json)
            if (parsed != null) return parsed.map { debt ->
                debt.copy(product = canonicalDebtProduct(debt.product))
            }
            prefs.edit { remove(ENCRYPTED_DEBTS_KEY) }
        }
        return listOf(Debt(id = System.nanoTime()))
    }

    fun loadClient(): ClientData {
        val json = loadProtectedJson(ENCRYPTED_CLIENT_KEY, LEGACY_CLIENT_KEY)
        if (json != null) {
            val parsed = PreferenceJsonCodec.decodeClient(gson, json)
            if (parsed != null) return parsed
            prefs.edit { remove(ENCRYPTED_CLIENT_KEY) }
        }
        return ClientData()
    }

    fun getCapacityCNCA(): String = prefs.getString("capCNCA", "") ?: ""

    private fun canonicalDebtProduct(product: String): String = when (product.trim().uppercase(Locale.ROOT)) {
        "CSB", "CSB ESP", "CSB VIP", "CONSUBANCO", "CONSUBANCO TRADICIONAL",
        "CONSUBANCO ESPECIAL", "CONSUBANCO VIP" -> "CSB"
        "OPC", "OPC MAX", "OPC ESP", "CONSUPAGO", "CONSUPAGO TRADICIONAL",
        "CONSUPAGO ESPECIAL", "CSP", "CSP ESP", "OPCIPRES", "OPCIPRES TRADICIONAL",
        "OPCIPRES ESPECIAL", "MASNOMINA", "MÁS NÓMINA", "MSN" -> "OPC"
        else -> product
    }
    fun getCreditAgreementId(): String {
        val stored = prefs.getString("creditAgreementId", FinancialConfig.AGREEMENT_IMSS) ?: FinancialConfig.AGREEMENT_IMSS
        return FinancialConfig.agreementFor(stored).id
    }
    fun getIsSoundEnabled(): Boolean = prefs.getBoolean("isSoundEnabled", true)
    fun getIsHapticEnabled(): Boolean = prefs.getBoolean("isHapticEnabled", true)

    @Synchronized
    fun incrementSupportCalculationCount(): Int {
        val next = prefs.getInt("supportCalculationCount", 0) + 1
        prefs.edit { putInt("supportCalculationCount", next) }
        return next
    }

    @Synchronized
    fun incrementSupportPdfCount(): Int {
        val next = prefs.getInt("supportPdfCount", 0) + 1
        prefs.edit { putInt("supportPdfCount", next) }
        return next
    }

    fun resetSupportCounters() {
        prefs.edit {
            putInt("supportCalculationCount", 0)
            putInt("supportPdfCount", 0)
        }
    }

    fun getInsuranceAge(): String = prefs.getString("insAge", "") ?: ""
    fun getInsuranceAmount(): String = prefs.getString("insAmount", "") ?: ""
    fun getInsuranceTerm(): Int = prefs.getInt("insTerm", 60)
    fun getInsuranceProvider(): String {
        val stored = prefs.getString("insProvider", DEFAULT_INSURANCE_PROVIDER)
            ?: DEFAULT_INSURANCE_PROVIDER
        // v1 stored bare brand names while the calculator requires a concrete product table.
        return when (stored.trim().uppercase(Locale.ROOT)) {
            "CONSUBANCO", "CONSUBANCO TRADICIONAL" -> "CSB"
            "CONSUBANCO ESPECIAL" -> "CSB ESP"
            "CONSUBANCO VIP" -> "CSB VIP"
            "CONSUPAGO ESPECIAL" -> "OPC MAX"
            "OPCIPRES ESPECIAL" -> "OPC ESP"
            "CONSUPAGO", "CONSUPAGO TRADICIONAL", "OPCIPRES", "OPCIPRES TRADICIONAL",
            "MASNOMINA", "MÁS NÓMINA", "MSN" -> "OPC"
            else -> stored
        }
    }
    fun getInsurancePensionType(): String = prefs.getString("insPensionType", "Invalidez") ?: "Invalidez"
    fun getInsuranceIsNew(): Boolean = prefs.getBoolean("insIsNew", false)
    fun getInsuranceAntiquity(): String = prefs.getString("insAntiquity", "") ?: ""

    fun saveData(
        debts: List<Debt>,
        client: ClientData,
        capacityCNCA: String,
        insAge: String,
        insAmount: String,
        insTerm: Int,
        insProvider: String,
        insPensionType: String,
        insIsNew: Boolean,
        insAntiquity: String,
        creditAgreementId: String,
        isSoundEnabled: Boolean,
        isHapticEnabled: Boolean
    ) {
        val encryptedDebts = encryptSecret(gson.toJson(debts))
        val encryptedClient = encryptSecret(gson.toJson(client))
        prefs.edit {
            putInt(SCHEMA_VERSION_KEY, CURRENT_SCHEMA_VERSION)
            remove(LEGACY_DEBTS_KEY)
            remove(LEGACY_CLIENT_KEY)
            if (encryptedDebts == null) remove(ENCRYPTED_DEBTS_KEY)
            else putString(ENCRYPTED_DEBTS_KEY, encryptedDebts)
            if (encryptedClient == null) remove(ENCRYPTED_CLIENT_KEY)
            else putString(ENCRYPTED_CLIENT_KEY, encryptedClient)
            putString("capCNCA", capacityCNCA)
            putString("insAge", insAge)
            putString("insAmount", insAmount)
            putInt("insTerm", insTerm)
            putString("insProvider", insProvider)
            putString("insPensionType", insPensionType)
            putBoolean("insIsNew", insIsNew)
            putString("insAntiquity", insAntiquity)
            putString("creditAgreementId", creditAgreementId)
            putBoolean("isSoundEnabled", isSoundEnabled)
            putBoolean("isHapticEnabled", isHapticEnabled)
        }
    }

    private fun loadPassword(): String {
        val encrypted = prefs.getString(ENCRYPTED_PASSWORD_KEY, null)
        if (encrypted != null) {
            val decrypted = decryptPassword(encrypted)
            if (decrypted != null) return decrypted
            // A restored or invalidated keystore key cannot decrypt the value. Fail closed.
            prefs.edit { remove(ENCRYPTED_PASSWORD_KEY) }
            return ""
        }

        // One-time migration from the old clear-text preference.
        val legacy = prefs.getString(LEGACY_PASSWORD_KEY, "").orEmpty()
        if (legacy.isBlank()) {
            prefs.edit { remove(LEGACY_PASSWORD_KEY) }
            return ""
        }

        val encryptedLegacy = encryptPassword(legacy)
        prefs.edit {
            remove(LEGACY_PASSWORD_KEY)
            if (encryptedLegacy != null) putString(ENCRYPTED_PASSWORD_KEY, encryptedLegacy)
        }
        return legacy
    }

    private fun loadProtectedJson(encryptedKey: String, legacyKey: String): String? {
        prefs.getString(encryptedKey, null)?.let { payload ->
            val clearText = decryptSecret(payload)
            if (clearText != null) return clearText
            prefs.edit { remove(encryptedKey) }
            return null
        }

        val legacyJson = prefs.getString(legacyKey, null) ?: return null
        val encryptedJson = encryptSecret(legacyJson)
        prefs.edit {
            remove(legacyKey)
            if (encryptedJson != null) putString(encryptedKey, encryptedJson)
        }
        return legacyJson
    }

    private fun encryptPassword(clearText: String): String? = encryptSecret(clearText)

    private fun encryptSecret(clearText: String): String? = runCatching {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreatePasswordKey())
        val encrypted = cipher.doFinal(clearText.toByteArray(Charsets.UTF_8))
        listOf(
            CIPHER_VERSION,
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        ).joinToString(":")
    }.getOrNull()

    private fun decryptPassword(payload: String): String? = decryptSecret(payload)

    private fun decryptSecret(payload: String): String? = runCatching {
        val parts = payload.split(":", limit = 3)
        require(parts.size == 3 && parts[0] == CIPHER_VERSION)
        val iv = Base64.decode(parts[1], Base64.NO_WRAP)
        val encrypted = Base64.decode(parts[2], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreatePasswordKey(), GCMParameterSpec(128, iv))
        cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }.getOrNull()

    private fun getOrCreatePasswordKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(PASSWORD_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    PASSWORD_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val SCHEMA_VERSION_KEY = "preferenceSchemaVersion"
        const val CURRENT_SCHEMA_VERSION = 2
        const val DEFAULT_INSURANCE_PROVIDER = "CSB"
        const val LEGACY_PASSWORD_KEY = "password"
        const val ENCRYPTED_PASSWORD_KEY = "passwordEncryptedV1"
        const val LEGACY_DEBTS_KEY = "debts"
        const val ENCRYPTED_DEBTS_KEY = "debtsEncryptedV1"
        const val LEGACY_CLIENT_KEY = "client"
        const val ENCRYPTED_CLIENT_KEY = "clientEncryptedV1"
        const val PASSWORD_KEY_ALIAS = "consucalcu.profile.password.v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val CIPHER_VERSION = "v1"
    }
}

internal object PreferenceJsonCodec {
    fun <T> fromJsonOrNull(gson: Gson, json: String, type: java.lang.reflect.Type): T? =
        runCatching { gson.fromJson<T>(json, type) }.getOrNull()

    fun decodeDebts(gson: Gson, json: String): List<Debt>? = runCatching {
        val array = gson.fromJson(json, com.google.gson.JsonArray::class.java) ?: return null
        array.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            Debt(
                id = item.long("id", System.nanoTime()),
                product = item.string("product", "CSB"),
                balance = item.string("balance"),
                payment = item.string("payment"),
                rate = item.string("rate"),
                originalTerm = item.string("originalTerm", "60"),
                paymentsMade = item.string("paymentsMade")
            )
        }
    }.getOrNull()

    fun decodeClient(gson: Gson, json: String): ClientData? = runCatching {
        val item = gson.fromJson(json, JsonObject::class.java) ?: return null
        ClientData(
            name = item.string("name"),
            nss = item.string("nss"),
            curp = item.string("curp"),
            rfc = item.string("rfc"),
            email = item.string("email"),
            phone = item.string("phone"),
            clabe = item.string("clabe"),
            bank = item.string("bank"),
            age = item.string("age"),
            pensionType = item.string("pensionType"),
            agreement = item.string("agreement", "IMSS"),
            disease = item.string("disease", "NINGUNA"),
            diseaseDate = item.string("diseaseDate"),
            transactionType = item.string("transactionType", "NUEVO"),
            creditId = item.string("creditId"),
            discountAmount = item.string("discountAmount"),
            numDiscounts = item.string("numDiscounts"),
            liquidatingBrand = item.string("liquidatingBrand"),
            isCommissionable = item.string("isCommissionable", "SÍ"),
            ref1Name = item.string("ref1Name"),
            ref1Phone = item.string("ref1Phone"),
            ref2Name = item.string("ref2Name"),
            ref2Phone = item.string("ref2Phone")
        )
    }.getOrNull()

    private fun JsonObject.string(name: String, default: String = ""): String =
        get(name)?.takeUnless { it.isJsonNull }?.takeIf { it.isJsonPrimitive }
            ?.asJsonPrimitive?.takeIf { it.isString }?.asString ?: default

    private fun JsonObject.long(name: String, default: Long): Long =
        get(name)?.takeUnless { it.isJsonNull }
            ?.let { element -> runCatching { element.asLong }.getOrNull() } ?: default
}
