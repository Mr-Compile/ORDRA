package com.ordra.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ordra.feature.analytics.AnalyticsScreen
import com.ordra.feature.calendar.DeliveryCalendarScreen
import com.ordra.feature.cod.CodOverviewScreen
import com.ordra.feature.detections.DetectionsScreen
import com.ordra.feature.detail.OrderDetailScreen
import com.ordra.feature.home.HomeScreen
import com.ordra.feature.orderedit.AddEditOrderScreen
import com.ordra.feature.orders.OrdersScreen
import com.ordra.feature.settings.AboutScreen
import com.ordra.feature.settings.SettingsScreen
import com.ordra.feature.smartimport.SmartImportScreen
import com.ordra.feature.timeline.ShipmentTimelineScreen
import com.ordra.core.ui.rememberReducedMotion
import com.ordra.core.ui.theme.microTween
import com.ordra.core.ui.theme.standardTween

@Composable
fun OrdraNavHost(navController: NavHostController) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.ordra.app.AppIntake.pendingRoute.collect { route ->
            navController.navigate(route)
        }
    }
    val reducedMotion = rememberReducedMotion()
    NavHost(
        navController = navController,
        startDestination = OrdraRoutes.HOME,
        enterTransition = {
            if (reducedMotion) {
                fadeIn(microTween())
            } else {
                slideInHorizontally(standardTween()) { it / 4 } + fadeIn(standardTween())
            }
        },
        exitTransition = {
            if (reducedMotion) {
                fadeOut(microTween())
            } else {
                slideOutHorizontally(standardTween()) { -it / 4 } + fadeOut(standardTween())
            }
        },
        popEnterTransition = {
            if (reducedMotion) {
                fadeIn(microTween())
            } else {
                slideInHorizontally(standardTween()) { -it / 4 } + fadeIn(standardTween())
            }
        },
        popExitTransition = {
            if (reducedMotion) {
                fadeOut(microTween())
            } else {
                slideOutHorizontally(standardTween()) { it / 4 } + fadeOut(standardTween())
            }
        },
    ) {
        composable(OrdraRoutes.HOME) {
            HomeScreen(
                onAddOrder = { navController.navigate(OrdraRoutes.orderEditor()) },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderDetail(id)) },
                onOpenOrders = { navController.navigate(OrdraRoutes.ORDERS) },
                onOpenCod = { navController.navigate(OrdraRoutes.COD) },
                onOpenSettings = { navController.navigate(OrdraRoutes.SETTINGS) },
                onOpenCalendar = { navController.navigate(OrdraRoutes.CALENDAR) },
                onSmartImport = { navController.navigate(OrdraRoutes.SMART_IMPORT) },
            )
        }
        composable(OrdraRoutes.ANALYTICS) {
            AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(OrdraRoutes.DETECTIONS) {
            DetectionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderDetail(id)) },
            )
        }
        composable(OrdraRoutes.SMART_IMPORT) {
            SmartImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(OrdraRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(OrdraRoutes.ABOUT) },
                onOpenDetections = { navController.navigate(OrdraRoutes.DETECTIONS) },
                onOpenAnalytics = { navController.navigate(OrdraRoutes.ANALYTICS) },
            )
        }
        composable(OrdraRoutes.ABOUT) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(OrdraRoutes.ORDERS) {
            OrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderDetail(id)) },
            )
        }
        composable(OrdraRoutes.CALENDAR) {
            DeliveryCalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderDetail(id)) },
            )
        }
        composable(OrdraRoutes.COD) {
            CodOverviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenOrder = { id -> navController.navigate(OrdraRoutes.orderDetail(id)) },
            )
        }
        composable(
            route = OrdraRoutes.ORDER_DETAIL,
            arguments = listOf(navArgument(OrdraRoutes.ARG_ORDER_ID) { type = NavType.StringType }),
        ) {
            OrderDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(OrdraRoutes.orderEditor(id)) },
                onViewTimeline = { id -> navController.navigate(OrdraRoutes.shipmentTimeline(id)) },
            )
        }
        composable(
            route = OrdraRoutes.SHIPMENT_TIMELINE,
            arguments = listOf(navArgument(OrdraRoutes.ARG_SHIPMENT_ID) { type = NavType.StringType }),
        ) {
            ShipmentTimelineScreen(onNavigateBack = { navController.popBackStack() })
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
