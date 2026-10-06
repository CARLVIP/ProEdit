package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun PreviewPlayer(
    project: Project,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    selectedClip: MediaClip?,
    onTogglePlay: () -> Unit,
    onStepFrame: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGridGuides by remember { mutableStateOf(false) }

    // Find active clip at current playhead
    var accumulatedMs = 0L
    var activeClip = project.clips.firstOrNull()
    var offsetInClip = 0L

    for (clip in project.clips) {
        val next = accumulatedMs + clip.effectiveDurationMs
        if (currentPlayheadMs in accumulatedMs until next) {
            activeClip = clip
            offsetInClip = currentPlayheadMs - accumulatedMs
            break
        }
        accumulatedMs = next
    }

    // Check active transition
    val isNearTransition = activeClip?.transitionIn != TransitionType.NONE &&
            offsetInClip < (activeClip?.transitionDurationMs ?: 600L)

    // Format timecode HH:MM:SS:FF
    val totalSeconds = currentPlayheadMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val frames = ((currentPlayheadMs % 1000) / (1000.0 / project.fps)).toInt()
    val timecodeStr = String.format("%02d:%02d:%02d", minutes, seconds, frames)

    val totalDurSec = project.totalDurationMs / 1000
    val totalTimeStr = String.format("%02d:%02d:00", totalDurSec / 60, totalDurSec % 60)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
    ) {
        // Top Player Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timecode display
            Surface(
                color = StudioSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isPlaying) StudioAccentGreen else StudioPlayheadRed,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$timecodeStr / $totalTimeStr",
                        color = StudioTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Ratio & Quality Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = StudioSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = project.aspectRatio.label,
                        color = StudioPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = StudioSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${project.fps} FPS",
                        color = StudioTextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = { showGridGuides = !showGridGuides },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (showGridGuides) Icons.Default.GridOn else Icons.Default.GridOff,
                        contentDescription = "Toggle Grid Guides",
                        tint = if (showGridGuides) StudioPrimary else StudioTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Canvas Viewport Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Aspect Ratio Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(project.aspectRatio.ratio)
                    .clip(RoundedCornerShape(4.dp))
                    .background(getCanvasBgBrush(project.canvasBg))
            ) {
                // Generative Realtime Video Canvas
                activeClip?.let { clip ->
                    VideoFrameCanvas(
                        clip = clip,
                        project = project,
                        currentPlayheadMs = currentPlayheadMs,
                        offsetInClip = offsetInClip,
                        isNearTransition = isNearTransition,
                        showGridGuides = showGridGuides
                    )
                }

                // Render Active PIP (Picture-in-Picture)
                for (pip in project.pipOverlays) {
                    if (currentPlayheadMs in pip.startOffsetMs until (pip.startOffsetMs + pip.durationMs)) {
                        PipLayer(pip = pip)
                    }
                }

                // Render Active Text Overlays
                for (textOverlay in project.textOverlays) {
                    if (currentPlayheadMs in textOverlay.startOffsetMs until (textOverlay.startOffsetMs + textOverlay.durationMs)) {
                        TextOverlayView(
                            overlay = textOverlay,
                            currentPlayheadMs = currentPlayheadMs
                        )
                    }
                }

                // Render Active Sticker Overlays
                for (sticker in project.stickerOverlays) {
                    if (currentPlayheadMs in sticker.startOffsetMs until (sticker.startOffsetMs + sticker.durationMs)) {
                        StickerOverlayView(sticker = sticker)
                    }
                }

                // Play / Pause central tap layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPlaying) {
                        Surface(
                            shape = CircleShape,
                            color = StudioDarkBg.copy(alpha = 0.65f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioPrimary.copy(alpha = 0.8f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transport Controls Row (Play, Step, Rewind)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step back 1 frame
                IconButton(
                    onClick = { onStepFrame(-1) },
                    modifier = Modifier.size(32.dp).testTag("step_backward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Frame",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play / Pause Button
                FilledIconButton(
                    onClick = onTogglePlay,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = StudioPrimary,
                        contentColor = StudioDarkBg
                    ),
                    modifier = Modifier.size(36.dp).testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Step forward 1 frame
                IconButton(
                    onClick = { onStepFrame(1) },
                    modifier = Modifier.size(32.dp).testTag("step_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Frame",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Active clip info badge
            activeClip?.let { clip ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (clip.chromaCutout) {
                        Surface(
                            color = StudioAccentGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CHROMA",
                                color = StudioAccentGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (clip.filter != FilterPreset.NORMAL) {
                        Surface(
                            color = StudioTertiary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = clip.filter.displayName,
                                color = StudioPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = clip.title,
                        color = StudioTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun VideoFrameCanvas(
    clip: MediaClip,
    project: Project,
    currentPlayheadMs: Long,
    offsetInClip: Long,
    isNearTransition: Boolean,
    showGridGuides: Boolean
) {
    val progress = (offsetInClip.toFloat() / clip.effectiveDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)

    // Keyframes interpolation
    val animatedScale = remember(clip, offsetInClip) {
        if (clip.keyframes.isEmpty()) clip.scale
        else {
            val sorted = clip.keyframes.sortedBy { it.timeMs }
            val nextKf = sorted.firstOrNull { it.timeMs >= offsetInClip }
            val prevKf = sorted.lastOrNull { it.timeMs <= offsetInClip }
            if (prevKf != null && nextKf != null && prevKf != nextKf) {
                val ratio = (offsetInClip - prevKf.timeMs).toFloat() / (nextKf.timeMs - prevKf.timeMs)
                prevKf.scale + (nextKf.scale - prevKf.scale) * ratio
            } else {
                nextKf?.scale ?: prevKf?.scale ?: clip.scale
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(animatedScale)
            .rotate(clip.rotation)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCinematicScene(
                clip = clip,
                progress = progress,
                sampleId = clip.sampleIdentifier,
                filter = clip.filter,
                lumetri = clip.lumetri,
                isNearTransition = isNearTransition
            )

            if (showGridGuides) {
                drawRuleOfThirdsGuides()
            }
        }
    }
}

private fun DrawScope.drawCinematicScene(
    clip: MediaClip,
    progress: Float,
    sampleId: String,
    filter: FilterPreset,
    lumetri: LumetriAdjustments,
    isNearTransition: Boolean
) {
    val w = size.width
    val h = size.height

    // Dynamic scene color pallets based on clip identifier
    val (baseTopColor, baseBottomColor, accentColor) = when (sampleId) {
        "sample_sunset" -> Triple(
            Color(0xFF8E2DE2),
            Color(0xFFF000FF),
            Color(0xFFFFB300)
        )
        "sample_racing" -> Triple(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF00E5FF)
        )
        "sample_coffee" -> Triple(
            Color(0xFF3E2723),
            Color(0xFF5D4037),
            Color(0xFFFFCC80)
        )
        else -> Triple( // "sample_urban" / Default
            Color(0xFF0B0C10),
            Color(0xFF1F2833),
            Color(0xFF66FCF1)
        )
    }

    // Apply Filter Preset color shifting
    val (finalTop, finalBottom) = when (filter) {
        FilterPreset.TEAL_ORANGE -> Pair(Color(0xFF003B46), Color(0xFFC75D14))
        FilterPreset.CYBERPUNK -> Pair(Color(0xFF2B003D), Color(0xFFFF007F))
        FilterPreset.FILM_NOIR -> Pair(Color(0xFF121212), Color(0xFF686868))
        FilterPreset.VINTAGE_90S -> Pair(Color(0xFF4A3B32), Color(0xFFA67B5B))
        FilterPreset.SUNSET_GLOW -> Pair(Color(0xFF6B1D2F), Color(0xFFFF8C42))
        FilterPreset.EMERALD_NIGHT -> Pair(Color(0xFF072619), Color(0xFF15653D))
        FilterPreset.DRAMATIC -> Pair(Color(0xFF1A1A24), Color(0xFF434354))
        FilterPreset.NORMAL -> Pair(baseTopColor, baseBottomColor)
    }

    // Draw Background Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(finalTop, finalBottom)
        )
    )

    // Dynamic moving horizon / motion elements
    val horizonY = h * 0.58f
    val sunRadius = w * 0.18f
    val sunCenterX = w * 0.5f + (progress - 0.5f) * (w * 0.2f)
    val sunCenterY = horizonY - sunRadius * 0.4f

    // Sun / Ambient source
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.9f), accentColor.copy(alpha = 0f)),
            center = Offset(sunCenterX, sunCenterY),
            radius = sunRadius * 1.5f
        ),
        radius = sunRadius * 1.5f,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Horizon line
    drawLine(
        color = accentColor.copy(alpha = 0.5f),
        start = Offset(0f, horizonY),
        end = Offset(w, horizonY),
        strokeWidth = 2.dp.toPx()
    )

    // Perspective Cyber Grid Lines (like synthwave / film studio)
    val gridCount = 9
    for (i in 0..gridCount) {
        val bottomX = w * (i.toFloat() / gridCount)
        val vanishX = w * 0.5f
        drawLine(
            color = accentColor.copy(alpha = 0.25f),
            start = Offset(vanishX, horizonY),
            end = Offset(bottomX, h),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Moving scan lines on grid
    val scanOffset = (progress * 5f) % 1f
    for (step in 1..5) {
        val yPos = horizonY + (h - horizonY) * ((step + scanOffset) / 6f)
        if (yPos < h) {
            drawLine(
                color = accentColor.copy(alpha = 0.3f),
                start = Offset(0f, yPos),
                end = Offset(w, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }
    }

    // Lumetri Adjustments Simulation:
    // Temperature: Cool blue vs Warm orange overlay
    if (lumetri.temperature != 0f) {
        val tempColor = if (lumetri.temperature > 0) Color(0xFFFF9800) else Color(0xFF00B0FF)
        drawRect(
            color = tempColor.copy(alpha = kotlin.math.abs(lumetri.temperature) * 0.25f)
        )
    }

    // Exposure: Brightness overlay
    if (lumetri.exposure != 0f) {
        val exposureColor = if (lumetri.exposure > 0) Color.White else Color.Black
        drawRect(
            color = exposureColor.copy(alpha = kotlin.math.abs(lumetri.exposure) * 0.4f)
        )
    }

    // Vignette
    if (lumetri.vignette > 0f) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = lumetri.vignette * 0.85f)),
                center = Offset(w / 2f, h / 2f),
                radius = kotlin.math.max(w, h) * 0.7f
            )
        )
    }

    // Transition effect simulation (Cross dissolve, Glitch, Flash White)
    if (isNearTransition) {
        when (clip.transitionIn) {
            TransitionType.FLASH_WHITE -> {
                drawRect(color = Color.White.copy(alpha = 0.65f))
            }
            TransitionType.GLITCH -> {
                // Glitch bars
                for (b in 0..4) {
                    val barY = h * (b * 0.2f + (progress % 0.1f))
                    drawRect(
                        color = Color.Cyan.copy(alpha = 0.4f),
                        topLeft = Offset(0f, barY),
                        size = Size(w, 8.dp.toPx())
                    )
                }
            }
            TransitionType.DISSOLVE -> {
                drawRect(color = Color.Black.copy(alpha = 0.35f))
            }
            else -> {}
        }
    }
}

private fun DrawScope.drawRuleOfThirdsGuides() {
    val col1 = size.width / 3f
    val col2 = (size.width / 3f) * 2f
    val row1 = size.height / 3f
    val row2 = (size.height / 3f) * 2f
    val guideColor = StudioPrimary.copy(alpha = 0.4f)
    val stroke = Stroke(width = 1.dp.toPx())

    drawLine(color = guideColor, start = Offset(col1, 0f), end = Offset(col1, size.height), strokeWidth = stroke.width)
    drawLine(color = guideColor, start = Offset(col2, 0f), end = Offset(col2, size.height), strokeWidth = stroke.width)
    drawLine(color = guideColor, start = Offset(0f, row1), end = Offset(size.width, row1), strokeWidth = stroke.width)
    drawLine(color = guideColor, start = Offset(0f, row2), end = Offset(size.width, row2), strokeWidth = stroke.width)
}

@Composable
fun PipLayer(pip: PIPOverlay) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant.copy(alpha = pip.opacity),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioPrimary),
            modifier = Modifier
                .fillMaxWidth(pip.scale)
                .aspectRatio(16f / 9f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "PIP",
                        tint = StudioPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = pip.title,
                        color = StudioTextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TextOverlayView(overlay: TextOverlay, currentPlayheadMs: Long) {
    val progress = ((currentPlayheadMs - overlay.startOffsetMs).toFloat() / overlay.durationMs).coerceIn(0f, 1f)

    val displayText = remember(overlay, progress) {
        if (overlay.animationStyle == TextAnimStyle.TYPEWRITER) {
            val length = (overlay.text.length * progress).toInt().coerceIn(0, overlay.text.length)
            overlay.text.take(length) + if (length < overlay.text.length) "▍" else ""
        } else {
            overlay.text
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = if (overlay.hasBackgroundPlate) StudioDarkBg.copy(alpha = 0.65f) else Color.Transparent,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = displayText,
                color = Color(overlay.textColorHex),
                fontSize = overlay.fontSizeSp.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = when (overlay.fontStyle) {
                    TextFontStyle.TYPEWRITER -> FontFamily.Monospace
                    TextFontStyle.CINEMATIC_SERIF -> FontFamily.Serif
                    else -> FontFamily.SansSerif
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun StickerOverlayView(sticker: StickerOverlay) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioDarkBg.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccentYellow.copy(alpha = 0.5f))
        ) {
            Text(
                text = sticker.iconEmoji,
                color = StudioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

fun getCanvasBgBrush(bg: CanvasBackground): Brush {
    return when (bg) {
        CanvasBackground.GRADIENT_CYAN_PURPLE -> Brush.linearGradient(
            listOf(Color(0xFF0F2027), Color(0xFF2C5364))
        )
        CanvasBackground.GRADIENT_SUNSET -> Brush.linearGradient(
            listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
        )
        CanvasBackground.BLUR_HEAVY -> Brush.radialGradient(
            listOf(Color(0xFF1E2230), Color(0xFF0A0C10))
        )
        CanvasBackground.BLUR_LIGHT -> Brush.radialGradient(
            listOf(Color(0xFF141724), Color(0xFF090A0E))
        )
        CanvasBackground.SOLID_BLACK -> Brush.linearGradient(
            listOf(Color.Black, Color.Black)
        )
        CanvasBackground.SOLID_DARK_SLATE -> Brush.linearGradient(
            listOf(Color(0xFF12141A), Color(0xFF12141A))
        )
    }
}
