package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getNearbyPosts(radiusInMeters: Double): Flow<List<Post>>
    suspend fun sendPost(text: String)
}
