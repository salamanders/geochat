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

data class RadarUiState(
    val currentUserLocation: GeoPoint? = null,
    val posts: List<PostWithMeta> = emptyList()
)

data class PostWithMeta(
    val post: Post,
    val distanceMeters: Double,
    val relevance: Float, // 0.0 to 1.0
    val fontSize: Float,
    val alpha: Float
)

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _currentLocation = locationRepository.getLocationUpdates()

    // We combine location and posts to recalculate distance dynamically
    val uiState: StateFlow<RadarUiState> = combine(
        _currentLocation,
        postRepository.getNearbyPosts(1000.0)
    ) { location, posts ->
        val processedPosts = posts.map { post ->
            val distance = DistanceUtils.calculateDistance(location, post.location)
            val relevance = DistanceUtils.calculateRelevance(distance)
            PostWithMeta(
                post = post,
                distanceMeters = distance,
                relevance = relevance,
                fontSize = DistanceUtils.calculateFontSize(relevance),
                alpha = DistanceUtils.calculateAlpha(relevance)
            )
        }.sortedBy { it.distanceMeters } // Sort by distance, closest first? Or newest? Spec says "newest at bottom".
        // If newest at bottom, we sort by timestamp.
        // Spec: "LazyColumn (list) with reverseLayout = true (newest at bottom)."
        // So we should sort by timestamp descending (newest first) if reverseLayout=true, or ascending if reverseLayout=false.
        // Actually, reverseLayout=true means index 0 is at the bottom.
        // Usually, chat apps have newest at bottom. So if we have a list [Old, New], and reverseLayout=true,
        // it renders [New, Old] visually from bottom up? No.
        // reverseLayout=true renders the first item in the list at the bottom of the screen.
        // So if we want newest at bottom, the list should be sorted [Newest, ..., Oldest].
        // Let's stick to standard timestamp sorting for now.
        .sortedByDescending { it.post.timestamp }

        RadarUiState(
            currentUserLocation = location,
            posts = processedPosts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RadarUiState()
    )

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
