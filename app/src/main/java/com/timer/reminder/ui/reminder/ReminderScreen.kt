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
import com.timer.reminder.data.local.entity.ReminderEntity
import com.timer.reminder.ui.common.TaskSelectionDialog
import com.timer.reminder.ui.theme.*
import com.timer.reminder.util.TimeUtils
import java.util.*

/** Repeat type options shown in the add dialog */
private val REPEAT_OPTIONS = listOf(
    "none"    to "仅一次",
    "daily"   to "每天",
    "workdays" to "工作日 (周一~周五)",
    "weekly"  to "每周",
    "monthly" to "每月"
)

/** Day-of-week names (Calendar.DAY_OF_WEEK value -> Chinese name) */
private val DOW_NAMES = mapOf(
    Calendar.MONDAY    to "周一",
    Calendar.TUESDAY   to "周二",
    Calendar.WEDNESDAY to "周三",
    Calendar.THURSDAY  to "周四",
    Calendar.FRIDAY    to "周五",
    Calendar.SATURDAY  to "周六",
    Calendar.SUNDAY    to "周日"
)

/** All 7 day-of-week values in order (Mon..Sun) */
private val ALL_DOW = listOf(
    Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
)

/** Common day-of-month options for monthly repeat */
private val DOM_OPTIONS = listOf(1, 5, 10, 15, 20, 25, 28)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Add-dialog state
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedTimeMillis by remember { mutableStateOf(System.currentTimeMillis() + 3600000) }
    var linkedTaskId by remember { mutableStateOf<Long?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    // Repeat state
    var repeatType by remember { mutableStateOf("none") }
    var selectedDaysOfWeek by remember { mutableStateOf(setOf<Int>()) }
    var selectedDaysOfMonth by remember { mutableStateOf(setOf(1, 15)) }
    var repeatTypeExpanded by remember { mutableStateOf(false) }

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
                        repeatType = "none"
                        selectedDaysOfWeek = emptySet()
                        selectedDaysOfMonth = setOf(1, 15)
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
                    val linkedTask = pendingTasks.find { it.id == reminder.linkedTaskId }
                    val repeatLabel = reminder.getRepeatLabel()

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
                                if (repeatLabel != "仅一次") {
                                    Text(
                                        text = "🔄 $repeatLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Tomato
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

    // ── Add reminder dialog ──
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加提醒") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("提醒标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Description
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("描述（可选）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Time picker
                    OutlinedButton(
                        onClick = {
                            val now = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.HOUR_OF_DAY, hour)
                                    cal.set(Calendar.MINUTE, minute)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
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
                        Icon(Icons.Filled.Schedule, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("时间: ${TimeUtils.formatTime(selectedTimeMillis)}")
                    }
                    Spacer(Modifier.height(8.dp))

                    // ── Repeat type dropdown ──
                    Text(
                        text = "重复方式",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = repeatTypeExpanded,
                        onExpandedChange = { repeatTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = REPEAT_OPTIONS.first { it.first == repeatType }.second,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatTypeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = TextHint.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = repeatTypeExpanded,
                            onDismissRequest = { repeatTypeExpanded = false }
                        ) {
                            REPEAT_OPTIONS.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        repeatType = value
                                        repeatTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // ── Weekly: day-of-week picker ──
                    if (repeatType == "weekly") {
                        Text(
                            text = "选择星期几",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ALL_DOW.forEach { dow ->
                                val isSelected = dow in selectedDaysOfWeek
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDaysOfWeek = if (isSelected) {
                                            selectedDaysOfWeek - dow
                                        } else {
                                            selectedDaysOfWeek + dow
                                        }
                                    },
                                    label = {
                                        Text(
                                            DOW_NAMES[dow] ?: "?",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Tomato.copy(alpha = 0.15f),
                                        selectedLabelColor = Tomato
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Monthly: day-of-month picker ──
                    if (repeatType == "monthly") {
                        Text(
                            text = "选择日期（每月几号）",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        // Common days as chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DOM_OPTIONS.forEach { dom ->
                                val isSelected = dom in selectedDaysOfMonth
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDaysOfMonth = if (isSelected) {
                                            selectedDaysOfMonth - dom
                                        } else {
                                            selectedDaysOfMonth + dom
                                        }
                                    },
                                    label = {
                                        Text(
                                            "${dom}号",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Tomato.copy(alpha = 0.15f),
                                        selectedLabelColor = Tomato
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Task binding ──
                    OutlinedButton(
                        onClick = { showTaskDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val linkedTask = pendingTasks.find { it.id == linkedTaskId }
                        if (linkedTask != null) {
                            Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("绑定: ${linkedTask.title}")
                        } else {
                            Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("绑定任务（可选）")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            when (repeatType) {
                                "weekly" -> {
                                    if (selectedDaysOfWeek.isEmpty()) return@TextButton
                                    val dowJson = selectedDaysOfWeek.joinToString(",", "[", "]")
                                    viewModel.addReminder(
                                        titleInput, descriptionInput, selectedTimeMillis,
                                        repeatType = "weekly",
                                        repeatDaysOfWeek = dowJson,
                                        linkedTaskId = linkedTaskId
                                    )
                                }
                                "monthly" -> {
                                    if (selectedDaysOfMonth.isEmpty()) return@TextButton
                                    val domJson = selectedDaysOfMonth.joinToString(",", "[", "]")
                                    viewModel.addReminder(
                                        titleInput, descriptionInput, selectedTimeMillis,
                                        repeatType = "monthly",
                                        repeatDaysOfMonth = domJson,
                                        linkedTaskId = linkedTaskId
                                    )
                                }
                                else -> {
                                    viewModel.addReminder(
                                        titleInput, descriptionInput, selectedTimeMillis,
                                        repeatType = repeatType,
                                        linkedTaskId = linkedTaskId
                                    )
                                }
                            }
                            showAddDialog = false
                            titleInput = ""
                            descriptionInput = ""
                        }
                    },
                    enabled = titleInput.isNotBlank() && when (repeatType) {
                        "weekly" -> selectedDaysOfWeek.isNotEmpty()
                        "monthly" -> selectedDaysOfMonth.isNotEmpty()
                        else -> true
                    }
                ) { Text("添加") }
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
