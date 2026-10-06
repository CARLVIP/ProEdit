package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.model.MediaClip
import com.example.ui.theme.*

@Composable
fun ChromaCutoutSheet(
    clip: MediaClip?,
    onToggleCutout: () -> Unit,
    onToleranceChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = clip?.chromaCutout == true
    val tolerance = clip?.chromaTolerance ?: 0.4f

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
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "AI Cutout",
                    tint = StudioAccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AI CUTOUT & CHROMA KEY (CAPCUT)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_chroma_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Toggle Card
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Smart Background Remover",
                        color = StudioTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant Green-Screen / AI Human cutout without mask",
                        color = StudioTextMuted,
                        fontSize = 9.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggleCutout() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioAccentGreen,
                        checkedTrackColor = StudioAccentGreen.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("chroma_toggle_switch")
                )
            }
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.height(14.dp))

            // Chroma Key Tolerance Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Key Tolerance", color = StudioTextSecondary, fontSize = 11.sp)
                Text(
                    "${(tolerance * 100).toInt()}%",
                    color = StudioAccentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = tolerance,
                onValueChange = onToleranceChange,
                valueRange = 0.1f..0.9f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioAccentGreen,
                    activeTrackColor = StudioAccentGreen,
                    inactiveTrackColor = StudioSurfaceVariant
                ),
                modifier = Modifier.height(28.dp).testTag("chroma_tolerance_slider")
            )

            // Feather edge indicator
            Text(
                text = "✓ Real-time edge softening applied with zero green spill",
                color = StudioAccentGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
