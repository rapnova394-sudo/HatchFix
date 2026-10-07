package com.example.ui.screens

import android.app.Activity
import android.media.AudioManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.ui.components.CircularProgressArc
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay

/**
 * High-efficiency, circular-optimized video player for Google Pixel Watch 1.
 * Features:
 * - Hardware accelerated playback via Media3 ExoPlayer
 * - Touch-first circular controls with minimum 48dp touch targets
 * - Outer dial progress ring + linear seeker
 * - Play/Pause, +10s forward, -10s rewind
 * - Volume control overlay with step buttons
 * - Battery saver mode for Wear OS AMOLED endurance
 * - Automatic progress reporting
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
  video: VideoItem,
  initialPositionMs: Long = 0L,
  onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onClose() }

  val context = LocalContext.current
  val audioManager = remember {
    context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager
  }

  // Manage Screen WakeLock on Wear OS during playback
  DisposableEffect(Unit) {
    val activity = context as? Activity
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  // Player state
  var isPlaying by remember { mutableStateOf(true) }
  var isBuffering by remember { mutableStateOf(true) }
  var currentPositionMs by remember { mutableLongStateOf(initialPositionMs) }
  var durationMs by remember { mutableLongStateOf(video.durationSeconds * 1000L) }
  var areControlsVisible by remember { mutableStateOf(true) }
  var isVolumeControlOpen by remember { mutableStateOf(false) }
  var isBatterySaverActive by remember { mutableStateOf(false) }

  // Volume state (0.0 to 1.0)
  var volumeLevel by remember {
    val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
    val curr = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
    mutableFloatStateOf((curr.toFloat() / max.toFloat()).coerceIn(0f, 1f))
  }

  // Initialize ExoPlayer with battery and hardware considerations
  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(video.videoUrl))
      prepare()
      seekTo(initialPositionMs)
      playWhenReady = true
    }
  }

  // Listen to playback state & update position periodically
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        isBuffering = playbackState == Player.STATE_BUFFERING
        if (playbackState == Player.STATE_READY) {
          if (exoPlayer.duration > 0) {
            durationMs = exoPlayer.duration
          }
        }
      }

      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }
    }
    exoPlayer.addListener(listener)

    onDispose {
      onProgressUpdate(exoPlayer.currentPosition, exoPlayer.duration)
      exoPlayer.removeListener(listener)
      exoPlayer.stop()
      exoPlayer.release()
    }
  }

  // Progress ticker loop
  LaunchedEffect(isPlaying) {
    while (true) {
      if (exoPlayer.isPlaying) {
        currentPositionMs = exoPlayer.currentPosition
        if (exoPlayer.duration > 0) {
          durationMs = exoPlayer.duration
        }
        onProgressUpdate(currentPositionMs, durationMs)
      }
      delay(500)
    }
  }

  // Auto-hide controls after inactivity
  LaunchedEffect(areControlsVisible, isPlaying, isVolumeControlOpen) {
    if (areControlsVisible && isPlaying && !isVolumeControlOpen) {
      delay(3500)
      areControlsVisible = false
    }
  }

  val progressPercent = if (durationMs > 0) {
    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
  } else 0f

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        areControlsVisible = !areControlsVisible
        if (!areControlsVisible) isVolumeControlOpen = false
      }
      .testTag("player_container")
  ) {
    // 1. Hardware-accelerated Video Surface
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("exoplayer_surface")
    )

    // Battery saver dimming veil (conserves Pixel Watch OLED power)
    if (isBatterySaverActive) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0x55000000))
      )
    }

    // Circular Bezel Progress Arc (Active throughout playback along watch edge)
    CircularProgressArc(
      progress = progressPercent,
      strokeWidth = 5f,
      trackColor = Color(0x33FFFFFF),
      progressColor = NetflixRed
    )

    // Buffering Spinner
    if (isBuffering) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          color = NetflixRed,
          strokeWidth = 3.dp,
          modifier = Modifier.size(36.dp)
        )
      }
    }

    // 2. Custom Wear OS Touch Controls Overlay
    AnimatedVisibility(
      visible = areControlsVisible,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0x88000000)) // Translucent shade for visibility
      ) {
        // Top bezel buttons: Close & Battery Saver
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(top = 16.dp, start = 20.dp, end = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Close button (>= 48dp touch target)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0x99181822))
              .clickable { onClose() }
              .testTag("player_close_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close player",
              tint = PureWhite,
              modifier = Modifier.size(20.dp)
            )
          }

          // Video title pill (truncated for circular screen)
          Text(
            text = video.title,
            color = PureWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 6.dp)
          )

          // Battery Saver Mode Toggle (>= 48dp touch target)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (isBatterySaverActive) WarningAmber else Color(0x99181822))
              .clickable { isBatterySaverActive = !isBatterySaverActive }
              .testTag("player_battery_saver_toggle"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.BatterySaver,
              contentDescription = "Battery Saver",
              tint = if (isBatterySaverActive) AmoledBlack else PureWhite,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Center Transport Controls: [ -10s ] [ Play/Pause ] [ +10s ]
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // -10s Rewind (>= 48dp)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0x99242430))
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .clickable {
                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                exoPlayer.seekTo(newPos)
                currentPositionMs = newPos
              }
              .testTag("player_seek_back_10"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Replay10,
              contentDescription = "Rewind 10 seconds",
              tint = PureWhite,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          // Big Center Play/Pause button (60dp touch target)
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(NetflixRed)
              .clickable {
                if (exoPlayer.isPlaying) {
                  exoPlayer.pause()
                } else {
                  exoPlayer.play()
                }
              }
              .testTag("player_play_pause_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = PureWhite,
              modifier = Modifier.size(34.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          // +10s Forward (>= 48dp)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0x99242430))
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .clickable {
                val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(durationMs)
                exoPlayer.seekTo(newPos)
                currentPositionMs = newPos
              }
              .testTag("player_seek_forward_10"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Forward10,
              contentDescription = "Forward 10 seconds",
              tint = PureWhite,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Bottom Controls: Progress Bar, Timestamp, and Volume Button
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 20.dp, start = 24.dp, end = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Time readout: Current / Total
          Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = formatTime(currentPositionMs),
              color = PureWhite,
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium
            )

            // Volume Toggle button (>= 48dp touch target)
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isVolumeControlOpen) NetflixRed else Color(0x7720202A))
                .clickable { isVolumeControlOpen = !isVolumeControlOpen }
                .testTag("player_volume_toggle"),
              contentAlignment = Alignment.Center
            ) {
              val volIcon = when {
                volumeLevel <= 0.05f -> Icons.AutoMirrored.Filled.VolumeMute
                volumeLevel < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                else -> Icons.AutoMirrored.Filled.VolumeUp
              }
              Icon(
                imageVector = volIcon,
                contentDescription = "Volume control",
                tint = PureWhite,
                modifier = Modifier.size(18.dp)
              )
            }

            Text(
              text = formatTime(durationMs),
              color = TextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium
            )
          }

          // Seek Slider optimized for thumb drags
          Slider(
            value = progressPercent,
            onValueChange = { newPercent ->
              val targetMs = (newPercent * durationMs).toLong()
              exoPlayer.seekTo(targetMs)
              currentPositionMs = targetMs
            },
            colors = SliderDefaults.colors(
              thumbColor = NetflixRed,
              activeTrackColor = NetflixRed,
              inactiveTrackColor = Color(0x55FFFFFF)
            ),
            modifier = Modifier
              .fillMaxWidth(0.86f)
              .height(28.dp)
              .testTag("player_seek_slider")
          )
        }
      }
    }

    // 3. Dedicated Circular Volume Control Overlay
    if (isVolumeControlOpen) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xDD0B0B10))
          .clickable { isVolumeControlOpen = false }
          .testTag("volume_overlay"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = "Volume",
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Volume Down button (>= 48dp)
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, CircleShape)
                .clickable {
                  val newVol = (volumeLevel - 0.15f).coerceAtLeast(0f)
                  volumeLevel = newVol
                  audioManager?.let { am ->
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (newVol * max).toInt(), 0)
                  }
                }
                .testTag("volume_down_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = "Lower volume",
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
              )
            }

            // Percentage readout
            Text(
              text = "${(volumeLevel * 100).toInt()}%",
              color = PureWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.width(44.dp)
            )

            // Volume Up button (>= 48dp)
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, CircleShape)
                .clickable {
                  val newVol = (volumeLevel + 0.15f).coerceAtMost(1f)
                  volumeLevel = newVol
                  audioManager?.let { am ->
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (newVol * max).toInt(), 0)
                  }
                }
                .testTag("volume_up_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Raise volume",
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Done button (>= 48dp)
          Box(
            modifier = Modifier
              .padding(top = 4.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(NetflixRed)
              .clickable { isVolumeControlOpen = false }
              .padding(horizontal = 20.dp, vertical = 8.dp)
              .testTag("volume_done_button")
          ) {
            Text(
              text = "OK",
              color = PureWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return "%02d:%02d".format(minutes, seconds)
}
