package com.example.booknest.data

import androidx.annotation.Keep

// Enum for splash screen outcomes
enum class AuthStatus {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED
}
enum class HotelStatus {
    AVAILABLE,
    SOLD_OUT

}

// Data models for booking

@Keep // Prevents R8/ProGuard from removing the class during minification
data class Hotel(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val status: HotelStatus = HotelStatus.AVAILABLE,
    val priceRange: String? = null,
    val rating: Double = 0.0,
    val about: String = "",
    val amenities: List<String> = emptyList(),
    val propertyRules: List<String> = emptyList()
)

data class Amenity(
    val name: String,
    val iconResourceId: Int // You'll need to add these drawable resources
)

@Keep
data class Room(
    val id: String = "",
    val hotelId: String = "",
    val type: String = "",
    val price: Double = 0.0,
    val imageUrl: String = ""
)
@Keep
data class Place(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val description: String = "" // Add description field
)

@Keep
data class PlaceDetails(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = ""
)



data class AppState(
    val authStatus: AuthStatus = AuthStatus.LOADING,

    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val otp: String = "",
    val isOtpSent: Boolean = false,
    val isVerificationSuccess: Boolean = false,
    val resendTimer: Int = 0,
    val showEditPhoneNumberDialog: Boolean = false,

    val hotels: List<Hotel> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val selectedHotel: Hotel? = null,
    val selectedRoom: Room? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val destination: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val numberOfRooms: String = "",
    val bestPlaces: List<Place> = emptyList(),
    val selectedPlaceDetails: PlaceDetails? = null,
)