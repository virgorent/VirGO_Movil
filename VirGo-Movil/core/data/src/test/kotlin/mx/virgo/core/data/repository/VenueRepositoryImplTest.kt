package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Venue
import mx.virgo.core.network.createVenuesApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VenueRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: VenueRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = VenueRepositoryImpl(createVenuesApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun catalogResponseMapsVenueFields() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(CATALOG_BODY))

            val result = repository.getVenues()

            val venues = (result as AppResult.Success).value
            assertEquals(1, venues.size)
            val venue = venues.first()
            assertEquals("uuid-1", venue.id)
            assertEquals("Casa VirGo", venue.name)
            assertEquals("Casa", venue.shortName)
            assertEquals("Centro", venue.zone)
            assertEquals(150, venue.capacity)
            assertEquals(4.5, venue.ratingAverage, 0.0)
            assertEquals(12, venue.ratingCount)
            assertEquals(20.7001, venue.latitude!!, 0.0001)
            assertEquals(-100.4455, venue.longitude!!, 0.0001)
            assertEquals("/api/v1/venues", server.takeRequest().path)
        }

    @Test
    fun queryParamsAreSentWhenProvided() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":[]}"""))

            repository.getVenues(zone = "Centro", category = "Elegante")

            assertEquals("/api/v1/venues?zone=Centro&category=Elegante", server.takeRequest().path)
        }

    @Test
    fun emptyCatalogReturnsEmptyList() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":[]}"""))

            val result = repository.getVenues()

            assertEquals(emptyList<Venue>(), (result as AppResult.Success).value)
        }

    @Test
    fun venueWithoutLocationKeepsNullCoordinates() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"data":[{"id":"uuid-2","name":"Sin Mapa"}]}"""),
            )

            val result = repository.getVenues()

            val venue = (result as AppResult.Success).value.single()
            assertNull(venue.latitude)
            assertNull(venue.longitude)
            assertEquals(0, venue.capacity)
            assertEquals(0.0, venue.ratingAverage, 0.0)
        }

    @Test
    fun notFoundResponseMapsToNotFound() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"title":"Not Found","status":404}"""))

            val result = repository.getVenues()

            assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
        }

    @Test
    fun serverErrorResponseMapsToServerError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500).setBody("""{"title":"Error","status":500}"""))

            val result = repository.getVenues()

            assertTrue((result as AppResult.Failure).error is AppError.Server)
        }

    @Test
    fun noConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.getVenues()

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }
}

private val CATALOG_BODY =
    """
    {"data":[{"id":"uuid-1","name":"Casa VirGo","shortName":"Casa","zone":"Centro","capacity":150,
    "rating":{"average":4.5,"count":12},"location":{"type":"Point","coordinates":[-100.4455,20.7001]}}]}
    """.trimIndent()
