package de.rogallab.mobile.ui.features.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraContent(
   controller: LifecycleCameraController,
   modifier: Modifier = Modifier
) {

   val lifecycleOwner = LocalLifecycleOwner.current

   AndroidView(
      factory = { context ->
         PreviewView(context).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
         }
      },
      modifier = modifier
   )
}