package com.example.whatilike.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatilike.cached.user.FolderViewModel
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.cached.user.UserProfileViewModel
import com.example.whatilike.data.ArtViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun NavigationGraph(
    user: FirebaseUser?,
    userProfileViewModel: UserProfileViewModel,
    artViewModel: ArtViewModel,
    foldersViewModel : FolderViewModel,
    likedArtworksViewModel: LikedArtworksViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("Favs", fontFamily = FontFamily.Monospace) },
                    selected = false,
                    onClick = { navController.navigate("favs") }
                )
                NavigationBarItem(
                    icon = { Text("Gallery", fontFamily = FontFamily.Monospace) },
                    selected = false,
                    onClick = { navController.navigate("gallery") }
                )
                NavigationBarItem(
                    icon = { Text("Me", fontFamily = FontFamily.Monospace) },
                    selected = false,
                    onClick = { navController.navigate("me") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "gallery", Modifier.padding(innerPadding)) {
            composable("favs") { FavouritesScreen(viewModel = likedArtworksViewModel, foldersViewModel = foldersViewModel) }
            composable("gallery") { GalleryScreen(user = user, artViewModel = artViewModel, likedViewModel = likedArtworksViewModel) }
            composable("me") {
                ProfileScreen(
                    user = user,
                    signOut = onSignOut,
                    userProfileViewModel = userProfileViewModel
                )
            }
        }
    }
}