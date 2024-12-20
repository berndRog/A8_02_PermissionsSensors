package de.rogallab.mobile.ui.features.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun PhotoContent(
   bitmaps: List<Bitmap>,
   modifier: Modifier = Modifier
) {

   if (bitmaps.isEmpty()) {
      Box(
         contentAlignment = Alignment.Center,
         modifier = Modifier.padding(16.dp)
      ) {
         Text(
            text = "No photos availible",
            style = MaterialTheme.typography.bodyLarge
         )
      }
   } else {

      LazyVerticalStaggeredGrid(
         columns = StaggeredGridCells.Fixed(2),
         horizontalArrangement = Arrangement.spacedBy(16.dp),
         verticalItemSpacing = 16.dp,
         contentPadding = PaddingValues(16.dp),
         modifier = modifier,

         ) {
         items(bitmaps) { bitmap ->
            AsyncImage(
               modifier = Modifier
                  .height(250.dp)
                  .clip(RoundedCornerShape(percent = 5)),
               model = bitmap,
               contentDescription = "Bild des Kontakts",
               alignment = Alignment.Center,
               contentScale = ContentScale.Fit
            )
         }
      }
   }

}