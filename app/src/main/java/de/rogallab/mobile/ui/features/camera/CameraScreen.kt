package de.rogallab.mobile.ui.features.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.composables.AppBottomBar
import de.rogallab.mobile.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
   viewModel: CameraViewModel,
   tag:String = "<-CameraScreen"
) {
   AppTheme {
      val context = LocalContext.current

      var recordingState: MutableState<Recording?> = remember { mutableStateOf(null) }

      val bitmaps: List<Bitmap> by viewModel.bitmapsStateFlow.collectAsStateWithLifecycle()

      val scaffoldState = rememberBottomSheetScaffoldState()
      val coroutineScope = rememberCoroutineScope()

      BottomSheetScaffold(
         topBar = {
            TopAppBar(
               title = { Text(text = "Kamera & Video") },
               navigationIcon = {
                  IconButton(
                     onClick = { viewModel.onNavigate(NavEvent.NavigateHome) }
                  ) {
                     Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back))
                  }
               }
            )
         },
         scaffoldState = scaffoldState,
         sheetPeekHeight = 0.dp,
         sheetContent = {
            PhotoContent(
               bitmaps = bitmaps,
               modifier = Modifier.fillMaxWidth()
            )
         }
      ) { paddingValues ->

         Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
         ) {
            CameraContent(
               controller = viewModel.cameraController,
               modifier = Modifier.fillMaxSize()
            )

            // CameraControls(
            IconButton(
               onClick = {
                  // switch camera front/back
                  viewModel.cameraController.cameraSelector =
                     if (viewModel.cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                     else
                        CameraSelector.DEFAULT_BACK_CAMERA
               },
               modifier = Modifier.offset(16.dp, 16.dp)
            ) {
               Icon(
                  imageVector = Icons.Default.Cameraswitch,
                  contentDescription = "Switch Front/Back"
               )
            }

            Row(
               modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.BottomCenter)
                  .padding(16.dp),
               horizontalArrangement = Arrangement.SpaceAround
            ) {
               IconButton(
                  onClick = {
                     coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                     }
                  }
               ) {
                  Icon(
                     imageVector = Icons.Default.Photo,
                     contentDescription = "Open Gallery"
                  )
               }
               IconButton(
                  onClick = {
                     logInfo(tag, "Take photo")
                     takePhoto(
                        context = context,
                        cameraController = viewModel.cameraController,
                        cameraPermissions = viewModel.CAMERAX_PERMISSIONS,
                        onPhotoTaken = viewModel::onAddPhoto
                     )
                  }
               ) {
                  Icon(
                     imageVector = Icons.Default.PhotoCamera,
                     contentDescription = "Take photo"
                  )
               }
               IconButton(
                  onClick = {
                     logInfo(tag, "Record video start/stop")

                     recordVideo(
                        context = context,
                        cameraController = viewModel.cameraController,
                        recording = recordingState.value,
                        onRecordVideo = { it -> recordingState.value = it },
                        cameraPermissions = viewModel.CAMERAX_PERMISSIONS
                     )
                  }
               ) {
                  Icon(
                     imageVector = Icons.Default.Videocam,
                     contentDescription = "Record video"
                  )
               }

            }


         }
      }
   }
}


private fun takePhoto(
   context: Context,
   cameraController: CameraController,
   cameraPermissions: Array<String>,
   onPhotoTaken: (Bitmap) -> Unit,
   tag: String = "<-CameraScreen"
){
   if( !hasRequiredPermission(context, cameraPermissions )) {
      logError(tag, "takePhoto(): Missing permissions to take a photo")
      return
   }

   cameraController.takePicture(
      ContextCompat.getMainExecutor(context),
      object : ImageCapture.OnImageCapturedCallback() {

         // Called when image capture is successful
         override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)

            // Play camera click
            val mediaPlayer = MediaPlayer.create(context, R.raw.camera_click)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }

            // rotate image to correct orientation
            val matrix = android.graphics.Matrix().apply {
               postRotate(image.imageInfo.rotationDegrees.toFloat())
            }
            val rotatedBitmap = Bitmap.createBitmap(
               image.toBitmap(),
               0,
               0,
               image.width,
               image.height,
               matrix,
               true //Bitmap.Config.ARGB_8888
            )

            saveImage(rotatedBitmap, context, "Permmissions_Camera")

            onPhotoTaken(rotatedBitmap)  // Event ↑

         }
         // Called when image capture fails
         override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            logError("<-CameraScreen", "Error taking photo: ${exception.message}")
         }
      }
   )

}

@SuppressLint("MissingPermission")
private fun recordVideo(
   context: Context,
   recording: Recording?,
   cameraController: CameraController,
   onRecordVideo: (Recording?) -> Unit,
   cameraPermissions: Array<String>,
   tag: String = "<-CameraScreen"
) {

   logInfo(tag,"recordVideo(): recording = $recording")

   // is recording running, then stop it
   if( recording != null ) {
      recording.stop()
      // set recording state (not recording)
      onRecordVideo(null)
      return
   }

   if( !hasRequiredPermission(context, cameraPermissions )) {
      logError(tag, "recordVideo(): Missing permissions to record video")
      return
   }

   // file to save video
   val fileDir: File = context.getDir("files", Context.MODE_PRIVATE)
//   val file = File(videoDir, "${UUID.randomUUID()}.mp4")
   val file = File(fileDir, "myvideo.mp4")



   val recorder = Recorder.Builder()
      .setQualitySelector(QualitySelector.from(Quality.SD))
      .build()

   cameraController.videoCaptureQualitySelector =
      QualitySelector.from(Quality.HD)

   cameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)


   CoroutineScope(Dispatchers.Main).launch {
      cameraController.startRecording(
         FileOutputOptions.Builder(file).build(),
         // permission already checked
         AudioConfig.create(true),
         ContextCompat.getMainExecutor(context)
      ) { event ->

         when (event) {

            is VideoRecordEvent.Finalize -> {
               logDebug("<-CameraScreen", "Recording stopped")
               if (event.hasError()) {
                  logError("<-CameraScreen", "Error recording video: ${event.error}")
                  recording?.close()
                  Toast.makeText(context, "Video capture failed", Toast.LENGTH_SHORT).show()
               } else {
                  Toast.makeText(context, "Video saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
               }
            }
            is VideoRecordEvent.Start -> {
               logDebug("<-CameraScreen", "Recording started")
            }
            is VideoRecordEvent.Pause -> {
               logDebug("<-CameraScreen", "Recording paused")
            }
            is VideoRecordEvent.Resume -> {
               logDebug("<-CameraScreen", "Recording resumed")
            }

         }
      }.apply {
         // set recording state (is recording)
         onRecordVideo(this)
      }
   }// end CoroutineScope(Dispatchers.Main).launch
}



private fun hasRequiredPermission(
   context: Context,
   cameraPermissions: Array<String>
): Boolean = cameraPermissions.all {
      ContextCompat.checkSelfPermission(context, it) ==
         PackageManager.PERMISSION_GRANTED
   }



/// @param folderName can be your app's name
private fun saveImage(
   bitmap: Bitmap,
   context: Context,
   folderName: String
) {
   if (android.os.Build.VERSION.SDK_INT >= 29) {
      val values = contentValues()
      values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
      values.put(MediaStore.Images.Media.IS_PENDING, true)
      // RELATIVE_PATH and IS_PENDING are introduced in API 29.
      val uri: Uri? = context.contentResolver.insert(
         MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
      )
      if (uri != null) {
         saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
         values.put(MediaStore.Images.Media.IS_PENDING, false)
         context.contentResolver.update(uri, values, null, null)
      }
   } else {
      val directory = File(Environment.getExternalStorageDirectory().toString()
            + separator + folderName)
      // getExternalStorageDirectory is deprecated in API 29

      if (!directory.exists()) {
         directory.mkdirs()
      }
      val fileName = System.currentTimeMillis().toString() + ".png"
      val file = File(directory, fileName)
      saveImageToStream(bitmap, FileOutputStream(file))
      if (file.absolutePath != null) {
         val values = contentValues()
         values.put(MediaStore.Images.Media.DATA, file.absolutePath)
         // .DATA is deprecated in API 29
         context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      }
   }
}

private fun contentValues() : ContentValues {
   val values = ContentValues()
   values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
   values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
   values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
   return values
}

private fun saveImageToStream(
   bitmap: Bitmap,
   outputStream: OutputStream?
) {
   if (outputStream != null) {
      try {
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
         outputStream.close()
      } catch (e: Exception) {
         e.printStackTrace()
      }
   }
}
