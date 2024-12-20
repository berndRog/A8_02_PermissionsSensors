package de.rogallab.mobile.ui.features.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.ISettingsRepository
import de.rogallab.mobile.data.repositories.SettingsRepository
import de.rogallab.mobile.domain.entities.Settings
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.navigation.NavEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
   application: Application,
   private val _errorHandler: IErrorHandler,
   private val _navHandler: INavigationHandler,
   private val _exceptionHandler: CoroutineExceptionHandler
) : ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navHandler {

   // we must fix this by using a dependency injection framework
   private val _context: Context = application.applicationContext
   private val settingsRepository: ISettingsRepository =
      SettingsRepository(_context)

   // Expose sensor state to the UI via a StateFlow
   private val _settingsUiStateFlow:MutableStateFlow<SettingsUiState>
      = MutableStateFlow(SettingsUiState())
   val settingsUiStateFlow: StateFlow<SettingsUiState>
      = _settingsUiStateFlow.asStateFlow()

   init {
      loadSettings()
   }

   private fun loadSettings() {
      val settings = settingsRepository.getSettings()
      _settingsUiStateFlow.update { settingsUiState: SettingsUiState ->
         settingsUiState.copy(
            isLocationSensorEnabled = settings.isLocationSensorEnabled,
            isPressureSensorEnabled = settings.isPressureSensorEnabled,
            isLightSensorEnabled = settings.isLightSensorEnabled,
            isOrientationSensorEnabled = settings.isOrientationSensorEnabled
         )
      }
   }

   fun updateSettings(settingsUiState: SettingsUiState) {
      val settings = Settings(
         isLocationSensorEnabled = settingsUiState.isLocationSensorEnabled,
         isPressureSensorEnabled = settingsUiState.isPressureSensorEnabled,
         isLightSensorEnabled = settingsUiState.isLightSensorEnabled,
         isOrientationSensorEnabled = settingsUiState.isOrientationSensorEnabled
      )
      saveSettings(settings)
   }

   private fun saveSettings(settings: Settings) {
      viewModelScope.launch {
         settingsRepository.saveSettings(settings)
      }
   }

   companion object {
      private const val TAG = "<-SettingsViewModel"
   }
}