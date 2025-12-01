package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<GeoPoint>

    // For Mock/Debug purposes
    suspend fun setLocation(location: GeoPoint)
}
