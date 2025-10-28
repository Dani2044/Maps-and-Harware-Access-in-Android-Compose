package com.example.taller2.map

import com.google.android.gms.maps.model.LatLng

data class MapUIState(
    val place: String = "",
    val searchMarker: LatLng? = null,
    val longClickMarker: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val userPathPoints: List<LatLng> = emptyList(),
    val darkMode: Boolean = false
)