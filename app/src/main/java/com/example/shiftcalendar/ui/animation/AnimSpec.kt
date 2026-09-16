package com.example.shiftcalendar.ui.animation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

@Composable
fun animSpec(
    durationMillis: Int = 300,
    fallback: FiniteAnimationSpec<Float> = tween(durationMillis)
): FiniteAnimationSpec<Float> {
    val s = LocalAnimationSettings.current
    return if (s.animationsEnabled && s.cardAnimations) fallback else snap()
}
