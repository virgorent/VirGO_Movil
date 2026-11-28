package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.FavoritesRepositoryImpl
import mx.virgo.core.data.repository.NoticesRepositoryImpl
import mx.virgo.core.data.repository.ProvidersRepositoryImpl
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.domain.NoticesRepository
import mx.virgo.core.domain.ProvidersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreModule {
    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindProvidersRepository(impl: ProvidersRepositoryImpl): ProvidersRepository

    @Binds
    @Singleton
    abstract fun bindNoticesRepository(impl: NoticesRepositoryImpl): NoticesRepository
}
