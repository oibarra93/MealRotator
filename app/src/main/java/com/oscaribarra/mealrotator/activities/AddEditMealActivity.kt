package com.oscaribarra.mealrotator.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oscaribarra.mealrotator.database.AppDatabase
import com.oscaribarra.mealrotator.databinding.ActivityAddEditMealBinding
import com.oscaribarra.mealrotator.models.Ingredient
import com.oscaribarra.mealrotator.models.Meal
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditMealActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMealBinding
    private lateinit var database: AppDatabase
    private var mealId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)
        mealId = intent.getLongExtra("MEAL_ID", -1)

        if (mealId != -1L) {
            // Editing existing meal
            loadMealData()
        }

        binding.saveButton.setOnClickListener {
            saveMeal()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadMealData() {
        GlobalScope.launch(Dispatchers.Main) {
            val meal = database.mealDao().getAllMeals().find { it.id == mealId }
            meal?.let {
                binding.mealNameEditText.setText(it.name)
                binding.instructionsEditText.setText(it.instructions)

                val ingredients = Gson().fromJson<List<Ingredient>>(
                    it.ingredientsJson,
                    object : TypeToken<List<Ingredient>>() {}.type
                )
                val ingredientsText = ingredients.joinToString("\n") { ingredient ->
                    "${ingredient.amount}|${ingredient.name}"
                }
                binding.ingredientsEditText.setText(ingredientsText)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveMeal() {
        val name = binding.mealNameEditText.text.toString()
        val instructions = binding.instructionsEditText.text.toString()

        val ingredients = binding.ingredientsEditText.text.toString()
            .split("\n")
            .filter { it.isNotBlank() }
            .map {
                val parts = it.split("|")
                if (parts.size >= 2) {
                    Ingredient(parts[1].trim(), parts[0].trim())
                } else {
                    Ingredient(it.trim(), "")
                }
            }

        val ingredientsJson = Gson().toJson(ingredients)

        // Create meal WITHOUT specifying ID
        val meal = Meal(
            name = name,
            instructions = instructions,
            ingredientsJson = ingredientsJson
        )

        lifecycleScope.launch(Dispatchers.IO) {
            if (mealId == -1L) {
                // Let Room auto-generate the ID
                database.mealDao().insert(meal)
            } else {
                // For updates, keep the existing ID
                database.mealDao().update(meal.copy(id = mealId))
            }
            finish()
        }
    }
}