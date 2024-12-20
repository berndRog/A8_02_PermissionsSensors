package de.rogallab.mobile.data.repositories
import android.content.Context
import android.content.SharedPreferences
import de.rogallab.mobile.domain.ISettingsRepository
import de.rogallab.mobile.domain.entities.Settings

class SettingsRepository(
   context: Context
): ISettingsRepository {

   private val _sharedPreferences: SharedPreferences =
      context.getSharedPreferences("settings", Context.MODE_PRIVATE)

   override fun getSettings(): Settings {
      return Settings(
         isLocationSensorEnabled = _sharedPreferences.getBoolean("locations_sensor", false),
         isPressureSensorEnabled = _sharedPreferences.getBoolean("pressure_sensor", true),
         isLightSensorEnabled = _sharedPreferences.getBoolean("light_sensor", true),
         isOrientationSensorEnabled = _sharedPreferences.getBoolean("orientation_sensor", false),
      )
   }

   override fun saveSettings(settings: Settings) {
      _sharedPreferences.edit().apply {
         putBoolean("locations_sensor", settings.isLocationSensorEnabled)
         putBoolean("pressure_sensor", settings.isPressureSensorEnabled)
         putBoolean("light_sensor", settings.isLightSensorEnabled)
         putBoolean("orientation_sensor", settings.isOrientationSensorEnabled)
         apply()
      }
   }
}