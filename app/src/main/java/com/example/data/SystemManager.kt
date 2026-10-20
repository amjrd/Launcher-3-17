package com.example.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class RealBatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val plugType: String = "Battery",
    val health: String = "Good",
    val temperatureC: Float = 28.0f,
    val voltageMv: Int = 4100
)

data class RealMemoryInfo(
    val totalRamGb: Double = 8.0,
    val usedRamGb: Double = 3.8,
    val freeRamGb: Double = 4.2,
    val usagePercent: Int = 48,
    val isLowMemory: Boolean = false
)

data class RealStorageInfo(
    val totalGb: Double = 128.0,
    val usedGb: Double = 48.0,
    val freeGb: Double = 80.0,
    val usagePercent: Int = 38
)

class SystemManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    // 1. Real Battery State
    private val _batteryInfo = MutableStateFlow(RealBatteryInfo())
    val batteryInfo: StateFlow<RealBatteryInfo> = _batteryInfo.asStateFlow()

    // 2. Real RAM & Storage State
    private val _memoryInfo = MutableStateFlow(RealMemoryInfo())
    val memoryInfo: StateFlow<RealMemoryInfo> = _memoryInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow(RealStorageInfo())
    val storageInfo: StateFlow<RealStorageInfo> = _storageInfo.asStateFlow()

    // 3. Real Flashlight State
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()
    private var cameraIdWithFlash: String? = null

    // 4. Real Audio State
    private val _mediaVolume = MutableStateFlow(0)
    val mediaVolume: StateFlow<Int> = _mediaVolume.asStateFlow()
    val maxMediaVolume: Int = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15

    private val _ringerMode = MutableStateFlow(AudioManager.RINGER_MODE_NORMAL)
    val ringerMode: StateFlow<Int> = _ringerMode.asStateFlow()

    // 5. System Uptime & Device Info
    val deviceModel: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateBatteryFromIntent(intent)
            }
        }
    }

    init {
        // Register Battery Receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Initialize Audio
        updateAudioState()

        // Initialize Camera Flash
        initFlashlight()

        // Background polling for Memory, Storage, Audio
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                updateMemoryAndStorage()
                updateAudioState()
                delay(3000L)
            }
        }
    }

    private fun updateBatteryFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 85

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Charging" else "On Battery"
        }

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good Condition"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280)
        val tempC = tempRaw / 10.0f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)

        _batteryInfo.value = RealBatteryInfo(
            level = percent,
            isCharging = isCharging,
            plugType = plugType,
            health = health,
            temperatureC = tempC,
            voltageMv = voltage
        )
    }

    private fun updateMemoryAndStorage() {
        // Memory (RAM)
        activityManager?.let { am ->
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val totalGb = mi.totalMem.toDouble() / (1024 * 1024 * 1024)
            val availGb = mi.availMem.toDouble() / (1024 * 1024 * 1024)
            val usedGb = totalGb - availGb
            val usagePercent = ((usedGb / totalGb) * 100).toInt().coerceIn(0, 100)

            _memoryInfo.value = RealMemoryInfo(
                totalRamGb = (totalGb * 10).toInt() / 10.0,
                usedRamGb = (usedGb * 10).toInt() / 10.0,
                freeRamGb = (availGb * 10).toInt() / 10.0,
                usagePercent = usagePercent,
                isLowMemory = mi.lowMemory
            )
        }

        // Storage
        try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes

            val totalGb = totalBytes.toDouble() / (1024 * 1024 * 1024)
            val freeGb = freeBytes.toDouble() / (1024 * 1024 * 1024)
            val usedGb = usedBytes.toDouble() / (1024 * 1024 * 1024)
            val usagePercent = ((usedGb / totalGb) * 100).toInt().coerceIn(0, 100)

            _storageInfo.value = RealStorageInfo(
                totalGb = (totalGb * 10).toInt() / 10.0,
                usedGb = (usedGb * 10).toInt() / 10.0,
                freeGb = (freeGb * 10).toInt() / 10.0,
                usagePercent = usagePercent
            )
        } catch (_: Exception) {
        }
    }

    private fun initFlashlight() {
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdWithFlash = id
                        break
                    }
                }
                if (cameraIdWithFlash == null && cm.cameraIdList.isNotEmpty()) {
                    cameraIdWithFlash = cm.cameraIdList[0]
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cm.registerTorchCallback(object : CameraManager.TorchCallback() {
                        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                            if (cameraId == cameraIdWithFlash) {
                                _isFlashlightOn.value = enabled
                            }
                        }
                    }, null)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun toggleFlashlight(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val camId = cameraIdWithFlash ?: return false
            return try {
                val newState = !_isFlashlightOn.value
                cameraManager?.setTorchMode(camId, newState)
                _isFlashlightOn.value = newState
                true
            } catch (e: Exception) {
                Toast.makeText(context, "Flashlight unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
                false
            }
        }
        return false
    }

    fun updateAudioState() {
        audioManager?.let { am ->
            _mediaVolume.value = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            _ringerMode.value = am.ringerMode
        }
    }

    fun setMediaVolume(newVolume: Int) {
        audioManager?.let { am ->
            val v = newVolume.coerceIn(0, maxMediaVolume)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0)
            _mediaVolume.value = v
        }
    }

    fun toggleMuteMedia() {
        audioManager?.let { am ->
            val current = _mediaVolume.value
            if (current > 0) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                _mediaVolume.value = 0
            } else {
                val half = (maxMediaVolume / 2).coerceAtLeast(1)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, half, 0)
                _mediaVolume.value = half
            }
        }
    }

    fun cycleRingerMode() {
        audioManager?.let { am ->
            val nextMode = when (am.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            try {
                am.ringerMode = nextMode
                _ringerMode.value = nextMode
            } catch (e: Exception) {
                openSoundSettings()
            }
        }
    }

    // System Intents & Navigation
    fun openBatterySettings() {
        try {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            }
        }
    }

    fun openStorageSettings() {
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    fun openClockOrAlarm() {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "System Clock App not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openSoundSettings() {
        try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    fun openDefaultHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            }
        }
    }

    fun unregister() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
        }
    }
}
