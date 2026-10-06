package com.example

import com.example.model.AspectRatio
import com.example.model.FilterPreset
import com.example.model.SpeedCurveType
import com.example.viewmodel.EditorViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EditorViewModelTest {

    private lateinit var viewModel: EditorViewModel

    @Before
    fun setup() {
        viewModel = EditorViewModel()
    }

    @Test
    fun testInitialProjectLoaded() {
        val project = viewModel.currentProject.value
        assertNotNull(project)
        assertTrue(project.clips.isNotEmpty())
        assertEquals(AspectRatio.RATIO_9_16, project.aspectRatio)
    }

    @Test
    fun testSpeedAndCurveUpdate() {
        viewModel.updateClipSpeed(2.0f, SpeedCurveType.FLASH)
        val clip = viewModel.selectedClip
        assertNotNull(clip)
        assertEquals(2.0f, clip!!.speed, 0.01f)
        assertEquals(SpeedCurveType.FLASH, clip.speedCurve)
    }

    @Test
    fun testFilterUpdate() {
        viewModel.updateFilter(FilterPreset.CYBERPUNK)
        val clip = viewModel.selectedClip
        assertNotNull(clip)
        assertEquals(FilterPreset.CYBERPUNK, clip!!.filter)
    }

    @Test
    fun testFastExportStart() {
        viewModel.startFastExport()
        val exportState = viewModel.exportState.value
        assertTrue(exportState.isExporting)
        assertTrue(exportState.totalFrames > 0)
    }
}
