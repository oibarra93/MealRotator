package com.oscaribarra.mealrotator.activities

import MealAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oscaribarra.mealrotator.R
import com.oscaribarra.mealrotator.database.AppDatabase
import com.oscaribarra.mealrotator.databinding.ActivityMainBinding
import com.oscaribarra.mealrotator.models.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: MealAdapter
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)

        // Toolbar
        setSupportActionBar(binding.toolbar)

        // Drawer
        drawerLayout = binding.drawerLayout
        binding.navView.setNavigationItemSelectedListener(this)

        // Put the hamburger on the left properly
        binding.toolbar.setNavigationIcon(R.drawable.menu_24dp_e3e3e3_fill0_wght400_grad0_opsz24)
        binding.toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Recycler
        setupRecyclerView()
        loadMeals()

        // FAB
        binding.fabAddMeal.setOnClickListener {
            startActivity(Intent(this, AddEditMealActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMeals()
    }

    private fun setupRecyclerView() {
        adapter = MealAdapter { meal ->
            val intent = Intent(this, MealDetailActivity::class.java).apply {
                putExtra("MEAL_ID", meal.id)
            }
            startActivity(intent)

            lifecycleScope.launch {
                database.mealDao().update(meal)
            }
        }

        binding.recyclerViewMeals.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun loadMeals() {
        lifecycleScope.launch {
            val meals = database.mealDao().getMealsInRotation()
            if (meals.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.recyclerViewMeals.visibility = View.GONE
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.recyclerViewMeals.visibility = View.VISIBLE
                adapter.submitList(meals)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_meals -> {
                // already here
            }

            R.id.nav_schedule -> {
                startActivity(Intent(this, ScheduleActivity::class.java))
            }

            R.id.nav_grocery_list -> {
                openGroceryListForCurrentWeek()
            }

            R.id.nav_settings -> {
                // startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // ---------------------------
    // Grocery List (from DB schedule)
    // ---------------------------

    private fun openGroceryListForCurrentWeek() {
        lifecycleScope.launch(Dispatchers.IO) {
            val weekStart = getCurrentWeekSunday().timeInMillis
            val scheduleRows = database.scheduleDao().getWeekSchedule(weekStart)

            val meals = scheduleRows.mapNotNull { it.meal }

            if (meals.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No meals scheduled for this week", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val groceryText = buildGroceryListText(meals)

            withContext(Dispatchers.Main) {
                val intent = Intent(this@MainActivity, GroceryListActivity::class.java).apply {
                    putExtra(GroceryListActivity.EXTRA_GROCERY_TEXT, groceryText)
                }
                startActivity(intent)
            }
        }
    }

    private fun buildGroceryListText(meals: List<com.oscaribarra.mealrotator.models.Meal>): String {
        val gson = Gson()
        val listType = object : TypeToken<List<Ingredient>>() {}.type

        val ingredients = meals.flatMap { meal ->
            try {
                gson.fromJson<List<Ingredient>>(meal.ingredientsJson, listType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        val grouped = ingredients
            .filter { it.name.isNotBlank() }
            .groupBy { it.name.trim().lowercase(Locale.getDefault()) }

        val sb = StringBuilder()
        sb.append("Meals this week:\n")
        meals.distinctBy { it.id }.forEach { sb.append("• ").append(it.name).append('\n') }

        sb.append("\nGrocery List:\n")
        grouped.toSortedMap().forEach { (_, items) ->
            val displayName = items.first().name.trim()
            val amounts = items.map { it.amount.trim() }.filter { it.isNotBlank() }

            if (amounts.isEmpty()) {
                sb.append("• ").append(displayName).append('\n')
            } else {
                sb.append("• ").append(displayName).append(" — ")
                    .append(amounts.joinToString(", "))
                    .append('\n')
            }
        }

        return sb.toString().trim()
    }

    private fun getCurrentWeekSunday(): Calendar {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
