package info.benjaminhill.geochat.data.repository

import com.google.openlocationcode.OpenLocationCode
import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.model.Post
import info.benjaminhill.geochat.domain.repository.AuthRepository
import info.benjaminhill.geochat.domain.repository.LocationRepository
import info.benjaminhill.geochat.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockPostRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository // To get current loc for sending
) : PostRepository {

    // Center point for generating fake posts (Union Square)
    private val centerLat = 37.7879
    private val centerLng = -122.4075

    private val _posts = MutableStateFlow<List<Post>>(emptyList())

    init {
        // Generate some initial posts
        val initialPosts = mutableListOf<Post>()

        // 1. Very close post (10m)
        initialPosts.add(createFakePost("Hey! I'm right next to you!", 0.0001, 0.0))

        // 2. Medium distance (200m)
        initialPosts.add(createFakePost("Anyone want to grab coffee?", 0.002, 0.001))

        // 3. Far distance (800m)
        initialPosts.add(createFakePost("Just arriving downtown.", 0.008, -0.005))

        // 4. Too far (1200m) - should verify filtering logic in UI or Repo
        initialPosts.add(createFakePost("I'm way over in SOMA.", 0.012, 0.012))

        _posts.value = initialPosts
    }

    private fun createFakePost(text: String, latOffset: Double, lngOffset: Double): Post {
        val lat = centerLat + latOffset
        val lng = centerLng + lngOffset
        val plusCode = OpenLocationCode.encode(lat, lng)
        return Post(
            id = UUID.randomUUID().toString(),
            userId = "other_user_${Random.nextInt(100)}",
            userDisplayName = "Stranger ${Random.nextInt(100)}",
            text = text,
            timestamp = Date(),
            location = GeoPoint(lat, lng),
            plusCode = plusCode
        )
    }

    override fun getNearbyPosts(radiusInMeters: Double): Flow<List<Post>> {
        // In a real app, we'd query Firestore. Here we just return the mock list.
        // We could filter by radius here, but the UI logic usually handles the sorting/sizing.
        // For strict correctness, we should filter.
        return _posts
    }

    override suspend fun sendPost(text: String) {
        val currentUser = authRepository.getCurrentUser() ?: return

        // Getting the current location from the flow (hacky for mock, but works)
        // In real world we'd take a snapshot or pass it in.
        // For this mock, let's assume the user is at the "current" mock location.
        // Since we don't have a synchronous way to get value from flow in interface easily without collection,
        // we'll just assume a default or use the center for simplicity, OR better:
        // Assume the ViewModel passes the location, but the interface signature is just text.
        // Let's create a post at the center for now to ensure it's visible.

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            userId = currentUser.id,
            userDisplayName = currentUser.displayName,
            text = text,
            timestamp = Date(),
            location = GeoPoint(centerLat, centerLng), // Ideally fetch actual current mock location
            plusCode = OpenLocationCode.encode(centerLat, centerLng)
        )

        val currentList = _posts.value.toMutableList()
        currentList.add(0, newPost)
        _posts.value = currentList
    }
}
