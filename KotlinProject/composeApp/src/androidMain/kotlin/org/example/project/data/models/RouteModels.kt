package org.example.project.data.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class Road(
    @SerialName("ID")
    val id: Int,
    @SerialName("Description")
    val description: String? = null,
    @SerialName("UserID")
    val userId: Int? = null,
    @SerialName("StartDateTime")
    val startDateTime: String,
    @SerialName("EndDateTime")
    val endDateTime: String? = null,
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Complexity")
    val complexity: String? = null,
    @SerialName("TotalDistance")
    val totalDistance: Double = 0.0,
    @SerialName("TotalClimb")
    val totalClimb: Double = 0.0,
    @SerialName("TotalDescent")
    val totalDescent: Double = 0.0,
    @SerialName("dots")
    val dots: List<Dot> = emptyList(),
    @SerialName("user")
    val user: User? = null,
    @SerialName("votes")
    val votes: List<Vote> = emptyList()
)

@Serializable
data class Dot(
    @SerialName("ID")
    val id: Int,
    @SerialName("ThisDotCoordinates")
    val thisDotCoordinates: PointCoordinates,
    @SerialName("NextDotCoordinates")
    val nextDotCoordinates: PointCoordinates? = null,
    @SerialName("RoadID")
    val roadId: Int
)

@Serializable(with = PointCoordinatesSerializer::class)
data class PointCoordinates(val lat: Double, val lng: Double)

@Serializable
data class Vote(
    @SerialName("ID")
    val id: Int,
    @SerialName("UserID")
    val userId: Int,
    @SerialName("RoadID")
    val roadId: Int,
    @SerialName("Vote")
    val vote: Int
)

@Serializable
data class TrackPoint(
    @SerialName("lat")
    val lat: Double,
    @SerialName("lng")
    val lng: Double,
    @SerialName("elevation")
    val elevation: Double,
    @SerialName("point_index")
    val pointIndex: Int
)

@Serializable
data class RouteStatistics(
    @SerialName("total_distance")
    val totalDistance: Int,
    @SerialName("total_difficulty")
    val totalDifficulty: Int,
    @SerialName("total_climb")
    val totalClimb: Int,
    @SerialName("total_descent")
    val totalDescent: Int,
    @SerialName("max_elevation")
    val maxElevation: Double,
    @SerialName("min_elevation")
    val minElevation: Double,
    @SerialName("avg_slope")
    val avgSlope: String,
    @SerialName("estimated_duration_hours")
    val estimatedDurationHours: Double
)

@Serializable
data class WeatherPoint(
    @SerialName("point_index")
    val pointIndex: Int,
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("time_offset_hours")
    val timeOffsetHours: Double,
    @SerialName("estimated_time")
    val estimatedTime: String,
    @SerialName("weather")
    val weather: WeatherData? = null
)

@Serializable
data class RouteElevationResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("track")
    val track: List<TrackPoint> = emptyList(),
    @SerialName("statistics")
    val statistics: RouteStatistics,
    @SerialName("weather_timeline")
    val weatherTimeline: List<WeatherPoint> = emptyList(),
    @SerialName("points_count")
    val pointsCount: Int
)

@Serializable
data class Coordinates(
    @SerialName("lat")
    val lat: Double,
    @SerialName("lng")
    val lng: Double
)

@Serializable
data class WeatherData(
    @SerialName("temperature")
    val temperature: Double,
    @SerialName("precipitation")
    val precipitation: Double,
    @SerialName("weathercode")
    val weatherCode: Int,
    @SerialName("windspeed")
    val windSpeed: Double,
    @SerialName("time")
    val time: String,
    @SerialName("relative_time")
    val relativeTime: String
)

object PointCoordinatesSerializer : KSerializer<PointCoordinates> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PointCoordinates")

    override fun deserialize(decoder: Decoder): PointCoordinates {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val element = input.decodeJsonElement()
        
        return when (element) {
            is JsonObject -> {
                val coords = element["coordinates"]?.jsonArray
                if (coords != null && coords.size >= 2) {
                    val lng = coords[0].jsonPrimitive.double
                    val lat = coords[1].jsonPrimitive.double
                    PointCoordinates(lat, lng)
                } else {
                    val lat = element["lat"]?.jsonPrimitive?.doubleOrNull ?: element["latitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val lng = element["lng"]?.jsonPrimitive?.doubleOrNull ?: element["longitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    PointCoordinates(lat, lng)
                }
            }
            is JsonArray -> {
                if (element.size >= 2) {
                    val lng = element[0].jsonPrimitive.double
                    val lat = element[1].jsonPrimitive.double
                    PointCoordinates(lat, lng)
                } else PointCoordinates(0.0, 0.0)
            }
            is JsonPrimitive -> {
                val content = element.content
                val parts = content.split(",")
                if (parts.size >= 2) {
                    val lat = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val lng = parts[1].trim().toDoubleOrNull() ?: 0.0
                    PointCoordinates(lat, lng)
                } else PointCoordinates(0.0, 0.0)
            }
            else -> PointCoordinates(0.0, 0.0)
        }
    }

    override fun serialize(encoder: Encoder, value: PointCoordinates) {
        encoder.encodeString("${value.lat},${value.lng}")
    }
}
