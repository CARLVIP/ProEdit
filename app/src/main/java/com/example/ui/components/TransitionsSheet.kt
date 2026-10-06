package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransitionType
import com.example.ui.theme.*

@Composable
fun TransitionsSheet(
    activeTransition: TransitionType,
    transitionDurationMs: Long,
    onTransitionChange: (TransitionType, Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var duration by remember { mutableStateOf(transitionDurationMs) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Transitions",
                    tint = StudioPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "DYNAMIC TRANSITIONS (CAPCUT & PREMIERE)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_transitions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transition Duration Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Transition Duration", color = StudioTextSecondary, fontSize = 11.sp)
            Text(
                "${String.format("%.1f", duration / 1000f)}s",
                color = StudioPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = duration.toFloat(),
            onValueChange = {
                duration = it.toLong()
                onTransitionChange(activeTransition, duration)
            },
            valueRange = 200f..1500f,
            colors = SliderDefaults.colors(
                thumbColor = StudioPrimary,
                activeTrackColor = StudioPrimary,
                inactiveTrackColor = StudioSurfaceVariant
            ),
            modifier = Modifier.height(28.dp).testTag("transition_duration_slider")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Transitions Grid
        Text(
            text = "Transition Effects",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (t in TransitionType.values()) {
                val isSelected = t == activeTransition
                Surface(
                    onClick = { onTransitionChange(t, duration) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .height(72.dp)
                        .testTag("trans_${t.name}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (t) {
                                TransitionType.DISSOLVE -> Icons.Default.BlurOn
                                TransitionType.GLITCH -> Icons.Default.FlashOn
                                TransitionType.FLASH_WHITE -> Icons.Default.BrightnessHigh
                                TransitionType.ZOOM_IN -> Icons.Default.ZoomIn
                                TransitionType.ZOOM_OUT -> Icons.Default.ZoomOut
                                TransitionType.WIPE_RIGHT -> Icons.Default.SwipeRight
                                TransitionType.WHIP_PAN -> Icons.Default.FastForward
                                TransitionType.FILM_ROLL -> Icons.Default.MovieFilter
                                TransitionType.NONE -> Icons.Default.HorizontalRule
                            },
                            contentDescription = t.label,
                            tint = if (isSelected) StudioPrimary else StudioTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = t.label,
                            color = if (isSelected) StudioPrimary else StudioTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
