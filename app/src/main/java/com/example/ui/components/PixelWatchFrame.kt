package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NetflixRed

/**
 * Renders the circular display for Google Pixel Watch 1.
 * Supports viewing in an authentic Pixel Watch chassis frame (with bezel and crown)
 * or in full round screen mode.
 */
@Composable
fun PixelWatchFrame(
  modifier: Modifier = Modifier,
  onCrownRotate: ((delta: Float) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  var isWatchFrameActive by remember { mutableStateOf(true) }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF070709)),
    contentAlignment = Alignment.Center
  ) {
    val minDimension = minOf(maxWidth, maxHeight)
    // Pixel Watch display size (approx 384dp circular screen)
    val watchDiameter = (minDimension * 0.90f).coerceAtMost(390.dp)

    if (isWatchFrameActive && minDimension > 350.dp) {
      // Outer watch casing: Deep stainless steel bezel + tactile crown
      Box(
        modifier = Modifier
          .size(watchDiameter + 36.dp),
        contentAlignment = Alignment.Center
      ) {
        // Physical crown button on the right edge (Google Pixel Watch design)
        Box(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = 12.dp)
            .size(width = 14.dp, height = 38.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xFF3A3A42), Color(0xFF6B6B78), Color(0xFF282830))
              )
            )
            .border(1.dp, Color(0xFF7A7A8A), RoundedCornerShape(4.dp))
            .clickable { onCrownRotate?.invoke(1f) }
            .testTag("pixel_watch_crown")
        )

        // Matte black 3D circular casing
        Box(
          modifier = Modifier
            .size(watchDiameter + 24.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(Color(0xFF1E1E24), Color(0xFF101014), Color(0xFF050507))
              )
            )
            .border(2.dp, Brush.linearGradient(listOf(Color(0xFF3F3F4E), Color(0xFF1B1B22))), CircleShape)
        )

        // Bezel rim and active AMOLED circular display
        Box(
          modifier = Modifier
            .size(watchDiameter)
            .clip(CircleShape)
            .background(AmoledBlack)
            .testTag("circular_watch_display"),
          contentAlignment = Alignment.Center
        ) {
          content()
        }
      }

      // Small subtle mode toggle indicator at bottom of screen
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 8.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x991E1E26))
          .clickable { isWatchFrameActive = false }
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .testTag("toggle_fullscreen_mode")
      ) {
        Text(
          text = "Pixel Watch 1 View • Tap for Fullscreen",
          color = Color(0xFFAAAAAA),
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium
        )
      }
    } else {
      // Direct fullscreen mode for actual Wear OS round devices or expanded view
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(AmoledBlack),
        contentAlignment = Alignment.Center
      ) {
        // Enforce circular safe layout
        Box(
          modifier = Modifier
            .size(minDimension)
            .clip(CircleShape)
            .background(AmoledBlack)
            .testTag("fullscreen_circular_display"),
          contentAlignment = Alignment.Center
        ) {
          content()
        }

        // Quick back to watch frame toggle
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 6.dp)
            .clip(CircleShape)
            .background(Color(0x882A2A35))
            .clickable { isWatchFrameActive = true }
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("toggle_watch_frame")
        ) {
          Text(
            text = "Watch Frame",
            color = Color(0xFF888899),
            fontSize = 9.sp
          )
        }
      }
    }
  }
}
