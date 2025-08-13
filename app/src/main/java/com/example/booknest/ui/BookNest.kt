package com.example.booknest.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.data.AppUiEvent
import com.example.booknest.viewmodel.HotelViewModel


enum class AppRoutes {
    SEND_OTP,
    VERIFY_OTP,
    ROOM_SEARCH,
    CHECKOUT,
    PLACE_DETAILS,
    SELECT_HOTEL,
    SELECT_ROOM,
    WHERE2GO
}



@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun BookNestApp(navController: NavHostController) {
    val hotelViewModel = HotelViewModel()
    val context = LocalContext.current
    val placeName = ""
    val hotelName = ""

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SEND_OTP.name
    ){
        composable(route = AppRoutes.SEND_OTP.name){
            SendOtpScreen(navController, hotelViewModel)
        }

        composable(route = AppRoutes.VERIFY_OTP.name) {
            VerifyOtpScreen(navController, hotelViewModel)
        }


        composable(route = AppRoutes.ROOM_SEARCH.name) {
            FindRoomScreen(
                navController = navController,
                hotelViewModel = hotelViewModel
            )
        }

        composable(route = AppRoutes.CHECKOUT.name){
            CheckoutScreen(
                navController,
                hotelViewModel
            )
        }

        composable(
            route = "${AppRoutes.PLACE_DETAILS.name}/{placeId}",
            arguments = listOf(navArgument("placeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")

            LaunchedEffect(key1 = placeId) {
                if (placeId != null) {
                    hotelViewModel.onEvent(AppUiEvent.FetchPlaceDetailsById(placeId))
                }
            }

            PlaceDetailsScreen(
                navController = navController,
                hotelViewModel = hotelViewModel
            )
        }


        composable(route = AppRoutes.SELECT_HOTEL.name) {
            SelectHotelScreen(
                navController = navController,
                hotelViewModel = hotelViewModel
            )
        }

        composable(route = AppRoutes.SELECT_ROOM.name){
            SelectRoomScreen(navController, hotelViewModel)

        }

        composable(route = AppRoutes.WHERE2GO.name){
            Where2GoScreen(
                navController = navController,
                hotelViewModel = hotelViewModel
            )
        }

    }

}