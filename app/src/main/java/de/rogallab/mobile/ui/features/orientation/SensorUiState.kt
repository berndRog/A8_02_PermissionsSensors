package de.rogallab.mobile.ui.features.orientation

import de.rogallab.mobile.domain.model.SensorValue
import de.rogallab.mobile.domain.utilities.RingBuffer

data class SensorUiState(
   val last: SensorValue? = null,
   val ringBuffer: RingBuffer<SensorValue> = RingBuffer(600),
)