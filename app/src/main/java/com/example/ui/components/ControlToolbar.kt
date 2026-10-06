package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaClip
import com.example.ui.theme.*
import com.example.viewmodel.EditorSheetType

@Composable
fun ControlToolbar(
    selectedClip: MediaClip?,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenSheet: (EditorSheetType) -> Unit,
    onToggleMute: () -> Unit,
    onToggleReverse: () -> Unit,
    onToggleFreeze: () -> Unit,
    onAddKeyframe: () -> Unit,
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(vertical = 4.dp)
    ) {
        // Quick Action Bar (Undo, Redo, Razor Split, Duplicate, Delete, Add Media)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Undo / Redo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(32.dp).testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(32.dp).testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Central Razor Cut Highlight Button
            Button(
                onClick = onSplit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAccentOrange.copy(alpha = 0.2f),
                    contentColor = StudioAccentOrange
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("split_clip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = "Split",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SPLIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Right Quick Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Duplicate
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(32.dp).testTag("duplicate_clip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_clip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = StudioPlayheadRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Media (+)
                FilledIconButton(
                    onClick = onAddMediaClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = StudioPrimary,
                        contentColor = StudioDarkBg
                    ),
                    modifier = Modifier.size(32.dp).testTag("add_media_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Media",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Divider(
            color = StudioCardBorder,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // Bottom Scrollable Studio Panels Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // CapCut Velocity Speed Curves
            StudioToolItem(
                label = "Speed Curves",
                subLabel = "CapCut",
                icon = Icons.Default.Speed,
                accentColor = StudioPrimary,
                onClick = { onOpenSheet(EditorSheetType.SPEED_CURVE) },
                testTag = "tool_speed_curves"
            )

            // Premiere Lumetri Color Grading
            StudioToolItem(
                label = "Lumetri Color",
                subLabel = "Premiere",
                icon = Icons.Default.ColorLens,
                accentColor = StudioSecondary,
                onClick = { onOpenSheet(EditorSheetType.LUMETRI) },
                testTag = "tool_lumetri_color"
            )

            // InShot Aspect Ratio & Canvas
            StudioToolItem(
                label = "Ratio & Canvas",
                subLabel = "InShot",
                icon = Icons.Default.AspectRatio,
                accentColor = StudioTertiary,
                onClick = { onOpenSheet(EditorSheetType.RATIO_CANVAS) },
                testTag = "tool_canvas_ratio"
            )

            // Dynamic Transitions
            StudioToolItem(
                label = "Transitions",
                subLabel = "VFX",
                icon = Icons.Default.Shuffle,
                accentColor = StudioPrimary,
                onClick = { onOpenSheet(EditorSheetType.TRANSITIONS) },
                testTag = "tool_transitions"
            )

            // AI Chroma Cutout / Green Screen
            StudioToolItem(
                label = "AI Cutout",
                subLabel = if (selectedClip?.chromaCutout == true) "ON" else "Chroma",
                icon = Icons.Default.AutoFixHigh,
                accentColor = StudioAccentGreen,
                isActive = selectedClip?.chromaCutout == true,
                onClick = { onOpenSheet(EditorSheetType.CHROMA_CUTOUT) },
                testTag = "tool_ai_cutout"
            )

            // Audio & SFX Mixer
            StudioToolItem(
                label = "Audio & SFX",
                subLabel = "Beats",
                icon = Icons.Default.GraphicEq,
                accentColor = StudioAccentGreen,
                onClick = { onOpenSheet(EditorSheetType.AUDIO_MIXER) },
                testTag = "tool_audio_mixer"
            )

            // Animated Titles & Text
            StudioToolItem(
                label = "Text & Titles",
                subLabel = "Animated",
                icon = Icons.Default.TextFields,
                accentColor = StudioSecondary,
                onClick = { onOpenSheet(EditorSheetType.TEXT_STICKER) },
                testTag = "tool_text_titles"
            )

            // Keyframes Diamond
            StudioToolItem(
                label = "Keyframes",
                subLabel = "Animate",
                icon = Icons.Default.Diamond,
                accentColor = StudioAccentYellow,
                onClick = { onOpenSheet(EditorSheetType.KEYFRAMES) },
                testTag = "tool_keyframes"
            )

            // Reverse
            StudioToolItem(
                label = "Reverse",
                subLabel = if (selectedClip?.isReversed == true) "ON" else "Off",
                icon = Icons.Default.SwapHoriz,
                accentColor = StudioTextPrimary,
                isActive = selectedClip?.isReversed == true,
                onClick = onToggleReverse,
                testTag = "tool_reverse"
            )

            // Freeze Frame
            StudioToolItem(
                label = "Freeze",
                subLabel = if (selectedClip?.isFrozen == true) "FROZEN" else "Frame",
                icon = Icons.Default.AcUnit,
                accentColor = StudioPrimary,
                isActive = selectedClip?.isFrozen == true,
                onClick = onToggleFreeze,
                testTag = "tool_freeze"
            )

            // Mute / Unmute
            StudioToolItem(
                label = "Audio Mute",
                subLabel = if (selectedClip?.isMuted == true) "MUTED" else "Active",
                icon = if (selectedClip?.isMuted == true) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                accentColor = StudioPlayheadRed,
                isActive = selectedClip?.isMuted == true,
                onClick = onToggleMute,
                testTag = "tool_mute"
            )
        }
    }
}

@Composable
private fun StudioToolItem(
    label: String,
    subLabel: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    isActive: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) accentColor.copy(alpha = 0.2f) else StudioSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) accentColor else StudioCardBorder
        ),
        modifier = Modifier
            .width(76.dp)
            .height(54.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) accentColor else StudioTextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = StudioTextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subLabel,
                color = if (isActive) accentColor else StudioTextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
