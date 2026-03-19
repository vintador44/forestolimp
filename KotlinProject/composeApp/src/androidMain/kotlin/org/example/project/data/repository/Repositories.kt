package org.example.project.data.repository

import org.example.project.data.api.ForestNavigationApi
import org.example.project.data.api.CreateLocationRequest
import org.example.project.data.api.CreateRoadServerRequest
import org.example.project.data.api.PhotosResponse
import org.example.project.data.api.UploadPhotosResponse
import org.example.project.data.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RoadRepository(private val api: ForestNavigationApi) {
    
    suspend fun getAllRoads(): Result<List<Road>> = try {
        val response = api.getAllRoads()
        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data.roads)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch roads"))
            }
        } else {
            Result.failure(Exception("Failed to fetch roads"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getRoadById(id: Int): Result<Road> = try {
        val response = api.getRoadById(id)
        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.success && apiResponse.data != null && apiResponse.data.road != null) {
                Result.success(apiResponse.data.road)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Road not found"))
            }
        } else {
            Result.failure(Exception("Failed to fetch road"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getRoadsByUser(userId: Int): Result<List<Road>> = try {
        val response = api.getRoadsByUser(userId)
        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data.roads)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch user roads"))
            }
        } else {
            Result.failure(Exception("Failed to fetch user roads"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun createRoad(request: CreateRoadServerRequest): Result<Road> = try {
        val response = api.createRoad(request)
        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.success && apiResponse.data != null && apiResponse.data.road != null) {
                Result.success(apiResponse.data.road)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create road"))
            }
        } else {
            Result.failure(Exception("Failed to create road: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getRouteElevations(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        startDateTime: String? = null,
        durationHours: Double = 3.0
    ): Result<RouteElevationResponse> = try {
        val response = api.getRouteElevations(startLat, startLng, endLat, endLng, startDateTime, durationHours)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to get route elevations"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class LocationRepository(private val api: ForestNavigationApi) {

    suspend fun getLocations(tags: List<String>? = null): Result<List<Location>> = try {
        val response = api.getLocations(tags)
        if (response.isSuccessful) {
            val locationsResponse = response.body()
            if (locationsResponse != null) {
                Result.success(locationsResponse.locations)
            } else {
                Result.failure(Exception("Empty response body"))
            }
        } else {
            Result.failure(Exception("HTTP error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getLocationById(id: Int): Result<Location> = try {
        val response = api.getLocationById(id)
        if (response.isSuccessful && response.body() != null) {
            val locationResponse = response.body()!!
            if (locationResponse.location != null) {
                Result.success(locationResponse.location)
            } else {
                Result.failure(Exception("Location not found"))
            }
        } else {
            Result.failure(Exception("Failed to fetch location"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun createLocation(request: CreateLocationRequest): Result<Location> = try {
        val response = api.createLocation(request)
        if (response.isSuccessful && response.body() != null) {
            val locationResponse = response.body()!!
            if (locationResponse.location != null) {
                Result.success(locationResponse.location)
            } else {
                Result.failure(Exception("Location not found in response"))
            }
        } else {
            Result.failure(Exception("HTTP error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadPhotos(locationId: Int, userId: Int, photos: List<ByteArray>): Result<UploadPhotosResponse> = try {
        val locationIdBody = locationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        
        val parts = photos.mapIndexed { index, bytes ->
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photos", "photo_$index.jpg", requestFile)
        }
        
        val response = api.uploadPhotos(locationIdBody, userIdBody, parts)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Upload failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPhotosByLocation(locationId: Int): Result<PhotosResponse> = try {
        val response = api.getPhotosByLocation(locationId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to fetch photos"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class WeatherRepository(private val api: ForestNavigationApi) {
    
    suspend fun getWeather(lat: Double, lng: Double): Result<WeatherResponse> = try {
        val response = api.getWeather(lat, lng)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to fetch weather"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class CategoryRepository(private val api: ForestNavigationApi) {
    
    suspend fun getCategories(): Result<List<Category>> = try {
        val response = api.getCategories()
        if (response.isSuccessful && response.body() != null) {
            val apiResponse = response.body()!!
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data.categories)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch categories"))
            }
        } else {
            Result.failure(Exception("Failed to fetch categories"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
