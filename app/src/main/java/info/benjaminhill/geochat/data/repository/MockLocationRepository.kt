package info.benjaminhill.geochat.data.repository

import info.benjaminhill.geochat.domain.model.GeoPoint
import info.benjaminhill.geochat.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
