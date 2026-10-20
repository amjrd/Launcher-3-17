package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppFolder
import com.example.data.HomeItem
import com.example.data.InstalledApp
import com.example.data.RealBatteryInfo
import com.example.data.RealMemoryInfo
import com.example.data.RealStorageInfo
import com.example.data.WallpaperType
import com.example.data.WidgetType
import com.example.ui.components.AnalogClockWidget
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppIconView
import com.example.ui.components.AtAGlanceWidget
import com.example.ui.components.DockView
import com.example.ui.components.ExpandedFolderDialog
import com.example.ui.components.FolderIconView
import com.example.ui.components.IosSideDrawer
import com.example.ui.components.LauncherSettingsSheet
import com.example.ui.components.LiveBatterySystemWidget
import com.example.ui.components.LiveStorageAndRamWidget
import com.example.ui.components.MediaPlayerWidget
import com.example.ui.components.StickyNoteWidget
import com.example.ui.components.SystemHardwareControlsWidget
import com.example.ui.components.SystemStatsWidget
import com.example.ui.components.WeatherCardWidget
import com.example.ui.components.WidgetPickerSheet
import com.example.ui.components.getWallpaperPreviewBrush
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherHomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val suggestedApps by viewModel.suggestedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val homePages by viewModel.homePages.collectAsState()
    val dockPackages by viewModel.dockPackages.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val activeFolder by viewModel.activeFolder.collectAsState()
    val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isWidgetsPickerOpen by viewModel.isWidgetsPickerOpen.collectAsState()
    val isSideMenuOpen by viewModel.isSideMenuOpen.collectAsState()
    val selectedAppForMenu by viewModel.selectedAppForMenu.collectAsState()
    val stickyNoteContent by viewModel.stickyNoteContent.collectAsState()

    // Real System State
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val memoryInfo by viewModel.memoryInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val mediaVolume by viewModel.mediaVolume.collectAsState()
    val ringerMode by viewModel.ringerMode.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { homePages.size })
    val coroutineScope = rememberCoroutineScope()

    val appsByPackage = installedApps.associateBy { it.info.packageName }
    val foldersById = folders.associateBy { it.id }

    val dockApps = dockPackages.mapNotNull { appsByPackage[it] }

    val isSelectedAppOnHome = selectedAppForMenu?.let { app ->
        homePages[pagerState.currentPage].any { it.packageName == app.info.packageName }
    } ?: false

    BackHandler(enabled = isSideMenuOpen || isDrawerOpen || isSettingsOpen || isWidgetsPickerOpen || selectedAppForMenu != null || activeFolder != null) {
        when {
            isSideMenuOpen -> viewModel.closeSideMenu()
            activeFolder != null -> viewModel.closeFolder()
            selectedAppForMenu != null -> viewModel.clearAppMenu()
            isDrawerOpen -> viewModel.closeDrawer()
            isSettingsOpen -> viewModel.closeSettings()
            isWidgetsPickerOpen -> viewModel.closeWidgetsPicker()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("launcher_root_container")
    ) {
        // 1. Wallpaper Layer (With Lawnchair Double Tap Detection)
        WallpaperBackground(
            wallpaperType = settings.wallpaperType,
            dimFactor = settings.wallpaperBlur,
            onDoubleTap = { viewModel.onDoubleTapHome() }
        )

        // 2. Desktop Workspace Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (delta < -30 && !isDrawerOpen) {
                            viewModel.openDrawer()
                        } else if (delta > 30 && !isDrawerOpen) {
                            viewModel.onSwipeDownHome()
                        }
                    },
                    orientation = Orientation.Vertical
                )
        ) {
            // iOS Top Bar with Menu Button (line.3.horizontal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.openSideMenu() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.28f))
                        .testTag("side_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Latéral",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.openSettings() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.28f))
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Paramètres",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Default Home Launcher Banner
            if (!settings.isDefaultLauncherPromptDismissed) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lawnchair & Launcher3 Home",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.openDefaultHomeSettings() },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Set Default", fontSize = 11.sp)
                            }
                            IconButton(
                                onClick = { viewModel.dismissDefaultHomePrompt() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Bar: Quick Launcher Controls (Add Widget, Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.clip(CircleShape)
                ) {
                    IconButton(
                        onClick = { viewModel.openWidgetsPicker() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Widget",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.clip(CircleShape)
                ) {
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Launcher Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Desktop Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val items = homePages.getOrElse(pageIndex) { emptyList() }
                DesktopWorkspacePage(
                    items = items,
                    appsByPackage = appsByPackage,
                    foldersById = foldersById,
                    batteryInfo = batteryInfo,
                    memoryInfo = memoryInfo,
                    storageInfo = storageInfo,
                    isFlashlightOn = isFlashlightOn,
                    mediaVolume = mediaVolume,
                    maxVolume = viewModel.maxVolume,
                    ringerMode = ringerMode,
                    showAtAGlance = settings.showAtAGlance,
                    showBatteryInGlance = settings.showBatteryInGlance,
                    showNotificationDots = settings.showNotificationDots,
                    iconShape = settings.iconShape,
                    iconScale = settings.iconScale,
                    showIconLabels = settings.showIconLabels,
                    themedIcons = settings.themedIcons,
                    stickyNoteContent = stickyNoteContent,
                    onNoteChange = { viewModel.updateStickyNote(it) },
                    onAppClick = { app -> viewModel.launchApp(app.info.packageName) },
                    onAppLongClick = { app -> viewModel.selectAppForMenu(app) },
                    onFolderClick = { folder -> viewModel.openFolder(folder) },
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    onVolumeChange = { viewModel.setMediaVolume(it) },
                    onToggleMute = { viewModel.toggleMuteMedia() },
                    onCycleRinger = { viewModel.cycleRingerMode() },
                    onOpenWifi = { viewModel.openWifiSettings() },
                    onOpenBluetooth = { viewModel.openBluetoothSettings() },
                    onOpenDisplay = { viewModel.openDisplaySettings() },
                    onOpenBatterySettings = { viewModel.openBatterySettings() },
                    onOpenStorageSettings = { viewModel.openStorageSettings() },
                    onOpenClockOrAlarm = { viewModel.openClockOrAlarm() },
                    onOpenSettings = { viewModel.openSettings() }
                )
            }

            // Page Indicator Dots
            if (homePages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in homePages.indices) {
                        val isCurrent = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }
            }

            // 3. Bottom Dock Bar (With Lawnchair options)
            DockView(
                dockApps = dockApps,
                iconShape = settings.iconShape,
                iconScale = settings.iconScale,
                themedIcons = settings.themedIcons,
                searchStyle = settings.dockSearchStyle,
                dockBackground = settings.dockBackground,
                onAppClick = { app -> viewModel.launchApp(app.info.packageName) },
                onAppLongClick = { app -> viewModel.selectAppForMenu(app) },
                onSearchClick = { viewModel.openDrawer() },
                onSwipeUpClick = { viewModel.openDrawer() }
            )
        }

        // 4. App Drawer Overlay (Lawnchair Top/Bottom Search & Sizing)
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerSheet(
                isOpen = isDrawerOpen,
                apps = filteredApps,
                suggestedApps = suggestedApps,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                iconShape = settings.iconShape,
                iconScale = settings.iconScale,
                showIconLabels = settings.showIconLabels,
                themedIcons = settings.themedIcons,
                searchPosition = settings.drawerSearchPosition,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onCategorySelect = { viewModel.selectCategory(it) },
                onAppClick = { app ->
                    viewModel.launchApp(app.info.packageName)
                    viewModel.closeDrawer()
                },
                onAppLongClick = { app ->
                    viewModel.selectAppForMenu(app)
                },
                onWebSearchClick = { query ->
                    viewModel.appManager.openWebSearch(query)
                    viewModel.closeDrawer()
                },
                onClose = { viewModel.closeDrawer() }
            )
        }

        // 5. Lawnchair & Launcher3 Settings Overlay
        AnimatedVisibility(
            visible = isSettingsOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            LauncherSettingsSheet(
                isOpen = isSettingsOpen,
                settings = settings,
                onUpdateWallpaper = { viewModel.updateWallpaper(it) },
                onUpdateWallpaperBlur = { viewModel.updateWallpaperBlur(it) },
                onUpdateThemePalette = { viewModel.updateThemePalette(it) },
                onUpdateIconShape = { viewModel.updateIconShape(it) },
                onUpdateIconScale = { viewModel.updateIconScale(it) },
                onToggleIconLabels = { viewModel.toggleIconLabels() },
                onToggleThemedIcons = { viewModel.toggleThemedIcons() },
                onToggleNotificationDots = { viewModel.toggleNotificationDots() },
                onUpdateDockSearchStyle = { viewModel.updateDockSearchStyle(it) },
                onToggleDockBackground = { viewModel.toggleDockBackground() },
                onUpdateDrawerSearchPosition = { viewModel.updateDrawerSearchPosition(it) },
                onUpdateDoubleTapAction = { viewModel.updateDoubleTapAction(it) },
                onUpdateSwipeDownAction = { viewModel.updateSwipeDownAction(it) },
                onUpdateGrid = { r, c -> viewModel.updateGrid(r, c) },
                onToggleAtAGlance = { viewModel.toggleAtAGlance() },
                onOpenDefaultHomeSettings = { viewModel.openDefaultHomeSettings() },
                onResetLayout = { viewModel.resetDefaultLayout() },
                onClose = { viewModel.closeSettings() }
            )
        }

        // 6. Widget Picker Modal
        WidgetPickerSheet(
            isOpen = isWidgetsPickerOpen,
            onAddWidget = { widget ->
                viewModel.addWidgetToHomeScreen(widget, pagerState.currentPage)
            },
            onDismiss = { viewModel.closeWidgetsPicker() }
        )

        // 7. Expanded Folder Dialog
        if (activeFolder != null) {
            val f = activeFolder!!
            val folderApps = f.packageNames.mapNotNull { appsByPackage[it] }
            ExpandedFolderDialog(
                folder = f,
                folderApps = folderApps,
                iconShape = settings.iconShape,
                themedIcons = settings.themedIcons,
                onAppClick = { app -> viewModel.launchApp(app.info.packageName) },
                onAppLongClick = { app -> viewModel.selectAppForMenu(app) },
                onRenameFolder = { newName -> viewModel.renameFolder(f.id, newName) },
                onDismiss = { viewModel.closeFolder() }
            )
        }

        // 8. Context Menu for Apps
        if (selectedAppForMenu != null) {
            val app = selectedAppForMenu!!
            AppContextMenuDialog(
                app = app,
                isOnHomeScreen = isSelectedAppOnHome,
                iconShape = settings.iconShape,
                themedIcons = settings.themedIcons,
                onDismiss = { viewModel.clearAppMenu() },
                onAddToHome = {
                    viewModel.addAppToHomeScreen(app.info.packageName, pagerState.currentPage)
                },
                onRemoveFromHome = {
                    val currentItems = homePages[pagerState.currentPage]
                    val homeItem = currentItems.find { it.packageName == app.info.packageName }
                    if (homeItem != null) {
                        viewModel.removeHomeItem(homeItem.id)
                    } else {
                        viewModel.clearAppMenu()
                    }
                },
                onPinToDock = {
                    viewModel.pinToDock(app.info.packageName)
                },
                onAppInfo = {
                    viewModel.openAppDetails(app.info.packageName)
                },
                onShareApp = {
                    viewModel.shareApp(app.info.packageName, app.info.label)
                },
                onOpenPlayStore = {
                    viewModel.openInPlayStore(app.info.packageName)
                },
                onToggleHideApp = {
                    viewModel.toggleHideApp(app.info.packageName)
                },
                onUninstall = {
                    viewModel.requestUninstall(app.info.packageName)
                }
            )
        }

        // 9. iOS Side Drawer Overlay (Menu Latéral - SideDrawerExample)
        IosSideDrawer(
            isOpen = isSideMenuOpen,
            onClose = { viewModel.closeSideMenu() },
            onOpenSettings = { viewModel.openSettings() },
            onOpenWidgets = { viewModel.openWidgetsPicker() },
            onOpenWallpaper = { viewModel.openSettings() },
            onOpenAppLibrary = { viewModel.openDrawer() },
            onOpenSearch = { viewModel.openDrawer() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperBackground(
    wallpaperType: WallpaperType,
    dimFactor: Float,
    onDoubleTap: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onDoubleClick = onDoubleTap
            )
    ) {
        when (wallpaperType) {
            WallpaperType.ABSTRACT -> {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_abstract),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperType.DARK -> {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_dark),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(getWallpaperPreviewBrush(wallpaperType))
                )
            }
        }

        val baseDim = if (wallpaperType == WallpaperType.DARK) 0.15f else 0.20f
        val totalDim = (baseDim + dimFactor).coerceIn(0f, 0.85f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = totalDim))
        )
    }
}

@Composable
fun DesktopWorkspacePage(
    items: List<HomeItem>,
    appsByPackage: Map<String, InstalledApp>,
    foldersById: Map<String, AppFolder>,
    batteryInfo: RealBatteryInfo,
    memoryInfo: RealMemoryInfo,
    storageInfo: RealStorageInfo,
    isFlashlightOn: Boolean,
    mediaVolume: Int,
    maxVolume: Int,
    ringerMode: Int,
    showAtAGlance: Boolean,
    showBatteryInGlance: Boolean,
    showNotificationDots: Boolean,
    iconShape: com.example.data.IconShape,
    iconScale: Float,
    showIconLabels: Boolean,
    themedIcons: Boolean,
    stickyNoteContent: String,
    onNoteChange: (String) -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    onToggleFlashlight: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onCycleRinger: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenStorageSettings: () -> Unit,
    onOpenClockOrAlarm: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(scrollState)
    ) {
        // At A Glance widget
        val atAGlanceItem = items.find { it.widgetType == WidgetType.AT_A_GLANCE }
        if (atAGlanceItem != null && showAtAGlance) {
            AtAGlanceWidget(
                showBattery = showBatteryInGlance,
                onClick = onOpenClockOrAlarm
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        val nonGlanceItems = items.filterNot { it.widgetType == WidgetType.AT_A_GLANCE }

        // 2x2 Widgets Row
        val compactWidgets = nonGlanceItems.filter {
            it.widgetType in listOf(
                WidgetType.SYSTEM_BATTERY_MONITOR,
                WidgetType.ANALOG_CLOCK,
                WidgetType.WEATHER_CARD,
                WidgetType.STICKY_NOTE
            )
        }

        if (compactWidgets.isNotEmpty()) {
            val chunked2x2 = compactWidgets.chunked(2)
            for (pair in chunked2x2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (wItem in pair) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (wItem.widgetType) {
                                WidgetType.SYSTEM_BATTERY_MONITOR -> LiveBatterySystemWidget(
                                    batteryInfo = batteryInfo,
                                    onOpenBatterySettings = onOpenBatterySettings,
                                    modifier = Modifier.fillMaxSize()
                                )
                                WidgetType.ANALOG_CLOCK -> AnalogClockWidget(
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = onOpenClockOrAlarm
                                )
                                WidgetType.WEATHER_CARD -> WeatherCardWidget(
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = onOpenSettings
                                )
                                WidgetType.STICKY_NOTE -> StickyNoteWidget(
                                    noteContent = stickyNoteContent,
                                    onNoteChange = onNoteChange,
                                    modifier = Modifier.fillMaxSize()
                                )
                                else -> {}
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Full Width System Widgets
        val fullWidthWidgets = nonGlanceItems.filter {
            it.widgetType in listOf(
                WidgetType.SYSTEM_HARDWARE_CONTROLS,
                WidgetType.SYSTEM_STORAGE_RAM,
                WidgetType.SYSTEM_STATS,
                WidgetType.MEDIA_PLAYER
            )
        }

        for (fw in fullWidthWidgets) {
            when (fw.widgetType) {
                WidgetType.SYSTEM_HARDWARE_CONTROLS -> SystemHardwareControlsWidget(
                    isFlashlightOn = isFlashlightOn,
                    mediaVolume = mediaVolume,
                    maxVolume = maxVolume,
                    ringerMode = ringerMode,
                    onToggleFlashlight = onToggleFlashlight,
                    onVolumeChange = onVolumeChange,
                    onToggleMute = onToggleMute,
                    onCycleRinger = onCycleRinger,
                    onOpenWifi = onOpenWifi,
                    onOpenBluetooth = onOpenBluetooth,
                    onOpenDisplay = onOpenDisplay,
                    modifier = Modifier.fillMaxWidth()
                )
                WidgetType.SYSTEM_STORAGE_RAM -> LiveStorageAndRamWidget(
                    memoryInfo = memoryInfo,
                    storageInfo = storageInfo,
                    onOpenStorageSettings = onOpenStorageSettings,
                    modifier = Modifier.fillMaxWidth()
                )
                WidgetType.SYSTEM_STATS -> SystemStatsWidget(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenSettings
                )
                WidgetType.MEDIA_PLAYER -> MediaPlayerWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    onClick = onOpenSettings
                )
                else -> {}
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Desktop App Shortcuts & Folders Grid
        val desktopEntries = nonGlanceItems.filter { it.packageName != null || it.folderId != null }
        val chunkedEntries = desktopEntries.chunked(4)

        for (rowItems in chunkedEntries) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in rowItems) {
                    if (item.folderId != null) {
                        val folder = foldersById[item.folderId]
                        if (folder != null) {
                            val folderApps = folder.packageNames.mapNotNull { appsByPackage[it] }
                            FolderIconView(
                                folder = folder,
                                apps = folderApps,
                                shape = iconShape,
                                iconScale = iconScale,
                                themed = themedIcons,
                                onClick = { onFolderClick(folder) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(72.dp))
                        }
                    } else if (item.packageName != null) {
                        val app = appsByPackage[item.packageName]
                        if (app != null) {
                            val hasDot = showNotificationDots && (
                                    app.info.hasNotification ||
                                            app.info.packageName.contains("message") ||
                                            app.info.packageName.contains("dialer")
                                    )
                            AppIconView(
                                app = app,
                                shape = iconShape,
                                themed = themedIcons,
                                showLabel = showIconLabels,
                                hasNotificationDot = hasDot,
                                iconScale = iconScale,
                                labelColor = Color.White,
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(72.dp))
                        }
                    }
                }
                for (k in 0 until (4 - rowItems.size)) {
                    Spacer(modifier = Modifier.width(72.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
