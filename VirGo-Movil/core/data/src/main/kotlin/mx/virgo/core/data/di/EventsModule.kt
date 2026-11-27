package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.EventFinanceRepositoryImpl
import mx.virgo.core.data.repository.EventPlanningRepositoryImpl
import mx.virgo.core.data.repository.EventsRepositoryImpl
import mx.virgo.core.domain.EventFinanceRepository
import mx.virgo.core.domain.EventPlanningRepository
import mx.virgo.core.domain.EventsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EventsModule {
    @Binds
    @Singleton
    abstract fun bindEventsRepository(impl: EventsRepositoryImpl): EventsRepository

    @Binds
    @Singleton
    abstract fun bindEventFinanceRepository(impl: EventFinanceRepositoryImpl): EventFinanceRepository

    @Binds
    @Singleton
    abstract fun bindEventPlanningRepository(impl: EventPlanningRepositoryImpl): EventPlanningRepository
}
