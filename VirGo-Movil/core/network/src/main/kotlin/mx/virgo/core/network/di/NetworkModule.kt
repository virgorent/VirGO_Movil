package mx.virgo.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.network.BuildConfig
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.api.HealthApi
import mx.virgo.core.network.createAuthApi
import mx.virgo.core.network.createHealthApi
import mx.virgo.core.network.createOkHttpClient
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenProvider: AuthTokenProvider): OkHttpClient =
        createOkHttpClient(authTokenProvider = authTokenProvider, isDebug = BuildConfig.IS_DEBUGGABLE)

    @Provides
    @Singleton
    fun provideAuthApi(client: OkHttpClient): AuthApi = createAuthApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideHealthApi(client: OkHttpClient): HealthApi = createHealthApi(BuildConfig.API_BASE_URL, client)
}
