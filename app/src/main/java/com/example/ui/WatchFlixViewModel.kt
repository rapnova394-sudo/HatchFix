package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.WatchFlixDatabase
import com.example.data.model.Category
import com.example.data.model.VideoItem
import com.example.data.model.WatchProgress
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WatchScreen {
  data object Catalog : WatchScreen
  data class Detail(val video: VideoItem) : WatchScreen
  data class Player(val video: VideoItem, val initialPositionMs: Long = 0L) : WatchScreen
  data object Settings : WatchScreen
}

class WatchFlixViewModel(
  application: Application,
  val repository: VideoRepository
) : AndroidViewModel(application) {

  constructor(application: Application) : this(
    application,
    VideoRepository(WatchFlixDatabase.getDatabase(application).favoriteDao())
  )

  companion object {
    fun provideFactory(application: Application): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val db = WatchFlixDatabase.getDatabase(application)
          val repo = VideoRepository(db.favoriteDao())
          return WatchFlixViewModel(application, repo) as T
        }
      }
  }

  private val _currentScreen = MutableStateFlow<WatchScreen>(WatchScreen.Catalog)
  val currentScreen: StateFlow<WatchScreen> = _currentScreen.asStateFlow()

  private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
  val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

  private val _categories = MutableStateFlow<List<Category>>(emptyList())
  val categories: StateFlow<List<Category>> = _categories.asStateFlow()

  private val _selectedCategory = MutableStateFlow("all")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  val progressMap: StateFlow<Map<String, WatchProgress>> = repository.progressMap
  val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds
  val isServerConnected: StateFlow<Boolean?> = repository.serverConnected

  init {
    loadInitialData()
  }

  fun loadInitialData() {
    viewModelScope.launch {
      _isLoading.value = true
      _categories.value = repository.getCategories()
      _videos.value = repository.getVideos(_selectedCategory.value)
      _isLoading.value = false
    }
  }

  fun selectCategory(categoryId: String) {
    _selectedCategory.value = categoryId
    viewModelScope.launch {
      _isLoading.value = true
      _videos.value = repository.getVideos(categoryId)
      _isLoading.value = false
    }
  }

  fun navigateToDetail(video: VideoItem) {
    _currentScreen.value = WatchScreen.Detail(video)
  }

  fun navigateToPlayer(video: VideoItem) {
    val existingProgress = progressMap.value[video.id]?.positionMs ?: 0L
    _currentScreen.value = WatchScreen.Player(video, existingProgress)
  }

  fun navigateToSettings() {
    _currentScreen.value = WatchScreen.Settings
  }

  fun navigateBack() {
    val current = _currentScreen.value
    when (current) {
      is WatchScreen.Player -> {
        // Return to detail screen of that video
        _currentScreen.value = WatchScreen.Detail(current.video)
      }
      is WatchScreen.Detail -> {
        _currentScreen.value = WatchScreen.Catalog
      }
      is WatchScreen.Settings -> {
        _currentScreen.value = WatchScreen.Catalog
      }
      WatchScreen.Catalog -> {
        // Already at root
      }
    }
  }

  fun updateProgress(videoId: String, positionMs: Long, durationMs: Long) {
    repository.updateWatchProgress(videoId, positionMs, durationMs)
  }

  fun toggleFavorite(videoId: String, video: VideoItem? = null) {
    repository.toggleFavorite(videoId, video)
    if (_selectedCategory.value == "favorites") {
      viewModelScope.launch {
        _videos.value = repository.getVideos("favorites")
      }
    }
  }
}
