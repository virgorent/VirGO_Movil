package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import retrofit2.HttpException

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_NOT_IMPLEMENTED = 501

/**
 * Llamada a un endpoint del contrato pendiente (`docs/contrato-apis-pendientes.md`): `501` cuenta como
 * endpoint ausente (igual que `404`) y `429` como límite de uso.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> contractCall(block: suspend () -> AppResult<T>): AppResult<T> =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: HttpException) {
        AppResult.Failure(
            when (error.code()) {
                HTTP_NOT_IMPLEMENTED -> AppError.NotFound
                HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
                else -> error.toAppError()
            },
        )
    } catch (error: Exception) {
        AppResult.Failure(error.toAppError())
    }
