package de.rogallab.mobile.ui.navigation

import androidx.lifecycle.ViewModel
import de.rogallab.mobile.ui.INavigationHandler

class NavigationViewModel(
   private val _navHandler: INavigationHandler
) : ViewModel(),
   INavigationHandler by _navHandler