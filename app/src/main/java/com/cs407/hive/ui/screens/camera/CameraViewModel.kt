package com.cs407.hive.ui.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.hive.data.perplexity.PerplexityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

sealed interface CameraUiState {
    data object Idle: CameraUiState
    data object Capturing: CameraUiState
    data class Analyzing(val message: String = "Analyzing..."): CameraUiState
    data class Success(val ingredients: List<String>, val rawResponse: String) : CameraUiState
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
                val ingredients = parseIngredients(response)
                _uiState.value = CameraUiState.Success(ingredients, response)
            } catch (t: Throwable) {
                _uiState.value = CameraUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    private fun parseIngredients(response: String): List<String> {
        val trimmed = response.trim()
        parseJsonObject(trimmed)?.let { return it }
        parseJsonArray(trimmed)?.let { return it }
        return trimmed.split('\n', ',', ';')
            .map { it.substringBefore('#').trim() }
            .mapNotNull(::normalizeIngredient)
            .distinct()
    }

    private fun parseJsonObject(raw: String): List<String>? = try {
        val obj = JSONObject(raw)
        val arr = obj.optJSONArray("ingredients") ?: return emptyList()
        buildList {
            for (i in 0 until arr.length()) {
                val value = arr.optString(i)
                normalizeIngredient(value)?.let { add(it) }
            }
        }.distinct()
    } catch (_: JSONException) {
        null
    }

    private fun parseJsonArray(raw: String): List<String>? = try {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val value = arr.optString(i)
                normalizeIngredient(value)?.let { add(it) }
            }
        }.distinct()
    } catch (_: JSONException) {
        null
    }

    private fun normalizeIngredient(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val cleaned = input
            .replace(Regex("[\\d•\\-*]"), " ")
            .replace(Regex("[^A-Za-z0-9\\s]"), " ")
            .trim()
        if (cleaned.isBlank()) return null
        return cleaned
            .lowercase(Locale.getDefault())
            .split(Regex("\\s+"))
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                }
            }
    }
}
