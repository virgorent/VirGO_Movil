package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createAuthApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecoveryRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RecoveryRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = RecoveryRepositoryImpl(createAuthApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun forgotAcceptedResponseReturnsSuccessAndPostsEmail() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(202))

            val result = repository.forgotPassword("ana@ejemplo.mx")

            assertEquals(AppResult.Success(Unit), result)
            val request = server.takeRequest()
            assertEquals("/api/v1/auth/password/forgot", request.path)
            assertTrue(request.body.readUtf8().contains("""{"email":"ana@ejemplo.mx"}"""))
        }

    @Test
    fun forgotValidationResponseMapsToValidationWithErrorFields() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400).setBody(PROBLEM_400))

            val result = repository.forgotPassword("no-es-correo")

            val error = (result as AppResult.Failure).error as AppError.Validation
            assertEquals(listOf("El correo no tiene un formato valido."), error.fields["email"])
        }

    @Test
    fun forgotRateLimitResponseMapsToServerError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(429).setBody(PROBLEM_429))

            val result = repository.forgotPassword("ana@ejemplo.mx")

            assertEquals(AppError.Server("trace-429"), (result as AppResult.Failure).error)
        }

    @Test
    fun forgotServerErrorResponseMapsToServerError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500).setBody(PROBLEM_500))

            val result = repository.forgotPassword("ana@ejemplo.mx")

            assertEquals(AppError.Server("trace-abc-123"), (result as AppResult.Failure).error)
        }

    @Test
    fun forgotWithoutConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.forgotPassword("ana@ejemplo.mx")

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }

    @Test
    fun resetSuccessResponseReturnsSuccessAndPostsCodeAndPassword() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200))

            val result = repository.resetPassword("123456", "password1")

            assertEquals(AppResult.Success(Unit), result)
            val request = server.takeRequest()
            assertEquals("/api/v1/auth/password/reset", request.path)
            val body = request.body.readUtf8()
            assertTrue(body.contains(""""code":"123456""""))
            assertTrue(body.contains(""""password":"password1""""))
        }

    @Test
    fun resetWrongCodeResponseMapsToGenericValidation() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400).setBody(PROBLEM_400_GENERIC))

            val result = repository.resetPassword("000000", "password1")

            assertEquals(AppError.Validation(emptyMap()), (result as AppResult.Failure).error)
        }

    @Test
    fun resetWithoutConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.resetPassword("123456", "password1")

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }

    private companion object {
        const val PROBLEM_400 =
            """{"title":"La solicitud tiene datos invalidos.","status":400,"traceId":"trace-400",""" +
                """"errors":{"email":["El correo no tiene un formato valido."]}}"""
        const val PROBLEM_400_GENERIC =
            """{"title":"La solicitud tiene datos invalidos.","status":400,"traceId":"trace-400"}"""
        const val PROBLEM_429 =
            """{"title":"Demasiadas solicitudes.","status":429,"traceId":"trace-429"}"""
        const val PROBLEM_500 =
            """{"title":"Ocurrio un problema al procesar tu solicitud.","status":500,""" +
                """"traceId":"trace-abc-123"}"""
    }
}
