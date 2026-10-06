package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import com.example.model.TextAnimStyle
import com.example.model.TextFontStyle
import com.example.ui.theme.*

@Composable
fun TextStickerSheet(
    onAddText: (String, TextFontStyle, TextAnimStyle) -> Unit,
    onAddSticker: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Animated Text, 1: Stickers & Badges
    var customText by remember { mutableStateOf("PRO CINEMA") }
    var selectedFont by remember { mutableStateOf(TextFontStyle.CYBER_NEON) }
    var selectedAnim by remember { mutableStateOf(TextAnimStyle.NEON_PULSE) }

    val fontScrollState = rememberScrollState()
    val animScrollState = rememberScrollState()
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
                    imageVector = Icons.Default.TextFields,
                    contentDescription = "Text & Titles",
                    tint = StudioSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ANIMATED TITLES & STICKERS (CAPCUT)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("close_text_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Animated Text vs Stickers
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioSurfaceVariant,
            contentColor = StudioSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .height(36.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Animated Titles", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("VFX Badges & Emojis", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Text creation form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text("Overlay Title Text") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioSecondary,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("title_input_field")
                )

                // Font Typography Styles
                Text("Font Typography", color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(fontScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (f in TextFontStyle.values()) {
                        val isSelected = f == selectedFont
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFont = f },
                            label = { Text(f.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSecondary.copy(alpha = 0.25f),
                                selectedLabelColor = StudioSecondary
                            )
                        )
                    }
                }

                // Animation Styles
                Text("Motion Animation", color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(animScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (a in TextAnimStyle.values()) {
                        val isSelected = a == selectedAnim
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAnim = a },
                            label = { Text(a.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = StudioPrimary
                            )
                        )
                    }
                }

                // Add to Timeline Button
                Button(
                    onClick = {
                        if (customText.isNotBlank()) {
                            onAddText(customText, selectedFont, selectedAnim)
                            onClose()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioSecondary,
                        contentColor = StudioTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("insert_title_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Title to Timeline", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Badges & Stickers Grid
            val badges = listOf(
                Pair("🔴 REC", "Live Record"),
                Pair("⚡ VFX", "Power Effect"),
                Pair("🔥 FIRE", "Trending Viral"),
                Pair("💯 100", "Top Quality"),
                Pair("🎬 ACTION", "Cinema Cut"),
                Pair("💎 4K", "Ultra HD"),
                Pair("✨ GLOW", "Aesthetic Glow"),
                Pair("🛸 CYBER", "Future Sci-Fi")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap any badge to insert at current playhead position:",
                    color = StudioTextSecondary,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 4) {
                        val item = badges[i]
                        Surface(
                            onClick = {
                                onAddSticker(item.first, item.second)
                                onClose()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(item.first, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(item.second, color = StudioTextMuted, fontSize = 8.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 4 until 8) {
                        val item = badges[i]
                        Surface(
                            onClick = {
                                onAddSticker(item.first, item.second)
                                onClose()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(item.first, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(item.second, color = StudioTextMuted, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
