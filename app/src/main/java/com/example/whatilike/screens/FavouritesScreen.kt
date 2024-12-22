package com.example.whatilike.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.example.whatilike.R
import com.example.whatilike.cached.user.FolderViewModel
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.data.ArtObject
import com.example.whatilike.data.downloadArtwork
import com.example.whatilike.ui.components.PaperBackground
import com.example.whatilike.ui.theme.DarkBeige
import com.example.whatilike.ui.theme.UltraLightGrey
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun FavouritesScreen(viewModel: LikedArtworksViewModel, foldersViewModel: FolderViewModel) {
    val likedArtworks by viewModel.likedArtworks.collectAsState(initial = emptyList())
    val folders by foldersViewModel.folders.collectAsState(initial = emptyList())

//    LaunchedEffect(Unit) {
//        viewModel.loadLikedArtworks()
//    }

//    LaunchedEffect(Unit) {
//        foldersViewModel.loadFolders()
//    }
//

    if (viewModel.isLoading.value) {
        CircularProgressIndicator()
    }

    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            PaperBackground(color = DarkBeige, modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(16.dp))
//                Row() {
                Text(
                    text = "Favourites",
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                )

//                    IconButton(onClick = { foldersViewModel.addFolder("folder1") }) { }
//                }

                if (likedArtworks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator()
                        } else {
                            Text(text = "No liked artworks", fontSize = 18.sp, color = Color.Black)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(likedArtworks) { artwork ->
                            LikedArtworkCard(
                                artwork = artwork,
                                onDeleteClicked = { viewModel.deleteLikedArtwork(artwork.objectID) },
                                likedArtworksViewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LikedArtworkCard(
    artwork: ArtObject,
    onDeleteClicked: () -> Unit,
    likedArtworksViewModel: LikedArtworksViewModel
) {
//    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwipedLeft by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

//    val animatedOffsetX by animateFloatAsState(
//        targetValue = if (isDeleted) -500f else offsetX,
//        animationSpec = tween(durationMillis = 300), label = ""
//    )

    val offsetX = remember { Animatable(0f) }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(color = UltraLightGrey.copy(alpha = 0.6f))
        .border(
            border = BorderStroke(0.3.dp, Color.Black),
            shape = RoundedCornerShape(4.dp)
        )
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = 100f
                    if (offsetX.value < -threshold) {
                        coroutineScope.launch {
                            isSwipedLeft = true
                            offsetX.animateTo(
                                targetValue = if (offsetX.value > 0) 0f else -200f,
                                animationSpec = tween(durationMillis = 200)
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            isSwipedLeft = false
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200)
                            )
                        }
                    }
                }
            ) { change, dragAmount ->
                change.consume()
                coroutineScope.launch {
                    offsetX.snapTo(offsetX.value + dragAmount)
                }
            }
        }
    ) {
        if (!isDeleted) {
            Card(
                colors = CardDefaults.cardColors(Color.Transparent),
                modifier = Modifier
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

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = artwork.primaryImage,
                                imageLoader = likedArtworksViewModel.imageLoader.value
                            ),
                            contentDescription = artwork.title,
                            modifier = Modifier
                                .height(200.dp)
                                .offset { IntOffset((offsetX.value / 2).roundToInt(), 0) }
                                .background(Color.Black)
                        )

                        if (isSwipedLeft && !isDeleted) {
                            println("AA")
                            IconButton(
                                onClick = {
                                    onDeleteClicked()
                                    isDeleted = true
                                    println("Artwork ${artwork.objectID} deleted!")
                                },
                                modifier = Modifier
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
                }
            }
        }

    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset(0f, 0f)) }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
            ) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 48.dp)
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = artwork.primaryImage,
                        imageLoader = likedArtworksViewModel.imageLoader.value

                    )

                    Image(
                        painter = painter,
                        contentDescription = artwork.title,
                        modifier = Modifier
                            .padding(16.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
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

                    Button(
                        onClick = {
                            downloadArtwork(context, artwork.primaryImage!!, artwork.title)
                        },
                        colors = ButtonColors(
                            contentColor = Color.Black.copy(alpha = 0.6f),
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            disabledContentColor = Color.Black.copy(alpha = 0.6f),
                            disabledContainerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = CircleShape,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)

                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.download),
                            contentDescription = "Download",
                            modifier = Modifier
                                .height(25.dp)
                                .width(25.dp)

                        )
                    }

                }
            }
        }
    }

}