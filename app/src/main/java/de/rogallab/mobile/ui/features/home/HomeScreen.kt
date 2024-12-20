package de.rogallab.mobile.ui.features.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.navigation.composables.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
   viewModel: HomeViewModel? = null,
   navController: NavController? = null
) {

   val activity = LocalContext.current as Activity
   BackHandler{
      activity.finish()
   }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.safeGestures)
   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = "Start") },
            navigationIcon = {
               IconButton(
                  onClick = {  activity.finish()}
               ) {
                  Icon(imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      bottomBar = {
         if (navController != null && viewModel != null)
            AppBottomBar(navController, viewModel)
      },
      snackbarHost = {
         SnackbarHost(snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { paddingValues: PaddingValues ->

      Column(
         modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
         verticalArrangement = Arrangement.Center,
         horizontalAlignment = Alignment.CenterHorizontally
      ) {

         Image(
            painter = painterResource(id = R.drawable.permiss02),
            contentDescription = "Example Image",
            contentScale = ContentScale.Fit
         )

         Spacer(modifier = Modifier.height(48.dp))

         Text(
            text = "Grant Permissions",
            style = MaterialTheme.typography.headlineMedium
         )
         Text(
            text = "for Devices & Sensors",
            style = MaterialTheme.typography.headlineMedium
         )
         Spacer(modifier = Modifier.height(40.dp))

         Text(
            text = "Berechtigung gewähren (Freigabe)",
            style = MaterialTheme.typography.bodyLarge
         )
         Text(
            text = "Geräte (Kamera, Mikrofon)",
            style = MaterialTheme.typography.bodyLarge
         )
         Text(
            text = "Sensoren (Standort, Orientierung, ..)",
            style = MaterialTheme.typography.bodyLarge
         )

      }
   }
}