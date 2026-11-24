package mx.virgo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.virgo.core.data.repository.RecoveryRepositoryImpl
import mx.virgo.core.domain.RecoveryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecoveryModule {
    @Binds
    @Singleton
    abstract fun bindRecoveryRepository(impl: RecoveryRepositoryImpl): RecoveryRepository
}
