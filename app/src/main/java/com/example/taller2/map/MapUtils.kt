package com.example.taller2.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MapUtils {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun findAddress(context: Context, location: LatLng, onResult: (String?) -> Unit) {
        val geocoder = Geocoder(context)
        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
            onResult(addresses.firstOrNull()?.getAddressLine(0))
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun findLocation(context: Context, address: String, onResult: (LatLng?) -> Unit) {
        val geocoder = Geocoder(context)
        geocoder.getFromLocationName(address, 1) { addresses ->
            val a = addresses.firstOrNull()
            onResult(a?.let { LatLng(it.latitude, it.longitude) })
        }
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun writeLocationToFile(ctx: Context, lat: Double, lon: Double) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val json = JSONObject().apply {
            put("latitude", lat)
            put("longitude", lon)
            put("datetime", timestamp)
        }
        val file = File(ctx.filesDir, "locations.json")
        file.appendText(json.toString() + "\n")
        Log.i("LOCATION", "Saved to ${file.absolutePath}")
    }

    fun readLocationsFromFile(ctx: Context): List<LatLng> {
        val points = mutableListOf<LatLng>()
        try {
            val file = File(ctx.filesDir, "locations.json")
            if (!file.exists()) return points
            BufferedReader(FileReader(file)).useLines { lines ->
                lines.forEach { line ->
                    val obj = JSONObject(line)
                    points.add(LatLng(obj.getDouble("latitude"), obj.getDouble("longitude")))
                }
            }
        } catch (e: Exception) {
            Log.e("LOCATION", "Error reading file", e)
        }
        return points
    }
}