package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.model.CanvasBackground
import com.example.ui.theme.*

@Composable
fun CanvasRatioSheet(
    activeRatio: AspectRatio,
    activeCanvasBg: CanvasBackground,
    onRatioChange: (AspectRatio) -> Unit,
    onCanvasBgChange: (CanvasBackground) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ratioScrollState = rememberScrollState()
    val bgScrollState = rememberScrollState()

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
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Canvas Ratio",
                    tint = StudioTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "CANVAS & RATIO (INSHOT)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_ratio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Aspect Ratio Selector
        Text(
            text = "Video Frame Format",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(ratioScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (ratio in AspectRatio.values()) {
                val isSelected = ratio == activeRatio
                Surface(
                    onClick = { onRatioChange(ratio) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioTertiary.copy(alpha = 0.25f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier
                        .width(96.dp)
                        .height(68.dp)
                        .testTag("ratio_${ratio.name}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ratio.label,
                            color = if (isSelected) StudioPrimary else StudioTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ratio.subLabel,
                            color = StudioTextMuted,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Style Selector (InShot style blur/gradient backgrounds)
        Text(
            text = "Canvas Background Style",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(bgScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (bg in CanvasBackground.values()) {
                val isSelected = bg == activeCanvasBg
                Surface(
                    onClick = { onCanvasBgChange(bg) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .height(52.dp)
                        .testTag("canvas_bg_${bg.name}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getCanvasBgBrush(bg).takeIf { isSelected } ?: androidx.compose.ui.graphics.Brush.linearGradient(listOf(StudioSurfaceVariant, StudioSurfaceVariant)))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bg.label,
                            color = if (isSelected) StudioPrimary else StudioTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
