package com.example.bucovinawanders.ui.screens.users

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import kotlinx.coroutines.*

import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, isLoggedIn: Boolean, userName: String?, onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())
    val pressedColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    //UI pentru settings screen
    BucovinaWandersTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        if (isLoggedIn) {
                            Text(
                                text = "Welcome, $userName!",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        //switch pentru dark mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark Mode", modifier = Modifier.weight(1f))
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { newValue ->
                                    coroutineScope.launch {
                                        ThemePreferences.setTheme(context, newValue)
                                    }
                                }
                            )
                        }

                        //buton pentru obiecgtivele vizitate vizibil doar daca utilizatorul este logat
                        if (isLoggedIn) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("visitedScreen") }, //navigare la visited screen
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Visited Sites")
                                Icon(
                                    Icons.Default.RemoveRedEye,
                                    contentDescription = "Visited",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    //buton pentru login sau logout/delete account
                    Column {
                        //butoane disponibile unui user logat
                        if (isLoggedIn) {
                            //buton pentru logout, suntem redirectionati la maps screen
                            Button(
                                onClick = {
                                    onLogout()
                                    navController.navigate("mapsScreen") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Logout")
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            //buton pentru delete account, suntem redirectionati la delete account screen
                            Button(
                                onClick = { navController.navigate("deleteAccountScreen") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Delete Account")
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            //buton de login, suntem redirectionati la login screen
                            Button(
                                onClick = { navController.navigate("loginScreen") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Login")
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = "Login",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}