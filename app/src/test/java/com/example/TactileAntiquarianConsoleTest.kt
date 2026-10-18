package com.example

import com.example.ui.components.DEFAULT_INSTRUMENTS
import com.example.ui.theme.LanternLightingMode
import com.example.ui.theme.LanternLightingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TactileAntiquarianConsoleTest {

    @Test
    fun testLanternLightingModeTransitions() {
        var mode = LanternLightingMode.KEROSENE
        assertEquals("Kerosene Lamp", mode.displayName)

        mode = mode.next()
        assertEquals(LanternLightingMode.TORCH, mode)
        assertEquals("Karst Torch", mode.displayName)

        mode = mode.next()
        assertEquals(LanternLightingMode.BLACKLIGHT_UV, mode)
        assertEquals("Blacklight UV", mode.displayName)

        mode = mode.next()
        assertEquals(LanternLightingMode.KEROSENE, mode)
    }

    @Test
    fun testLanternLightingStateCalculations() {
        val keroseneState = LanternLightingState(mode = LanternLightingMode.KEROSENE)
        assertFalse("Kerosene mode should not have UV active", keroseneState.isUvActive)
        assertFalse("Secret ink should be concealed under kerosene", keroseneState.secretInkVisible)
        assertNotNull(keroseneState.ambientGlowColor)
        assertNotNull(keroseneState.paperSurfaceColor)

        val uvState = LanternLightingState(mode = LanternLightingMode.BLACKLIGHT_UV)
        assertTrue("Blacklight mode must have UV active", uvState.isUvActive)
        assertTrue("Secret ink must be revealed under UV", uvState.secretInkVisible)
        assertEquals(LanternLightingMode.BLACKLIGHT_UV.glowColor, uvState.mode.glowColor)
    }

    @Test
    fun testTactileInstrumentDockTokens() {
        assertEquals("Must provide 6 expedition instrument tokens", 6, DEFAULT_INSTRUMENTS.size)

        val indices = DEFAULT_INSTRUMENTS.map { it.index }
        assertEquals(listOf(0, 1, 2, 3, 4, 5), indices)

        val titles = DEFAULT_INSTRUMENTS.map { it.title }
        assertTrue(titles.contains("Desk"))
        assertTrue(titles.contains("Radar"))
        assertTrue(titles.contains("Tour"))
        assertTrue(titles.contains("Library"))
        assertTrue(titles.contains("Relics"))
        assertTrue(titles.contains("Pack"))

        DEFAULT_INSTRUMENTS.forEach { token ->
            assertNotNull(token.icon)
            assertFalse(token.testTag.isBlank())
        }
    }
}
