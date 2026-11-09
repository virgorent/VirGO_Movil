package mx.virgo.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.network.BuildConfig
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.createAuthApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi = createAuthApi(BuildConfig.API_BASE_URL)
}
