package de.rogallab.mobile.ui.features.orientation

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SensorViewModel(
   application: Application,
   private val _errorHandler: IErrorHandler,
   private val _navHandler: INavigationHandler,
   private val _exceptionHandler: CoroutineExceptionHandler
) : ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navHandler {

   private val _context: Context = application.applicationContext

   private var _isSensorServiceRunning: Boolean = false

   //region Broadcast receiver to receive sensor updates
   private val orientationReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         val time = intent?.getLongExtra("time", 0L) ?: 0L
         val yaw = intent?.getFloatExtra("yaw", 0.0F) ?: 0.0F
         val pitch = intent?.getFloatExtra("pitch", 0.0F) ?: 0.0F
         val roll = intent?.getFloatExtra("roll", 0.0F) ?: 0.0F

         val sensorValues = SensorValue(
            time = time,
            yaw = yaw,
            pitch = pitch,
            roll = roll
         )
         // get location updates (tracking data)
         // via broadcast from the location manager
         processOrientation(sensorValues)
      }
   }

   init {
      val filter = IntentFilter("SENSOR_UPDATE")
      application.registerReceiver(orientationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
   }
   //endregion

   //region Expose sensor updates to the UI
   private val _sensorUiStateFlow = MutableStateFlow(SensorUiState())
   val sensorUiStateFlow: StateFlow<SensorUiState> = _sensorUiStateFlow.asStateFlow()

   private fun processOrientation(sensorValues: SensorValue) {
      _sensorUiStateFlow.update { sensorUiState ->
         // add the new sensor value to the ringBuffer
         sensorUiState.ringBuffer.add(sensorValues)
         // update the last sensor value
         sensorUiState.copy(
            last = sensorValues,
            ringBuffer = sensorUiState.ringBuffer
         )
      }
   }
   //endregion

   // region Process Intents
   fun processIntent(intent: SensorIntent) {
      when (intent) {
         SensorIntent.Start -> startSensorService()
         SensorIntent.Stop -> stopSensorService()
      }
   }

   private fun startSensorService() {
      viewModelScope.launch {
         if(!_isSensorServiceRunning) {
            logDebug(TAG, "Start Sensor service")
            Intent(_context, AppSensorService::class.java).apply {
               action = AppSensorService.Action.START.name
               _context.startService(this)
            }
            _isSensorServiceRunning = true
         } else {
            logDebug(TAG, "Sensor Service already running, not starting again.")
         }
      }
   }

   private fun stopSensorService() {
      viewModelScope.launch {
         if(_isSensorServiceRunning) {
            logDebug(TAG, "Stop Sensor Service")
            Intent(_context, AppSensorService::class.java).apply {
               action = AppSensorService.Action.STOP.name
               val stopped = _context.stopService(this)
               logDebug(TAG, "Stop Sensor Service returned: $stopped")
            }
            _isSensorServiceRunning = false
         } else {
            logDebug(TAG, "Sensor Service already stopped, not stopping again.")
         }
      }
   }
   // endregion

   override fun onCleared() {
      super.onCleared()
      // Unregister the broadcast receiver if still registered
      _context.unregisterReceiver(orientationReceiver)
      // Optionally stop the sensor service here if it's still running
      stopSensorService()
   }

   companion object {
      const val TAG = "<-SensorViewModel"
   }
}