package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterPreset
import com.example.model.LumetriAdjustments
import com.example.ui.theme.*

@Composable
fun LumetriColorSheet(
    activeFilter: FilterPreset,
    lumetri: LumetriAdjustments,
    onFilterChange: (FilterPreset) -> Unit,
    onLumetriChange: (LumetriAdjustments) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: LUT Presets, 1: Basic Adjustments
    val lutScrollState = rememberScrollState()
    val adjustScrollState = rememberScrollState()

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
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "Lumetri Color",
                    tint = StudioSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "LUMETRI COLOR STUDIO",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_lumetri_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: LUTs vs Curves/Sliders
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioSurfaceVariant,
            contentColor = StudioPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .height(38.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("LUT Presets", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Pro Sliders (Lumetri)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // LUT Presets Horizontal Grid
            Column {
                Text(
                    text = "Cinematic Color Profiles",
                    color = StudioTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(lutScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (preset in FilterPreset.values()) {
                        val isSelected = preset == activeFilter
                        Surface(
                            onClick = { onFilterChange(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) StudioSecondary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) StudioSecondary else StudioCardBorder
                            ),
                            modifier = Modifier
                                .width(110.dp)
                                .height(72.dp)
                                .testTag("filter_${preset.name}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = preset.displayName,
                                    color = if (isSelected) StudioSecondary else StudioTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.desc,
                                    color = StudioTextMuted,
                                    fontSize = 9.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed Lumetri Sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(adjustScrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Exposure
                LumetriSliderRow(
                    label = "Exposure",
                    value = lumetri.exposure,
                    range = -1f..1f,
                    displayValue = "${if (lumetri.exposure >= 0) "+" else ""}${String.format("%.2f", lumetri.exposure)}",
                    onValueChange = { onLumetriChange(lumetri.copy(exposure = it)) }
                )

                // Contrast
                LumetriSliderRow(
                    label = "Contrast",
                    value = lumetri.contrast,
                    range = 0.5f..2.0f,
                    displayValue = "${String.format("%.2f", lumetri.contrast)}x",
                    onValueChange = { onLumetriChange(lumetri.copy(contrast = it)) }
                )

                // Temperature (Cool vs Warm)
                LumetriSliderRow(
                    label = "Temperature",
                    value = lumetri.temperature,
                    range = -1f..1f,
                    displayValue = if (lumetri.temperature < 0) "Cool (${String.format("%.2f", lumetri.temperature)})" else "Warm (${String.format("%.2f", lumetri.temperature)})",
                    onValueChange = { onLumetriChange(lumetri.copy(temperature = it)) }
                )

                // Saturation
                LumetriSliderRow(
                    label = "Saturation",
                    value = lumetri.saturation,
                    range = 0f..2.5f,
                    displayValue = "${(lumetri.saturation * 100).toInt()}%",
                    onValueChange = { onLumetriChange(lumetri.copy(saturation = it)) }
                )

                // Vignette
                LumetriSliderRow(
                    label = "Vignette",
                    value = lumetri.vignette,
                    range = 0f..1f,
                    displayValue = "${(lumetri.vignette * 100).toInt()}%",
                    onValueChange = { onLumetriChange(lumetri.copy(vignette = it)) }
                )

                // Reset Button
                OutlinedButton(
                    onClick = { onLumetriChange(LumetriAdjustments()) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reset_lumetri_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Lumetri Adjustments", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun LumetriSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = StudioTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = displayValue, color = StudioPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = StudioPrimary,
                activeTrackColor = StudioPrimary,
                inactiveTrackColor = StudioSurfaceVariant
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
