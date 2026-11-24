package com.example.pw2.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object UserInfo : Screen("user_info")
    object ChargingDetails : Screen("charging_details")
    object RegistrationForm : Screen("registration_form")
    object MvvmForecast : Screen("mvvm_forecast")
    object RoomDatabase : Screen("room_database")
}