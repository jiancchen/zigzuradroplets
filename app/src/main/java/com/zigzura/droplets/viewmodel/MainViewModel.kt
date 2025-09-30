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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val claudeRepository: ClaudeRepository
) : ViewModel() {
    private val _currentHtml = MutableStateFlow("")
    val currentHtml: StateFlow<String> = _currentHtml.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val promptHistory = preferencesManager.promptHistory

    fun generateHtml(prompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            claudeRepository.generateHtml(prompt).fold(
                onSuccess = { html ->
                    _currentHtml.value = html
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }
            )

            _isLoading.value = false
        }
    }

    fun loadHistoryItem(historyItem: PromptHistory) {
        _currentHtml.value = historyItem.html
    }

    fun clearError() {
        _error.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
        }
    }
}
