package info.benjaminhill.geochat.data.repository

import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A simulation of the device's location services.
 *
 * **Purpose:**
 * This class provides a static or controllable location stream without accessing the actual
 * device GPS hardware. It allows the app to be tested on emulators or in environments where
 * GPS is unavailable or unreliable.
 *
 * **Architecture:**
 * - **Layer:** Data Layer (Implementation).
 * - **Relations:**
 *   - Implements [info.benjaminhill.geochat.domain.repository.LocationRepository].
 *   - Injected into [MockPostRepository] and ViewModels via Hilt.
 *
 * **Why keep it?**
 * It is essential for verifying the distance-based UI logic. By manually setting the location
 * (using [setLocation]), we can simulate "moving" closer or further from a message to see
 * if the font size changes correctly, which is much harder to do with real GPS.
 */
@Singleton
class MockLocationRepository @Inject constructor() : LocationRepository {
    // Start at a default location (e.g., Union Square, SF)
    private val _locationFlow = MutableStateFlow(GeoPoint(37.7879, -122.4075))

    override fun getLocationUpdates(): Flow<GeoPoint> {
        return _locationFlow.asStateFlow()
    }

    override suspend fun setLocation(location: GeoPoint) {
        _locationFlow.emit(location)
    }
}
