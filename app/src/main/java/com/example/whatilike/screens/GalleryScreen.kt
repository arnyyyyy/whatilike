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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GalleryScreen(viewModel: ArtViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
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
        CardSwiper(viewModel = viewModel)
    }
}

@Composable
fun CardSwiper(viewModel: ArtViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val artworks by viewModel.artworks
    var currentIndex by remember { mutableStateOf(0) }
    val currentArtwork = artworks.getOrNull(currentIndex)

    LaunchedEffect(currentIndex) {
        if (currentIndex == artworks.size - 1) {
            viewModel.loadRandomArtworks(10)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        currentArtwork?.let { artwork ->
            ArtworkCard(
                artwork = artwork,
                onSwiped = {
                    if (currentIndex < artworks.size - 1) {
                        currentIndex++
                    }
                }
            )
        }
    }
}

@Composable
fun ArtworkCard(
    artwork: ArtObject,
    onSwiped: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX > 300f || offsetX < -300f) {
                            onSwiped()
                            offsetX = 0f
                        } else {
                            offsetX = 0f
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                }
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .fillMaxSize()
//                .aspectRatio(1f)
        ) {
            if (isFlipped) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
//                        .padding(16.dp)
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = artwork.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = artwork.artistDisplayName,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Image(
                    painter = rememberImagePainter(data = artwork.primaryImage),
                    contentDescription = artwork.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
