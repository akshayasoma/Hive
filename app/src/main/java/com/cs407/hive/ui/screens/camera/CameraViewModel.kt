package com.cs407.hive.ui.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.hive.data.perplexity.PerplexityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CameraUiState {
    data object Idle: CameraUiState
    data object Capturing: CameraUiState
    data class Analyzing(val message: String = "Analyzing..."): CameraUiState
    data class Success(val response: String): CameraUiState
    data class Error(val error: String): CameraUiState
}

class CameraViewModel(
    private val repository: PerplexityRepository = PerplexityRepository()
): ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun reset() { _uiState.value = CameraUiState.Idle }

    fun analyzePhoto(bitmap: Bitmap, prompt: String) {
        _uiState.value = CameraUiState.Analyzing()
        viewModelScope.launch {
            try {
                val response = repository.analyzeBitmap(bitmap, prompt)
                _uiState.value = CameraUiState.Success(response)
            } catch (t: Throwable) {
                _uiState.value = CameraUiState.Error(t.message ?: "Unknown error")
            }
        }
    }
}

