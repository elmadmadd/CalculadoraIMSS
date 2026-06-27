package com.example.consucalcu.data

import android.content.Context
import com.example.consucalcu.model.ClientData
import com.example.consucalcu.model.Debt
import com.example.consucalcu.model.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferenceRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadProfile(): UserProfile {
        return UserProfile(
            prefs.getString("name", "") ?: "",
            prefs.getString("username", "") ?: "",
            prefs.getString("password", "") ?: "",
            prefs.getString("supervisor", "") ?: ""
        )
    }

    fun saveProfile(p: UserProfile) {
        prefs.edit()
            .putString("name", p.name)
            .putString("username", p.username)
            .putString("password", p.password)
            .putString("supervisor", p.supervisor)
            .apply()
    }

    fun loadDebts(): List<Debt> {
        val json = prefs.getString("debts", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Debt>>() {}.type)
        } else {
            listOf(Debt(id = System.nanoTime()))
        }
    }

    fun loadClient(): ClientData {
        val json = prefs.getString("client", null)
        return if (json != null) {
            gson.fromJson(json, ClientData::class.java)
        } else {
            ClientData()
        }
    }

    fun getCapacityCNCA(): String = prefs.getString("capCNCA", "") ?: ""
    fun getIsSoundEnabled(): Boolean = prefs.getBoolean("isSoundEnabled", true)
    fun getIsHapticEnabled(): Boolean = prefs.getBoolean("isHapticEnabled", true)

    fun getInsuranceAge(): String = prefs.getString("insAge", "") ?: ""
    fun getInsuranceAmount(): String = prefs.getString("insAmount", "") ?: ""
    fun getInsuranceTerm(): Int = prefs.getInt("insTerm", 60)
    fun getInsuranceProvider(): String = prefs.getString("insProvider", "CONSUBANCO") ?: "CONSUBANCO"
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
        isSoundEnabled: Boolean,
        isHapticEnabled: Boolean
    ) {
        prefs.edit()
            .putString("debts", gson.toJson(debts))
            .putString("client", gson.toJson(client))
            .putString("capCNCA", capacityCNCA)
            .putString("insAge", insAge)
            .putString("insAmount", insAmount)
            .putInt("insTerm", insTerm)
            .putString("insProvider", insProvider)
            .putString("insPensionType", insPensionType)
            .putBoolean("insIsNew", insIsNew)
            .putString("insAntiquity", insAntiquity)
            .putBoolean("isSoundEnabled", isSoundEnabled)
            .putBoolean("isHapticEnabled", isHapticEnabled)
            .apply()
    }
}
