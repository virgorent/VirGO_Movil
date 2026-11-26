package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

private const val MX_COUNTRY_CODE = "+52"

class RegisterUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(
            name: String,
            email: String,
            phone: String,
            password: String,
            role: AccountRole = AccountRole.CLIENT,
        ): AppResult<RegisteredUser> =
            repository.register(
                name = name.trim(),
                email = email.trim().lowercase(),
                phone = MX_COUNTRY_CODE + phone.trim(),
                password = password,
                role = role,
            )
    }
