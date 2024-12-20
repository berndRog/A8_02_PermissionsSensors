package de.rogallab.mobile.ui.features.location.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.formatEpochLatLng
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.composables.AppBottomBar
import de.rogallab.mobile.ui.features.location.LocationIntent
import de.rogallab.mobile.ui.features.location.LocationUiState
import de.rogallab.mobile.ui.features.location.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
   viewModel: LocationViewModel,
   navController: NavController
) {

   val tag = "<-LocationsListScreen"
   val context = LocalContext.current

   // collect the locationUiState from the viewModel
   val locationUiState: LocationUiState
      by viewModel.locationUiStateFlow.collectAsStateWithLifecycle()

   // Handle back navigation
   BackHandler{
      logInfo(tag, "BackHandler -> navigate to Home")
      viewModel.onNavigate(NavEvent.NavigateBack(NavScreen.Home.route))
   }

   val snackbarHostState = remember { SnackbarHostState() }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.navigationBars)
      .add(WindowInsets.ime)
      .add(WindowInsets.safeGestures)

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.locationsList)) },
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
      bottomBar = {
         AppBottomBar(navController, viewModel)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
         }
      }
   ) { paddingValues: PaddingValues ->

      Column(modifier = Modifier
         .padding(paddingValues = paddingValues)
         .padding(horizontal = 16.dp)
      ) {

         Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
         ) {
            Button(
               modifier = Modifier.weight(0.4f),
               onClick = {
                  logDebug(tag,"Start AppLocationService")
                  viewModel.processIntent(LocationIntent.StartLocationService)
               }
            ) {
               Text(text = "Start")
            }

            Spacer(modifier = Modifier.weight(0.1f))

            Button(
               modifier = Modifier.weight(0.4f),
               onClick = {
                  logDebug(tag,"Stop AppLocationService")
                  viewModel.processIntent(LocationIntent.StopLocationService)
               }
            ) {
               Text(text = "Stop")
            }
         }

         val locationValue = locationUiState.last
         Text(
            text = formatEpochLatLng(locationValue), //"L/B:$latitude/$longitude",
            style = MaterialTheme.typography.bodySmall
         )

         GoogleMapsScreen(
            context = context,
            viewModel = viewModel
         )
      }
   }
}