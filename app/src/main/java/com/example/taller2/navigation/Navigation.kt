package com.example.taller2.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller2.camera.CameraScreen
import com.example.taller2.contacts.ContactsScreen
import com.example.taller2.home.HomeScreen
import com.example.taller2.map.MapScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = AppScreens.Home.name) {
        composable(AppScreens.Home.name) {
            HomeScreen(navController)
        }
        composable(AppScreens.Contacts.name) {
            ContactsScreen()
        }
        composable(AppScreens.Camera.name) {
            CameraScreen()
        }
        composable(AppScreens.Map.name) {
            MapScreen()
        }
    }
}