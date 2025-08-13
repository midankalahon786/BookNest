package com.example.booknest.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.booknest.data.AppUiEvent
import com.example.booknest.data.AppState
import com.example.booknest.ui.theme.RobotoCondensed
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.lightBlue
import com.example.booknest.ui.theme.navyBlue
import com.example.booknest.viewmodel.HotelViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun SendOtpScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()
    val context = LocalContext.current

    val callbacks = remember(hotelViewModel) {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                hotelViewModel.onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                hotelViewModel.onVerificationFailed(e.localizedMessage ?: "An unknown error occurred")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                hotelViewModel.onCodeSent(verificationId)
            }
        }
    }

    LaunchedEffect(key1 = state.isOtpSent) {
        if (state.isOtpSent) {
            navController.navigate(AppRoutes.VERIFY_OTP.name)
            hotelViewModel.onEvent(AppUiEvent.ResetOtpSentFlag)
        }
    }

    SendOtpScreenLayout(
        state = state,
        onNameChange = { hotelViewModel.onEvent(AppUiEvent.NameChanged(it)) },
        onPhoneChange = { hotelViewModel.onEvent(AppUiEvent.PhoneNumberChanged(it)) },
        onEmailChange = { hotelViewModel.onEvent(AppUiEvent.EmailChanged(it)) },
        onSendOtpClick = {
            // Basic validation
            if (state.name.isBlank() || state.phoneNumber.isBlank() || state.email.isBlank()) {
                Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@SendOtpScreenLayout
            }

            // Set loading state to true
            hotelViewModel.onEvent(AppUiEvent.SendOtpClicked)

            // Start Firebase verification
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+91${state.phoneNumber}")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(context as Activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    )
}

// "Dumb" Composable: UI Layout only
@Composable
fun SendOtpScreenLayout(
    state: AppState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSendOtpClick: () -> Unit
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8EC5FC), Color(0xFFA861FA))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(lightBlue, navyBlue))),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sign Up",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = RobotoCondensed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush = Brush.radialGradient(colors = listOf(lightBlue, navyBlue)))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name", fontFamily = RobotoCondensed, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Person, "Full Name") },
                    colors = defaultTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number", fontFamily = RobotoCondensed, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Call, "Phone Number") },
                    colors = defaultTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email", fontFamily = RobotoCondensed, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Email, "Email") },
                    colors = defaultTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSendOtpClick,
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxWidth().size(height = 50.dp, width = 200.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = gradientBrush, shape = CutCornerShape(30)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Send OTP", fontFamily = RobotoCondensedBold)
                    }
                }
            }
        }

        // Loading Animation Overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun defaultTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedIndicatorColor = Color(0xFF6A1B9A),
        unfocusedIndicatorColor = Color.Gray,
        focusedLabelColor = Color(0xFF6A1B9A),
        cursorColor = Color(0xFF6A1B9A),
        unfocusedLabelColor = Color.Gray,
        unfocusedContainerColor = Color(0xFFF3E5F5),
        focusedContainerColor = Color.Transparent
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SendOtpScreenPreview() {
    SendOtpScreenLayout(
        state = AppState(isLoading = true), // Preview the loading state
        onNameChange = {},
        onPhoneChange = {},
        onEmailChange = {},
        onSendOtpClick = {}
    )
}
