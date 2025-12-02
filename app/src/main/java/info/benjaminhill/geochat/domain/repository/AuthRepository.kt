package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.User

/**
 * Defines the contract for user authentication operations.
 *
 * **Purpose:**
 * This interface abstracts the details of *how* a user is authenticated (e.g., Firebase, OAuth, Mock)
 * from *what* the app needs to know (Who is the current user?).
 *
 * **Architecture:**
 * - **Layer:** Domain Interface (Repository Pattern).
 * - **Relations:**
 *   - Implemented by [info.benjaminhill.geochat.data.repository.MockAuthRepository] (Data Layer).
 *   - Injected into Use Cases or ViewModels that need user identity.
 *
 * **Why keep it?**
 * It allows the app to switch authentication providers or use a mock provider (like we are doing now)
 * without changing a single line of code in the UI or business logic.
 */
interface AuthRepository {
    fun getCurrentUser(): User?
}
