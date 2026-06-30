package com.timer.reminder.ui.tomato

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timer.reminder.ui.theme.*
import com.timer.reminder.util.TimeUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomatoScreen(
    viewModel: TomatoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTaskInput by remember { mutableStateOf(false) }
    var taskInput by remember { mutableStateOf("") }

    // Tick sound - generate a short click using AudioTrack
    fun playTickSound() {
        try {
            val sampleRate = 44100
            val durationMs = 50L
            val numSamples = (sampleRate * durationMs / 1000).toInt()
            val buffer = ShortArray(numSamples)
            // Generate a short click waveform
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // 800Hz sine wave with fast decay
                val amplitude = (Short.MAX_VALUE * 0.6 * Math.exp(-t * 80.0)).toInt()
                buffer[i] = (amplitude * Math.sin(2.0 * Math.PI * 800.0 * t)).toInt().toShort()
            }
            val track = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                numSamples * 2,
                AudioTrack.MODE_STATIC,
                sampleRate
            )
            track.write(buffer, 0, numSamples)
            track.play()
            track.release()
        } catch (e: Exception) {
            // Silently ignore sound errors
        }
    }

    // Collect tick events and play sound
    LaunchedEffect(Unit) {
        viewModel.tickEvent.collect {
            playTickSound()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timer section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (uiState.state) {
                            TomatoState.IDLE -> "🍅 准备就绪"
                            TomatoState.WORKING -> "🎯 专注中"
                            TomatoState.BREAK -> "☕ 休息时间"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.state == TomatoState.WORKING) Tomato
                        else if (uiState.state == TomatoState.BREAK) TaskGreen
                        else TextPrimary
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = TimeUtils.formatTimer(uiState.remainingSeconds),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.state == TomatoState.WORKING) Tomato
                        else if (uiState.state == TomatoState.BREAK) TaskGreen
                        else TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (uiState.state) {
                            TomatoState.IDLE -> {
                                Button(
                                    onClick = {
                                        if (uiState.currentSessionTask.isEmpty()) {
                                            showTaskInput = true
                                        } else {
                                            viewModel.startWork(uiState.currentSessionTask)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Tomato)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("开始专注")
                                }
                            }
                            TomatoState.WORKING -> {
                                OutlinedButton(onClick = { viewModel.stop() }) {
                                    Icon(Icons.Filled.Pause, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("暂停")
                                }
                            }
                            TomatoState.BREAK -> {
                                OutlinedButton(onClick = { viewModel.stop() }) {
                                    Icon(Icons.Filled.Stop, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("结束休息")
                                }
                            }
                        }

                        if (uiState.state != TomatoState.IDLE) {
                            OutlinedButton(onClick = { viewModel.reset() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("重置")
                            }
                        }
                    }

                    if (uiState.state != TomatoState.IDLE) {
                        // Tick sound toggle during active timer
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("嘀嗒声 ", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = uiState.isTickSoundEnabled,
                                onCheckedChange = { viewModel.toggleTickSound() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Tomato,
                                    checkedTrackColor = TomatoLight
                                )
                            )
                            Text(
                                text = if (uiState.isTickSoundEnabled) "🔊" else "🔇",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (uiState.state == TomatoState.IDLE) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("工作时长: ", style = MaterialTheme.typography.bodyMedium)
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text("${uiState.workDuration}分钟")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf(15, 20, 25, 30, 45, 60).forEach { min ->
                                        DropdownMenuItem(
                                            text = { Text("${min}分钟") },
                                            onClick = {
                                                viewModel.updateWorkDuration(min)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text("休息时长: ", style = MaterialTheme.typography.bodyMedium)
                            var expanded2 by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded2 = true }) {
                                    Text("${uiState.breakDuration}分钟")
                                }
                                DropdownMenu(
                                    expanded = expanded2,
                                    onDismissRequest = { expanded2 = false }
                                ) {
                                    listOf(3, 5, 10, 15, 20).forEach { min ->
                                        DropdownMenuItem(
                                            text = { Text("${min}分钟") },
                                            onClick = {
                                                viewModel.updateBreakDuration(min)
                                                expanded2 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.totalCompleted}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Tomato
                        )
                        Text("完成番茄", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }

    if (showTaskInput) {
        AlertDialog(
            onDismissRequest = { showTaskInput = false },
            title = { Text("这次要做什么？") },
            text = {
                OutlinedTextField(
                    value = taskInput,
                    onValueChange = { taskInput = it },
                    label = { Text("任务描述（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startWork(taskInput)
                        showTaskInput = false
                        taskInput = ""
                    }
                ) { Text("开始专注") }
            },
            dismissButton = {
                TextButton(onClick = { showTaskInput = false }) { Text("取消") }
            }
        )
    }
}
