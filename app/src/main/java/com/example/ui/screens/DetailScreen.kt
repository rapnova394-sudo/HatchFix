package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItem
import com.example.data.model.WatchProgress
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle

/**
 * Detailed view of a movie/series item tailored for the circular screen of Pixel Watch 1.
 */
@Composable
fun DetailScreen(
  video: VideoItem,
  progress: WatchProgress?,
  isFavorite: Boolean,
  onPlayClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val context = LocalContext.current

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("detail_scroll_view"),
      contentPadding = PaddingValues(top = 20.dp, bottom = 44.dp, start = 16.dp, end = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top Navigation / Back button header
      item(key = "top_bar") {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Circular back button (meets >= 48dp touch target)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(DarkSurface)
              .border(1.dp, DarkSurfaceBorder, CircleShape)
              .clickable { onBackClick() }
              .testTag("detail_back_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to catalog",
              tint = PureWhite,
              modifier = Modifier.size(20.dp)
            )
          }

          // Heart / Favorite button (meets >= 48dp touch target)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(DarkSurface)
              .border(1.dp, DarkSurfaceBorder, CircleShape)
              .clickable { onToggleFavorite() }
              .testTag("detail_heart_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
              tint = if (isFavorite) NetflixRed else PureWhite,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Circular/Pill Hero Poster
      item(key = "poster") {
        Box(
          modifier = Modifier
            .size(140.dp, 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(video.backdropUrl.ifEmpty { video.posterUrl })
              .crossfade(true)
              .build(),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, Color(0x99000000))
                )
              )
          )
        }
      }

      // Video Title
      item(key = "title") {
        Text(
          text = video.title,
          color = PureWhite,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 12.dp)
        )
      }

      // Metadata pill tags
      item(key = "tags") {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF2B2B36))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = video.rating,
              color = PureWhite,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "${video.year}",
            color = TextMuted,
            fontSize = 9.sp
          )

          Text(
            text = "•",
            color = TextSubtle,
            fontSize = 9.sp
          )

          Text(
            text = video.formattedDuration,
            color = TextMuted,
            fontSize = 9.sp
          )
        }
      }

      // Primary Large Play CTA Button (Wear OS touch target)
      item(key = "play_cta") {
        Button(
          onClick = { onPlayClick() },
          colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(48.dp)
            .testTag("detail_play_cta")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(20.dp)
            )
            val buttonText = if (progress != null && progress.progressPercent > 0.05f) {
              "Resume (${(progress.progressPercent * 100).toInt()}%)"
            } else {
              "Play Video"
            }
            Text(
              text = buttonText,
              color = PureWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Full Synopsis description
      item(key = "description") {
        Box(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(10.dp)
        ) {
          Text(
            text = video.description,
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Start
          )
        }
      }

      // Battery & Watch Hardware Optimization Info
      item(key = "hw_info") {
        Row(
          modifier = Modifier
            .fillMaxWidth(0.88f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF16161E))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = NetflixRed,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Hardware Accelerated • AMOLED Battery Saver",
            color = TextSubtle,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
