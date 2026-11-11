package mx.virgo.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.datastore.KeystoreAead
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.tokenDataStore

    @Provides
    @Singleton
    fun provideAead(
        @ApplicationContext context: Context,
    ): Aead = KeystoreAead.create(context)
}

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_store")
