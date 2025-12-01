package info.benjaminhill.geochat.domain.util

import info.benjaminhill.geochat.domain.model.GeoPoint
import kotlin.math.*

object DistanceUtils {
    const val MAX_DISTANCE_METERS = 1000.0
    const val MIN_FONT_SP = 8f
    const val MAX_FONT_SP = 32f

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

    fun calculateRelevance(distanceInMeters: Double): Float {
        // relevance = 1f - (distance / MAX).coerceIn(0f, 1f)
        return (1f - (distanceInMeters / MAX_DISTANCE_METERS).toFloat()).coerceIn(0f, 1f)
    }

    fun calculateFontSize(relevance: Float): Float {
        return MIN_FONT_SP + (relevance * (MAX_FONT_SP - MIN_FONT_SP))
    }

    fun calculateAlpha(relevance: Float): Float {
        return 0.3f + (relevance * 0.7f)
    }
}
