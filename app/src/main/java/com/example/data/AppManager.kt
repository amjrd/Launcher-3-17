package com.example.data

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val appList = mutableListOf<InstalledApp>()
        val seenPackages = mutableSetOf<String>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            // Don't show ourselves in launcher if desired, or show
            if (seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val label = try {
                info.loadLabel(packageManager).toString().ifBlank {
                    info.activityInfo.name.substringAfterLast('.')
                }
            } catch (e: Exception) {
                pkg.substringAfterLast('.')
            }

            val isSystem = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: Exception) {
                false
            }

            val iconDrawable = try {
                info.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val iconBitmap = iconDrawable?.let { drawableToImageBitmap(it) }

            val category = categorizeApp(label, pkg)

            appList.add(
                InstalledApp(
                    info = AppItem(
                        label = label,
                        packageName = pkg,
                        activityName = info.activityInfo.name,
                        isSystemApp = isSystem,
                        category = category
                    ),
                    iconBitmap = iconBitmap
                )
            )
        }

        // If very few apps exist (e.g. minimal test emulator), provide core Android utilities
        if (appList.size < 5) {
            val coreUtilities = getFallbackCoreApps()
            for (util in coreUtilities) {
                if (!seenPackages.contains(util.info.packageName)) {
                    appList.add(util)
                }
            }
        }

        appList.sortedBy { it.info.label.lowercase() }
    }

    private fun categorizeApp(label: String, pkg: String): String {
        val l = (label + " " + pkg).lowercase()
        return when {
            l.contains("chrome") || l.contains("browser") || l.contains("web") -> "Internet"
            l.contains("camera") || l.contains("photo") || l.contains("gallery") -> "Media"
            l.contains("message") || l.contains("chat") || l.contains("dialer") || l.contains("phone") || l.contains("contact") -> "Communication"
            l.contains("setting") || l.contains("config") || l.contains("system") -> "System"
            l.contains("clock") || l.contains("alarm") || l.contains("timer") || l.contains("calc") || l.contains("note") -> "Productivity"
            l.contains("music") || l.contains("audio") || l.contains("sound") || l.contains("video") || l.contains("player") -> "Entertainment"
            else -> "Tools"
        }
    }

    private fun getFallbackCoreApps(): List<InstalledApp> {
        val coreList = listOf(
            AppItem("Settings", "com.android.settings", isSystemApp = true, category = "System"),
            AppItem("Chrome", "com.android.chrome", isSystemApp = false, category = "Internet"),
            AppItem("Phone", "com.google.android.dialer", isSystemApp = true, category = "Communication"),
            AppItem("Messages", "com.google.android.apps.messaging", isSystemApp = true, category = "Communication"),
            AppItem("Camera", "com.android.camera2", isSystemApp = true, category = "Media"),
            AppItem("Clock", "com.google.android.deskclock", isSystemApp = true, category = "Productivity"),
            AppItem("Calculator", "com.google.android.calculator", isSystemApp = true, category = "Productivity"),
            AppItem("Files", "com.google.android.documentsui", isSystemApp = true, category = "Tools")
        )
        return coreList.map { InstalledApp(it, null) }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // Fallbacks for common apps
                fallbackLaunch(packageName)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot launch app: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun fallbackLaunch(packageName: String): Boolean {
        return try {
            val intent = when {
                packageName.contains("settings") -> Intent(Settings.ACTION_SETTINGS)
                packageName.contains("dialer") || packageName.contains("phone") -> Intent(Intent.ACTION_DIAL)
                packageName.contains("camera") -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                packageName.contains("message") -> Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) }
                packageName.contains("chrome") || packageName.contains("browser") -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                packageName.contains("gallery") || packageName.contains("photo") -> Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
                else -> null
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                Toast.makeText(context, "App not available on this device", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestUninstall(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot uninstall: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWebSearch(query: String) {
        try {
            val intent = if (query.isNotBlank()) {
                Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Browser not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openSystemSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Settings not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp(packageName: String, label: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out $label on Google Play: https://play.google.com/store/apps/details?id=$packageName")
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share $label").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Play Store not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerHapticFeedback() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap? {
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
            val targetSize = 144.coerceAtMost(maxOf(width, height))
            val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
