package mx.virgo.core.network.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.virgo.core.common.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.Response

private const val AUTHORIZATION_HEADER = "Authorization"

class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.token.first() }
        val request =
            if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain
                    .request()
                    .newBuilder()
                    .header(AUTHORIZATION_HEADER, "Bearer $token")
                    .build()
            }
        return chain.proceed(request)
    }
}
