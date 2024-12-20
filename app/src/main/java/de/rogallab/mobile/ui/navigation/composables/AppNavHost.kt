package de.rogallab.mobile.ui.navigation.composables
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.features.camera.CameraScreen
import de.rogallab.mobile.ui.features.camera.CameraViewModel
import de.rogallab.mobile.ui.features.home.HomeScreen
import de.rogallab.mobile.ui.features.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.features.orientation.SensorViewModel
import de.rogallab.mobile.ui.features.orientation.composables.SensorListScreen
import de.rogallab.mobile.ui.features.location.LocationViewModel
import de.rogallab.mobile.ui.features.location.composables.LocationListScreen
import de.rogallab.mobile.ui.features.people.PersonViewModel
import de.rogallab.mobile.ui.features.people.composables.PeopleListScreen
import de.rogallab.mobile.ui.features.people.composables.PersonScreen
import de.rogallab.mobile.ui.navigation.NavState
import de.rogallab.mobile.ui.features.settings.SettingsScreen
import de.rogallab.mobile.ui.features.settings.SettingsViewModel
import kotlinx.coroutines.flow.combine
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
   navController: NavHostController = rememberNavController(),
   // Injecting the ViewModel by koin()
   homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
   peopleViewModel: PersonViewModel = koinViewModel<PersonViewModel>(),
   cameraViewModel: CameraViewModel = koinViewModel<CameraViewModel>(),
   locationViewModel: LocationViewModel = koinViewModel<LocationViewModel>(),
   sensorViewModel: SensorViewModel = koinViewModel<SensorViewModel>(),
   settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
   navigationViewModel: NavigationViewModel = koinViewModel<NavigationViewModel>()
) {
   val tag = "<-AppNavHost"
   val duration = 1000  // in milliseconds
   // create a NavHostController with a factory function

   //region  N A V I G A T I O N    H O S T --------------------------------------------------------
   NavHost(
      navController = navController,
      startDestination = NavScreen.Home.route,
      enterTransition = { enterTransition(duration) },
      exitTransition  = { exitTransition(duration)  },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      // feature H O M E
      composable( route = NavScreen.Home.route ) {
         HomeScreen(
            viewModel = homeViewModel,
            navController = navController
         )
      }
      // feature P E O P L E
      composable( route = NavScreen.PeopleList.route ) {
         PeopleListScreen(
            viewModel = peopleViewModel,
            navController = navController
         )
      }
      composable( route = NavScreen.PersonInput.route ) {
         PersonScreen(
            viewModel = peopleViewModel,
            isInputScreen = true
         )
      }
      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType}),
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")
         PersonScreen(
            viewModel = peopleViewModel,
            isInputScreen = false,
            id = id
         )
      }
      // feature CAMERA (photo&video)
      composable( route = NavScreen.Camera.route )  {
         CameraScreen(
            viewModel = cameraViewModel
         )
      }
      // feature LOCATIONS  (Google maps)
      composable( route = NavScreen.LocationsList.route )  {
         LocationListScreen(
            viewModel = locationViewModel,
            navController = navController
         )
      }
      // feature ORIENTATION  (sensors)
      composable( route = NavScreen.SensorsList.route ) {
         SensorListScreen(
            viewModel = sensorViewModel,
            navController = navController
         )
      }
      // feature SETTINGS
      composable( route = NavScreen.Settings.route ) {
         SettingsScreen(
            settingsViewModel = settingsViewModel
         )
      }
   }
   //endregion---

   //region O N E   T I M E   E V E N T S   N A V I G A T I O N ------------------------------------
   // Observing the navigation state and handle navigation
   // Combine navUiStateFlow from multiple ViewModels

   val combinedNavEvent: NavEvent? by combine(
      navigationViewModel.navStateFlow,
      homeViewModel.navStateFlow,
      cameraViewModel.navStateFlow,
      peopleViewModel.navStateFlow,
      locationViewModel.navStateFlow,
      sensorViewModel.navStateFlow,
      settingsViewModel.navStateFlow,
   ) { states: Array<NavState> ->
      // Access the elements by index
      val nav = states[0]
      val home = states[1]
      val camera = states[2]
      val people = states[3]
      val location = states[4]
      val sensor = states[5]
      val setting = states[6]
      // Combine the states as needed, here we just return the first non-null event
      nav.navEvent ?: home.navEvent ?: camera.navEvent ?: people.navEvent ?:
      location.navEvent ?: sensor.navEvent ?: setting.navEvent
   }.collectAsStateWithLifecycle(initialValue = null)

   combinedNavEvent?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      // check which ViewModel has the navEvent
      val navigationHandler: INavigationHandler = when {
         navigationViewModel.navStateFlow.value.navEvent == navEvent -> navigationViewModel
         homeViewModel.navStateFlow.value.navEvent == navEvent -> homeViewModel
         cameraViewModel.navStateFlow.value.navEvent == navEvent -> cameraViewModel
         peopleViewModel.navStateFlow.value.navEvent == navEvent -> peopleViewModel
         locationViewModel.navStateFlow.value.navEvent == navEvent -> locationViewModel
         sensorViewModel.navStateFlow.value.navEvent == navEvent -> sensorViewModel
         settingsViewModel.navStateFlow.value.navEvent == navEvent -> settingsViewModel
         else -> return@let
      }

      when(navEvent) {
         is NavEvent.NavigateHome -> {
            navController.navigate(NavScreen.Home.route) {
               popUpTo(navController.graph.startDestinationRoute ?: NavScreen.Home.route) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateLateral -> {
            navController.navigate(navEvent.route) {
               popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)

            // onNavEventHandled() resets the navEvent to null
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateBack -> {
            navController.popBackStack()
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.BottomNav -> {
            // navigateUp() pops the back stack to the previous destination
            navController.popBackStack()
            navController.navigate(navEvent.route) {
               navController.graph.startDestinationRoute?.let { route ->
                  popUpTo(route) { saveState = true  }
               }
               // Avoid multiple copies of the same destination when
               // reselecting the same item
               launchSingleTop = true
               // Restore state when reselecting a previously selected item
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

      }
   }
   //endregion
}

//region A N I M A T I O N S -----------------------------------------------------------------------
private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(
   animationSpec = tween(duration)
) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(
   animationSpec = tween(duration)
) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)


private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = scaleIn(
   initialScale = 0.1f,
   animationSpec = tween(duration)
) + fadeIn(animationSpec = tween(duration))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
   duration: Int
) = scaleOut(
   targetScale = 3.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
//endregion