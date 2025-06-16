package com.example.Cookbook.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Cookbook.data.ItemEntity
import com.example.Cookbook.repository.ItemRepository
import kotlinx.coroutines.launch

class EditItemViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _itemId = mutableStateOf(0)
    val itemId: State<Int> = _itemId

    private val _igLink = mutableStateOf("")
    val igLink: State<String> = _igLink

    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    private val _recipeName = mutableStateOf("")
    val recipeName: State<String> = _recipeName

    private val _recipeIngredients = mutableStateOf("")
    val recipeIngredients: State<String> = _recipeIngredients

    private val _recipeSteps = mutableStateOf("")
    val recipeSteps: State<String> = _recipeSteps

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _updateSuccess = mutableStateOf(false)
    val updateSuccess: State<Boolean> = _updateSuccess

    private val _isLoadingItem = mutableStateOf(false)
    val isLoadingItem: State<Boolean> = _isLoadingItem

    fun loadItem(itemId: Int) {
        _itemId.value = itemId
        _isLoadingItem.value = true
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            item?.let {
                _igLink.value = it.igLink
                _imageUri.value = Uri.parse(it.imageUrl)
                _recipeName.value = it.recipeName
                _recipeIngredients.value = it.recipeIngredients
                _recipeSteps.value = it.recipeSteps
            }
            _isLoadingItem.value = false
        }
    }

    fun updateIgLink(newIgLink: String) {
        _igLink.value = newIgLink
    }

    fun updateImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun updateRecipeName(newRecipeName: String) {
        _recipeName.value = newRecipeName
    }

    fun updateRecipeIngredients(newIngredients: String) {
        _recipeIngredients.value = newIngredients
    }

    fun updateRecipeSteps(newSteps: String) {
        _recipeSteps.value = newSteps
    }

    fun updateItem() {
        if (canUpdateItem()) {
            _isLoading.value = true
            viewModelScope.launch {
                    val updatedItem = ItemEntity(
                        id = _itemId.value,
                        igLink = _igLink.value,
                        imageUrl = _imageUri.value.toString(),
                        recipeName = _recipeName.value,
                        recipeIngredients = _recipeIngredients.value,
                        recipeSteps = _recipeSteps.value
                    )
                    repository.updateItem(updatedItem)
                    _updateSuccess.value = true
                _isLoading.value = false
            }
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun canUpdateItem(): Boolean {
        return _recipeName.value.isNotBlank() &&
                _imageUri.value != null &&
                _recipeIngredients.value.isNotBlank() &&
                _recipeSteps.value.isNotBlank()
    }
}