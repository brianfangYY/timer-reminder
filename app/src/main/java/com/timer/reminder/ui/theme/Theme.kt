package com.timer.reminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Tomato,
    onPrimary = Color.White,
    primaryContainer = TomatoLight,
    onPrimaryContainer = TomatoDark,
    secondary = ReminderBlue,
    onSecondary = Color.White,
    secondaryContainer = ReminderBlueLight,
    tertiary = TaskGreen,
    onTertiary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.White
)

@Composable
fun TimerReminderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = TimerReminderTypography,
        content = content
    )
}
