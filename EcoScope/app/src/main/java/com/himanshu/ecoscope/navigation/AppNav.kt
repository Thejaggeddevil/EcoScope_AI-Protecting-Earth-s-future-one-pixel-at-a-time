package com.himanshu.ecoscope.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.himanshu.ecoscope.ui.screens.*


@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { loginnav(navController) }
       composable("home") { homenav(navController) }
        composable("glacial") { Glacialnav(navController) }
        composable("road") { Roadnav(navController) }
        composable("drainage") { Drainagenav(navController) }
        composable("signup") { Signnav(navController) }
        composable("settings") { SettingsScreen(navController) }

    }
}
