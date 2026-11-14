package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.ThemeMode
import com.example.di.AppContainer
import com.example.ui.MahayRahaApp
import com.example.ui.theme.MahayRahaTheme
import com.example.ui.viewmodel.MahayRahaViewModel

class MainActivity : ComponentActivity() {
  private lateinit var appContainer: AppContainer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    appContainer = AppContainer(applicationContext)
    val factory =
      MahayRahaViewModel.Factory(
        repository = appContainer.repository,
        preferencesRepository = appContainer.preferencesRepository,
        ttsManager = appContainer.ttsManager,
      )
    val viewModel = ViewModelProvider(this, factory)[MahayRahaViewModel::class.java]

    setContent {
      val prefs by viewModel.preferences.collectAsStateWithLifecycle()
      val isDark =
        when (prefs.themeMode) {
          ThemeMode.SYSTEM -> isSystemInDarkTheme()
          ThemeMode.LIGHT -> false
          ThemeMode.DARK -> true
        }

      MahayRahaTheme(darkTheme = isDark) {
        MahayRahaApp(viewModel = viewModel)
      }
    }
  }
}
