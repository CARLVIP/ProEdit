package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.TrackType

@Composable
fun TimelineView(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipId: String?,
    selectedTrackType: TrackType,
    timelineZoom: Float,
    onSeekTo: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectTrackType: (TrackType) -> Unit,
    onOpenTransitions: () -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Timeline scale: base pixels per second
    val pixelsPerSecond = (70f * timelineZoom).coerceAtLeast(35f)
    val totalSeconds = (project.totalDurationMs / 1000f).coerceAtLeast(10f)
    val totalTimelineWidthDp = (totalSeconds * (pixelsPerSecond / 2.5f)).dp.coerceAtLeast(600.dp)

    // Center scroll to playhead if auto-scrolling
    val playheadFraction = (currentPlayheadMs.toFloat() / project.totalDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
    ) {
        // Zoom and Snapping Utility Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = "Timeline Tracks",
                    tint = StudioPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "MULTI-TRACK TIMELINE",
                    color = StudioTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Zoom Controller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onZoomChange((timelineZoom - 0.25f).coerceAtLeast(0.5f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${String.format("%.1f", timelineZoom)}x",
                    color = StudioPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = { onZoomChange((timelineZoom + 0.25f).coerceAtMost(3.0f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Timeline Scroll Area with Header & Tracks
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StudioSurface)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Fixed Left Track Headers (Premiere Pro style track headers)
                TrackHeadersColumn(
                    modifier = Modifier.width(60.dp),
                    selectedTrackType = selectedTrackType,
                    onSelectTrackType = onSelectTrackType
                )

                // Horizontal Scrolling Timeline Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .pointerInput(project.totalDurationMs) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val seekTime = (fraction * project.totalDurationMs).toLong()
                                onSeekTo(seekTime)
                            }
                        }
                        .pointerInput(project.totalDurationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                val seekTime = (fraction * project.totalDurationMs).toLong()
                                onSeekTo(seekTime)
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(totalTimelineWidthDp)
                            .fillMaxHeight()
                    ) {
                        // 1. Timecode Ruler
                        TimeRuler(
                            totalDurationMs = project.totalDurationMs,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        )

                        // 2. Text / Titles Track
                        TextTrackRow(
                            project = project,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )

                        // 3. PIP / Overlay Track
                        PIPTrackRow(
                            project = project,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        )

                        // 4. Main Video Track
                        VideoTrackRow(
                            project = project,
                            selectedClipId = selectedClipId,
                            onSelectClip = onSelectClip,
                            onOpenTransitions = onOpenTransitions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        )

                        // 5. Audio Track
                        AudioTrackRow(
                            project = project,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        )

                        // 6. Stickers / VFX Track
                        StickersTrackRow(
                            project = project,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        )
                    }

                    // Playhead Line with Red Needle Needle Head
                    PlayheadIndicator(
                        playheadFraction = playheadFraction,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackHeadersColumn(
    modifier: Modifier = Modifier,
    selectedTrackType: TrackType,
    onSelectTrackType: (TrackType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(StudioSurfaceVariant)
            .border(androidx.compose.foundation.BorderStroke(0.5.dp, StudioCardBorder))
    ) {
        // Ruler Spacer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(StudioTimelineRuler),
            contentAlignment = Alignment.Center
        ) {
            Text("SEC", color = StudioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // T1: Text Track Header
        TrackHeaderItem(
            label = "T1",
            icon = Icons.Default.Title,
            height = 26.dp,
            isSelected = selectedTrackType == TrackType.TEXT,
            onClick = { onSelectTrackType(TrackType.TEXT) }
        )

        // V2: PIP Track Header
        TrackHeaderItem(
            label = "V2",
            icon = Icons.Default.Layers,
            height = 26.dp,
            isSelected = selectedTrackType == TrackType.PIP,
            onClick = { onSelectTrackType(TrackType.PIP) }
        )

        // V1: Main Video Track Header
        TrackHeaderItem(
            label = "V1",
            icon = Icons.Default.Videocam,
            height = 64.dp,
            isSelected = selectedTrackType == TrackType.VIDEO,
            onClick = { onSelectTrackType(TrackType.VIDEO) }
        )

        // A1: Audio Track Header
        TrackHeaderItem(
            label = "A1",
            icon = Icons.Default.Audiotrack,
            height = 42.dp,
            isSelected = selectedTrackType == TrackType.AUDIO,
            onClick = { onSelectTrackType(TrackType.AUDIO) }
        )

        // FX: Sticker / VFX Track Header
        TrackHeaderItem(
            label = "FX",
            icon = Icons.Default.AutoAwesome,
            height = 24.dp,
            isSelected = selectedTrackType == TrackType.STICKER,
            onClick = { onSelectTrackType(TrackType.STICKER) }
        )
    }
}

@Composable
private fun TrackHeaderItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    height: androidx.compose.ui.unit.Dp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(if (isSelected) StudioPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) StudioPrimary else StudioTextMuted,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = if (isSelected) StudioPrimary else StudioTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimeRuler(
    totalDurationMs: Long,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (totalDurationMs / 1000).toInt() + 2

    Box(
        modifier = modifier
            .background(StudioTimelineRuler)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalW = size.width
            val stepPx = totalW / totalSeconds

            for (sec in 0..totalSeconds) {
                val x = sec * stepPx
                // Major second tick
                drawLine(
                    color = StudioTextMuted,
                    start = Offset(x, size.height * 0.4f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )

                // Minor half-second tick
                val midX = x + (stepPx / 2f)
                drawLine(
                    color = StudioTextMuted.copy(alpha = 0.4f),
                    start = Offset(midX, size.height * 0.7f),
                    end = Offset(midX, size.height),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }

        // Second Labels
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (sec in 0..totalSeconds step 2) {
                Text(
                    text = "${sec}s",
                    color = StudioTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoTrackRow(
    project: Project,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onOpenTransitions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDur = project.totalDurationMs.toFloat().coerceAtLeast(1000f)

    Row(
        modifier = modifier
            .background(StudioDarkBg)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for ((index, clip) in project.clips.withIndex()) {
            val clipWeight = (clip.effectiveDurationMs.toFloat() / totalDur).coerceIn(0.05f, 1.0f)
            val isSelected = clip.id == selectedClipId

            // Transition badge between clips
            if (index > 0) {
                Surface(
                    onClick = onOpenTransitions,
                    shape = RoundedCornerShape(3.dp),
                    color = if (clip.transitionIn != TransitionType.NONE) StudioPrimary else StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(horizontal = 1.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (clip.transitionIn != TransitionType.NONE) Icons.Default.Shuffle else Icons.Default.Add,
                            contentDescription = "Transition",
                            tint = if (clip.transitionIn != TransitionType.NONE) StudioDarkBg else StudioTextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Clip Block
            Box(
                modifier = Modifier
                    .weight(clipWeight)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) StudioTrackVideo else StudioTrackVideo.copy(alpha = 0.7f)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) StudioPrimary else StudioCardBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.title,
                            color = StudioTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                color = StudioAccentYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom clip badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${clip.effectiveDurationMs / 1000f}s",
                            color = StudioTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (clip.chromaCutout) {
                            Text(
                                text = "AI",
                                color = StudioAccentGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (clip.filter != FilterPreset.NORMAL) {
                            Text(
                                text = "LUT",
                                color = StudioPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioTrackRow(
    project: Project,
    modifier: Modifier = Modifier
) {
    val totalDur = project.totalDurationMs.toFloat().coerceAtLeast(1000f)

    Row(
        modifier = modifier
            .background(StudioDarkBg)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (audio in project.audioTracks) {
            val weight = (audio.durationMs.toFloat() / totalDur).coerceIn(0.1f, 1.0f)

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioTrackAudio)
                    .border(0.5.dp, StudioAccentGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎵 ${audio.title}",
                            color = StudioAccentGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${(audio.volume * 100).toInt()}%",
                            color = StudioTextSecondary,
                            fontSize = 8.sp
                        )
                    }

                    // Mini Audio Waveform Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {
                        val points = audio.waveformPoints
                        if (points.isNotEmpty()) {
                            val barW = size.width / points.size
                            for ((i, p) in points.withIndex()) {
                                val barH = size.height * p
                                val x = i * barW
                                val y = (size.height - barH) / 2f
                                drawRect(
                                    color = StudioAccentGreen.copy(alpha = 0.7f),
                                    topLeft = Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barW * 0.7f, barH)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextTrackRow(
    project: Project,
    modifier: Modifier = Modifier
) {
    val totalDur = project.totalDurationMs.toFloat().coerceAtLeast(1000f)

    Row(
        modifier = modifier
            .background(StudioDarkBg)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (item in project.textOverlays) {
            val weight = (item.durationMs.toFloat() / totalDur).coerceIn(0.08f, 1.0f)

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioTrackText)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "T: ${item.text}",
                    color = StudioTextPrimary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PIPTrackRow(
    project: Project,
    modifier: Modifier = Modifier
) {
    val totalDur = project.totalDurationMs.toFloat().coerceAtLeast(1000f)

    Row(
        modifier = modifier
            .background(StudioDarkBg)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (pip in project.pipOverlays) {
            val weight = (pip.durationMs.toFloat() / totalDur).coerceIn(0.08f, 1.0f)

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E3A4C))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "PIP: ${pip.title}",
                    color = StudioPrimary,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StickersTrackRow(
    project: Project,
    modifier: Modifier = Modifier
) {
    val totalDur = project.totalDurationMs.toFloat().coerceAtLeast(1000f)

    Row(
        modifier = modifier
            .background(StudioDarkBg)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (stk in project.stickerOverlays) {
            val weight = (stk.durationMs.toFloat() / totalDur).coerceIn(0.08f, 1.0f)

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioTrackEffect)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "FX: ${stk.iconEmoji}",
                    color = StudioAccentYellow,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlayheadIndicator(
    playheadFraction: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val playheadX = maxWidth * playheadFraction

        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(maxWidth)
        ) {
            val x = playheadX.toPx()

            // Red vertical playhead line
            drawLine(
                color = StudioPlayheadRed,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.dp.toPx()
            )

            // Red top needle diamond cap
            val headPath = Path().apply {
                moveTo(x - 6.dp.toPx(), 0f)
                lineTo(x + 6.dp.toPx(), 0f)
                lineTo(x + 6.dp.toPx(), 12.dp.toPx())
                lineTo(x, 18.dp.toPx())
                lineTo(x - 6.dp.toPx(), 12.dp.toPx())
                close()
            }
            drawPath(path = headPath, color = StudioPlayheadRed)
        }
    }
}
