package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.RecoveryRepository
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.dto.ForgotPasswordRequestDto
import mx.virgo.core.network.dto.ResetPasswordRequestDto
import javax.inject.Inject

class RecoveryRepositoryImpl
    @Inject
    constructor(
        private val authApi: AuthApi,
    ) : RecoveryRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun forgotPassword(email: String): AppResult<Unit> =
            try {
                authApi.forgotPassword(ForgotPasswordRequestDto(email = email))
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }

        @Suppress("TooGenericExceptionCaught")
        override suspend fun resetPassword(
            code: String,
            password: String,
        ): AppResult<Unit> =
            try {
                authApi.resetPassword(ResetPasswordRequestDto(code = code, password = password))
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }
