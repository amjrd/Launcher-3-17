package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IconShape
import com.example.data.InstalledApp

fun getIconShape(shape: IconShape): Shape {
    return when (shape) {
        IconShape.CIRCLE -> CircleShape
        IconShape.SQUIRCLE -> RoundedCornerShape(percent = 35)
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(percent = 22)
        IconShape.TEARDROP -> RoundedCornerShape(
            topStart = CornerSize(50),
            topEnd = CornerSize(50),
            bottomStart = CornerSize(50),
            bottomEnd = CornerSize(12)
        )
        IconShape.HEXAGON -> CutCornerShape(percent = 28)
        IconShape.CLOVER -> RoundedCornerShape(percent = 45)
        IconShape.OCTAGON -> CutCornerShape(percent = 30)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconView(
    app: InstalledApp,
    shape: IconShape,
    themed: Boolean,
    showLabel: Boolean = true,
    hasNotificationDot: Boolean = false,
    iconSize: Dp = 54.dp,
    iconScale: Float = 1.0f,
    labelColor: Color = Color.White,
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
            .testTag("app_icon_${app.info.packageName}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(scaledSize)
                .shadow(elevation = 2.dp, shape = cornerShape)
                .clip(cornerShape)
                .background(
                    if (themed) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (app.iconBitmap == null) {
                        getFallbackBgColor(app.info.packageName)
                    } else {
                        Color.Transparent
                    }
                )
        ) {
            if (themed) {
                // Themed icon mode: monochrome icon tinted with onPrimaryContainer
                ThemedAppIcon(
                    app = app,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    size = scaledSize * 0.58f
                )
            } else if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.info.label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FallbackAppIcon(
                    app = app,
                    size = scaledSize * 0.58f
                )
            }

            if (hasNotificationDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.info.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor,
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
}

@Composable
fun ThemedAppIcon(
    app: InstalledApp,
    tint: Color,
    size: Dp
) {
    val iconVector = getCategoryVector(app.info.packageName, app.info.category)
    Icon(
        imageVector = iconVector,
        contentDescription = app.info.label,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
fun FallbackAppIcon(
    app: InstalledApp,
    size: Dp
) {
    val iconVector = getCategoryVector(app.info.packageName, app.info.category)
    Icon(
        imageVector = iconVector,
        contentDescription = app.info.label,
        tint = Color.White,
        modifier = Modifier.size(size)
    )
}

fun getCategoryVector(pkg: String, category: String): ImageVector {
    val p = pkg.lowercase()
    return when {
        p.contains("chrome") || p.contains("browser") -> Icons.Default.Language
        p.contains("camera") -> Icons.Default.CameraAlt
        p.contains("message") || p.contains("chat") -> Icons.Default.Chat
        p.contains("dialer") || p.contains("phone") -> Icons.Default.Phone
        p.contains("setting") -> Icons.Default.Settings
        category == "Media" -> Icons.Default.CameraAlt
        category == "Communication" -> Icons.Default.Chat
        category == "Internet" -> Icons.Default.Language
        category == "System" -> Icons.Default.Settings
        else -> Icons.Default.Widgets
    }
}

fun getFallbackBgColor(pkg: String): Color {
    val hash = pkg.hashCode()
    val colors = listOf(
        Color(0xFF2979FF),
        Color(0xFF00B0FF),
        Color(0xFF00E676),
        Color(0xFFFF9100),
        Color(0xFFFF5252),
        Color(0xFFE040FB),
        Color(0xFF7C4DFF),
        Color(0xFF1DE9B6)
    )
    val index = Math.abs(hash) % colors.size
    return colors[index]
}
