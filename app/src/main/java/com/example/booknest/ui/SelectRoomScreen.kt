package com.example.booknest.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.booknest.R
import com.example.booknest.data.AppUiEvent
import com.example.booknest.data.AppState
import com.example.booknest.data.Hotel
import com.example.booknest.data.HotelStatus
import com.example.booknest.data.Room
import com.example.booknest.ui.theme.RobotoCondensed
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSans
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectRoomScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()

    SelectRoomScreenLayout(
        state = state,
        onBackClick = {
            hotelViewModel.onEvent(AppUiEvent.ClearHotelSelection)
            navController.popBackStack()
        },
        onLogoutClick = {
            hotelViewModel.onEvent(AppUiEvent.LogoutClicked)
            navController.navigate(AppRoutes.SEND_OTP.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        },
        onRoomSelect = { room ->
            hotelViewModel.onEvent(AppUiEvent.RoomSelected(room))
        },
        onCheckoutClick = {
            navController.navigate(AppRoutes.CHECKOUT.name)
        }
    )
}

// "Dumb" Composable: UI Layout only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectRoomScreenLayout(
    state: AppState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRoomSelect: (Room) -> Unit,
    onCheckoutClick: () -> Unit
) {
    val selectedHotel = state.selectedHotel
    val selectedRoom = state.selectedRoom

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Room", fontFamily = WorkSansBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Icon(painter = painterResource(R.drawable.baseline_logout_24),
                            contentDescription = "Logout", tint = blue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontFamily = WorkSansBold, color = blue)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (selectedHotel == null) {
                Text("No hotel selected. Please go back.", fontFamily = RobotoCondensedBold)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        HotelDetailsHeader(hotel = selectedHotel)
                    }

                    item {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("Select a room", style = MaterialTheme.typography.titleLarge,
                                fontFamily = RobotoCondensedBold)
                        }
                    }

                    // **LOGIC FOR SOLD OUT**
                    if (selectedHotel.status == HotelStatus.SOLD_OUT) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "SOLD OUT",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.Red.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RobotoCondensedBold
                                )
                            }
                        }
                    } else {
                        items(state.rooms) { room ->
                            RoomListItem(
                                room = room,
                                isSelected = state.selectedRoom?.id == room.id,
                                onSelect = { onRoomSelect(room) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                if (selectedRoom != null) {
                    Button(
                        onClick = onCheckoutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blue,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text("Checkout", fontFamily = RobotoCondensed)
                    }
                }
            }
        }
    }
}

@Composable
fun HotelDetailsHeader(hotel: Hotel) {
    Column {
        AsyncImage(
            model = hotel.imageUrl,
            contentDescription = hotel.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.booknest),
            error = painterResource(id = R.drawable.booknest)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hotel.name, style = MaterialTheme.typography.headlineMedium, fontFamily = RobotoCondensedBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hotel.rating.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text("About the hotel", style = MaterialTheme.typography.titleLarge, fontFamily = WorkSans)
            Text(hotel.about, style = MaterialTheme.typography.bodyMedium, fontFamily = RobotoCondensed)

            Text("Amenities available", style = MaterialTheme.typography.titleLarge, fontFamily = WorkSans)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                hotel.amenities.forEach { amenityName ->
                    AmenityItem(name = amenityName)
                }
            }

            Text("Property Rules & Information", style = MaterialTheme.typography.titleLarge,
                fontFamily = RobotoCondensedBold)
            hotel.propertyRules.forEach { rule ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(rule, style = MaterialTheme.typography.bodySmall, fontFamily = RobotoCondensed)
                }
            }
        }
    }
}

@Composable
fun AmenityItem(name: String) {
    val icon: Any = when (name.lowercase()) {
        "gym" -> Icons.Default.Star
        "free parking" -> painterResource(R.drawable.baseline_local_parking_24)
        "restaurant" -> painterResource(R.drawable.baseline_restaurant_24)
        "wifi" -> painterResource(R.drawable.baseline_wifi_24)
        else -> Icons.Default.Check
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (icon) {
            is ImageVector -> {
                Icon(imageVector = icon, contentDescription = name)
            }
            is androidx.compose.ui.graphics.painter.Painter -> {
                Icon(painter = icon, contentDescription = name)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 12.sp, fontFamily = RobotoCondensedBold)
    }
}


@Composable
fun RoomListItem(room: Room, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = room.imageUrl,
                contentDescription = room.type,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.booknest),
                error = painterResource(id = R.drawable.booknest)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(room.type, style = MaterialTheme.typography.titleMedium, fontFamily = RobotoCondensedBold)
                Text("Rs. ${room.price} / night", style = MaterialTheme.typography.bodyMedium,
                    fontFamily = RobotoCondensedBold)
            }
            Button(onClick = onSelect) {
                Text(if (isSelected) "Selected" else "Select", fontFamily = RobotoCondensedBold)
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showSystemUi = true)
@Composable
fun SelectRoomScreenPreview() {
    val dummyState = AppState(
        selectedHotel = Hotel(
            id = "h1", name = "Fairfield Hotel", location = "Agra", status = HotelStatus.SOLD_OUT, // Preview SOLD_OUT state
            rating = 4.3,
            about = "Our hotel is located in Ashok Cosmos Mall in Sanjay Place, one of the largest shopping malls in the city...",
            amenities = listOf("Gym", "Free Parking", "Restaurant", "WiFi"),
            propertyRules = listOf(
                "Check-in: 12:00 PM, Check-out: 11:00 AM",
                "Pets are not allowed.",
                "Passport, Aadhaar, and Govt. ID are accepted as ID proofs."
            )
        ),
        rooms = emptyList() // No rooms to show when sold out
    )

    SelectRoomScreenLayout(
        state = dummyState,
        onBackClick = {},
        onLogoutClick = {},
        onRoomSelect = {},
        onCheckoutClick = {}
    )
}
