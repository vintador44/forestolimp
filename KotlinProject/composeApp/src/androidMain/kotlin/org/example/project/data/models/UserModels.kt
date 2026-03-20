@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package org.example.project.data.models

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable
data class User(
    // Обрабатываем все возможные варианты написания ID
    @SerialName("ID") @JsonNames("id", "UserID", "userId")
    val id: Int? = null,
    
    @SerialName("FIO") @JsonNames("fio", "name", "Name")
    val fio: String? = null,
    
    @SerialName("Email") @JsonNames("email")
    val email: String? = null
)

@Serializable
data class Location(
    @SerialName("ID")
    val id: Int,
    @SerialName("LocationName")
    val name: String? = null,
    @SerialName("Coordinates")
    @Serializable(with = CoordinatesSerializer::class)
    val coordinates: List<Double> = emptyList(),
    @SerialName("Description")
    val description: String? = null,
    @SerialName("Categories")
    val categories: String? = null
)

@Serializable
data class Category(
    @SerialName("ID")
    val id: Int,
    @SerialName("CategoryName")
    val name: String
)

@Serializable
data class Photo(
    @SerialName("ID")
    val id: Int,
    @SerialName("UserID")
    val userId: Int,
    @SerialName("LocationID")
    val locationId: Int
)

object CoordinatesSerializer : KSerializer<List<Double>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Coordinates")

    override fun deserialize(decoder: Decoder): List<Double> {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        return try {
            val element = input.decodeJsonElement()
            when (element) {
                is JsonArray -> element.map { it.jsonPrimitive.double }
                is JsonObject -> {
                    val coordsArray = element["coordinates"] as? JsonArray ?: element["Coordinates"] as? JsonArray
                    coordsArray?.map { it.jsonPrimitive.double } ?: emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<Double>) {
        encoder.encodeSerializableValue(ListSerializer(Double.serializer()), value)
    }
}
