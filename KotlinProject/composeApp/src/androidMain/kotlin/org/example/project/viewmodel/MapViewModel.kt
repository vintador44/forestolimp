package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.models.Road
import org.example.project.data.models.Location
import org.example.project.data.models.PointCoordinates
import org.example.project.data.repository.RoadRepository
import org.example.project.data.repository.LocationRepository
import org.example.project.data.api.RetrofitClient
import org.example.project.data.api.CreateLocationRequest
import org.example.project.data.api.PhotoData
import android.util.Log

class MapViewModel : ViewModel() {
    private val roadRepository = RoadRepository(RetrofitClient.instance)
    private val locationRepository = LocationRepository(RetrofitClient.instance)

    private val _roads = MutableStateFlow<List<Road>>(emptyList())
    val roads: StateFlow<List<Road>> = _roads

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _locationPhotos = MutableStateFlow<Map<Int, List<PhotoData>>>(emptyMap())
    val locationPhotos: StateFlow<Map<Int, List<PhotoData>>> = _locationPhotos

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val roadsResult = roadRepository.getAllRoads()
                val locationsResult = locationRepository.getLocations()

                if (roadsResult.isSuccess) {
                    _roads.value = roadsResult.getOrDefault(emptyList())
                }
                if (locationsResult.isSuccess) {
                    _locations.value = locationsResult.getOrDefault(emptyList())
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPhotos(locationId: Int) {
        viewModelScope.launch {
            val result = locationRepository.getPhotosByLocation(locationId)
            if (result.isSuccess) {
                val photos = result.getOrNull()?.photos ?: emptyList()
                val currentMap = _locationPhotos.value.toMutableMap()
                currentMap[locationId] = photos
                _locationPhotos.value = currentMap
            }
        }
    }

    fun uploadPhotos(locationId: Int, photos: List<ByteArray>) {
        val currentUserId = RetrofitClient.currentUser?.id
        if (currentUserId == null) {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = locationRepository.uploadPhotos(locationId, currentUserId, photos)
            if (result.isSuccess) {
                loadPhotos(locationId)
            } else {
                _error.value = "Ошибка загрузки фото"
            }
            _isLoading.value = false
        }
    }

    fun createLocation(name: String, description: String, lat: Double, lng: Double, categories: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateLocationRequest(name, PointCoordinates(lat, lng), description, categories)
                val result = locationRepository.createLocation(request)
                if (result.isSuccess) {
                    loadData()
                    onResult(true)
                } else {
                    _error.value = "Ошибка при создании"
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
