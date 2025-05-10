package com.example.bucovinawanders.ui.screens.obiective

import android.os.Build
import android.content.*
import android.net.*
import android.widget.*

import androidx.annotation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.core.net.*

import coil.compose.*
import kotlinx.coroutines.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.models.users.*
import com.example.bucovinawanders.utils.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObiectivDetailsScreen(obiectiv: ObiectivTuristicModel?, token: String?, onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var savedObjectives by remember { mutableStateOf<List<ObiectivTuristicModel>>(emptyList()) }
    var isSaved by remember { mutableStateOf(false) }
    var userHasReviewed by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(3) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewStatus by remember { mutableStateOf<String?>(null) }

    //functie care afiseaza un pop-up cu un mesaj
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //resetam lista de obiective salvate cand se schimba token
    LaunchedEffect(token) {
        savedObjectives = emptyList()
        errorMessage = null
    }

    //verificam daca obiectivul este deja salvat
    LaunchedEffect(token, obiectiv?.idObiectiv) {
        if (!token.isNullOrEmpty() && obiectiv != null) {
            try {
                val response = ApiClient.savesApi.getSavedObiective("Bearer $token")
                if (response.isSuccessful) {
                    val savedList = response.body() ?: emptyList()
                    isSaved = savedList.any { it.idObiectiv == obiectiv.idObiectiv }
                }
            } catch (_: Exception) {
            }
        }
    }

    //verificam daca utilizatorul a dat deja un review
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = ApiClient.reviewsApi.hasReviewed("Bearer $token",
                    idObiectiv = obiectiv?.idObiectiv
                )
                if (response.isSuccessful) {
                    userHasReviewed = response.body() == true
                }
            } catch (_: Exception) {
            }
        }
    }

    //afisam detaliile obiectivului
    if (obiectiv != null) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //header cu numele obiectivului
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = obiectiv.nume,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                //butoane
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //favorite
                    Button(
                        onClick = {
                            if (!token.isNullOrEmpty()) {
                                coroutineScope.launch {
                                    try {
                                        val response = if (!isSaved) {
                                            val request = UserSavesCreateModel(idObiectiv = obiectiv.idObiectiv)
                                            ApiClient.savesApi.saveObiectiv("Bearer $token", request)
                                        } else {
                                            ApiClient.savesApi.deleteObiectiv("Bearer $token", obiectiv.idObiectiv)
                                        }

                                        if (response.isSuccessful) {
                                            isSaved = !isSaved
                                            if (isSaved) {
                                                showToast("Site added to favorites.")
                                            } else {
                                                showToast("Site removed from favorites.")
                                            }
                                        } else {
                                            showToast("Error saving site.")
                                        }
                                    } catch (_ : Exception) {
                                        showToast("Error saving site.")
                                    }
                                }
                            } else {
                                showToast("You must login first.")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = if (isSaved) "Remove from favorites." else "Add to favorites.",
                            tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    //vizitat
                    Button(
                        onClick = {
                            if (!token.isNullOrEmpty()) {
                                coroutineScope.launch {
                                    try {
                                        val request =
                                            UserVisitCreateModel(idObiectiv = obiectiv.idObiectiv)
                                        val response = ApiClient.visitsApi.visitObiectiv(
                                            "Bearer $token",
                                            request
                                        )
                                        if (response.isSuccessful) {
                                            showToast("Marked as visited.")
                                        } else {
                                            showToast("Error marking as visited.")
                                        }
                                    } catch (_ : Exception) {
                                        showToast("Error marking as visited.")
                                    }
                                }
                            } else {
                                showToast("You must login first.")
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = "Visited",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    //directii
                    Button(
                        onClick = {
                            val gmmIntentUri =
                                "geo:${obiectiv.coordonataX},${obiectiv.coordonataY}?q=${obiectiv.coordonataX},${obiectiv.coordonataY}(${
                                    Uri.encode(obiectiv.nume)
                                })".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Directions",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                //program de functionare
                val programList = programFormat(obiectiv.program)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Program",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Program:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        //listam programul
                        programList.forEach { (zi, interval, isToday) ->
                            Text(
                                text = "\t$zi: $interval",
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                //descriere
                if (!obiectiv.descriere.isNullOrEmpty()) {
                    Text(
                        text = obiectiv.descriere,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                //numar recenzii si nota recenzii
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Reviews: ${obiectiv.numarRecenzii ?: 0} | Rating: ${obiectiv.notaRecenzii?.twoDecimalPlaces() }/5",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                //sectiune pentru review vizibila doar daca userul este conectat
                if (!token.isNullOrEmpty() && !userHasReviewed) {
                    Text(
                        text = "Leave a review:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Rating:", fontSize = 16.sp)
                    Slider(
                        value = selectedRating.toFloat(),
                        onValueChange = { selectedRating = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Feedback:", fontSize = 16.sp)
                    TextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Leave a feedback.") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                reviewStatus = try {
                                    val request = ReviewCreateModel(
                                        idObiectiv = obiectiv.idObiectiv,
                                        nota = selectedRating,
                                        comentariu = reviewComment
                                    )
                                    val response = ApiClient.reviewsApi.sendReview("Bearer $token", request)
                                    if (response.isSuccessful) {
                                        userHasReviewed = true
                                        "Review sent successfully."
                                    } else {
                                        "Error sending review: ${response.errorBody()?.string()}"
                                    }
                                } catch (e: Exception) {
                                    "Network error: ${e.message}"
                                }
                            }
                        },
                        enabled = reviewComment.length >= 5
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Send review.")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    reviewStatus?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                } else if (!token.isNullOrEmpty() && userHasReviewed) {
                    Text(
                        text = "You have already submitted a review.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                //poze obiectiv turistic
                if (obiectiv.poze.isNotEmpty()) {
                    Text(
                        text = "Photos:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(obiectiv.poze.size) { index ->
                            Image(
                                painter = rememberAsyncImagePainter(obiectiv.poze[index].urlPoza),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}