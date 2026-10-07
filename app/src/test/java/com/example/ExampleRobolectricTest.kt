package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.WatchFlixDatabase
import com.example.data.repository.VideoRepository
import com.example.ui.WatchFlixViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: WatchFlixDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, WatchFlixDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WatchFlix", appName)
  }

  @Test
  fun `room database inserts and queries favorites correctly`() = runBlocking {
    val dao = db.favoriteDao()
    val entity = FavoriteVideoEntity(
      videoId = "vid-123",
      title = "Test Sci-Fi",
      posterUrl = "https://example.com/poster.jpg",
      category = "Sci-Fi"
    )

    dao.insertFavorite(entity)

    val favoriteIds = dao.getAllFavoriteIds().first()
    assertTrue("Favorite IDs must contain vid-123", favoriteIds.contains("vid-123"))

    val isFav = dao.isFavorite("vid-123").first()
    assertTrue("isFavorite must return true", isFav)

    // Delete favorite
    dao.deleteFavoriteById("vid-123")
    val updatedIds = dao.getAllFavoriteIds().first()
    assertFalse("Favorite IDs must not contain vid-123 after delete", updatedIds.contains("vid-123"))
  }

  @Test
  fun `repository returns videos and categories in fallback mock mode`() = runBlocking {
    val repository = VideoRepository(favoriteDao = db.favoriteDao())
    val videos = repository.getVideos()
    val categories = repository.getCategories()

    assertTrue("Videos catalog should not be empty", videos.isNotEmpty())
    assertTrue("Categories should not be empty", categories.isNotEmpty())

    val firstVideo = repository.getVideoById("1")
    assertNotNull("Video with ID 1 should exist", firstVideo)
    assertEquals("Big Buck Bunny", firstVideo?.title)
  }

  @Test
  fun `repository updates watch progress correctly`() {
    val repository = VideoRepository(favoriteDao = db.favoriteDao())
    repository.updateWatchProgress(videoId = "1", positionMs = 30000L, durationMs = 60000L)

    val progress = repository.progressMap.value["1"]
    assertNotNull(progress)
    assertEquals(30000L, progress?.positionMs)
    assertEquals(60000L, progress?.durationMs)
    assertEquals(0.5f, progress?.progressPercent ?: 0f, 0.01f)
  }

  @Test
  fun `viewModel instantiates correctly with Application context`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = WatchFlixViewModel(context)
    assertNotNull("ViewModel instance must not be null", vm)
    assertNotNull("ViewModel repository must not be null", vm.repository)

    val factory = WatchFlixViewModel.provideFactory(context)
    val vmFromFactory = factory.create(WatchFlixViewModel::class.java)
    assertNotNull("ViewModel from factory must not be null", vmFromFactory)
  }
}
