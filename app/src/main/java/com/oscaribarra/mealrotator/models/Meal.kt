package com.oscaribarra.mealrotator.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // Let Room handle ID generation
    val name: String,
    val instructions: String,
    val ingredientsJson: String,
    var isPinned: Boolean = false,
    var isInRotation: Boolean = true
) {
    // Helper function to convert to Recipe object
    fun toRecipe(): Recipe {
        return Recipe(
            instructions = instructions,
            ingredients = Gson().fromJson(
                ingredientsJson,
                object : TypeToken<List<Ingredient>>() {}.type
            ),
            name = TODO()
        )
    }

    companion object {
        // Helper function to create Meal from Recipe
        fun fromRecipe(name: String, recipe: Recipe): Meal {
            return Meal(
                name = name,
                instructions = recipe.instructions,
                ingredientsJson = Gson().toJson(recipe.ingredients)
            )
        }
    }
}