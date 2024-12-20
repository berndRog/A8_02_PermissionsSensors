package de.rogallab.mobile.ui.features.people.composables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import de.rogallab.mobile.ui.features.people.whichImagePath
import org.koin.compose.koinInject

@Composable
fun SelectAndShowImage(
   localImage: String?, // State ↓
   remoteImage: String?,                  // State ↓
   onImagePathChange: (String) -> Unit,   // Event ↑
   imageLoader: ImageLoader = koinInject()
) {

   Row(
      modifier = Modifier
         .padding(vertical = 8.dp)
         .fillMaxWidth()
   ) {
      // localImage first
      var imagePath: String? = whichImagePath(localImage, remoteImage)
      if(!imagePath.isNullOrEmpty()) {
         AsyncImage(
            modifier = Modifier
               .size(width = 150.dp, height = 200.dp)
               .clip(RoundedCornerShape(percent = 5)),
            model = imagePath,
            imageLoader = imageLoader,
            contentDescription = "Bild des Kontakts",
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop
         )
      }
      Column(
         modifier = Modifier.fillMaxWidth(),
         verticalArrangement = Arrangement.Center
      ) {
         SelectPhotoFromGallery(
            onImagePathChanged = onImagePathChange
         )
         Spacer(modifier = Modifier.padding(vertical = 4.dp))

         CameraCheckPermission(
            onPermissionGranted = {
               CameraTakePhoto(onImagePathChange)
            }
         )
      }
   }
}