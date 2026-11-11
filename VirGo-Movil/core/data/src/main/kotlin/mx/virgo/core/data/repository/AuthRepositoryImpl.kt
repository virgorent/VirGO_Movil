package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AuthRepository
import mx.virgo.core.domain.AuthSession
import mx.virgo.core.domain.LoggedInUser
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.dto.LoginRequestDto
import mx.virgo.core.network.dto.LoginResponseDto
import mx.virgo.core.network.dto.ProblemDetailsDto
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

private val problemJson = Json { ignoreUnknownKeys = true }

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_NOT_FOUND = 404

class AuthRepositoryImpl
    @Inject
    constructor(
        private val authApi: AuthApi,
    ) : AuthRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun login(
            email: String,
            password: String,
            device: String?,
        ): AppResult<AuthSession> =
            try {
                val response = authApi.login(LoginRequestDto(email = email, password = password, device = device))
                AppResult.Success(response.toAuthSession())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun LoginResponseDto.toAuthSession(): AuthSession =
    AuthSession(
        accessToken = accessToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
        refreshToken = refreshToken,
        refreshTokenExpiresAt = refreshTokenExpiresAt,
        user =
            LoggedInUser(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role,
            ),
    )

internal fun Throwable.toAppError(): AppError =
    when (this) {
        is SocketTimeoutException -> AppError.Timeout
        is HttpException -> response()?.toAppError() ?: AppError.Unknown
        is IOException -> AppError.Network
        else -> AppError.Unknown
    }

private fun retrofit2.Response<*>.toAppError(): AppError {
    val problem =
        runCatching { errorBody()?.string() }
            .getOrNull()
            ?.let { body -> runCatching { problemJson.decodeFromString<ProblemDetailsDto>(body) }.getOrNull() }
    return when (code()) {
        HTTP_BAD_REQUEST -> AppError.Validation(problem?.errors ?: emptyMap())
        HTTP_UNAUTHORIZED -> AppError.Unauthorized
        HTTP_NOT_FOUND -> AppError.NotFound
        else -> AppError.Server(problem?.traceId)
    }
}
