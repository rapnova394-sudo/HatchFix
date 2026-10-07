package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WatchFlixViewModel
import com.example.ui.WatchScreen
import com.example.ui.components.PixelWatchFrame
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        WatchFlixApp()
      }
    }
  }
}

@Composable
fun WatchFlixApp(
  context: Application = LocalContext.current.applicationContext as Application,
  viewModel: WatchFlixViewModel = viewModel(factory = WatchFlixViewModel.provideFactory(context))
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val videos by viewModel.videos.collectAsState()
  val categories by viewModel.categories.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val progressMap by viewModel.progressMap.collectAsState()
  val favoriteIds by viewModel.favoriteIds.collectAsState()
  val isServerConnected by viewModel.isServerConnected.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AmoledBlack)
  ) {
    PixelWatchFrame {
      when (val screen = currentScreen) {
        is WatchScreen.Catalog -> {
          CatalogScreen(
            videos = videos,
            categories = categories,
            selectedCategory = selectedCategory,
            isLoading = isLoading,
            progressMap = progressMap,
            favoriteIds = favoriteIds,
            isServerConnected = isServerConnected,
            onSelectCategory = { viewModel.selectCategory(it) },
            onVideoClick = { viewModel.navigateToDetail(it) },
            onPlayClick = { viewModel.navigateToPlayer(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenSettings = { viewModel.navigateToSettings() }
          )
        }

        is WatchScreen.Detail -> {
          DetailScreen(
            video = screen.video,
            progress = progressMap[screen.video.id],
            isFavorite = favoriteIds.contains(screen.video.id),
            onPlayClick = { viewModel.navigateToPlayer(screen.video) },
            onToggleFavorite = { viewModel.toggleFavorite(screen.video.id, screen.video) },
            onBackClick = { viewModel.navigateBack() }
          )
        }

        is WatchScreen.Player -> {
          PlayerScreen(
            video = screen.video,
            initialPositionMs = screen.initialPositionMs,
            onProgressUpdate = { pos, dur ->
              viewModel.updateProgress(screen.video.id, pos, dur)
            },
            onClose = { viewModel.navigateBack() }
          )
        }

        is WatchScreen.Settings -> {
          SettingsScreen(
            repository = viewModel.repository,
            onBackClick = { viewModel.navigateBack() }
          )
        }
      }
    }
  }
}
