package mx.virgo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Base64
import javax.inject.Inject

class TokenStoreImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val aead: Aead,
    ) : TokenStore {
        @Volatile
        private var blocked = false

        override val session: Flow<StoredSession?> =
            dataStore.data.map { preferences ->
                if (blocked) null else readSession(preferences[SESSION_KEY])
            }

        override val token: Flow<String?> =
            session.map { it?.accessToken }

        override suspend fun saveSession(session: StoredSession) {
            blocked = true
            require(session.isValidAt(System.currentTimeMillis())) { "Invalid session" }
            val ciphertext = aead.encrypt(SessionCodec.encode(session), SESSION_AAD)
            val encoded = Base64.getEncoder().encodeToString(ciphertext)
            dataStore.edit { preferences ->
                preferences.remove(TOKEN_KEY)
                preferences[SESSION_KEY] = encoded
            }
            blocked = false
        }

        override suspend fun save(token: String) {
            val ciphertext = aead.encrypt(token.toByteArray(Charsets.UTF_8), ByteArray(0))
            val encoded = Base64.getEncoder().encodeToString(ciphertext)
            dataStore.edit { preferences ->
                preferences.remove(SESSION_KEY)
                preferences[TOKEN_KEY] = encoded
            }
            blocked = false
        }

        override suspend fun clear() {
            blocked = true
            dataStore.edit { preferences ->
                preferences.remove(TOKEN_KEY)
                preferences.remove(SESSION_KEY)
            }
        }

        private fun readSession(encoded: String?): StoredSession? {
            if (encoded == null) return null
            return try {
                val plaintext = aead.decrypt(Base64.getDecoder().decode(encoded), SESSION_AAD)
                SessionCodec.decode(plaintext).takeIf { it.isValidAt(System.currentTimeMillis()) }
            } catch (ignored: GeneralSecurityException) {
                null
            } catch (ignored: IllegalArgumentException) {
                null
            } catch (ignored: IOException) {
                null
            }
        }

        companion object {
            internal val TOKEN_KEY = stringPreferencesKey("auth_token")
            internal val SESSION_KEY = stringPreferencesKey("auth_session_v1")
            private val SESSION_AAD = "virgo.auth.session.v1".toByteArray(Charsets.UTF_8)
        }
    }
