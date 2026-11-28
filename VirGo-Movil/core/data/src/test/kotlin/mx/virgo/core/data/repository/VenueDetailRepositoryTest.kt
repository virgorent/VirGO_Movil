package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createVenuesApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Forma de `GET /venues/:id` según `VenuesController.Detail` de `feat/api-mobile-priority` (`7cf6124`). */
class VenueDetailRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repository(token: String? = null): VenueRepositoryImpl {
        val tokens =
            object : AuthTokenProvider {
                override val token: Flow<String?> = flowOf(token)
            }
        val client =
            createOkHttpClient(tokens, isDebug = false)
                .newBuilder()
                .readTimeout(DETAIL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
        return VenueRepositoryImpl(createVenuesApi(server.url("/").toString(), client))
    }

    @Test
    fun fullDetailWithSessionMapsPriceAndSendsBearer() =
        runTest {
            server.enqueue(MockResponse().setBody(DETAIL_WITH_PRICE))

            val venue = (repository("client.token").venue(VENUE_ID) as AppResult.Success).value

            assertEquals("Salón Las Palmas", venue.name)
            assertEquals("Av. Juriquilla 100, Querétaro", venue.address)
            assertEquals(220, venue.capacity)
            assertEquals(4, venue.minimumHours)
            assertEquals(1600.0, venue.priceBase!!, 0.0)
            assertEquals(PriceUnit.DAY, venue.priceUnit)
            assertEquals(listOf("Estacionamiento", "Sonido"), venue.amenities)
            assertFalse(venue.verified)
            assertEquals(listOf("https://f.mx/1.jpg", "https://f.mx/2.jpg"), venue.photos)
            val request = server.takeRequest()
            assertEquals("/api/v1/venues/$VENUE_ID", request.path)
            assertEquals("Bearer client.token", request.getHeader("Authorization"))
        }

    @Test
    fun anonymousDetailHasNoPriceAndNoBearer() =
        runTest {
            server.enqueue(MockResponse().setBody(DETAIL_ANONYMOUS))

            val venue = (repository().venue(VENUE_ID) as AppResult.Success).value

            assertNull(venue.priceBase)
            assertNull(venue.priceUnit)
            assertNull(server.takeRequest().getHeader("Authorization"))
        }

    @Test
    fun minimalDetailToleratesMissingFields() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"id":"$VENUE_ID","name":"Solo nombre","description":" "}"""))

            val venue = (repository().venue(VENUE_ID) as AppResult.Success).value

            assertEquals(0, venue.capacity)
            assertNull(venue.description)
            assertNull(venue.zone)
            assertTrue(venue.categories.isEmpty())
            assertEquals(0, venue.ratingCount)
        }

    @Test
    fun unknownPriceUnitIsNotGuessed() =
        runTest {
            server.enqueue(
                MockResponse().setBody("""{"id":"$VENUE_ID","name":"X","price":{"baseAmount":10,"unit":"event"}}"""),
            )

            assertNull((repository("t").venue(VENUE_ID) as AppResult.Success).value.priceUnit)
        }

    @Test
    fun errorsMapToAppErrors() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppResult.Failure(AppError.NotFound), repository().venue(VENUE_ID))
            server.enqueue(MockResponse().setResponseCode(500).setBody("""{"traceId":"t-9"}"""))
            assertEquals(AppResult.Failure(AppError.Server("t-9")), repository().venue(VENUE_ID))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            assertEquals(AppResult.Failure(AppError.Timeout), repository().venue(VENUE_ID))
        }
}

private const val VENUE_ID = "132a1f58-ebf9-4082-acdf-7241975f1811"
private const val DETAIL_TIMEOUT_MS = 300L

private const val DETAIL_ANONYMOUS =
    """{"id":"$VENUE_ID","name":"Salón Las Palmas","shortName":"Las Palmas",""" +
        """"description":"Salón con jardín para 200 invitados en Juriquilla.",""" +
        """"address":"Av. Juriquilla 100, Querétaro","zone":"Juriquilla","capacity":220,"minimumHours":4,""" +
        """"categories":["Salon","Jardin"],"amenities":["Estacionamiento","Sonido"],""" +
        """"photos":["https://f.mx/1.jpg","https://f.mx/2.jpg"],""" +
        """"rating":{"average":0,"count":0}}"""

private const val DETAIL_WITH_PRICE =
    """{"id":"$VENUE_ID","name":"Salón Las Palmas","shortName":"Las Palmas",""" +
        """"description":"Salón con jardín para 200 invitados en Juriquilla.",""" +
        """"address":"Av. Juriquilla 100, Querétaro","zone":"Juriquilla","capacity":220,"minimumHours":4,""" +
        """"categories":["Salon","Jardin"],"amenities":["Estacionamiento","Sonido"],""" +
        """"photos":["https://f.mx/1.jpg","https://f.mx/2.jpg"],""" +
        """"rating":{"average":0,"count":0},"price":{"baseAmount":1600,"unit":"day"}}"""
