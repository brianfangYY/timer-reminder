package com.timer.reminder.ui.reminder

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timer.reminder.ui.theme.*

class ReminderAlertActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent?.getStringExtra("title") ?: "小叮当"
        val message = intent?.getStringExtra("message") ?: "时间到了！"
        val type = intent?.getStringExtra("type") ?: ""

        // 播放铃声（循环播放）
        try {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(this, ringtoneUri).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            // 铃声播放失败，静默处理
        }

        // 震动
        vibrateDevice()

        setContent {
            TimerReminderTheme {
                AlertScreen(
                    title = title,
                    message = message,
                    type = type,
                    onDismiss = {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        finish()
                    }
                )
            }
        }
    }

    private fun vibrateDevice() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 300, 500, 300),
                        intArrayOf(0, 255, 0, 255, 0),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 300, 500, 300), -1)
            }
        } catch (e: Exception) {
            // 震动失败，静默处理
        }
    }

    override fun onDestroy() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        super.onDestroy()
    }
}

@Composable
fun AlertScreen(
    title: String,
    message: String,
    type: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Big star/notification icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = ReminderBlue.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = ReminderBlue,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Title
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Message
            Text(
                text = message,
                fontSize = 20.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (type.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "📌 $type",
                    fontSize = 16.sp,
                    color = ReminderBlue,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(48.dp))

            // 关闭按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReminderBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "我知道了 ✅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
