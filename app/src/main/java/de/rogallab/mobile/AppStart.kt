package de.rogallab.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.features.location.AppLocationService
import de.rogallab.mobile.ui.features.orientation.AppSensorService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup.onKoinStartup
import org.koin.core.logger.Level


@Suppress("OPT_IN_USAGE")
class AppStart : Application(), DefaultLifecycleObserver {

   init{
      logInfo(TAG, "init: onKoinStartUp{...}")
      onKoinStartup {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@AppStart)
         // Load modules
         modules(domainModules, dataModules, uiModules)
      }
   }


   override fun onCreate() {

      super<Application>.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(TAG, "onCreate() maxMemory $maxMemory kB")


      // Register the lifecycle observer
      ProcessLifecycleOwner.get().lifecycle.addObserver(this)

      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

         val notificationChannel = NotificationChannel(
            AppLocationService.LOCATION_CHANNEL,
            "Location",
            NotificationManager.IMPORTANCE_LOW
         )

         val orientationChannel = NotificationChannel(
            AppSensorService.ORIENTATION_CHANNEL,
            "Orientation",
            NotificationManager.IMPORTANCE_LOW
         )
         val notificationManager:NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

         notificationManager.createNotificationChannel(notificationChannel)
         notificationManager.createNotificationChannel(orientationChannel)
      }
   }


   // stop the location foreground service when the app is closed
   override fun onStop(owner: LifecycleOwner) {
      logInfo(TAG, "onStop()")
      val stopIntent = Intent(this, AppLocationService::class.java)
      stopService(stopIntent)
   }

   companion object {
      private const val TAG = "<-AppStart"
      const val IS_DEBUG = true
      const val IS_INFO = true
      const val IS_VERBOSE = true

      const val DATABASE_NAME = "db_8_02_PermissionsServices.db"
      const val DATABASE_VERSION = 1

      //    const val BASE_URL: String = "http://10.0.2.2:5010/"        // localhost für AVD
      const val BASE_URL: String = "http://192.168.178.23:6100/"  // physical mobile device
      const val API_KEY:  String = ""
      const val BEARER_TOKEN:  String = ""

   }
}