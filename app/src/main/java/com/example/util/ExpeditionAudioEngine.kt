package com.example.util

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.StateFlow

/**
 * Unified Expedition Audio & Haptics Engine.
 * Consolidates one-shot procedural acoustics, continuous ambient soundscapes,
 * and subterranean hydrophone sonar telemetry into a high-performance, single-instance facade.
 */
class ExpeditionAudioEngine(
    val context: Context,
    val adventure: AdventureSoundEngine = AdventureSoundEngine(context),
    val ambient: AmbientSoundEngine = AmbientSoundEngine(context)
) {
    var isSoundEnabled: Boolean
        get() = adventure.isSoundEnabled
        set(value) {
            adventure.isSoundEnabled = value
            if (!value) {
                ambient.stopAmbient()
            }
        }

    val isAmbientPlaying: StateFlow<Boolean> get() = ambient.isAmbientPlaying
    val ambientTheme: StateFlow<String> get() = ambient.ambientTheme

    // --- Subterranean Karst Sounding & Winch Operations ---
    fun playWinchRatchetTick() = adventure.playWinchRatchetTick()
    fun playCableCreak() = adventure.playCableCreak()
    fun playHydrophoneSplash() = adventure.playHydrophoneSplash()
    fun playCavernSonarPulse(depthRatio: Float = 0.5f) = adventure.playCavernSonarPulse(depthRatio)

    fun startSubterraneanHydrophoneAudio() {
        ambient.startAmbient("CAVERN")
    }

    fun stopSubterraneanHydrophoneAudio() {
        if (ambient.ambientTheme.value == "CAVERN") {
            ambient.stopAmbient()
        }
    }

    // --- Tactile Antiquarian Operations ---
    fun playLanternWickDial() = adventure.playLanternWickDial()
    fun playPaperFold() = adventure.playPaperFold()
    fun playPaperUnfold() = adventure.playPaperUnfold()
    fun playBrassToggleSwitch() = adventure.playBrassToggleSwitch()
    fun playWaxStampImpact() = adventure.playWaxStampImpact()
    fun playWaxSealBreak() = adventure.playWaxSealBreak()
    fun playParchmentRustle() = adventure.playParchmentRustle()
    fun playLeatherUnclasp() = adventure.playLeatherUnclasp()
    fun playTactileClick() = adventure.playTactileClick()
    fun playSonarPing() = adventure.playSonarPing()
    fun playCavernDrip() = adventure.playCavernDrip()
    fun playCompassClick() = adventure.playCompassClick()
    fun playCameraShutter() = adventure.playCameraShutter()
    fun playCharcoalScratch(intensity: Float = 1.0f) = adventure.playCharcoalScratch(intensity)
    fun playTapeDeckLatch() = adventure.playTapeDeckLatch()
    fun playRadioStaticBurst() = adventure.playRadioStaticBurst()

    // --- Continuous Ambience ---
    fun startAmbient(theme: String) = ambient.startAmbient(theme)
    fun stopAmbient() = ambient.stopAmbient()
    fun setDucked(ducked: Boolean) = ambient.setDucked(ducked)
}

val LocalExpeditionAudio = compositionLocalOf<ExpeditionAudioEngine?> { null }
