package com.timer.reminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val triggerAtMillis: Long,
    val isRepeating: Boolean = false,
    val repeatIntervalMinutes: Long = 0,
    val repeatType: String = "none",  // "none" | "daily" | "workdays" | "weekly" | "monthly"
    val repeatDaysOfWeek: String = "[]",  // JSON array of Calendar.DAY_OF_WEEK values
    val repeatDaysOfMonth: String = "[]", // JSON array of day-of-month values
    val isEnabled: Boolean = true,
    val linkedTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Parse repeatDaysOfWeek JSON into a list of integers */
    fun getRepeatDaysOfWeekList(): List<Int> {
        return try {
            JSONArray(repeatDaysOfWeek).let { arr ->
                (0 until arr.length()).map { arr.getInt(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Parse repeatDaysOfMonth JSON into a list of integers */
    fun getRepeatDaysOfMonthList(): List<Int> {
        return try {
            JSONArray(repeatDaysOfMonth).let { arr ->
                (0 until arr.length()).map { arr.getInt(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Human-readable label for the repeat type */
    fun getRepeatLabel(): String {
        return when (repeatType) {
            "none" -> "仅一次"
            "daily" -> "每天"
            "workdays" -> "工作日"
            "weekly" -> {
                val dayNames = mapOf(
                    1 to "周日", 2 to "周一", 3 to "周二", 4 to "周三",
                    5 to "周四", 6 to "周五", 7 to "周六"
                )
                getRepeatDaysOfWeekList().map { dayNames[it] ?: "?" }.joinToString("、")
            }
            "monthly" -> {
                "每月${getRepeatDaysOfMonthList().joinToString("、")}号"
            }
            else -> "仅一次"
        }
    }
}
