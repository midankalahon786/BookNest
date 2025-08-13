package com.example.booknest.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.booknest.data.Place
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Where2GoScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()

    Where2GoScreenLayout(
        state = state,
        navController = navController,
        onPlaceClick = { place ->
            navController.navigate("${AppRoutes.PLACE_DETAILS.name}/${place.id}")
        },
        onBackClick = { navController.popBackStack() },
        onLogoutClick = {
            hotelViewModel.onEvent(AppUiEvent.LogoutClicked)
            navController.navigate(AppRoutes.SEND_OTP.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Where2GoScreenLayout(
    state: AppState,
    navController: NavController, // Receive NavController
    onPlaceClick: (Place) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedBottomNavItem by remember { mutableIntStateOf(1) }
    val bottomNavItems = listOf("Home", "Where2Go", "FAQs")
    val bottomNavIcons = listOf(Icons.Default.Home, Icons.Default.LocationOn, Icons.Default.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Where2Go", fontFamily = WorkSansBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Icon(painter = painterResource(R.drawable.baseline_logout_24),
                            contentDescription = "Logout", tint = blue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontFamily = RobotoCondensedBold, color = blue)
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
                                "Home" -> {
                                    // Navigate back to the FindRoomScreen
                                    navController.popBackStack(route = AppRoutes.ROOM_SEARCH.name, inclusive = false)
                                }
                                "Where2Go" -> {
                                    // Already on this screen, do nothing or refresh
                                }
                                "FAQs" -> {
                                }
                            }
                        },
                        icon = { Icon(bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item, fontFamily = RobotoCondensedBold) }
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
                items(state.bestPlaces) { place ->
                    PlaceListItemCard(
                        place = place,
                        onPlaceClick = { onPlaceClick(place) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceListItemCard(
    place: Place,
    onPlaceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaceClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.booknest),
                error = painterResource(id = R.drawable.booknest)
            )
            Text(
                text = place.name,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSansBold
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Where2GoScreenPreview() {
    val dummyState = AppState(
        isLoading = false,
        bestPlaces = listOf(
            Place(id = "p1", name = "Agra", imageUrl = ""),
            Place(id = "p2", name = "Bengaluru", imageUrl = ""),
            Place(id = "p3", name = "Guwahati", imageUrl = "")
        )
    )
    Where2GoScreenLayout(
        state = dummyState,
        navController = rememberNavController(), // Pass a NavController for the preview
        onPlaceClick = {},
        onBackClick = {},
        onLogoutClick = {}
    )
}
