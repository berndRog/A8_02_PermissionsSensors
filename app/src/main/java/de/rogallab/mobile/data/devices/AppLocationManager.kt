package de.rogallab.mobile.data.devices

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import de.rogallab.mobile.domain.IAppLocationManager
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class AppLocationManager(
   private val _context: Context,
): IAppLocationManager {

    // required permissions for location updates
    private val _requiredPermissions = arrayOf(
       Manifest.permission.POST_NOTIFICATIONS,
       Manifest.permission.ACCESS_COARSE_LOCATION,
       Manifest.permission.ACCESS_FINE_LOCATION,
       Manifest.permission.FOREGROUND_SERVICE,
    )

    private val fusedLocationClient =
       LocationServices.getFusedLocationProviderClient(_context)

    override fun getLocation(
       onSuccess: (location: Location) -> Unit
    ) {

        val locationManager =
           _context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled =
           locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled =
           locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            //throw ILocationClient.LocationException("GPS is disabled")
        }

        fusedLocationClient
           .lastLocation
           .addOnSuccessListener { location ->
               logDebug(TAG, "getLocation: ${location.latitude}, ${location.longitude}")
               onSuccess(location)
           }
    }

    override fun trackLocation(
       interval: Long
    ): Flow<Location> {
        return callbackFlow {

            val locationCallback = locationCallback { location ->
                launch {
                    // send location to the flow
                    send(location)
                }
            }

            val request = LocationRequest.Builder(interval)
               .setIntervalMillis(interval)
               .setMinUpdateIntervalMillis(interval)
               .build()

            if (_requiredPermissions.all { permission ->
                   ContextCompat.checkSelfPermission(_context, permission
                   ) == PackageManager.PERMISSION_GRANTED
               }
            ) {
                logDebug(TAG, "Permission granted: requestLocationUpdates")
                fusedLocationClient.requestLocationUpdates(
                   request,
                   locationCallback,
                   Looper.getMainLooper()
                )
            }

            awaitClose {
                logDebug(TAG, "awaitClose")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun locationCallback(
        onResult: (location: Location) -> Unit
    ): LocationCallback {

        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    onResult(location)
                }
            }
        }
    }

    companion object {
        const val TAG = "<-AppLocationManager"
    }
}