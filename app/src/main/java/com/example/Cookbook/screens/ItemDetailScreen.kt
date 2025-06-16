package com.example.Cookbook.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.Cookbook.data.AppDatabase
import com.example.Cookbook.repository.ItemRepository
import com.example.Cookbook.viewmodel.ItemDetailViewModel
import com.example.Cookbook.viewmodel.ViewModelFactory
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    navigateBack: () -> Unit,
    navigateToEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.Companion.getInstance(context) }
    val repository = remember { ItemRepository(db.itemDao()) }
    val viewModelFactory = remember { ViewModelFactory(repository) }
    val viewModel: ItemDetailViewModel = viewModel(factory = viewModelFactory)

    val item by viewModel.item
    val isLoading by viewModel.isLoading
    val deleteSuccess by viewModel.deleteSuccess

    // Load item when screen is first opened
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    // Navigate back when delete is successful
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteSuccess()
            navigateBack()
        }
    }

    // Function to convert Instagram URL to embed URL
    fun getInstagramEmbedUrl(igLink: String): String? {
        return try {
            // Extract post ID from various Instagram URL formats
            val postId = when {
                igLink.contains("www.instagram.com") -> {
                    val igLinkComponents: List<String> = igLink.split("/")
                    igLinkComponents[4]
                }

                igLink.contains("shorts") -> {
                    val ytLinkComponents: List<String> = igLink.split("/")
                    ytLinkComponents[4]
                    //val ytBetter: List<String> = ytLinkComponents[4].split("?")
                    //ytBetter[1]

                }

                else -> null
            }
            when {
                igLink.contains("www.instagram.com") -> {
                    postId?.let {
                        "https://www.instagram.com/p/$it/embed/"
                    }
                }

                igLink.contains("shorts") -> {
                    postId?.let {
                        //"https://youtube.com/embed/$it"           // lepsza wersja, jeszcze nie wiem czemu ale nie działa
                        "https://youtube.com/shorts/$it"
                    }
                }

                else -> null
            }
        }catch (e: Exception){
            null
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (item != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar with back button
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

            // Recipe Name
            Text(
                text = item!!.recipeName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instagram Video Section (if available)
            if (item!!.igLink.isNotBlank()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Instagram Video",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        //WebView
                        var showWebView by remember { mutableStateOf(true) }
                        var webViewFailed by remember { mutableStateOf(false) }

                        if (showWebView && !webViewFailed) {
                            val embedUrl = getInstagramEmbedUrl(item!!.igLink)

                            if (embedUrl != null) {
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(450.dp),
                                    factory = { context ->
                                        WebView(context).apply {
                                            webViewClient = object : WebViewClient() {
                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    errorCode: Int,
                                                    description: String?,
                                                    failingUrl: String?
                                                ) {
                                                    super.onReceivedError(view, errorCode, description, failingUrl)
                                                    webViewFailed = true
                                                }
                                            }
                                            settings.javaScriptEnabled = true
                                            settings.loadWithOverviewMode = true
                                            settings.setSupportZoom(false)
                                        }
                                    },
                                    update = { webView ->
                                        webView.loadUrl(embedUrl)
                                    }
                                )
                            } else {
                                webViewFailed = true
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recipe Image
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = item!!.imageUrl.toUri(),
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ingredients section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item!!.recipeIngredients,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Steps section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item!!.recipeSteps,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        navigateToEdit(item!!.id)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit Recipe")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.deleteItem()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete Recipe", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    } else {
        // Item not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Recipe not found",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = navigateBack) {
                    Text("Go Back")
                }
            }
        }
    }
}