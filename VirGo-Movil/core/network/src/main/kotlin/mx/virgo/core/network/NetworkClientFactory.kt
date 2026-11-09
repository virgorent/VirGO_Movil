package mx.virgo.core.network

import kotlinx.serialization.json.Json
import mx.virgo.core.network.api.AuthApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 20L
private const val JSON_MEDIA_TYPE = "application/json"

fun createAuthApi(
    baseUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
): AuthApi {
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()
        .create(AuthApi::class.java)
}

fun defaultOkHttpClient(): OkHttpClient =
    OkHttpClient
        .Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
