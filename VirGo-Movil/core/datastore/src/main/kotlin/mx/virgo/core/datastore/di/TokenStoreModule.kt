package mx.virgo.core.datastore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.datastore.TokenStore
import mx.virgo.core.datastore.TokenStoreImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenStoreModule {
    @Binds
    @Singleton
    abstract fun bindTokenStore(impl: TokenStoreImpl): TokenStore
}
