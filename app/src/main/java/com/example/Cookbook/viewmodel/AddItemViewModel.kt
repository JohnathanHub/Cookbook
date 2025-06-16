package com.example.Cookbook.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Cookbook.data.ItemEntity
import com.example.Cookbook.repository.ItemRepository
import kotlinx.coroutines.launch

class AddItemViewModel(private val repository: ItemRepository) : ViewModel() {

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

    private val _addItemSuccess = mutableStateOf(false)
    val addItemSuccess: State<Boolean> = _addItemSuccess

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

    fun addItem() {
        if (canAddItem()) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val item = ItemEntity(
                        igLink = _igLink.value,
                        imageUrl = _imageUri.value.toString(),
                        recipeName = _recipeName.value,
                        recipeIngredients = _recipeIngredients.value,
                        recipeSteps = _recipeSteps.value
                    )
                    repository.insertItem(item)

                    // Reset form after successful insertion
                    _igLink.value = ""
                    _imageUri.value = null
                    _recipeName.value = ""
                    _recipeIngredients.value = ""
                    _recipeSteps.value = ""
                    _addItemSuccess.value = true
                } catch (e: Exception) {
                    // Handle error if needed
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun resetAddItemSuccess() {
        _addItemSuccess.value = false
    }

    fun canAddItem(): Boolean {
        return _recipeName.value.isNotBlank() &&
                _imageUri.value != null &&
                _recipeIngredients.value.isNotBlank() &&
                _recipeSteps.value.isNotBlank()
    }
}