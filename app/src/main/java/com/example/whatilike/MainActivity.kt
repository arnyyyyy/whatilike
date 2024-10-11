package com.example.whatilike

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

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

    @Composable
    fun MainScreen() {
        var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }


        LaunchedEffect(Unit) {
            val user = auth.currentUser
            currentUser = user
        }



        if (currentUser != null) {
            HomeScreen(currentUser)
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
    fun HomeScreen(user: FirebaseUser?) {
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
                    val user = auth.currentUser
                } else {
                }
            }
        } catch (e: ApiException) {
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
