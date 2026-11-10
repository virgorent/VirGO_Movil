package mx.virgo.core.network

import kotlinx.serialization.json.Json
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.api.HealthApi
import mx.virgo.core.network.interceptor.AuthInterceptor
import mx.virgo.core.network.interceptor.CorrelationIdInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 20L
private const val JSON_MEDIA_TYPE = "application/json"

fun createAuthApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): AuthApi = createRetrofit(baseUrl, client).create(AuthApi::class.java)

fun createHealthApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): HealthApi = createRetrofit(baseUrl, client).create(HealthApi::class.java)

fun defaultOkHttpClient(): OkHttpClient =
    OkHttpClient
        .Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

fun createOkHttpClient(
    authTokenProvider: AuthTokenProvider,
    isDebug: Boolean,
): OkHttpClient =
    defaultOkHttpClient()
        .newBuilder()
        .addInterceptor(CorrelationIdInterceptor())
        .addInterceptor(AuthInterceptor(authTokenProvider))
        .apply {
            if (isDebug) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }.build()

private fun createRetrofit(
    baseUrl: String,
    client: OkHttpClient,
): Retrofit {
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()
}
