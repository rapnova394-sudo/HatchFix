package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Model representing a video item in the WatchFlix catalog.
 * Designed to deserialize directly from the Node.js backend (which interfaces with Supabase).
 */
@JsonClass(generateAdapter = true)
data class VideoItem(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "description") val description: String,
  @Json(name = "poster_url") val posterUrl: String,
  @Json(name = "backdrop_url") val backdropUrl: String,
  @Json(name = "video_url") val videoUrl: String,
  @Json(name = "duration_seconds") val durationSeconds: Int,
  @Json(name = "category") val category: String,
  @Json(name = "year") val year: Int = 2024,
  @Json(name = "rating") val rating: String = "PG-13",
  @Json(name = "is_featured") val isFeatured: Boolean = false,
  @Json(name = "tags") val tags: List<String> = emptyList()
) {
  val formattedDuration: String
    get() {
      val minutes = durationSeconds / 60
      val remainingSeconds = durationSeconds % 60
      return if (minutes > 0) "${minutes}m ${remainingSeconds}s" else "${remainingSeconds}s"
    }
}

@JsonClass(generateAdapter = true)
data class Category(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class WatchProgress(
  @Json(name = "video_id") val videoId: String,
  @Json(name = "position_ms") val positionMs: Long,
  @Json(name = "duration_ms") val durationMs: Long,
  @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
  val progressPercent: Float
    get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
