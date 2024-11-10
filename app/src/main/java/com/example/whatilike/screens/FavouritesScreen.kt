package com.example.whatilike.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.whatilike.data.ArtObject
import com.example.whatilike.repository.ArtRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun FavouritesScreen(user: FirebaseUser?,) {
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
                Text(text = "No liked artworks", fontFamily = FontFamily.Monospace, color = Color.Black)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(likedArtworks) { artwork ->
                    LikedArtworkCard(artwork)
                }
            }
        }
    }
}

@Composable
fun LikedArtworkCard(artwork: ArtObject) {
    Card(
        modifier = Modifier
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
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
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
