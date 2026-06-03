package com.techmomentum.wc2026.data.model

fun Player.isGoalkeeper(): Boolean = position.equals("Goalkeeper", ignoreCase = true)

fun Player.hasCompleteRatings(): Boolean = ratings.isCompleteFor(position)
