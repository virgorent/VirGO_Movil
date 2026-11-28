package mx.virgo.core.network.api

import mx.virgo.core.network.dto.ForgotPasswordRequestDto
import mx.virgo.core.network.dto.LoginRequestDto
import mx.virgo.core.network.dto.LoginResponseDto
import mx.virgo.core.network.dto.RegisterRequestDto
import mx.virgo.core.network.dto.RegisterResponseDto
import mx.virgo.core.network.dto.ResetPasswordRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): LoginResponseDto

    @POST("api/v1/auth/password/forgot")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequestDto,
    )

    @POST("api/v1/auth/password/reset")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequestDto,
    )

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto,
    ): RegisterResponseDto

    /** El controlador responde 204 y el OpenAPI declara 200: se aceptan ambos (030 ADR-02). */
    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>
}
