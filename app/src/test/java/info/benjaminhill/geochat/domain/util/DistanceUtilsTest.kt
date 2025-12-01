package info.benjaminhill.geochat.domain.util

import info.benjaminhill.geochat.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceUtilsTest {

    @Test
    fun testDistanceCalculation() {
        // Known distance between two points
        val p1 = GeoPoint(37.7749, -122.4194) // San Francisco
        val p2 = GeoPoint(34.0522, -118.2437) // Los Angeles
        // Approx 559 km
        val distance = DistanceUtils.calculateDistance(p1, p2)
        assertEquals(559000.0, distance, 10000.0) // Within 10km accuracy is fine for this test
    }

    @Test
    fun testRelevance() {
        // 0 meters -> 1.0 relevance
        assertEquals(1.0f, DistanceUtils.calculateRelevance(0.0), 0.001f)

        // 500 meters -> 0.5 relevance (given MAX 1000)
        assertEquals(0.5f, DistanceUtils.calculateRelevance(500.0), 0.001f)

        // 1000 meters -> 0.0 relevance
        assertEquals(0.0f, DistanceUtils.calculateRelevance(1000.0), 0.001f)

        // 1500 meters -> 0.0 relevance (clamped)
        assertEquals(0.0f, DistanceUtils.calculateRelevance(1500.0), 0.001f)
    }

    @Test
    fun testFontSize() {
        // Relevance 1.0 -> Max Font
        assertEquals(DistanceUtils.MAX_FONT_SP, DistanceUtils.calculateFontSize(1.0f), 0.001f)

        // Relevance 0.0 -> Min Font
        assertEquals(DistanceUtils.MIN_FONT_SP, DistanceUtils.calculateFontSize(0.0f), 0.001f)
    }
}
