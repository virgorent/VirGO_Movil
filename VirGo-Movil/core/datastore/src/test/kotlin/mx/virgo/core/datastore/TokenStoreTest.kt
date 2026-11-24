package mx.virgo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.AesGcmJce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.security.GeneralSecurityException
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TokenStoreTest {
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var tokenStore: TokenStoreImpl
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStoreFile = File(context.filesDir, "token_store_${System.nanoTime()}.preferences_pb")
        val aead: Aead = AesGcmJce(ByteArray(32) { it.toByte() })
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { dataStoreFile })
        tokenStore = TokenStoreImpl(dataStore = dataStore, aead = aead)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun saveThenReadReturnsSameToken() =
        runTest {
            assertNull(tokenStore.token.first())
            tokenStore.saveSession(storedSession())
            assertEquals("virgo-token-abc", tokenStore.token.first())
        }

    @Test
    fun clearRemovesToken() =
        runTest {
            tokenStore.saveSession(storedSession())
            tokenStore.clear()
            assertNull(tokenStore.token.first())
        }

    @Test
    fun fileDoesNotContainPlainTextToken() =
        runTest {
            val token = "virgo-token-abc"
            tokenStore.saveSession(storedSession())
            val raw = dataStoreFile.readBytes().decodeToString()
            assertFalse(raw.contains(token))
        }

    @Test
    fun unreadableValueReadsAsNull() =
        runTest {
            dataStore.edit { preferences -> preferences[TokenStoreImpl.TOKEN_KEY] = "!!!" }
            assertNull(tokenStore.token.first())
            dataStore.edit { preferences ->
                preferences[TokenStoreImpl.TOKEN_KEY] = Base64.getEncoder().encodeToString(ByteArray(16))
            }
            assertNull(tokenStore.token.first())
        }

    @Test
    fun legacyTokenWithoutMetadataCannotAuthenticate() =
        runTest {
            tokenStore.save("legacy-token")
            assertNull(tokenStore.token.first())
            assertNull(tokenStore.session.first())
        }

    @Test
    fun corruptSessionCannotExposeTokenOrIdentity() =
        runTest {
            tokenStore.saveSession(storedSession())
            dataStore.edit { preferences -> preferences[TokenStoreImpl.SESSION_KEY] = "!!!" }
            assertNull(tokenStore.token.first())
            assertNull(tokenStore.session.first())
        }

    @Test
    fun identityIsEncryptedAndRoundTripsWithToken() =
        runTest {
            val session = storedSession()
            tokenStore.saveSession(session)
            assertEquals(session, tokenStore.session.first())
            val raw = dataStoreFile.readBytes().decodeToString()
            assertFalse(raw.contains(session.account.accountId))
            assertFalse(raw.contains(session.account.email))
        }

    @Test
    fun failedEncryptionCannotPublishHalfOfANewSession() =
        runTest {
            tokenStore.saveSession(storedSession())
            val before = dataStore.data.first()[TokenStoreImpl.SESSION_KEY]
            val failingAead =
                object : Aead {
                    override fun encrypt(
                        plaintext: ByteArray,
                        associatedData: ByteArray,
                    ): ByteArray = throw GeneralSecurityException("Simulated encryption failure")

                    override fun decrypt(
                        ciphertext: ByteArray,
                        associatedData: ByteArray,
                    ): ByteArray = throw GeneralSecurityException("Simulated decryption failure")
                }
            val failingStore = TokenStoreImpl(dataStore, failingAead)
            try {
                failingStore.saveSession(storedSession().copy(accessToken = "replacement-test-token"))
                org.junit.Assert.fail("Expected encryption failure")
            } catch (expected: GeneralSecurityException) {
                assertNull(failingStore.token.first())
                assertNull(failingStore.session.first())
            }
            assertEquals(before, dataStore.data.first()[TokenStoreImpl.SESSION_KEY])
        }

    private fun storedSession(): StoredSession {
        val now = System.currentTimeMillis()
        return StoredSession(
            accessToken = "virgo-token-abc",
            account = StoredAccount("test-account-id", "Sample", "sample@example.test", "client"),
            issuedAtMillis = now,
            expiresAtMillis = now + StoredSession.MAX_ACCESS_LIFETIME_MILLIS,
        )
    }
}
