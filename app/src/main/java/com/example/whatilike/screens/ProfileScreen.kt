package com.example.whatilike.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun ProfileScreen(user: FirebaseUser?, signOut: () -> Unit) {
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var nickname by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    val firestore = Firebase.firestore

    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    photoUrl = document.getString("photoUrl") ?: user.photoUrl?.toString()
                    nickname = document.getString("nickname") ?: user.displayName
                }
                .addOnFailureListener {
                    photoUrl = user.photoUrl?.toString()
                    nickname = user.displayName
                }
        }
    }

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
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Hello, ${nickname ?: "User"}!",
            fontFamily = FontFamily.Monospace,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.size(240.dp)
            )
        } else {
            Text(text = "No photo available", fontFamily = FontFamily.Monospace)
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(text = "Upload Photo", fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickname ?: "",
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.End) {
            Box() {}
            IconButton(
                onClick = {
                    isEditing = !isEditing
                    user?.uid?.let { userId ->
                        updateNickname(userId, nickname.orEmpty()) { updatedName ->
                            nickname = updatedName
                        }
                    }
                },
                modifier = Modifier.clip(RectangleShape)
            ) {
                Icon(
                    tint = Color.Black,
                    imageVector = if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit"
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))


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

fun updateNickname(userId: String, nickname: String, onSuccess: (String) -> Unit) {
    val firestore = Firebase.firestore
    firestore.collection("users").document(userId)
        .update("nickname", nickname)
        .addOnSuccessListener {
            onSuccess(nickname)
        }
        .addOnFailureListener {
        }
}


fun uploadUserPhoto(uri: Uri, userId: String, onSuccess: (String) -> Unit) {
    val storage = Firebase.storage
    val firestore = Firebase.firestore
    val storageRef = storage.reference.child("user_photos/$userId.jpg")

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val photoUrl = downloadUri.toString()
                firestore.collection("users").document(userId)
                    .set(mapOf("photoUrl" to photoUrl))
                    .addOnSuccessListener {
                        onSuccess(photoUrl)
                    }
            }
        }
        .addOnFailureListener {
        }
}
