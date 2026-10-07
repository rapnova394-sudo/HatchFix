package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity storing user favorite videos locally on the smartwatch.
 */
@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
  @PrimaryKey val videoId: String,
  val title: String,
  val posterUrl: String,
  val category: String,
  val addedAt: Long = System.currentTimeMillis()
)
