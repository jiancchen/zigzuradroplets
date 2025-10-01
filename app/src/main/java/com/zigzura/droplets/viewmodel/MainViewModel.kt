package com.zigzura.droplets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zigzura.droplets.data.PreferencesManager
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.repository.ClaudeRepository
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
}
