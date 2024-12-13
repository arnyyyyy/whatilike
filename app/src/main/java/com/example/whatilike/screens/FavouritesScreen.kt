package com.example.whatilike.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import com.example.whatilike.cached.user.FolderViewModel
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.data.ArtObject
import com.example.whatilike.data.downloadArtwork
import com.example.whatilike.ui.components.PaperBackground
import com.example.whatilike.ui.theme.DarkBeige
import com.example.whatilike.ui.theme.UltraLightGrey
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
                                onDeleteClicked = { viewModel.deleteLikedArtwork(artwork.objectID) }
                            )
                        }
                    }
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
            border = BorderStroke(0.3.dp, Color.Black),
            shape = RoundedCornerShape(4.dp)
        )
        .pointerInput(Unit) {
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
                            model = artwork.primaryImage + "?w=1000&h=1000",
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
                    Image(
                        painter = rememberAsyncImagePainter(artwork.primaryImage + "?w=1000&h=1000"),
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

                    IconButton(
                        onClick = {
                            downloadArtwork(context, artwork.primaryImage!! + "?w=1000&h=1000")
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