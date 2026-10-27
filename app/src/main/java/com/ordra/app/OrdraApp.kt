package com.ordra.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ordra.app.navigation.OrdraNavHost

@Composable
fun OrdraApp() {
    val navController = rememberNavController()
    OrdraNavHost(navController = navController)
}
