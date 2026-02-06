package com.oscaribarra.ibarramealprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oscaribarra.ibarramealprep.ui.theme.IbarraMealPrepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IbarraMealPrepTheme {
                MealPrepApp()
            }
        }
    }
}

@Composable
fun MealPrepApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as MealPrepApplication // Get application instance
    val mealRepository = application.mealRepository // Get repository

    NavHost(navController = navController, startDestination = NavRoutes.WEEKLY_SCHEDULE) {
        composable(NavRoutes.WEEKLY_SCHEDULE) {
            // Create ViewModel using the factory
            val weeklyScheduleViewModel: WeeklyScheduleViewModel = viewModel(
                factory = MealViewModelFactory(mealRepository)
            )
            WeeklyScheduleScreen(
                viewModel = weeklyScheduleViewModel,
                onNavigateToAddMealScreen = { mealId ->
                    // If mealId is null, it means new meal. Route will handle optional arg.
                    // If mealId is present, it's for editing.
                    val route = if (mealId != null) {
                        "${NavRoutes.ADD_EDIT_MEAL}?${NavRoutes.MEAL_ID_ARG}=$mealId"
                    } else {
                        NavRoutes.ADD_EDIT_MEAL // Navigating without the optional arg
                    }
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = NavRoutes.ADD_MEAL_ROUTE, // Using the route with optional argument
            arguments = listOf(
                navArgument(NavRoutes.MEAL_ID_ARG) {
                    type = NavType.IntType
                    defaultValue = -1 // Indicates no ID / new meal if arg not passed or is default
                }
            )
        ) { backStackEntry ->
            val mealIdArg = backStackEntry.arguments?.getInt(NavRoutes.MEAL_ID_ARG)
            val effectiveMealId = if (mealIdArg == -1) null else mealIdArg // Convert -1 to null for ViewModel

            // Create ViewModel using the factory, passing the mealId
            val addMealViewModel: AddMealViewModel = viewModel(
                factory = AddMealViewModelFactory(mealRepository, effectiveMealId)
            )
            AddMealScreen(
                viewModel = addMealViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Add other destinations here if needed
    }
}