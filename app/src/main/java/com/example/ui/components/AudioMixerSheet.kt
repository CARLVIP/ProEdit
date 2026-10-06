package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleMediaRepository
import com.example.model.AudioClip
import com.example.model.AudioType
import com.example.ui.theme.*

@Composable
fun AudioMixerSheet(
    audioTracks: List<AudioClip>,
    onVolumeChange: (String, Float) -> Unit,
    onAddTrack: (AudioClip) -> Unit,
    onDeleteTrack: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Active Tracks Mixer, 1: Add Music & SFX
    val scrollState = rememberScrollState()

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
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Audio Mixer",
                    tint = StudioAccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AUDIO & SFX STUDIO (BEAT SYNC)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_audio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioSurfaceVariant,
            contentColor = StudioAccentGreen,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .height(36.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Mixer", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sound & SFX Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Active tracks volume sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (audioTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No audio tracks added yet", color = StudioTextMuted, fontSize = 12.sp)
                    }
                } else {
                    for (track in audioTracks) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = track.title,
                                            color = StudioTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${track.type.name} • ${track.artist}",
                                            color = StudioAccentGreen,
                                            fontSize = 9.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTrack(track.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Track",
                                            tint = StudioPlayheadRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = StudioTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Slider(
                                        value = track.volume,
                                        onValueChange = { onVolumeChange(track.id, it) },
                                        valueRange = 0f..2.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = StudioAccentGreen,
                                            activeTrackColor = StudioAccentGreen,
                                            inactiveTrackColor = StudioCardBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                    )
                                    Text(
                                        text = "${(track.volume * 100).toInt()}%",
                                        color = StudioAccentGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Sound Library (Add BGM / SFX)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (libAudio in SampleMediaRepository.sampleAudioLibrary) {
                    val alreadyAdded = audioTracks.any { it.title == libAudio.title }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = libAudio.title,
                                    color = StudioTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${libAudio.type} • ${libAudio.artist}",
                                    color = StudioTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                            Button(
                                onClick = {
                                    onAddTrack(
                                        libAudio.copy(
                                            id = "aud_" + java.util.UUID.randomUUID().toString().take(6)
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (alreadyAdded) StudioCardBorder else StudioAccentGreen,
                                    contentColor = if (alreadyAdded) StudioTextSecondary else StudioDarkBg
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (alreadyAdded) "Add More" else "Add",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
