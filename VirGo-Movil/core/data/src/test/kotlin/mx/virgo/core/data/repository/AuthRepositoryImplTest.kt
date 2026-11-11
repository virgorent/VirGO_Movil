package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createAuthApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = AuthRepositoryImpl(createAuthApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun successResponseMapsToAuthSession() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

            val result = repository.login("ana@ejemplo.mx", "secret1", "Android 14")

            val session = (result as AppResult.Success).value
            assertEquals("eyJhbGciOiJIUzI1NiIs", session.accessToken)
            assertEquals("Bearer", session.tokenType)
            assertEquals(900L, session.expiresIn)
            assertEquals("q3Jx-refresh-token", session.refreshToken)
            assertEquals("2026-12-03T18:00:00Z", session.refreshTokenExpiresAt)
            assertEquals("0b6c5d7e-2f1a-4c3b-9e8d-7a6b5c4d3e2f", session.user.id)
            assertEquals("Ana Lopez", session.user.name)
            assertEquals("ana@ejemplo.mx", session.user.email)
            assertEquals("client", session.user.role)

            val request = server.takeRequest()
            assertEquals("/api/v1/auth/login", request.path)
            val body = request.body.readUtf8()
            assertTrue(body.contains("ana@ejemplo.mx"))
            assertTrue(body.contains("secret1"))
            assertTrue(body.contains("Android 14"))
        }

    @Test
    fun unauthorizedResponseMapsToUnauthorizedError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(401).setBody(PROBLEM_401))

            val result = repository.login("ana@ejemplo.mx", "wrong")

            assertEquals(AppError.Unauthorized, (result as AppResult.Failure).error)
        }

    @Test
    fun validationResponseMapsToValidationWithErrorFields() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400).setBody(PROBLEM_400))

            val result = repository.login("no-es-correo", "secret1")

            val error = (result as AppResult.Failure).error as AppError.Validation
            assertEquals(listOf("El correo no tiene un formato valido."), error.fields["email"])
        }

    @Test
    fun serverErrorResponseMapsToServerWithTraceId() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500).setBody(PROBLEM_500))

            val result = repository.login("ana@ejemplo.mx", "secret1")

            assertEquals(AppError.Server("trace-abc-123"), (result as AppResult.Failure).error)
        }

    @Test
    fun notFoundResponseMapsToNotFound() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404).setBody(PROBLEM_404))

            val result = repository.login("ana@ejemplo.mx", "secret1")

            assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
        }

    @Test
    fun shutdownServerMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.login("ana@ejemplo.mx", "secret1")

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }

    @Test
    fun slowResponseMapsToTimeoutError() =
        runTest {
            val slowClient =
                OkHttpClient
                    .Builder()
                    .connectTimeout(100, TimeUnit.MILLISECONDS)
                    .readTimeout(100, TimeUnit.MILLISECONDS)
                    .build()
            val slowRepository = AuthRepositoryImpl(createAuthApi(server.url("/").toString(), slowClient))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

            val result = slowRepository.login("ana@ejemplo.mx", "secret1")

            assertEquals(AppError.Timeout, (result as AppResult.Failure).error)
        }

    @Test
    fun malformedSuccessBodyMapsToUnknownError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))

            val result = repository.login("ana@ejemplo.mx", "secret1")

            assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
        }

    private companion object {
        const val SUCCESS_BODY =
            """{"accessToken":"eyJhbGciOiJIUzI1NiIs","tokenType":"Bearer","expiresIn":900,""" +
                """"refreshToken":"q3Jx-refresh-token","refreshTokenExpiresAt":"2026-12-03T18:00:00Z",""" +
                """"user":{"id":"0b6c5d7e-2f1a-4c3b-9e8d-7a6b5c4d3e2f","name":"Ana Lopez",""" +
                """"email":"ana@ejemplo.mx","role":"client"}}"""
        const val PROBLEM_401 =
            """{"type":"https://tools.ietf.org/html/rfc9110#section-15.5.2",""" +
                """"title":"Correo o contrasena incorrectos.","status":401,"traceId":"trace-401"}"""
        const val PROBLEM_400 =
            """{"title":"La solicitud tiene datos invalidos.","status":400,"traceId":"trace-400",""" +
                """"errors":{"email":["El correo no tiene un formato valido."]}}"""
        const val PROBLEM_500 =
            """{"title":"Ocurrio un problema al procesar tu solicitud.","status":500,""" +
                """"traceId":"trace-abc-123"}"""
        const val PROBLEM_404 = """{"title":"Recurso no encontrado.","status":404}"""
    }
}
