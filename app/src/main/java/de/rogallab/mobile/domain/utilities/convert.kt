package de.rogallab.mobile.domain.utilities

import android.location.Location
import de.rogallab.mobile.domain.model.LocationValue

fun formatf11p7(value: Double): String {
   if(value >= 0) return "%11.7f".format(value)
   return "%11.6f".format(value)
}


fun formatEpochLatLng(locationValue: LocationValue): String {
   val ldt = epochToLocalDateTime(locationValue.time).toDateTimeString()
   val latitude = formatf11p7(locationValue.latitude)
   val longitude = formatf11p7(locationValue.longitude)
   return "$ldt  $latitude/$longitude"
}

fun formatEpochLatLng(location: Location): String {
   val ldt = epochToLocalDateTime(location.time).toDateTimeString()
   val latitude = formatf11p7(location.latitude)
   val longitude = formatf11p7(location.longitude)
   return "$ldt  $latitude/$longitude"
}