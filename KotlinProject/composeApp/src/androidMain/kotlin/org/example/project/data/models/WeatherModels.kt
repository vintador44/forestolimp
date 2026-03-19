package org.example.project.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("temperature")
    val temperature: Double,
    @SerialName("description")
    val description: String,
    @SerialName("wind")
    val wind: Double
)

@Serializable
data class WeatherForecastResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("city")
    val city: String,
    @SerialName("forecast_count")
    val forecastCount: Int,
    @SerialName("forecast")
    val forecast: List<String>
)