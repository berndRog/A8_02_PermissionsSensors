package de.rogallab.mobile.domain

import android.graphics.Bitmap
import java.io.File

interface IMediaStoreRepository {
   // return the uri of the saved image
   suspend fun saveImage(bitmap: Bitmap): String?

   // return the uri of the saved video
   suspend fun saveVideo(file: File): String?

   // return the uri of the saved audio
   suspend fun saveAudio(file: File): String?

} //
