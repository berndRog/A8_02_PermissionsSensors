package de.rogallab.mobile.ui.features.location

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IAppLocationManager
import de.rogallab.mobile.domain.model.LocationValue
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationViewModel(
   application: Application,
   private val _locationManager: IAppLocationManager,
   private val _errorHandler: IErrorHandler,
   private val _navHandler: INavigationHandler,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navHandler {

   private val _context: Context = application.applicationContext

   //region Broadcast receiver to receive location updates
   private val locationReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         val time = intent?.getLongExtra("time", 0L) ?: 0L
         val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
         val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
         val location = Location("dummyprovider").apply {
            this.time = time
            this.latitude = latitude
            this.longitude = longitude
         }
         // get location updates (tracking data), via broadcast from the location manager
         processLocation(location)
      }
   }

   init {
      val filter = IntentFilter("LOCATION_UPDATE")
      application.registerReceiver(locationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
   }
   //endregion

   //region Expose location updates to the UI
   private val _locationUiStateFlow: MutableStateFlow<LocationUiState> =
      MutableStateFlow(LocationUiState())
   val locationUiStateFlow: StateFlow<LocationUiState> =
      _locationUiStateFlow.asStateFlow()

   private fun processLocation(location: Location) {
      _locationUiStateFlow.update { locationUiState ->
         // new location value object
         val locationValue = LocationValue(
            time = location.time,
            latitude = location.latitude,
            longitude = location.longitude,
         )
         // add the new locationValue to the ringBuffer
         locationUiState.ringBuffer.add(locationValue)
         // update the last locationValue
         locationUiState.copy(
            last = locationValue,
            ringBuffer = locationUiState.ringBuffer
         )
      }
   }
   //endregion

   // region Process Intents
   fun processIntent(intent: LocationIntent) {
      when (intent) {
         is LocationIntent.GetLocation -> getLocation()
         is LocationIntent.StartLocationService -> startLocationService()
         is LocationIntent.StopLocationService -> stopLocationService()
      }
   }
   private fun getLocation(){
      // get last known location and update the locationflow
      _locationManager.getLocation { location ->
         processLocation(location)
      }
   }
   private fun startLocationService() {
      viewModelScope.launch {
         withContext(_dispatcher + _exceptionHandler) {
            Intent(_context, AppLocationService::class.java).apply {
               action = AppLocationService.Action.START.name
               _context.startService(this)
            }
         }
      }
   }
   private fun stopLocationService() {
      viewModelScope.launch {
         withContext(_dispatcher + _exceptionHandler) {
            Intent(_context, AppLocationService::class.java).apply {
               action = AppLocationService.Action.STOP.name
               _context.stopService(this)
            }
         }
      }
   }
   //endregion

   override fun onCleared() {
      stopLocationService()
      super.onCleared()
   }

   companion object {
      const val TAG = "<-LocationViewModel"
   }
}