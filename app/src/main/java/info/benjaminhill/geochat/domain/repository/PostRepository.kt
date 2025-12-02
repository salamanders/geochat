package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for fetching and sending messages (Posts).
 *
 * **Purpose:**
 * This interface acts as the bridge between the app's business logic and the data storage.
 * It defines *what* operations are available (get nearby posts, send a post) without specifying
 * *how* (Firestore, SQL, In-Memory).
 *
 * **Architecture:**
 * - **Layer:** Domain Interface (Repository Pattern).
 * - **Relations:**
 *   - Implemented by [info.benjaminhill.geochat.data.repository.MockPostRepository] (Data Layer).
 *   - Used by the RadarViewModel to populate the feed.
 *
 * **Why keep it?**
 * It is crucial for the "Proximity Chat" feature. It enforces that fetching posts requires a
 * radius, ensuring the app's core geospatial constraint is respected by any data source.
 */
interface PostRepository {
    fun getNearbyPosts(radiusInMeters: Double): Flow<List<Post>>
    suspend fun sendPost(text: String)
}
