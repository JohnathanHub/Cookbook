package com.example.Cookbook.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Cookbook.data.ItemEntity
import com.example.Cookbook.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemDetailViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _item = mutableStateOf<ItemEntity?>(null)
    val item: State<ItemEntity?> = _item

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _deleteSuccess = mutableStateOf(false)
    val deleteSuccess: State<Boolean> = _deleteSuccess

    fun loadItem(itemId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _item.value = repository.getItemById(itemId)
            } catch (e: Exception) {
                _item.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteItem() {
        _item.value?.let { currentItem ->
            viewModelScope.launch {
                repository.deleteItemById(currentItem.id)
                _deleteSuccess.value = true
            }
        }
    }

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }
}