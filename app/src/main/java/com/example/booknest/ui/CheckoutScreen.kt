package com.example.booknest.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.AppState
import com.example.booknest.data.Hotel
import com.example.booknest.data.HotelStatus
import com.example.booknest.data.Room
import com.example.booknest.ui.theme.RobotoCondensed
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()

    CheckoutScreenLayout(
        state = state,
        onBackClick = { navController.popBackStack() },
        onConfirmClick = {
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreenLayout(
    state: AppState,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val hotel = state.selectedHotel
    val room = state.selectedRoom
    val user = state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontFamily = WorkSansBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Confirm Your Booking", style = MaterialTheme.typography.headlineMedium,
                fontFamily = RobotoCondensedBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hotel != null && room != null) {
                        BookingDetailRow("Hotel", hotel.name)
                        BookingDetailRow("Room", room.type)
                        BookingDetailRow("Check-in", state.checkInDate)
                        BookingDetailRow("Check-out", state.checkOutDate)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        BookingDetailRow("Name", user.name)
                        BookingDetailRow("Email", user.email)
                        BookingDetailRow("Phone", user.phoneNumber)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        BookingDetailRow("Room Price", "Rs. ${room.price}")
                        BookingDetailRow("To Pay", "Rs. ${room.price}", isTotal = true)
                    } else {
                        Text("Booking details not available.")
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onConfirmClick,
                colors = ButtonColors(
                    containerColor = blue,
                    contentColor = Color.White,
                    disabledContentColor = Color.Gray,
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm & Pay", fontFamily = RobotoCondensed)
            }
        }
    }
}

@Composable
fun BookingDetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontFamily = RobotoCondensed)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showSystemUi = true)
@Composable
fun CheckoutScreenPreview() {
    // Create dummy data for the preview
    val dummyState = AppState(
        name = "John Doe",
        email = "john.doe@example.com",
        phoneNumber = "9876543210",
        checkInDate = "14 Aug 2025",
        checkOutDate = "16 Aug 2025",
        selectedHotel = Hotel(id = "h1", name = "Fairfield Hotel", location = "Agra", status = HotelStatus.AVAILABLE),
        selectedRoom = Room(id = "r1", hotelId = "h1", type = "Deluxe Room", price = 5000.0)
    )
    CheckoutScreenLayout(
        state = dummyState,
        onBackClick = {},
        onConfirmClick = {}
    )
}
