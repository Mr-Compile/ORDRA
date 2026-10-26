package com.ordra.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ordra.feature.home.HomeScreen

object OrdraDestinations {
    const val HOME = "home"
}

@Composable
fun OrdraApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OrdraDestinations.HOME,
    ) {
        composable(OrdraDestinations.HOME) {
            HomeScreen()
        }
    }
}
