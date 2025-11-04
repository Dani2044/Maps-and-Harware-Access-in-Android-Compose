package com.example.taller2.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller2.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapViewModel: MapViewModel = viewModel()
    val ui by mapViewModel.uI.collectAsState()
    val locationViewModel: LocationViewModel = viewModel()
    val loc by locationViewModel.state.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 3f)
    }
    val scope = rememberCoroutineScope()

    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(2000)
            .build()
    }
    var lastSaved by remember { mutableStateOf<LatLng?>(null) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val l = result.lastLocation ?: return
                val lat = l.latitude
                val lon = l.longitude
                locationViewModel.update(lat, lon)
                val prev = lastSaved
                val now = LatLng(lat, lon)
                if (prev == null) {
                    lastSaved = now
                } else {
                    val dKm = MapUtils.distance(prev.latitude, prev.longitude, lat, lon)
                    if (dKm > 0.03) {
                        MapUtils.writeLocationToFile(context, lat, lon)
                        lastSaved = now
                    }
                }
            }
        }
    }

    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val lightListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values[0]
                    mapViewModel.setDarkMode(lux < 2000f)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    val locationPermissionCheck = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) fused.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    LaunchedEffect(Unit) {
        if (!locationPermissionCheck) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    DisposableEffect(locationPermissionCheck) {
        if (locationPermissionCheck) {
            fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            lightSensor?.let {
                sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        onDispose {
            fused.removeLocationUpdates(locationCallback)
            sensorManager.unregisterListener(lightListener)
        }
    }

    val currentAddress = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(loc.latitude, loc.longitude) {
        val here = LatLng(loc.latitude, loc.longitude)
        MapUtils.findAddress(context, here) { a -> currentAddress.value = a }
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(here, 16f))
    }

    val lightMap =
        runCatching { MapStyleOptions.loadRawResourceStyle(context, R.raw.default_map) }.getOrNull()
    val darkMap = runCatching {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.aubergine_map)
    }.getOrNull()
    val currentStyle =
        remember(ui.darkMode, lightMap, darkMap) { if (ui.darkMode) darkMap else lightMap }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true
            )
        )
    }
    val locationPermissionCheckNow = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = MapProperties(
                mapStyleOptions = currentStyle,
                isMyLocationEnabled = locationPermissionCheckNow
            ),
            onMapLongClick = { position ->
                MapUtils.findAddress(context, position) { address ->
                    val title = address ?: "${position.latitude}, ${position.longitude}"
                    mapViewModel.addMarker(position, title)
                    scope.launch {
                        val meters = (MapUtils.distance(
                            loc.latitude,
                            loc.longitude,
                            position.latitude,
                            position.longitude
                        ) * 1000).toInt()
                        Toast.makeText(context, "$title\nDist: $meters m", Toast.LENGTH_SHORT)
                            .show()

                        val origin = LatLng(loc.latitude, loc.longitude)
                        val pts = withContext(Dispatchers.IO) {
                            mapViewModel.fetchRoute(origin, position)
                        }
                        if (pts.isNotEmpty()) {
                            mapViewModel.setRoute(pts)
                        } else {
                            Toast.makeText(context, "No route found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        ) {
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(loc.latitude, loc.longitude)
                ),
                title = currentAddress.value ?: "Current location",
                visible = ui.showCurrentMarker
            )

            ui.markers.forEach { m ->
                Marker(
                    state = rememberUpdatedMarkerState(position = m.position),
                    title = m.title,
                    snippet = m.snippet
                )
            }
            if (ui.routePoints.isNotEmpty()) {
                Polyline(
                    points = ui.routePoints,
                    color = colorResource(id = R.color.blue),
                    width = 8f
                )
            }
            if (ui.userPathPoints.isNotEmpty()) {
                Polyline(
                    points = ui.userPathPoints,
                    color = if (ui.useAltPathColor)
                        colorResource(id = R.color.red)
                    else
                        colorResource(id = R.color.darkGreen),
                    width = 6f
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 16.dp, top = 48.dp, end = 16.dp)
        ) {
            TextField(
                value = ui.place,
                onValueChange = { mapViewModel.setPlace(it) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorResource(id = R.color.white33),
                    unfocusedContainerColor = colorResource(id = R.color.white22),
                    focusedLabelColor = colorResource(id = R.color.white),
                    unfocusedLabelColor = colorResource(id = R.color.white),
                    focusedTextColor = colorResource(id = R.color.white),
                    unfocusedTextColor = colorResource(id = R.color.white)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        MapUtils.findLocation(context, ui.place) { found ->
                            if (found != null) {
                                MapUtils.findAddress(context, found) { address ->
                                    val title = address ?: ui.place
                                    mapViewModel.addMarker(found, title, ui.place)
                                    scope.launch {
                                        val meters = (MapUtils.distance(
                                            loc.latitude, loc.longitude,
                                            found.latitude, found.longitude
                                        ) * 1000).toInt()
                                        Toast.makeText(
                                            context,
                                            "$title\nDist: $meters m",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(found, 15f)
                                        )

                                        val origin = LatLng(loc.latitude, loc.longitude)
                                        val pts = withContext(Dispatchers.IO) {
                                            mapViewModel.fetchRoute(origin, found)
                                        }
                                        if (pts.isNotEmpty()) {
                                            mapViewModel.setRoute(pts)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No route found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mapViewModel.loadUserPath(context) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.darkTeal)
                    )
                ) {
                    Text("Show Path", color = colorResource(id = R.color.white))
                }

                Button(
                    onClick = {
                        if (ui.userPathPoints.isEmpty()) mapViewModel.loadUserPath(context)
                        mapViewModel.toggleUserPathColor()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.indigo)
                    )
                ) {
                    Text("Color User Path", color = colorResource(id = R.color.white))
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = {
                    val lat = loc.latitude
                    val lon = loc.longitude
                    if (lat == 0.0 && lon == 0.0) {
                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!ui.showCurrentMarker) {
                            mapViewModel.showCurrentMarker()
                            val here = LatLng(lat, lon)
                            MapUtils.findAddress(context, here) { a -> currentAddress.value = a }
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(here, 16f)
                                )
                            }
                        } else {
                            mapViewModel.hideCurrentMarker()
                        }
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.MyLocation,
                    contentDescription = "Toggle current marker",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = { mapViewModel.clearMarkers() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Clear map",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}