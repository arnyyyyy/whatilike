package com.example.whatilike.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import com.example.whatilike.data.ArtObject
import com.example.whatilike.data.downloadArtwork
import com.example.whatilike.data.ArtRepositoryFactory
import com.example.whatilike.ui.theme.UltraLightGrey
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun FavouritesScreen(user: FirebaseUser?) {
    val context = LocalContext.current
    val artRepository = remember { ArtRepositoryFactory(context).create() }
    var likedArtworks by remember { mutableStateOf<List<ArtObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            scope.launch {
                val artworkIds = fetchLikedArtworkIds(userId)
                likedArtworks = artRepository.getArtworksByIds(artworkIds)
                isLoading = false
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Favourites",
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (likedArtworks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No liked artworks",
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(likedArtworks) { artwork ->
                    LikedArtworkCard(
                        artwork = artwork,
                        onDeleteClicked = {
                            deleteArtworkFromLiked(user?.uid ?: "", artwork.objectID)
                            likedArtworks =
                                likedArtworks.filterNot { it.objectID == artwork.objectID }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LikedArtworkCard(artwork: ArtObject, onDeleteClicked: () -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwipedLeft by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isDeleted) -500f else offsetX,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(color = UltraLightGrey.copy(alpha = 0.6f))
        .border(
            border = BorderStroke(0.3.dp, Color.Black) ,
            shape = RoundedCornerShape(4.dp)
        )
        .pointerInput(Unit){
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount < 0) {
                    offsetX += dragAmount
                    isSwipedLeft = if (offsetX < -150f) {
                        true
                    } else {
                        false
                    }
                } else if (dragAmount > 0) {
                    if (offsetX > 0) {
                        offsetX = 0f
                        isSwipedLeft = false
                    } else {
                        offsetX += dragAmount
                    }
                }
            }
        }
    ) {
        if (!isDeleted) {
            Card(
                colors = CardDefaults.cardColors(Color.Transparent),
                modifier = Modifier
                    .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { showDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = artwork.title,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = artwork.primaryImage,
                            imageLoader = Coil.imageLoader(context)
                        ),
                        contentDescription = artwork.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }

        if (isSwipedLeft && !isDeleted) {
            IconButton(
                onClick = {
                    onDeleteClicked()
                    isDeleted = true
                    println("Artwork ${artwork.objectID} deleted!")
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color.Gray)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16))
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 48.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(artwork.primaryImage),
                        contentDescription = artwork.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    Text(
                        text = artwork.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)

                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick  = {
                            downloadArtwork(context, artwork.primaryImage!!)
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Sharp.Add,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

}


fun deleteArtworkFromLiked(userId: String, artworkId: Int) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("liked_artworks")
        .whereEqualTo("artworkId", artworkId)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            document?.reference?.delete()
                ?.addOnSuccessListener {
                    println("Artwork successfully deleted from liked list")
                }
                ?.addOnFailureListener { exception ->
                    println("Error deleting artwork: $exception")
                }
        }
        .addOnFailureListener { exception ->
            println("Error fetching liked artwork: $exception")
        }
}


suspend fun fetchLikedArtworkIds(userId: String): List<Int> = withContext(Dispatchers.IO) {
    val result = FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("liked_artworks")
        .get()
        .await()
    return@withContext result.documents.mapNotNull { it.getLong("artworkId")?.toInt() }
}