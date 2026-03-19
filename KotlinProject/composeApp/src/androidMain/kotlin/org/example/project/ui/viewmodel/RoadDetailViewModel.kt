package org.example.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.models.Road
import org.example.project.data.models.RouteElevationResponse
import org.example.project.data.repository.RoadRepository

data class RoadDetailUiState(
    val road: Road? = null,
    val routeDetails: RouteElevationResponse? = null,
    val isLoading: Boolean = false,
    val isDetailsLoading: Boolean = false,
    val error: String? = null
)

class RoadDetailViewModel(private val repository: RoadRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadDetailUiState())
    val uiState: StateFlow<RoadDetailUiState> = _uiState

    fun loadRoad(roadId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = repository.getRoadById(roadId)
                if (result.isSuccess) {
                    val road = result.getOrNull()
                    if (road != null) {
                        _uiState.value = _uiState.value.copy(road = road, isLoading = false)
                        fetchRouteDetails(road, road.startDateTime)
                    } else {
                        _uiState.value = _uiState.value.copy(error = "Маршрут не найден", isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Ошибка загрузки", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun updateDepartureTime(newTime: String) {
        val road = _uiState.value.road ?: return
        fetchRouteDetails(road, newTime)
    }

    private fun fetchRouteDetails(road: Road, startDateTime: String) {
        val firstDot = road.dots.firstOrNull()?.thisDotCoordinates
        val lastDot = road.dots.lastOrNull()?.thisDotCoordinates ?: firstDot
        
        if (firstDot != null && lastDot != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isDetailsLoading = true)
                val result = repository.getRouteElevations(
                    startLat = firstDot.lat,
                    startLng = firstDot.lng,
                    endLat = lastDot.lat,
                    endLng = lastDot.lng,
                    startDateTime = startDateTime,
                    durationHours = 3.0
                )
                result.onSuccess { details ->
                    _uiState.value = _uiState.value.copy(routeDetails = details, isDetailsLoading = false)
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(isDetailsLoading = false)
                }
            }
        }
    }
}
