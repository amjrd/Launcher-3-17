package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.IconShape
import com.example.data.InstalledApp

@Composable
fun AppContextMenuDialog(
    app: InstalledApp?,
    isOnHomeScreen: Boolean = false,
    iconShape: IconShape,
    themedIcons: Boolean,
    onDismiss: () -> Unit,
    onAddToHome: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onPinToDock: () -> Unit,
    onAppInfo: () -> Unit,
    onShareApp: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onToggleHideApp: () -> Unit,
    onUninstall: () -> Unit
) {
    if (app == null) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("app_context_menu_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with App Icon and Names
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIconView(
                        app = app,
                        shape = iconShape,
                        themed = themedIcons,
                        showLabel = false,
                        iconSize = 48.dp,
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = app.info.label,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = app.info.packageName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))

                // Menu items
                if (!isOnHomeScreen) {
                    MenuItemRow(
                        icon = Icons.Default.Add,
                        label = "Add to Home Screen",
                        onClick = onAddToHome
                    )
                } else {
                    MenuItemRow(
                        icon = Icons.Default.DeleteOutline,
                        label = "Remove from Home",
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = onRemoveFromHome
                    )
                }

                MenuItemRow(
                    icon = Icons.Default.PushPin,
                    label = "Pin to Dock",
                    onClick = onPinToDock
                )

                MenuItemRow(
                    icon = Icons.Default.Share,
                    label = "Share App",
                    onClick = onShareApp
                )

                MenuItemRow(
                    icon = Icons.Default.Shop,
                    label = "Open in Play Store",
                    onClick = onOpenPlayStore
                )

                MenuItemRow(
                    icon = Icons.Default.VisibilityOff,
                    label = "Hide from Drawer",
                    onClick = onToggleHideApp
                )

                MenuItemRow(
                    icon = Icons.Default.Info,
                    label = "App Info",
                    onClick = onAppInfo
                )

                if (!app.info.isSystemApp) {
                    MenuItemRow(
                        icon = Icons.Default.Delete,
                        label = "Uninstall",
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = onUninstall
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
