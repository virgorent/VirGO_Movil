package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AccountRepository
import mx.virgo.core.domain.UserProfile
import mx.virgo.core.network.api.AuthApi
import mx.virgo.core.network.api.UsersApi
import mx.virgo.core.network.dto.UserProfileDto
import retrofit2.HttpException
import javax.inject.Inject

class AccountRepositoryImpl
    @Inject
    constructor(
        private val usersApi: UsersApi,
        private val authApi: AuthApi,
    ) : AccountRepository {
        override suspend fun getProfile(): AppResult<UserProfile> =
            safeCall { AppResult.Success(usersApi.me().toUserProfile()) }

        override suspend fun logout(): AppResult<Unit> =
            safeCall {
                val response = authApi.logout()
                if (response.isSuccessful) {
                    AppResult.Success(Unit)
                } else {
                    AppResult.Failure(HttpException(response).toAppError())
                }
            }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> =
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun UserProfileDto.toUserProfile(): UserProfile =
    UserProfile(
        id = id,
        name = name,
        email = email,
        phone = phone?.takeIf { it.isNotBlank() },
        role = role,
        isVerified = isVerified,
    )
