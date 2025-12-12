package info.benjaminhill.geochat.domain.model

/**
 * Represents an authenticated user in the geochat application.
 *
 * **Purpose:**
 * This data class exists to encapsulate the core identity information of a user (ID and display name)
 * in a way that is decoupled from any specific backend implementation (like Firebase User or Auth0).
 * It serves as the "Domain" representation of a user.
 *
 * **Architecture:**
 * - **Layer:** Domain Model.
 * - **Relations:**
 *   - Used by [info.benjaminhill.geochat.domain.repository.AuthRepository] to return the current user.
 *   - Used by logic that needs to tag content with the author's identity.
 *
 * **Why keep it?**
 * It provides a clean, type-safe way to pass user data around the app without leaking backend-specific
 * libraries (like `com.google.firebase.auth.FirebaseUser`) into the domain or UI layers.
 */
data class User(
    val id: String,
    val displayName: String
)
