package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.ExternalPayment
import mx.virgo.core.domain.PaymentStatus
import mx.virgo.core.network.createEventsApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

/** Contrato §4: `budgetPlan` y `externalPayments` en `GET`/`PATCH /events/:id`. */
class EventFinanceRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: EventFinanceRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = EventFinanceRepositoryImpl(createEventsApi(server.url("/").toString(), OkHttpClient()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun currentReadsPlanAndPaymentsOfTheFirstEvent() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[{"id":"e1","name":"Boda","budget":150000}]}"""))
            server.enqueue(
                MockResponse().setBody(
                    """{"id":"e1","name":"Boda","budget":150000,"budgetPlan":{"Salón":45000,"Raro":1},""" +
                        """"externalPayments":[{"id":"p1","concept":"DJ","amount":3000,"status":"completed",""" +
                        """"paidAt":"2026-11-20T00:00:00Z"}]}""",
                ),
            )
            val finance = (repository.current() as AppResult.Success).value!!
            assertEquals(mapOf(BudgetCategory.VENUE to BigDecimal.valueOf(45000.0)), finance.plan)
            assertEquals(LocalDate.parse("2026-11-20"), finance.payments.single().paidAt)
            server.takeRequest()
            assertEquals("/api/v1/events/e1", server.takeRequest().path)
        }

    @Test
    fun withoutEventsThereIsNothingToPlan() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[]}"""))
            assertNull((repository.current() as AppResult.Success).value)
        }

    @Test
    fun savingSendsOnlyTheChangedField() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"id":"e1"}"""))
            server.enqueue(MockResponse().setBody("""{"id":"e1"}"""))
            repository.savePlan("e1", mapOf(BudgetCategory.CATERING to BigDecimal("52500")))
            val payment =
                ExternalPayment("p1", " DJ ", BigDecimal("3000"), PaymentStatus.PENDING, LocalDate.parse("2026-11-20"))
            repository.savePayments("e1", listOf(payment))
            val plan = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals(
                52500.0,
                plan["budgetPlan"]
                    ?.jsonObject
                    ?.get("Catering")
                    ?.jsonPrimitive
                    ?.double,
            )
            assertFalse("externalPayments" in plan)
            val sent = server.takeRequest()
            assertEquals("PATCH", sent.method)
            val body = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            val first = body["externalPayments"]?.jsonArray?.single()?.jsonObject
            assertEquals("DJ", first?.get("concept")?.jsonPrimitive?.content)
            assertEquals("pending", first?.get("status")?.jsonPrimitive?.content)
            assertFalse("budgetPlan" in body)
        }
}
