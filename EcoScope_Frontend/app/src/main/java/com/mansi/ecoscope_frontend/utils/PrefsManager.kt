package com.mansi.ecoscope_frontend.utils




import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsManager {

    private const val PREF_NAME = "guardian_prefs"

    private const val KEY_CONTACT_NUMBERS = "saved_contacts"
    private const val KEY_CONTACT_NAMES = "saved_names"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_LANGUAGE_HINDI = "lang_pref"
    private const val KEY_SOS_HISTORY = "sos_history"
    private const val KEY_USERNAME = "user_name" // ✅ NEW: Local Username Cache

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ✅ Username (Local Fallback)
    fun setUsername(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_USERNAME, name).apply()
    }

    fun getUsername(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, "User") ?: "User"
    }

    // 🔒 Contacts List (only numbers)
    fun saveContacts(context: Context, contacts: List<String>) {
        val json = Gson().toJson(contacts)
        getPrefs(context).edit().putString(KEY_CONTACT_NUMBERS, json).apply()
    }

    fun getContacts(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_CONTACT_NUMBERS, null)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
        } else emptyList()
    }

    // 🔒 Contact Names Map (number → name)
    fun saveContactNames(context: Context, names: Map<String, String>) {
        val json = Gson().toJson(names)
        getPrefs(context).edit().putString(KEY_CONTACT_NAMES, json).apply()
    }

    fun getContactNames(context: Context): Map<String, String> {
        val json = getPrefs(context).getString(KEY_CONTACT_NAMES, null)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
        } else emptyMap()
    }



    fun setDarkMode(context: Context, isDark: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    // 🌐 Language (Hindi = true, English = false)
    fun getLangPref(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LANGUAGE_HINDI, false)
    }

    fun setLangPref(context: Context, isHindi: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LANGUAGE_HINDI, isHindi).apply()
    }




}
