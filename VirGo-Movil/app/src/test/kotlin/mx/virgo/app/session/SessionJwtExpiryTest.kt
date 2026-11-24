package mx.virgo.app.session

import kotlinx.coroutines.test.runTest
import mx.virgo.core.testing.sampleAuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64

class SessionJwtExpiryTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-23T00:00:00Z"), ZoneOffset.UTC)
    private val store = FakeSessionTokenStore()
    private val repository = SessionRepository(store, clock)

    @Test
    fun jwtExpiryCanOnlyShortenDeclaredLifetime() =
        runTest {
            val earlier = clock.instant().epochSecond + SHORT_LIFETIME_SECONDS
            repository.establish(sampleAuthSession().copy(accessToken = jwt(earlier)))
            assertEquals(earlier * MILLIS_PER_SECOND, store.stored.value?.expiresAtMillis)
        }

    @Test
    fun unverifiedJwtCannotExtendDeclaredLifetime() =
        runTest {
            val later = clock.instant().epochSecond + LONG_LIFETIME_SECONDS
            repository.establish(sampleAuthSession().copy(accessToken = jwt(later)))
            val expected = clock.millis() + sampleAuthSession().expiresIn * MILLIS_PER_SECOND
            assertEquals(expected, store.stored.value?.expiresAtMillis)
        }

    @Test
    fun expiredOrMalformedJwtFailsClosed() =
        runTest {
            val expired = jwt(clock.instant().epochSecond)
            listOf(expired, "invalid.payload.signature").forEach { token ->
                assertFalse(repository.establish(sampleAuthSession().copy(accessToken = token)))
                assertNull(repository.state.value.session)
                assertNull(store.stored.value)
            }
        }

    private fun jwt(expirySeconds: Long): String {
        val payload = """{"exp":$expirySeconds}""".toByteArray(Charsets.UTF_8)
        return "test-header.${Base64.getUrlEncoder().withoutPadding().encodeToString(payload)}.test-signature"
    }
}

private const val SHORT_LIFETIME_SECONDS = 60L
private const val LONG_LIFETIME_SECONDS = 3_600L
private const val MILLIS_PER_SECOND = 1_000L
