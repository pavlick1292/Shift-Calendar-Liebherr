package com.example.shiftcalendar.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable

@Composable
fun SmartAnimatedVisibility(
    visible: Boolean,
    enter: EnterTransition = fadeIn(tween(250)) + expandVertically(tween(250)),
    exit: ExitTransition = fadeOut(tween(200)) + shrinkVertically(tween(200)),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val s = LocalAnimationSettings.current
    AnimatedVisibility(
        visible = visible,
        enter = if (s.animationsEnabled) enter else EnterTransition.None,
        exit = if (s.animationsEnabled) exit else ExitTransition.None,
        content = content
    )
}
