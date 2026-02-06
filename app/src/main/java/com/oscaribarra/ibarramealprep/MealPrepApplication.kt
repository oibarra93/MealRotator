package com.oscaribarra.ibarramealprep

import android.app.Application

class MealPrepApplication : Application() {
    // Using by lazy so the database and repository are only created when they're needed
    // and only once per application lifecycle.
    val database by lazy { AppDatabase.getDatabase(this) }
    val mealRepository by lazy { MealRepository(database.mealDao()) }
}