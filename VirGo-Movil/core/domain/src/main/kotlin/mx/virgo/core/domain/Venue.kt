package mx.virgo.core.domain

data class Venue(
    val id: String,
    val name: String,
    val shortName: String?,
    val zone: String?,
    val capacity: Int,
    val ratingAverage: Double,
    val ratingCount: Int,
    val latitude: Double?,
    val longitude: Double?,
    val priceBaseAmount: Double? = null,
    val priceUnit: String? = null,
    val categories: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
)
