package com.mansi.ecoscope_frontend.ui.components


import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


@Composable
fun MapPicker(
    initialPosition: LatLng = LatLng(28.7041, 77.1025),
    onLocationPicked: (LatLng) -> Unit,
    onCancel: () -> Unit
) {
    var selectedPosition by remember { mutableStateOf(initialPosition) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(initialPosition, 6f)
    }



    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng -> selectedPosition = latLng }
        ) {
            Marker(
                state = rememberMarkerState(position = selectedPosition)
            )

        }
        Button(
            onClick = { onLocationPicked(selectedPosition) },
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        ) {
            Text("Select This Location")
        }
        Button(
            onClick = onCancel,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
        ) {
            Text("Cancel")
        }
    }
}