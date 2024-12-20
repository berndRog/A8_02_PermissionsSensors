package de.rogallab.mobile.ui.features.location

import de.rogallab.mobile.domain.model.LocationValue
import de.rogallab.mobile.domain.utilities.RingBuffer

data class LocationUiState(
   val last: LocationValue = LocationValue(),
   val ringBuffer: RingBuffer<LocationValue> = RingBuffer(600)
)