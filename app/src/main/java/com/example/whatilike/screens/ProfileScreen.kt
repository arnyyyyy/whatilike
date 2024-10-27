package com.example.whatilike.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(user: FirebaseUser?, signOut : () -> Unit) {
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
              Spacer(modifier = Modifier.height(400.dp))
              Button(
                     onClick = { signOut() },
                     colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.White
                     ),
                     shape = RectangleShape,
                     border = BorderStroke(1.dp, Color.Black)

              ) {
                     Text(text = "Sign Out", fontFamily = FontFamily.Monospace)
              }
       }
}
