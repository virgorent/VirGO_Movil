package mx.virgo.app.ui.ownervenues

import kotlinx.serialization.Serializable
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueDraft
import java.math.BigDecimal

private val PRICE_PATTERN = Regex("""^\d{1,9}([.,]\d{1,2})?$""")
private const val MAX_WHOLE_DIGITS = 6

/** Texto tal como lo escribe la persona; se guarda en `SavedStateHandle` (022 RNF-03). */
@Serializable
data class VenueForm(
    val name: String = "",
    val shortName: String = "",
    val description: String = "",
    val address: String = "",
    val zone: String? = null,
    val capacity: String = "",
    val price: String = "",
    val unit: String = PriceUnit.HOUR.wire,
    val minHours: String = "",
    val categories: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val services: String = "",
) {
    fun toDraft(): VenueDraft =
        VenueDraft(
            name = name,
            shortName = shortName,
            description = description,
            address = address,
            zone = zone,
            capacity = capacity.toWholeNumber(),
            priceBase = price.toPrice(),
            priceUnit = PriceUnit.fromWire(unit),
            minHours = minHours.toWholeNumber(),
            categories = categories.toSet(),
            amenities = amenities.toSet(),
            services = services.split(',').map { it.trim() }.filter { it.isNotEmpty() },
        )

    companion object {
        fun from(venue: OwnerVenue): VenueForm =
            with(venue.draft) {
                VenueForm(
                    name = name,
                    shortName = shortName,
                    description = description,
                    address = address,
                    zone = zone,
                    capacity = capacity?.toString().orEmpty(),
                    price = priceBase?.stripTrailingZeros()?.toPlainString().orEmpty(),
                    unit = priceUnit.wire,
                    minHours = minHours?.toString().orEmpty(),
                    categories = categories.toList(),
                    amenities = amenities.toList(),
                    services = services.joinToString(", "),
                )
            }
    }
}

private fun String.toWholeNumber(): Int? =
    trim().takeIf { it.length in 1..MAX_WHOLE_DIGITS && it.all(Char::isDigit) }?.toInt()

private fun String.toPrice(): BigDecimal? =
    trim().takeIf { PRICE_PATTERN.matches(it) }?.replace(',', '.')?.let(::BigDecimal)
