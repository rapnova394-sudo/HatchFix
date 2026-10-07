package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteVideoEntity
import com.example.data.model.Category
import com.example.data.model.VideoItem
import com.example.data.model.WatchProgress
import com.example.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository providing video catalog, detail lookups, and watch progress.
 * Integrates Room database for local favorites persistence across app restarts.
 * Attempts to query the user's Node.js server via HTTPS; if endpoints are not yet deployed,
 * it seamlessly falls back to realistic test movies and test streams.
 */
class VideoRepository(
  private val favoriteDao: FavoriteDao? = null
) {

  private val scope = CoroutineScope(Dispatchers.IO)

  private val _progressMap = MutableStateFlow<Map<String, WatchProgress>>(emptyMap())
  val progressMap: StateFlow<Map<String, WatchProgress>> = _progressMap.asStateFlow()

  private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

  private val _serverConnected = MutableStateFlow<Boolean?>(null)
  val serverConnected: StateFlow<Boolean?> = _serverConnected.asStateFlow()

  init {
    if (favoriteDao != null) {
      scope.launch {
        favoriteDao.getAllFavoriteIds().collect { idList ->
          _favoriteIds.value = idList.toSet()
        }
      }
    }
  }

  // Realistic mock catalog populated with reliable public sample video streams
  private val mockVideos = listOf(
    VideoItem(
      id = "1",
      title = "Big Buck Bunny",
      description = "A large and lovable rabbit takes sweet revenge on bullying forest creatures.",
      posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
      durationSeconds = 596,
      category = "Animation",
      year = 2023,
      rating = "ALL",
      isFeatured = true,
      tags = listOf("4K", "OLED Ready", "Popular")
    ),
    VideoItem(
      id = "2",
      title = "Tears of Steel",
      description = "Dystopian sci-fi set in Amsterdam where survivors battle robotic creatures to fix the timeline.",
      posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
      durationSeconds = 734,
      category = "Sci-Fi",
      year = 2024,
      rating = "16+",
      isFeatured = false,
      tags = listOf("HDR", "Sci-Fi", "Action")
    ),
    VideoItem(
      id = "3",
      title = "Sintel",
      description = "A lonely young traveler forms an unbreakable bond with a wounded baby dragon.",
      posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
      durationSeconds = 888,
      category = "Fantasy",
      year = 2023,
      rating = "PG-13",
      isFeatured = true,
      tags = listOf("Epic", "Fantasy", "Drama")
    ),
    VideoItem(
      id = "4",
      title = "Elephants Dream",
      description = "Two explorers navigate the bizarre mechanical insides of a giant, shifting machine.",
      posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
      durationSeconds = 653,
      category = "Sci-Fi",
      year = 2022,
      rating = "PG",
      isFeatured = false,
      tags = listOf("Cyberpunk", "Surreal")
    ),
    VideoItem(
      id = "5",
      title = "Cosmic Blazes",
      description = "High-octane racing through futuristic orbital highways under extreme gravity.",
      posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
      durationSeconds = 15,
      category = "Action",
      year = 2024,
      rating = "13+",
      isFeatured = false,
      tags = listOf("Speed", "Thriller")
    ),
    VideoItem(
      id = "6",
      title = "Bullrun Odyssey",
      description = "An adrenaline-fueled cross-country endurance sprint across desert horizons.",
      posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80",
      backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80",
      videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
      durationSeconds = 60,
      category = "Action",
      year = 2024,
      rating = "PG",
      isFeatured = false,
      tags = listOf("Docu", "Adrenaline")
    )
  )

  private val mockCategories = listOf(
    Category("all", "All"),
    Category("favorites", "Favorites"),
    Category("featured", "Featured"),
    Category("Animation", "Animation"),
    Category("Sci-Fi", "Sci-Fi"),
    Category("Action", "Action"),
    Category("Fantasy", "Fantasy")
  )

  suspend fun getVideos(categoryFilter: String? = null): List<VideoItem> = withContext(Dispatchers.IO) {
    if (categoryFilter == "favorites") {
      val all = try {
        val service = ApiClient.getService()
        val response = service.getVideos()
        if (response.isSuccessful && !response.body().isNullOrEmpty()) response.body()!! else mockVideos
      } catch (_: Exception) {
        mockVideos
      }
      return@withContext all.filter { _favoriteIds.value.contains(it.id) }
    }

    try {
      val service = ApiClient.getService()
      val response = service.getVideos(category = if (categoryFilter == "all" || categoryFilter == "featured") null else categoryFilter)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        _serverConnected.value = true
        val videos = response.body()!!
        return@withContext if (categoryFilter == "featured") {
          videos.filter { it.isFeatured }
        } else {
          videos
        }
      }
    } catch (_: Exception) {
      _serverConnected.value = false
    }

    // Fallback to local realistic mock data
    return@withContext filterMockVideos(categoryFilter)
  }

  suspend fun getVideoById(id: String): VideoItem? = withContext(Dispatchers.IO) {
    try {
      val response = ApiClient.getService().getVideoDetails(id)
      if (response.isSuccessful && response.body() != null) {
        return@withContext response.body()
      }
    } catch (_: Exception) {
      // Fallback
    }
    return@withContext mockVideos.find { it.id == id }
  }

  suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
    try {
      val response = ApiClient.getService().getCategories()
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        return@withContext response.body()!!
      }
    } catch (_: Exception) {
      // Fallback
    }
    return@withContext mockCategories
  }

  suspend fun checkServerConnection(): Boolean = withContext(Dispatchers.IO) {
    try {
      val response = ApiClient.getService().checkHealth()
      val ok = response.isSuccessful
      _serverConnected.value = ok
      ok
    } catch (_: Exception) {
      _serverConnected.value = false
      false
    }
  }

  fun updateWatchProgress(videoId: String, positionMs: Long, durationMs: Long) {
    val progress = WatchProgress(
      videoId = videoId,
      positionMs = positionMs,
      durationMs = durationMs
    )
    val current = _progressMap.value.toMutableMap()
    current[videoId] = progress
    _progressMap.value = current

    // Send asynchronously to Node.js server (non-blocking)
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launchSilently {
      try {
        ApiClient.getService().updateWatchProgress(progress)
      } catch (_: Exception) {
        // Ignored; local state already updated
      }
    }
  }

  fun toggleFavorite(videoId: String, video: VideoItem? = null) {
    val current = _favoriteIds.value.toMutableSet()
    val isFav = current.contains(videoId)
    if (isFav) {
      current.remove(videoId)
      _favoriteIds.value = current
      if (favoriteDao != null) {
        scope.launch {
          try {
            favoriteDao.deleteFavoriteById(videoId)
          } catch (_: Exception) { }
        }
      }
    } else {
      current.add(videoId)
      _favoriteIds.value = current
      if (favoriteDao != null) {
        scope.launch {
          try {
            val item = video ?: mockVideos.find { it.id == videoId }
            val entity = FavoriteVideoEntity(
              videoId = videoId,
              title = item?.title ?: "Video $videoId",
              posterUrl = item?.posterUrl ?: "",
              category = item?.category ?: "Movie",
              addedAt = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(entity)
          } catch (_: Exception) { }
        }
      }
    }
  }

  private fun filterMockVideos(categoryFilter: String?): List<VideoItem> {
    if (categoryFilter.isNullOrBlank() || categoryFilter == "all") {
      return mockVideos
    }
    if (categoryFilter == "favorites") {
      return mockVideos.filter { _favoriteIds.value.contains(it.id) }
    }
    if (categoryFilter == "featured") {
      return mockVideos.filter { it.isFeatured }
    }
    return mockVideos.filter { it.category.equals(categoryFilter, ignoreCase = true) }
  }
}

private fun kotlinx.coroutines.CoroutineScope.launchSilently(block: suspend () -> Unit) {
  launch(Dispatchers.IO) {
    try {
      block()
    } catch (_: Exception) {
      // Silent failure for background progress report
    }
  }
}
