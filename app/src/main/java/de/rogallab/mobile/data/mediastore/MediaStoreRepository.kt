package de.rogallab.mobile.data.mediastore

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MediaStoreRepository(
   private val _context: Context
) : IMediaStoreRepository {

   // return the uri of the saved image
   override suspend fun saveImage(
      bitmap: Bitmap
   ): String? = withContext(Dispatchers.IO) {
      // resolver is used to access the MediaStore
      val resolver = _context.contentResolver

      // imageCollection is the directory where the image will be saved
      val imageCollection = MediaStore.Images.Media.getContentUri(
         MediaStore.VOLUME_EXTERNAL_PRIMARY
      )

      // timeMillis is the time when the image was taken (i.e. stored)
      val timeMillis = System.currentTimeMillis()

      // imageContentValues are the meta data of the image
      val imageContentValues = ContentValues().apply {
         put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
         put(MediaStore.Images.Media.DISPLAY_NAME, "$timeMillis.jpg")
         put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
         put(MediaStore.Images.Media.DATE_TAKEN, timeMillis)
         put(MediaStore.Images.Media.IS_PENDING, 1)  // not yet finished
      }

      // insert the image into the MediaStore
      val imageMediaStoreUri = resolver.insert(
         imageCollection,
         imageContentValues
      )
      imageMediaStoreUri?.let { uri ->
         try {
            // open the output stream to write the image
            resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            imageContentValues.clear()
            imageContentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(
               uri, imageContentValues, null, null
            )
            logDebug(TAG, "savedImage: $uri")
            return@withContext uri.toString()

         } catch (e: Exception) {
            logError("MediaStoreUtil", "Failed to save image")
            resolver.delete(uri, null, null)
            //throw IOException("Failed to save video", e)
            return@withContext null
         }
      } // end of let
   } // end of withContext

   // return the uri of the saved video
   override suspend fun saveVideo(
      file: File
   ): String? = withContext(Dispatchers.IO) {
      // resolver is used to access the MediaStore
      val resolver = _context.contentResolver

      // videoCollection is the directory where the video will be saved
      val videoCollection = MediaStore.Video.Media.getContentUri(
         MediaStore.VOLUME_EXTERNAL_PRIMARY
      )

      // timeMillis is the time when the image was taken (i.e. stored)
      val timeMillis = System.currentTimeMillis()

      // imageContentValues are the meta data of the image
      val videoContentValues = ContentValues().apply {
         put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
         put(MediaStore.Video.Media.DISPLAY_NAME, "$timeMillis.mp4")
         put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
         put(MediaStore.Video.Media.DATE_ADDED, timeMillis)
         put(MediaStore.Video.Media.IS_PENDING, 1)  // not yet finished
      }

      // insert the image into the MediaStore
      val videoMediaStorUri = resolver.insert(
         videoCollection,
         videoContentValues
      )
      videoMediaStorUri?.let { uri ->
         try {
            // open the output stream to write the video file
            resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
               // open the input stream to read the video file
               resolver.openInputStream(
                  Uri.fromFile(file)
               )?.use { inputStream: InputStream ->
                  // copy the input stream to the output stream
                  inputStream.copyTo(outputStream)
               }
            }
            videoContentValues.clear()
            videoContentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(
               uri, videoContentValues, null, null
            )
            logDebug(TAG, "savedVideo: $uri")
            return@withContext uri.toString()

         } catch (e: Exception) {
            logError("MediaStoreUtil", "Failed to save image")
            resolver.delete(uri, null, null)
            // throw IOException("Failed to save image", e)
            return@withContext null
         }
      } // end of let
   } // end of withContext

   // return the uri of the saved audio
   override suspend fun saveAudio(
      file: File
   ): String? = withContext(Dispatchers.IO) {
      // resolver is used to access the MediaStore
      val resolver = _context.contentResolver

      // videoCollection is the directory where the video will be saved
      val audioCollection = MediaStore.Audio.Media.getContentUri(
         MediaStore.VOLUME_EXTERNAL_PRIMARY
      )

      // timeMillis is the time when the audio was added (i.e. stored)
      val timeMillis = System.currentTimeMillis()

      // audioContentValues are the meta data of the audio
      val audioContentValues = ContentValues().apply {
         put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
         put(MediaStore.Audio.Media.DISPLAY_NAME, "$timeMillis.mpeg")
         put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
         put(MediaStore.Audio.Media.DATE_ADDED, timeMillis)
         put(MediaStore.Audio.Media.IS_PENDING, 1)  // not yet finished
      }

      // insert the audio into the MediaStore
      val audioMediaStorUri = resolver.insert(
         audioCollection,
         audioContentValues
      )
      audioMediaStorUri?.let { uri ->
         try {
            // open the output stream to write the audio file
            resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
               // open the input stream to read the audio file
               resolver.openInputStream(
                  Uri.fromFile(file)
               )?.use { inputStream: InputStream ->
                  // copy the input stream to the output stream
                  inputStream.copyTo(outputStream)
               }
            }
            audioContentValues.clear()
            audioContentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(
               uri, audioContentValues, null, null
            )
            logDebug(TAG, "savedAudio: $uri")
            return@withContext uri.toString()

         } catch (e: Exception) {
            logError("MediaStoreUtil", "Failed to save audio file")
            resolver.delete(uri, null, null)
            // throw IOException("Failed to save audio file", e)
            return@withContext null
         }
      } // end of let
   } // end of withContext

   companion object {
      private const val TAG = "<-MediaStoreRepository"
   }


} //
