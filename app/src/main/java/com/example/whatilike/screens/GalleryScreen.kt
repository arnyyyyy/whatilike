package com.example.whatilike.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.whatilike.data.ArtViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whatilike.data.ArtObject
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.whatilike.R
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.data.MuseumApi
import com.example.whatilike.ui.components.PaperBackground
import com.example.whatilike.ui.theme.Brown
import com.example.whatilike.ui.theme.DarkBeige
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun GalleryScreen(
    artViewModel: ArtViewModel,
    likedViewModel: LikedArtworksViewModel,
    user: FirebaseUser?
) {
    val artworks by artViewModel.artworks.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    artViewModel.setCurrentApi(MuseumApi.HARVARD)
                    Log.d("Gallery", "moved to Harvard")
                }, colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Brown,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.harvard_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(3.dp)
                        .height(20.dp)
                        .width(20.dp),
                    contentScale = ContentScale.Crop
                )

            }

            Button(
                onClick = {
                    artViewModel.setCurrentApi(MuseumApi.MET)
                    Log.d("Gallery", "moved to Met")
                }, colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Brown,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.met_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(3.dp)
                        .height(20.dp)
                        .width(20.dp),
                    contentScale = ContentScale.Crop
                )

            }

            Button(
                onClick = {
                    artViewModel.setCurrentApi(MuseumApi.HERMITAGE)
                    Log.d("Gallery", "moved to Hermitage")
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Brown,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hermitage_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(3.dp)
                        .height(20.dp)
                        .width(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            PaperBackground(color = DarkBeige, modifier = Modifier.fillMaxSize())
            if (artworks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (artViewModel.isLoading.value) {
                        CircularProgressIndicator()
                    } else {
                        Text(text = "No artworks found", fontSize = 18.sp, color = Color.Black)
                    }
                }
            } else {
                if (user != null) {
                    CardSwiper(
                        viewModel = artViewModel,
                        likedViewModel = likedViewModel,
                    )
                }
            }
        }
    }
}


@Composable
fun CardSwiper(
    viewModel: ArtViewModel = viewModel(),
    likedViewModel: LikedArtworksViewModel = viewModel(),
) {
    val artworks by viewModel.artworks.collectAsState(initial = emptyList())
    var currentIndex by viewModel.currentIndex
    val currentArtwork = artworks.getOrNull(currentIndex)
    val context = LocalContext.current

    Log.d("CARD", "entered card swiper")

    val nextArtwork = artworks.getOrNull(currentIndex + 1)

    LaunchedEffect(nextArtwork) {
        nextArtwork?.let {
            viewModel.preloadImage(it.primaryImageSmall, context)
        }
    }


    if (artworks.isNotEmpty()) {
        val artworksToPreload = listOf(
            artworks.getOrNull(currentIndex + 2),
            artworks.getOrNull(currentIndex + 3),
            artworks.getOrNull(currentIndex + 4),
            artworks.getOrNull(currentIndex + 5),
            artworks.getOrNull(currentIndex + 6),
            artworks.getOrNull(currentIndex + 7),
            artworks.getOrNull(currentIndex + 8),
            artworks.getOrNull(currentIndex + 9),
            artworks.getOrNull(currentIndex + 10),
            artworks.getOrNull(currentIndex + 11),
        )

        artworksToPreload.forEach { artwork ->
            artwork?.let {
                LaunchedEffect(it) {
                    viewModel.preloadImage(it.primaryImageSmall, context)
                }
            }
        }
    }

    if (viewModel.isLoading.value) {
        CircularProgressIndicator()
    }

    nextArtwork?.let { next ->
        val painter = rememberAsyncImagePainter(
            next.primaryImageSmall, imageLoader = viewModel.imageLoader.value
        )
        Image(
            painter = painter,
            contentDescription = next.title,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.0f)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        nextArtwork?.let {
            val nextPainter = rememberAsyncImagePainter(
                model = it.primaryImageSmall,
                imageLoader = viewModel.imageLoader.value

            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = nextPainter,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        currentArtwork?.let { artwork ->
            ArtworkCard(
                artwork = artwork,
                onSwiped = {
                    currentIndex = (currentIndex + 1) % artworks.size
                },
                viewModel = viewModel,
                likedViewModel = likedViewModel
            )
        }
    }
}

@Composable
fun ArtworkCard(
    artwork: ArtObject,
    onSwiped: () -> Unit,
    viewModel: ArtViewModel,
    likedViewModel: LikedArtworksViewModel
) {
    var isFlipped by remember { mutableStateOf(false) }

    val currentArtwork = rememberUpdatedState(artwork)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    val offsetX = remember { Animatable(0f) }
    val maxTiltAngle = 3f
    val maxOffset = 1000f
    val tiltAngle = (offsetX.value / maxOffset) * maxTiltAngle
    val alpha_ = 1.0f

//    println("SOS: Image URL - ${currentArtwork.value.primaryImage}, ID - ${currentArtwork.value.objectID}")


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
                                    likedViewModel.viewModelScope.launch {
                                        likedViewModel.addLikedArtwork(currentArtwork.value.objectID)
                                    }
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
                val painter = rememberAsyncImagePainter(
                    model = artwork.primaryImageSmall,
                    imageLoader = viewModel.imageLoader.value

                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painter,
                        contentDescription = currentArtwork.value.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.9f)
                    )
                    if (painter.state is AsyncImagePainter.State.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
