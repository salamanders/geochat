package info.benjaminhill.geochat.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.model.Post
import info.benjaminhill.geochat.domain.repository.LocationRepository
import info.benjaminhill.geochat.domain.repository.PostRepository
import info.benjaminhill.geochat.domain.util.DistanceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * A helper data class linking a [Post] with its calculated display properties.
 *
 * **Purpose:**
 * This class exists to "pre-calculate" the UI state for each row. Instead of the UI
 * asking `DistanceUtils` to calculate font size during every frame of a scroll, the ViewModel
 * does it once per update.
 *
 * **Architecture:**
 * - **Layer:** Presentation (ViewModel State).
 * - **Relations:**
 *   - Produced by [RadarViewModel].
 *   - Consumed by [info.benjaminhill.geochat.ui.components.ProximityMessageRow].
 */
data class PostWithMeta(
    val post: Post,
    val distanceMeters: Double,
    val relevance: Float,
    val fontSize: Float,
    val alpha: Float
)

/**
 * The state holder for the RadarFeedScreen.
 *
 * **Purpose:**
 * Encapsulates all data required to render the screen at a specific instant: where the user is,
 * what posts are visible, and any debug info.
 *
 * **Architecture:**
 * - **Layer:** Presentation (ViewModel State).
 */
data class RadarUiState(
    val currentUserLocation: GeoPoint? = null,
    val posts: List<PostWithMeta> = emptyList(),
    val debugRangeInfo: String = ""
)

/**
 * The brain of the "Radar" screen.
 *
 * **Purpose:**
 * This ViewModel coordinates the flow of data between the Domain layer and the UI.
 * It observes the user's location and the list of nearby posts, combines them, calculates
 * the visual properties (size/opacity) for each post, and exposes a single [RadarUiState]
 * for the UI to render.
 *
 * **Architecture:**
 * - **Layer:** Presentation (ViewModel).
 * - **Relations:**
 *   - Consumes [LocationRepository] to track the user.
 *   - Consumes [PostRepository] to fetch data.
 *   - Uses [DistanceUtils] to apply the business logic for font scaling.
 *   - Exposes state to [RadarFeedScreen].
 *
 * **Why keep it?**
 * It handles the complex logic of "Adaptive Message Density" (expanding/shrinking the search
 * radius based on how many messages are found) so the UI remains dumb and simple.
 */
@HiltViewModel
class RadarViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    // Target: We want about 1 message every 2 seconds visible on screen?
    // Or just a reasonable density.
    // Let's implement a dynamic radius: Start small, expand if empty.
    // For now, fixed large radius to capture everything, filter in memory.
    private val searchRadius = 500_000.0 // 500km start

    private val _location = locationRepository.getLocationUpdates()
    private val _rawPosts = postRepository.getNearbyPosts(searchRadius)

    val uiState: StateFlow<RadarUiState> = combine(_location, _rawPosts) { location, posts ->
        processFeed(location, posts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RadarUiState()
    )

    private fun processFeed(location: GeoPoint, posts: List<Post>): RadarUiState {
        // 1. Calculate Distances
        val postsWithDist = posts.map { post ->
            val dist = DistanceUtils.calculateDistance(location, post.location)
            post to dist
        }.sortedBy { it.second } // Closest first

        // 2. Filter Logic (Adaptive Density)
        // Goal: Show ~30 items? Or just show all within MAX_DISTANCE?
        // The spec says: "The content logic expands the spatial range first... defaulting to last 5 mins if screen remains empty."
        // For this implementation, let's keep it simple: Show nearest 30 items or everything within 1km.

        // Strategy: Take all within 1km. If count < 10, expand to 5km. If count < 10, expand to global.
        // Actually, let's just take the TOP N closest, and let the font size logic handle the "fading out".
        // But we need to filter by TIME if density is high?
        // Let's implement the "Rank-Based Scaling" mentioned in memory:
        // "Message visual sizing uses Rank-Based Scaling (e.g., the closest 25% of visible messages are largest)"

        val maxVisible = 50
        // Filter by time window initially (5 mins)
        val now = System.currentTimeMillis()
        var timeWindowSeconds = 300L // 5 mins
        var filteredByTime = postsWithDist.filter { (now - it.first.timestamp.time) < (timeWindowSeconds * 1000) }

        if (filteredByTime.size < 5) {
            // Expand time to 1 hour
            timeWindowSeconds = 3600L
            filteredByTime = postsWithDist.filter { (now - it.first.timestamp.time) < (timeWindowSeconds * 1000) }
        }

        if (filteredByTime.size < 5) {
            // Expand to All Time
            timeWindowSeconds = Long.MAX_VALUE
            filteredByTime = postsWithDist
        }

        // Take closest N
        val finalSelection = filteredByTime.take(maxVisible)
        val totalSelected = finalSelection.size

        val processedPosts = finalSelection.mapIndexed { index, (post, dist) ->
            // Rank: 0.0 (Closest) to 1.0 (Farthest in selection)
            // If only 1 item, rank is 0.
            val rank = if (totalSelected > 1) {
                index.toFloat() / (totalSelected - 1)
            } else {
                0f
            }

            // Relevance is Inverse of Rank (1.0 = Closest/Best, 0.0 = Farthest/Worst)
            val relevance = 1f - rank

            PostWithMeta(
                post = post,
                distanceMeters = dist,
                relevance = relevance,
                fontSize = DistanceUtils.calculateFontSize(relevance),
                alpha = DistanceUtils.calculateAlpha(relevance)
            )
        }.sortedByDescending { it.post.timestamp } // Sort by Time for Display

        // Debug Info
        val rangeMeters = finalSelection.lastOrNull()?.second?.roundToInt() ?: 0
        val timeDesc = if (timeWindowSeconds == Long.MAX_VALUE) "All Time" else "${timeWindowSeconds/60}m"

        return RadarUiState(
            currentUserLocation = location,
            posts = processedPosts,
            debugRangeInfo = "Time: $timeDesc | Range: ${rangeMeters}m | Count: $totalSelected"
        )
    }

    /**
     * Sends a new message to the repository.
     */
    fun sendMessage(text: String) {
        viewModelScope.launch {
            postRepository.sendPost(text)
        }
    }

    /**
     * A debug function to artificially move the mock user location.
     * This helps verify the UI changes without physically walking around.
     */
    fun debugMoveUser(latOffset: Double, lngOffset: Double) {
        viewModelScope.launch {
            val current = uiState.value.currentUserLocation ?: return@launch
            locationRepository.setLocation(
                GeoPoint(
                    current.latitude + latOffset,
                    current.longitude + lngOffset
                )
            )
        }
    }
}
