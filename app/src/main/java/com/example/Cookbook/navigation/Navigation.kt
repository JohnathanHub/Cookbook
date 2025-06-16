package com.example.Cookbook.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object AddItem : Screen("add_item", "Add Recipe", Icons.Default.Add)
    object ItemsList : Screen("items_list", "My Recipes", Icons.AutoMirrored.Filled.List)
    object ItemDetail : Screen("item_detail/{itemId}", "Recipe Details", Icons.Default.Info) {
        fun createRoute(itemId: Int) = "item_detail/$itemId"
    }
    object EditItem : Screen("edit_item/{itemId}", "Edit Recipe", Icons.Default.Edit) {
        fun createRoute(itemId: Int) = "edit_item/$itemId"
    }
}

// List of screens that should show the bottom navigation
val bottomNavScreens = listOf(Screen.AddItem, Screen.ItemsList)