package com.zigzura.droplets.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zigzura.droplets.data.PreferencesManager
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.repository.ClaudeRepository
import com.zigzura.droplets.utils.AppNotificationManager
import com.zigzura.droplets.utils.ScreenshotUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val claudeRepository: ClaudeRepository,
    private val appNotificationManager: AppNotificationManager
) : ViewModel() {
    private val _currentHtml = MutableStateFlow("")
    val currentHtml: StateFlow<String> = _currentHtml.asStateFlow()

    private val _currentHistoryItem = MutableStateFlow<PromptHistory?>(null)
    val currentHistoryItem: StateFlow<PromptHistory?> = _currentHistoryItem.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Global generation state for showing progress across screens
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow("")
    val generationProgress: StateFlow<String> = _generationProgress.asStateFlow()

    // Track if user is currently viewing the generation process
    private val _isAppInForeground = MutableStateFlow(true)
    val isAppInForeground: StateFlow<Boolean> = _isAppInForeground.asStateFlow()

    // Track if user was on create screen when generation started
    private val _wasGenerationStartedFromCreateScreen = MutableStateFlow(false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _promptRejection = MutableStateFlow<String?>(null)
    val promptRejection: StateFlow<String?> = _promptRejection.asStateFlow()

    val promptHistory = preferencesManager.promptHistory

    @OptIn(DelicateCoroutinesApi::class)
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

        // Update UI state immediately in viewModelScope
        viewModelScope.launch {
            _isLoading.value = true
            _isGenerating.value = true
            _generationProgress.value = "Preparing your request..."
            _error.value = null
            _promptRejection.value = null
            _currentHistoryItem.value = tempPromptHistory // Set immediately with UUID

            // Save the PromptHistory immediately so it persists even if user navigates away
            try {
                val savedPromptHistory = preferencesManager.savePrompt(
                    uuid = uuid,
                    prompt = prompt,
                    html = "GENERATING...", // Placeholder to indicate generation in progress
                    title = "", // Will be updated later
                    model = "claude-3-haiku-20240307" // Default, will be updated with actual model used
                )
                _currentHistoryItem.value = savedPromptHistory
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to save initial prompt history", e)
            }
        }

        // Run the actual generation in GlobalScope so it persists across navigation
        GlobalScope.launch {
            try {
                // Update progress from GlobalScope
                viewModelScope.launch { _generationProgress.value = "Connecting to Claude AI..." }

                claudeRepository.generateHtml(uuid, prompt, enableDebug, temperature).fold(
                    onSuccess = { result ->
                        // Update UI state on success
                        viewModelScope.launch {
                            _generationProgress.value = "Finalizing your app..."
                            _currentHtml.value = result.html
                            _currentHistoryItem.value = result.promptHistory // Update with complete data
                            _generationProgress.value = "App ready!"
                        }
                    },
                    onFailure = { exception ->
                        // Clear the "GENERATING..." placeholder on error
                        try {
                            preferencesManager.deletePromptHistory(uuid)
                        } catch (e: Exception) {
                            android.util.Log.e("MainViewModel", "Failed to cleanup failed generation", e)
                        }

                        val errorMessage = exception.message ?: "Unknown error occurred"
                        viewModelScope.launch {
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
                    }
                )
            } finally {
                // Reset UI state
                viewModelScope.launch {
                    _isLoading.value = false
                    _isGenerating.value = false
                    _generationProgress.value = ""
                }
            }
        }
    }

    fun incrementAccessCount(id: String) {
        viewModelScope.launch {
            preferencesManager.incrementAccessCount(id)
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

    fun deletePromptHistory(context: Context, id: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            // Delete the screenshot file first
            ScreenshotUtils.deleteScreenshot(context, id)
            // Then delete the prompt history data
            preferencesManager.deletePromptHistory(id)
            // Call the completion callback on the main thread
            onComplete?.invoke()
        }
    }

    /**
     * Set app foreground state for notification logic
     */
    fun setAppInForeground(isInForeground: Boolean) {
        _isAppInForeground.value = isInForeground
    }

    /**
     * Mark that generation was started from create screen
     */
    fun setGenerationStartedFromCreateScreen(fromCreateScreen: Boolean) {
        _wasGenerationStartedFromCreateScreen.value = fromCreateScreen
    }

    /**
     * Show notification when generation completes (if app is in background)
     */
    fun showGenerationCompleteNotificationIfNeeded(context: Context) {
        if (!_isAppInForeground.value) {
            val historyItem = _currentHistoryItem.value
            appNotificationManager.showAppGenerationCompleteNotification(
                context = context,
                appTitle = historyItem?.title
            )
        }
    }

    /**
     * Show notification when generation fails (if app is in background)
     */
    fun showGenerationFailedNotificationIfNeeded(context: Context, errorMessage: String) {
        if (!_isAppInForeground.value) {
            appNotificationManager.showAppGenerationFailedNotification(
                context = context,
                errorMessage = errorMessage
            )
        }
    }
}
