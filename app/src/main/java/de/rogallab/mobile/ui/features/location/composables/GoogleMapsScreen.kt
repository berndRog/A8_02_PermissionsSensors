package de.rogallab.mobile.ui.features.location.composables

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.rogallab.mobile.domain.model.LocationValue
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.features.location.LocationIntent
import de.rogallab.mobile.ui.features.location.LocationViewModel

@Composable
fun GoogleMapsScreen(
   context: Context,
   viewModel: LocationViewModel
) {
   val tag = "<-GoogleMapsScreen"

   var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
   var zoom by remember { mutableFloatStateOf(15f) }
   val polylinePoints = remember { mutableStateListOf<LatLng>() }

   // Initialize the camera position state with the desired initial position and zoom level
   val cameraPositionState = rememberCameraPositionState {
      position = CameraPosition.fromLatLngZoom(currentLocation, zoom)
   }

   // Initialize the marker state with the initial position
   val markerState = rememberMarkerState(position = currentLocation)

   LaunchedEffect(cameraPositionState.isMoving) {
      if (!cameraPositionState.isMoving) {
         zoom = cameraPositionState.position.zoom
      }
   }

   val locationUiState by
      viewModel.locationUiStateFlow.collectAsStateWithLifecycle()

   LaunchedEffect(Unit) {
      logInfo(tag, "get location")
      viewModel.processIntent(LocationIntent.GetLocation)
   }


   LaunchedEffect(locationUiState.last) {
      locationUiState.last?.let { it: LocationValue ->
         currentLocation = LatLng(it.latitude, it.longitude)
         markerState.position = currentLocation
         polylinePoints.add(currentLocation)
         cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, zoom)
      }
   }

   // Broadcast receiver: location updates from service
   val locationReceiver = remember {
      object : BroadcastReceiver() {
         override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
            val newLocation = LatLng(latitude, longitude)
            polylinePoints.add(newLocation)
            currentLocation = newLocation
            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, zoom)
         }
      }
   }
   DisposableEffect(Unit) {
      val filter = IntentFilter("LOCATION_UPDATE")
      context.registerReceiver(locationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
      onDispose {
         context.unregisterReceiver(locationReceiver)
      }
   }

   Column(modifier = Modifier.fillMaxSize()) {

      var markerIsEnabled by remember { mutableStateOf(true) }
      var polylineIsEnabled by remember { mutableStateOf(true) }

      // Switch to enable/disable the marker
      SwitchWithLabel(
         label ="Show Marker",
         checked = markerIsEnabled,
         onCheckedChange = {
            markerIsEnabled = it
         }
      )

      // Center the map around the marker when enabled
      LaunchedEffect(markerIsEnabled) {
         logInfo(tag, "markerState: ${markerState.position.latitude}, ${markerState.position.longitude}")
         if (markerIsEnabled) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(markerState.position, zoom)
         }
      }

      GoogleMap(
         modifier = Modifier.fillMaxSize(),
         cameraPositionState = cameraPositionState,
         uiSettings = MapUiSettings(
            zoomControlsEnabled = false, // Disable default zoom controls
            myLocationButtonEnabled = true // Enable the My Location button
         ),

      ) {
         if (markerIsEnabled) {
            Marker(
               state = markerState,
               title = "Hier bin ich"
            )
         }
         // Add Polyline to the map
         Polyline(
            points = polylinePoints,
            color = Color.Blue,
            width = 5f
         )
      }

   }
}

