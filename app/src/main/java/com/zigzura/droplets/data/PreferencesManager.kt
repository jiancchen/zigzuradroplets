package com.zigzura.droplets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val PROMPT_HISTORY = stringPreferencesKey("prompt_history")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    val promptHistory: Flow<List<PromptHistory>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[PROMPT_HISTORY] ?: "[]"
            val type = object : TypeToken<List<PromptHistory>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun savePrompt(uuid: String, prompt: String, html: String, title: String = ""): PromptHistory {
        val newPrompt = PromptHistory(
            id = uuid, // Use the pre-generated UUID instead of timestamp
            prompt = prompt,
            html = html,
            title = title
        )

        context.dataStore.edit { preferences ->
            val currentJson = preferences[PROMPT_HISTORY] ?: "[]"
            val type = object : TypeToken<List<PromptHistory>>() {}.type
            val currentList: MutableList<PromptHistory> = gson.fromJson(currentJson, type) ?: mutableListOf()

            currentList.add(0, newPrompt) // Add to beginning

            // Keep only last 50 prompts
            if (currentList.size > 50) {
                currentList.removeAt(currentList.size - 1)
            }

            preferences[PROMPT_HISTORY] = gson.toJson(currentList)
        }

        return newPrompt // Return the created PromptHistory with the UUID
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PROMPT_HISTORY] ?: "[]"
            val type = object : TypeToken<List<PromptHistory>>() {}.type
            val currentList: MutableList<PromptHistory> = gson.fromJson(currentJson, type) ?: mutableListOf()

            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                val item = currentList[index]
                currentList[index] = item.copy(favorite = !(item.favorite ?: false))
                preferences[PROMPT_HISTORY] = gson.toJson(currentList)
            }
        }
    }

    suspend fun updateTitle(id: String, title: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PROMPT_HISTORY] ?: "[]"
            val type = object : TypeToken<List<PromptHistory>>() {}.type
            val currentList: MutableList<PromptHistory> = gson.fromJson(currentJson, type) ?: mutableListOf()

            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                val item = currentList[index]
                currentList[index] = item.copy(title = title)
                preferences[PROMPT_HISTORY] = gson.toJson(currentList)
            }
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(API_KEY)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(PROMPT_HISTORY)
        }
    }
}
