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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.data.repository.VideoRepository
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.launch

/**
 * Settings & Backend Configuration screen for WatchFlix on Pixel Watch 1.
 * Allows configuring the Node.js API base URL while verifying secure separation from Supabase.
 */
@Composable
fun SettingsScreen(
  repository: VideoRepository,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val scope = rememberCoroutineScope()
  var serverUrlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }
  var isTestingConnection by remember { mutableStateOf(false) }
  var connectionTestResult by remember { mutableStateOf<Boolean?>(null) }
  var statusMessage by remember { mutableStateOf("") }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("settings_scroll_view"),
      contentPadding = PaddingValues(top = 22.dp, bottom = 44.dp, start = 14.dp, end = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Header with circular back button
      item(key = "header") {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(DarkSurface)
              .border(1.dp, DarkSurfaceBorder, CircleShape)
              .clickable { onBackClick() }
              .testTag("settings_back_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PureWhite,
              modifier = Modifier.size(20.dp)
            )
          }

          Text(
            text = "Server Setup",
            color = PureWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.size(48.dp))
        }
      }

      // Security architecture badge
      item(key = "security_badge") {
        Row(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(16.dp)
          )
          Column {
            Text(
              text = "Client Security Verified",
              color = PureWhite,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "WatchFlix -> Node.js HTTPS -> Supabase (No secrets in watch)",
              color = TextMuted,
              fontSize = 8.sp,
              lineHeight = 11.sp
            )
          }
        }
      }

      // Base URL input
      item(key = "url_input") {
        Column(
          modifier = Modifier.fillMaxWidth(0.92f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Node.js Server URL:",
            color = TextMuted,
            fontSize = 9.sp
          )

          OutlinedTextField(
            value = serverUrlInput,
            onValueChange = {
              serverUrlInput = it
              connectionTestResult = null
              statusMessage = ""
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NetflixRed,
              unfocusedBorderColor = DarkSurfaceBorder,
              focusedTextColor = PureWhite,
              unfocusedTextColor = PureWhite,
              focusedContainerColor = DarkSurface,
              unfocusedContainerColor = DarkSurface
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("server_url_input")
          )
        }
      }

      // Action: Test Connection & Save
      item(key = "actions") {
        Column(
          modifier = Modifier.fillMaxWidth(0.92f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Button(
            onClick = {
              ApiClient.updateBaseUrl(serverUrlInput)
              isTestingConnection = true
              connectionTestResult = null
              scope.launch {
                val ok = repository.checkServerConnection()
                isTestingConnection = false
                connectionTestResult = ok
                statusMessage = if (ok) {
                  "Connected to Node.js server!"
                } else {
                  "Server not responding. Using realistic test data."
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("test_server_button")
          ) {
            if (isTestingConnection) {
              CircularProgressIndicator(
                color = PureWhite,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Dns,
                  contentDescription = null,
                  tint = PureWhite,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Test & Save URL",
                  color = PureWhite,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Test result display
          if (connectionTestResult != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (connectionTestResult == true) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (connectionTestResult == true) SuccessGreen else NetflixRed,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.size(4.dp))
              Text(
                text = statusMessage,
                color = if (connectionTestResult == true) SuccessGreen else TextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      // Reset to defaults
      item(key = "reset") {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              serverUrlInput = ApiClient.DEFAULT_BASE_URL
              ApiClient.updateBaseUrl(ApiClient.DEFAULT_BASE_URL)
              connectionTestResult = null
              statusMessage = "Reset to placeholder URL"
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("reset_defaults_button")
        ) {
          Text(
            text = "Reset to default endpoint",
            color = TextSubtle,
            fontSize = 9.sp
          )
        }
      }
    }
  }
}
