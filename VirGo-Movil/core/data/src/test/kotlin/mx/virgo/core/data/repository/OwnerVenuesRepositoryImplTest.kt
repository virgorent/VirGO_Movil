package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueDraft
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createOwnerVenuesApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/** Cuerpos copiados de respuestas reales del backend `feat/api-mobile-priority` en local (2026-11-25). */
class OwnerVenuesRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: OwnerVenuesRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = repositoryWith(createOkHttpClient(tokens, isDebug = false))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repositoryWith(client: OkHttpClient) =
        OwnerVenuesRepositoryImpl(createOwnerVenuesApi(server.url("/").toString(), client))

    private fun body(): JsonObject = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject

    @Test
    fun mineMapsTheRealResponse() =
        runTest {
            server.enqueue(MockResponse().setBody(MINE_BODY))

            val venue = (repository.mine() as AppResult.Success).value.single()

            assertEquals("132a1f58-ebf9-4082-acdf-7241975f1811", venue.id)
            assertEquals(VenueStatus.DRAFT, venue.status)
            assertEquals("Juriquilla", venue.draft.zone)
            assertEquals(220, venue.draft.capacity)
            assertEquals(0, BigDecimal("1600").compareTo(venue.draft.priceBase))
            assertEquals(PriceUnit.DAY, venue.draft.priceUnit)
            assertEquals(1, venue.draft.minHours)
            assertEquals(setOf("Salon", "Jardin"), venue.draft.categories)
            assertTrue(venue.draft.services.isEmpty())
            val request = server.takeRequest()
            assertEquals("/api/v1/venues/mine", request.path)
            assertEquals("Bearer $OWNER_TOKEN", request.getHeader("Authorization"))
        }

    @Test
    fun createSendsTheServerContractOnlyOnce() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_BODY))

            val created = repository.create(sampleVenueDraft())

            assertEquals(VenueStatus.DRAFT, (created as AppResult.Success).value.status)
            server.takeRequest()
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            val json = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals("Juriquilla", json["zone"]!!.jsonPrimitive.content)
            val price = json["price"]!!.jsonObject
            assertEquals("hour", price["unit"]!!.jsonPrimitive.content)
            assertEquals(4, price["minHours"]!!.jsonPrimitive.content.toInt())
            val service = json["services"]!!.jsonArray.single().jsonObject
            assertEquals("Mobiliario", service["name"]!!.jsonPrimitive.content)
            assertEquals("true", service["included"]!!.jsonPrimitive.content)
            listOf("status", "ownerId", "rating", "verified", "photos", "location").forEach {
                assertFalse("no debe enviar $it", json.containsKey(it))
            }
            assertEquals(2, server.requestCount)
        }

    @Test
    fun createSendsEmptyListsWhenNothingIsSelected() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            server.enqueue(MockResponse().setResponseCode(201).setBody(CREATED_BODY))

            repository.create(
                sampleVenueDraft().copy(categories = emptySet(), amenities = emptySet(), services = emptyList()),
            )

            server.takeRequest()
            val json = body()
            listOf("services", "categories", "amenities").forEach { assertTrue(json[it]!!.jsonArray.isEmpty()) }
        }

    @Test
    fun updateOmitsServicesWhenNotIncluded() =
        runTest {
            server.enqueue(MockResponse().setBody(CREATED_BODY))

            repository.update("v1", sampleVenueDraft(), includeServices = false)

            val request = server.takeRequest()
            assertEquals("PATCH", request.method)
            assertEquals("/api/v1/venues/v1", request.path)
            val json = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertFalse(json.containsKey("services"))
            assertTrue(json.containsKey("price"))
        }

    @Test
    fun publishReadsTheNewStatus() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"id":"v1","status":"published"}"""))

            assertEquals(AppResult.Success(VenueStatus.PUBLISHED), repository.publish("v1"))
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/venues/v1/publish", request.path)
        }

    @Test
    fun validationErrorsKeepServerFieldKeys() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            server.enqueue(MockResponse().setResponseCode(400).setBody(VALIDATION_BODY))

            val error = (repository.create(sampleVenueDraft()) as AppResult.Failure).error as AppError.Validation

            assertEquals(setOf("description", "zone", "capacity", "price.unit"), error.fields.keys)
        }

    @Test
    fun statusCodesMapToErrors() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(401))
            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.mine())
            server.enqueue(MockResponse().setResponseCode(403))
            assertEquals(AppResult.Failure(AppError.Forbidden), repository.publish("v1"))
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.update("v1", sampleVenueDraft(), true))
            server.enqueue(MockResponse().setResponseCode(500).setBody("""{"traceId":"t-1"}"""))
            assertEquals(AppResult.Failure(AppError.Server("t-1")), repository.mine())
        }

    @Test
    fun unansweredCreateRecoversTheVenueInsteadOfDuplicating() =
        runTest {
            val fast = repositoryWith(fastClient())
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            server.enqueue(MockResponse().setBody(MINE_BODY))

            val recovered = fast.create(MINE_DRAFT)

            assertEquals("132a1f58-ebf9-4082-acdf-7241975f1811", (recovered as AppResult.Success).value.id)
            assertEquals(3, server.requestCount)
        }

    @Test
    fun unansweredCreateWithoutMatchKeepsTheTimeout() =
        runTest {
            val fast = repositoryWith(fastClient())
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))

            assertEquals(AppResult.Failure(AppError.Timeout), fast.create(sampleVenueDraft()))
        }

    private fun fastClient(): OkHttpClient =
        createOkHttpClient(tokens, isDebug = false)
            .newBuilder()
            .readTimeout(FAST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build()

    private val tokens =
        object : AuthTokenProvider {
            override val token: Flow<String?> = flowOf(OWNER_TOKEN)
        }
}

private const val OWNER_TOKEN = "owner.header.signature"
private const val FAST_TIMEOUT_MS = 300L

private fun sampleVenueDraft(): VenueDraft =
    VenueDraft(
        name = "Salón Uno",
        shortName = "Uno",
        description = "Salón con jardín para 200 invitados.",
        address = "Centro 1",
        zone = "Juriquilla",
        capacity = 200,
        priceBase = BigDecimal("1500"),
        priceUnit = PriceUnit.HOUR,
        minHours = 4,
        categories = setOf("Salon", "Jardin"),
        amenities = setOf("Estacionamiento"),
        services = listOf("Mobiliario"),
    )

private val MINE_DRAFT =
    sampleVenueDraft().copy(
        name = "Salón Las Palmas",
        shortName = "Las Palmas",
        address = "Av. Juriquilla 100, Querétaro",
    )

private const val MINE_BODY =
    """{"data":[{"id":"132a1f58-ebf9-4082-acdf-7241975f1811","name":"Salón Las Palmas","shortName":"Las Palmas",""" +
        """"description":"Salón con jardín para 200 invitados en Juriquilla.",""" +
        """"address":"Av. Juriquilla 100, Querétaro",""" +
        """"zone":"Juriquilla","capacity":220,"minimumHours":1,"price":{"baseAmount":1600,"unit":"day"},""" +
        """"categories":["Salon","Jardin"],"amenities":["Estacionamiento","Sonido"],"photos":[],"status":"draft",""" +
        """"rating":{"average":0,"count":0}}]}"""

private const val CREATED_BODY =
    """{"id":"132a1f58-ebf9-4082-acdf-7241975f1811","name":"Salón Las Palmas","shortName":"Las Palmas",""" +
        """"description":"Salón con jardín para 200 invitados en Juriquilla.",""" +
        """"address":"Av. Juriquilla 100, Querétaro",""" +
        """"zone":"Juriquilla","capacity":200,"minimumHours":4,"price":{"baseAmount":1500,"unit":"hour"},""" +
        """"categories":["Salon","Jardin"],"amenities":["Estacionamiento","Sonido"],"photos":[],"status":"draft",""" +
        """"rating":{"average":0,"count":0}}"""

private const val VALIDATION_BODY =
    """{"title":"La solicitud tiene datos inválidos.","status":400,"instance":"/api/v1/venues",""" +
        """"errors":{"description":["El campo debe tener entre 20 y 2000 caracteres."],""" +
        """"zone":["La zona no es válida."],""" +
        """"capacity":["La capacidad debe ser al menos 1."],"price.unit":["La unidad debe ser hour o day."]},""" +
        """"traceId":"0HNQB2BHJU21C:00000001"}"""
