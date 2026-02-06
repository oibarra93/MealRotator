package com.oscaribarra.mealrotator.models

data class GroceryItem(
    val name: String,
    val amounts: List<String>,
    var isChecked: Boolean = false
)
