package com.mansi.ecoscope_frontend.utils




import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient

object LocationUtils {

    private var fusedLocationClient: FusedLocationProviderClient? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation(context: Context, callback: (Location?) -> Unit) {
        if (!hasLocationPermission(context)) {
            callback(null)
            return
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location)
            } else {
                // Fallback to active location update
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000L
                ).apply {
                    setWaitForAccurateLocation(true)
                    setMaxUpdates(1)
                }.build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient?.removeLocationUpdates(this)
                        callback(result.lastLocation)
                    }
                }

                fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }?.addOnFailureListener {
            callback(null)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.run {
            isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } ?: false
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
