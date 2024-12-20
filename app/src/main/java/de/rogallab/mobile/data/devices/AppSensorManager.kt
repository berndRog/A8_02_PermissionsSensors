package de.rogallab.mobile.data.devices

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import de.rogallab.mobile.domain.IAppSensorManager
import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppSensorManager(
   context: Context,
) : IAppSensorManager {

   private val _sensorManager: SensorManager =
      context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

   private val _pressureSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
   private val _lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
   private val _proximitySensor = _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
   private val _gyroscopeSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
   private val _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
   private val _magnetometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
   private val _rotVecSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

   private var _pressure: Float = 0f
   private var _light: Float = 0f
   private var _proximity: Float = 0f

   private val _gyrosData = FloatArray(3)
   private val _accData = FloatArray(3)
   private val _magnetometerData = FloatArray(3)
   private val _rotVector = FloatArray(4)
   private val _rotMatrix = FloatArray(9)
   private val _inclinationMatrix = FloatArray(9)
   private val _orientationAngles = FloatArray(3)
   private var _isRotVecSensorAvailable = false

   private var _currentTime: Long = 0L
   private var _lastUpdateTime = 0L
   private var _updateInterval: Long = 1000L

   init {
      val deviceSensors: List<Sensor> = _sensorManager.getSensorList(Sensor.TYPE_ALL)
      logInfo(TAG, "Available sensors: ${deviceSensors.size}")
      deviceSensors.forEach { it ->
         logInfo(TAG, "Available sensor: ${it.name} - ${it.stringType}")
      }
   }

   override fun setUpdateInterval(interval: Long) {
      logInfo(TAG, "setUpdateInterval($interval)")
      _updateInterval = interval
   }

   override fun sensorValuesFlow(): Flow<SensorValue> = callbackFlow {
      val sensorEventListener = object : SensorEventListener {
         override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
               val (shouldSend, sensorValue) = updateSensorValue(event)
               if (shouldSend) {
                  trySend(sensorValue!!).isSuccess
               }
            }
         }

         override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            logInfo(TAG, "Sensor accuracy changed: $accuracy")
         }
      }

      startListening(sensorEventListener)

      awaitClose {
         stopListening(sensorEventListener)
      }
   }

   private fun updateSensorValue(event: SensorEvent): Pair<Boolean, SensorValue?> {
      when (event.sensor.type) {
         Sensor.TYPE_PRESSURE -> _pressure = event.values[0]
         Sensor.TYPE_LIGHT -> _light = event.values[0]
         Sensor.TYPE_PROXIMITY -> _proximity = event.values[0]
         Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, _accData, 0, _accData.size)
         Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, _magnetometerData, 0, _magnetometerData.size)
         Sensor.TYPE_ROTATION_VECTOR -> {
            if (event.values.size >= 4) {
               System.arraycopy(event.values, 0, _rotVector, 0, 4)
            }
            _isRotVecSensorAvailable = true
         }
         Sensor.TYPE_GYROSCOPE -> System.arraycopy(event.values, 0, _gyrosData, 0, _gyrosData.size)
      }

      _currentTime = System.currentTimeMillis()
      if (_currentTime - _lastUpdateTime <= _updateInterval) return Pair(false, null)

      _lastUpdateTime = _currentTime
      val sensorValue = processSensorValue()
      return Pair(true, sensorValue)
   }

   private fun processSensorValue(): SensorValue {
      var sensorValue = SensorValue(
         time = _currentTime,
         pressure = _pressure,
         light = _light
      )
      if (_isRotVecSensorAvailable) {
         if (_rotVector.size == 4) {
            val q0 = _rotVector[3]
            val q1 = _rotVector[0]
            val q2 = _rotVector[1]
            val q3 = _rotVector[2]
            val norm = Math.sqrt((q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3).toDouble())
            _rotVector[0] = (q1 / norm).toFloat()
            _rotVector[1] = (q2 / norm).toFloat()
            _rotVector[2] = (q3 / norm).toFloat()
            _rotVector[3] = (q0 / norm).toFloat()
         }
         SensorManager.getRotationMatrixFromVector(_rotMatrix, _rotVector)
         SensorManager.getOrientation(_rotMatrix, _orientationAngles)
      } else if (_accData.isNotEmpty() && _magnetometerData.isNotEmpty()) {
         if (SensorManager.getRotationMatrix(_rotMatrix, _inclinationMatrix, _accData, _magnetometerData)) {
            SensorManager.getOrientation(_rotMatrix, _orientationAngles)
         }
      }

      val yaw = _orientationAngles[0] * RADIANS_TO_DEGREES
      val pitch = _orientationAngles[1] * RADIANS_TO_DEGREES
      val roll = _orientationAngles[2] * RADIANS_TO_DEGREES

      sensorValue = sensorValue.copy(
         yaw = yaw,
         pitch = pitch,
         roll = roll,
         accLx = _accData[0],
         accLy = _accData[1],
         accLz = _accData[2],
      )
      return sensorValue
   }

   private fun startListening(listener: SensorEventListener) {
      logInfo(TAG, "startListening()")
      val delayType = SensorManager.SENSOR_DELAY_NORMAL

      _pressureSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      _lightSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      _proximitySensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      _accSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      _magnetometerSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
      _rotVecSensor?.let { _sensorManager.registerListener(listener, it, delayType) }
   }

   private fun stopListening(listener: SensorEventListener) {
      logInfo(TAG, "stopListening()")
      _sensorManager.unregisterListener(listener)
   }

   companion object {
      private const val TAG = "<-AppSensorManager"
      private const val RADIANS_TO_DEGREES = 57.29578f
   }
}