package com.oscaribarra.mealrotator.models

data class Recipe(
    val name: String,
    val instructions: String,
    val ingredients: List<Ingredient>
)