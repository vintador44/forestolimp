package org.example.project.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun formatTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun getDateDifference(startDate: String, endDate: String?): String {
        return if (endDate.isNullOrEmpty()) {
            "Продолжается"
        } else {
            "Завершено"
        }
    }
}

object CoordinateUtils {
    
    fun parseCoordinates(coordString: String): Pair<Double, Double>? {
        return try {
            val parts = coordString.split(",")
            if (parts.size == 2) {
                Pair(parts[0].toDouble(), parts[1].toDouble())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun formatCoordinates(lat: Double, lng: Double): String {
        return String.format("%.4f, %.4f", lat, lng)
    }
    
    fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val radius = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radius * c
    }
}

object FormatUtils {
    
    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.toInt()} м"
            else -> "${(meters / 1000).toInt()} км"
        }
    }
    
    fun formatElevation(meters: Double): String {
        return "${meters.toInt()} м"
    }
    
    fun formatDuration(hours: Double): String {
        val wholeHours = hours.toInt()
        val minutes = ((hours - wholeHours) * 60).toInt()
        return when {
            wholeHours > 0 -> "$wholeHours ч $minutes м"
            else -> "$minutes м"
        }
    }
    
    fun formatDifficulty(difficulty: Int): String {
        return when {
            difficulty < 1000 -> "$difficulty ед."
            difficulty < 1000000 -> "${(difficulty / 1000)} тыс. ед."
            else -> "${(difficulty / 1000000)} млн ед."
        }
    }
}
