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

/**
 * A Hilt Module that configures how dependencies are provided throughout the app.
 *
 * **Purpose:**
 * This object instructs Hilt on which concrete class to use when an interface is requested.
 * For example, when a ViewModel asks for a [AuthRepository], this module tells Hilt to provide
 * the [MockAuthRepository].
 *
 * **Architecture:**
 * - **Layer:** Dependency Injection (Infrastructure).
 * - **Relations:**
 *   - Binds [MockAuthRepository] -> [AuthRepository].
 *   - Binds [MockLocationRepository] -> [LocationRepository].
 *   - Binds [MockPostRepository] -> [PostRepository].
 *
 * **Why keep it?**
 * It is the switchboard of the application. By changing the bindings in this file (or creating
 * a "Prod" version of it), we can swap the entire backend from "Mock" to "Firebase" without
 * changing a single line of code in the UI or Domain layers.
 */
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
