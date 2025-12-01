package info.benjaminhill.geochat.data.repository

import info.benjaminhill.geochat.domain.model.User
import info.benjaminhill.geochat.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    private val fakeUser = User("user_123", "Demo User")

    override fun getCurrentUser(): User? {
        return fakeUser
    }
}
