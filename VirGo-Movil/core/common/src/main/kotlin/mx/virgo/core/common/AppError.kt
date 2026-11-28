package mx.virgo.core.common

sealed interface AppError {
    data object Network : AppError

    data object Timeout : AppError

    data object Unauthorized : AppError

    /** 403: la cuenta no tiene el rol necesario. */
    data object Forbidden : AppError

    data object NotFound : AppError

    data object Conflict : AppError

    /** 429 de la IA (`012`, `013`): demasiadas peticiones; se reintenta más tarde. */
    data object RateLimited : AppError

    data class Validation(
        val fields: Map<String, List<String>>,
    ) : AppError

    data class Server(
        val traceId: String?,
    ) : AppError

    data object Unknown : AppError
}
