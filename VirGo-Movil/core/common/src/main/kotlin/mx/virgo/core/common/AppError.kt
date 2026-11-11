package mx.virgo.core.common

sealed interface AppError {
    data object Network : AppError

    data object Timeout : AppError

    data object Unauthorized : AppError

    data object NotFound : AppError

    data class Validation(
        val fields: Map<String, List<String>>,
    ) : AppError

    data class Server(
        val traceId: String?,
    ) : AppError

    data object Unknown : AppError
}
