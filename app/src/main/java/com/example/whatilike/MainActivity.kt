package com.example.whatilike

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatilike.cached.AppDatabase
import com.example.whatilike.cached.UserProfileViewModel
import com.example.whatilike.cached.UserProfileViewModelFactory
import com.example.whatilike.screens.GalleryScreen
import com.example.whatilike.ui.theme.WhatilikeTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.example.whatilike.screens.AuthScreen
import com.example.whatilike.screens.FavouritesScreen
import com.example.whatilike.screens.ProfileScreen
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentUser by mutableStateOf<FirebaseUser?>(null)

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser = firebaseAuth.currentUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener(authListener)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        val userProfileDao = AppDatabase.getInstance(this).userProfileDao()
        val firestore = FirebaseFirestore.getInstance()

        val viewModelFactory = UserProfileViewModelFactory(userProfileDao, firestore, this)
        val userProfileViewModel = ViewModelProvider(this, viewModelFactory)[UserProfileViewModel::class.java]


//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                println("FCM TOKEN: $token")
//            } else {
//                println("Failed to retrieve FCM token")
//            }
//        }

        createNotificationChannels()
        configureGoogleSignIn()

        setContent {
            WhatilikeTheme {
                MainScreen(userProfileViewModel = userProfileViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authListener)
    }

    private fun signOut() {
        auth.signOut()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                "channel1_id",
                "Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "This is Channel 1"
            },
            NotificationChannel(
                "channel2_id",
                "Channel 2",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "This is Channel 2"
            }
        )

        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }

    @Composable
    fun MainScreen(userProfileViewModel: UserProfileViewModel) {
        if (currentUser != null) {
            NavigationGraph(currentUser, userProfileViewModel)
        } else {
            AuthScreen(onSignInClick = { signInWithGoogle() })
        }
    }

    @Composable
    fun NavigationGraph(user: FirebaseUser?, userProfileViewModel: UserProfileViewModel) {
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
            NavHost(navController, startDestination = "favs", Modifier.padding(innerPadding)) {
                composable("favs") { FavouritesScreen(user = user) }
                composable("gallery") { GalleryScreen(user = user) }
                composable("me") { ProfileScreen(user, signOut = { signOut() }, userProfileViewModel = userProfileViewModel) }
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    println("Sign in successful")
                } else {
                    println("Sign in failed: ${signInTask.exception?.message}")
                }
            }
        } catch (e: ApiException) {
            println("Google sign-in failed: ${e.message}")
        }
    }
}


