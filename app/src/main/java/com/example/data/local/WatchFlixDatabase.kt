package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Local Room database providing on-device persistence for favorites and user state.
 */
@Database(entities = [FavoriteVideoEntity::class], version = 1, exportSchema = false)
abstract class WatchFlixDatabase : RoomDatabase() {

  abstract fun favoriteDao(): FavoriteDao

  companion object {
    @Volatile
    private var INSTANCE: WatchFlixDatabase? = null

    fun getDatabase(context: Context): WatchFlixDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          WatchFlixDatabase::class.java,
          "watchflix_local.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
