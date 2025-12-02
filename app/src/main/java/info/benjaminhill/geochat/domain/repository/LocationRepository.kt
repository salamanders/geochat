package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for accessing the device's physical location.
 *
 * **Purpose:**
 * This interface abstracts the source of location data. Whether the data comes from
 * Android's GPS hardware, a replay file, or a static mock, the Domain layer consumes it
 * as a simple stream of [GeoPoint]s.
 *
 * **Architecture:**
 * - **Layer:** Domain Interface (Repository Pattern).
 * - **Relations:**
 *   - Implemented by [info.benjaminhill.geochat.data.repository.MockLocationRepository] (Data Layer).
 *   - Consumed by ViewModels to update the UI "Center" or by Repositories to query nearby data.
 *
 * **Why keep it?**
 * Location is a volatile dependency (battery heavy, permissions required). Abstracting it
 * keeps the complex Android permission/callback logic isolated in the Data layer implementation.
 */
interface LocationRepository {
    fun getLocationUpdates(): Flow<GeoPoint>

    // For Mock/Debug purposes
    suspend fun setLocation(location: GeoPoint)
}
