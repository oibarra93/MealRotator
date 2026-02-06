// app/src/main/java/com/oscaribarra/mealrotator/models/ScheduleWithMeal.kt
package com.oscaribarra.mealrotator.models

import androidx.room.Embedded
import androidx.room.Relation

data class ScheduleWithMeal(
    @Embedded val schedule: Schedule,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "id"
    )
    val meal: Meal?
)