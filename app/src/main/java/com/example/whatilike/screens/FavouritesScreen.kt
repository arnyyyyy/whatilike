package com.example.whatilike.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun FavouritesScreen() {
      Column {
            Spacer(modifier = Modifier.height(300.dp))
            Text(
                  text = "Favourites",
                  fontFamily = FontFamily.Monospace,
                  color = Color.Black
            )
      }

}