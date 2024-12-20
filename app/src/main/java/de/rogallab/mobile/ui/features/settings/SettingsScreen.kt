package de.rogallab.mobile.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
   settingsViewModel: SettingsViewModel
) {

   val settingsUiState by
      settingsViewModel.settingsUiStateFlow.collectAsState()

   Column(modifier = Modifier.padding(16.dp)) {
      Text("Sensor Settings", style = MaterialTheme.typography.displayMedium)

      SwitchSetting(
         label = "Drucksensor",
         isChecked = settingsUiState.isPressureSensorEnabled,
         onCheckedChange = {
            settingsViewModel.updateSettings(
               settingsUiState.copy(isPressureSensorEnabled = it)
            )
         }
      )

      SwitchSetting(
         label = "Helligkeitssensor",
         isChecked = settingsUiState.isLightSensorEnabled,
         onCheckedChange = {
            settingsViewModel.updateSettings(
               settingsUiState.copy(isLightSensorEnabled = it)
            )
         }
      )

      SwitchSetting(
         label = "Orientierungssensor",
         isChecked = settingsUiState.isOrientationSensorEnabled,
         onCheckedChange = {
            settingsViewModel.updateSettings(
               settingsUiState.copy(
                  isOrientationSensorEnabled = it
               )
            )
         }
      )
   }
}

@Composable
fun SwitchSetting(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
   Row(verticalAlignment = Alignment.CenterVertically) {
      Text(label, modifier = Modifier.weight(1f))
      Switch(checked = isChecked, onCheckedChange = onCheckedChange)
   }
}