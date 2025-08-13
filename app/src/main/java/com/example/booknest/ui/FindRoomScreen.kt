package com.example.booknest.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.booknest.R
import com.example.booknest.data.AppState
import com.example.booknest.data.AppUiEvent
import com.example.booknest.data.Place
import com.example.booknest.ui.theme.RobotoCondensed
import com.example.booknest.ui.theme.RobotoCondensedBold
import com.example.booknest.ui.theme.WorkSans
import com.example.booknest.ui.theme.WorkSansBold
import com.example.booknest.ui.theme.blue
import com.example.booknest.viewmodel.HotelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// "Smart" Composable: Manages state, logic, and navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRoomScreen(
    navController: NavController,
    hotelViewModel: HotelViewModel
) {
    val state by hotelViewModel.state.collectAsState()
    val context = LocalContext.current // Get context for the Toast
    var showCheckinDialog by remember { mutableStateOf(false) }
    var selectedCheckinDate by remember { mutableStateOf("") }
    val checkinInteractionSource = remember { MutableInteractionSource() }

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedCheckoutDate by remember { mutableStateOf("") }
    val checkoutInteractionSource = remember { MutableInteractionSource() }

    val checkinDatePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)
    val checkoutDatePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)

    LaunchedEffect(checkinInteractionSource) {
        checkinInteractionSource.interactions.collect { if (it is PressInteraction.Release) showCheckinDialog = true }
    }
    LaunchedEffect(checkoutInteractionSource) {
        checkoutInteractionSource.interactions.collect { if (it is PressInteraction.Release) showCheckoutDialog = true }
    }

    FindRoomScreenLayout(
        state = state,
        navController = navController,
        checkinDate = selectedCheckinDate,
        checkoutDate = selectedCheckoutDate,
        checkinInteractionSource = checkinInteractionSource,
        checkoutInteractionSource = checkoutInteractionSource,
        onSearchClick = {
            // **VALIDATION LOGIC WITH TOAST**
            val isFormComplete = state.destination.isNotBlank() &&
                    selectedCheckinDate.isNotBlank() &&
                    selectedCheckoutDate.isNotBlank()

            if (isFormComplete) {
                hotelViewModel.onEvent(AppUiEvent.SearchClicked)
                navController.navigate(AppRoutes.SELECT_HOTEL.name)
            } else {
                Toast.makeText(context, "Please fill all fields to search.", Toast.LENGTH_SHORT).show()
            }
        },
        onPlaceClick = { place ->
            navController.navigate("${AppRoutes.PLACE_DETAILS.name}/${place.id}")
        },
        onLogoutClick = {
            hotelViewModel.onEvent(AppUiEvent.LogoutClicked)
            navController.navigate(AppRoutes.SEND_OTP.name) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        },
        onDestinationChanged = { hotelViewModel.onEvent(AppUiEvent.DestinationChanged(it)) },
        onRoomsChanged = { hotelViewModel.onEvent(AppUiEvent.NumberOfRoomsChanged(it)) }
    )

    if (showCheckinDialog) {
        DatePickerDialog(
            onDismissRequest = { showCheckinDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedCheckinDate = formatDate(checkinDatePickerState.selectedDateMillis)
                    hotelViewModel.onEvent(AppUiEvent.CheckInDateChanged(selectedCheckinDate))
                    showCheckinDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCheckinDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = checkinDatePickerState) }
    }

    if (showCheckoutDialog) {
        DatePickerDialog(
            onDismissRequest = { showCheckoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedCheckoutDate = formatDate(checkoutDatePickerState.selectedDateMillis)
                    hotelViewModel.onEvent(AppUiEvent.CheckOutDateChanged(selectedCheckoutDate))
                    showCheckoutDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCheckoutDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = checkoutDatePickerState) }
    }
}

// "Dumb" Composable: UI Layout only
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRoomScreenLayout(
    state: AppState,
    navController: NavController,
    checkinDate: String,
    checkoutDate: String,
    checkinInteractionSource: MutableInteractionSource,
    checkoutInteractionSource: MutableInteractionSource,
    onSearchClick: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onLogoutClick: () -> Unit,
    onDestinationChanged: (String) -> Unit,
    onRoomsChanged: (String) -> Unit
) {
    var selectedBottomNavItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf("Home", "Where2Go", "FAQs")
    val bottomNavIcons = listOf(Icons.Default.Home, Icons.Default.LocationOn, Icons.Default.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BookNest", fontWeight = FontWeight.Bold, fontFamily =
                    WorkSansBold) },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Icon(painter = painterResource(R.drawable.baseline_logout_24),
                            contentDescription = "Logout", tint = blue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontFamily = WorkSansBold, color= blue)
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
                            // **FIXED NAVIGATION LOGIC**
                            when (item) {
                                "Home" -> {
                                    // Already on the home screen, do nothing.
                                }
                                "Where2Go" -> {
                                    navController.navigate(AppRoutes.WHERE2GO.name)
                                }
                                "FAQs" -> {
                                    // TODO: Navigate to FAQs screen when created
                                }
                            }
                        },
                        icon = { Icon(bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item, fontFamily= RobotoCondensedBold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DestinationSearchField(
                    destination = state.destination,
                    places = state.bestPlaces,
                    onDestinationChanged = onDestinationChanged
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = checkinDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Check-in Date", fontFamily = RobotoCondensed) },
                    leadingIcon = { Icon(Icons.Default.DateRange, "Calendar Icon") },
                    interactionSource = checkinInteractionSource
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = checkoutDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Check-out Date", fontFamily = RobotoCondensed) },
                    leadingIcon = { Icon(Icons.Default.DateRange, "Calendar Icon") },
                    interactionSource = checkoutInteractionSource
                )
                Spacer(modifier = Modifier.height(8.dp))
                RoomsDialogField(
                    numberOfRooms = state.numberOfRooms,
                    onRoomsChanged = onRoomsChanged
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF37B4E0), Color(0xFF138AD8)))
                            ), contentAlignment = Alignment.Center
                    ) {
                        Text("SEARCH", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "BEST PLACES",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = RobotoCondensedBold
                    )
                    TextButton(onClick = { navController.navigate(AppRoutes.WHERE2GO.name) }) {
                        Text("VIEW ALL", fontFamily = RobotoCondensedBold, color=blue)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.bestPlaces) { place ->
                        PlaceCard(place = place, onPlaceClick = { onPlaceClick(place) })
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationSearchField(
    destination: String,
    places: List<Place>,
    onDestinationChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlaces = remember(searchQuery, places) {
        if (searchQuery.isBlank()) places
        else places.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = destination,
            onValueChange = {},
            readOnly = true,
            label = { Text("Destination", fontFamily = RobotoCondensed) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...") },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                singleLine = true
            )
            if (filteredPlaces.isEmpty()) {
                DropdownMenuItem(text = { Text("No results", fontFamily=RobotoCondensed) },
                    onClick = {},
                    enabled =
                    false)
            } else {
                filteredPlaces.forEach { place ->
                    DropdownMenuItem(
                        text = { Text(place.name, fontFamily=RobotoCondensedBold) },
                        onClick = {
                            onDestinationChanged(place.name)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomsDialogField(
    numberOfRooms: String,
    onRoomsChanged: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val roomsOptions = (1..5).map { it.toString() }

    TextButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Number of Rooms", tint =
                blue)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Number of Rooms: $numberOfRooms",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = RobotoCondensedBold,
                color = blue
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Number of Rooms", fontFamily=RobotoCondensedBold) },
            text = {
                Column {
                    roomsOptions.forEach { rooms ->
                        Text(
                            text = rooms,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRoomsChanged(rooms)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            fontFamily = RobotoCondensedBold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel", fontFamily =
                    RobotoCondensedBold) }
            }
        )
    }
}

@Composable
fun PlaceCard(place: Place, onPlaceClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .size(width = 160.dp, height = 200.dp)
            .clickable { onPlaceClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.booknest),
                error = painterResource(id = R.drawable.booknest)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 400f
                        )
                    )
            )
            Text(
                text = place.name,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontFamily = WorkSans
            )
        }
    }
}

fun formatDate(millis: Long?): String {
    return if (millis != null) {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        formatter.format(Date(millis))
    } else {
        ""
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showSystemUi = true)
@Composable
fun FindRoomScreenPreview() {
    val fakeNavController = rememberNavController()
    val dummyState = AppState(
        bestPlaces = listOf(
            Place(id = "p1", name = "Agra", imageUrl = ""),
            Place(id = "p2", name = "Bengaluru", imageUrl = "")
        ),
        destination = "Agra",
        numberOfRooms = "2"
    )
    FindRoomScreenLayout(
        state = dummyState,
        navController = fakeNavController,
        checkinDate = "13 Aug 2025",
        checkoutDate = "15 Aug 2025",
        checkinInteractionSource = remember { MutableInteractionSource() },
        checkoutInteractionSource = remember { MutableInteractionSource() },
        onSearchClick = {},
        onPlaceClick = {},
        onLogoutClick = {},
        onDestinationChanged = {},
        onRoomsChanged = {}
    )
}

