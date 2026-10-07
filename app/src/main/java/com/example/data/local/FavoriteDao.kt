package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local favorite videos persistence.
 */
@Dao
interface FavoriteDao {

  @Query("SELECT * FROM favorite_videos ORDER BY addedAt DESC")
  fun getAllFavorites(): Flow<List<FavoriteVideoEntity>>

  @Query("SELECT videoId FROM favorite_videos")
  fun getAllFavoriteIds(): Flow<List<String>>

  @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE videoId = :videoId)")
  fun isFavorite(videoId: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(favorite: FavoriteVideoEntity)

  @Query("DELETE FROM favorite_videos WHERE videoId = :videoId")
  suspend fun deleteFavoriteById(videoId: String)
}
