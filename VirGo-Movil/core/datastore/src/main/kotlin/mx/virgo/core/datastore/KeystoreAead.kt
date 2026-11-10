package mx.virgo.core.datastore

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.IOException
import java.security.GeneralSecurityException

internal object KeystoreAead {
    private const val KEYSET_NAME = "virgo_token_keyset"
    private const val PREFS_FILE = "virgo_token_keystore"
    private const val MASTER_KEY_URI = "android-keystore://virgo_token_master_key"
    private const val KEY_TEMPLATE = "AES256_GCM"

    fun create(context: Context): Aead =
        try {
            AndroidKeysetManager
                .Builder()
                .withSharedPref(context.applicationContext, KEYSET_NAME, PREFS_FILE)
                .withMasterKeyUri(MASTER_KEY_URI)
                .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE))
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        } catch (ignored: GeneralSecurityException) {
            throw IllegalStateException("The token keyset could not be initialized", ignored)
        } catch (ignored: IOException) {
            throw IllegalStateException("The token keyset could not be read", ignored)
        }
}
