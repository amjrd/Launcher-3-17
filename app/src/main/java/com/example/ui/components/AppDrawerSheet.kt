package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrawerSearchPosition
import com.example.data.IconShape
import com.example.data.InstalledApp
import kotlinx.coroutines.launch

@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    apps: List<InstalledApp>,
    suggestedApps: List<InstalledApp>,
    searchQuery: String,
    selectedCategory: String,
    iconShape: IconShape,
    iconScale: Float = 1.0f,
    showIconLabels: Boolean = true,
    themedIcons: Boolean,
    searchPosition: DrawerSearchPosition = DrawerSearchPosition.TOP,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onWebSearchClick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    BackHandler {
        onClose()
    }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("All", "Communication", "Internet", "Media", "Productivity", "System", "Tools")
    val alphabet = ('A'..'Z').toList()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_sheet"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // If search is at TOP
            if (searchPosition == DrawerSearchPosition.TOP) {
                DrawerSearchBar(
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onClose = onClose,
                    onSearchAction = {
                        if (apps.isNotEmpty()) onAppClick(apps.first())
                        else if (searchQuery.isNotBlank()) onWebSearchClick(searchQuery)
                    }
                )

                // Category Chips Row
                DrawerCategoryChips(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = onCategorySelect
                )
            }

            // Main App Grid + A-Z Fast Scroller Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    state = gridState,
                    contentPadding = PaddingValues(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("app_drawer_grid")
                ) {
                    // Suggested Apps Section
                    if (searchQuery.isBlank() && suggestedApps.isNotEmpty() && selectedCategory == "All") {
                        item(span = { GridItemSpan(4) }) {
                            Column(modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)) {
                                Text(
                                    text = "SUGGESTIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (app in suggestedApps) {
                                        AppIconView(
                                            app = app,
                                            shape = iconShape,
                                            themed = themedIcons,
                                            showLabel = showIconLabels,
                                            iconScale = iconScale,
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            onClick = { onAppClick(app) },
                                            onLongClick = { onAppLongClick(app) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    // All Apps Label
                    if (searchQuery.isBlank() && selectedCategory == "All") {
                        item(span = { GridItemSpan(4) }) {
                            Text(
                                text = "ALL APPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }

                    // Empty Search Results State
                    if (apps.isEmpty()) {
                        item(span = { GridItemSpan(4) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No apps found for \"$searchQuery\"",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onWebSearchClick(searchQuery) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Search Web for \"$searchQuery\"")
                                }
                            }
                        }
                    } else {
                        items(apps, key = { it.info.packageName }) { app ->
                            AppIconView(
                                app = app,
                                shape = iconShape,
                                themed = themedIcons,
                                showLabel = showIconLabels,
                                iconScale = iconScale,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }
                }

                // A-Z Quick Fast Scroller Sidebar
                if (searchQuery.isBlank() && apps.size > 8) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(28.dp)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (letter in alphabet) {
                            Text(
                                text = letter.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val targetIdx = apps.indexOfFirst {
                                            it.info.label.startsWith(letter, ignoreCase = true)
                                        }
                                        if (targetIdx != -1) {
                                            coroutineScope.launch {
                                                gridState.animateScrollToItem(targetIdx + 1)
                                            }
                                        }
                                    }
                                    .padding(vertical = 1.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            // If search is at BOTTOM (Lawnchair signature)
            if (searchPosition == DrawerSearchPosition.BOTTOM) {
                DrawerCategoryChips(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = onCategorySelect
                )

                DrawerSearchBar(
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onClose = onClose,
                    onSearchAction = {
                        if (apps.isNotEmpty()) onAppClick(apps.first())
                        else if (searchQuery.isNotBlank()) onWebSearchClick(searchQuery)
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerSearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onSearchAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close Drawer",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .testTag("drawer_search_input"),
            placeholder = {
                Text(
                    text = "Search apps...",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchAction() })
        )
    }
}

@Composable
private fun DrawerCategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (cat in categories.take(4)) {
            val isSelected = selectedCategory == cat
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(cat) },
                label = { Text(cat, fontSize = 12.sp) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
