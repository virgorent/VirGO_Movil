package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.domain.Gig
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createProvidersApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/** Rutas y cuerpos según los specs del backend `034`–`041` (especificados, aún sin implementar). */
class ProvidersRepositoryImplTest {
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

    private fun repository(): ProvidersRepositoryImpl {
        val tokens =
            object : AuthTokenProvider {
                override val token: Flow<String?> = flowOf("provider.token")
            }
        return ProvidersRepositoryImpl(
            createProvidersApi(server.url("/").toString(), createOkHttpClient(tokens, false)),
        )
    }

    @Test
    fun catalogMapsTheProviderSchemaAndFiltersByCategory() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[$PROVIDER]}"""))

            val provider = (repository().catalog(ProviderCategory.MUSIC) as AppResult.Success).value.single()

            assertEquals("DJ Sonido Norte", provider.businessName)
            assertEquals(ProviderCategory.MUSIC, provider.category)
            assertEquals(ProviderPricing.PER_EVENT, provider.pricing)
            assertEquals(BigDecimal("9000.0"), provider.priceFrom)
            assertEquals(listOf("https://f.mx/1.jpg", "https://f.mx/2.jpg"), provider.gallery)
            assertTrue(provider.published)
            assertEquals(4.5, provider.ratingAverage, 0.0)
            assertEquals("/api/v1/providers?category=M%C3%BAsica", server.takeRequest().path)
        }

    @Test
    fun missingEndpointIsComingSoon() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppError.NotFound, (repository().catalog() as AppResult.Failure).error)
            server.enqueue(MockResponse().setResponseCode(501))
            assertEquals(AppError.NotFound, (repository().provider("p-1") as AppResult.Failure).error)
        }

    @Test
    fun withoutProfileMineIsEmptyAndPublishSendsOnlyTheStatus() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            assertNull((repository().mine() as AppResult.Success).value)

            server.enqueue(MockResponse().setBody(PROVIDER))
            repository().publish()

            server.takeRequest()
            val request = server.takeRequest()
            assertEquals("PATCH", request.method)
            assertEquals("/api/v1/providers/me", request.path)
            val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals(setOf("status"), body.keys)
            assertEquals("published", body["status"]!!.jsonPrimitive.content)
        }

    @Test
    fun newGigIsPostedAndAnExistingOneIsPatched() =
        runTest {
            val gig =
                Gig(
                    eventName = "Boda de Ana",
                    date = LocalDate.parse("2027-05-11"),
                    start = LocalTime.parse("18:00"),
                    end = LocalTime.parse("22:00"),
                )
            server.enqueue(MockResponse().setBody(GIG))
            server.enqueue(MockResponse().setBody(GIG))

            val saved = (repository().saveGig(gig) as AppResult.Success).value
            repository().saveGig(saved)

            val created = server.takeRequest()
            assertEquals("POST", created.method)
            val body = Json.parseToJsonElement(created.body.readUtf8()).jsonObject
            assertEquals("18:00", body["start"]!!.jsonPrimitive.content)
            assertFalse(body.containsKey("id"))
            val updated = server.takeRequest()
            assertEquals("PATCH", updated.method)
            assertEquals("/api/v1/providers/me/gigs/g-1", updated.path)
        }
}

private const val PROVIDER =
    """{"id":"p-1","businessName":"DJ Sonido Norte","category":"Música","description":"Música para bodas.",""" +
        """"pricing":"per_event","responseTime":"Menos de 2 h","packages":[{"id":"k1","name":"Básico","price":9000,""" +
        """"includes":"5 horas"},{"id":"k2","name":"Plus","price":14000}],"gallery":[{"url":"https://f.mx/2.jpg",""" +
        """"order":1},{"url":"https://f.mx/1.jpg","order":0}],"zones":["Centro"],"status":"published",""" +
        """"rating":{"average":4.5,"count":8}}"""

private const val GIG =
    """{"id":"g-1","eventName":"Boda de Ana","clientName":"","place":"","date":"2027-05-11",""" +
        """"start":"18:00","end":"22:00"}"""
