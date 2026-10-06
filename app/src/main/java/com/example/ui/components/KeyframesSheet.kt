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
fun KeyframesSheet(
    clip: MediaClip?,
    currentPlayheadMs: Long,
    onAddKeyframe: () -> Unit,
    onTransformChange: (scale: Float, rotation: Float, flipH: Boolean, flipV: Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = clip?.scale ?: 1f
    val rotation = clip?.rotation ?: 0f
    val keyframesCount = clip?.keyframes?.size ?: 0

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
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Keyframes",
                    tint = StudioAccentYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "KEYFRAME ANIMATION (PREMIERE PRO)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_keyframes_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Set Keyframe Diamond Action Bar
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Keyframes: $keyframesCount",
                        color = StudioTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pin transforms (scale, angle) to current playhead",
                        color = StudioTextMuted,
                        fontSize = 9.sp
                    )
                }

                Button(
                    onClick = onAddKeyframe,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioAccentYellow,
                        contentColor = StudioDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("add_keyframe_diamond_button")
                ) {
                    Icon(imageVector = Icons.Default.Diamond, contentDescription = "Add Keyframe", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Keyframe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scale Slider (0.5x to 3.0x)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Clip Scale (Zoom)", color = StudioTextSecondary, fontSize = 11.sp)
            Text(
                "${String.format("%.2f", scale)}x",
                color = StudioPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = scale,
            onValueChange = { onTransformChange(it, rotation, clip?.flipH ?: false, clip?.flipV ?: false) },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(
                thumbColor = StudioPrimary,
                activeTrackColor = StudioPrimary,
                inactiveTrackColor = StudioSurfaceVariant
            ),
            modifier = Modifier.height(28.dp).testTag("scale_slider")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Rotation & Flip quick buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onTransformChange(scale, (rotation + 90f) % 360f, clip?.flipH ?: false, clip?.flipV ?: false) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { onTransformChange(scale, rotation, !(clip?.flipH ?: false), clip?.flipV ?: false) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Flip, contentDescription = "Flip Horizontal", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip H", fontSize = 11.sp)
            }
        }
    }
}
