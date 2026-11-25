package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.VenueRepositoryImpl
import mx.virgo.core.domain.VenueDetailRepository
import mx.virgo.core.domain.VenueRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VenueModule {
    @Binds
    @Singleton
    abstract fun bindVenueRepository(impl: VenueRepositoryImpl): VenueRepository

    @Binds
    @Singleton
    abstract fun bindVenueDetailRepository(impl: VenueRepositoryImpl): VenueDetailRepository
}
