package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.pack.LanguagePackInstaller
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MahayRahaRepository
import com.example.domain.translation.OfflineLexiconTranslationEngine
import com.example.service.TtsManager

class AppContainer(context: Context) {
  private val appContext = context.applicationContext

  val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

  val packInstaller: LanguagePackInstaller by lazy {
    LanguagePackInstaller(appContext, database)
  }

  val translationEngine: OfflineLexiconTranslationEngine by lazy {
    OfflineLexiconTranslationEngine(
      languagePackDao = database.languagePackDao(),
      dictionaryDao = database.dictionaryDao(),
    )
  }

  val repository: MahayRahaRepository by lazy {
    MahayRahaRepository(
      database = database,
      packInstaller = packInstaller,
      translationEngine = translationEngine,
    )
  }

  val preferencesRepository: UserPreferencesRepository by lazy {
    UserPreferencesRepository(appContext)
  }

  val ttsManager: TtsManager by lazy { TtsManager(appContext) }
}
