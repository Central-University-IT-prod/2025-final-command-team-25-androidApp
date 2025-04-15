package com.chupapis.bookit.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chupapis.bookit.data.model.figure.Figure
import com.chupapis.bookit.data.network.ApiService
import kotlinx.coroutines.launch

class FigureViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _figures = MutableLiveData<List<Figure>>()
    val figures: LiveData<List<Figure>> = _figures

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFigures(coworkingId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getTables(coworkingId)
                if (response.isSuccessful) {
                    _figures.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    _error.value = "Ошибка загрузки столов: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FigureViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FigureViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
