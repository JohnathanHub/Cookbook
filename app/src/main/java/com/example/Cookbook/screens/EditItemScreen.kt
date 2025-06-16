package com.example.Cookbook.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.Cookbook.data.AppDatabase
import com.example.Cookbook.repository.ItemRepository
import com.example.Cookbook.viewmodel.EditItemViewModel
import com.example.Cookbook.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: Int,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { ItemRepository(db.itemDao()) }
    val viewModelFactory = remember { ViewModelFactory(repository) }
    val viewModel: EditItemViewModel = viewModel(factory = viewModelFactory)

    val igLink by viewModel.igLink
    val imageUri by viewModel.imageUri
    val recipeName by viewModel.recipeName
    val recipeIngredients by viewModel.recipeIngredients
    val recipeSteps by viewModel.recipeSteps
    val isLoading by viewModel.isLoading
    val isLoadingItem by viewModel.isLoadingItem
    val updateSuccess by viewModel.updateSuccess

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.resetUpdateSuccess()
            navigateBack()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // get uri permission
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.updateImageUri(it)
                Log.d("EditItemScreen", "Persistent permission taken for URI: $it")
            } catch (e: SecurityException) {
                Log.w("EditItemScreen", "Could not take persistent permission for URI: $it", e)
                viewModel.updateImageUri(it)
            }
        }
    }

    if (!isLoadingItem) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Edit Recipe",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = recipeName,
                onValueChange = { viewModel.updateRecipeName(it) },
                label = { Text("Recipe Name *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = igLink,
                onValueChange = { viewModel.updateIgLink(it) },
                label = { Text("Instagram Link (optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // preview image
            imageUri?.let { uri ->
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Current Recipe Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Image picker
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Recipe Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Ingredients
            OutlinedTextField(
                value = recipeIngredients,
                onValueChange = { viewModel.updateRecipeIngredients(it) },
                label = { Text("Ingredients *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 3,
                maxLines = 6,
                placeholder = { Text("List all ingredients here...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Steps
            OutlinedTextField(
                value = recipeSteps,
                onValueChange = { viewModel.updateRecipeSteps(it) },
                label = { Text("Recipe Steps *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                minLines = 4,
                maxLines = 8,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = navigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { viewModel.updateItem() },
                    enabled = viewModel.canUpdateItem() && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating...")
                        }
                    } else {
                        Text("Update Recipe")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}