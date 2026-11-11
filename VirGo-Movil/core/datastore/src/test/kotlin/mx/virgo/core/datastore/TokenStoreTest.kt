package mx.virgo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.AesGcmJce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TokenStoreTest {
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var tokenStore: TokenStoreImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStoreFile = File(context.filesDir, "token_store.preferences_pb")
        val aead: Aead = AesGcmJce(ByteArray(32) { it.toByte() })
        dataStore = PreferenceDataStoreFactory.create(produceFile = { dataStoreFile })
        tokenStore = TokenStoreImpl(dataStore = dataStore, aead = aead)
    }

    @Test
    fun saveThenReadReturnsSameToken() =
        runTest {
            assertNull(tokenStore.token.first())
            tokenStore.save("virgo-token-abc")
            assertEquals("virgo-token-abc", tokenStore.token.first())
        }

    @Test
    fun clearRemovesToken() =
        runTest {
            tokenStore.save("virgo-token-abc")
            tokenStore.clear()
            assertNull(tokenStore.token.first())
        }

    @Test
    fun fileDoesNotContainPlainTextToken() =
        runTest {
            val token = "virgo-token-abc"
            tokenStore.save(token)
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
}
