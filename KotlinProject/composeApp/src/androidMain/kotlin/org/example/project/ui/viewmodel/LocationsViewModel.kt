package org.example.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.models.Location
import org.example.project.data.repository.LocationRepository
import org.example.project.data.api.CreateLocationRequest

data class LocationsUiState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LocationsViewModel(private val repository: LocationRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LocationsUiState())
    val uiState: StateFlow<LocationsUiState> = _uiState
    
    init {
        loadLocations()
    }
    
    fun loadLocations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getLocations()
            result.onSuccess { locations ->
                _uiState.value = _uiState.value.copy(locations = locations, isLoading = false, error = null)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "Unknown error", isLoading = false)
            }
        }
    }
    
    fun createLocation(name: String, description: String, lat: Double, lng: Double, categories: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val request = CreateLocationRequest(
                name = name,
                coordinates = listOf(lat, lng),
                description = description,
                categories = categories
            )
            val result = repository.createLocation(request)
            result.onSuccess {
                loadLocations() // Refresh list
                onResult(true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "Unknown error", isLoading = false)
                onResult(false)
            }
        }
    }
    
    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getLocations(tags = listOf(category))
            result.onSuccess { locations ->
                _uiState.value = _uiState.value.copy(locations = locations, isLoading = false, error = null)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "Unknown error", isLoading = false)
            }
        }
    }
}
