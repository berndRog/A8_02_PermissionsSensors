package de.rogallab.mobile

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import coil.ImageLoader
import de.rogallab.mobile.data.devices.AppLocationManager
import de.rogallab.mobile.data.devices.AppSensorManager
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.data.mediastore.MediaStoreRepository
import de.rogallab.mobile.data.remote.IPersonWebservice
import de.rogallab.mobile.data.remote.ImageWebservice
import de.rogallab.mobile.data.remote.network.ApiKey
import de.rogallab.mobile.data.remote.network.BearerToken
import de.rogallab.mobile.data.remote.network.NetworkConnection
import de.rogallab.mobile.data.remote.network.NetworkConnectivity
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import de.rogallab.mobile.data.remote.network.createWebservice
import de.rogallab.mobile.data.repositories.ImageRepositoryImpl
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.data.repositories.SettingsRepository
import de.rogallab.mobile.domain.IAppLocationManager
import de.rogallab.mobile.domain.IAppSensorManager
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ISettingsRepository
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.createImageLoader
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.features.camera.CameraViewModel
import de.rogallab.mobile.ui.features.location.LocationViewModel
import de.rogallab.mobile.ui.features.orientation.SensorViewModel
import de.rogallab.mobile.ui.features.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.features.people.PersonValidator
import de.rogallab.mobile.ui.features.people.PersonViewModel
import de.rogallab.mobile.ui.navigation.NavigationHandler
import de.rogallab.mobile.ui.features.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

typealias CoroutineDispatcherMain = CoroutineDispatcher
typealias CoroutineDispatcherIo = CoroutineDispatcher
typealias CoroutineScopeMain = CoroutineScope
typealias CoroutineScopeIo = CoroutineScope
typealias CoroutineContextMain = CoroutineContext
typealias CoroutineContextIo = CoroutineContext

val domainModules: Module = module {
   val tag = "<-domainModules"


   logInfo(tag, "single    -> CoroutineExceptionHandler")
   single<CoroutineExceptionHandler> {
      CoroutineExceptionHandler { _, exception ->
         logError(tag, "Coroutine exception: ${exception.localizedMessage}")
      }
   }
   logInfo( tag, "factory   -> CoroutineDispatcherMain")
   factory<CoroutineDispatcherMain> { Dispatchers.Main }

   logInfo(tag, "factory   -> CoroutineDispatcherIo)")
   factory<CoroutineDispatcherIo>{ Dispatchers.IO }


   logInfo(tag, "factory   -> CoroutineScopeMain")
   factory<CoroutineScopeMain> {
      CoroutineScope(
         SupervisorJob() +
            get<CoroutineExceptionHandler>() +
            get<CoroutineDispatcherIo>()
      )
   }

   logInfo(tag, "factory   -> CoroutineScopeIo")
   factory<CoroutineScopeIo> {
      CoroutineScope(
         SupervisorJob() +
            get<CoroutineExceptionHandler>() +
            get<CoroutineDispatcherIo>()
      )
   }

}

val dataModules = module {
   val tag = "<-dataModules"

   // Provide SharedPreferences
   logInfo(tag, "single    -> SharedPreferences")
   single<SharedPreferences> {
      androidContext().getSharedPreferences(
         "sensor_settings",
         Context.MODE_PRIVATE
      )
   }


   //region local database
   logInfo(tag, "single    -> Seed")
   single<Seed> {
      Seed(
         context = androidContext(),
         resources = androidContext().resources
      )
   }

   logInfo(tag, "single    -> SeedDatabase")
   single<SeedDatabase> {
      SeedDatabase(
         _database = get<AppDatabase>(),
         _personDao = get<IPersonDao>(),
         _seed = get<Seed>(),
         _dispatcher = get<CoroutineDispatcher>(named("DispatcherIO")),
      )
   }

   logInfo(tag, "single    -> AppDatabase")
   single {
      Room.databaseBuilder(
         context = androidContext(),
         klass = AppDatabase::class.java,
         name = AppStart.DATABASE_NAME
      ).build()
   }

   logInfo(tag, "single    -> IPersonDao")
   single<IPersonDao> { get<AppDatabase>().createPersonDao() }
   //endregion

   //region remote webservice
   logInfo(tag, "single    -> NetworkConnection")
   single<NetworkConnection> {
      NetworkConnection(context = androidContext())
   }
   logInfo(tag, "single    -> NetworkConnectivity")
   single<NetworkConnectivity> { NetworkConnectivity(get<NetworkConnection>()) }

   logInfo(tag, "single    -> BearerToken")
   single<BearerToken> { BearerToken() }

   logInfo(tag, "single    -> ApiKey")
   single<ApiKey> { ApiKey(AppStart.API_KEY) }

   logInfo(tag, "single    -> HttpLoggingInterceptor")
   single<HttpLoggingInterceptor> {
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
   }
   logInfo(tag, "single    -> OkHttpClient")
   single<OkHttpClient> {
      createOkHttpClient(
         bearerToken = get<BearerToken>(),
         apiKey = get<ApiKey>(),
         networkConnectivity = get<NetworkConnectivity>(),
         loggingInterceptor = get<HttpLoggingInterceptor>()
      )
   }

   logInfo(tag, "single    -> GsonConverterFactory")
   single<GsonConverterFactory> { GsonConverterFactory.create() }

   logInfo(tag, "single    -> Retrofit")
   single<Retrofit> {
      createRetrofit(
         okHttpClient = get<OkHttpClient>(),
         gsonConverterFactory = get<GsonConverterFactory>()
      )
   }

   logInfo(tag, "single    -> IPersonWebservice")
   single<IPersonWebservice> {
      createWebservice<IPersonWebservice>(
         retrofit = get<Retrofit>(),
         webserviceName = "IPersonWebservice"
      )
   }
   logInfo(tag, "single    -> ImageWebservice")
   single<ImageWebservice> {
      createWebservice<ImageWebservice>(
         retrofit = get<Retrofit>(),
         webserviceName = "ImageWebservice"
      )
   }
   //endregion

   // Provide IPersonRepository, injecting the `viewModelScope`
   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _personDao =  get<IPersonDao>(),
         _webservice = get<IPersonWebservice>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> ImageRepositoryImpl: ImageRepository")
   single<ImageRepository> {
      ImageRepositoryImpl(
         _webService = get<ImageWebservice>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   // Provide IMediaStoreRepository
   logInfo(tag, "single    -> MediaStoreRepository: IMediaStoreRepository")
   single<IMediaStoreRepository>{
      MediaStoreRepository(
         androidContext()
      )
   }


   // Provide IPersonRepository
   logInfo(tag, "single    -> SettingsRepository: ISettingsRepository")
   single<ISettingsRepository>{ SettingsRepository( androidContext() ) }

   logInfo(tag, "single    -> AppLocationManager")
   single<IAppLocationManager> {
      AppLocationManager(_context = androidContext())
   }

   logInfo(tag, "single    -> AppSensorManager")
   single<IAppSensorManager> {
      AppSensorManager(context = androidContext())
   }
}


val uiModules: Module = module {

   val tag = "<-uiModules"
   //logInfo("<-uiModules", "single -> Application")
   //single { get<Application>() }

   logInfo(tag, "single    -> createImageLoader")
   single<ImageLoader> { createImageLoader(androidContext()) }

   logInfo(tag, "factory   -> LifecycleOwner")
   factory<LifecycleOwner> { ProcessLifecycleOwner.get() }

   factory<IErrorHandler> {
      ErrorHandler(
         _coroutineScopeMain = get<CoroutineScopeMain>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   factory<INavigationHandler> {
      NavigationHandler(
         _coroutineScopeMain = get<CoroutineScopeMain>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "viewModel -> HomeViewModel")
   viewModel<HomeViewModel> {
      HomeViewModel(
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "viewModel -> PersonValidator")
   factory<PersonValidator> {
      PersonValidator(
         _context = androidContext()
      )
   }


   logInfo(tag, "viewModel -> PersonViewModel")
   viewModel<PersonViewModel> {
      PersonViewModel(
         _context = androidContext(),
         _personRepository = get<IPersonRepository>(),
         _imageRepository = get<ImageRepository>(),
         _validator = get<PersonValidator>(),
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _imageLoader = get<ImageLoader>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "viewModel -> CameraViewModel")
   viewModel<CameraViewModel>{
      CameraViewModel(
         _context = androidContext(),
         _lifecyleOwner = get<LifecycleOwner>(),
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

//   logInfo(tag, "single    -> AppLocationService")
//   single<AppLocationService> {
//      AppLocationService( ) // to start the service by manifest, class must have a default ctor
//   }

   logInfo(tag, "viewModel -> LocationViewModel")
   viewModel<LocationViewModel> {
      LocationViewModel(
         application = androidApplication(),
         _locationManager = get<IAppLocationManager>(),
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

//   logInfo(tag, "single    -> AppSensorService")
//   single<AppSensorService> {
//      AppSensorService( )  // to start the service by manifest, class must have a default ctor
//   }

   logInfo(tag, "viewModel -> SensorViewModel")
   viewModel<SensorViewModel> {
      SensorViewModel(
         application = androidApplication(),
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "viewModel -> NavigationViewModel")
   viewModel<NavigationViewModel> {
      NavigationViewModel(
         _navHandler = get<INavigationHandler>()
      )
   }

   logInfo(tag, "viewModel -> SettingsViewModel")
   viewModel<SettingsViewModel> {
      SettingsViewModel(
         application = androidApplication(),
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

}