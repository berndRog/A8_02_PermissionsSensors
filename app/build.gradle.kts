/**
 * Module-level functions
 * These functions are used to provide dependencies for the app.
 *
 * The first section in the build configuration applies the Android Gradle plugin
 * to this build and makes the android block available to specify
 * Android-specific build options.
 */
plugins {
   alias(libs.plugins.android.application) 
   alias(libs.plugins.kotlin.android)
   alias(libs.plugins.google.devtools.ksp)
   alias(libs.plugins.kotlin.serialization)
   alias(libs.plugins.kotlin.compose.compiler)

   alias(libs.plugins.google.map.secrets)
}

/**
 * Locate (and possibly download) a JDK used to build your kotlin
 * source code. This also acts as a default for sourceCompatibility,
 * targetCompatibility and jvmTarget. Note that this does not affect which JDK
 * is used to run the Gradle build itself, and does not need to take into
 * account the JDK version required by Gradle plugins (such as the
 * Android Gradle Plugin)
 */
kotlin {
   jvmToolchain(17)
}

android {
   namespace = "de.rogallab.mobile"
   compileSdk = 35

   defaultConfig {
      applicationId = "de.rogallab.mobile"
      minSdk = 32
      targetSdk = 34
      versionCode = 1
      versionName = "1.0"

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      vectorDrawables {
         useSupportLibrary = true
      }
   }

   buildTypes {
      release {
         isMinifyEnabled = false
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
   }
   kotlinOptions {
      jvmTarget = "17"
   }

   lint {
      abortOnError = false
      disable  += "unchecked"
   }
   buildFeatures {
      compose = true
      buildConfig = true
   }
   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
   }
   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
         excludes += "/META-INF/LICENSE.md"
         excludes += "/META-INF/LICENSE-notice.md"
      }
   }
}

dependencies {
   // Gradle version catalo
   // https://www.youtube.com/watch?v=MWw1jcwPK3Q

   // Kotlin
   // https://developer.android.com/jetpack/androidx/releases/core
   implementation(libs.androidx.core.ktx)

   // Kotlin Coroutines
   // https://kotlinlang.org/docs/releases.html
   implementation (libs.kotlinx.coroutines.core)
   implementation (libs.kotlinx.coroutines.android)
   // https://github.com/Kotlin/kotlinx-datetime
   implementation (libs.kotlinx.datetime)

   // Ui Activity
   // https://developer.android.com/jetpack/androidx/releases/activity
   implementation(libs.androidx.activity.compose)

   // Ui Compose
   // https://developer.android.com/jetpack/compose/bom/bom-mapping
   val composeBom = platform(libs.androidx.compose.bom)
   implementation(composeBom)
   testImplementation(composeBom)
   androidTestImplementation(composeBom)
   implementation(libs.androidx.compose.foundation)
   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.material.icons)
   implementation(libs.androidx.compose.ui.text.google.fonts)

   // Ui Lifecycle
   // https://developer.android.com/jetpack/androidx/releases/lifecycle
   // val archVersion = "2.2.0"
   implementation(libs.androidx.lifecycle.viewmodel.ktx)
   // ViewModel utilities for Compose
   implementation(libs.androidx.lifecycle.viewmodel.compose)
   // Lifecycle utilities for Compose
   implementation (libs.androidx.lifecycle.runtime.compose)
   implementation(libs.androidx.lifecycle.process)

   // Ui Navigation
   // https://developer.android.com/jetpack/androidx/releases/navigation
   // Jetpack Compose Integration
   implementation(libs.androidx.navigation.compose)

   // Room
   implementation(libs.androidx.room.ktx)
   implementation(libs.androidx.room.runtime)
   ksp(libs.androidx.room.compiler)

   // Retrofit,  OkHttp Logging
   implementation (libs.gson.json)
   implementation (libs.retrofit2.core)
   implementation (libs.retrofit2.gson)
   implementation (libs.retrofit2.logging)

   // Ui Camera
   // https://developer.android.com/jetpack/androidx/releases/camera
   implementation(libs.androidx.camera.camera2)
   implementation(libs.androidx.camera.core)
   implementation(libs.androidx.camera.lifecycle)
   implementation(libs.androidx.camera.video)
   implementation(libs.androidx.camera.view)
   implementation(libs.androidx.camera.extensions)

   // Image loading
   // https://coil-kt.github.io/coil/
   implementation(libs.coil.compose)

   // Google Maps Compose
   implementation (libs.maps.compose)
   // Play Services Location for FusedLocationProvider
   implementation(libs.play.services.location)

   // Koin
   // https://insert-koin.io/docs/3.2.0/getting-started/android/
   implementation(platform(libs.koin.bom))
   implementation(libs.koin.android)
   implementation(libs.koin.androidx.compose)
   implementation(libs.koin.androidx.startup)

   // TESTS -----------------------
   testImplementation(libs.junit)
   testImplementation(libs.koin.test)
   // Koin for JUnit 4 / 5
   testImplementation(libs.koin.test.junit4)

   // ANDROID TESTS ---------------
   // https://developer.android.com/jetpack/androidx/releases/test
   // To use the androidx.test.core APIs
   androidTestImplementation(libs.androidx.test.core)
   androidTestImplementation(libs.androidx.test.core.ktx)

   // To use the androidx.test.espresso
   androidTestImplementation(libs.androidx.test.espresso.core)

   // To use the JUnit Extension APIs
   androidTestImplementation(libs.androidx.test.ext.junit)
   androidTestImplementation(libs.androidx.test.ext.junit.ktx)
   // To use the Truth Extension APIs
   androidTestImplementation(libs.androidx.test.ext.truth)

   // To use the androidx.test.runner APIs
   androidTestImplementation(libs.androidx.test.runner)

   // To use Compose Testing
   androidTestImplementation(libs.androidx.ui.test.junit4)
   // testing navigation
   androidTestImplementation(libs.androidx.navigation.testing)
   // optional - Test helpers
   androidTestImplementation(libs.androidx.room.testing)

   androidTestImplementation(libs.androidx.arch.core.testing)

   // testing coroutines
   androidTestImplementation(libs.kotlinx.coroutines.test)

   androidTestImplementation(libs.koin.test)
   androidTestImplementation(libs.koin.test.junit4)
   androidTestImplementation(libs.koin.android.test)

   androidTestImplementation(libs.mockito.core)
   androidTestImplementation(libs.mockito.android)
   androidTestImplementation(libs.mockito.kotlin)


   androidTestImplementation(libs.androidx.ui.test.manifest)
   debugImplementation(libs.androidx.ui.test.manifest)



}