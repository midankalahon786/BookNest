package com.example.booknest.data

data class HotelListState(
    val hotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
