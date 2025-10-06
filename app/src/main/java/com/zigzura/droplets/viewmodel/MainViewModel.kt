package com.zigzura.droplets.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zigzura.droplets.data.PreferencesManager
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.repository.ClaudeRepository
import com.zigzura.droplets.utils.ScreenshotUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val claudeRepository: ClaudeRepository
) : ViewModel() {
    private val _currentHtml = MutableStateFlow("")
    val currentHtml: StateFlow<String> = _currentHtml.asStateFlow()

    private val _currentHistoryItem = MutableStateFlow<PromptHistory?>(null)
    val currentHistoryItem: StateFlow<PromptHistory?> = _currentHistoryItem.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _promptRejection = MutableStateFlow<String?>(null)
    val promptRejection: StateFlow<String?> = _promptRejection.asStateFlow()

    val promptHistory = preferencesManager.promptHistory

    fun generateHtml(prompt: String, enableDebug: Boolean = false, temperature: Double = 0.7) {
        // Generate UUID immediately so it's available to UI right away
        val uuid = UUID.randomUUID().toString()

        // Create temporary PromptHistory object with the UUID
        val tempPromptHistory = PromptHistory(
            id = uuid,
            prompt = prompt,
            html = "", // Will be updated when request completes
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _promptRejection.value = null
            _currentHistoryItem.value = tempPromptHistory // Set immediately with UUID

            claudeRepository.generateHtml(uuid, prompt, enableDebug, temperature).fold(
                onSuccess = { result ->
                    _currentHtml.value = result.html
                    _currentHistoryItem.value = result.promptHistory // Update with complete data
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Unknown error occurred"
                    if (errorMessage.startsWith("PROMPT_REJECTED:")) {
                        val rejectionReason = errorMessage.substringAfter("PROMPT_REJECTED:")
                        _promptRejection.value = rejectionReason
                    } else if (errorMessage.startsWith("API Error:")) {
                        // Convert different API errors to user-friendly messages
                        val userFriendlyMessage = when {
                            errorMessage.contains("529") || errorMessage.contains("overloaded_error") ||
                            errorMessage.contains("Overloaded") -> "Claude is temporarily overloaded. Please try again in a moment."

                            errorMessage.contains("401") || errorMessage.contains("authentication_error") ||
                            errorMessage.contains("invalid_request_error") -> "Authentication failed. Please check your API key in settings."

                            errorMessage.contains("403") || errorMessage.contains("permission_error") ->
                            "Access denied. Please check your API key permissions."

                            errorMessage.contains("429") || errorMessage.contains("rate_limit_error") ->
                            "Rate limit exceeded. Please wait a moment before trying again."

                            errorMessage.contains("500") || errorMessage.contains("502") || errorMessage.contains("503") ||
                            errorMessage.contains("server_error") -> "Server error. Please try again later."

                            errorMessage.contains("400") || errorMessage.contains("invalid_request_error") ->
                            "Invalid request. Please try rephrasing your prompt."

                            errorMessage.contains("413") || errorMessage.contains("request_too_large") ->
                            "Your prompt is too long. Please try a shorter prompt."

                            errorMessage.contains("422") -> "Request format error. Please try again."

                            errorMessage.contains("timeout") || errorMessage.contains("network") ->
                            "Network timeout. Please check your connection and try again."

                            else -> "Server issue, please try again later"
                        }
                        _error.value = userFriendlyMessage
                    } else {
                        _error.value = errorMessage
                    }
                    _currentHistoryItem.value = null // Clear on error
                }
            )

            _isLoading.value = false
        }
    }

    fun loadHistoryItem(historyItem: PromptHistory) {
        _currentHtml.value = historyItem.html
        _currentHistoryItem.value = historyItem // Track the loaded history item
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPromptRejection() {
        _promptRejection.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(id)
        }
    }

    fun updateTitle(id: String, title: String) {
        viewModelScope.launch {
            preferencesManager.updateTitle(id, title)
        }
    }

    fun updateScreenshot(id: String, screenshotPath: String?) {
        viewModelScope.launch {
            preferencesManager.updateScreenshot(id, screenshotPath)
        }
    }

    fun deletePromptHistory(context: Context, id: String) {
        viewModelScope.launch {
            // Delete the screenshot file first
            ScreenshotUtils.deleteScreenshot(context, id)
            // Then delete the prompt history data
            preferencesManager.deletePromptHistory(id)
        }
    }
}
