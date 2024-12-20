package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.openAppSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Modifier

@Composable
fun RequestPermissions(
   permissionsDeferred: CompletableDeferred<Boolean>
) {
   val tag = "<-RequestPermissions"
   val context = LocalContext.current

   val permissions: Array<String> =
      remember { getPermissionsFromManifest(context) }
   val permissionStates: MutableState<Map<String, Boolean>> =
      remember { mutableStateOf(permissions.associateWith { false }) }
   val showRationaleStates = remember { mutableStateOf(permissions.associateWith { false }) }

   val coroutineScope = rememberCoroutineScope()

   // Local state for permission queue
   val permissionQueue: SnapshotStateList<String> = remember { mutableStateListOf<String>() }
   val permissionsToRequest: MutableList<String> = remember { mutableListOf() }

   // Setup multiple permission request launcher (ActivityCompat.requestPermissions)
   val requestLauncher = rememberLauncherForActivityResult(
      // RequestMultiplePermissions() is a built-in ActivityResultContract
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // Callback for the result of the permission request
      // the result is a Map<String, Boolean> with key=permission value=isGranted
      onResult = { grantResults: Map<String, Boolean> ->
         grantResults.forEach { (permission, isGranted) ->
            logDebug(tag, "$permission = $isGranted")
            if (!isGranted && !permissionQueue.contains(permission)) {
               logDebug(tag, "add permission to queue")
               permissionQueue.add(permission)
            }

            // gemini
            if (!isGranted) {
               if (ActivityCompat.shouldShowRequestPermissionRationale(
                     context as Activity,
                     permission
                  )
               ) {
                  showRationaleStates.value += (permission to true)
                  logDebug(tag, "showRationaleStates: $permission ${showRationaleStates.value[permission]}")
               } else {
                  //onPermissionPermanentlyDenied(permission)
                  logVerbose(tag, "gemini: onPermissionPermanentlyDenied() $permission")
               }
            }
            // end gemini

         }
         // set the deferred to true, when all perssions are granted
         if (grantResults.all { it.value }) {
            logInfo(tag, "All permissions already granted")
            permissionsDeferred.complete(true)
         }

         // gemini
         if (permissionStates.value.all { it.value }) {
            logVerbose(tag, "gemini: onAllPermissionsGranted()")
         }
         // end gemini
      }
   )

   // launch permission requests that are not already granted
   LaunchedEffect(Unit) {
      // Filter permissions from manifest that are not granted yet
      filterPermissions(context, permissions) { permission ->
//       logInfo(tag, "Permission to request: $permission")
         permissionsToRequest.add(permission)
      }
      // launch permission requests that are not already granted
      if (permissionsToRequest.isNotEmpty()) {
         requestLauncher.launch(permissionsToRequest.toTypedArray())
      } else {
         logInfo(tag, "no more permissions to request")
         permissionsDeferred.complete(true)
      }
   }

   /*
   permissions.forEach { permission ->
      if (showRationaleStates.value[permission] == true) {

         val isPermanentlyDeclined =
            !(context as Activity).shouldShowRequestPermissionRationale(permission)
         val permissionText = getPermissionText(context, permission)
            ?.getDescription(isPermanentlyDeclined)
            ?: "no text availible"

         AlertDialog(
            onDismissRequest = {
               showRationaleStates.value += (permission to false)
               logDebug(tag, "onDismissRequest showRationaleStates: $permission ${showRationaleStates.value[permission]}")
            },
            title = { Text(stringResource(R.string.permissionRequired)) },
            //text = { Text("This app needs access to $permission to provide its functionality.") },
            text = { Text(permissionText) },
            confirmButton = {
               Button(onClick = {
                  showRationaleStates.value += (permission to false)
                  logDebug(tag, "confirm showRationaleStates: $permission ${showRationaleStates.value[permission]}")
                  requestLauncher.launch(arrayOf(permission))
               }) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            dismissButton = {
               Button(onClick = {
                  showRationaleStates.value += (permission to false)
                  logDebug(tag, "dismiss showRationaleStates: $permission ${showRationaleStates.value[permission]}")

                  if (isPermanentlyDeclined) {
                     context.openAppSettings()
                  }
               }) {
                  Text(text = stringResource(R.string.refuse))
//                  Text("Deny")
               }
            }
         )
      }
   }
*/

   // Handle permission rationale and app settings
   permissionQueue.reversed().forEach { permission ->
      var dialogOpen by remember { mutableStateOf(true) }
      val isPermanentlyDeclined =
         (context as Activity).shouldShowRequestPermissionRationale(permission)
      val permissionText = getPermissionText(context, permission)

      logDebug(tag, "AlertDialog showRationaleStates: $permission ${showRationaleStates.value[permission]}")
      if (dialogOpen) {
         logDebug(tag, "Alert Dialog $permission")
         AlertDialog(
            onDismissRequest = {
               dialogOpen = false
               showRationaleStates.value += (permission to false)
               logDebug(tag, "onDismissRequest showRationaleStates: $permission ${showRationaleStates.value[permission]}")
            },
            confirmButton = {
               Button(
                  onClick = {
                     showRationaleStates.value += (permission to false)
                     logDebug(tag, "confirm showRationaleStates: $permission ${showRationaleStates.value[permission]}")
                     requestLauncher.launch(arrayOf(permission))
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            dismissButton = {
               Button(
                  onClick = {
                     showRationaleStates.value += (permission to false)
                     logDebug(tag, "dismiss showRationaleStates: $permission ${showRationaleStates.value[permission]}")

                     // Show rationale
                     if (!isPermanentlyDeclined) {
                        logDebug(tag, "dismiss: ShowRationale $permission")
                        permissionQueue.remove(permission)
                        requestLauncher.launch(arrayOf(permission))
                     }
                     // Permission is permanently denied, open app settings
                     else {
                        val text = permissionText?.getDescription(isPermanentlyDeclined)

                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                        coroutineScope.launch {
                           delay(2000)
                           context.openAppSettings()
                        }

//                   //   context.finish()
                     }
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.refuse))
               }
            },
            icon = {},
            title = { Text(text = stringResource(R.string.permissionRequired)) },
            text = {
               Text(text = permissionText?.getDescription(!isPermanentlyDeclined) ?: "")
            }
         )
      }
   }
}

private fun filterPermissions(
   context: Context,
   permissionsFromManifest: Array<String>,
   onPermissionToRequest: (String) -> Unit
) {
   val tag = "<-FilterPermissions"

   permissionsFromManifest.forEach { permission ->
      // is permission already granted?
      if (ContextCompat.checkSelfPermission(context, permission) ==
         PackageManager.PERMISSION_GRANTED
      ) {
         logDebug(tag, "already granted:       $permission")
         return@forEach
      }

      // no permission check needed
      if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }
      if (permission == Manifest.permission.READ_EXTERNAL_STORAGE &&  Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }

      if (permission == Manifest.permission.FOREGROUND_SERVICE_LOCATION
      ) {
         logDebug(tag, "implicit granted:      $permission")
         return@forEach
      }

      logDebug(tag, "Permission to request: $permission")
      onPermissionToRequest(permission)
   }
}

private fun getPermissionsFromManifest(context: Context): Array<String> {
   val packageInfo = context.packageManager
      .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
   return packageInfo.requestedPermissions ?: emptyArray()
}

private fun getPermissionText(context: Context, permission: String): IPermissionText? {
   return when (permission) {
      // Permissions that have to be granted by the user
      Manifest.permission.CAMERA -> PermissionCamera(context, permission)
      Manifest.permission.RECORD_AUDIO -> PermissionRecordAudio(context, permission)
      Manifest.permission.READ_EXTERNAL_STORAGE -> PermissionExternalStorage(context, permission)
      Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionExternalStorage(context, permission)
      Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation(context, permission)
      Manifest.permission.ACCESS_FINE_LOCATION -> PermissionFineLocation(context, permission)
      else -> PermissionDefault(context, permission)
   }
}