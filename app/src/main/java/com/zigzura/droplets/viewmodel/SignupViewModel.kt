package com.zigzura.droplets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zigzura.droplets.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val existingApiKey = preferencesManager.apiKey

    fun updateApiKey(key: String) {
        _apiKey.value = key
        _error.value = null
    }

    fun saveApiKey() {
        if (_apiKey.value.isBlank()) {
            _error.value = "Please enter a valid API key"
            return
        }

        if (!_apiKey.value.startsWith("sk-")) {
            _error.value = "API key should start with 'sk-'"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                preferencesManager.saveApiKey(_apiKey.value)
                _isSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to save API key: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            preferencesManager.clearApiKey()
            _apiKey.value = ""
            _isSuccess.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
