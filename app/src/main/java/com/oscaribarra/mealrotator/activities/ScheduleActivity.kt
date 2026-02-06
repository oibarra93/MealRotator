package com.oscaribarra.mealrotator.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oscaribarra.mealrotator.R
import com.oscaribarra.mealrotator.database.AppDatabase
import com.oscaribarra.mealrotator.databinding.ActivityScheduleBinding
import com.oscaribarra.mealrotator.models.Ingredient
import com.oscaribarra.mealrotator.models.Meal
import com.oscaribarra.mealrotator.models.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private lateinit var database: AppDatabase

    private val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val currentWeek = MutableList<Meal?>(7) { null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)

        setupViews()
        loadSchedule()
    }

    private fun setupViews() {
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
            showDateMeals(selectedDate)
        }

        binding.btnGenerateWeek.setOnClickListener { generateWeeklySchedule() }
        binding.btnSaveSchedule.setOnClickListener { saveWeeklySchedule() }

        // If you want a button in this screen too (optional), add one and hook it here.
        // Example:
        // binding.btnGroceryList.setOnClickListener { openGroceryList() }
    }

    override fun onResume() {
        super.onResume()
        loadSchedule()
    }

    private fun loadSchedule() {
        lifecycleScope.launch(Dispatchers.IO) {
            val weekStart = getCurrentWeekSunday().timeInMillis
            val scheduleRows = database.scheduleDao().getWeekSchedule(weekStart)

            withContext(Dispatchers.Main) {
                if (scheduleRows.isEmpty()) {
                    currentWeek.indices.forEach { i ->
                        currentWeek[i] = null
                        updateDayView(i, null, isPinned = false)
                    }
                    showEmptyState()
                } else {
                    currentWeek.indices.forEach { dayIndex ->
                        val row = scheduleRows.firstOrNull { it.schedule.dayOfWeek == dayIndex }
                        val meal = row?.meal
                        val pinned = row?.schedule?.isPinned == true
                        currentWeek[dayIndex] = meal
                        updateDayView(dayIndex, meal, pinned)
                    }
                }
            }
        }
    }

    private fun generateWeeklySchedule() {
        lifecycleScope.launch(Dispatchers.IO) {
            val meals = database.mealDao().getMealsInRotation()
            if (meals.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScheduleActivity, "No meals available", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val shuffledMeals = meals.shuffled()

            currentWeek.indices.forEach { dayIndex ->
                val meal = shuffledMeals.getOrNull(dayIndex % shuffledMeals.size)
                currentWeek[dayIndex] = meal

                withContext(Dispatchers.Main) {
                    updateDayView(dayIndex, meal, isPinned = false)
                }
            }
        }
    }

    private fun updateDayView(dayIndex: Int, meal: Meal?, isPinned: Boolean) {
        runOnUiThread {
            val dayView = layoutInflater.inflate(
                R.layout.item_day_schedule,
                binding.weekContainer,
                false
            ).apply {
                val tvDay = findViewById<TextView>(R.id.tvDay)
                val tvMealName = findViewById<TextView>(R.id.tvMealName)
                val btnPin = findViewById<ImageButton>(R.id.btnPin)

                tvDay.text = days[dayIndex]

                if (meal != null) {
                    tvMealName.text = meal.name

                    // ✅ open meal details on click
                    setOnClickListener {
                        val intent = Intent(this@ScheduleActivity, MealDetailActivity::class.java).apply {
                            putExtra("MEAL_ID", meal.id)
                        }
                        startActivity(intent)
                    }

                    btnPin.visibility = View.VISIBLE
                    btnPin.setImageResource(if (isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin_outline)
                    btnPin.setOnClickListener { togglePinStatus(dayIndex) }

                } else {
                    tvMealName.text = "No meal scheduled"
                    setOnClickListener {
                        Toast.makeText(this@ScheduleActivity, "No meal scheduled for ${days[dayIndex]}", Toast.LENGTH_SHORT).show()
                    }
                    btnPin.visibility = View.INVISIBLE
                    btnPin.setOnClickListener(null)
                }
            }

            if (binding.weekContainer.childCount > dayIndex) {
                binding.weekContainer.removeViewAt(dayIndex)
                binding.weekContainer.addView(dayView, dayIndex)
            } else {
                while (binding.weekContainer.childCount < dayIndex) {
                    val filler = layoutInflater.inflate(R.layout.item_day_schedule, binding.weekContainer, false)
                    binding.weekContainer.addView(filler)
                }
                binding.weekContainer.addView(dayView)
            }
        }
    }

    private fun togglePinStatus(dayIndex: Int) {
        val meal = currentWeek.getOrNull(dayIndex)
        if (meal == null) {
            Toast.makeText(this, "No meal to pin for ${days[dayIndex]}", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val weekStart = getCurrentWeekSunday().timeInMillis

            val existing = database.scheduleDao()
                .getWeekSchedule(weekStart)
                .firstOrNull { it.schedule.dayOfWeek == dayIndex }

            if (existing == null) {
                database.scheduleDao().insert(
                    Schedule(
                        weekStartDate = weekStart,
                        dayOfWeek = dayIndex,
                        mealId = meal.id,
                        isPinned = true
                    )
                )
                withContext(Dispatchers.Main) {
                    updateDayView(dayIndex, meal, isPinned = true)
                }
                return@launch
            }

            val newPinned = !existing.schedule.isPinned
            database.scheduleDao().updatePinnedStatus(existing.schedule.id, newPinned)

            withContext(Dispatchers.Main) {
                updateDayView(dayIndex, meal, isPinned = newPinned)
            }
        }
    }

    private fun saveWeeklySchedule() {
        lifecycleScope.launch(Dispatchers.IO) {
            val weekStart = getCurrentWeekSunday().timeInMillis

            database.scheduleDao().getWeekSchedule(weekStart).forEach {
                database.scheduleDao().deleteDaySchedule(weekStart, it.schedule.dayOfWeek)
            }

            currentWeek.forEachIndexed { index, meal ->
                meal?.let {
                    database.scheduleDao().insert(
                        Schedule(
                            weekStartDate = weekStart,
                            dayOfWeek = index,
                            mealId = it.id,
                            isPinned = false
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ScheduleActivity, "Schedule saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDateMeals(date: Calendar) {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1
        val meal = currentWeek.getOrNull(dayOfWeek)

        AlertDialog.Builder(this)
            .setTitle(days[dayOfWeek])
            .setMessage(meal?.name ?: "No meal scheduled")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEmptyState() {
        // Basic placeholder behavior
        if (binding.weekContainer.childCount == 0) {
            currentWeek.indices.forEach { dayIndex ->
                updateDayView(dayIndex, null, isPinned = false)
            }
        }
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

    // ---------------------------
    // Grocery list feature
    // ---------------------------

    fun openGroceryListFromSchedule() {
        val meals = currentWeek.filterNotNull()
        if (meals.isEmpty()) {
            Toast.makeText(this, "No meals scheduled this week", Toast.LENGTH_SHORT).show()
            return
        }

        val groceryText = buildGroceryListText(meals)

        val intent = Intent(this, GroceryListActivity::class.java).apply {
            putExtra(GroceryListActivity.EXTRA_GROCERY_TEXT, groceryText)
        }
        startActivity(intent)
    }

    private fun buildGroceryListText(meals: List<Meal>): String {
        val gson = Gson()
        val listType = object : TypeToken<List<Ingredient>>() {}.type

        val ingredients = meals.flatMap { meal ->
            try {
                gson.fromJson<List<Ingredient>>(meal.ingredientsJson, listType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        // Group by ingredient name (case-insensitive) and combine amounts as a simple list
        val grouped = ingredients
            .filter { it.name.isNotBlank() }
            .groupBy { it.name.trim().lowercase(Locale.getDefault()) }

        val sb = StringBuilder()
        sb.append("Meals this week:\n")
        meals.forEach { sb.append("• ").append(it.name).append('\n') }

        sb.append("\nGrocery List:\n")
        grouped.toSortedMap().forEach { (_, items) ->
            val displayName = items.first().name.trim()
            val amounts = items.mapNotNull { it.amount?.trim() }
                .filter { it.isNotBlank() }

            if (amounts.isEmpty()) {
                sb.append("• ").append(displayName).append('\n')
            } else {
                // If you want “summed amounts” later, we can add parsing rules. For now we concatenate.
                sb.append("• ").append(displayName).append(" — ")
                    .append(amounts.joinToString(", "))
                    .append('\n')
            }
        }

        return sb.toString().trim()
    }
}
