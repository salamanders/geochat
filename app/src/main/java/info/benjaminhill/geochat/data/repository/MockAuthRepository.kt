package info.benjaminhill.geochat.data.repository

import info.benjaminhill.geochat.domain.model.User
import info.benjaminhill.geochat.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A dummy implementation of the authentication system.
 *
 * **Purpose:**
 * This class simulates a logged-in user state without connecting to a real backend (like Firebase Auth).
 * It always returns a single hardcoded "Demo User".
 *
 * **Architecture:**
 * - **Layer:** Data Layer (Implementation).
 * - **Relations:**
 *   - Implements [info.benjaminhill.geochat.domain.repository.AuthRepository].
 *   - Injected into [MockPostRepository] to tag new posts with an author.
 *
 * **Why keep it?**
 * It allows developers to work on features that require an authenticated user (like "Send Message")
 * before the actual login screens or backend connection are built.
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    private val fakeUser = User("user_123", "Demo User")

    override fun getCurrentUser(): User? {
        return fakeUser
    }
}
