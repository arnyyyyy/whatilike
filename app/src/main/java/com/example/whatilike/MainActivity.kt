package com.example.whatilike

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.example.whatilike.cached.user.ArtworkFoldersDatabase
import com.example.whatilike.cached.user.FolderViewModel
import com.example.whatilike.cached.user.FolderViewModelFactory
import com.example.whatilike.cached.user.LikedArtworksDatabase
import com.example.whatilike.cached.user.LikedArtworksViewModel
import com.example.whatilike.cached.user.LikedArtworksViewModelFactory
import com.example.whatilike.cached.user.UserDatabase
import com.example.whatilike.cached.user.UserProfileViewModel
import com.example.whatilike.cached.user.UserProfileViewModelFactory
import com.example.whatilike.data.ArtViewModel
import com.example.whatilike.data.ArtViewModelFactory
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
import com.example.whatilike.screens.NavigationGraph
import com.example.whatilike.screens.SplashScreen
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

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

        val models = initializeViewModels()
        val userProfileViewModel :UserProfileViewModel = models["userProfile"] as UserProfileViewModel
        val artViewModel : ArtViewModel =  models["art"] as ArtViewModel

        val likedArtViewModel : LikedArtworksViewModel = models["liked"] as LikedArtworksViewModel
        val foldersViewModel : FolderViewModel = models["folders"] as FolderViewModel


//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                println("FCM TOKEN: $token")
//            } else {
//                println("Failed to retrieve FCM token")
//            }
//        }

//        createNotificationChannels()
        configureGoogleSignIn()

        setContent {
            WhatilikeTheme {
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    artViewModel.loadRandomArtworks(20)
                    likedArtViewModel.loadLikedArtworks()
                    delay(4000)
                    isLoading = false
                }

                SplashScreen(
                    isLoading = isLoading,
                    onLoadingComplete = {
                        MainScreen(
                            userProfileViewModel = userProfileViewModel,
                            artViewModel = artViewModel,
                            likedArtworksViewModel = likedArtViewModel,
                            foldersViewModel = foldersViewModel
                        )
                    }
                )
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

    private fun initializeViewModels(): Map<String, Any> {
        val userProfileDao = UserDatabase.getInstance(this).userProfileDao()
        val userViewModelFactory =
            UserProfileViewModelFactory(userProfileDao = userProfileDao, FirebaseFirestore.getInstance(), this)
        val userProfileViewModel =
            ViewModelProvider(this, userViewModelFactory)[UserProfileViewModel::class.java]

        val likedArtworksDao = LikedArtworksDatabase.getInstance(this).likedArtworks()
        val likedArtworksViewModelFactory =
            LikedArtworksViewModelFactory(likedArtworksDao, FirebaseFirestore.getInstance(), this)
        val likedArtworksViewModel =
            ViewModelProvider(this, likedArtworksViewModelFactory)[LikedArtworksViewModel::class.java]

        val foldersDao = ArtworkFoldersDatabase.getInstance(this).folders()
        val foldersViewModelFactory =
            FolderViewModelFactory(foldersDao, FirebaseFirestore.getInstance(), this)
        val folderViewModel =
            ViewModelProvider(this, foldersViewModelFactory)[FolderViewModel::class.java]

        val artworkViewModelFactory = ArtViewModelFactory(context = this)
        val artViewModel =
            ViewModelProvider(this, artworkViewModelFactory)[ArtViewModel::class.java]


        return mapOf(
            "userProfile" to userProfileViewModel,
            "art" to artViewModel,
            "liked" to likedArtworksViewModel,
            "folders" to folderViewModel
        )
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

//    private fun createNotificationChannels() {
//        val channels = listOf(
//            NotificationChannel(
//                "channel1_id",
//                "Channel 1",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "This is Channel 1"
//            },
//            NotificationChannel(
//                "channel2_id",
//                "Channel 2",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "This is Channel 2"
//            }
//        )
//
//        val manager = getSystemService(NotificationManager::class.java)
//        channels.forEach { manager.createNotificationChannel(it) }
//    }

    @Composable
    fun MainScreen(userProfileViewModel: UserProfileViewModel, artViewModel: ArtViewModel, likedArtworksViewModel: LikedArtworksViewModel, foldersViewModel : FolderViewModel) {
        if (currentUser != null) {
            userProfileViewModel.initializeUserProfileFromFirebase()
            NavigationGraph(currentUser, userProfileViewModel, artViewModel, likedArtworksViewModel =likedArtworksViewModel, foldersViewModel = foldersViewModel) { signOut() }
        } else {
            AuthScreen(onSignInClick = { signInWithGoogle() })
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


