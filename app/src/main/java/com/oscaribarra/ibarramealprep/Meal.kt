package com.oscaribarra.ibarramealprep // Make sure this matches your package name

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals") // 1. @Entity annotation
data class Meal(
    @PrimaryKey(autoGenerate = true) // 2. @PrimaryKey annotation
    val id: Int = 0,                 // 3. Unique ID for each meal

    val name: String,                // 4. Name of the meal

    val ingredients: String,         // 5. Ingredients for the meal
    //    Consider a more structured approach later if needed

    val instructions: String,        // 6. Cooking instructions or notes

    val cookTime: String,            // 7. Estimated cook time (e.g., "30 minutes")

    var isPinnedForNextWeek: Boolean = false, // 8. To track if the user wants this next week
    //    'var' because it can change

    var lastScheduledDate: Long? = null   // 9. Timestamp of when this meal was last
    //    part of a weekly schedule. 'var' and nullable.
    //    Used for rotation logic.
)