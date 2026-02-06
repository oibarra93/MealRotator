package com.oscaribarra.mealrotator.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oscaribarra.mealrotator.database.AppDatabase
import com.oscaribarra.mealrotator.databinding.ActivityMealDetailBinding
import com.oscaribarra.mealrotator.models.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealDetailBinding
    private lateinit var database: AppDatabase
    private var mealId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)
        mealId = intent.getLongExtra("MEAL_ID", -1)

        if (mealId != -1L) {
            loadMealDetails()
            setupDeleteButton()
        } else {
            Toast.makeText(this, "Meal not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete this meal? This will also remove it from any saved schedules.")
            .setPositiveButton("Delete") { _, _ ->
                deleteMeal()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    /**
     * Deletes the meal AND removes any schedule rows that still reference it.
     * This prevents ScheduleWithMeal from crashing when it tries to load a deleted meal.
     */
    private fun deleteMeal() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ✅ Important: delete schedules referencing this meal first
                // Requires: ScheduleDao has `deleteSchedulesForMeal(mealId: Long)`
                database.scheduleDao().deleteSchedulesForMeal(mealId)

                // Then delete the meal
                database.mealDao().deleteById(mealId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MealDetailActivity, "Meal deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MealDetailActivity,
                        "Failed to delete meal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadMealDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            val meal = database.mealDao().getAllMeals().find { it.id == mealId }

            withContext(Dispatchers.Main) {
                if (meal == null) {
                    Toast.makeText(this@MealDetailActivity, "Meal not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@withContext
                }

                binding.mealName.text = meal.name
                binding.instructions.text = meal.instructions

                val ingredients = try {
                    Gson().fromJson<List<Ingredient>>(
                        meal.ingredientsJson,
                        object : TypeToken<List<Ingredient>>() {}.type
                    ) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                binding.ingredients.text = if (ingredients.isEmpty()) {
                    "No ingredients listed"
                } else {
                    ingredients.joinToString("\n") { ingredient ->
                        "• ${ingredient.amount} ${ingredient.name}"
                    }
                }
            }
        }
    }
}
