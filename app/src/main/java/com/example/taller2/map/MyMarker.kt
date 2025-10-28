package com.example.taller2.map

import com.google.android.gms.maps.model.LatLng

data class MyMarker(
    val position: LatLng,
    val title: String = "Marker",
    val snippet: String = ""
)