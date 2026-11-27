package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueSetups
import mx.virgo.core.network.createOwnerToolsApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/** Contrato §5 y §6. */
class OwnerToolsRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: OwnerToolsRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = OwnerToolsRepositoryImpl(createOwnerToolsApi(server.url("/").toString(), OkHttpClient()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun body() = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject

    @Test
    fun venuesReadTodaysPriceShapeAndNewFields() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """{"data":[{"id":"v1","name":"Cristal","status":"published","minimumHours":4,""" +
                        """"price":{"baseAmount":28000,"unit":"day","weekendPct":10},"setups":{"banquet":180}}]}""",
                ),
            )
            val venue = (repository.venues() as AppResult.Success).value.single()
            assertTrue(venue.published)
            assertEquals(4, venue.pricing.minHours)
            assertEquals(10, venue.pricing.weekendPct)
            assertEquals(180, venue.setups?.banquet)
        }

    @Test
    fun pricingSendsTheWholePriceAndSetupsAllThree() =
        runTest {
            server.enqueue(MockResponse().setBody("{}"))
            server.enqueue(MockResponse().setBody(SAVED))
            server.enqueue(MockResponse().setBody("{}"))
            server.enqueue(MockResponse().setBody(SAVED))
            val pricing = VenuePricing(BigDecimal("3500"), "hour", 5, 15, 20, setOf(12, 11))
            assertEquals(AppResult.Success(Unit), repository.savePricing("v1", pricing))
            assertEquals(AppResult.Success(Unit), repository.saveSetups("v1", VenueSetups(banquet = 200)))
            val price = body()["price"]!!.jsonObject
            assertEquals(5, price["minHours"]?.jsonPrimitive?.int)
            assertEquals(listOf(11, 12), price["highSeasonMonths"]?.jsonArray?.map { it.jsonPrimitive.int })
            assertEquals("/api/v1/venues/mine", server.takeRequest().path)
            val setups = body()["setups"]!!.jsonObject
            assertEquals(setOf("banquet", "auditorium", "cocktail"), setups.keys)
        }

    @Test
    fun okWithoutTheFieldPersistedIsComingSoonNotSaved() =
        runTest {
            val ignored = """{"data":[{"id":"v1","name":"Cristal","price":{"baseAmount":3500,"unit":"hour"}}]}"""
            server.enqueue(MockResponse().setBody("{}"))
            server.enqueue(MockResponse().setBody(ignored))
            server.enqueue(MockResponse().setBody("{}"))
            server.enqueue(MockResponse().setBody(ignored))
            val pricing = VenuePricing(BigDecimal("3500"), "hour", 5, 20, 0, emptySet())
            assertEquals(AppResult.Failure(AppError.NotFound), repository.savePricing("v1", pricing))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.saveSetups("v1", VenueSetups(banquet = 1)))
        }

    @Test
    fun reviewCarriesBookingAndTargetType() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"r1","stars":4,"text":"Bien"}"""))
            server.enqueue(MockResponse().setResponseCode(409))
            repository.create("v1", "b1", 4, " Bien ")
            val sent = body()
            assertEquals("venue", sent["targetType"]?.jsonPrimitive?.content)
            assertEquals("b1", sent["bookingId"]?.jsonPrimitive?.content)
            assertEquals("Bien", sent["text"]?.jsonPrimitive?.content)
            assertEquals(AppResult.Failure(AppError.Conflict), repository.create("v1", "b1", 4, "Bien"))
        }

    @Test
    fun patchWithoutTheFieldIsReportedAsValidation() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"title":"x"}"""))
            val result = repository.saveSetups("v1", VenueSetups())
            assertTrue((result as AppResult.Failure).error is AppError.Validation)
            assertFalse(result.error == AppError.NotFound)
        }
}

private const val SAVED =
    """{"data":[{"id":"v1","name":"Cristal","price":{"weekendPct":15},"setups":{"banquet":200}}]}"""
