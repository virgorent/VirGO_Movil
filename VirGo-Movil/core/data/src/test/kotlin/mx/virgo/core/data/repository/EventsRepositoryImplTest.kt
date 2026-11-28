package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.domain.EventInput
import mx.virgo.core.domain.GuestInput
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.TaskInput
import mx.virgo.core.network.createEventPlanningApi
import mx.virgo.core.network.createEventsApi
import mx.virgo.core.network.createOkHttpClient
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

/** Forma de `EventsController` de `feat/api-mobile-priority` (`7cf6124`). */
class EventsRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var events: EventsRepositoryImpl
    private lateinit var planning: EventPlanningRepositoryImpl

    private val input =
        EventInput("Boda de Ana", QuoteEventType.WEDDING, LocalDate.parse("2027-05-15"), 120, BigDecimal("150000.5"), 6)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val tokens =
            object : AuthTokenProvider {
                override val token: Flow<String?> = flowOf("client.token")
            }
        val client = createOkHttpClient(tokens, isDebug = false)
        val url = server.url("/").toString()
        events = EventsRepositoryImpl(createEventsApi(url, client))
        planning = EventPlanningRepositoryImpl(createEventPlanningApi(url, client))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun sentJson(): JsonObject = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject

    @Test
    fun createAndUpdateSendTheFullContractBody() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody(EVENT_BODY))
            server.enqueue(MockResponse().setBody(EVENT_BODY))

            val created = (events.create(input) as AppResult.Success).value
            assertEquals("Salón Las Palmas", created.venueName)
            assertEquals(QuoteEventType.WEDDING, created.eventType)
            val body = sentJson()
            assertEquals("Boda", body["eventType"]!!.jsonPrimitive.content)
            assertEquals("2027-05-15", body["date"]!!.jsonPrimitive.content)
            assertEquals(150000.5, body["budget"]!!.jsonPrimitive.content.toDouble(), 0.0)
            assertFalse(body.containsKey("venueId"))

            events.update("e1", input.copy(venueId = "v1"))
            val patch = server.takeRequest()
            assertEquals("PATCH", patch.method)
            assertEquals("/api/v1/events/e1", patch.path)
            val patchBody = Json.parseToJsonElement(patch.body.readUtf8()).jsonObject
            assertEquals("v1", patchBody["venueId"]!!.jsonPrimitive.content)
            assertEquals(6, patchBody["hours"]!!.jsonPrimitive.content.toInt())
        }

    @Test
    fun listAndDetailToleratePartialData() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[$EVENT_BODY,{"id":"bare"}]}"""))
            server.enqueue(MockResponse().setResponseCode(404))

            val list = (events.list() as AppResult.Success).value
            assertEquals(2, list.size)
            val bare = list[1]
            assertNull(bare.date)
            assertNull(bare.venueName)
            assertEquals(0, BigDecimal.ZERO.compareTo(bare.budget))
            assertEquals(AppResult.Failure(AppError.NotFound), events.get("x"))
        }

    @Test
    fun guestsAreReadCreatedUpdatedAndDeleted() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[$GUEST_BODY]}"""))
            server.enqueue(MockResponse().setResponseCode(201).setBody(GUEST_BODY))
            server.enqueue(MockResponse().setBody(GUEST_BODY))
            server.enqueue(MockResponse().setResponseCode(204))
            server.enqueue(MockResponse().setResponseCode(404))

            val guest = (planning.guests("e1") as AppResult.Success).value.single()
            assertEquals(GuestStatus.CONFIRMED, guest.status)
            assertEquals("Mesa 3", guest.table)
            assertEquals("/api/v1/events/e1/guests", server.takeRequest().path)

            val guestInput = GuestInput("Ana", "4421234567", 1, GuestStatus.PENDING, null)
            planning.saveGuest("e1", null, guestInput)
            val created = sentJson()
            assertEquals("pending", created["status"]!!.jsonPrimitive.content)
            assertFalse(created.containsKey("table"))
            planning.saveGuest("e1", "g1", guestInput)
            assertEquals("/api/v1/events/e1/guests/g1", server.takeRequest().path)
            assertEquals(AppResult.Success(Unit), planning.deleteGuest("e1", "g1"))
            assertEquals("DELETE", server.takeRequest().method)
            assertEquals(AppResult.Failure(AppError.NotFound), planning.deleteGuest("e1", "g1"))
        }

    @Test
    fun tasksAreReadCreatedUpdatedAndDeleted() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[$TASK_BODY]}"""))
            server.enqueue(MockResponse().setResponseCode(201).setBody(TASK_BODY))
            server.enqueue(MockResponse().setBody(TASK_BODY.replace("false", "true")))
            server.enqueue(MockResponse().setResponseCode(204))

            val task = (planning.tasks("e1") as AppResult.Success).value.single()
            assertEquals(LocalDate.parse("2027-02-01"), task.dueDate)
            server.takeRequest()
            planning.saveTask("e1", null, TaskInput("Fotógrafo", LocalDate.parse("2027-02-01"), false))
            val created = sentJson()
            assertEquals("2027-02-01", created["dueDate"]!!.jsonPrimitive.content)
            assertEquals("false", created["done"]!!.jsonPrimitive.content)
            val toggled =
                (
                    planning.saveTask(
                        "e1",
                        "t1",
                        TaskInput("Fotógrafo", LocalDate.parse("2027-02-01"), true),
                    ) as AppResult.Success
                ).value
            assertTrue(toggled.done)
            val patch = server.takeRequest()
            assertEquals("PATCH", patch.method)
            assertEquals("/api/v1/events/e1/tasks/t1", patch.path)
            assertEquals(AppResult.Success(Unit), planning.deleteTask("e1", "t1"))
        }

    @Test
    fun errorsAreMapped() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setBody("""{"title":"inválida","errors":{"phone":["phone es obligatorio."]}}"""),
            )
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse().setResponseCode(500))
            val validation =
                (
                    planning.saveGuest(
                        "e1",
                        null,
                        GuestInput("a", "1", 0, GuestStatus.PENDING, null),
                    ) as AppResult.Failure
                ).error as AppError.Validation
            assertEquals(listOf("phone es obligatorio."), validation.fields["phone"])
            assertEquals(AppResult.Failure(AppError.Unauthorized), events.list())
            assertTrue((planning.tasks("e1") as AppResult.Failure).error is AppError.Server)
        }
}

private const val EVENT_BODY =
    """{"id":"3f0c1d2e-0000-4000-8000-000000000001","ownerId":"c1","name":"Boda de Ana","eventType":"Boda",""" +
        """"date":"2027-05-15","guests":120,"budget":150000.5,"hours":6,""" +
        """"venue":{"venueId":"v1","name":"Salón Las Palmas","photoUrl":""},"design":null,"budgetPlan":null,""" +
        """"invitation":null,"reminders":[],"vendors":[],"externalPayments":[],"collaborators":[],""" +
        """"createdAt":"2026-11-25T18:00:00Z","updatedAt":"2026-11-25T18:00:00Z"}"""

private const val GUEST_BODY =
    """{"id":"g1","eventId":"e1","name":"María","phone":"4421234567",""" +
        """"companions":1,"status":"confirmed","table":"Mesa 3"}"""

private const val TASK_BODY =
    """{"id":"t1","eventId":"e1","title":"Fotógrafo","dueDate":"2027-02-01","done":false,"suggested":false}"""
