package com.himanshu.ecoscope

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

@SuppressLint("MissingPermission")
fun saveUserData(
    activity: ComponentActivity,
    username: String
) {
    // Token le raha
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) return@addOnCompleteListener
        val token = task.result

        // Location client
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        // Agar permission nahi hai toh return
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@addOnCompleteListener
        }

        // Location le raha
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val latitude = location?.latitude ?: 0.0
            val longitude = location?.longitude ?: 0.0

            // Firebase mein data save kar raha
            val db = FirebaseDatabase.getInstance()
            val ref = db.getReference("users").child(username)
            val data = mapOf(
                "token" to token,
                "latitude" to latitude,
                "longitude" to longitude
            )
            ref.setValue(data)
        }
    }
}
