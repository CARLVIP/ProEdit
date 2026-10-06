package com.example.model

enum class AspectRatio(val label: String, val subLabel: String, val ratio: Float, val iconName: String) {
    RATIO_16_9("16:9", "YouTube / TV", 16f / 9f, "play_arrow"),
    RATIO_9_16("9:16", "Reels / TikTok", 9f / 16f, "smartphone"),
    RATIO_1_1("1:1", "Square / Insta", 1f, "crop_square"),
    RATIO_4_5("4:5", "Feed Portrait", 4f / 5f, "view_column"),
    RATIO_21_9("21:9", "CinemaScope", 21f / 9f, "movie")
}

enum class CanvasBackground(val label: String) {
    BLUR_LIGHT("Soft Blur"),
    BLUR_HEAVY("Deep Blur"),
    GRADIENT_CYAN_PURPLE("Neon Cyber"),
    GRADIENT_SUNSET("Sunset Vibe"),
    SOLID_BLACK("Pure Black"),
    SOLID_DARK_SLATE("Studio Slate")
}

data class LumetriAdjustments(
    val exposure: Float = 0f,        // -1f..1f
    val contrast: Float = 1f,        // 0.5f..2.0f
    val highlights: Float = 0f,      // -1f..1f
    val shadows: Float = 0f,         // -1f..1f
    val temperature: Float = 0f,     // -1f..1f (Warm / Cool)
    val tint: Float = 0f,            // -1f..1f (Green / Magenta)
    val saturation: Float = 1f,      // 0f..2.5f
    val vignette: Float = 0f         // 0f..1f
)

enum class FilterPreset(val displayName: String, val desc: String) {
    NORMAL("Normal", "Natural clean profile"),
    TEAL_ORANGE("Teal & Orange", "Hollywood cinematic contrast"),
    CYBERPUNK("Cyber Neon", "Electric blue & hyper magenta"),
    FILM_NOIR("Film Noir", "High contrast black and white"),
    VINTAGE_90S("Vintage 90s", "Warm nostalgic VHS film grain"),
    SUNSET_GLOW("Sunset Glow", "Warm golden hour peach tone"),
    EMERALD_NIGHT("Emerald Night", "Moody dark green mystery"),
    DRAMATIC("Dramatic", "Punchy shadows and high punch")
}

enum class SpeedCurveType(val label: String, val speedFactor: Float, val desc: String) {
    STANDARD("Standard (1.0x)", 1.0f, "Constant regular velocity"),
    MONTAGE("Montage Ramp", 1.4f, "Slow start, turbo cut, smooth landing"),
    HERO("Hero Impact", 0.6f, "Fast approach into ultra slow-motion punch"),
    FLASH("Flash Pulse", 2.5f, "Rapid burst transition"),
    BULLET("Bullet Time", 0.35f, "Cinematic 120fps slow freeze feel")
}

enum class TransitionType(val label: String, val icon: String) {
    NONE("Direct Cut", "horizontal_rule"),
    DISSOLVE("Cross Dissolve", "blur_on"),
    WIPE_RIGHT("Wipe Right", "swipe_right"),
    ZOOM_IN("Zoom In", "zoom_in"),
    ZOOM_OUT("Zoom Out", "zoom_out"),
    GLITCH("Glitch VFX", "flash_on"),
    FLASH_WHITE("Flash White", "brightness_high"),
    FILM_ROLL("Film Roll", "movie_filter"),
    WHIP_PAN("Whip Pan", "fast_forward")
}

data class Keyframe(
    val id: String,
    val timeMs: Long,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

data class MediaClip(
    val id: String,
    val title: String,
    val uri: String? = null,
    val sampleIdentifier: String = "sample_urban",
    val durationMs: Long = 6000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 6000L,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val speedCurve: SpeedCurveType = SpeedCurveType.STANDARD,
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val isFrozen: Boolean = false,
    val filter: FilterPreset = FilterPreset.NORMAL,
    val lumetri: LumetriAdjustments = LumetriAdjustments(),
    val chromaCutout: Boolean = false,
    val chromaTolerance: Float = 0.4f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val keyframes: List<Keyframe> = emptyList(),
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 600L
) {
    val effectiveDurationMs: Long
        get() {
            val base = (endTrimMs - startTrimMs).coerceAtLeast(500L)
            return (base / speed).toLong()
        }
}

enum class AudioType { BGM, SFX, RECORDING }

data class AudioClip(
    val id: String,
    val title: String,
    val artist: String,
    val type: AudioType = AudioType.BGM,
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 12000L,
    val volume: Float = 0.8f,
    val fadeInSec: Float = 0.5f,
    val fadeOutSec: Float = 0.5f,
    val pitchShift: Float = 0f,
    val noiseReduction: Boolean = false,
    val waveformPoints: List<Float> = emptyList(),
    val beatMarkers: List<Long> = emptyList()
)

enum class TextFontStyle(val label: String) {
    CYBER_NEON("Cyber Neon"),
    BOLD_SANS("Impact Sans"),
    CINEMATIC_SERIF("Cinema Serif"),
    TYPEWRITER("Typewriter Monospace"),
    RETRO_GLITCH("Retro Glitch"),
    MINIMAL_CLEAN("Minimal Studio")
}

enum class TextAnimStyle(val label: String) {
    NONE("Static"),
    TYPEWRITER("Typewriter"),
    NEON_PULSE("Neon Pulse"),
    GLITCH_FLICKER("Glitch Flicker"),
    BOUNCE_IN("Pop & Bounce"),
    FADE("Smooth Fade")
}

data class TextOverlay(
    val id: String,
    val text: String,
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 3000L,
    val fontStyle: TextFontStyle = TextFontStyle.CYBER_NEON,
    val textColorHex: Long = 0xFFFFFFFF,
    val animationStyle: TextAnimStyle = TextAnimStyle.NEON_PULSE,
    val fontSizeSp: Float = 24f,
    val positionYRatio: Float = 0.75f,
    val hasBackgroundPlate: Boolean = true
)

data class StickerOverlay(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 3000L,
    val positionXRatio: Float = 0.5f,
    val positionYRatio: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

data class PIPOverlay(
    val id: String,
    val title: String,
    val sampleIdentifier: String = "sample_gaming",
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 4000L,
    val positionXRatio: Float = 0.72f,
    val positionYRatio: Float = 0.28f,
    val scale: Float = 0.38f,
    val opacity: Float = 0.95f,
    val cornerRadius: Float = 16f
)

data class Project(
    val id: String,
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val canvasBg: CanvasBackground = CanvasBackground.BLUR_LIGHT,
    val fps: Int = 30,
    val clips: List<MediaClip> = emptyList(),
    val audioTracks: List<AudioClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickerOverlays: List<StickerOverlay> = emptyList(),
    val pipOverlays: List<PIPOverlay> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(1000L)
}

enum class ExportResolution(val label: String, val width: Int, val height: Int, val badge: String) {
    RES_720P("720p HD", 1280, 720, "FAST"),
    RES_1080P("1080p FHD", 1920, 1080, "PRO"),
    RES_4K("4K Ultra HD", 3840, 2160, "MASTER")
}

enum class ExportBitrate(val label: String, val mbps: String) {
    STANDARD("Standard (Fast Web)", "12 Mbps"),
    HIGH("High Quality (Studio)", "24 Mbps"),
    ULTRA("Ultra Master (Lossless)", "50 Mbps")
}

enum class ExportCodec(val label: String, val ext: String) {
    H264("H.264 (AVC - Universal)", "mp4"),
    HEVC("H.265 (HEVC - High Efficiency)", "mp4"),
    PRORES("ProRes 422 Proxy (Clean Master)", "mov")
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: Int = 60,
    val bitrate: ExportBitrate = ExportBitrate.HIGH,
    val codec: ExportCodec = ExportCodec.H264,
    val hardwareAccelerated: Boolean = true
)

data class ExportState(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val renderedFrames: Int = 0,
    val totalFrames: Int = 0,
    val renderSpeedX: Float = 0f,
    val remainingSeconds: Int = 0,
    val fileSizeMb: Float = 0f,
    val isCompleted: Boolean = false,
    val exportedFileName: String = ""
)
