package com.example.whatilike.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.whatilike.data.ArtObject
import com.example.whatilike.repository.ArtRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FavouritesScreen(user: FirebaseUser?) {
    val artRepository = ArtRepository()
    var likedArtworks by remember { mutableStateOf<List<ArtObject>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            fetchLikedArtworkIds(userId) { artworkIds ->
                scope.launch {
                    val artworks = artRepository.getArtworksByIds(artworkIds)
                    likedArtworks = artworks
                }
            }
        }
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
    var offsetX by remember { mutableStateOf(0f) }
    var isSwipedLeft by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isDeleted) -500f else offsetX,
        animationSpec = tween(durationMillis = 300)
    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount < 0) {
                    offsetX += dragAmount
                    if (offsetX < -150f) {
                        isSwipedLeft = true
                    } else {
                        isSwipedLeft = false
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
                modifier = Modifier
                    .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = artwork.title,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Image(
                        painter = rememberImagePainter(artwork.primaryImage),
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
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
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

fun fetchLikedArtworkIds(userId: String, onIdsFetched: (List<Int>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("liked_artworks")
        .get()
        .addOnSuccessListener { result ->
            val artworkIds = result.documents.mapNotNull { document ->
                document.getLong("artworkId")?.toInt()
            }
            onIdsFetched(artworkIds)
        }
        .addOnFailureListener { exception ->
            println("Error fetching liked artwork IDs: $exception")
        }
}