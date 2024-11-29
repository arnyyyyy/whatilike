package com.example.whatilike.screens

import android.graphics.Bitmap

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.whatilike.cached.user.UserProfileViewModel

@Composable
fun ProfileScreen(user: FirebaseUser?, signOut: () -> Unit,
    userProfileViewModel: UserProfileViewModel
) {
    val userProfile by userProfileViewModel.currentUserProfile.collectAsState()
    val isLoading by userProfileViewModel.isLoading.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    var nickname by remember { mutableStateOf(userProfile?.nickname.orEmpty()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                user?.uid?.let { userId ->
                    userProfileViewModel.uploadUserPhoto(userId, it)
                }
            }
        }
    )

    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            userProfileViewModel.loadUserProfile(userId)
            bitmap = userProfileViewModel.getLocalImage(userId)

        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Text(text = "Loading...", fontFamily = FontFamily.Monospace)
        } else {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Hello, ${userProfile?.nickname ?: "User"}!",
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
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "User Photo",
                    modifier = Modifier.size(240.dp)
                )
            } else if (userProfile?.photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(userProfile?.photoUrl),
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
                    value = nickname,
                    onValueChange = { newNickname ->
                        nickname = newNickname
                    },
                    label = { Text("Nickname") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.Black,
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            userProfile?.uid?.let { userId ->
                                userProfileViewModel.updateNickname(userId, nickname)
                            }
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.End) {
                Box {}
                IconButton(
                    onClick = {
                        isEditing = !isEditing
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
}
