package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.network.createAuthApi
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createUsersApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AccountRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AccountRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val tokens =
            object : AuthTokenProvider {
                override val token: Flow<String?> = flowOf(TOKEN)
            }
        val client = createOkHttpClient(tokens, isDebug = false)
        val baseUrl = server.url("/").toString()
        repository = AccountRepositoryImpl(createUsersApi(baseUrl, client), createAuthApi(baseUrl, client))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun profileMapsServerFieldsAndSendsBearer() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(PROFILE_BODY))

            val profile = (repository.getProfile() as AppResult.Success).value

            assertEquals("0b6c5d7e-2f1a-4c3b-9e8d-7a6b5c4d3e2f", profile.id)
            assertEquals("Ana Lopez", profile.name)
            assertEquals("ana@ejemplo.mx", profile.email)
            assertEquals("+525512345678", profile.phone)
            assertEquals("owner", profile.role)
            assertTrue(profile.isVerified)
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/api/v1/users/me", request.path)
            assertEquals("Bearer $TOKEN", request.getHeader("Authorization"))
        }

    @Test
    fun blankPhoneIsNotInvented() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(PROFILE_BODY.replace("+525512345678", "")))

            assertNull((repository.getProfile() as AppResult.Success).value.phone)
        }

    @Test
    fun profileUnauthorizedAndNotFoundAreDistinct() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse().setResponseCode(404))

            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.getProfile())
            assertEquals(AppResult.Failure(AppError.NotFound), repository.getProfile())
        }

    @Test
    fun profileServerErrorKeepsTraceId() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setHeader("Content-Type", "application/problem+json")
                    .setBody("""{"title":"Error","status":500,"traceId":"trace-9"}"""),
            )

            assertEquals(AppResult.Failure(AppError.Server("trace-9")), repository.getProfile())
        }

    @Test
    fun logoutAccepts204And200() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(204))
            server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

            assertEquals(AppResult.Success(Unit), repository.logout())
            assertEquals(AppResult.Success(Unit), repository.logout())
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/auth/logout", request.path)
            assertEquals("Bearer $TOKEN", request.getHeader("Authorization"))
        }

    @Test
    fun logoutUnauthorizedAndServerErrorsAreReported() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse().setResponseCode(503))

            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.logout())
            assertTrue((repository.logout() as AppResult.Failure).error is AppError.Server)
        }

    @Test
    fun logoutTimeoutMapsToTimeout() =
        runTest {
            val fastClient = OkHttpClient.Builder().readTimeout(SHORT_TIMEOUT_MS, TimeUnit.MILLISECONDS).build()
            val baseUrl = server.url("/").toString()
            val fast = AccountRepositoryImpl(createUsersApi(baseUrl, fastClient), createAuthApi(baseUrl, fastClient))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

            assertEquals(AppResult.Failure(AppError.Timeout), fast.logout())
        }

    @Test
    fun logoutWithoutNetworkMapsToNetwork() =
        runTest {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

            assertEquals(AppResult.Failure(AppError.Network), repository.logout())
        }
}

private const val TOKEN = "header.payload.signature"
private const val SHORT_TIMEOUT_MS = 200L
private const val PROFILE_BODY =
    """{"id":"0b6c5d7e-2f1a-4c3b-9e8d-7a6b5c4d3e2f","name":"Ana Lopez","email":"ana@ejemplo.mx",""" +
        """"phone":"+525512345678","role":"owner","isVerified":true,"extra":"ignored"}"""
