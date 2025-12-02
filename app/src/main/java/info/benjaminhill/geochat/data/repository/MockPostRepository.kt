package info.benjaminhill.geochat.data.repository

import com.google.openlocationcode.OpenLocationCode
import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.model.Post
import info.benjaminhill.geochat.domain.repository.AuthRepository
import info.benjaminhill.geochat.domain.repository.LocationRepository
import info.benjaminhill.geochat.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * A simulation of the backend database (Firestore).
 *
 * **Purpose:**
 * This class provides a pre-filled list of fake posts scattered around a central location.
 * It is designed to stress-test the UI's filtering and sorting logic by including posts
 * at varying distances (from 20m to 1000km) and ages (1 minute to 30 days).
 *
 * **Architecture:**
 * - **Layer:** Data Layer (Implementation).
 * - **Relations:**
 *   - Implements [info.benjaminhill.geochat.domain.repository.PostRepository].
 *   - Consumes [AuthRepository] to get the current user for new posts.
 *   - Consumes [LocationRepository] to simulate where new posts are created.
 *
 * **Why keep it?**
 * It is the primary data source for the current "Phase 1" of development. It ensures the
 * "Radar" UI works correctly (showing near items big, far items small) without needing a
 * live internet connection or a populated database.
 */
@Singleton
class MockPostRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : PostRepository {

    // Center point for generating fake posts (Union Square)
    private val centerLat = 37.7879
    private val centerLng = -122.4075

    private val _posts = MutableStateFlow<List<Post>>(emptyList())

    init {
        generateDiverseMockData()
    }

    private fun generateDiverseMockData() {
        val posts = mutableListOf<Post>()
        val now = System.currentTimeMillis()
        val r = Random(123) // Fixed seed for reproducibility

        // 1. Very Recent & Very Close (Density Burst)
        // Simulate a crowd nearby posting right now
        repeat(20) {
            posts.add(
                createFakePost(
                    "Crowd member $it",
                    centerLat + (r.nextDouble() - 0.5) * 0.0002, // ~20m spread
                    centerLng + (r.nextDouble() - 0.5) * 0.0002,
                    Date(now - r.nextLong(60_000)) // Last 1 minute
                )
            )
        }

        // 2. Recent (5 mins) & Medium Distance (500m)
        repeat(30) {
            posts.add(
                createFakePost(
                    "Nearby Walker $it",
                    centerLat + (r.nextDouble() - 0.5) * 0.01, // ~1km spread
                    centerLng + (r.nextDouble() - 0.5) * 0.01,
                    Date(now - r.nextLong(300_000)) // Last 5 minutes
                )
            )
        }

        // 3. Older (1 Hour) & Farther (5km)
        repeat(30) {
            posts.add(
                createFakePost(
                    "City Dweller $it",
                    centerLat + (r.nextDouble() - 0.5) * 0.1, // ~10km spread
                    centerLng + (r.nextDouble() - 0.5) * 0.1,
                    Date(now - r.nextLong(3_600_000)) // Last 1 hour
                )
            )
        }

        // 4. Very Old (1 Day) & Very Far (Global/Regional)
        repeat(50) {
            posts.add(
                createFakePost(
                    "Regional user $it",
                    centerLat + (r.nextDouble() - 0.5) * 2.0, // ~200km spread
                    centerLng + (r.nextDouble() - 0.5) * 2.0,
                    Date(now - r.nextLong(86_400_000)) // Last 24 hours
                )
            )
        }

        // 5. Ancient History
         repeat(50) {
            posts.add(
                createFakePost(
                    "Ancient user $it",
                    centerLat + (r.nextDouble() - 0.5) * 10.0, // ~1000km spread
                    centerLng + (r.nextDouble() - 0.5) * 10.0,
                    Date(now - r.nextLong(30L * 86_400_000)) // Last 30 days
                )
            )
        }

        _posts.value = posts
    }

    private fun createFakePost(text: String, lat: Double, lng: Double, timestamp: Date = Date()): Post {
        val plusCode = OpenLocationCode.encode(lat, lng)
        return Post(
            id = UUID.randomUUID().toString(),
            userId = "mock_user_${Random.nextInt(1000)}",
            userDisplayName = "Stranger",
            text = text,
            timestamp = timestamp,
            location = GeoPoint(lat, lng),
            plusCode = plusCode
        )
    }

    override fun getNearbyPosts(radiusInMeters: Double): Flow<List<Post>> {
        return _posts
    }

    override suspend fun sendPost(text: String) {
        val currentUser = authRepository.getCurrentUser() ?: return

        // Use center for simplicity in mock
        val newPost = Post(
            id = UUID.randomUUID().toString(),
            userId = currentUser.id,
            userDisplayName = currentUser.displayName,
            text = text,
            timestamp = Date(),
            location = GeoPoint(centerLat, centerLng),
            plusCode = OpenLocationCode.encode(centerLat, centerLng)
        )

        val currentList = _posts.value.toMutableList()
        currentList.add(0, newPost)
        _posts.value = currentList
    }
}
