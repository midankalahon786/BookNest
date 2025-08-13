package com.example.booknest.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.booknest.R
import com.example.booknest.data.AppUiEvent
import com.example.booknest.data.AppState
import com.example.booknest.data.Hotel
import com.example.booknest.data.HotelStatus
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel

// "Smart" Composable: Handles state and logic
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectHotelScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()

    SelectHotelScreenLayout(
        state = state,
        onHotelClick = { hotel ->
            hotelViewModel.onEvent(AppUiEvent.HotelSelected(hotel))
            navController.navigate(AppRoutes.SELECT_ROOM.name)
        },
        onBackClick = { navController.popBackStack() },
        onLogoutClick = {
            hotelViewModel.onEvent(AppUiEvent.LogoutClicked)
            navController.navigate(AppRoutes.SEND_OTP.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        },
        onHomeClick = { navController.popBackStack(AppRoutes.ROOM_SEARCH.name, false) },
        onWhere2GoClick = { navController.navigate(AppRoutes.WHERE2GO.name) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectHotelScreenLayout(
    state: AppState,
    onHotelClick: (Hotel) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWhere2GoClick: () -> Unit
) {
    var selectedBottomNavItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf("Home", "Where2Go", "FAQs")
    val bottomNavIcons = listOf(Icons.Default.Home, Icons.Default.LocationOn, Icons.Default.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Hotel", fontFamily = WorkSansBold) },
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
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomNavItem == index,
                        onClick = {
                            selectedBottomNavItem = index
                            when (item) {
                                "Home" -> onHomeClick()
                                "Where2Go" -> onWhere2GoClick()
                            }
                        },
                        icon = { Icon(bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item, fontFamily= RobotoCondensedBold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.hotels) { hotel ->
                    HotelCard(
                        hotel = hotel,
                        onHotelClick = { onHotelClick(hotel) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTag(hotel: Hotel) {
    val tagColor = when (hotel.status) {
        HotelStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        HotelStatus.SOLD_OUT -> Color(0xFFE57373)
    }
    val tagText = when (hotel.status) {
        HotelStatus.AVAILABLE -> hotel.priceRange ?: "N/A"
        HotelStatus.SOLD_OUT -> "Sold Out"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .background(tagColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tagText,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HotelCard(
    hotel: Hotel,
    onHotelClick: (Hotel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onHotelClick(hotel) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.booknest),
                    error = painterResource(id = R.drawable.booknest)
                )
                StatusTag(hotel = hotel)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${hotel.name}, ${hotel.location}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSansBold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showSystemUi = true)
@Composable
fun SelectHotelScreenPreview() {
    val dummyState = AppState(
        hotels = listOf(
            Hotel(id = "h1", name = "Fairfield Hotel", location = "Agra", status = HotelStatus.AVAILABLE, priceRange = "Rs 4,999 - 9,999"),
            Hotel(id = "h2", name = "Grand Victoria", location = "Agra", status = HotelStatus.SOLD_OUT)
        )
    )
    SelectHotelScreenLayout(
        state = dummyState,
        onHotelClick = {},
        onBackClick = {},
        onLogoutClick = {},
        onHomeClick = {},
        onWhere2GoClick = {}
    )
}
