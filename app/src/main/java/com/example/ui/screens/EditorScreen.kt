package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleMediaRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.EditorSheetType
import com.example.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.currentProject.collectAsState()
    val currentPlayheadMs by viewModel.currentPlayheadMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val selectedTrackType by viewModel.selectedTrackType.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val timelineZoom by viewModel.timelineZoom.collectAsState()
    val exportConfig by viewModel.exportConfig.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsState()

    var showMediaPickerModal by remember { mutableStateOf(false) }

    // Android Standard zero-permission photo/video picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addMediaClip(
                title = "Device Clip",
                sampleId = "sample_urban",
                durationMs = 6000L,
                uri = uri.toString()
            )
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg),
        containerColor = StudioDarkBg,
        topBar = {
            EditorTopBar(
                title = project.title,
                aspectRatio = project.aspectRatio,
                onNavigateBack = onNavigateBack,
                onOpenExport = { viewModel.openExportDialog() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Real-time Video Viewport
            PreviewPlayer(
                project = project,
                currentPlayheadMs = currentPlayheadMs,
                isPlaying = isPlaying,
                selectedClip = viewModel.selectedClip,
                onTogglePlay = { viewModel.togglePlay() },
                onStepFrame = { viewModel.stepFrame(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )

            // 2. Multi-track Timeline View
            TimelineView(
                project = project,
                currentPlayheadMs = currentPlayheadMs,
                selectedClipId = selectedClipId,
                selectedTrackType = selectedTrackType,
                timelineZoom = timelineZoom,
                onSeekTo = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onSelectTrackType = { viewModel.selectTrackType(it) },
                onOpenTransitions = { viewModel.openSheet(EditorSheetType.TRANSITIONS) },
                onZoomChange = { viewModel.setTimelineZoom(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 3. Pro Action Toolbar
            ControlToolbar(
                selectedClip = viewModel.selectedClip,
                onSplit = { viewModel.splitClipAtPlayhead() },
                onDelete = { viewModel.deleteSelectedClip() },
                onDuplicate = { viewModel.duplicateSelectedClip() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onOpenSheet = { viewModel.openSheet(it) },
                onToggleMute = { viewModel.toggleMuteClip() },
                onToggleReverse = { viewModel.toggleReverseClip() },
                onToggleFreeze = { viewModel.toggleFreezeClip() },
                onAddKeyframe = { viewModel.addKeyframeAtCurrentTime() },
                onAddMediaClick = { showMediaPickerModal = true }
            )

            // Active Sheet Panel
            activeSheet?.let { sheet ->
                when (sheet) {
                    EditorSheetType.LUMETRI -> {
                        LumetriColorSheet(
                            activeFilter = viewModel.selectedClip?.filter ?: FilterPreset.NORMAL,
                            lumetri = viewModel.selectedClip?.lumetri ?: LumetriAdjustments(),
                            onFilterChange = { viewModel.updateFilter(it) },
                            onLumetriChange = { viewModel.updateLumetri(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.SPEED_CURVE -> {
                        SpeedCurvesSheet(
                            currentSpeed = viewModel.selectedClip?.speed ?: 1.0f,
                            currentCurve = viewModel.selectedClip?.speedCurve ?: SpeedCurveType.STANDARD,
                            onSpeedChange = { s, c -> viewModel.updateClipSpeed(s, c) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.RATIO_CANVAS -> {
                        CanvasRatioSheet(
                            activeRatio = project.aspectRatio,
                            activeCanvasBg = project.canvasBg,
                            onRatioChange = { viewModel.updateAspectRatio(it) },
                            onCanvasBgChange = { viewModel.updateCanvasBg(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.TRANSITIONS -> {
                        TransitionsSheet(
                            activeTransition = viewModel.selectedClip?.transitionIn ?: TransitionType.NONE,
                            transitionDurationMs = viewModel.selectedClip?.transitionDurationMs ?: 600L,
                            onTransitionChange = { t, d -> viewModel.updateTransition(t, d) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.AUDIO_MIXER -> {
                        AudioMixerSheet(
                            audioTracks = project.audioTracks,
                            onVolumeChange = { id, v -> viewModel.updateAudioVolume(id, v) },
                            onAddTrack = { viewModel.addAudioTrack(it) },
                            onDeleteTrack = { viewModel.deleteAudioTrack(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.TEXT_STICKER -> {
                        TextStickerSheet(
                            onAddText = { txt, font, anim -> viewModel.addTextOverlay(txt, font, anim) },
                            onAddSticker = { emoji, name -> viewModel.addStickerOverlay(emoji, name) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.CHROMA_CUTOUT -> {
                        ChromaCutoutSheet(
                            clip = viewModel.selectedClip,
                            onToggleCutout = { viewModel.toggleChromaCutout() },
                            onToleranceChange = { viewModel.updateChromaTolerance(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    EditorSheetType.KEYFRAMES -> {
                        KeyframesSheet(
                            clip = viewModel.selectedClip,
                            currentPlayheadMs = currentPlayheadMs,
                            onAddKeyframe = { viewModel.addKeyframeAtCurrentTime() },
                            onTransformChange = { scale, rot, flipH, flipV ->
                                viewModel.updateClipTransform(scale, rot, flipH, flipV)
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
            }
        }
    }

    // Media Import Modal (Choose from Pro Sample Library or Pick Local Device Video)
    if (showMediaPickerModal) {
        MediaPickerModal(
            onDismiss = { showMediaPickerModal = false },
            onPickDeviceMedia = {
                showMediaPickerModal = false
                mediaPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onAddSampleClip = { sampleClip ->
                showMediaPickerModal = false
                viewModel.addMediaClip(
                    title = sampleClip.title,
                    sampleId = sampleClip.sampleIdentifier,
                    durationMs = sampleClip.durationMs
                )
            }
        )
    }

    // Fast Export Dialog ("قابلیت خروجی گرفتن سریع")
    FastExportDialog(
        isOpen = isExportDialogOpen,
        exportConfig = exportConfig,
        exportState = exportState,
        onConfigChange = { viewModel.updateExportConfig(it) },
        onStartExport = { viewModel.startFastExport() },
        onCancelExport = { viewModel.cancelExport() },
        onDismiss = { viewModel.closeExportDialog() }
    )
}

@Composable
private fun EditorTopBar(
    title: String,
    aspectRatio: AspectRatio,
    onNavigateBack: () -> Unit,
    onOpenExport: () -> Unit
) {
    Surface(
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, StudioCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp).testTag("back_to_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = StudioTextPrimary
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ProEdit",
                            color = StudioPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = StudioSurfaceVariant,
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "STUDIO",
                                color = StudioTextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        color = StudioTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // High-Impact Fast Export Button
            Button(
                onClick = onOpenExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary,
                    contentColor = StudioDarkBg
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("export_video_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Fast Export",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXPORT (خروجی)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun MediaPickerModal(
    onDismiss: () -> Unit,
    onPickDeviceMedia: () -> Unit,
    onAddSampleClip: (MediaClip) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = "Import", tint = StudioPrimary)
                Text("Add Video Clips (افزودن مدیا)", color = StudioTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Device Photo/Video Picker Button
                Button(
                    onClick = onPickDeviceMedia,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioPrimary.copy(alpha = 0.2f),
                        contentColor = StudioPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pick_device_video_button")
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Pick Device", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select from Device Gallery (گالری)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = StudioCardBorder, thickness = 0.5.dp)

                Text("Or Choose Built-in 4K Pro Clips:", color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)

                for (demo in SampleMediaRepository.defaultDemoClips) {
                    Surface(
                        onClick = { onAddSampleClip(demo) },
                        shape = RoundedCornerShape(6.dp),
                        color = StudioSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(demo.title, color = StudioTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${demo.durationMs / 1000}s • 4K HDR", color = StudioTextSecondary, fontSize = 9.sp)
                            }
                            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Add", tint = StudioPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StudioTextSecondary)
            }
        }
    )
}
