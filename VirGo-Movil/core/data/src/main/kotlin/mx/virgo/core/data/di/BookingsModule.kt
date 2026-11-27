package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.AiRepositoryImpl
import mx.virgo.core.data.repository.AvailabilityRepositoryImpl
import mx.virgo.core.data.repository.BookingsRepositoryImpl
import mx.virgo.core.domain.AiRepository
import mx.virgo.core.domain.AvailabilityRepository
import mx.virgo.core.domain.BookingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingsModule {
    @Binds
    @Singleton
    abstract fun bindBookingsRepository(impl: BookingsRepositoryImpl): BookingsRepository

    @Binds
    @Singleton
    abstract fun bindAvailabilityRepository(impl: AvailabilityRepositoryImpl): AvailabilityRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
}
