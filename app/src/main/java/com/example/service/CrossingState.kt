package com.example.service

/**
 * State machine for proximity and corridor crossing detection.
 * Prevents alert spamming and manages transitions:
 * FAR -> APPROACHING -> NEAR -> CROSSED / INSIDE -> LEAVING -> COOLDOWN
 */
enum class CrossingState {
    FAR,
    APPROACHING,
    NEAR,
    CROSSED,
    INSIDE,
    LEAVING,
    COOLDOWN
}
