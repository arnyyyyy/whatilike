package com.example.whatilike

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.rememberImagePainter
import com.example.whatilike.api.ArtObject
import com.example.whatilike.repository.ArtRepository
import kotlinx.coroutines.launch

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
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authListener)
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
    fun MainScreen() {
        if (currentUser != null) {
            NavigationGraph(currentUser)
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

    @Composable
    fun AuthScreen(onSignInClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.dali),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(120.dp))
                Text(
                    text = getString(R.string.auth_query),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                )
                Spacer(modifier = Modifier.height(380.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Button(
                        onClick = onSignInClick, colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(text = "Sign In", fontFamily = FontFamily.Monospace)
                    }

                    Text(
                        text = "|",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(10.dp)
                    )

                    Button(
                        onClick = onSignInClick, colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(text = "Sign Up", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    @Composable
    fun NavigationGraph(user: FirebaseUser?) {
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
                composable("favs") { HomeScreen(user, navController) }
                composable("gallery") { SettingsScreen() }
                composable("me") { ProfileScreen() }
            }
        }
    }

    @Composable
    fun HomeScreen(user: FirebaseUser?, navController: NavController) {
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

    private fun signOut() {
        auth.signOut()
    }

    @Composable
    fun SettingsScreen() {
//        Text(text = "Settings Screen", color = Color.Black)
    }

    @Composable
    fun ProfileScreen() {
//        Text(text = "Profile Screen", color = Color.Black)
        ArtScreen()
    }


    class ArtViewModel : ViewModel() {
        private val repository = ArtRepository()
        private val _artworks = mutableStateOf<List<ArtObject>>(emptyList())
        val artworks: State<List<ArtObject>> = _artworks

        fun loadRandomArtworks(count: Int) {
            viewModelScope.launch {
                try {
                    val result = repository.getRandomArtworks(count)
                    _artworks.value = result
                    Log.d("ArtViewModel", "Number of artworks loaded: ${result.size}")
                } catch (e: Exception) {
                    Log.e("ArtViewModel", "Failed to load artworks", e)
                }
            }
        }
    }

//    @Composable
//    fun ArtScreen(viewModel: ArtViewModel = ArtViewModel()) {
//        val artworks by viewModel.artworks
//
////        LaunchedEffect(Unit) {
//            viewModel.loadRandomArtworks(5)
////        }
//
//        if (artworks.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = "No artworks found", fontSize = 18.sp, color = Color.Black)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                items(artworks) { artwork ->
//                    ArtworkItem(artwork)
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(text = "found", fontSize = 18.sp, color = Color.Black)
//                }
//            }
//        }
//    }

    @Composable
    fun ArtScreen(viewModel: ArtViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        val artworks by viewModel.artworks

        // Загружаем данные только при первом запуске
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
                    .height(200.dp)
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

}


