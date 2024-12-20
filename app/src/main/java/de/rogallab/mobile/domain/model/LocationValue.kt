package de.rogallab.mobile.domain.model

data class LocationValue(
   val time: Long = System.currentTimeMillis(),
   val latitude: Double = 0.0,
   val longitude: Double = 0.0,
)