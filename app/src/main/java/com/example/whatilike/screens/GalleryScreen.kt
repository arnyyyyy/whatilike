package com.example.whatilike.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.whatilike.data.ArtObject
import com.example.whatilike.data.ArtViewModel

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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(artworks) { artwork ->
                ArtworkItem(artwork)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}

@Composable
fun ArtworkItem(artwork: ArtObject) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter(data = artwork.primaryImage),
            contentDescription = artwork.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artwork.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = artwork.artistDisplayName,
            fontSize = 16.sp
        )
    }
}

