package mx.virgo.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.network.BuildConfig
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.api.BookingsApi
import mx.virgo.core.network.api.FavoritesApi
import mx.virgo.core.network.api.HealthApi
import mx.virgo.core.network.api.NoticesApi
import mx.virgo.core.network.api.OwnerVenuesApi
import mx.virgo.core.network.api.UsersApi
import mx.virgo.core.network.api.VenuesApi
import mx.virgo.core.network.createAuthApi
import mx.virgo.core.network.createBookingsApi
import mx.virgo.core.network.createFavoritesApi
import mx.virgo.core.network.createHealthApi
import mx.virgo.core.network.createNoticesApi
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createOwnerVenuesApi
import mx.virgo.core.network.createUsersApi
import mx.virgo.core.network.createVenuesApi
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
    fun provideVenuesApi(client: OkHttpClient): VenuesApi = createVenuesApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideHealthApi(client: OkHttpClient): HealthApi = createHealthApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideFavoritesApi(client: OkHttpClient): FavoritesApi = createFavoritesApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideNoticesApi(client: OkHttpClient): NoticesApi = createNoticesApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideUsersApi(client: OkHttpClient): UsersApi = createUsersApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideBookingsApi(client: OkHttpClient): BookingsApi = createBookingsApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideOwnerVenuesApi(client: OkHttpClient): OwnerVenuesApi =
        createOwnerVenuesApi(BuildConfig.API_BASE_URL, client)
}
