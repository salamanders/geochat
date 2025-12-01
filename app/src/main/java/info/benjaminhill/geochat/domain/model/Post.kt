package info.benjaminhill.geochat.domain.model

import java.util.Date

data class Post(
    val id: String,
    val userId: String,
    val userDisplayName: String,
    val text: String,
    val timestamp: Date,
    val location: GeoPoint,
    val plusCode: String
)
