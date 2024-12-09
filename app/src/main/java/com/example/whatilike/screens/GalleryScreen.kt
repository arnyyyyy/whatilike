package com.example.whatilike.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import coil.compose.rememberImagePainter
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.whatilike.R
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.data.MuseumApi
import com.example.whatilike.ui.components.PaperBackground
import com.example.whatilike.ui.theme.Brown
import com.example.whatilike.ui.theme.DarkBeige
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun GalleryScreen(
    artViewModel: ArtViewModel,
    likedViewModel: LikedArtworksViewModel,
    user: FirebaseUser?
) {
    val artworks by artViewModel.artworks.collectAsState(initial = emptyList())
    var isInit by remember { mutableStateOf(true) }


    LaunchedEffect(isInit) {
        if (isInit) {
            delay(80000)
            artViewModel.loadRandomArtworks(150)
            isInit = false
        }
    }

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
                    artViewModel.setCurrentApi(MuseumApi.MET)
                    isInit = true
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
                    isInit = true
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
                    }
                    else {
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

suspend fun preloadImage(imageUrl: String?, context: Context) {

    val imageLoader = Coil.imageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .build()

    try {
        val result = imageLoader.execute(request) as SuccessResult
    } catch (e: Exception) {
        e.printStackTrace()
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

    val nextArtwork = artworks.getOrNull(currentIndex + 1)
    val thirdArtwork = artworks.getOrNull(currentIndex + 2)

    LaunchedEffect(nextArtwork) {
        nextArtwork?.let {
            preloadImage(it.primaryImage, context)
        }
    }

    if (viewModel.isLoading.value) {
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
                onSwiped = {
                    currentIndex = (currentIndex + 1) % artworks.size
                },
                artViewModel = viewModel,
                likedViewModel = likedViewModel
            )
        }
        nextArtwork?.let { next ->
            val painter = rememberAsyncImagePainter(next.primaryImage)
            Image(
                painter = painter,
                contentDescription = next.title,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.0f)
            )
        }
        thirdArtwork?.let { next ->
            val painter = rememberAsyncImagePainter(next.primaryImage)
            Image(
                painter = painter,
                contentDescription = next.title,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.0f)
            )
        }
    }
}

@Composable
fun ArtworkCard(
    artwork: ArtObject,
    onSwiped: () -> Unit,
    artViewModel: ArtViewModel,
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
    val alpha_ = 1f - (offsetX.value.absoluteValue / maxOffset).coerceIn(0f, 1f)

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
                                    likedViewModel.addLikedArtwork(currentArtwork.value.objectID)
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
                    model = artwork.primaryImage
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
