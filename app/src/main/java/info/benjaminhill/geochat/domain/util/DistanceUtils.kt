package info.benjaminhill.geochat.domain.util

import info.benjaminhill.geochat.domain.model.GeoPoint
import kotlin.math.*

/**
 * A utility singleton for geospatial calculations and UI scaling logic.
 *
 * **Purpose:**
 * This object centralizes the math required to determine how "close" two points are and
 * how that distance translates into UI properties (Font Size, Alpha).
 *
 * **Architecture:**
 * - **Layer:** Domain Utilities.
 * - **Relations:**
 *   - Used by the UI layer to render [info.benjaminhill.geochat.ui.components.ProximityMessageRow].
 *   - Uses [GeoPoint] for coordinate math.
 *
 * **Why keep it?**
 * It encapsulates the specific "Distance-Font Algorithm" defined in the project requirements.
 * Keeping this math pure and isolated makes it easy to unit test and tweak the visual feel
 * of the app (e.g., adjusting `MAX_DISTANCE_METERS`) without touching UI code.
 */
object DistanceUtils {
    const val MAX_DISTANCE_METERS = 1000.0
    const val MIN_FONT_SP = 8f
    const val MAX_FONT_SP = 32f

    /**
     * Calculates the distance in meters between two [GeoPoint]s using the Haversine formula.
     */
    fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371e3 // Earth radius in meters
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    /**
     * Converts a distance (in meters) to a 0.0 - 1.0 relevance score.
     * 1.0 = Immediately nearby (0 meters).
     * 0.0 = At or beyond MAX_DISTANCE_METERS.
     */
    fun calculateRelevance(distanceInMeters: Double): Float {
        // relevance = 1f - (distance / MAX).coerceIn(0f, 1f)
        return (1f - (distanceInMeters / MAX_DISTANCE_METERS).toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Scales the relevance score to a font size between MIN_FONT_SP and MAX_FONT_SP.
     */
    fun calculateFontSize(relevance: Float): Float {
        return MIN_FONT_SP + (relevance * (MAX_FONT_SP - MIN_FONT_SP))
    }

    /**
     * Scales the relevance score to an alpha (opacity) value.
     */
    fun calculateAlpha(relevance: Float): Float {
        return 0.3f + (relevance * 0.7f)
    }
}
