package com.timer.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.timer.reminder.ui.navigation.AppNavGraph
import com.timer.reminder.ui.permission.PermissionCheckDialog
import com.timer.reminder.ui.theme.TimerReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 让状态栏图标变为深色（适配浅色背景）
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }

        setContent {
            TimerReminderTheme {
                var showPermissionDialog by remember { mutableStateOf(true) }

                // Show permission check dialog on first launch
                if (showPermissionDialog) {
                    PermissionCheckDialog(
                        onDismiss = { showPermissionDialog = false }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
