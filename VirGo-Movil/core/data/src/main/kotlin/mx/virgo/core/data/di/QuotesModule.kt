package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.QuotesRepositoryImpl
import mx.virgo.core.domain.QuotesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuotesModule {
    @Binds
    @Singleton
    abstract fun bindQuotesRepository(impl: QuotesRepositoryImpl): QuotesRepository
}
