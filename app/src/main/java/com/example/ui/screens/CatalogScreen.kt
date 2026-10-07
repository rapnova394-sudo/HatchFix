package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Category
import com.example.data.model.VideoItem
import com.example.data.model.WatchProgress
import com.example.ui.components.WatchHeader
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.launch

/**
 * Main catalog screen optimized for the round Google Pixel Watch 1 screen.
 * Displays scrollable movies and series with posters, titles, synopses, and quick play buttons.
 */
@Composable
fun CatalogScreen(
  videos: List<VideoItem>,
  categories: List<Category>,
  selectedCategory: String,
  isLoading: Boolean,
  progressMap: Map<String, WatchProgress>,
  favoriteIds: Set<String>,
  isServerConnected: Boolean?,
  onSelectCategory: (String) -> Unit,
  onVideoClick: (VideoItem) -> Unit,
  onPlayClick: (VideoItem) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
  ) {
    if (isLoading && videos.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          color = NetflixRed,
          strokeWidth = 3.dp,
          modifier = Modifier.size(32.dp)
        )
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .testTag("catalog_list"),
        // Generous vertical padding allows items to roll comfortably into the circular center
        contentPadding = PaddingValues(top = 28.dp, bottom = 48.dp, start = 14.dp, end = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Watch header row
        item(key = "header") {
          WatchHeader(
            isServerConnected = isServerConnected,
            onSettingsClick = onOpenSettings,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }

        // Horizontal category chip filter
        item(key = "categories") {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("categories_row")
          ) {
            items(categories, key = { it.id }) { cat ->
              val isSelected = selectedCategory.equals(cat.id, ignoreCase = true)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSelected) NetflixRed else DarkSurfaceElevated)
                  .border(
                    1.dp,
                    if (isSelected) NetflixRed else DarkSurfaceBorder,
                    RoundedCornerShape(16.dp)
                  )
                  .clickable { onSelectCategory(cat.id) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("category_${cat.id}"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = cat.name,
                  color = if (isSelected) PureWhite else TextMuted,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              }
            }
          }
        }

        // Empty state check
        if (videos.isEmpty()) {
          item(key = "empty") {
            Column(
              modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              if (selectedCategory == "favorites") {
                Icon(
                  imageVector = Icons.Default.FavoriteBorder,
                  contentDescription = null,
                  tint = TextMuted,
                  modifier = Modifier.size(24.dp)
                )
                Text(
                  text = "No favorite videos yet",
                  color = PureWhite,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Tap the heart on any video to save it locally.",
                  color = TextMuted,
                  fontSize = 9.sp,
                  modifier = Modifier.padding(horizontal = 20.dp)
                )
              } else {
                Text(
                  text = "No videos available",
                  color = TextMuted,
                  fontSize = 12.sp
                )
              }
            }
          }
        }

        // Video items
        items(videos, key = { it.id }) { video ->
          val progress = progressMap[video.id]
          val isFav = favoriteIds.contains(video.id)

          VideoCatalogCard(
            video = video,
            progress = progress,
            isFavorite = isFav,
            onClick = { onVideoClick(video) },
            onPlay = { onPlayClick(video) },
            onToggleFavorite = { onToggleFavorite(video.id) }
          )
        }
      }
    }
  }
}

/**
 * Individual video card shaped and padded for round smartwatch interactions.
 */
@Composable
fun VideoCatalogCard(
  video: VideoItem,
  progress: WatchProgress?,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onPlay: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Box(
    modifier = modifier
      .fillMaxWidth(0.94f)
      .clip(RoundedCornerShape(18.dp))
      .background(DarkSurface)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag("video_card_${video.id}")
  ) {
    Column {
      // Poster banner with top tags & quick play overlay
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(96.dp)
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(video.posterUrl)
            .crossfade(true)
            .build(),
          contentDescription = video.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, Color(0x99000000), DarkSurface)
              )
            )
        )

        // Category & duration pill in top-left
        Row(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xCC000000))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = video.category,
              color = PureWhite,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xCC000000))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = video.formattedDuration,
              color = TextMuted,
              fontSize = 8.sp
            )
          }
        }

        // Heart favorite toggle button in top-right (meets >= 48dp touch target)
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(2.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable { onToggleFavorite() }
            .testTag("heart_button_${video.id}"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) NetflixRed else PureWhite,
            modifier = Modifier.size(20.dp)
          )
        }

        // Circular Play Button (meets >= 48dp accessibility touch target)
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 10.dp, bottom = 4.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(NetflixRed)
            .clickable { onPlay() }
            .testTag("play_button_${video.id}"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play ${video.title}",
            tint = PureWhite,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      // Card metadata and short synopsis
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp)
      ) {
        Text(
          text = video.title,
          color = PureWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = video.description,
          color = TextMuted,
          fontSize = 10.sp,
          lineHeight = 13.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        // Resume progress bar if partially watched
        if (progress != null && progress.progressPercent > 0.05f) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .clip(CircleShape)
                .background(Color(0xFF333340))
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(progress.progressPercent)
                  .height(3.dp)
                  .clip(CircleShape)
                  .background(NetflixRed)
              )
            }
            Text(
              text = "${(progress.progressPercent * 100).toInt()}%",
              color = TextSubtle,
              fontSize = 8.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}
