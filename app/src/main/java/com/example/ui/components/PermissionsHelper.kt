package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.util.LocationPermissionStage
import com.example.util.PermissionStateModel

@Composable
fun LivePermissionsCard(
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("permissions_ui_prefs", android.content.Context.MODE_PRIVATE) }
    var permissionSnapshot by remember {
        mutableStateOf(PermissionStateModel.checkPermissions(context))
    }
    var isDismissed by remember {
        mutableStateOf(prefs.getBoolean("banner_dismissed", false))
    }

    // Launcher for Foreground permissions (Fine + Coarse + Notifications)
    val foregroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionSnapshot = PermissionStateModel.checkPermissions(context)
        if (permissionSnapshot.hasForegroundLocation) {
            onPermissionsGranted()
        }
    }

    // Only show if foreground location or notifications are not yet granted AND user hasn't dismissed it
    val showBanner = !isDismissed && (!permissionSnapshot.hasForegroundLocation || !permissionSnapshot.hasNotifications)

    AnimatedVisibility(visible = showBanner, modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, LanternAmberBright.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable {
                    val perms = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    foregroundLauncher.launch(perms.toTypedArray())
                }
                .testTag("permissions_banner"),
            color = Color(0xFF1E293B)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = LanternAmberBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enable GPS & Alerts for live road proximity",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = ParchmentPaper,
                        maxLines = 1
                    )
                }

                androidx.compose.material3.IconButton(
                    onClick = {
                        isDismissed = true
                        prefs.edit().putBoolean("banner_dismissed", true).apply()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = ParchmentMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
