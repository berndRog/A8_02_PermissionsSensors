package de.rogallab.mobile.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.features.orientation.SensorViewModel
import de.rogallab.mobile.ui.features.home.HomeScreen
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.navigation.composables.AppDrawer
import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import de.rogallab.mobile.ui.permissions.RequestPermissions
import de.rogallab.mobile.ui.theme.AppTheme
import kotlinx.coroutines.CompletableDeferred
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext

class MainActivity : BaseActivity(TAG) {

   private val _navigationViewModel: NavigationViewModel by inject()
   private val _sensorViewModel: SensorViewModel by inject<SensorViewModel>()


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {

         // Request permissions, wait for the permissions result
         val permissionsDeferred: CompletableDeferred<Boolean> =
            remember { CompletableDeferred() }
         // Wait for the permissions result, then continue
         var permissionsGranted: Boolean
            by remember { mutableStateOf<Boolean>(false) }

         KoinContext() {
            AppTheme {
               Surface(modifier = Modifier.fillMaxSize()) {
                  val drawerState = rememberDrawerState(DrawerValue.Closed)
                  val scope = rememberCoroutineScope()

                  ModalNavigationDrawer(
                     drawerState = drawerState,
                     drawerContent = {
                        Surface(
                           modifier = Modifier.fillMaxWidth(0.75f), // Set the width of the drawer
                        ) {
                           AppDrawer(drawerState, _navigationViewModel, scope)
                        }
                     }
                  ) {

                     RequestPermissions(permissionsDeferred)

                     LaunchedEffect(Unit) {
                        // wait until permissions are granted
                        permissionsGranted = permissionsDeferred.await()
                        if (permissionsGranted)
                           logInfo(TAG, "Permissions are granted")
                         else
                           logError(TAG, "Permissions not granted")
                     }

                     // Show the home screen if permissions are not granted
                     if (permissionsGranted) AppNavHost(navigationViewModel = _navigationViewModel)
                     else HomeScreen()

                  } // ModalNavigationDrawer
               } // Surface
            } // AppTheme
         } // KoinContext
      } // setContent
   } // onCreate




   companion object {
      private const val TAG = "<-MainActivity"
      private const val PERMISSION_REQUEST_CODE = 999
   }
}

// E X T E N S I O N  F U C T I O N S

// static extension function for Activity
fun Context.openAppSettings() {
   Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", packageName, null)
   ).also(::startActivity)
}

fun Context.hasLocationPermission(): Boolean {
   return ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_COARSE_LOCATION
   ) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(
         this,
         Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
}

