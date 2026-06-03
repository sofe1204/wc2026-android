package com.techmomentum.wc2026.data.model

/**
 * Six outfield attributes plus six goalkeeper attributes (use position to decide which set to show).
 */
data class PlayerRatings(
    val overall: Int = 0,
    val pace: Int = 0,
    val shooting: Int = 0,
    val passing: Int = 0,
    val dribbling: Int = 0,
    val defending: Int = 0,
    val physical: Int = 0,
    val diving: Int = 0,
    val handling: Int = 0,
    val kicking: Int = 0,
    val reflexes: Int = 0,
    val speed: Int = 0,
    val positioning: Int = 0,
) {
    fun isCompleteFor(position: String): Boolean {
        if (overall <= 0) return false
        return if (position.equals("Goalkeeper", ignoreCase = true)) {
            listOf(diving, handling, kicking, reflexes, speed, positioning).all { it > 0 }
        } else {
            listOf(pace, shooting, passing, dribbling, defending, physical).all { it > 0 }
        }
    }
}
