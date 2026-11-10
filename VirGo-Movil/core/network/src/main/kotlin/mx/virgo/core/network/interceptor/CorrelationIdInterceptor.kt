package mx.virgo.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

private const val CORRELATION_ID_HEADER = "X-Correlation-Id"

class CorrelationIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
            chain
                .request()
                .newBuilder()
                .header(CORRELATION_ID_HEADER, UUID.randomUUID().toString())
                .build()
        return chain.proceed(request)
    }
}
