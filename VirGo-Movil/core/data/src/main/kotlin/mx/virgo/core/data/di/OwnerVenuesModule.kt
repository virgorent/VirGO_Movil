package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.OwnerVenuesRepositoryImpl
import mx.virgo.core.domain.OwnerVenuesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OwnerVenuesModule {
    @Binds
    @Singleton
    abstract fun bindOwnerVenuesRepository(impl: OwnerVenuesRepositoryImpl): OwnerVenuesRepository
}
