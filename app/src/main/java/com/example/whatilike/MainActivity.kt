package com.example.whatilike

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
            MaterialTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }
        var showDialog by remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            val user = auth.currentUser
            currentUser = user
        }


        if (currentUser != null) {
            HomeScreen(currentUser)
        } else {
            SignInScreen(
                onSignInClick = { signInWithGoogle() },
                onSignUpClick = { showDialog = true }
            )
        }

        if (showDialog) {
            SignUpDialog(onDismiss = { showDialog = false })
        }
    }

    @Composable
    fun SignInScreen(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome! Please Sign In")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSignInClick) {
                Text(text = "Sign in with Google")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSignUpClick) {
                Text(text = "Sign Up")
            }
        }
    }

    @Composable
    fun SignUpDialog(onDismiss: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Register") },
            text = {
                Column {
                    TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                    TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                Button(onClick = {
                    createAccount(email, password)
                    onDismiss()
                }) {
                    Text("Register")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                } else {
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
            Text(text = "Hello, ${user?.displayName ?: "User"}!")
            Text(text = "Your email: ${user?.email}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { signOut() }) {
                Text(text = "Sign Out")
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
