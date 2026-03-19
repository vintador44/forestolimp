package org.example.project.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: T? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class RoadsResponse(
    @SerialName("roads")
    val roads: List<Road> = emptyList()
)

@Serializable
data class RoadResponse(
    @SerialName("road")
    val road: Road? = null
)

@Serializable
data class LocationsResponse(
    @SerialName("locations")
    val locations: List<Location> = emptyList()
)

@Serializable
data class LocationResponse(
    @SerialName("location")
    val location: Location? = null
)

@Serializable
data class ElevationResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("elevation")
    val elevation: Double,
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("unit")
    val unit: String
)

@Serializable
data class CategoriesResponse(
    @SerialName("categories")
    val categories: List<Category> = emptyList()
)
