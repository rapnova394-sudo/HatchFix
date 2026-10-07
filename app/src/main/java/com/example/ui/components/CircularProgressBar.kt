package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DialTrack
import com.example.ui.theme.NetflixRed

/**
 * Draws an unobtrusive circular progress ring along the outer edge of the Pixel Watch screen.
 * Perfect for video playback time tracking on circular displays.
 */
@Composable
fun CircularProgressArc(
  progress: Float, // 0.0f to 1.0f
  modifier: Modifier = Modifier,
  strokeWidth: Float = 6f,
  trackColor: Color = DialTrack,
  progressColor: Color = NetflixRed
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val diameter = minOf(size.width, size.height) - strokeWidth * 2
    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
    val arcSize = Size(diameter, diameter)

    // Full 360-degree background track
    drawArc(
      color = trackColor,
      startAngle = -90f,
      sweepAngle = 360f,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeWidth)
    )

    // Active progress arc starting from top (12 o'clock = -90 degrees)
    val clampedProgress = progress.coerceIn(0f, 1f)
    if (clampedProgress > 0f) {
      drawArc(
        color = progressColor,
        startAngle = -90f,
        sweepAngle = clampedProgress * 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )
    }
  }
}
