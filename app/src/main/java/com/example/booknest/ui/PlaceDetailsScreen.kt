package com.example.booknest.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.example.booknest.R
import com.example.booknest.data.AppState
import com.example.booknest.data.AppUiEvent
import com.example.booknest.data.PlaceDetails
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSans
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel

// "Smart" Composable: Handles state and logic
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()

    PlaceDetailsScreenLayout(
        state = state,
        onBackClick = { navController.popBackStack() },
        onLogoutClick = {
            hotelViewModel.onEvent(AppUiEvent.LogoutClicked)
            navController.navigate(AppRoutes.SEND_OTP.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        },
        onHomeClick = { navController.popBackStack(AppRoutes.ROOM_SEARCH.name, inclusive = false) },
        onWhere2GoClick = { navController.navigate(AppRoutes.WHERE2GO.name) }
    )
}

// "Dumb" Composable: UI Layout only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsScreenLayout(
    state: AppState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWhere2GoClick: () -> Unit
) {
    val placeDetails = state.selectedPlaceDetails
    var selectedBottomNavItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf("Home", "Where2Go", "FAQs")
    val bottomNavIcons = listOf(Icons.Default.Home, Icons.Default.Info, Icons.Default.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Places", fontFamily = WorkSansBold) },
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
                                "Home" -> onHomeClick()
                                "Where2Go" -> onWhere2GoClick()
                            }
                        },
                        icon = { Icon(bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (placeDetails == null) {
                Text("Place details not found.", fontFamily = RobotoCondensedBold)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = placeDetails.imageUrl,
                        contentDescription = placeDetails.name,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.booknest),
                        error = painterResource(id = R.drawable.booknest)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = placeDetails.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = WorkSansBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = placeDetails.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                            fontFamily = WorkSans
                        )
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PlaceDetailsScreenPreview() {
    val dummyState = AppState(
        isLoading = false,
        selectedPlaceDetails = PlaceDetails(
            id = "p1",
            name = "Bengaluru",
            description = "Known as the Silicon Valley of India, Bengaluru is a hub of technology and innovation, offering a unique blend of modern advancements and lush, green parks.",
            imageUrl = ""
        )
    )
    PlaceDetailsScreenLayout(
        state = dummyState,
        onBackClick = {},
        onLogoutClick = {},
        onHomeClick = {},
        onWhere2GoClick = {}
    )
}
