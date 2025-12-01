package info.benjaminhill.geochat.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.benjaminhill.geochat.data.repository.MockAuthRepository
import info.benjaminhill.geochat.data.repository.MockLocationRepository
import info.benjaminhill.geochat.data.repository.MockPostRepository
import info.benjaminhill.geochat.domain.repository.AuthRepository
import info.benjaminhill.geochat.domain.repository.LocationRepository
import info.benjaminhill.geochat.domain.repository.PostRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        mockAuthRepository: MockAuthRepository
    ): AuthRepository

    @Binds
    abstract fun bindLocationRepository(
        mockLocationRepository: MockLocationRepository
    ): LocationRepository

    @Binds
    abstract fun bindPostRepository(
        mockPostRepository: MockPostRepository
    ): PostRepository
}
