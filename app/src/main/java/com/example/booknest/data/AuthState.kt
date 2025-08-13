package com.example.booknest.data

data class AuthState(
    val phoneNumber: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val errorMessage: String? = null,
    val isVerificationSuccess: Boolean = false
)
