package mx.virgo.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SessionCodecTest {
    private val issued = 1_000_000L
    private val session =
        StoredSession(
            accessToken = "test-access",
            account = StoredAccount("account", "Sample", "sample@example.test", "owner"),
            issuedAtMillis = issued,
            expiresAtMillis = issued + StoredSession.MAX_ACCESS_LIFETIME_MILLIS,
        )

    @Test
    fun roundTripPreservesTokenMetadataAndExpiryAsOneRecord() {
        assertEquals(session, SessionCodec.decode(SessionCodec.encode(session)))
    }

    @Test(expected = IOException::class)
    fun truncatedRecordIsRejected() {
        val encoded = SessionCodec.encode(session)
        SessionCodec.decode(encoded.copyOf(encoded.size - 1))
    }

    @Test(expected = IOException::class)
    fun trailingDataIsRejected() {
        SessionCodec.decode(SessionCodec.encode(session) + byteArrayOf(1))
    }

    @Test
    fun expiresAtBoundaryAndRejectsClockRollback() {
        assertTrue(session.isValidAt(issued))
        assertFalse(session.isValidAt(issued - 1))
        assertFalse(session.isValidAt(session.expiresAtMillis))
    }

    @Test
    fun rejectsInvalidLifetimeAndUnknownRole() {
        assertFalse(session.copy(expiresAtMillis = Long.MAX_VALUE).isValidAt(issued))
        assertFalse(session.copy(expiresAtMillis = issued).isValidAt(issued))
        assertFalse(session.copy(account = session.account.copy(role = "admin")).isValidAt(issued))
    }

    @Test
    fun sessionStringRepresentationDoesNotExposeCredentialsOrIdentity() {
        assertFalse(session.toString().contains(session.accessToken))
        assertFalse(session.toString().contains(session.account.email))
        assertFalse(session.account.toString().contains(session.account.accountId))
    }
}
