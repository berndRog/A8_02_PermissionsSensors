package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomBar(
   navController: NavController,
   navHandler: INavigationHandler
) {
   val topLevelScreens = listOf(
      NavScreen.Home,
      NavScreen.PeopleList,
      NavScreen.Camera,
      NavScreen.LocationsList,
      NavScreen.SensorsList
   )

   NavigationBar() {
      val tag = "<-AppBottomBar"

      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route

      topLevelScreens.forEach { topLevelScreen ->
         NavigationBarItem(
            icon = {
               BadgedBox(
                  badge = {
                     if(topLevelScreen.badgeCount != null) {
                        Badge { Text(text = topLevelScreen.badgeCount.toString() ) }
                     } else if(topLevelScreen.hasNews) {
                        Badge()
                     }
                  }
               ) {
                  Icon(
                     imageVector =
                        if(currentRoute == topLevelScreen.route) topLevelScreen.selectedIcon
                        else                                     topLevelScreen.unSelectedIcon,
                     contentDescription = topLevelScreen.title
                  )
               }
            },
            label = { Text(text = topLevelScreen.title) },
            alwaysShowLabel = true,
            selected = currentRoute == topLevelScreen.route,
            onClick = {
               logDebug(tag,"onNavigate ${topLevelScreen.route}")
               navHandler.onNavigate(NavEvent.NavigateLateral(topLevelScreen.route))
            }
         )
      }
   }
}