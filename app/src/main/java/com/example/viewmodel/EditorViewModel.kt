package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleMediaRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TrackType { VIDEO, AUDIO, TEXT, PIP, STICKER }

enum class EditorSheetType {
    LUMETRI,
    SPEED_CURVE,
    TRANSITIONS,
    RATIO_CANVAS,
    AUDIO_MIXER,
    TEXT_STICKER,
    KEYFRAMES,
    CHROMA_CUTOUT
}

class EditorViewModel : ViewModel() {

    private val _currentProject = MutableStateFlow(SampleMediaRepository.createInitialProject())
    val currentProject: StateFlow<Project> = _currentProject.asStateFlow()

    private val _currentPlayheadMs = MutableStateFlow(0L)
    val currentPlayheadMs: StateFlow<Long> = _currentPlayheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(
        SampleMediaRepository.createInitialProject().clips.firstOrNull()?.id
    )
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTrackType = MutableStateFlow(TrackType.VIDEO)
    val selectedTrackType: StateFlow<TrackType> = _selectedTrackType.asStateFlow()

    private val _activeSheet = MutableStateFlow<EditorSheetType?>(null)
    val activeSheet: StateFlow<EditorSheetType?> = _activeSheet.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f)
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    private val _exportConfig = MutableStateFlow(ExportConfig())
    val exportConfig: StateFlow<ExportConfig> = _exportConfig.asStateFlow()

    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _userProjects = MutableStateFlow<List<Project>>(SampleMediaRepository.sampleProjectTemplates)
    val userProjects: StateFlow<List<Project>> = _userProjects.asStateFlow()

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private var playbackJob: Job? = null
    private var exportJob: Job? = null

    val selectedClip: MediaClip?
        get() = currentProject.value.clips.find { it.id == selectedClipId.value }

    fun togglePlay() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val tickRate = 33L // ~30 fps tick
            while (_isPlaying.value) {
                delay(tickRate)
                val total = _currentProject.value.totalDurationMs
                val next = _currentPlayheadMs.value + tickRate
                if (next >= total) {
                    _currentPlayheadMs.value = 0L
                    _isPlaying.value = false
                } else {
                    _currentPlayheadMs.value = next
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        val total = _currentProject.value.totalDurationMs
        val clamped = timeMs.coerceIn(0L, total)
        _currentPlayheadMs.value = clamped
        autoSelectClipAtTime(clamped)
    }

    fun stepFrame(deltaFrames: Int) {
        pause()
        val frameDurationMs = (1000L / _currentProject.value.fps.coerceAtLeast(24)).coerceAtLeast(16L)
        val target = _currentPlayheadMs.value + (deltaFrames * frameDurationMs)
        seekTo(target)
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
        _selectedTrackType.value = TrackType.VIDEO
    }

    fun selectTrackType(trackType: TrackType) {
        _selectedTrackType.value = trackType
    }

    fun openSheet(sheet: EditorSheetType?) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = null
    }

    fun setTimelineZoom(zoom: Float) {
        _timelineZoom.value = zoom.coerceIn(0.5f, 3.5f)
    }

    private fun pushUndo() {
        undoStack.add(_currentProject.value)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_currentProject.value)
            _currentProject.value = previous
            if (_selectedClipId.value !in previous.clips.map { it.id }) {
                _selectedClipId.value = previous.clips.firstOrNull()?.id
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_currentProject.value)
            _currentProject.value = next
        }
    }

    private fun autoSelectClipAtTime(timeMs: Long) {
        var runningMs = 0L
        for (clip in _currentProject.value.clips) {
            val endMs = runningMs + clip.effectiveDurationMs
            if (timeMs in runningMs until endMs) {
                _selectedClipId.value = clip.id
                return
            }
            runningMs = endMs
        }
    }

    // Premiere Razor / Split Tool
    fun splitClipAtPlayhead() {
        val clip = selectedClip ?: return
        pushUndo()
        val clips = _currentProject.value.clips
        val clipIndex = clips.indexOfFirst { it.id == clip.id }
        if (clipIndex == -1) return

        var clipStartMs = 0L
        for (i in 0 until clipIndex) {
            clipStartMs += clips[i].effectiveDurationMs
        }
        val playhead = _currentPlayheadMs.value
        val offsetInsideClip = (playhead - clipStartMs).coerceIn(0L, clip.effectiveDurationMs)

        // Only split if offset is far enough from boundaries (min 300ms)
        if (offsetInsideClip < 300L || (clip.effectiveDurationMs - offsetInsideClip) < 300L) {
            return
        }

        val splitPointMediaMs = clip.startTrimMs + (offsetInsideClip * clip.speed).toLong()

        val firstHalf = clip.copy(
            id = clip.id,
            endTrimMs = splitPointMediaMs
        )
        val secondHalf = clip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            title = "${clip.title} (Part 2)",
            startTrimMs = splitPointMediaMs,
            endTrimMs = clip.endTrimMs,
            transitionIn = TransitionType.NONE
        )

        val updatedClips = clips.toMutableList()
        updatedClips[clipIndex] = firstHalf
        updatedClips.add(clipIndex + 1, secondHalf)

        _currentProject.update { it.copy(clips = updatedClips) }
        _selectedClipId.value = secondHalf.id
    }

    fun deleteSelectedClip() {
        val clipId = _selectedClipId.value ?: return
        pushUndo()
        val remaining = _currentProject.value.clips.filterNot { it.id == clipId }
        if (remaining.isNotEmpty()) {
            _currentProject.update { it.copy(clips = remaining) }
            _selectedClipId.value = remaining.first().id
            seekTo(0L)
        }
    }

    fun duplicateSelectedClip() {
        val clip = selectedClip ?: return
        pushUndo()
        val clips = _currentProject.value.clips.toMutableList()
        val index = clips.indexOfFirst { it.id == clip.id }
        val copy = clip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            title = "${clip.title} Copy"
        )
        if (index != -1) {
            clips.add(index + 1, copy)
            _currentProject.update { it.copy(clips = clips) }
            _selectedClipId.value = copy.id
        }
    }

    fun updateSelectedClipTrim(startTrim: Long, endTrim: Long) {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(
            startTrimMs = startTrim.coerceIn(0L, clip.durationMs - 500L),
            endTrimMs = endTrim.coerceIn(startTrim + 500L, clip.durationMs)
        )
        updateClip(updated)
    }

    fun updateClipSpeed(speed: Float, curve: SpeedCurveType) {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(
            speed = speed.coerceIn(0.1f, 10.0f),
            speedCurve = curve
        )
        updateClip(updated)
    }

    fun updateLumetri(adjustments: LumetriAdjustments) {
        val clip = selectedClip ?: return
        val updated = clip.copy(lumetri = adjustments)
        updateClip(updated, recordUndo = false)
    }

    fun commitLumetriUndo() {
        pushUndo()
    }

    fun updateFilter(filter: FilterPreset) {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(filter = filter)
        updateClip(updated)
    }

    fun updateTransition(transition: TransitionType, durationMs: Long = 600L) {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(transitionIn = transition, transitionDurationMs = durationMs)
        updateClip(updated)
    }

    fun toggleChromaCutout() {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(chromaCutout = !clip.chromaCutout)
        updateClip(updated)
    }

    fun updateChromaTolerance(tolerance: Float) {
        val clip = selectedClip ?: return
        val updated = clip.copy(chromaTolerance = tolerance.coerceIn(0.1f, 0.9f))
        updateClip(updated, recordUndo = false)
    }

    fun toggleMuteClip() {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(isMuted = !clip.isMuted)
        updateClip(updated)
    }

    fun toggleReverseClip() {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(isReversed = !clip.isReversed)
        updateClip(updated)
    }

    fun toggleFreezeClip() {
        val clip = selectedClip ?: return
        pushUndo()
        val updated = clip.copy(isFrozen = !clip.isFrozen)
        updateClip(updated)
    }

    fun updateClipTransform(scale: Float, rotation: Float, flipH: Boolean, flipV: Boolean) {
        val clip = selectedClip ?: return
        val updated = clip.copy(
            scale = scale.coerceIn(0.5f, 3.0f),
            rotation = rotation,
            flipH = flipH,
            flipV = flipV
        )
        updateClip(updated)
    }

    fun addKeyframeAtCurrentTime() {
        val clip = selectedClip ?: return
        pushUndo()
        val timeOffset = (_currentPlayheadMs.value % clip.effectiveDurationMs)
        val kf = Keyframe(
            id = "kf_" + UUID.randomUUID().toString().take(6),
            timeMs = timeOffset,
            scale = clip.scale,
            rotation = clip.rotation
        )
        val updated = clip.copy(keyframes = clip.keyframes + kf)
        updateClip(updated)
    }

    fun updateAspectRatio(aspectRatio: AspectRatio) {
        pushUndo()
        _currentProject.update { it.copy(aspectRatio = aspectRatio) }
    }

    fun updateCanvasBg(bg: CanvasBackground) {
        pushUndo()
        _currentProject.update { it.copy(canvasBg = bg) }
    }

    fun addTextOverlay(text: String, fontStyle: TextFontStyle, anim: TextAnimStyle) {
        pushUndo()
        val newText = TextOverlay(
            id = "txt_" + UUID.randomUUID().toString().take(6),
            text = text,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 3000L,
            fontStyle = fontStyle,
            animationStyle = anim,
            fontSizeSp = 24f,
            positionYRatio = 0.5f
        )
        _currentProject.update { it.copy(textOverlays = it.textOverlays + newText) }
    }

    fun deleteTextOverlay(id: String) {
        pushUndo()
        _currentProject.update { it.copy(textOverlays = it.textOverlays.filterNot { item -> item.id == id }) }
    }

    fun addStickerOverlay(emoji: String, name: String) {
        pushUndo()
        val sticker = StickerOverlay(
            id = "stk_" + UUID.randomUUID().toString().take(6),
            name = name,
            iconEmoji = emoji,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 3500L,
            positionXRatio = 0.5f,
            positionYRatio = 0.5f
        )
        _currentProject.update { it.copy(stickerOverlays = it.stickerOverlays + sticker) }
    }

    fun deleteStickerOverlay(id: String) {
        pushUndo()
        _currentProject.update { it.copy(stickerOverlays = it.stickerOverlays.filterNot { item -> item.id == id }) }
    }

    fun addPIPOverlay(title: String, sampleRes: String = "sample_racing") {
        pushUndo()
        val pip = PIPOverlay(
            id = "pip_" + UUID.randomUUID().toString().take(6),
            title = title,
            sampleIdentifier = sampleRes,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 4000L,
            positionXRatio = 0.72f,
            positionYRatio = 0.28f,
            scale = 0.38f
        )
        _currentProject.update { it.copy(pipOverlays = it.pipOverlays + pip) }
    }

    fun addAudioTrack(audioClip: AudioClip) {
        pushUndo()
        _currentProject.update { it.copy(audioTracks = it.audioTracks + audioClip) }
    }

    fun updateAudioVolume(id: String, volume: Float) {
        _currentProject.update { project ->
            val updated = project.audioTracks.map {
                if (it.id == id) it.copy(volume = volume.coerceIn(0f, 2.0f)) else it
            }
            project.copy(audioTracks = updated)
        }
    }

    fun deleteAudioTrack(id: String) {
        pushUndo()
        _currentProject.update { it.copy(audioTracks = it.audioTracks.filterNot { item -> item.id == id }) }
    }

    fun addMediaClip(title: String, sampleId: String, durationMs: Long = 5000L, uri: String? = null) {
        pushUndo()
        val newClip = MediaClip(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            title = title,
            sampleIdentifier = sampleId,
            durationMs = durationMs,
            startTrimMs = 0L,
            endTrimMs = durationMs,
            uri = uri
        )
        _currentProject.update { it.copy(clips = it.clips + newClip) }
        _selectedClipId.value = newClip.id
    }

    private fun updateClip(updatedClip: MediaClip, recordUndo: Boolean = true) {
        if (recordUndo) pushUndo()
        _currentProject.update { project ->
            val updated = project.clips.map { if (it.id == updatedClip.id) updatedClip else it }
            project.copy(clips = updated)
        }
    }

    fun loadTemplate(project: Project) {
        pushUndo()
        _currentProject.value = project
        _selectedClipId.value = project.clips.firstOrNull()?.id
        _currentPlayheadMs.value = 0L
    }

    // Export Controls
    fun openExportDialog() {
        _isExportDialogOpen.value = true
    }

    fun closeExportDialog() {
        _isExportDialogOpen.value = false
    }

    fun updateExportConfig(config: ExportConfig) {
        _exportConfig.value = config
    }

    // Quick Export Simulator ("قابلیت خروجی گرفتن سریع")
    fun startFastExport() {
        pause()
        exportJob?.cancel()
        val totalDuration = _currentProject.value.totalDurationMs
        val fps = _exportConfig.value.fps
        val totalFrames = ((totalDuration / 1000f) * fps).toInt().coerceAtLeast(30)
        val resolution = _exportConfig.value.resolution
        val isHardwareAcc = _exportConfig.value.hardwareAccelerated

        // Hardware acceleration speeds up render
        val renderDelayPerFrame = if (isHardwareAcc) 15L else 40L
        val renderSpeed = if (isHardwareAcc) 4.5f else 1.8f
        val approxSizeMb = when (resolution) {
            ExportResolution.RES_720P -> (totalDuration / 1000f) * 1.5f
            ExportResolution.RES_1080P -> (totalDuration / 1000f) * 3.2f
            ExportResolution.RES_4K -> (totalDuration / 1000f) * 8.5f
        }

        _exportState.value = ExportState(
            isExporting = true,
            progress = 0f,
            renderedFrames = 0,
            totalFrames = totalFrames,
            renderSpeedX = renderSpeed,
            remainingSeconds = (totalFrames * (renderDelayPerFrame / 1000f)).toInt(),
            fileSizeMb = approxSizeMb,
            isCompleted = false,
            exportedFileName = "ProEdit_${_currentProject.value.title.replace(" ", "_")}_${resolution.badge}.${_exportConfig.value.codec.ext}"
        )

        exportJob = viewModelScope.launch {
            var currentFrame = 0
            val batchSize = if (isHardwareAcc) 6 else 2

            while (currentFrame < totalFrames) {
                delay(renderDelayPerFrame)
                currentFrame = (currentFrame + batchSize).coerceAtMost(totalFrames)
                val progress = currentFrame.toFloat() / totalFrames
                val remainingFrames = totalFrames - currentFrame
                val remainingSec = (remainingFrames * (renderDelayPerFrame / 1000f)).toInt()

                _exportState.value = _exportState.value.copy(
                    progress = progress,
                    renderedFrames = currentFrame,
                    remainingSeconds = remainingSec
                )
            }

            _exportState.value = _exportState.value.copy(
                progress = 1.0f,
                renderedFrames = totalFrames,
                isExporting = false,
                isCompleted = true,
                remainingSeconds = 0
            )
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        _exportState.value = ExportState()
    }
}
