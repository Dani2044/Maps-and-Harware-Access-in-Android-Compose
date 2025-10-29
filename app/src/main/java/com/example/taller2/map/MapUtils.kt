package com.example.taller2.map

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.URL
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
        val decodeLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(decodeLat / 2).pow(2.0) +
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

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val decodeLat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += decodeLat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val decodeLong = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += decodeLong
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    fun getDirections(origin: LatLng, dest: LatLng, apiKey: String): List<LatLng>? {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=driving&key=$apiKey"
        val json = URL(url).readText()
        val obj = JSONObject(json)
        val routes = obj.optJSONArray("routes") ?: return null
        if (routes.length() == 0) return null
        val poly = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
        return decodePolyline(poly)
    }

    fun getMapsApiKey(context: Context): String? {
        return try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            }
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            Log.e("MAPS", "API key not found in manifest meta-data", e)
            null
        }
    }
}