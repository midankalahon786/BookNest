package com.example.booknest.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.AppState
import com.example.booknest.data.AppUiEvent
import com.example.booknest.ui.theme.Purple40
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
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

// "Smart" Composable: Handles all state and logic
@Composable
fun VerifyOtpScreen(
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

    // ** ADDED THIS EFFECT TO NOTIFY THE USER **
    // This shows a toast when the code is sent successfully.
    LaunchedEffect(key1 = state.isOtpSent) {
        if (state.isOtpSent) {
            Toast.makeText(context, "OTP Sent!", Toast.LENGTH_SHORT).show()
            hotelViewModel.onEvent(AppUiEvent.ResetOtpSentFlag)
        }
    }

    LaunchedEffect(key1 = state.isVerificationSuccess) {
        if (state.isVerificationSuccess) {
            Toast.makeText(context, "Verification Successful!", Toast.LENGTH_SHORT).show()
            navController.navigate(AppRoutes.ROOM_SEARCH.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(key1 = state.resendTimer) {
        while (state.resendTimer > 0) {
            delay(1000)
            hotelViewModel.onEvent(AppUiEvent.DecrementResendTimer)
        }
    }

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            hotelViewModel.onEvent(AppUiEvent.ClearErrorMessage)
        }
    }

    val onResendOtp: () -> Unit = {
        hotelViewModel.onEvent(AppUiEvent.SendOtpClicked) // Show loader
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber("+91${state.phoneNumber}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    VerifyOtpScreenLayout(
        state = state,
        onOtpChange = { hotelViewModel.onEvent(AppUiEvent.OtpChanged(it)) },
        onVerifyClick = { hotelViewModel.onVerifyOtpManually(state.otp) },
        onResendClick = onResendOtp,
        onEditPhoneClick = { hotelViewModel.onEvent(AppUiEvent.ShowEditPhoneNumberDialog) }
    )

    if (state.showEditPhoneNumberDialog) {
        EditPhoneNumberDialog(
            currentPhoneNumber = state.phoneNumber,
            onDismiss = { hotelViewModel.onEvent(AppUiEvent.DismissEditPhoneNumberDialog) },
            onConfirm = { newNumber ->
                hotelViewModel.onEvent(AppUiEvent.PhoneNumberChanged(newNumber))
                hotelViewModel.onEvent(AppUiEvent.DismissEditPhoneNumberDialog)
                hotelViewModel.onEvent(AppUiEvent.SendOtpClicked) // Show loader
                val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber("+91${newNumber}")
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(context as Activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        )
    }
}

// "Dumb" Composable: Only displays UI
@Composable
fun VerifyOtpScreenLayout(
    state: AppState,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    onEditPhoneClick: () -> Unit
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
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Verify OTP", fontFamily = RobotoCondensed, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "OTP has been sent to you on your mobile number ${state.phoneNumber}, please enter it below",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                OtpTextField(
                    otpText = state.otp,
                    onOtpTextChange = { value, _ -> onOtpChange(value) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onVerifyClick,
                    // **FIXED**: Also disable the button while loading
                    enabled = state.otp.length == 6 && !state.isLoading,
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxWidth().size(height = 50.dp, width = 200.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brush = gradientBrush, shape = CutCornerShape(30)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Verify OTP", fontFamily = RobotoCondensedBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onResendClick,
                    enabled = state.resendTimer == 0,
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxWidth().size(height = 50.dp, width = 200.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brush = gradientBrush, shape = CutCornerShape(30)),
                        contentAlignment = Alignment.Center
                    ) {
                        val buttonText = if (state.resendTimer > 0) "Resend OTP in ${state.resendTimer}s" else "Resend OTP"
                        Text(buttonText, fontFamily = RobotoCondensedBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEditPhoneClick,
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxWidth().size(height = 50.dp, width = 200.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(color = Purple40, shape = CutCornerShape(30)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Edit Phone Number", fontFamily = RobotoCondensedBold)
                    }
                }
            }
        }

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyOtpScreenPreview() {
    VerifyOtpScreenLayout(
        state = AppState(phoneNumber = "1234567890", resendTimer = 30, isLoading = true),
        onOtpChange = {},
        onVerifyClick = {},
        onResendClick = {},
        onEditPhoneClick = {}
    )
}

@Composable
fun EditPhoneNumberDialog(
    currentPhoneNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPhoneNumber by remember { mutableStateOf(currentPhoneNumber) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Phone Number") },
        text = {
            OutlinedTextField(
                value = newPhoneNumber,
                onValueChange = { newPhoneNumber = it },
                label = { Text("New Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPhoneNumber) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    BasicTextField(
        modifier = modifier,
        value = otpText,
        onValueChange = {
            if (it.length <= otpCount && it.all { char -> char.isDigit() }) {
                onOtpTextChange(it, it.length == otpCount)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(otpCount) { index ->
                    val char = when {
                        index < otpText.length -> otpText[index].toString()
                        else -> ""
                    }
                    val isFocused = otpText.length == index
                    val borderWidth by animateDpAsState(
                        targetValue = if (isFocused) 2.dp else 1.dp,
                        animationSpec = tween(200)
                    )

                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(horizontal = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(
                            width = borderWidth,
                            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = char,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}
