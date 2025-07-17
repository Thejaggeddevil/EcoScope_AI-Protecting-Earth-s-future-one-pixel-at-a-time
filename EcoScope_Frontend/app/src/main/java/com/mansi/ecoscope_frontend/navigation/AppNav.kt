package com.mansi.ecoscope_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mansi.ecoscope_frontend.ui.screens.*

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            GeoEyeDashboardScreen(
                navController = navController,
                onGlacialLakesClick = { navController.navigate("glacial") },
                onRoadNetworksClick = { navController.navigate("road") },
                onDrainageSystemsClick = { navController.navigate("drainage") },
                onSatelliteAnalysisClick = { navController.navigate("satellite") }
            )
        }
        composable("glacial") {
            GlacialLakesMonitoringScreen(onBackClick = { navController.popBackStack() })
        }
        composable("drainage") {
            DrainageSystemsMonitoringScreen(onBackClick = { navController.popBackStack() })
        }
        composable("road") {
            RoadNetworksAnalysisScreen(onBackClick = { navController.popBackStack() })
        }
        composable("satellite") {
            com.mansi.ecoscope_frontend.ui.screens.SatelliteAnalysisScreen(onBackClick = { navController.popBackStack() })
        }
        // Add login, signup, settings, etc. as needed
        composable("login") { loginnav(navController) }
        composable("signup") { Signnav(navController) }
        composable("settings") { SettingsScreen(navController) }
        
    }
}
