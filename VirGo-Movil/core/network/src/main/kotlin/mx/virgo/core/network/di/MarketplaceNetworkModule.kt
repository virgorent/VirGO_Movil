package mx.virgo.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.network.BuildConfig
import mx.virgo.core.network.api.AiApi
import mx.virgo.core.network.api.AvailabilityApi
import mx.virgo.core.network.api.BookingsApi
import mx.virgo.core.network.api.EventPlanningApi
import mx.virgo.core.network.api.EventsApi
import mx.virgo.core.network.api.OwnerVenuesApi
import mx.virgo.core.network.api.QuotesApi
import mx.virgo.core.network.createAiApi
import mx.virgo.core.network.createAvailabilityApi
import mx.virgo.core.network.createBookingsApi
import mx.virgo.core.network.createEventPlanningApi
import mx.virgo.core.network.createEventsApi
import mx.virgo.core.network.createOwnerVenuesApi
import mx.virgo.core.network.createQuotesApi
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** API del MVP: salones del propietario, reservaciones, cotizaciones y eventos. */
@Module
@InstallIn(SingletonComponent::class)
object MarketplaceNetworkModule {
    @Provides
    @Singleton
    fun provideBookingsApi(client: OkHttpClient): BookingsApi = createBookingsApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideOwnerVenuesApi(client: OkHttpClient): OwnerVenuesApi =
        createOwnerVenuesApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideQuotesApi(client: OkHttpClient): QuotesApi = createQuotesApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideAiApi(client: OkHttpClient): AiApi = createAiApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideAvailabilityApi(client: OkHttpClient): AvailabilityApi =
        createAvailabilityApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideEventsApi(client: OkHttpClient): EventsApi = createEventsApi(BuildConfig.API_BASE_URL, client)

    @Provides
    @Singleton
    fun provideEventPlanningApi(client: OkHttpClient): EventPlanningApi =
        createEventPlanningApi(BuildConfig.API_BASE_URL, client)
}
