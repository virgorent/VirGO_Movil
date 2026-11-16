package mx.virgo.core.network.api

import mx.virgo.core.network.dto.LoginRequestDto
import mx.virgo.core.network.dto.LoginResponseDto
import mx.virgo.core.network.dto.RegisterRequestDto
import mx.virgo.core.network.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): LoginResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto,
    ): RegisterResponseDto
}
