package com.example.taller2.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.taller2.R
import com.example.taller2.navigation.AppScreens

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { navController.navigate(AppScreens.Contacts.name) },
            modifier = Modifier.background(color = Color.Transparent),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painterResource(R.drawable.contacts_image),
                "Contacts"
            )
        }
        Button(
            onClick = { navController.navigate(AppScreens.Camera.name) },
            modifier = Modifier.background(color = Color.Transparent),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painterResource(R.drawable.camera_image),
                "Camera"
            )
        }
        Button(
            onClick = { navController.navigate(AppScreens.Map.name) },
            modifier = Modifier.background(color = Color.Transparent),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painterResource(R.drawable.map_image),
                "Map"
            )
        }
    }
}