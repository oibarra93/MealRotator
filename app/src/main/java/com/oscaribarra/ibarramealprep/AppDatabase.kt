package com.oscaribarra.ibarramealprep // Make sure this matches your package name

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Meal::class], version = 1, exportSchema = false) // 1. @Database annotation
abstract class AppDatabase : RoomDatabase() { // 2. Extends RoomDatabase

    abstract fun mealDao(): MealDao // 3. Abstract method for your DAO

    companion object { // 4. Singleton pattern implementation
        @Volatile // 5. @Volatile annotation
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 6. synchronized block
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( // 7. Room.databaseBuilder
                    context.applicationContext, // Use application context
                    AppDatabase::class.java,
                    "meal_planner_db" // 8. Name of your database file
                )
                    // .addMigrations(MIGRATION_1_2) // 9. Placeholder for migrations
                    // .fallbackToDestructiveMigration() // Use only during development if you don't want to write migrations yet
                    .build()

                INSTANCE = instance
                instance // Return the newly created instance
            }
        }
    }
}