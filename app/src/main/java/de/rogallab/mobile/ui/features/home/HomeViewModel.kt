package de.rogallab.mobile.ui.features.home

import androidx.lifecycle.ViewModel
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineExceptionHandler

class HomeViewModel(
   private val _errorHandler: IErrorHandler,
   private val _navHandler: INavigationHandler,
   private val _exceptionHandler: CoroutineExceptionHandler
): ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navHandler
