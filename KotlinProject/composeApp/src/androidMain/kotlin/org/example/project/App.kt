package org.example.project

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.example.project.data.api.RetrofitClient
import org.example.project.data.repository.RoadRepository
import org.example.project.data.repository.LocationRepository
import org.example.project.ui.screens.LoginScreen
import org.example.project.ui.screens.MapScreen
import org.example.project.ui.screens.LocationsScreen
import org.example.project.ui.screens.RoadsListScreen
import org.example.project.ui.screens.RoadDetailScreen
import org.example.project.ui.viewmodel.LocationsViewModel
import org.example.project.ui.viewmodel.RoadsViewModel
import org.example.project.ui.viewmodel.RoadDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    Surface(color = Color.White) {
        val navController = rememberNavController()
        
        // Создаем репозитории один раз с помощью remember
        val roadRepository = remember { RoadRepository(RetrofitClient.instance) }
        val locationRepository = remember { LocationRepository(RetrofitClient.instance) }
        
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            
            composable("map") {
                MapScreen(
                    onNavigateToRoads = { navController.navigate("roads_list") },
                    onNavigateToLocations = { navController.navigate("locations") }
                )
            }
            
            composable("roads_list") {
                val roadsViewModel: RoadsViewModel = viewModel { RoadsViewModel(roadRepository) }
                RoadsListScreen(
                    viewModel = roadsViewModel,
                    onRoadClick = { roadId: Int ->
                        navController.navigate("road_detail/$roadId")
                    }
                )
            }
            
            composable("locations") {
                val locationsViewModel: LocationsViewModel = viewModel { LocationsViewModel(locationRepository) }
                LocationsScreen(
                    viewModel = locationsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable(
                route = "road_detail/{roadId}",
                arguments = listOf(
                    navArgument("roadId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val roadId = backStackEntry.arguments?.getInt("roadId") ?: return@composable
                val roadDetailViewModel: RoadDetailViewModel = viewModel { RoadDetailViewModel(roadRepository) }
                RoadDetailScreen(
                    viewModel = roadDetailViewModel,
                    roadId = roadId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
