package com.timer.reminder.ui.reminder

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.reminder.ui.common.TaskSelectionDialog
import com.timer.reminder.ui.theme.*
import com.timer.reminder.util.TimeUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedTimeMillis by remember { mutableStateOf(System.currentTimeMillis() + 3600000) }
    var linkedTaskId by remember { mutableStateOf<Long?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        titleInput = ""
                        descriptionInput = ""
                        linkedTaskId = null
                        selectedTimeMillis = System.currentTimeMillis() + 3600000
                        showAddDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加提醒")
                    }
                }
            )
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextHint
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("暂无提醒", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    // Find linked task name
                    val linkedTask = pendingTasks.find { it.id == reminder.linkedTaskId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = ReminderBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (reminder.description.isNotEmpty()) {
                                    Text(
                                        text = reminder.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                if (linkedTask != null) {
                                    Text(
                                        text = "📎 ${linkedTask.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TaskGreen
                                    )
                                }
                                Text(
                                    text = TimeUtils.formatDateTime(reminder.triggerAtMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ReminderBlue
                                )
                            }
                            IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "删除",
                                    tint = Error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add reminder dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加提醒") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("描述（可选）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            val now = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    cal.set(Calendar.HOUR_OF_DAY, hour)
                                    cal.set(Calendar.MINUTE, minute)
                                    cal.set(Calendar.SECOND, 0)
                                    if (cal.before(now)) cal.add(Calendar.DAY_OF_YEAR, 1)
                                    selectedTimeMillis = cal.timeInMillis
                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择时间: ${TimeUtils.formatTime(selectedTimeMillis)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    // Task binding button
                    OutlinedButton(
                        onClick = { showTaskDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val linkedTask = pendingTasks.find { it.id == linkedTaskId }
                        if (linkedTask != null) {
                            Text("📎 绑定任务: ${linkedTask.title}")
                        } else {
                            Text("绑定任务（可选）")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            viewModel.addReminder(titleInput, descriptionInput, selectedTimeMillis, linkedTaskId)
                            showAddDialog = false
                            titleInput = ""
                            descriptionInput = ""
                        }
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }

    // Task selection dialog
    if (showTaskDialog) {
        TaskSelectionDialog(
            tasks = pendingTasks,
            selectedTaskId = linkedTaskId,
            onSelect = { id -> linkedTaskId = id },
            onDismiss = { showTaskDialog = false }
        )
    }
}
