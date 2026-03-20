package org.example.project.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.models.*
import org.example.project.data.repository.RoadRepository
import org.example.project.data.api.RetrofitClient
import org.example.project.data.api.CreateRoadServerRequest
import org.example.project.data.api.RoadDataRequest
import org.example.project.data.api.DotServerRequest
import com.yandex.mapkit.geometry.Point
import java.text.SimpleDateFormat
import java.util.*

data class RoadsUiState(
    val roads: List<Road> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val previewRoute: RouteElevationResponse? = null
)

class RoadsViewModel(private val repository: RoadRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoadsUiState())
    val uiState: StateFlow<RoadsUiState> = _uiState
    
    init {
        loadRoads()
    }
    
    fun loadRoads() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getAllRoads()
            result.onSuccess { roads ->
                _uiState.value = _uiState.value.copy(roads = roads, isLoading = false)
            }.onFailure { error ->
                Log.e("RoadsViewModel", "Failed to load roads", error)
                _uiState.value = _uiState.value.copy(error = "Ошибка загрузки: ${error.message}", isLoading = false)
            }
        }
    }

    fun getRoutePreview(points: List<Point>, duration: Double, startDateTime: String) {
        if (points.size < 2) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getRouteElevations(
                startLat = points.first().latitude,
                startLng = points.first().longitude,
                endLat = points.last().latitude,
                endLng = points.last().longitude,
                startDateTime = startDateTime,
                durationHours = duration
            )
            result.onSuccess { preview ->
                _uiState.value = _uiState.value.copy(previewRoute = preview, isLoading = false)
            }.onFailure { error ->
                Log.e("RoadsViewModel", "Failed to get preview", error)
                _uiState.value = _uiState.value.copy(error = "Ошибка расчета: ${error.message}", isLoading = false)
            }
        }
    }

    fun createRoad(
        name: String,
        description: String,
        points: List<Point>,
        duration: Double,
        startDateTime: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUserId = RetrofitClient.currentUser?.id
        Log.d("RoadsViewModel", "Starting createRoad. UserID: $currentUserId, Name: $name, Points: ${points.size}")

        if (currentUserId == null) {
            val errorMsg = "Ошибка: Пользователь не авторизован (UserID is null)"
            Log.e("RoadsViewModel", errorMsg)
            _uiState.value = _uiState.value.copy(error = errorMsg)
            onResult(false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val startDate = sdf.parse(startDateTime) ?: Date()
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                calendar.add(Calendar.MINUTE, (duration * 60).toInt())
                val endDateTime = sdf.format(calendar.time)

                val preview = _uiState.value.previewRoute
                val storedDescription = "$name\n$description"

                val roadData = RoadDataRequest(
                    name = name,
                    description = storedDescription,
                    complexity = if (preview != null) calculateComplexity(preview.statistics.totalDifficulty) else "Средний",
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    userId = currentUserId,
                    totalDistance = preview?.statistics?.totalDistance?.toDouble() ?: 0.0,
                    totalClimb = preview?.statistics?.totalClimb?.toDouble() ?: 0.0,
                    totalDescent = preview?.statistics?.totalDescent?.toDouble() ?: 0.0
                )

                val dots = points.mapIndexed { index, point ->
                    val nextPoint = points.getOrNull(index + 1)
                    DotServerRequest(
                        thisDotCoordinates = PointCoordinates(point.latitude, point.longitude),
                        nextDotCoordinates = nextPoint?.let { PointCoordinates(it.latitude, it.longitude) }
                    )
                }

                val request = CreateRoadServerRequest(road = roadData, dots = dots)
                Log.d("RoadsViewModel", "Sending request to server: roads/create")
                
                val result = repository.createRoad(request)
                if (result.isSuccess) {
                    Log.d("RoadsViewModel", "Road created successfully")
                    loadRoads()
                    onResult(true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Неизвестная ошибка сервера"
                    Log.e("RoadsViewModel", "Server returned error: $errorMsg")
                    _uiState.value = _uiState.value.copy(error = "Ошибка создания: $errorMsg", isLoading = false)
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("RoadsViewModel", "Exception during createRoad", e)
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}", isLoading = false)
                onResult(false)
            }
        }
    }

    private fun calculateComplexity(difficulty: Int): String {
        return when {
            difficulty < 5000 -> "Лёгкий"
            difficulty < 15000 -> "Средний"
            difficulty < 30000 -> "Сложный"
            else -> "Эксперт"
        }
    }
}
