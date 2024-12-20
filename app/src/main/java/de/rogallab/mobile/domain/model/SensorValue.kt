package de.rogallab.mobile.domain.model

data class SensorValue(

   val time: Long = System.currentTimeMillis(), // epoch

   // Environment ----------------------------------------------------
   // pressure in milli Pascal
   val pressure: Float = 0.0f,
   // light in lumen
   val light: Float = 0.0f,

   // Orientation ----------------------------------------------------
   // yaw is the rotation around the device vertical axis
   val yaw: Float = 0.0f,
   // pitch is the rotation around the wings (transverse axis)
   val pitch: Float = 0.0f,
   // roll is the rotation around the fuselage (longitudinal axis)
   val roll: Float = 0.0f,
   // acceleration in local x-direction
   val accLx: Float = 0.0f,
   // acceleration in local y-direction
   val accLy: Float = 0.0f,
   // acceleration in local z-direction
   val accLz: Float = 0.0f

)
