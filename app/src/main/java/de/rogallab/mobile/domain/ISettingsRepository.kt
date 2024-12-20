package de.rogallab.mobile.domain
import de.rogallab.mobile.domain.entities.Settings

interface ISettingsRepository {
   fun getSettings(): Settings
   fun saveSettings(settings: Settings)
}