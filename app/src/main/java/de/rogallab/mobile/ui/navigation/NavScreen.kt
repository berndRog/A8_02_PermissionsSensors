package de.rogallab.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(
   val route: String,
   val title: String,
   val selectedIcon: ImageVector,
   val unSelectedIcon: ImageVector,
   val hasNews: Boolean = false,
   val badgeCount: Int? = null
) {

   data object Home: NavScreen(
      route = "HomeScreen",
      title = "Start",
      selectedIcon =  Icons.Outlined.Home,
      unSelectedIcon =  Icons.Filled.Home
   )

   data object PeopleList: NavScreen(
      route = "PeopleListScreen",
      title = "Personen",
      selectedIcon =  Icons.Outlined.Group,
      unSelectedIcon =  Icons.Filled.Group
   )
   data object PersonInput: NavScreen(
      route = "PersonInputScreen",
      title = "Person hinzufügen",
      selectedIcon = Icons.Outlined.PersonAdd,
      unSelectedIcon = Icons.Filled.PersonAdd,
   )
   data object PersonDetail: NavScreen(
      route = "PersonDetailScreen",
      title = "Person ändern",
      selectedIcon = Icons.Outlined.Person,
      unSelectedIcon = Icons.Filled.Person
   )

   data object Camera: NavScreen(
      route = "Camera&VideoScreen",
      title = "Camera & Video",
      selectedIcon = Icons.Outlined.PhotoCamera,
      unSelectedIcon = Icons.Filled.PhotoCamera
   )

   data object LocationsList: NavScreen(
      route = "LocationsListScreen",
      title = "Positionen",
      selectedIcon = Icons.Outlined.LocationOn,
      unSelectedIcon = Icons.Filled.LocationOn
   )
   data object SensorsList: NavScreen(
      route = "SensorsListScreen",
      title = "Sensoren",
      selectedIcon = Icons.Outlined.MyLocation,
      unSelectedIcon = Icons.Filled.MyLocation
   )

   data object Settings: NavScreen(
      route = "SettingsScreen",
      title = "Einstellungen",
      selectedIcon = Icons.Outlined.Settings,
      unSelectedIcon = Icons.Filled.Settings
   )
}