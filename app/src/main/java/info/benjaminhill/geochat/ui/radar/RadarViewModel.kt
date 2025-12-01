package info.benjaminhill.geochat.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.model.Post
import info.benjaminhill.geochat.domain.repository.LocationRepository
import info.benjaminhill.geochat.domain.repository.PostRepository
import info.benjaminhill.geochat.domain.util.DistanceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

data class RadarUiState(
    val currentUserLocation: GeoPoint? = null,
    val posts: List<PostWithMeta> = emptyList(),
    val debugRangeInfo: String = ""
)

data class PostWithMeta(
    val post: Post,
    val distanceMeters: Double,
    val relevance: Float, // 0.0 to 1.0 (Based on Rank now)
    val fontSize: Float,
    val alpha: Float
)

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _currentLocation = locationRepository.getLocationUpdates()

    // Ticker to refresh time-based filtering every second
    private val _ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    // We combine location, posts, and time ticker
    val uiState: StateFlow<RadarUiState> = combine(
        _currentLocation,
        postRepository.getNearbyPosts(1000.0), // Currently returns all mock posts
        _ticker
    ) { location, allPosts, currentTime ->
        processPosts(location, allPosts, currentTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RadarUiState()
    )

    private fun processPosts(location: GeoPoint, allPosts: List<Post>, currentTime: Long): RadarUiState {
        // Algorithm Parameters
        val minDisplayableCount = 10
        val maxItems = 150
        val targetDensityMsgPerSec = 0.5

        // 1. Time Expansion Strategy
        var timeWindowSeconds = 5 * 60L // Start with 5 minutes
        var candidatePosts: List<Post>

        // Expansion Levels: 5m -> 1h -> 24h -> 7d -> All
        val expansionLevels = listOf(
            5 * 60L,
            60 * 60L,
            24 * 60 * 60L,
            7 * 24 * 60 * 60L,
            Long.MAX_VALUE
        )

        var effectiveExpansionLevelIndex = 0

        while (true) {
             timeWindowSeconds = expansionLevels[effectiveExpansionLevelIndex]
             candidatePosts = if (timeWindowSeconds == Long.MAX_VALUE) {
                 allPosts
             } else {
                 allPosts.filter {
                     val age = currentTime - it.timestamp.time
                     age >= 0 && age <= timeWindowSeconds * 1000
                 }
             }

             if (candidatePosts.size >= minDisplayableCount || effectiveExpansionLevelIndex == expansionLevels.lastIndex) {
                 break
             }
             effectiveExpansionLevelIndex++
        }

        // 2. Density Filtering (Spatial)
        // Calculate Distance for all candidates
        val candidatesWithDist = candidatePosts.map { post ->
            val dist = DistanceUtils.calculateDistance(location, post.location)
            post to dist
        }.sortedBy { it.second } // Closest first

        // Determine Cutoff Count
        // Target = TimeWindow * 0.5 msg/sec
        // But capped at MAX_ITEMS
        // Note: If TimeWindow is MAX_VALUE, we treat it as "Large enough to allow MAX_ITEMS"

        val allowedCountByTime = if (timeWindowSeconds == Long.MAX_VALUE) {
            maxItems
        } else {
            (timeWindowSeconds * targetDensityMsgPerSec).roundToInt()
        }

        val finalCount = allowedCountByTime.coerceIn(minDisplayableCount, maxItems)
        val finalSelection = candidatesWithDist.take(finalCount)

        // 3. Rank-Based Sizing
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

    fun sendMessage(text: String) {
        viewModelScope.launch {
            postRepository.sendPost(text)
        }
    }

    // Debug method to move user
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
