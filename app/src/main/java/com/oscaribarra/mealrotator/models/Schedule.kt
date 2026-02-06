// app/src/main/java/com/oscaribarra/mealrotator/models/Schedule.kt
package com.oscaribarra.mealrotator.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStartDate: Long,  // Timestamp of Sunday
    val dayOfWeek: Int,       // 0-6 (Sunday-Saturday)
    val mealId: Long,         // Foreign key to Meal
    val isPinned: Boolean = false
) {
    // Optional: Add this if you need to convert day numbers to names
    fun dayName(): String {
        return when(dayOfWeek) {
            0 -> "Sunday"
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "Unknown"
        }
    }
}