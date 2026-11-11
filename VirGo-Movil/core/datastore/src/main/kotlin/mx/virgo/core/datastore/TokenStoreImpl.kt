package mx.virgo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.GeneralSecurityException
import java.util.Base64
import javax.inject.Inject

class TokenStoreImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val aead: Aead,
    ) : TokenStore {
        override val token: Flow<String?> =
            dataStore.data.map { preferences -> decryptOrNull(preferences[TOKEN_KEY]) }

        override suspend fun save(token: String) {
            val ciphertext = aead.encrypt(token.toByteArray(Charsets.UTF_8), ByteArray(0))
            val encoded = Base64.getEncoder().encodeToString(ciphertext)
            dataStore.edit { preferences -> preferences[TOKEN_KEY] = encoded }
        }

        override suspend fun clear() {
            dataStore.edit { preferences -> preferences.remove(TOKEN_KEY) }
        }

        private fun decryptOrNull(encoded: String?): String? {
            if (encoded == null) return null
            return try {
                val ciphertext = Base64.getDecoder().decode(encoded)
                aead.decrypt(ciphertext, ByteArray(0)).toString(Charsets.UTF_8)
            } catch (ignored: GeneralSecurityException) {
                null
            } catch (ignored: IllegalArgumentException) {
                null
            }
        }

        companion object {
            internal val TOKEN_KEY = stringPreferencesKey("auth_token")
        }
    }
