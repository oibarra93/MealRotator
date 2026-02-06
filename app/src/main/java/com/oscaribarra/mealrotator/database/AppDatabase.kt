// app/src/main/java/com/oscaribarra/mealrotator/database/AppDatabase.kt
package com.oscaribarra.mealrotator.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.oscaribarra.mealrotator.models.Meal
import com.oscaribarra.mealrotator.models.Schedule

@Database(
    entities = [Meal::class, Schedule::class],
    version = 3,
    exportSchema = true  // Changed to true for better debugging
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meal_database"
                )
                    .fallbackToDestructiveMigration(false) // Keep this for now
                    .addCallback(object : Callback() {
                        // Optional: Add callbacks if needed
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}