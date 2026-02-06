package com.oscaribarra.ibarramealprep // Make sure this matches your package name

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Important for reactive data

@Dao // 1. @Dao annotation
interface MealDao {

    // 2. Insert a meal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    // 3. Update an existing meal
    @Update
    suspend fun updateMeal(meal: Meal)

    // 4. Delete a meal
    @Delete
    suspend fun deleteMeal(meal: Meal)

    // 5. Get all meals, ordered by name, observed as a Flow
    @Query("SELECT * FROM meals ORDER BY name ASC")
    fun getAllMeals(): Flow<List<Meal>>

    // 6. Get a specific meal by its ID
    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: Int): Meal? // Nullable if meal might not exist

    // 7. Get all meals that are pinned for next week, observed as a Flow
    @Query("SELECT * FROM meals WHERE isPinnedForNextWeek = 1 ORDER BY name ASC") // SQLite uses 1 for true
    fun getPinnedMeals(): Flow<List<Meal>>

    // Optional: You might want a method to get meals for the weekly rotation
    // that are NOT pinned and considers lastScheduledDate
    @Query("SELECT * FROM meals WHERE isPinnedForNextWeek = 0 ORDER BY lastScheduledDate ASC, name ASC")
    fun getUnpinnedMealsForRotation(): Flow<List<Meal>>

    // Optional: Get a specific number of meals for the weekly schedule,
    // perhaps excluding certain IDs (e.g., already scheduled ones)
    // This is just an idea and might need more complex logic in your ViewModel/Repository
    @Query("SELECT * FROM meals WHERE id NOT IN (:excludedIds) ORDER BY lastScheduledDate ASC, RANDOM() LIMIT :limit")
    suspend fun getMealsForScheduling(limit: Int, excludedIds: List<Int>): List<Meal>
}