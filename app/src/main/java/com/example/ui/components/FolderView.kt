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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppFolder
import com.example.data.IconShape
import com.example.data.InstalledApp

@Composable
fun FolderIconView(
    folder: AppFolder,
    apps: List<InstalledApp>,
    shape: IconShape,
    themed: Boolean,
    iconSize: Dp = 54.dp,
    iconScale: Float = 1.0f,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cornerShape = getIconShape(shape)
    val scaledSize = iconSize * iconScale

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width((72 * iconScale).dp.coerceAtLeast(64.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("folder_icon_${folder.id}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(scaledSize)
                .shadow(elevation = 2.dp, shape = cornerShape)
                .clip(cornerShape)
                .background(Color.White.copy(alpha = 0.22f))
                .padding(4.dp)
        ) {
            // 2x2 Mini Apps Grid Preview
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val previewApps = apps.take(4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0 until 2) {
                        val app = previewApps.getOrNull(i)
                        if (app != null) {
                            MiniAppDot(app = app, themed = themed)
                        } else {
                            Spacer(modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 2 until 4) {
                        val app = previewApps.getOrNull(i)
                        if (app != null) {
                            MiniAppDot(app = app, themed = themed)
                        } else {
                            Spacer(modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = folder.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 1f),
                    blurRadius = 3f
                )
            ),
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun MiniAppDot(
    app: InstalledApp,
    themed: Boolean
) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(
                if (themed) MaterialTheme.colorScheme.primaryContainer
                else getFallbackBgColor(app.info.packageName)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (themed) {
            ThemedAppIcon(
                app = app,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                size = 10.dp
            )
        } else {
            FallbackAppIcon(app = app, size = 10.dp)
        }
    }
}

@Composable
fun ExpandedFolderDialog(
    folder: AppFolder?,
    folderApps: List<InstalledApp>,
    iconShape: IconShape,
    themedIcons: Boolean,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (folder == null) return

    var isEditingName by remember { mutableStateOf(false) }
    var folderNameInput by remember(folder.name) { mutableStateOf(folder.name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("expanded_folder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Folder Name and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = folderNameInput,
                            onValueChange = { folderNameInput = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = {
                            onRenameFolder(folderNameInput)
                            isEditingName = false
                        }) {
                            Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isEditingName = true }
                        ) {
                            Text(
                                text = folder.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Folder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Folder Apps Grid (4 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    items(folderApps, key = { it.info.packageName }) { app ->
                        AppIconView(
                            app = app,
                            shape = iconShape,
                            themed = themedIcons,
                            showLabel = true,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                onAppClick(app)
                                onDismiss()
                            },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            }
        }
    }
}
