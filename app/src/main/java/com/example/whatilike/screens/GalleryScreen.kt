package com.example.whatilike.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.whatilike.data.ArtViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.whatilike.data.ArtObject
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun GalleryScreen(viewModel: ArtViewModel = viewModel(), user: FirebaseUser?) {
    val artworks by viewModel.artworks

    LaunchedEffect(Unit) {
        viewModel.loadRandomArtworks(15)
    }

    if (artworks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No artworks found", fontSize = 18.sp, color = Color.Black)
        }
    } else {
        if (user != null) {
            CardSwiper(viewModel = viewModel, userId = user.uid)
        }
    }
}


fun addLikedArtworkToFirestore(userId: String, artwork: ArtObject) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .collection("liked_artworks")
        .document(artwork.objectID.toString())
        .set(mapOf("artworkId" to artwork.objectID))
        .addOnSuccessListener {
            println(artwork.objectID.toString())
            println("Artwork added to liked_artworks successfully")
        }
        .addOnFailureListener { exception ->
            println("Error adding artwork: $exception")
        }
}


@Composable
fun CardSwiper(viewModel: ArtViewModel = viewModel(), userId: String) {
    val artworks by viewModel.artworks
    var currentIndex by remember { mutableStateOf(0) }
    val currentArtwork = artworks.getOrNull(currentIndex)

    LaunchedEffect(currentIndex) {
        if (currentIndex >= artworks.size - 1) {
            viewModel.loadRandomArtworks(10)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        currentArtwork?.let { artwork ->
            println("iiiha" + artwork.objectID.toString())
            ArtworkCard(
                artwork = artwork,
                userId = userId,
                onSwiped = {
                    currentIndex = (currentIndex + 1) % artworks.size
                }
            )
        }
    }
}


@Composable
fun ArtworkCard(
    artwork: ArtObject,
    userId: String,
    onSwiped: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isFlipped by remember { mutableStateOf(false) }

    val currentArtwork = rememberUpdatedState(artwork)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 300)
    )

    println("SOS: Image URL - ${currentArtwork.value.primaryImage}, ID - ${currentArtwork.value.objectID}") // Нормальный id

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        onSwiped()
                        if (offsetX > 300f) {
                            println("ll ${currentArtwork.value.objectID}") // id текущей картины
                            addLikedArtworkToFirestore(userId, currentArtwork.value)
                        }
                        offsetX = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                }
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        println("suk " + currentArtwork.value.objectID) // Нормальный id
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .fillMaxSize()
        ) {
            if (isFlipped) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentArtwork.value.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentArtwork.value.artistDisplayName,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentArtwork.value.period ?: currentArtwork.value.objectDate ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Image(
                    painter = rememberImagePainter(data = currentArtwork.value.primaryImage),
                    contentDescription = currentArtwork.value.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

