package com.oscaribarra.mealrotator.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oscaribarra.mealrotator.models.Meal

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal): Long

    @Update
    suspend fun update(meal: Meal)

    @Query("SELECT * FROM meals WHERE isInRotation = 1")
    suspend fun getMealsInRotation(): List<Meal>

    @Query("SELECT * FROM meals WHERE isPinned = 1")
    suspend fun getPinnedMeals(): List<Meal>

    @Query("SELECT * FROM meals")
    suspend fun getAllMeals(): List<Meal>

    @Delete
    suspend fun delete(meal: Meal)

    // OR if you prefer to delete by ID:
    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteById(mealId: Long)

}