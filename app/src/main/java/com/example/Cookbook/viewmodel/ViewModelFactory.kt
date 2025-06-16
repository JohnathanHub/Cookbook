package com.example.Cookbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.Cookbook.repository.ItemRepository

class ViewModelFactory(private val repository: ItemRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddItemViewModel::class.java) -> {
                AddItemViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ItemsListViewModel::class.java) -> {
                ItemsListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ItemDetailViewModel::class.java) -> {
                ItemDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(EditItemViewModel::class.java) -> {
                EditItemViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}