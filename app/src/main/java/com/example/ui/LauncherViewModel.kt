package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppFolder
import com.example.data.AppItem
import com.example.data.AppManager
import com.example.data.DockSearchStyle
import com.example.data.DrawerSearchPosition
import com.example.data.GestureAction
import com.example.data.HomeItem
import com.example.data.IconShape
import com.example.data.InstalledApp
import com.example.data.LauncherFont
import com.example.data.LauncherSettings
import com.example.data.RealBatteryInfo
import com.example.data.RealMemoryInfo
import com.example.data.RealStorageInfo
import com.example.data.SystemManager
import com.example.data.ThemePalette
import com.example.data.WallpaperType
import com.example.data.WidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val appManager = AppManager(application)
    val systemManager = SystemManager(application, viewModelScope)

    // Real System Hardware & Stats StateFlows
    val batteryInfo: StateFlow<RealBatteryInfo> = systemManager.batteryInfo
    val memoryInfo: StateFlow<RealMemoryInfo> = systemManager.memoryInfo
    val storageInfo: StateFlow<RealStorageInfo> = systemManager.storageInfo
    val isFlashlightOn: StateFlow<Boolean> = systemManager.isFlashlightOn
    val mediaVolume: StateFlow<Int> = systemManager.mediaVolume
    val maxVolume: Int = systemManager.maxMediaVolume
    val ringerMode: StateFlow<Int> = systemManager.ringerMode

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredApps: StateFlow<List<InstalledApp>> = combine(
        _installedApps,
        _searchQuery,
        _selectedCategory,
        _settings
    ) { apps, query, category, settings ->
        var list = apps
        if (query.isBlank()) {
            list = list.filterNot { settings.hiddenPackages.contains(it.info.packageName) }
        }
        if (category != "All") {
            list = list.filter { it.info.category.equals(category, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.info.label.lowercase().contains(q) || it.info.packageName.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestedApps: StateFlow<List<InstalledApp>> = _installedApps.combine(_installedApps) { apps, _ ->
        apps.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _homePages = MutableStateFlow<List<List<HomeItem>>>(listOf(emptyList(), emptyList()))
    val homePages: StateFlow<List<List<HomeItem>>> = _homePages.asStateFlow()

    private val _folders = MutableStateFlow<List<AppFolder>>(emptyList())
    val folders: StateFlow<List<AppFolder>> = _folders.asStateFlow()

    private val _activeFolder = MutableStateFlow<AppFolder?>(null)
    val activeFolder: StateFlow<AppFolder?> = _activeFolder.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _dockPackages = MutableStateFlow<List<String>>(emptyList())
    val dockPackages: StateFlow<List<String>> = _dockPackages.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isWidgetsPickerOpen = MutableStateFlow(false)
    val isWidgetsPickerOpen: StateFlow<Boolean> = _isWidgetsPickerOpen.asStateFlow()

    private val _isSideMenuOpen = MutableStateFlow(false)
    val isSideMenuOpen: StateFlow<Boolean> = _isSideMenuOpen.asStateFlow()

    private val _selectedAppForMenu = MutableStateFlow<InstalledApp?>(null)
    val selectedAppForMenu: StateFlow<InstalledApp?> = _selectedAppForMenu.asStateFlow()

    private val _stickyNoteContent = MutableStateFlow("Tap to edit memo\n- System widgets active\n- Torch & Volume controls working\n- Live Battery & RAM monitoring")
    val stickyNoteContent: StateFlow<String> = _stickyNoteContent.asStateFlow()

    init {
        refreshApps()
    }

    override fun onCleared() {
        super.onCleared()
        systemManager.unregister()
    }

    fun refreshApps() {
        viewModelScope.launch {
            val apps = appManager.loadInstalledApps()
            _installedApps.value = apps
            initDefaultHomeIfEmpty(apps)
        }
    }

    private fun initDefaultHomeIfEmpty(apps: List<InstalledApp>) {
        if (_homePages.value[0].isNotEmpty()) return

        val page0Items = mutableListOf<HomeItem>()

        // 1. At a Glance widget at top (span 4x1)
        page0Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 0,
                row = 0,
                col = 0,
                spanX = 4,
                spanY = 1,
                widgetType = WidgetType.AT_A_GLANCE
            )
        )

        // 2. Real System Live Battery Widget (span 2x2)
        page0Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 0,
                row = 1,
                col = 0,
                spanX = 2,
                spanY = 2,
                widgetType = WidgetType.SYSTEM_BATTERY_MONITOR
            )
        )

        // 3. Real Analog Clock Widget (span 2x2)
        page0Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 0,
                row = 1,
                col = 2,
                spanX = 2,
                spanY = 2,
                widgetType = WidgetType.ANALOG_CLOCK
            )
        )

        // 4. Real Hardware Controls Widget (span 4x2) with Flashlight, Volume Slider, Ringer, Quick Toggles
        page0Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 0,
                row = 3,
                col = 0,
                spanX = 4,
                spanY = 2,
                widgetType = WidgetType.SYSTEM_HARDWARE_CONTROLS
            )
        )

        // 5. App shortcuts & folders on row 5 or page 0
        val availableAppPackages = apps.map { it.info.packageName }
        var appIdx = 0

        // Create a default "Google / Tools" folder if there are several tools
        val toolsApps = apps.filter { it.info.category == "Tools" || it.info.category == "Internet" || it.info.category == "Communication" }
        if (toolsApps.size >= 3) {
            val folderId = UUID.randomUUID().toString()
            val folder = AppFolder(
                id = folderId,
                name = "Essentials",
                packageNames = toolsApps.take(4).map { it.info.packageName }
            )
            _folders.value = listOf(folder)
            page0Items.add(
                HomeItem(
                    id = UUID.randomUUID().toString(),
                    page = 0,
                    row = 5,
                    col = 0,
                    spanX = 1,
                    spanY = 1,
                    folderId = folderId
                )
            )
        }

        // Fill remaining slots on row 5
        val startCol = if (_folders.value.isNotEmpty()) 1 else 0
        for (col in startCol until 4) {
            if (appIdx < availableAppPackages.size) {
                page0Items.add(
                    HomeItem(
                        id = UUID.randomUUID().toString(),
                        page = 0,
                        row = 5,
                        col = col,
                        spanX = 1,
                        spanY = 1,
                        packageName = availableAppPackages[appIdx]
                    )
                )
                appIdx++
            }
        }

        // Page 1: Storage & RAM Stats, Weather Card, Sticky Note, Media Player
        val page1Items = mutableListOf<HomeItem>()

        // Real Storage & RAM Live Widget
        page1Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 1,
                row = 0,
                col = 0,
                spanX = 4,
                spanY = 1,
                widgetType = WidgetType.SYSTEM_STORAGE_RAM
            )
        )

        // Weather Card (2x2)
        page1Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 1,
                row = 1,
                col = 0,
                spanX = 2,
                spanY = 2,
                widgetType = WidgetType.WEATHER_CARD
            )
        )

        // Sticky Note (2x2)
        page1Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 1,
                row = 1,
                col = 2,
                spanX = 2,
                spanY = 2,
                widgetType = WidgetType.STICKY_NOTE
            )
        )

        // Media Player
        page1Items.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = 1,
                row = 3,
                col = 0,
                spanX = 4,
                spanY = 1,
                widgetType = WidgetType.MEDIA_PLAYER
            )
        )

        // App shortcuts on page 1
        for (r in 4..5) {
            for (c in 0 until 4) {
                if (appIdx < availableAppPackages.size) {
                    page1Items.add(
                        HomeItem(
                            id = UUID.randomUUID().toString(),
                            page = 1,
                            row = r,
                            col = c,
                            spanX = 1,
                            spanY = 1,
                            packageName = availableAppPackages[appIdx]
                        )
                    )
                    appIdx++
                }
            }
        }

        _homePages.value = listOf(page0Items, page1Items)

        // Dock: 5 pinned apps
        val dockList = mutableListOf<String>()
        val defaultDockPreferences = listOf("phone", "dialer", "message", "chrome", "camera", "setting")
        for (pref in defaultDockPreferences) {
            val found = apps.find { it.info.packageName.contains(pref) || it.info.label.lowercase().contains(pref) }
            if (found != null && !dockList.contains(found.info.packageName) && dockList.size < 5) {
                dockList.add(found.info.packageName)
            }
        }
        for (app in apps) {
            if (dockList.size >= 5) break
            if (!dockList.contains(app.info.packageName)) {
                dockList.add(app.info.packageName)
            }
        }
        _dockPackages.value = dockList
    }

    // Hardware Actions
    fun toggleFlashlight() {
        appManager.triggerHapticFeedback()
        systemManager.toggleFlashlight()
    }

    fun setMediaVolume(vol: Int) {
        systemManager.setMediaVolume(vol)
    }

    fun toggleMuteMedia() {
        appManager.triggerHapticFeedback()
        systemManager.toggleMuteMedia()
    }

    fun cycleRingerMode() {
        appManager.triggerHapticFeedback()
        systemManager.cycleRingerMode()
    }

    fun openBatterySettings() {
        appManager.triggerHapticFeedback()
        systemManager.openBatterySettings()
    }

    fun openStorageSettings() {
        appManager.triggerHapticFeedback()
        systemManager.openStorageSettings()
    }

    fun openClockOrAlarm() {
        appManager.triggerHapticFeedback()
        systemManager.openClockOrAlarm()
    }

    fun openWifiSettings() {
        appManager.triggerHapticFeedback()
        systemManager.openWifiSettings()
    }

    fun openBluetoothSettings() {
        appManager.triggerHapticFeedback()
        systemManager.openBluetoothSettings()
    }

    fun openDisplaySettings() {
        appManager.triggerHapticFeedback()
        systemManager.openDisplaySettings()
    }

    fun openDefaultHomeSettings() {
        appManager.triggerHapticFeedback()
        systemManager.openDefaultHomeSettings()
        dismissDefaultHomePrompt()
    }

    fun dismissDefaultHomePrompt() {
        _settings.value = _settings.value.copy(isDefaultLauncherPromptDismissed = true)
    }

    // Folders
    fun openFolder(folder: AppFolder) {
        _activeFolder.value = folder
        appManager.triggerHapticFeedback()
    }

    fun closeFolder() {
        _activeFolder.value = null
    }

    fun renameFolder(folderId: String, newName: String) {
        _folders.value = _folders.value.map {
            if (it.id == folderId) it.copy(name = newName) else it
        }
        _activeFolder.value = _activeFolder.value?.let {
            if (it.id == folderId) it.copy(name = newName) else it
        }
        appManager.triggerHapticFeedback()
    }

    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
    }

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setPage(page: Int) {
        _currentPage.value = page.coerceIn(0, _homePages.value.size - 1)
    }

    fun openDrawer() {
        _isDrawerOpen.value = true
        _searchQuery.value = ""
        appManager.triggerHapticFeedback()
    }

    fun closeDrawer() {
        _isDrawerOpen.value = false
        _searchQuery.value = ""
    }

    fun openSettings() {
        _isSettingsOpen.value = true
        appManager.triggerHapticFeedback()
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun openWidgetsPicker() {
        _isWidgetsPickerOpen.value = true
        appManager.triggerHapticFeedback()
    }

    fun closeWidgetsPicker() {
        _isWidgetsPickerOpen.value = false
    }

    fun openSideMenu() {
        _isSideMenuOpen.value = true
        appManager.triggerHapticFeedback()
    }

    fun closeSideMenu() {
        _isSideMenuOpen.value = false
    }

    fun toggleSideMenu() {
        _isSideMenuOpen.value = !_isSideMenuOpen.value
        appManager.triggerHapticFeedback()
    }

    fun selectAppForMenu(app: InstalledApp) {
        _selectedAppForMenu.value = app
        appManager.triggerHapticFeedback()
    }

    fun clearAppMenu() {
        _selectedAppForMenu.value = null
    }

    fun addAppToHomeScreen(packageName: String, page: Int = _currentPage.value) {
        val currentPages = _homePages.value.toMutableList()
        val targetPageItems = currentPages[page].toMutableList()

        targetPageItems.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = page,
                row = 5,
                col = (targetPageItems.size % 4),
                spanX = 1,
                spanY = 1,
                packageName = packageName
            )
        )
        currentPages[page] = targetPageItems
        _homePages.value = currentPages
        appManager.triggerHapticFeedback()
        clearAppMenu()
        closeDrawer()
    }

    fun removeHomeItem(id: String) {
        val currentPages = _homePages.value.map { pageItems ->
            pageItems.filterNot { it.id == id }
        }
        _homePages.value = currentPages
        appManager.triggerHapticFeedback()
        clearAppMenu()
    }

    fun addWidgetToHomeScreen(widgetType: WidgetType, page: Int = _currentPage.value) {
        val currentPages = _homePages.value.toMutableList()
        val targetPageItems = currentPages[page].toMutableList()

        targetPageItems.add(
            HomeItem(
                id = UUID.randomUUID().toString(),
                page = page,
                row = 0,
                col = 0,
                spanX = widgetType.spanX,
                spanY = widgetType.spanY,
                widgetType = widgetType
            )
        )
        currentPages[page] = targetPageItems
        _homePages.value = currentPages
        appManager.triggerHapticFeedback()
        closeWidgetsPicker()
    }

    fun pinToDock(packageName: String, slot: Int = -1) {
        val current = _dockPackages.value.toMutableList()
        current.remove(packageName)
        if (slot in 0 until current.size) {
            current.add(slot, packageName)
        } else {
            if (current.size >= 5) {
                current.removeAt(current.size - 1)
            }
            current.add(packageName)
        }
        _dockPackages.value = current.take(5)
        appManager.triggerHapticFeedback()
        clearAppMenu()
    }

    fun updateWallpaper(type: WallpaperType) {
        _settings.value = _settings.value.copy(wallpaperType = type)
        appManager.triggerHapticFeedback()
    }

    fun updateWallpaperBlur(blur: Float) {
        _settings.value = _settings.value.copy(wallpaperBlur = blur)
    }

    fun updateThemePalette(palette: ThemePalette) {
        _settings.value = _settings.value.copy(themePalette = palette)
        appManager.triggerHapticFeedback()
    }

    fun updateIconShape(shape: IconShape) {
        _settings.value = _settings.value.copy(iconShape = shape)
        appManager.triggerHapticFeedback()
    }

    fun toggleThemedIcons() {
        _settings.value = _settings.value.copy(themedIcons = !_settings.value.themedIcons)
        appManager.triggerHapticFeedback()
    }

    fun toggleNotificationDots() {
        _settings.value = _settings.value.copy(showNotificationDots = !_settings.value.showNotificationDots)
        appManager.triggerHapticFeedback()
    }

    fun updateGrid(rows: Int, cols: Int) {
        _settings.value = _settings.value.copy(gridRows = rows, gridCols = cols)
        appManager.triggerHapticFeedback()
    }

    fun toggleAtAGlance() {
        _settings.value = _settings.value.copy(showAtAGlance = !_settings.value.showAtAGlance)
    }

    fun updateStickyNote(text: String) {
        _stickyNoteContent.value = text
    }

    fun resetDefaultLayout() {
        _homePages.value = listOf(emptyList(), emptyList())
        initDefaultHomeIfEmpty(_installedApps.value)
        _settings.value = LauncherSettings()
        appManager.triggerHapticFeedback()
    }

    fun launchApp(packageName: String) {
        appManager.triggerHapticFeedback()
        appManager.launchApp(packageName)
    }

    fun openAppDetails(packageName: String) {
        clearAppMenu()
        appManager.openAppDetails(packageName)
    }

    fun requestUninstall(packageName: String) {
        clearAppMenu()
        appManager.requestUninstall(packageName)
    }

    // Lawnchair Superpowers
    fun shareApp(packageName: String, label: String) {
        clearAppMenu()
        appManager.shareApp(packageName, label)
    }

    fun openInPlayStore(packageName: String) {
        clearAppMenu()
        appManager.openInPlayStore(packageName)
    }

    fun toggleHideApp(packageName: String) {
        val current = _settings.value.hiddenPackages.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _settings.value = _settings.value.copy(hiddenPackages = current)
        appManager.triggerHapticFeedback()
        clearAppMenu()
    }

    fun updateIconScale(scale: Float) {
        _settings.value = _settings.value.copy(iconScale = scale)
    }

    fun toggleIconLabels() {
        _settings.value = _settings.value.copy(showIconLabels = !_settings.value.showIconLabels)
        appManager.triggerHapticFeedback()
    }

    fun updateDockSearchStyle(style: DockSearchStyle) {
        _settings.value = _settings.value.copy(dockSearchStyle = style)
        appManager.triggerHapticFeedback()
    }

    fun updateDrawerSearchPosition(pos: DrawerSearchPosition) {
        _settings.value = _settings.value.copy(drawerSearchPosition = pos)
        appManager.triggerHapticFeedback()
    }

    fun updateDoubleTapAction(action: GestureAction) {
        _settings.value = _settings.value.copy(doubleTapAction = action)
        appManager.triggerHapticFeedback()
    }

    fun updateSwipeDownAction(action: GestureAction) {
        _settings.value = _settings.value.copy(swipeDownAction = action)
        appManager.triggerHapticFeedback()
    }

    fun toggleDockBackground() {
        _settings.value = _settings.value.copy(dockBackground = !_settings.value.dockBackground)
        appManager.triggerHapticFeedback()
    }

    fun onDoubleTapHome() {
        appManager.triggerHapticFeedback()
        executeGesture(_settings.value.doubleTapAction)
    }

    fun onSwipeDownHome() {
        appManager.triggerHapticFeedback()
        executeGesture(_settings.value.swipeDownAction)
    }

    private fun executeGesture(action: GestureAction) {
        when (action) {
            GestureAction.QUICK_CONTROLS -> openSettings()
            GestureAction.NOTIFICATION_SHADE -> systemManager.openDisplaySettings()
            GestureAction.OPEN_SEARCH -> openDrawer()
            GestureAction.OPEN_SETTINGS -> openSettings()
            GestureAction.NONE -> {}
        }
    }
}
