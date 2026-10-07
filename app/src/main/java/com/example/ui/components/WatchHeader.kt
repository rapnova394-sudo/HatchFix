package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Watch header displaying real-time clock, server connection indicator, and settings access.
 * Styled to respect the round top bezel safe insets of Pixel Watch 1.
 */
@Composable
fun WatchHeader(
  modifier: Modifier = Modifier,
  isServerConnected: Boolean? = null,
  onSettingsClick: (() -> Unit)? = null
) {
  var currentTime by remember {
    mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
  }

  LaunchedEffect(Unit) {
    while (true) {
      delay(30000)
      currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Current time
    Text(
      text = currentTime,
      color = PureWhite,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.testTag("watch_header_time")
    )

    // Brand accent in center
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(NetflixRed)
      )
      Text(
        text = "WATCHFLIX",
        color = PureWhite,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp
      )
    }

    // Server status and Settings trigger
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      val statusIcon = if (isServerConnected == true) Icons.Default.CloudDone else Icons.Default.CloudOff
      val statusTint = if (isServerConnected == true) SuccessGreen else TextMuted

      Icon(
        imageVector = statusIcon,
        contentDescription = if (isServerConnected == true) "Node.js Server Online" else "Offline / Test Mode",
        tint = statusTint,
        modifier = Modifier.size(11.dp)
      )

      if (onSettingsClick != null) {
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .clickable { onSettingsClick() }
            .testTag("header_settings_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Server Settings",
            tint = PureWhite,
            modifier = Modifier.size(11.dp)
          )
        }
      }
    }
  }
}
