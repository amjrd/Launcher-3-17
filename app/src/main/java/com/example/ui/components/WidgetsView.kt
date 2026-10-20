package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var hours by remember { mutableFloatStateOf(0f) }
    var minutes by remember { mutableFloatStateOf(0f) }
    var seconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            val sec = cal.get(Calendar.SECOND).toFloat()
            val min = cal.get(Calendar.MINUTE).toFloat() + sec / 60f
            val hr = (cal.get(Calendar.HOUR) % 12).toFloat() + min / 60f
            seconds = sec
            minutes = min
            hours = hr
            delay(1000L)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .testTag("analog_clock_widget")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2) - 8.dp.toPx()

                // Dial background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // 12 Hour tick marks
                for (i in 0 until 12) {
                    val angle = (i * 30.0 - 90.0) * (Math.PI / 180.0)
                    val isQuarter = (i % 3 == 0)
                    val tickLen = if (isQuarter) 10.dp.toPx() else 5.dp.toPx()
                    val strokeW = if (isQuarter) 3.dp.toPx() else 1.5.dp.toPx()
                    val p1 = Offset(
                        (center.x + (radius - tickLen) * cos(angle)).toFloat(),
                        (center.y + (radius - tickLen) * sin(angle)).toFloat()
                    )
                    val p2 = Offset(
                        (center.x + radius * cos(angle)).toFloat(),
                        (center.y + radius * sin(angle)).toFloat()
                    )
                    drawElevationLine(p1, p2, primaryColor, strokeW)
                }

                // Hour Hand
                val hourAngle = (hours * 30.0 - 90.0) * (Math.PI / 180.0)
                val hourHandLength = radius * 0.52f
                val hourEnd = Offset(
                    (center.x + hourHandLength * cos(hourAngle)).toFloat(),
                    (center.y + hourHandLength * sin(hourAngle)).toFloat()
                )
                drawLine(
                    color = primaryColor,
                    start = center,
                    end = hourEnd,
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minute Hand
                val minAngle = (minutes * 6.0 - 90.0) * (Math.PI / 180.0)
                val minHandLength = radius * 0.75f
                val minEnd = Offset(
                    (center.x + minHandLength * cos(minAngle)).toFloat(),
                    (center.y + minHandLength * sin(minAngle)).toFloat()
                )
                drawLine(
                    color = secondaryColor,
                    start = center,
                    end = minEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Second Hand
                val secAngle = (seconds * 6.0 - 90.0) * (Math.PI / 180.0)
                val secHandLength = radius * 0.85f
                val secEnd = Offset(
                    (center.x + secHandLength * cos(secAngle)).toFloat(),
                    (center.y + secHandLength * sin(secAngle)).toFloat()
                )
                drawLine(
                    color = tertiaryColor,
                    start = center,
                    end = secEnd,
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center hub
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawElevationLine(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

@Composable
fun WeatherCardWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .testTag("weather_card_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mountain View",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "72°",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD54F).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Condition",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Mostly Sunny",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "H: 76°  L: 58°",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 10% Rain",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StickyNoteWidget(
    noteContent: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempText by remember(noteContent) { mutableStateOf(noteContent) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9C4).copy(alpha = 0.95f)
        ),
        modifier = modifier
            .padding(4.dp)
            .clickable { isEditing = !isEditing }
            .testTag("sticky_note_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Memo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = if (isEditing) "Done" else "Tap to edit",
                    fontSize = 10.sp,
                    color = Color(0xFF8D6E63)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = {
                        tempText = it
                        onNoteChange(it)
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF3E2723))
                )
            } else {
                Text(
                    text = noteContent,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF3E2723),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SystemStatsWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick)
            .testTag("system_stats_widget")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memory",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Android 17 Launcher3",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Build AP2A • Android V/17 Release",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RAM 4.2 / 8 GB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.52f },
                    modifier = Modifier
                        .width(70.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun MediaPlayerWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .testTag("media_player_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF80AB), Color(0xFF8C9EFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Album",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Solar Resonance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Material Ambient",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
