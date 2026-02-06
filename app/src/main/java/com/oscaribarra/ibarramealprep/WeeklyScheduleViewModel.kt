package com.oscaribarra.ibarramealprep
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.contains
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
data class WeeklyScheduleUiState(
    val weeklyMeals: List<Meal?> = List(7) { null },
    val allMeals: List<Meal> = emptyList(),
    val isLoading: Boolean = true,
    val hasMealsInSystem: Boolean = false,
    val errorMessage: String? = null
)
class WeeklyScheduleViewModel(private val mealRepository: MealRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WeeklyScheduleUiState())
    val uiState: StateFlow<WeeklyScheduleUiState> = _uiState.asStateFlow()
    private val allMealsFlow: StateFlow<List<Meal>> = mealRepository.allMeals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    private val pinnedMealsFlow: StateFlow<List<Meal>> = mealRepository.pinnedMeals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    private val unpinnedMealsForRotationFlow: StateFlow<List<Meal>> = mealRepository.unpinnedMealsForRotation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    init {
        viewModelScope.launch {
            combine(
                allMealsFlow,
                pinnedMealsFlow,
                unpinnedMealsForRotationFlow
            ) { allMeals, pinnedMeals, unpinnedMeals ->
                val hasAnyMeals = allMeals.isNotEmpty()
                val currentSchedule = generateWeeklySchedule(allMeals, pinnedMeals, unpinnedMeals)
                currentSchedule.filterNotNull().forEach { mealInSchedule ->
                    val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                    if (mealInSchedule.lastScheduledDate == null || mealInSchedule.lastScheduledDate!! < oneWeekAgo) {
                        updateMealLastScheduledDate(mealInSchedule, System.currentTimeMillis())
                    }
                }
                WeeklyScheduleUiState(
                    weeklyMeals = currentSchedule,
                    allMeals = allMeals,
                    isLoading = false,
                    hasMealsInSystem = hasAnyMeals,
                    errorMessage = null
                )
            }.collect { newState -> _uiState.value = newState }
        }
    }
    private fun generateWeeklySchedule(
        allMealsAvailable: List<Meal>,
        pinnedForThisWeek: List<Meal>,
        unpinnedRotationCandidates: List<Meal>
    ): List<Meal?> {
        if (allMealsAvailable.isEmpty()) {
            _uiState.value = _uiState.value.copy(hasMealsInSystem = false, isLoading = false)
            return List(7) { null }
        }
        _uiState.value = _uiState.value.copy(hasMealsInSystem = true)
        val schedule = MutableList<Meal?>(7) { null }
        val scheduledMealIdsThisWeek = mutableSetOf<Int>()
        var dayIndex = 0
        val uniquePinnedMeals = pinnedForThisWeek.distinctBy { it.id }
        uniquePinnedMeals.forEach { pinnedMeal ->
            if (dayIndex < 7) {
                schedule[dayIndex++] = pinnedMeal
                scheduledMealIdsThisWeek.add(pinnedMeal.id)
            }
        }
        val rotationCandidates = unpinnedRotationCandidates
            .filter { !scheduledMealIdsThisWeek.contains(it.id) }
            .toMutableList()
        while (dayIndex < 7 && rotationCandidates.isNotEmpty()) {
            val mealToSchedule = rotationCandidates.removeAt(0)
            schedule[dayIndex++] = mealToSchedule
            scheduledMealIdsThisWeek.add(mealToSchedule.id)
        }
        if (dayIndex < 7) {
            val remainingUniqueMeals = allMealsAvailable
                .filter { !scheduledMealIdsThisWeek.contains(it.id) }
                .shuffled().toMutableList()
            while (dayIndex < 7 && remainingUniqueMeals.isNotEmpty()) {
                val mealToSchedule = remainingUniqueMeals.removeAt(0)
                schedule[dayIndex++] = mealToSchedule
                scheduledMealIdsThisWeek.add(mealToSchedule.id)
            }
        }
        if (dayIndex < 7 && allMealsAvailable.isNotEmpty()) {
            val fallbackPool = allMealsAvailable.shuffled().toMutableList()
            while (dayIndex < 7) {
                if (fallbackPool.isEmpty() && allMealsAvailable.isNotEmpty()) { // Should not happen if allMealsAvailable is not empty
                    schedule[dayIndex++] = allMealsAvailable.random() // Absolute fallback
                } else if (fallbackPool.isNotEmpty()) {
                    schedule[dayIndex++] = fallbackPool.removeAt(0)
                } else { // No meals left at all, fill with null (should be covered by initial check)
                    schedule[dayIndex++] = null
                }
            }
        }
        return schedule
    }
    fun togglePinMeal(meal: Meal) {
        viewModelScope.launch {
            val updatedMeal = meal.copy(isPinnedForNextWeek = !meal.isPinnedForNextWeek)
            mealRepository.update(updatedMeal)
        }
    }
    private fun updateMealLastScheduledDate(meal: Meal, date: Long) {
        viewModelScope.launch {
            mealRepository.update(meal.copy(lastScheduledDate = date))
        }
    }
    fun refreshSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Indicate loading
            val newSchedule = generateWeeklySchedule(
                allMealsFlow.value,
                pinnedMealsFlow.value,
                unpinnedMealsForRotationFlow.value
            )
            newSchedule.filterNotNull().forEach { mealInSchedule ->
                val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                if (mealInSchedule.lastScheduledDate == null || mealInSchedule.lastScheduledDate!! < oneWeekAgo) {
                    updateMealLastScheduledDate(mealInSchedule, System.currentTimeMillis())
                }
            }
            _uiState.value = _uiState.value.copy(
                weeklyMeals = newSchedule,
                isLoading = false,
                allMeals = allMealsFlow.value, // Ensure allMeals is also up-to-date
                hasMealsInSystem = allMealsFlow.value.isNotEmpty()
            )
        }
    }
}