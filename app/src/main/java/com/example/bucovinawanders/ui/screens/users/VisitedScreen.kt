package com.example.bucovinawanders.ui.screens.users

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.users.*
import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitedScreen(navController: NavController, token: String?) {
    val context = LocalContext.current

    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())

    var visitedObiective by remember { mutableStateOf<List<UserVisitedModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Variabile pentru sortare
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESCENDING) }
    var expandedMenu by remember { mutableStateOf(false) } //pentru DropdownMenu

    val onObiectivClick: (UserVisitedModel) -> Unit = { obiectiv ->
        navController.navigate("mapsScreen?obiectivId=${obiectiv.idObiectiv}")
    }

    LaunchedEffect(token) {
        visitedObiective = emptyList()
        errorMessage = null
        isLoading = true

        if (token != null) {
            try {
                val response = ApiClient.visitsApi.getVisitedObiective("Bearer $token")
                if (response.isSuccessful) {
                    visitedObiective = response.body() ?: emptyList()
                } else {
                    errorMessage = "Error loading visited sites."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Aplicăm sortare în funcție de sortOption
    val sortedList = remember(visitedObiective, sortOption) {
        when (sortOption) {
            SortOption.NAME_ASCENDING -> visitedObiective.sortedBy { it.nume.lowercase() }
            SortOption.NAME_DESCENDING -> visitedObiective.sortedByDescending { it.nume.lowercase() }
            SortOption.DATE_ASCENDING -> visitedObiective.sortedBy { it.dataVizita }
            SortOption.DATE_DESCENDING -> visitedObiective.sortedByDescending { it.dataVizita }
        }
    }

    LaunchedEffect(sortOption) {
        listState.animateScrollToItem(0)
    }

    BucovinaWandersTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Visited Sites") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { expandedMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Sort Options")
                            }

                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name: A → Z") },
                                    onClick = {
                                        sortOption = SortOption.NAME_ASCENDING
                                        expandedMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name: Z → A") },
                                    onClick = {
                                        sortOption = SortOption.NAME_DESCENDING
                                        expandedMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date: Newest First") },
                                    onClick = {
                                        sortOption = SortOption.DATE_DESCENDING
                                        expandedMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date: Oldest First") },
                                    onClick = {
                                        sortOption = SortOption.DATE_ASCENDING
                                        expandedMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                } else if (visitedObiective.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "You have no visited sites.",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding() + 16.dp, // adaugă și spațiu extra
                            bottom = padding.calculateBottomPadding() + 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(sortedList) { obiectiv ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onObiectivClick(obiectiv) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = obiectiv.nume,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = formatDate(obiectiv.dataVizita),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//definim optiunile de sortare
enum class SortOption {
    NAME_ASCENDING,
    NAME_DESCENDING,
    DATE_ASCENDING,
    DATE_DESCENDING
}