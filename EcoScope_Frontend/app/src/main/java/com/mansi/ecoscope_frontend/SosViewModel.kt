package com.mansi.ecoscope_frontend

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mansi.ecoscope_frontend.utils.LocationUtils
import java.text.SimpleDateFormat
import java.util.*

class SosViewModel : ViewModel() {
    fun sendSOS(context: Context, contacts: List<String>) {
        LocationUtils.getLastLocation(context) { location ->
            val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val message = if (location != null) {
                "\uD83D\uDEA8 SOS! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "\uD83D\uDEA8 SOS! I need help. Location unavailable."
            }

            contacts.forEach { phoneNumber ->
                if (phoneNumber.isNotBlank()) {
                    try {
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("smsto:$phoneNumber")
                            putExtra("sms_body", message)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(smsIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "\u274C Failed to send SMS to $phoneNumber", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}