package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
   drawerState: DrawerState,
   navigationViewModel: NavigationViewModel = viewModel(),
   scope: CoroutineScope
) {

   ModalDrawerSheet(
      drawerState = drawerState,
      drawerShape = MaterialTheme.shapes.extraSmall,
   // drawerContainerColor = MaterialTheme.colorScheme.secondaryContainer,
   ) {
      Text(
         text = "Permissions: Camera, Video, Location, Sensors",
         style = MaterialTheme.typography.headlineMedium,
         modifier = Modifier.padding(16.dp)
      )

      HorizontalDivider()

      // H O M E ---------------------------------------------------------------
      DrawerItem(
         icon = NavScreen.Home.unSelectedIcon,
         label = NavScreen.Home.title,
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
            navigationViewModel.onNavigate(NavEvent.NavigateHome)
            navigationViewModel.onNavEventHandled()
         }
      )
      // P E O P L E ---------------------------------------------------------
      DrawerItem(
         icon = NavScreen.PeopleList.unSelectedIcon,
         label = NavScreen.PeopleList.title,
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
            navigationViewModel.onNavigate(
               NavEvent.NavigateLateral(NavScreen.PeopleList.route))
            navigationViewModel.onNavEventHandled()
         }
      )
      // C A M E R A (Photo&Video) -------------------------------------------
      // location
      DrawerItem(
         icon = NavScreen.LocationsList.unSelectedIcon,
         label = NavScreen.LocationsList.title,
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
            navigationViewModel.onNavigate(
               NavEvent.NavigateLateral(NavScreen.LocationsList.route))
            navigationViewModel.onNavEventHandled()
         }
      )
      // S E N S O R S ---------------------------------------------------------
      DrawerItem(
         icon = NavScreen.SensorsList.unSelectedIcon,
         label = NavScreen.SensorsList.title,
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
            navigationViewModel.onNavigate(
               NavEvent.NavigateLateral(NavScreen.SensorsList.route))
            navigationViewModel.onNavEventHandled()
         }
      )

      DrawerItem(
         icon = Icons.Default.Settings,
         label = "Settings",
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
         }
      )
      // Add more items as needed
   }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .clickable(onClick = onClick)
         .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
   ) {
      Icon(imageVector = icon, contentDescription = label)
      Spacer(modifier = Modifier.width(16.dp))
      Text(text = label, style = MaterialTheme.typography.bodyLarge)
   }
}