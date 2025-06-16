package com.example.Cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.Cookbook.navigation.Screen
import com.example.Cookbook.navigation.bottomNavScreens
import com.example.Cookbook.screens.AddItemScreen
import com.example.Cookbook.screens.ItemDetailScreen
import com.example.Cookbook.screens.ItemsListScreen
import com.example.Cookbook.ui.theme.CookbookTheme
import com.example.Cookbook.screens.EditItemScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookbookTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AddItem.route,
        modifier = modifier
    ) {
        composable(Screen.AddItem.route) {
            AddItemScreen()
        }
        composable(Screen.ItemsList.route) {
            ItemsListScreen(
                navigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            ItemDetailScreen(
                itemId = itemId,
                navigateBack = { navController.popBackStack() },
                navigateToEdit = { editItemId ->
                    navController.navigate(Screen.EditItem.createRoute(editItemId))
                }
            )
        }
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            EditItemScreen(
                itemId = itemId,
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // navbar setup (only screens additem/itemslist)
    val shouldShowBottomNav = bottomNavScreens.any { screen ->
        currentRoute == screen.route
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomNav) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        // Pop up to the start destination to avoid building up a large stack
                                        popUpTo(navController.graph.startDestinationId)
                                        // Avoid multiple copies of the same destination
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}