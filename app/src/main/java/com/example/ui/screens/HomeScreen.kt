package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleMediaRepository
import com.example.model.Project
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    recentProjects: List<Project>,
    onOpenProject: (Project) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templateScrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg),
        containerColor = StudioDarkBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewProject,
                containerColor = StudioPrimary,
                contentColor = StudioDarkBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_project_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Project")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEW PROJECT (پروژه جدید)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Pro Studio Hero Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StudioHeroBanner(onNewProject = onNewProject)
            }

            // 2. Pro Features Badge Trio
            item {
                StudioPowersRow()
            }

            // 3. Project Templates (CapCut & InShot style presets)
            item {
                Text(
                    text = "PRO TEMPLATES (قالب‌های آماده)",
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(templateScrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (tpl in SampleMediaRepository.sampleProjectTemplates) {
                        Surface(
                            onClick = { onOpenProject(tpl) },
                            shape = RoundedCornerShape(12.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier
                                .width(150.dp)
                                .height(100.dp)
                                .testTag("template_${tpl.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(StudioSurfaceVariant, Color(0xFF192231))
                                        )
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = StudioPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tpl.aspectRatio.label,
                                            color = StudioPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = tpl.title,
                                            color = StudioTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = "${tpl.fps} FPS • ${tpl.clips.size} clips",
                                            color = StudioTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Recent Projects Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT PROJECTS (پروژه‌های اخیر)",
                        color = StudioTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${recentProjects.size} Projects",
                        color = StudioTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            items(recentProjects) { project ->
                RecentProjectCard(
                    project = project,
                    onClick = { onOpenProject(project) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StudioHeroBanner(onNewProject: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF132F3E), StudioSurface),
                        radius = 600f
                    )
                )
                .padding(18.dp)
        ) {
            Column {
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
                                .size(36.dp)
                                .background(StudioPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "ProEdit",
                                tint = StudioDarkBg,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ProEdit",
                                color = StudioTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "ULTIMATE VIDEO STUDIO",
                                color = StudioPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Surface(
                        color = StudioAccentGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioAccentGreen.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⚡ FAST EXPORT",
                            color = StudioAccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تدوین حرفه‌ای ویدیو با تمام قابلیت‌های پریمیر پرو، کپ‌کات و این‌شات در یک تم دارک مدرن و خروجی فوق‌سریع.",
                    color = StudioTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioPrimary,
                        contentColor = StudioDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.VideoCameraBack, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OPEN EDITING STUDIO (شروع تدوین)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun StudioPowersRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PowerBadge(
            title = "Premiere Pro",
            desc = "Lumetri & Keyframes",
            accent = StudioSecondary,
            modifier = Modifier.weight(1f)
        )
        PowerBadge(
            title = "CapCut",
            desc = "Speed Curves & VFX",
            accent = StudioPrimary,
            modifier = Modifier.weight(1f)
        )
        PowerBadge(
            title = "InShot",
            desc = "Canvas & Collage",
            accent = StudioTertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PowerBadge(
    title: String,
    desc: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = modifier.height(56.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            Text(desc, color = StudioTextMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun RecentProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Video thumbnail box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2027), Color(0xFF2C5364))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieCreation,
                        contentDescription = "Project",
                        tint = StudioPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = project.title,
                        color = StudioTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = StudioSurfaceVariant,
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = project.aspectRatio.label,
                                color = StudioPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "${project.totalDurationMs / 1000}s • ${project.clips.size} clips • ${project.fps} FPS",
                            color = StudioTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = StudioPrimary
                )
            }
        }
    }
}
