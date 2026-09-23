package com.example.rentalwheels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalwheels.ui.LoginScreen
import com.example.rentalwheels.ui.MainAppScreen
import com.example.rentalwheels.ui.SignupScreen
import com.example.rentalwheels.ui.theme.RentalWheelsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RentalWheelsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: RentalViewModel = viewModel()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    if (!state.ready) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (state.loggedIn) {
                        MainAppScreen(state = state, viewModel = viewModel)
                    } else {
                        var isSignup by remember { mutableStateOf(false) }
                        var isGuestBrowsing by remember { mutableStateOf(false) }

                        if (isGuestBrowsing) {
                            MainAppScreen(state = state, viewModel = viewModel)
                        } else if (isSignup) {
                            SignupScreen(
                                state = state,
                                onSignup = { username, email, phone, password, role ->
                                    viewModel.signup(username, email, phone, password, role)
                                },
                                onBack = { isSignup = false }
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LoginScreen(
                                        state = state,
                                        onLogin = { username, password ->
                                            viewModel.login(username, password)
                                        },
                                        onOpenSignup = { isSignup = true },
                                        onSaveApi = { url -> viewModel.saveApiBase(url) }
                                    )
                                }
                                OutlinedButton(
                                    onClick = { isGuestBrowsing = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 24.dp)
                                ) {
                                    Text("Browse Vehicles as Guest")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
