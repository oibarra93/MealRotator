package com.oscaribarra.ibarramealprep // Make sure this matches your package name

import kotlinx.coroutines.flow.Flow // For reactive data streams

// 1. Constructor takes MealDao as a dependency
class MealRepository(private val mealDao: MealDao) {

    // 2. Expose Flow for all meals from the DAO
    // The ViewModel will observe this Flow for UI updates.
    val allMeals: Flow<List<Meal>> = mealDao.getAllMeals()

    // 3. Expose Flow for pinned meals from the DAO
    val pinnedMeals: Flow<List<Meal>> = mealDao.getPinnedMeals()

    // 4. Expose Flow for unpinned meals (useful for rotation logic)
    val unpinnedMealsForRotation: Flow<List<Meal>> = mealDao.getUnpinnedMealsForRotation()

    // 5. Suspending function to insert a meal
    // This will be called from a coroutine (likely in the ViewModel).
    suspend fun insert(meal: Meal) {
        mealDao.insertMeal(meal)
    }

    // 6. Suspending function to update a meal
    suspend fun update(meal: Meal) {
        mealDao.updateMeal(meal)
    }

    // 7. Suspending function to delete a meal
    suspend fun delete(meal: Meal) {
        mealDao.deleteMeal(meal)
    }

    // 8. Suspending function to get a specific meal by its ID
    suspend fun getMealById(mealId: Int): Meal? {
        return mealDao.getMealById(mealId)
    }

    // 9. Suspending function to get a specific number of meals for scheduling
    // This demonstrates how repository can encapsulate more complex query logic if needed.
    suspend fun getMealsForScheduling(limit: Int, excludedIds: List<Int>): List<Meal> {
        return mealDao.getMealsForScheduling(limit, excludedIds)
    }

    // You could add more complex data handling logic here in the future,
    // e.g., fetching from a network if local data is stale, caching, etc.
}