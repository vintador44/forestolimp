package org.example.project.data.api

import org.example.project.data.models.*
import retrofit2.http.*
import retrofit2.Response
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ForestNavigationApi {
    
    // Auth
    @POST("/api/registration")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    // Routes
    @GET("/api/roads")
    suspend fun getAllRoads(): Response<ApiResponse<RoadsResponse>>
    
    @GET("/api/roads/{id}")
    suspend fun getRoadById(@Path("id") id: Int): Response<ApiResponse<RoadResponse>>
    
    @GET("/api/roads/user/{userId}")
    suspend fun getRoadsByUser(@Path("userId") userId: Int): Response<ApiResponse<RoadsResponse>>
    
    @POST("/api/roads/create")
    suspend fun createRoad(@Body request: CreateRoadServerRequest): Response<ApiResponse<RoadResponse>>
    
    @GET("/api/route/elevations")
    suspend fun getRouteElevations(
        @Query("startLat") startLat: Double,
        @Query("startLng") startLng: Double,
        @Query("endLat") endLat: Double,
        @Query("endLng") endLng: Double,
        @Query("startDateTime") startDateTime: String? = null,
        @Query("durationHours") durationHours: Double = 3.0
    ): Response<RouteElevationResponse>
    
    // Locations
    @GET("/api/locations")
    suspend fun getLocations(@Query("tags") tags: List<String>? = null): Response<LocationsResponse>
    
    @GET("/api/locations/{id}")
    suspend fun getLocationById(@Path("id") id: Int): Response<LocationResponse>
    
    @POST("/api/locations")
    suspend fun createLocation(@Body request: CreateLocationRequest): Response<LocationResponse>
    
    // Photos
    @Multipart
    @POST("/api/upload")
    suspend fun uploadPhotos(
        @Part("locationId") locationId: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Response<UploadPhotosResponse>
    
    @GET("/api/photos/location/{locationId}")
    suspend fun getPhotosByLocation(@Path("locationId") locationId: Int): Response<PhotosResponse>
    
    // Elevation
    @GET("/api/elevation")
    suspend fun getElevation(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<ElevationResponse>
    
    // Weather
    @GET("/api/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<WeatherResponse>
    
    @GET("/api/weather/forecast/range")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): Response<WeatherForecastResponse>
    
    // Categories
    @GET("/api/categories")
    suspend fun getCategories(): Response<ApiResponse<CategoriesResponse>>
}

@Serializable
data class RegisterRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("FIO")
    val fio: String
)

@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("user")
    val user: User? = null,
    @SerialName("token")
    val token: String? = null
)

@Serializable
data class CreateLocationRequest(
    @SerialName("LocationName")
    val name: String,
    @SerialName("Coordinates")
    val coordinates: PointCoordinates,
    @SerialName("Description")
    val description: String,
    @SerialName("Categories")
    val categories: String? = null
)

@Serializable
data class CreateRoadServerRequest(
    @SerialName("road")
    val road: RoadDataRequest,
    @SerialName("dots")
    val dots: List<DotServerRequest>
)

@Serializable
data class RoadDataRequest(
    @SerialName("Name")
    val name: String,
    @SerialName("Description")
    val description: String,
    @SerialName("Complexity")
    val complexity: String,
    @SerialName("StartDateTime")
    val startDateTime: String,
    @SerialName("EndDateTime")
    val endDateTime: String,
    @SerialName("UserID")
    val userId: Int,
    @SerialName("TotalDistance")
    val totalDistance: Double,
    @SerialName("TotalClimb")
    val totalClimb: Double,
    @SerialName("TotalDescent")
    val totalDescent: Double
)

@Serializable
data class DotServerRequest(
    @SerialName("ThisDotCoordinates")
    val thisDotCoordinates: PointCoordinates,
    @SerialName("NextDotCoordinates")
    val nextDotCoordinates: PointCoordinates? = null
)

@Serializable
data class UploadPhotosResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("photoIds")
    val photoIds: List<Int>? = null
)

@Serializable
data class PhotosResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("photos")
    val photos: List<PhotoData> = emptyList()
)

@Serializable
data class PhotoData(
    @SerialName("ID")
    val id: Int,
    @SerialName("UserID")
    val userId: Int,
    @SerialName("LocationID")
    val locationId: Int,
    @SerialName("mimetype")
    val mimetype: String,
    @SerialName("base64")
    val base64: String
)
