package com.example.data

import com.example.model.*
import kotlin.random.Random

object SampleMediaRepository {

    fun generateWaveform(count: Int = 40): List<Float> {
        val random = Random(42)
        return List(count) { 0.2f + random.nextFloat() * 0.8f }
    }

    val defaultDemoClips = listOf(
        MediaClip(
            id = "clip_neon_tokyo",
            title = "Tokyo Neon Walk",
            sampleIdentifier = "sample_urban",
            durationMs = 5000L,
            startTrimMs = 0L,
            endTrimMs = 5000L,
            filter = FilterPreset.TEAL_ORANGE,
            lumetri = LumetriAdjustments(exposure = 0.1f, contrast = 1.2f, saturation = 1.3f, vignette = 0.25f),
            transitionIn = TransitionType.NONE
        ),
        MediaClip(
            id = "clip_sunset_drone",
            title = "Coastline Sunset 4K",
            sampleIdentifier = "sample_sunset",
            durationMs = 6000L,
            startTrimMs = 0L,
            endTrimMs = 6000L,
            filter = FilterPreset.SUNSET_GLOW,
            lumetri = LumetriAdjustments(temperature = 0.4f, saturation = 1.25f, contrast = 1.15f),
            transitionIn = TransitionType.DISSOLVE,
            transitionDurationMs = 700L
        ),
        MediaClip(
            id = "clip_cyber_drift",
            title = "Night Drift Racing",
            sampleIdentifier = "sample_racing",
            durationMs = 4500L,
            startTrimMs = 0L,
            endTrimMs = 4500L,
            filter = FilterPreset.CYBERPUNK,
            speedCurve = SpeedCurveType.MONTAGE,
            speed = 1.2f,
            transitionIn = TransitionType.GLITCH,
            transitionDurationMs = 500L
        ),
        MediaClip(
            id = "clip_coffee_aesthetic",
            title = "Aesthetic Espresso",
            sampleIdentifier = "sample_coffee",
            durationMs = 4000L,
            startTrimMs = 0L,
            endTrimMs = 4000L,
            filter = FilterPreset.VINTAGE_90S,
            transitionIn = TransitionType.FLASH_WHITE,
            transitionDurationMs = 400L
        )
    )

    val sampleAudioLibrary = listOf(
        AudioClip(
            id = "audio_phonk_pulse",
            title = "Midnight Phonk Pulse",
            artist = "ProEdit Studio Beats",
            type = AudioType.BGM,
            startOffsetMs = 0L,
            durationMs = 19500L,
            volume = 0.85f,
            waveformPoints = generateWaveform(50),
            beatMarkers = listOf(800L, 1600L, 2400L, 3200L, 4800L, 6400L, 8000L, 9600L, 11200L, 12800L, 14400L, 16000L)
        ),
        AudioClip(
            id = "audio_synth_wave",
            title = "Retrowave Horizon 80s",
            artist = "Neon Dreamer",
            type = AudioType.BGM,
            startOffsetMs = 0L,
            durationMs = 15000L,
            volume = 0.75f,
            waveformPoints = generateWaveform(45),
            beatMarkers = listOf(1000L, 2000L, 3000L, 4000L, 6000L, 8000L, 10000L, 12000L)
        ),
        AudioClip(
            id = "sfx_cinematic_whoosh",
            title = "Cinematic Hyper Whoosh",
            artist = "CapCut Trending SFX",
            type = AudioType.SFX,
            startOffsetMs = 4800L,
            durationMs = 900L,
            volume = 1.0f,
            waveformPoints = generateWaveform(15)
        ),
        AudioClip(
            id = "sfx_glitch_hit",
            title = "Digital Glitch Stutter",
            artist = "Premiere VFX Pack",
            type = AudioType.SFX,
            startOffsetMs = 10200L,
            durationMs = 1100L,
            volume = 0.95f,
            waveformPoints = generateWaveform(18)
        )
    )

    val sampleTextPresets = listOf(
        TextOverlay(
            id = "text_title_1",
            text = "PRO EDIT • 4K",
            startOffsetMs = 500L,
            durationMs = 3500L,
            fontStyle = TextFontStyle.CYBER_NEON,
            animationStyle = TextAnimStyle.NEON_PULSE,
            fontSizeSp = 28f,
            positionYRatio = 0.22f
        ),
        TextOverlay(
            id = "text_caption_2",
            text = "Next-Gen Mobile Studio",
            startOffsetMs = 4200L,
            durationMs = 4000L,
            fontStyle = TextFontStyle.BOLD_SANS,
            animationStyle = TextAnimStyle.TYPEWRITER,
            fontSizeSp = 22f,
            positionYRatio = 0.78f
        )
    )

    val sampleStickers = listOf(
        StickerOverlay(
            id = "stk_rec",
            name = "REC Indicator",
            iconEmoji = "🔴 REC",
            startOffsetMs = 0L,
            durationMs = 19500L,
            positionXRatio = 0.2f,
            positionYRatio = 0.12f,
            scale = 1.0f
        ),
        StickerOverlay(
            id = "stk_vfx",
            name = "Glitch Spark",
            iconEmoji = "⚡ VFX",
            startOffsetMs = 5000L,
            durationMs = 3000L,
            positionXRatio = 0.8f,
            positionYRatio = 0.15f,
            scale = 1.1f
        )
    )

    val samplePIPOverlays = listOf(
        PIPOverlay(
            id = "pip_reaction",
            title = "Reaction Cam",
            sampleIdentifier = "sample_face",
            startOffsetMs = 2000L,
            durationMs = 5000L,
            positionXRatio = 0.76f,
            positionYRatio = 0.24f,
            scale = 0.36f
        )
    )

    fun createInitialProject(): Project {
        return Project(
            id = "proj_master_1",
            title = "Neon Drift Story",
            aspectRatio = AspectRatio.RATIO_9_16,
            canvasBg = CanvasBackground.BLUR_LIGHT,
            fps = 60,
            clips = defaultDemoClips,
            audioTracks = sampleAudioLibrary,
            textOverlays = sampleTextPresets,
            stickerOverlays = sampleStickers,
            pipOverlays = samplePIPOverlays
        )
    }

    val sampleProjectTemplates = listOf(
        Project(
            id = "tpl_reels",
            title = "Viral TikTok & Reels 9:16",
            aspectRatio = AspectRatio.RATIO_9_16,
            canvasBg = CanvasBackground.GRADIENT_CYAN_PURPLE,
            fps = 60,
            clips = defaultDemoClips.take(3),
            audioTracks = sampleAudioLibrary.take(2),
            textOverlays = sampleTextPresets,
            stickerOverlays = sampleStickers
        ),
        Project(
            id = "tpl_youtube",
            title = "Cinematic 4K Vlog 16:9",
            aspectRatio = AspectRatio.RATIO_16_9,
            canvasBg = CanvasBackground.SOLID_BLACK,
            fps = 60,
            clips = defaultDemoClips,
            audioTracks = sampleAudioLibrary,
            textOverlays = listOf(sampleTextPresets.first())
        ),
        Project(
            id = "tpl_instagram",
            title = "Clean Aesthetic Post 1:1",
            aspectRatio = AspectRatio.RATIO_1_1,
            canvasBg = CanvasBackground.BLUR_HEAVY,
            fps = 30,
            clips = defaultDemoClips.take(2),
            audioTracks = listOf(sampleAudioLibrary.first())
        ),
        Project(
            id = "tpl_cinema",
            title = "Epic CinemaScope 21:9",
            aspectRatio = AspectRatio.RATIO_21_9,
            canvasBg = CanvasBackground.SOLID_BLACK,
            fps = 24,
            clips = defaultDemoClips,
            audioTracks = sampleAudioLibrary
        )
    )
}
