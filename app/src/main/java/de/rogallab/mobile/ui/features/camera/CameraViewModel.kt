package de.rogallab.mobile.ui.features.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel(
   private val _context: Context,
   private val _lifecyleOwner: LifecycleOwner,
   private val _errorHandler: IErrorHandler,
   private val _navHandler: INavigationHandler,
   private val _exceptionHandler: CoroutineExceptionHandler
): ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navHandler {

   val CAMERAX_PERMISSIONS = arrayOf(
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
      //android.Manifest.permission.WRITE_EXTERNAL_STORAGE
   )

   val cameraController = LifecycleCameraController(_context)
      .apply {
         bindToLifecycle(_lifecyleOwner)
         setEnabledUseCases(
            CameraController.IMAGE_CAPTURE or
            CameraController.VIDEO_CAPTURE
         )
      }

   private val _bitmapsStateFlow: MutableStateFlow<List<Bitmap>> = MutableStateFlow(emptyList())
   val bitmapsStateFlow: StateFlow<List<Bitmap>> = _bitmapsStateFlow.asStateFlow()

   fun onAddPhoto(bitmap: Bitmap) {
      _bitmapsStateFlow.update { bitmaps ->
         // add the new bitmap to the observable list of bitmaps
         bitmaps + bitmap
      }
      logDebug(TAG, "onAddPhoto: ${_bitmapsStateFlow.value.size}")
   }

   companion object {
      private const val TAG = "<-CameraViewModel"
   }
}