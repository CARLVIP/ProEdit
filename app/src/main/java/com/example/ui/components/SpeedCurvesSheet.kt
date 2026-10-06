package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedCurveType
import com.example.ui.theme.*

@Composable
fun SpeedCurvesSheet(
    currentSpeed: Float,
    currentCurve: SpeedCurveType,
    onSpeedChange: (Float, SpeedCurveType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isOpticalFlowEnabled by remember { mutableStateOf(true) }

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
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Velocity Speed Curves",
                    tint = StudioPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "VELOCITY SPEED CURVES (CAPCUT)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_speed_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Velocity Curve Canvas Graph Visualization
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val w = size.width
                    val h = size.height

                    // Baseline (1.0x)
                    val midY = h * 0.5f
                    drawLine(
                        color = StudioTextMuted.copy(alpha = 0.4f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Bezier Velocity Curve based on curve type
                    val path = Path()
                    when (currentCurve) {
                        SpeedCurveType.MONTAGE -> {
                            path.moveTo(0f, midY + 15f)
                            path.quadraticTo(w * 0.35f, h * 0.1f, w * 0.7f, midY - 20f)
                            path.lineTo(w, midY)
                        }
                        SpeedCurveType.HERO -> {
                            path.moveTo(0f, h * 0.15f)
                            path.quadraticTo(w * 0.4f, h * 0.85f, w * 0.8f, h * 0.85f)
                            path.lineTo(w, midY)
                        }
                        SpeedCurveType.FLASH -> {
                            path.moveTo(0f, midY)
                            path.lineTo(w * 0.3f, midY)
                            path.lineTo(w * 0.5f, h * 0.05f)
                            path.lineTo(w * 0.7f, midY)
                            path.lineTo(w, midY)
                        }
                        SpeedCurveType.BULLET -> {
                            path.moveTo(0f, midY)
                            path.quadraticTo(w * 0.2f, h * 0.9f, w * 0.8f, h * 0.9f)
                            path.lineTo(w, midY)
                        }
                        SpeedCurveType.STANDARD -> {
                            val y = h - (currentSpeed / 5f * h).coerceIn(0f, h)
                            path.moveTo(0f, y)
                            path.lineTo(w, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = StudioPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "${String.format("%.1f", currentSpeed)}x Active Speed",
                    color = StudioPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Velocity Curve Chips
        Text(
            text = "Curve Velocity Presets",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (curve in SpeedCurveType.values()) {
                val isSelected = curve == currentCurve
                Surface(
                    onClick = { onSpeedChange(curve.speedFactor, curve) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("speed_curve_${curve.name}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = curve.label.split(" ").first(),
                            color = if (isSelected) StudioPrimary else StudioTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${curve.speedFactor}x",
                            color = StudioTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Linear Speed Fine-Tuner Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Fine Speed Adjust", color = StudioTextPrimary, fontSize = 11.sp)
            Text(
                "${String.format("%.2f", currentSpeed)}x",
                color = StudioAccentYellow,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentSpeed,
            onValueChange = { onSpeedChange(it, SpeedCurveType.STANDARD) },
            valueRange = 0.1f..5.0f,
            colors = SliderDefaults.colors(
                thumbColor = StudioAccentYellow,
                activeTrackColor = StudioAccentYellow,
                inactiveTrackColor = StudioSurfaceVariant
            ),
            modifier = Modifier.height(28.dp).testTag("speed_slider")
        )

        // Smooth Slow-Motion Optical Flow Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Optical Flow Slow-Mo", color = StudioTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("AI frame interpolation for buttery smooth motion", color = StudioTextMuted, fontSize = 9.sp)
            }
            Switch(
                checked = isOpticalFlowEnabled,
                onCheckedChange = { isOpticalFlowEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioPrimary,
                    checkedTrackColor = StudioPrimary.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("optical_flow_switch")
            )
        }
    }
}
