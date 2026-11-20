package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VenueTest {
    private fun venue(
        price: Double? = null,
        unit: String? = null,
        categories: List<String> = emptyList(),
        photos: List<String> = emptyList(),
    ): Venue =
        Venue(
            id = "uuid-1",
            name = "Casa VirGo",
            shortName = "Casa",
            zone = "Centro",
            capacity = 150,
            ratingAverage = 4.5,
            ratingCount = 12,
            latitude = 20.7001,
            longitude = -100.4455,
            priceBaseAmount = price,
            priceUnit = unit,
            categories = categories,
            photos = photos,
        )

    @Test
    fun catalogFieldsDefaultToEmptyWhenTheContractDoesNotSendThem() {
        val plain =
            Venue(
                id = "uuid-1",
                name = "Casa VirGo",
                shortName = "Casa",
                zone = "Centro",
                capacity = 150,
                ratingAverage = 4.5,
                ratingCount = 12,
                latitude = 20.7001,
                longitude = -100.4455,
            )

        assertEquals(null, plain.priceBaseAmount)
        assertEquals(null, plain.priceUnit)
        assertEquals(emptyList<String>(), plain.categories)
        assertEquals(emptyList<String>(), plain.photos)
    }

    @Test
    fun equalityComparesPriceCategoriesAndPhotos() {
        val base = venue(price = 45000.0, unit = "day", categories = listOf("Jardines"))

        assertEquals(base, venue(price = 45000.0, unit = "day", categories = listOf("Jardines")))
        assertNotEquals(base, venue(price = 38000.0, unit = "day", categories = listOf("Jardines")))
        assertNotEquals(base, venue(price = 45000.0, unit = "hour", categories = listOf("Jardines")))
        assertNotEquals(base, venue(price = 45000.0, unit = "day", categories = listOf("Alberca")))
        assertNotEquals(base, venue(price = 45000.0, unit = "day", photos = listOf("/a.png")))
    }

    @Test
    fun copyChangesOnlyTheRequestedField() {
        val original = venue(price = 45000.0, unit = "day")

        val discounted = original.copy(priceBaseAmount = 30000.0)

        assertEquals(30000.0, discounted.priceBaseAmount!!, 0.0)
        assertEquals(original.id, discounted.id)
        assertEquals(original.categories, discounted.categories)
        assertEquals(original.name, discounted.name)
    }

    @Test
    fun equalVenuesShareHashAndDescribeTheCatalogFields() {
        val left = venue(price = 45000.0, unit = "day", categories = listOf("Jardines"))
        val right = venue(price = 45000.0, unit = "day", categories = listOf("Jardines"))

        assertEquals(left.hashCode(), right.hashCode())
        assertNotEquals(left.hashCode(), venue(price = 1.0).hashCode())
        assertTrue(left.toString().contains("Casa VirGo"))
        assertTrue(left.toString().contains("45000.0"))
        assertNotEquals(left.toString(), venue(price = 1.0).toString())
    }
}
