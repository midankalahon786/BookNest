package com.example.booknest.data

import com.google.firebase.auth.PhoneAuthCredential

sealed interface AppUiEvent {
    data class NameChanged(val name: String) : AppUiEvent
    data class EmailChanged(val email: String) : AppUiEvent
    data class PhoneNumberChanged(val number: String) : AppUiEvent
    data class OtpChanged(val code: String) : AppUiEvent
    object SendOtpClicked : AppUiEvent
    data object LogoutClicked : AppUiEvent
    data class VerifyOtpWithCredential(val credential: PhoneAuthCredential) : AppUiEvent
    object ClearErrorMessage : AppUiEvent
    object ResetOtpSentFlag : AppUiEvent


    object DecrementResendTimer : AppUiEvent
    object ShowEditPhoneNumberDialog : AppUiEvent
    object DismissEditPhoneNumberDialog : AppUiEvent

    object FetchHotels : AppUiEvent
    data class HotelSelected(val hotel: Hotel) : AppUiEvent
    data class RoomSelected(val room: Room) : AppUiEvent
    object ClearHotelSelection : AppUiEvent // To go back from room list

    data class DestinationChanged(val destination: String) : AppUiEvent
    data class CheckInDateChanged(val date: String) : AppUiEvent
    data class CheckOutDateChanged(val date: String) : AppUiEvent
    data class NumberOfRoomsChanged(val count: String) : AppUiEvent
    object SearchClicked : AppUiEvent
    object FetchBestPlaces : AppUiEvent
    data class FetchPlaceDetailsById(val id: String) : AppUiEvent
}