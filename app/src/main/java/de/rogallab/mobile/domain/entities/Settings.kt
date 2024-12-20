package de.rogallab.mobile.domain.entities

data class Settings(
   val isLocationSensorEnabled: Boolean = false,
   val isPressureSensorEnabled: Boolean = true,
   val isLightSensorEnabled: Boolean = true,
   val isOrientationSensorEnabled: Boolean = false,
)