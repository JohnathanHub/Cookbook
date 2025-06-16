package com.example.Cookbook.screens

import androidx.compose.animation.core.*
import android.media.MediaPlayer
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.Cookbook.viewmodel.AddItemViewModel
import com.example.Cookbook.data.AppDatabase
import com.example.Cookbook.repository.ItemRepository
import com.example.Cookbook.viewmodel.ViewModelFactory
import com.example.Cookbook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.Companion.getInstance(context) }
    val repository = remember { ItemRepository(db.itemDao()) }
    val viewModelFactory = remember { ViewModelFactory(repository) }
    val viewModel: AddItemViewModel = viewModel(factory = viewModelFactory)

    val igLink by viewModel.igLink
    val imageUri by viewModel.imageUri
    val recipeName by viewModel.recipeName
    val recipeIngredients by viewModel.recipeIngredients
    val recipeSteps by viewModel.recipeSteps
    val isLoading by viewModel.isLoading
    val addItemSuccess by viewModel.addItemSuccess

    // Animation state for button rotation
    var isAnimating by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            if (isAnimating) {
                isAnimating = false
            }
        }
    )

    // MediaPlayer for custom sound
    val mediaPlayer: MediaPlayer? = remember {
        try {
            MediaPlayer.create(context, R.raw.duolingo_correct)
        } catch (e: Exception) {
            Log.e("AddItemScreen", "Error creating MediaPlayer", e)
            null
        }
    }

    // Clean up MediaPlayer
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Take persistent permission for the URI
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.updateImageUri(it)
                Log.d("AddItemScreen", "Persistent permission taken for URI: $it")
            } catch (e: SecurityException) {
                // Some content providers don't support persistent permissions
                // In this case, you might want to copy the image to internal storage
                Log.w("AddItemScreen", "Could not take persistent permission for URI: $it", e)
                viewModel.updateImageUri(it) // Still set the URI, but it may not persist
            }
        }
    }

    // Show success message and reset
    LaunchedEffect(addItemSuccess) {
        if (addItemSuccess) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                        player.prepare()
                    }
                    player.start()
                }
            } catch (e: Exception) {
                Log.e("AddItemScreen", "Error playing sound", e)
            }

            isAnimating = true
            viewModel.resetAddItemSuccess()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add New Recipe",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Recipe Name
        OutlinedTextField(
            value = recipeName,
            onValueChange = { viewModel.updateRecipeName(it) },
            label = { Text("Recipe Name *") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Instagram Link (optional)
        OutlinedTextField(
            value = igLink,
            onValueChange = { viewModel.updateIgLink(it) },
            label = { Text("Instagram Link (optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Image picker
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Recipe Image *")
        }

        imageUri?.let {
            Text(
                text = "✓ Image selected",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
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
            placeholder = { Text("List all ingredients here.") }
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
            placeholder = { Text("Step-by-step instructions.") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.addItem() },
            enabled = viewModel.canAddItem() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotationAngle)
        ) {
            Text("Add Recipe")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "* Required fields",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}