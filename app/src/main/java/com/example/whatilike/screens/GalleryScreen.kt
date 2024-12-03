package com.example.whatilike.screens

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import com.example.whatilike.R
import com.example.whatilike.cached.user.addLikedArtworkToFirestore
import com.example.whatilike.ui.theme.DarkBeige
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun GalleryScreen(artViewModel: ArtViewModel, user: FirebaseUser?) {
    val artworks by artViewModel.artworks

    LaunchedEffect(Unit) {
        artViewModel.loadRandomArtworks(15)
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
            CardSwiper(viewModel = artViewModel, userId = user.uid)
        }
    }
}


@Composable
fun CardSwiper(viewModel: ArtViewModel = viewModel(), userId: String) {
    val artworks by viewModel.artworks
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentArtwork = artworks.getOrNull(currentIndex)

    LaunchedEffect(currentIndex) {
        if (currentIndex >= artworks.size - 3) {
            println("timetoload")
            viewModel.loadRandomArtworks(10)
        }
    }

    if (viewModel.isLoading.value && currentIndex >= artworks.size - 3) {
        CircularProgressIndicator()
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
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ArtworkCard(
    artwork: ArtObject,
    userId: String,
    onSwiped: () -> Unit,
    viewModel: ArtViewModel
) {
    var isFlipped by remember { mutableStateOf(false) }

    val currentArtwork = rememberUpdatedState(artwork)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )
    val scale by animateFloatAsState(targetValue = if (isFlipped) 1.5f else 1f)


    val offsetX = remember { Animatable(0f) }
    val maxTiltAngle = 3f
    val maxOffset = 1000f
    val tiltAngle = (offsetX.value / maxOffset) * maxTiltAngle
    val alpha_ = 1f - (offsetX.value.absoluteValue / maxOffset).coerceIn(0f, 1f)

    println("SOS: Image URL - ${currentArtwork.value.primaryImage}, ID - ${currentArtwork.value.objectID}")


    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = tiltAngle
                alpha = alpha_
            }
            .offset { IntOffset((offsetX.value * 1.2).roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val threshold = 300f
                        if (offsetX.value.absoluteValue > threshold) {
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    targetValue = if (offsetX.value > 0) 600f else -600f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                                onSwiped()
                                if (offsetX.value > 0) {
                                    addLikedArtworkToFirestore(userId, currentArtwork.value)
                                }
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    viewModel.removeArtworkFromCache(currentArtwork.value)
                                }
                                isFlipped = false
                                offsetX.snapTo(0f)
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                    }
                }
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        PaperBackground(color = DarkBeige, modifier = Modifier.fillMaxSize())
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
                        .graphicsLayer {
                            rotationY = 180f
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    PaperBackground(color = Color.White, modifier = Modifier.fillMaxSize())
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
                            text = currentArtwork.value.period ?: currentArtwork.value.objectDate
                            ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                val painter = rememberImagePainter(
                    data = artwork.primaryImage,
                    builder = {
                        crossfade(true)
                        placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        error(android.R.drawable.stat_notify_error)
                    }
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painter,
                        contentDescription = currentArtwork.value.title,
                        modifier = Modifier.fillMaxSize().alpha(0.9f)
                    )
                    if (painter.state is AsyncImagePainter.State.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun PaperBackground(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
        )
        Image(
            painter = painterResource(id = R.drawable.paper_texture_white),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
            contentScale = ContentScale.FillBounds
        )
    }
}
