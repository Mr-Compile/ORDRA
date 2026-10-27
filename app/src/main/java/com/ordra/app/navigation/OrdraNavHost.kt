package com.ordra.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ordra.feature.home.HomeScreen

@Composable
fun OrdraNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = OrdraRoutes.HOME,
    ) {
        composable(OrdraRoutes.HOME) {
            HomeScreen()
        }
    }
}
