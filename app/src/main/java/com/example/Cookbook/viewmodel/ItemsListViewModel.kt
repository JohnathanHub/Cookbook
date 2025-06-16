package com.example.Cookbook.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Cookbook.data.ItemEntity
import com.example.Cookbook.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemsListViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _items = mutableStateOf<List<ItemEntity>>(emptyList())
    val items: State<List<ItemEntity>> = _items

    private val _filteredItems = mutableStateOf<List<ItemEntity>>(emptyList())
    val filteredItems: State<List<ItemEntity>> = _filteredItems

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _allItems = mutableStateOf<List<ItemEntity>>(emptyList())

    init {
        loadItems()
    }

    fun loadItems() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val itemsList = repository.getAllItems()
                _allItems.value = itemsList
                _items.value = itemsList
                applySearchFilter()
            } catch (e: Exception) {
                _allItems.value = emptyList()
                _items.value = emptyList()
                _filteredItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshItems() {
        loadItems()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val query = _searchQuery.value.trim()
        val allItems = _allItems.value

        if (query.isEmpty()) {
            _filteredItems.value = allItems
            return
        }

        // Parse the search query for advanced filtering
        val nameFilters = mutableListOf<String>()
        val ingredientFilters = mutableListOf<String>()
        var generalSearchTerm = ""

        // Split by spaces but keep quoted strings together
        val parts = parseSearchQuery(query)

        for (part in parts) {
            when {
                part.startsWith("n:\"") && part.endsWith("\"") && part.length > 3 -> {
                    nameFilters.add(part.substring(3, part.length - 1).lowercase())
                }
                part.startsWith("i:\"") && part.endsWith("\"") && part.length > 3 -> {
                    ingredientFilters.add(part.substring(3, part.length - 1).lowercase())
                }
                part.startsWith("n:") && part.length > 2 -> {
                    nameFilters.add(part.substring(2).lowercase())
                }
                part.startsWith("i:") && part.length > 2 -> {
                    ingredientFilters.add(part.substring(2).lowercase())
                }
                else -> {
                    generalSearchTerm += "$part "
                }
            }
        }

        generalSearchTerm = generalSearchTerm.trim().lowercase()

        _filteredItems.value = allItems.filter { item ->
            val recipeName = item.recipeName.lowercase()
            val recipeIngredients = item.recipeIngredients.lowercase()

            // Check name filters
            val nameMatches = if (nameFilters.isEmpty()) true else {
                nameFilters.all { filter -> recipeName.contains(filter) }
            }

            // Check ingredient filters
            val ingredientMatches = if (ingredientFilters.isEmpty()) true else {
                ingredientFilters.all { filter -> recipeIngredients.contains(filter) }
            }

            // Check general search term (searches both name and ingredients)
            val generalMatches = if (generalSearchTerm.isEmpty()) true else {
                recipeName.contains(generalSearchTerm) || recipeIngredients.contains(generalSearchTerm)
            }

            nameMatches && ingredientMatches && generalMatches
        }
    }

    private fun parseSearchQuery(query: String): List<String> {
        val result = mutableListOf<String>()
        var currentToken = ""
        var insideQuotes = false
        var i = 0

        while (i < query.length) {
            val char = query[i]

            when {
                char == '"' -> {
                    insideQuotes = !insideQuotes
                    currentToken += char
                }
                char == ' ' && !insideQuotes -> {
                    if (currentToken.isNotEmpty()) {
                        result.add(currentToken)
                        currentToken = ""
                    }
                }
                else -> {
                    currentToken += char
                }
            }
            i++
        }

        if (currentToken.isNotEmpty()) {
            result.add(currentToken)
        }

        return result
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _filteredItems.value = _allItems.value
    }
}