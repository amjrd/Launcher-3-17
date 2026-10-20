package com.example.data

import androidx.compose.ui.graphics.ImageBitmap

enum class IconShape(val label: String, val cornerPercent: Int) {
    SQUIRCLE("Squircle", 35),
    CIRCLE("Circle", 50),
    ROUNDED_SQUARE("Rounded Square", 22),
    TEARDROP("Teardrop", 40),
    HEXAGON("Hexagon", 28),
    CLOVER("Clover Leaf", 45),
    OCTAGON("Octagon", 30)
}

enum class WallpaperType(val label: String, val drawableResName: String) {
    ABSTRACT("Material Glass", "wallpaper_abstract"),
    DARK("Velvet Midnight", "wallpaper_dark"),
    GRADIENT_SUNSET("Sunset Bloom", "gradient_sunset"),
    GRADIENT_OCEAN("Deep Aqua", "gradient_ocean"),
    GRADIENT_FOREST("Emerald Pine", "gradient_forest")
}

enum class ThemePalette(val label: String) {
    DYNAMIC("Material You Dynamic"),
    OCEAN("Ocean Blue"),
    PEACH("Peach Coral"),
    EMERALD("Emerald Green"),
    LAVENDER("Soft Lavender"),
    SUNSET("Amber Sunset")
}

enum class DockSearchStyle(val label: String) {
    PIXEL_PILL("Google Pixel Pill"),
    LAWNCHAIR_BAR("Lawnchair Search Bar"),
    MINIMAL_G("Minimal G Floating"),
    HIDDEN("Hide Dock Search")
}

enum class DrawerSearchPosition(val label: String) {
    TOP("Top of App Drawer"),
    BOTTOM("Bottom of App Drawer (Thumb Friendly)")
}

enum class GestureAction(val label: String) {
    QUICK_CONTROLS("Open Quick Hardware Controls"),
    NOTIFICATION_SHADE("Open Device Settings / Notifications"),
    OPEN_SEARCH("Open Universal Search"),
    OPEN_SETTINGS("Open Launcher Settings"),
    NONE("No Action")
}

enum class LauncherFont(val label: String) {
    SYSTEM_DEFAULT("System Default"),
    ROUNDED("Material Rounded"),
    MONO("Tech Monospace"),
    SERIF("Classic Serif")
}

enum class WidgetType(val title: String, val subtitle: String, val spanX: Int, val spanY: Int) {
    AT_A_GLANCE("At a Glance / Smartspace", "Live dynamic date, weather & clock pill", 4, 1),
    SYSTEM_BATTERY_MONITOR("Live Battery & Health", "Real-time battery %, charging state, temp & battery manager", 2, 2),
    SYSTEM_HARDWARE_CONTROLS("Hardware Controls", "Real flashlight torch, interactive volume slider & ringer", 4, 2),
    SYSTEM_STORAGE_RAM("Storage & Memory (RAM)", "Real internal storage & live RAM usage from system", 4, 1),
    ANALOG_CLOCK("Material Clock", "Live ticking timepiece with system alarm trigger", 2, 2),
    WEATHER_CARD("Weather Station", "Temperatures, conditions & forecast summary", 2, 2),
    STICKY_NOTE("Quick Memo", "Direct editable sticky note on home screen", 2, 2),
    SYSTEM_STATS("Device Overview", "Device manufacturer, Android version & RAM", 4, 1),
    MEDIA_PLAYER("Now Playing", "Media playback card with quick controls", 4, 1)
}

data class AppItem(
    val label: String,
    val packageName: String,
    val activityName: String = "",
    val isSystemApp: Boolean = false,
    val category: String = "App",
    val usageCount: Int = 0,
    val hasNotification: Boolean = false,
    val isHidden: Boolean = false
)

data class InstalledApp(
    val info: AppItem,
    val iconBitmap: ImageBitmap? = null
)

data class AppFolder(
    val id: String,
    val name: String,
    val packageNames: List<String>
)

data class HomeItem(
    val id: String,
    val page: Int = 0,
    val row: Int,
    val col: Int,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val packageName: String? = null,
    val folderId: String? = null,
    val widgetType: WidgetType? = null
)

data class LauncherSettings(
    val gridRows: Int = 5,
    val gridCols: Int = 4,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconScale: Float = 1.0f,
    val showIconLabels: Boolean = true,
    val themedIcons: Boolean = false,
    val showAtAGlance: Boolean = true,
    val showBatteryInGlance: Boolean = true,
    val showNotificationDots: Boolean = true,
    val wallpaperType: WallpaperType = WallpaperType.ABSTRACT,
    val wallpaperBlur: Float = 0f,
    val themePalette: ThemePalette = ThemePalette.DYNAMIC,
    val dockSearchStyle: DockSearchStyle = DockSearchStyle.PIXEL_PILL,
    val drawerSearchPosition: DrawerSearchPosition = DrawerSearchPosition.TOP,
    val doubleTapAction: GestureAction = GestureAction.QUICK_CONTROLS,
    val swipeDownAction: GestureAction = GestureAction.QUICK_CONTROLS,
    val dockIconCount: Int = 5,
    val dockBackground: Boolean = true,
    val hiddenPackages: List<String> = emptyList(),
    val fontStyle: LauncherFont = LauncherFont.SYSTEM_DEFAULT,
    val iconSizeDp: Int = 54,
    val isDefaultLauncherPromptDismissed: Boolean = false
)
