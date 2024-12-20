package de.rogallab.mobile.ui.features.orientation.composables

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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.epochToLocalDateTime
import de.rogallab.mobile.domain.utilities.toTimeString
import de.rogallab.mobile.ui.navigation.composables.AppBottomBar
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.features.orientation.SensorIntent
import de.rogallab.mobile.ui.features.orientation.SensorUiState
import de.rogallab.mobile.ui.features.orientation.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorListScreen(
   viewModel: SensorViewModel,
   navController: NavController
) {
   val tag = "<-SensorsListScreen"

   // Environment & Orientation sensors state
   val sensorUiState: SensorUiState
      by viewModel.sensorUiStateFlow.collectAsStateWithLifecycle()

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
            title = { Text(text = stringResource(R.string.sensorsList)) },
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
         .padding(paddingValues)
         .padding(horizontal = 16.dp)
         .fillMaxSize()
      )  {
         Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
         ) {
            Button(
               modifier = Modifier.weight(0.4f),
               onClick = {
                  logDebug(tag, "Start Sensor Listener")
                  viewModel.processIntent(SensorIntent.Start)
               }
            ) {
               Text(text = "Start")
            }

            Spacer(modifier = Modifier.weight(0.1f))

            Button(
               modifier = Modifier.weight(0.4f),
               onClick = {
                  logDebug(tag, "Stop Sensor Listener")
                  viewModel.processIntent(SensorIntent.Stop)
               }
            ) {
               Text(text = "Stop")
            }
         }

         LazyColumn() {
            val sensorValues = sensorUiState.ringBuffer
               .toList()
               .sortedByDescending { it.time }

            item {
               Row(modifier = Modifier.fillMaxWidth()) {
                  Text(modifier = Modifier.weight(0.25f),
                     text = "Time",
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = "Yaw",
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = "Pitch",
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = "Roll",
                     textAlign = TextAlign.End)
               }
            }

            items(
               count = sensorValues.size,
            ) { it: Int ->  // index

               Row(modifier = Modifier.fillMaxWidth()) {
                  Text(modifier = Modifier.weight(0.25f),
                     text = epochToLocalDateTime(sensorValues[it].time).toTimeString(),
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = String.format("%.1f", sensorValues[it].yaw),
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = String.format("%.1f", sensorValues[it].pitch),
                     textAlign = TextAlign.End)
                  Text(modifier = Modifier.weight(0.2f),
                     text = String.format("%.1f", sensorValues[it].roll),
                     textAlign = TextAlign.End)
               }
            }
         }
      }
   }
}

