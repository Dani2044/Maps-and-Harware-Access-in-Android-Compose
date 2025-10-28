package com.example.taller2.map

import com.google.android.gms.maps.model.LatLng

data class MapUIState(
    val place: String = "",
    val markers: List<MyMarker> = emptyList(),
    val showCurrentMarker: Boolean = false,
    val routePoints: List<LatLng> = emptyList(),
    val userPathPoints: List<LatLng> = emptyList(),
    val darkMode: Boolean = false
)