package info.benjaminhill.geochat.domain.model

import java.util.Date

/**
 * Represents a single message broadcast by a user at a specific location and time.
 *
 * **Purpose:**
 * This is the core data unit of the Geochat application. It bundles the "content" (text)
 * with the "context" (who, where, when) required to render the distance-based UI.
 *
 * **Architecture:**
 * - **Layer:** Domain Model.
 * - **Relations:**
 *   - Returned by [info.benjaminhill.geochat.domain.repository.PostRepository] in lists.
 *   - Consumed by the UI (ProximityMessageRow) to render text size based on [location].
 *
 * **Why keep it?**
 * It is the single source of truth for what a "Post" is in the app. It ensures that regardless of
 * whether data comes from a mock list or a Firestore document, the app processes it uniformly.
 */
data class Post(
    val id: String,
    val userId: String,
    val userDisplayName: String,
    val text: String,
    val timestamp: Date,
    val location: GeoPoint,
    val plusCode: String
)
