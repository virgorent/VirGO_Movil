package mx.virgo.core.network

import mx.virgo.core.network.api.BookingsApi
import mx.virgo.core.network.api.OwnerVenuesApi
import okhttp3.OkHttpClient

/** Fábricas de las API del MVP (salones del propietario y reservaciones). */
fun createBookingsApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): BookingsApi = createRetrofit(baseUrl, client).create(BookingsApi::class.java)

fun createOwnerVenuesApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): OwnerVenuesApi = createRetrofit(baseUrl, client).create(OwnerVenuesApi::class.java)
