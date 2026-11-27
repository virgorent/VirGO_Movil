package mx.virgo.core.network

import mx.virgo.core.network.api.AiApi
import mx.virgo.core.network.api.AvailabilityApi
import mx.virgo.core.network.api.BookingsApi
import mx.virgo.core.network.api.ConversationsApi
import mx.virgo.core.network.api.EventPlanningApi
import mx.virgo.core.network.api.EventsApi
import mx.virgo.core.network.api.OwnerVenuesApi
import mx.virgo.core.network.api.QuotesApi
import okhttp3.OkHttpClient

/** Fábricas de las API del MVP (salones del propietario, reservaciones, cotizaciones y eventos). */
fun createBookingsApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): BookingsApi = createRetrofit(baseUrl, client).create(BookingsApi::class.java)

fun createQuotesApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): QuotesApi = createRetrofit(baseUrl, client).create(QuotesApi::class.java)

fun createOwnerVenuesApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): OwnerVenuesApi = createRetrofit(baseUrl, client).create(OwnerVenuesApi::class.java)

fun createAiApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): AiApi = createRetrofit(baseUrl, client).create(AiApi::class.java)

fun createConversationsApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): ConversationsApi = createRetrofit(baseUrl, client).create(ConversationsApi::class.java)

fun createAvailabilityApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): AvailabilityApi = createRetrofit(baseUrl, client).create(AvailabilityApi::class.java)

fun createEventsApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): EventsApi = createRetrofit(baseUrl, client).create(EventsApi::class.java)

fun createEventPlanningApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): EventPlanningApi = createRetrofit(baseUrl, client).create(EventPlanningApi::class.java)
