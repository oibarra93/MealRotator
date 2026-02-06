package com.oscaribarra.mealrotator.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.oscaribarra.mealrotator.models.Meal

// Schedule.kt
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: Long, // Sunday timestamp
    val dayOfWeek: Int, // 0-6 (Sunday-Saturday)
    val mealId: Long,
    val isPinned: Boolean = false
)

// ScheduleWithMeal.kt
data class ScheduleWithMeal(
    @Embedded val schedule: Schedule,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "id"
    )
    val meal: Meal
)