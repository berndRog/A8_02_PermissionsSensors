package de.rogallab.mobile.domain

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface IAppLocationManager {
   fun getLocation(onSuccess: (Location) -> Unit)
   fun trackLocation(interval: Long = 5000L): Flow<Location>
}