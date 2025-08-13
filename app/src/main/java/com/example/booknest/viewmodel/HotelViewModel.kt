package com.example.booknest.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HotelViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    private var storedVerificationId: String? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://booknest-b44fe-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    init {
        checkUserAuthentication()
        onEvent(AppUiEvent.FetchHotels)
        onEvent(AppUiEvent.FetchBestPlaces)
    }

    fun onEvent(event: AppUiEvent) {
        when (event) {
            // Auth Events
            is AppUiEvent.NameChanged -> _state.update { it.copy(name = event.name) }
            is AppUiEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is AppUiEvent.PhoneNumberChanged -> _state.update { it.copy(phoneNumber = event.number) }
            is AppUiEvent.OtpChanged -> _state.update { it.copy(otp = event.code) }
            is AppUiEvent.SendOtpClicked -> sendOtp()
            is AppUiEvent.DecrementResendTimer -> _state.update { it.copy(resendTimer = it.resendTimer - 1) }
            is AppUiEvent.ShowEditPhoneNumberDialog -> _state.update { it.copy(showEditPhoneNumberDialog = true) }
            is AppUiEvent.DismissEditPhoneNumberDialog -> _state.update { it.copy(showEditPhoneNumberDialog = false) }
            is AppUiEvent.ClearErrorMessage -> _state.update { it.copy(errorMessage = null) }
            is AppUiEvent.ResetOtpSentFlag -> _state.update { it.copy(isOtpSent = false) }
            is AppUiEvent.LogoutClicked -> logout()

            // Booking Events
            is AppUiEvent.FetchHotels -> fetchHotels()
            is AppUiEvent.HotelSelected -> fetchRoomsForHotel(event.hotel)
            is AppUiEvent.RoomSelected -> {
                val currentSelection = state.value.selectedRoom

                val newSelection = if (currentSelection?.id == event.room.id) {
                    null
                } else {
                    event.room
                }

                _state.update { it.copy(selectedRoom = newSelection) }
            }
            is AppUiEvent.ClearHotelSelection -> _state.update {
                it.copy(selectedHotel = null, rooms = emptyList(), selectedRoom = null)
            }

            is AppUiEvent.DestinationChanged -> _state.update { it.copy(destination = event.destination) }
            is AppUiEvent.CheckInDateChanged -> _state.update { it.copy(checkInDate = event.date) }
            is AppUiEvent.CheckOutDateChanged -> _state.update { it.copy(checkOutDate = event.date) }
            is AppUiEvent.NumberOfRoomsChanged -> _state.update { it.copy(numberOfRooms = event.count) }
            is AppUiEvent.SearchClicked -> searchHotels()
            is AppUiEvent.FetchBestPlaces -> fetchBestPlaces()
            is AppUiEvent.FetchPlaceDetailsById -> fetchPlaceDetailsById(event.id)

            is AppUiEvent.VerifyOtpWithCredential -> verifyOtp(event.credential)
        }
    }

    // --- Private Functions for Logic ---

    private fun checkUserAuthentication() {
        viewModelScope.launch {
            _state.update { it.copy(authStatus = AuthStatus.LOADING) }
            delay(2500) // Simulate checking Firebase Auth
            val isLoggedIn = false // Dummy value
            _state.update {
                it.copy(authStatus = if (isLoggedIn) AuthStatus.AUTHENTICATED else AuthStatus.UNAUTHENTICATED)
            }
        }
    }

    private fun sendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isOtpSent = true, resendTimer = 60) } // Start
            // the timer
            // here
            println("Sending dummy OTP to ${state.value.phoneNumber}...")
            delay(1000)
            val dummyOtp = "123456"
            _state.update {
                it.copy(
                    name = state.value.name,
                    email = state.value.email,
                    phoneNumber = state.value.phoneNumber,
                    isLoading = false,
                    isOtpSent = true,
                    otp = dummyOtp,
                    errorMessage = null
                )
            }
        }
    }

    fun onVerificationCompleted(credential: PhoneAuthCredential) {
        _state.update { it.copy(isLoading = false, isVerificationSuccess = true, otp = credential.smsCode ?: "") }
        // You can also sign in the user directly here if needed
        // auth.signInWithCredential(credential).addOnCompleteListener { ... }
    }
    fun onVerificationFailed(message: String) {
        _state.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun onCodeSent(verificationId: String) {
        storedVerificationId = verificationId
        _state.update { it.copy(isLoading = false, resendTimer = 60) }
    }

    private fun verifyOtp(credential: PhoneAuthCredential) {
        _state.update { it.copy(isLoading = true) }

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isVerificationSuccess = true,
                            errorMessage = null
                        )
                    }
                } else {
                    // Sign-in failed
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = task.exception?.message ?: "Invalid OTP."
                        )
                    }
                }
            }
    }


    // We need a new event for this logic
    fun onVerifyOtpManually(otp: String) {
        if (storedVerificationId != null) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
            onEvent(AppUiEvent.VerifyOtpWithCredential(credential))
        } else {
            _state.update { it.copy(errorMessage = "Verification process not started.") }
        }
    }

    private fun fetchHotels() {
        _state.update { it.copy(isLoading = true) }
        database.child("hotels").get().addOnSuccessListener { dataSnapshot ->
            val hotels = dataSnapshot.children.mapNotNull { it.getValue(Hotel::class.java) }
            // 2. Log success
            Log.d("DATABASE", "fetchHotels: Success - Fetched ${hotels.size} hotels")
            _state.update { it.copy(isLoading = false, hotels = hotels) }
        }.addOnFailureListener { exception ->
            // 3. Log failure
            Log.e("DATABASE", "fetchHotels: Failure", exception)
            _state.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
        }
    }
    // In HotelViewModel.kt

    private fun logout() {
        viewModelScope.launch {
            // 1. IMPORTANT: Clear any persisted user data.
            // This is where you would clear a user token from DataStore or SharedPreferences.
            // e.g., userRepository.clearUserSession()

            // 2. Reset the entire app state to its initial default values.
            // This ensures the UI reflects the logged-out state immediately.
            _state.update { AppState() }

        }
    }

    private fun fetchRoomsForHotel(hotel: Hotel) {
        _state.update { it.copy(isLoading = true, selectedHotel = hotel, rooms = emptyList()) }
        database.child("rooms").child(hotel.id).get().addOnSuccessListener { dataSnapshot ->
            val rooms = dataSnapshot.children.mapNotNull { it.getValue(Room::class.java) }
            Log.d("DATABASE", "fetchRoomsForHotel: Success - Fetched ${rooms.size} rooms for " +
                    "hotel" + " ${hotel.id}")
            _state.update { it.copy(isLoading = false, rooms = rooms) }
        }.addOnFailureListener { exception ->
            Log.e("DATABASE", "fetchRoomsForHotel: Failure for hotel ${hotel.id}", exception)
            _state.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
        }
    }

    private fun fetchBestPlaces() {
        _state.update { it.copy(isLoading = true) }
        database.child("places").get().addOnSuccessListener { dataSnapshot ->
            val places = dataSnapshot.children.mapNotNull { it.getValue(Place::class.java) }
            Log.d("DATABASE", "fetchBestPlaces: Success - Fetched ${places.size} places")
            _state.update { it.copy(isLoading = false, bestPlaces = places) }
        }.addOnFailureListener { exception ->
            Log.e("DATABASE", "fetchBestPlaces: Failure", exception)
            _state.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
        }
    }

    private fun searchHotels() {
        val currentState = state.value

        // 1. Add validation to prevent searching without a destination
        if (currentState.destination.isBlank()) {
            Log.w("SearchHotel", "searchHotels: Aborted. Destination is blank.")
            // You can optionally show a message to the user
            _state.update { it.copy(errorMessage = "Please select a destination.") }
            return // Stop the function here
        }

        _state.update { it.copy(isLoading = true) }
        Log.d("SearchHotel", "searchHotels: Searching for hotels in '${currentState.destination}'")

        // This query fetches only the hotels that match the selected location.
        database.child("hotels").orderByChild("location").equalTo(currentState.destination)
            .get().addOnSuccessListener { dataSnapshot ->
                val hotels = dataSnapshot.children.mapNotNull { it.getValue(Hotel::class.java) }
                Log.d("SearchHotel", "searchHotels: Success - Found ${hotels.size} hotels for " +
                        "'${currentState.destination}'")

                _state.update { it.copy(isLoading = false, hotels = hotels) }

            }.addOnFailureListener { exception ->
                Log.e("SearchHotel", "searchHotels: Failure for destination '${currentState.destination}'", exception)
                _state.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
            }
    }

    private fun fetchPlaceDetailsById(id: String) {
        _state.update { it.copy(isLoading = true, selectedPlaceDetails = null) }
        database.child("places").child(id).get().addOnSuccessListener { dataSnapshot ->
            val place = dataSnapshot.getValue(Place::class.java)
            if (place != null) {
                Log.d("DATABASE", "fetchPlaceDetailsById: Success - Found place with id $id")
                val details = PlaceDetails(
                    id = place.id,
                    name = place.name,
                    description = place.description,
                    imageUrl = place.imageUrl
                )
                _state.update { it.copy(isLoading = false, selectedPlaceDetails = details) }
            } else {
                Log.w("DATABASE", "fetchPlaceDetailsById: Warning - Place with id $id not found")
                _state.update { it.copy(isLoading = false, errorMessage = "Place not found.") }
            }
        }.addOnFailureListener { exception ->
            Log.e("DATABASE", "fetchPlaceDetailsById: Failure for id $id", exception)
            _state.update { it.copy(isLoading = false, errorMessage = exception.localizedMessage) }
        }
    }

}