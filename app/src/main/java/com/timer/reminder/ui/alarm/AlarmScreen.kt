package com.timer.reminder.ui.alarm

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
import com.timer.reminder.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var alarmLabel by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("闹钟", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val now = Calendar.getInstance()
                        selectedHour = now.get(Calendar.HOUR_OF_DAY)
                        selectedMinute = now.get(Calendar.MINUTE)
                        alarmLabel = ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加闹钟")
                    }
                }
            )
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.AlarmOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextHint
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("暂无闹钟", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
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
                items(alarms, key = { it.id }) { alarm ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alarm.isEnabled) CardBackground else Surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Alarm,
                                contentDescription = null,
                                tint = if (alarm.isEnabled) AlarmGold else TextHint,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        alarm.hourOfDay,
                                        alarm.minute
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alarm.isEnabled) TextPrimary else TextHint
                                )
                                if (alarm.label.isNotEmpty()) {
                                    Text(
                                        text = alarm.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                if (alarm.repeatDays.isNotEmpty()) {
                                    val days = alarm.repeatDays.split(",")
                                        .map { dayNumberToName(it.toIntOrNull() ?: 0) }
                                    Text(
                                        text = days.joinToString(" "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AlarmGold
                                    )
                                }
                            }
                            Switch(
                                checked = alarm.isEnabled,
                                onCheckedChange = { viewModel.toggleAlarm(alarm) }
                            )
                            IconButton(onClick = { viewModel.deleteAlarm(alarm.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加闹钟") },
            text = {
                Column {
                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text("标签（可选）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    selectedHour = hour
                                    selectedMinute = minute
                                },
                                selectedHour, selectedMinute, true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            String.format(
                                Locale.getDefault(),
                                "选择时间: %02d:%02d",
                                selectedHour, selectedMinute
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addAlarm(selectedHour, selectedMinute, alarmLabel)
                    showAddDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}

private fun dayNumberToName(day: Int): String {
    return when (day) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> ""
    }
}
