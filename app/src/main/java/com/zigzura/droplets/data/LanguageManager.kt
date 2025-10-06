package com.zigzura.droplets.data

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

// Extension to create DataStore
private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

class LanguageManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        const val SYSTEM_DEFAULT = "system_default"
        const val ENGLISH = "en"
        const val SPANISH = "es"
        const val FRENCH = "fr"
        const val GERMAN = "de"
    }

    // Available languages
    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String
    )

    val availableLanguages = listOf(
        Language(SYSTEM_DEFAULT, "System Default", "System"),
        Language(ENGLISH, "English", "English"),
        Language(SPANISH, "Spanish", "Español"),
        Language(FRENCH, "French", "Français"),
        Language(GERMAN, "German", "Deutsch")
    )

    // Save selected language
    suspend fun setLanguage(languageCode: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    // Get selected language as Flow
    val selectedLanguage: Flow<String> = context.languageDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: SYSTEM_DEFAULT
    }

    // Apply language configuration to context
    fun applyLanguage(context: Context, languageCode: String): Context {
        return if (languageCode == SYSTEM_DEFAULT) {
            // Use system default
            context
        } else {
            // Validate that the language is supported, fallback to English if not
            val supportedLanguages = listOf(ENGLISH, SPANISH, FRENCH, GERMAN)
            val validLanguageCode = if (supportedLanguages.contains(languageCode)) {
                languageCode
            } else {
                ENGLISH // Fallback to English if language not supported
            }

            // Apply selected language
            val locale = Locale(validLanguageCode)
            Locale.setDefault(locale)

            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)

            context.createConfigurationContext(configuration)
        }
    }

    // Get display name for a language code
    fun getLanguageDisplayName(code: String): String {
        return availableLanguages.find { it.code == code }?.displayName ?: "System Default"
    }

    // Get native name for a language code
    fun getLanguageNativeName(code: String): String {
        return availableLanguages.find { it.code == code }?.nativeName ?: "System"
    }
}
