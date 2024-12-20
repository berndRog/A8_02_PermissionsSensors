package de.rogallab.mobile.ui.features.orientation

sealed class SensorIntent {
   data object Start : SensorIntent()
   data object Stop: SensorIntent()
}