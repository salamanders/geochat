package info.benjaminhill.geochat.domain.model

/**
 * A platform-agnostic representation of a geographical location (Latitude/Longitude).
 *
 * **Purpose:**
 * This class exists to store coordinates without relying on Android-specific classes (like `android.location.Location`)
 * or Google Play Services classes (like `com.google.android.gms.maps.model.LatLng`). This makes the
 * Domain layer pure Kotlin and testable without an Android emulator/device.
 *
 * **Architecture:**
 * - **Layer:** Domain Model.
 * - **Relations:**
 *   - Embedded in [Post] to mark where a message was sent.
 *   - Used in [info.benjaminhill.geochat.domain.util.DistanceUtils] for math calculations.
 *   - Used in [info.benjaminhill.geochat.domain.repository.LocationRepository] to stream updates.
 *
 * **Why keep it?**
 * To maintain Clean Architecture principles by preventing Android framework dependencies from leaking
 * into the inner Domain logic.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
