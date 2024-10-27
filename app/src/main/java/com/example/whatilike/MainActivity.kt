package com.example.whatilike

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigationItem
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                println("TOKEN: $token")
//            } else {
//                println("NO TOKEN")
//            }
//        }

        createNotificationChannels()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            WhatilikeTheme {
                MainScreen()
            }
        }
    }

    private fun createNotificationChannels() {
        val channel1 = NotificationChannel(
            "channel1_id",
            "Channel 1",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "This is Channel 1"
        }

        val channel2 = NotificationChannel(
            "channel2_id",
            "Channel 2",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "This is Channel 2"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel1)
        manager.createNotificationChannel(channel2)
    }


    @Composable
    fun NavigationGraph(user: FirebaseUser?) {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                NavigationBar {
                    BottomNavigationItem(
                        icon = { null },
                        label = { Text("Home", fontFamily = FontFamily.Monospace) },
                        selected = false,
                        onClick = { navController.navigate("home") }
                    )
                    BottomNavigationItem(
                        icon = { null },
                        label = { Text("+", fontFamily = FontFamily.Monospace) },
                        selected = false,
                        onClick = { navController.navigate("+") }
                    )
                    BottomNavigationItem(
                        icon = { null },
                        label = { Text("Profile", fontFamily = FontFamily.Monospace) },
                        selected = false,
                        onClick = { navController.navigate("profile") }
                    )

                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
                composable("home") { HomeScreen(user, navController) }
                composable("+") { SettingsScreen() }
                composable("profile") { ProfileScreen() }
            }
        }
    }

    @Composable
    fun MainScreen() {
        var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }

        LaunchedEffect(Unit) {
            val user = auth.currentUser
            currentUser = user
        }

        if (currentUser != null) {
            NavigationGraph(currentUser)
        } else {
            AuthScreen(
                onSignInClick = { signInWithGoogle() },
                onSignUpClick = { signInWithGoogle() },
            )
        }
    }

    @Composable
    fun AuthScreen(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.dali),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
                        onClick = onSignUpClick, colors = ButtonColors(
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        auth.signOut()
    }


    @Composable
    fun HomeScreen(user: FirebaseUser?, navController: NavController) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Hello, ${user?.displayName ?: "User"}!", fontFamily = FontFamily.Monospace)
            Text(text = "Your email: ${user?.email}", fontFamily = FontFamily.Monospace)
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


    @Composable
    fun SettingsScreen() {
        Text(text = "Settings Screen")
    }

    @Composable
    fun ProfileScreen() {
        Text(text = "Profile Screen")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                } else {
                }
            }
        } catch (_: ApiException) {
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}