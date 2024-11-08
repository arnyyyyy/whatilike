package com.example.whatilike.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun ProfileScreen(user: FirebaseUser?, signOut: () -> Unit) {
    var photoUrl by remember { mutableStateOf(user?.photoUrl?.toString()) }
    val storageRef = Firebase.storage.reference

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                uploadUserPhoto(it, user?.uid ?: UUID.randomUUID().toString()) { url ->
                    photoUrl = url
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello, ${user?.displayName ?: "User"}!",
            fontFamily = FontFamily.Monospace,
            color = Color.Black
        )
        Text(
            text = "Your email: ${user?.email}",
            fontFamily = FontFamily.Monospace,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUrl),
                contentDescription = "User Photo",
                modifier = Modifier.size(120.dp)
            )
        } else {
            Text(text = "No photo available", fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Upload Photo", fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(400.dp))

        Button(
            onClick = { signOut() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            shape = RectangleShape,
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(text = "Sign Out", fontFamily = FontFamily.Monospace)
        }
    }
}

fun uploadUserPhoto(uri: Uri, userId: String, onSuccess: (String) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.reference.child("user_photos/$userId.jpg")

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }
        }
        .addOnFailureListener {
        }
}
