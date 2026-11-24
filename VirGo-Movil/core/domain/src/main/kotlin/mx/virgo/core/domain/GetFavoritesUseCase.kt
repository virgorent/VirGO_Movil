package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class GetFavoritesUseCase
    @Inject
    constructor(
        private val repository: FavoritesRepository,
    ) {
        suspend operator fun invoke(): AppResult<List<Favorite>> = repository.getFavorites()
    }
