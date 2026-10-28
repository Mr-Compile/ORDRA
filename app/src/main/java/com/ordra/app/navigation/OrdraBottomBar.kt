package com.ordra.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ordra.R

/** Routes that show the bottom bar — mirrors the navbar items in mockup/index.html. */
val TOP_LEVEL_ROUTES = setOf(
    OrdraRoutes.HOME,
    OrdraRoutes.ORDERS,
    OrdraRoutes.ANALYTICS,
    OrdraRoutes.SETTINGS,
)

/**
 * Primary navigation: Home / Orders / [Add] / Analytics / Settings, with the Add
 * action as a visually emphasized center FAB — mirrors mockup/index.html .navbar.
 */
@Composable
fun OrdraBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == OrdraRoutes.HOME,
            onClick = { onNavigate(OrdraRoutes.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
        )
        NavigationBarItem(
            selected = currentRoute == OrdraRoutes.ORDERS,
            onClick = { onNavigate(OrdraRoutes.ORDERS) },
            icon = { Icon(Icons.Filled.Inventory2, contentDescription = null) },
            label = { Text(stringResource(R.string.orders_title)) },
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nav_add_order),
                )
            }
        }
        NavigationBarItem(
            selected = currentRoute == OrdraRoutes.ANALYTICS,
            onClick = { onNavigate(OrdraRoutes.ANALYTICS) },
            icon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_analytics)) },
        )
        NavigationBarItem(
            selected = currentRoute == OrdraRoutes.SETTINGS,
            onClick = { onNavigate(OrdraRoutes.SETTINGS) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings_title)) },
        )
    }
}
