package com.ordra.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ordra.feature.home.HomeScreen
import com.ordra.feature.orderedit.AddEditOrderScreen

@Composable
fun OrdraNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = OrdraRoutes.HOME,
    ) {
        composable(OrdraRoutes.HOME) {
            HomeScreen(
                onAddOrder = { navController.navigate(OrdraRoutes.orderEditor()) },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderEditor(id)) },
            )
        }
        composable(
            route = OrdraRoutes.ORDER_EDITOR,
            arguments = listOf(
                navArgument(OrdraRoutes.ARG_ORDER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            AddEditOrderScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
