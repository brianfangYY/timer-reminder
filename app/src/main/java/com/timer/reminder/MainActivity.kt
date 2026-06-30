package com.timer.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.timer.reminder.ui.navigation.AppNavGraph
import com.timer.reminder.ui.permission.PermissionCheckDialog
import com.timer.reminder.ui.theme.TimerReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
