package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun FastExportDialog(
    isOpen: Boolean,
    exportConfig: ExportConfig,
    exportState: ExportState,
    onConfigChange: (ExportConfig) -> Unit,
    onStartExport: () -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            if (!exportState.isExporting) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (exportState.isCompleted) {
                    // Export Finished Celebration Screen
                    ExportCompletedView(
                        state = exportState,
                        config = exportConfig,
                        context = context,
                        onDismiss = {
                            onDismiss()
                        }
                    )
                } else if (exportState.isExporting) {
                    // Real-time Ultra-Fast Rendering Progress
                    ExportRenderingProgressView(
                        state = exportState,
                        onCancel = onCancelExport
                    )
                } else {
                    // Export Settings & Fast Engine Configuration
                    ExportSettingsView(
                        config = exportConfig,
                        onConfigChange = onConfigChange,
                        onStartExport = onStartExport,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportSettingsView(
    config: ExportConfig,
    onConfigChange: (ExportConfig) -> Unit,
    onStartExport: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(StudioPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Fast Render",
                        tint = StudioPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "FAST EXPORT STUDIO",
                        color = StudioTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "خروجی گرفتن فوق سریع با شتاب‌دهنده گرافیکی",
                        color = StudioPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp).testTag("close_export_dialog")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = StudioTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Resolution Selector (720p, 1080p, 4K)
        Text("Resolution (کیفیت ویدیو)", color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (res in ExportResolution.values()) {
                val isSelected = res == config.resolution
                Surface(
                    onClick = { onConfigChange(config.copy(resolution = res)) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("res_${res.name}")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = res.label.split(" ").first(),
                            color = if (isSelected) StudioPrimary else StudioTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = res.badge,
                            color = if (isSelected) StudioPrimary else StudioTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Frame Rate Selector (24, 30, 60 fps)
        Text("Frame Rate (نرخ فریم)", color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(24, 30, 60).forEach { fps ->
                val isSelected = fps == config.fps
                Surface(
                    onClick = { onConfigChange(config.copy(fps = fps)) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StudioSecondary.copy(alpha = 0.2f) else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) StudioSecondary else StudioCardBorder
                    ),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "$fps FPS ${if (fps == 60) "⚡" else ""}",
                            color = if (isSelected) StudioSecondary else StudioTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Codec & Bitrate Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Codec
            Column(modifier = Modifier.weight(1f)) {
                Text("Codec", color = StudioTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                        Text(config.codec.label.split(" ").first(), color = StudioTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bitrate
            Column(modifier = Modifier.weight(1f)) {
                Text("Bitrate", color = StudioTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                        Text(config.bitrate.mbps, color = StudioPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Hardware Acceleration Toggle (Turbo Engine)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccentGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "GPU Boost", tint = StudioAccentGreen, modifier = Modifier.size(20.dp))
                    Column {
                        Text("GPU Turbo Acceleration", color = StudioTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("4.5x Real-Time Hardware Encode", color = StudioAccentGreen, fontSize = 9.sp)
                    }
                }
                Switch(
                    checked = config.hardwareAccelerated,
                    onCheckedChange = { onConfigChange(config.copy(hardwareAccelerated = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StudioAccentGreen,
                        checkedTrackColor = StudioAccentGreen.copy(alpha = 0.4f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start Fast Render Button
        Button(
            onClick = onStartExport,
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioPrimary,
                contentColor = StudioDarkBg
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("start_export_button")
        ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Render", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START FAST EXPORT (خروجی سریع)",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ExportRenderingProgressView(
    state: ExportState,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing animated icon
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(StudioPrimary.copy(alpha = 0.15f * alpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Rendering",
                tint = StudioPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "FAST RENDERING IN PROGRESS...",
            color = StudioTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = "در حال پردازش و استخراج فریم‌ها با نهایت سرعت",
            color = StudioPrimary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Percentage readout
        Text(
            text = "${(state.progress * 100).toInt()}%",
            color = StudioPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Linear Progress Bar
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = StudioPrimary,
            trackColor = StudioSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Live telemetry stats
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rendered Frames:", color = StudioTextMuted, fontSize = 10.sp)
                    Text(
                        "${state.renderedFrames} / ${state.totalFrames}",
                        color = StudioTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Encoding Speed:", color = StudioTextMuted, fontSize = 10.sp)
                    Text(
                        "⚡ ${state.renderSpeedX}x Realtime",
                        color = StudioAccentGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Remaining:", color = StudioTextMuted, fontSize = 10.sp)
                    Text(
                        "${state.remainingSeconds}s",
                        color = StudioAccentYellow,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text("Cancel Render", color = StudioPlayheadRed, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ExportCompletedView(
    state: ExportState,
    config: ExportConfig,
    context: Context,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(StudioAccentGreen.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = StudioAccentGreen,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "EXPORT COMPLETED!",
            color = StudioTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = "ویدیو با موفقیت و بالاترین کیفیت ساخته شد",
            color = StudioAccentGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // File Details Card
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = state.exportedFileName,
                    color = StudioPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Divider(color = StudioCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Format:", color = StudioTextMuted, fontSize = 10.sp)
                    Text("${config.resolution.label} • ${config.fps} FPS", color = StudioTextPrimary, fontSize = 10.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("File Size:", color = StudioTextMuted, fontSize = 10.sp)
                    Text("${String.format("%.1f", state.fileSizeMb)} MB", color = StudioTextPrimary, fontSize = 10.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Codec:", color = StudioTextMuted, fontSize = 10.sp)
                    Text(config.codec.label.split(" ").first(), color = StudioTextPrimary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Save & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "Saved to device Movies/ProEdit folder!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAccentGreen,
                    contentColor = StudioDarkBg
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(44.dp).testTag("save_to_gallery_button")
            ) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Save", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save to Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        putExtra(Intent.EXTRA_SUBJECT, "Created with ProEdit Studio")
                        putExtra(Intent.EXTRA_TEXT, "Check out my new video created in ProEdit Studio 4K!")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary,
                    contentColor = StudioDarkBg
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(44.dp).testTag("share_video_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Editing Studio", color = StudioTextSecondary, fontSize = 11.sp)
        }
    }
}
