package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var isIosMode by remember { mutableStateOf(true) }
    var activeCategorySheet by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Communication", "Internet", "Media", "Productivity", "System", "Tools")
    val alphabet = ('A'..'Z').toList()

    // Group apps by category for iOS Bento Grid
    val groupedApps: Map<String, List<InstalledApp>> = remember(apps) {
        val groups = mutableMapOf<String, MutableList<InstalledApp>>()
        for (app in apps) {
            val cat = when (app.info.category) {
                "Communication" -> "Social"
                "Internet" -> "Utilitaire"
                "Media" -> "Créativité"
                "Entertainment" -> "Divertissement"
                "Productivity" -> "Productivité"
                "System" -> "Système"
                else -> "Outils"
            }
            groups.getOrPut(cat) { mutableListOf() }.add(app)
        }
        groups
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_sheet"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // iOS Header & Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isIosMode) "Bibliothèque d'apps" else "Toutes les applications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { isIosMode = !isIosMode },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector = if (isIosMode) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Basculer la vue",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Search Bar (iOS Style)
            DrawerSearchBar(
                searchQuery = searchQuery,
                onQueryChange = onQueryChange,
                onClose = onClose,
                onSearchAction = {
                    if (apps.isNotEmpty()) onAppClick(apps.first())
                    else if (searchQuery.isNotBlank()) onWebSearchClick(searchQuery)
                }
            )

            // Category Chips if not in iOS mode and search is blank
            if (!isIosMode && searchQuery.isBlank()) {
                DrawerCategoryChips(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = onCategorySelect
                )
            }

            // MAIN CONTENT AREA
            if (isIosMode && searchQuery.isBlank()) {
                // 1. iOS App Library Bento Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ios_app_library_grid")
                ) {
                    items(groupedApps.keys.toList(), key = { it }) { categoryName ->
                        val catApps = groupedApps[categoryName] ?: emptyList()
                        IosCategoryCard(
                            categoryName = categoryName,
                            apps = catApps,
                            iconShape = iconShape,
                            themedIcons = themedIcons,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick,
                            onCategoryClick = { activeCategorySheet = categoryName }
                        )
                    }
                }
            } else {
                // 2. Alphabetical / Search Results View
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
                                    text = "TOUTES LES APPLICATIONS",
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
                                        text = "Aucune application trouvée pour \"$searchQuery\"",
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
                                        Text("Rechercher sur le Web \"$searchQuery\"")
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
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            val index = apps.indexOfFirst {
                                                it.info.label.startsWith(letter, ignoreCase = true)
                                            }
                                            if (index >= 0) {
                                                coroutineScope.launch {
                                                    gridState.animateScrollToItem(index)
                                                }
                                            }
                                        }
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composant Carte de Catégorie (Style iOS App Library Bento Box)
 */
@Composable
fun IosCategoryCard(
    categoryName: String,
    apps: List<InstalledApp>,
    iconShape: IconShape,
    themedIcons: Boolean,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onCategoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onCategoryClick)
            .padding(12.dp)
    ) {
        // Mini grille 2x2 des icônes d'applications
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val displayApps = apps.take(if (apps.size > 4) 3 else 4)
            items(displayApps) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAppClick(app) }
                        .padding(2.dp)
                ) {
                    AppIconView(
                        app = app,
                        shape = iconShape,
                        themed = themedIcons,
                        showLabel = false,
                        iconSize = 42.dp,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.info.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Slot 4: Si plus de 4 apps, afficher le cluster de dossier iOS
            if (apps.size > 4) {
                item {
                    val remainingCount = apps.size - 3
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f))
                            .clickable { onCategoryClick() }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Dossier",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "+$remainingCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Titre de la Catégorie (Style iOS)
        Text(
            text = categoryName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 2.dp)
        )
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
