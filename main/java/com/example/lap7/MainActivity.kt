package com.example.lap7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setContent {
            var currentScreen by remember {
                mutableStateOf(if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) "MAIN" else "AUTH")
            }

            MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        "AUTH" -> {
                            AuthScreen(onAuthSuccess = {
                                currentScreen = "MAIN"
                            })
                        }
                        "MAIN" -> {
                            SinhVienScreen(
                                onLogout = {
                                    auth.signOut()
                                    currentScreen = "AUTH"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
