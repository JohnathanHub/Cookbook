package com.example.Cookbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val igLink: String,
    val imageUrl: String,
    val recipeName: String,
    val recipeIngredients: String,
    val recipeSteps: String
)