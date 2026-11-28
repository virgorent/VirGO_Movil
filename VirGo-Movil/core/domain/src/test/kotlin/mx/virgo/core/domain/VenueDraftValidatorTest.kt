package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class VenueDraftValidatorTest {
    private val valid =
        VenueDraft(
            name = "Salón Las Palmas",
            shortName = "Las Palmas",
            description = "Salón con jardín para 200 invitados.",
            address = "Av. Juriquilla 100",
            zone = "Juriquilla",
            capacity = 200,
            priceBase = BigDecimal("1500"),
            minHours = 4,
            categories = setOf("Salon"),
            amenities = setOf("Sonido"),
            services = listOf("Mobiliario"),
        )

    private fun details(draft: VenueDraft) = VenueDraftValidator.validate(draft, VenueStep.DETAILS)

    private fun capacity(draft: VenueDraft) = VenueDraftValidator.validate(draft, VenueStep.CAPACITY_PRICE)

    @Test
    fun validDraftPassesEveryStep() {
        VenueStep.entries.forEach { assertTrue(VenueDraftValidator.validate(valid, it).isEmpty()) }
    }

    @Test
    fun descriptionNeedsTwentyToTwoThousandCharacters() {
        assertEquals(FieldProblem.TOO_SHORT, details(valid.copy(description = "a".repeat(19)))[VenueField.DESCRIPTION])
        assertNull(details(valid.copy(description = "a".repeat(20)))[VenueField.DESCRIPTION])
        assertNull(details(valid.copy(description = "a".repeat(2000)))[VenueField.DESCRIPTION])
        assertEquals(FieldProblem.TOO_LONG, details(valid.copy(description = "a".repeat(2001)))[VenueField.DESCRIPTION])
        assertEquals(FieldProblem.REQUIRED, details(valid.copy(description = "   "))[VenueField.DESCRIPTION])
    }

    @Test
    fun textLimitsMatchTheServer() {
        assertNull(details(valid.copy(shortName = "a".repeat(20)))[VenueField.SHORT_NAME])
        assertEquals(FieldProblem.TOO_LONG, details(valid.copy(shortName = "a".repeat(21)))[VenueField.SHORT_NAME])
        assertNull(details(valid.copy(name = "a".repeat(100)))[VenueField.NAME])
        assertEquals(FieldProblem.TOO_LONG, details(valid.copy(name = "a".repeat(101)))[VenueField.NAME])
        assertNull(details(valid.copy(address = "a".repeat(200)))[VenueField.ADDRESS])
        assertEquals(FieldProblem.TOO_LONG, details(valid.copy(address = "a".repeat(201)))[VenueField.ADDRESS])
        assertEquals(FieldProblem.REQUIRED, details(valid.copy(name = ""))[VenueField.NAME])
    }

    @Test
    fun zoneMustBeOneOfTheServerZones() {
        assertEquals(FieldProblem.REQUIRED, details(valid.copy(zone = null))[VenueField.ZONE])
        assertEquals(FieldProblem.INVALID, details(valid.copy(zone = "Norte"))[VenueField.ZONE])
        VenueCatalogs.zones.forEach { assertNull(details(valid.copy(zone = it))[VenueField.ZONE]) }
    }

    @Test
    fun numbersMustBePositive() {
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(capacity = 0))[VenueField.CAPACITY])
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(capacity = null))[VenueField.CAPACITY])
        assertNull(capacity(valid.copy(capacity = 1))[VenueField.CAPACITY])
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(priceBase = BigDecimal.ZERO))[VenueField.PRICE_BASE])
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(priceBase = null))[VenueField.PRICE_BASE])
        assertNull(capacity(valid.copy(priceBase = BigDecimal("0.01")))[VenueField.PRICE_BASE])
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(minHours = 0))[VenueField.MIN_HOURS])
        assertNull(capacity(valid.copy(minHours = 1))[VenueField.MIN_HOURS])
    }

    @Test
    fun listsOnlyAcceptServerValues() {
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(categories = setOf("Bar")))[VenueField.CATEGORIES])
        assertEquals(FieldProblem.INVALID, capacity(valid.copy(amenities = setOf("Alberca")))[VenueField.AMENITIES])
        assertTrue(
            capacity(valid.copy(categories = emptySet(), amenities = emptySet(), services = emptyList())).isEmpty(),
        )
    }

    @Test
    fun servicesAreLimitedToOneHundredCharacters() {
        assertNull(capacity(valid.copy(services = listOf("a".repeat(100))))[VenueField.SERVICES])
        assertEquals(
            FieldProblem.TOO_LONG,
            capacity(valid.copy(services = listOf("a".repeat(101))))[VenueField.SERVICES],
        )
        assertEquals(FieldProblem.TOO_LONG, capacity(valid.copy(services = listOf(" ")))[VenueField.SERVICES])
    }

    @Test
    fun stepsOnlyCheckTheirOwnFields() {
        val broken = VenueDraft()
        assertTrue(details(broken).keys.all { VenueDraftValidator.stepOf(it) == VenueStep.DETAILS })
        assertTrue(capacity(broken).keys.all { VenueDraftValidator.stepOf(it) == VenueStep.CAPACITY_PRICE })
        val review = VenueDraftValidator.validate(broken, VenueStep.REVIEW)
        assertEquals(details(broken).keys + capacity(broken).keys, review.keys)
    }

    @Test
    fun stepOfPlacesEveryField() {
        assertEquals(VenueStep.DETAILS, VenueDraftValidator.stepOf(VenueField.ZONE))
        assertEquals(VenueStep.DETAILS, VenueDraftValidator.stepOf(VenueField.NAME))
        assertEquals(VenueStep.CAPACITY_PRICE, VenueDraftValidator.stepOf(VenueField.PRICE_UNIT))
        assertEquals(VenueStep.CAPACITY_PRICE, VenueDraftValidator.stepOf(VenueField.SERVICES))
    }

    @Test
    fun wireValuesMapToEnums() {
        assertEquals(PriceUnit.DAY, PriceUnit.fromWire("day"))
        assertEquals(PriceUnit.HOUR, PriceUnit.fromWire("hour"))
        assertEquals(PriceUnit.HOUR, PriceUnit.fromWire("event"))
        assertEquals(VenueStatus.DRAFT, VenueStatus.fromWire("draft"))
        assertEquals(VenueStatus.PUBLISHED, VenueStatus.fromWire("published"))
        assertEquals(VenueStatus.REVIEW, VenueStatus.fromWire("review"))
        assertEquals(VenueStatus.REVIEW, VenueStatus.fromWire("unknown"))
        assertEquals(VenueStatus.REVIEW, VenueStatus.fromWire(null))
    }
}
