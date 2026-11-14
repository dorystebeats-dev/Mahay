package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mahay_raha_prefs")

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
}

data class UserPreferences(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val speechRate: Float = 1.0f,
  val sourceLangCode: String = "mg",
  val targetLangCode: String = "fr",
)

class UserPreferencesRepository(private val context: Context) {
  private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SPEECH_RATE = floatPreferencesKey("speech_rate")
    val SOURCE_LANG = stringPreferencesKey("source_lang")
    val TARGET_LANG = stringPreferencesKey("target_lang")
  }

  val preferencesFlow: Flow<UserPreferences> =
    context.dataStore.data.map { prefs ->
      val theme =
        runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name) }
          .getOrDefault(ThemeMode.SYSTEM)
      UserPreferences(
        themeMode = theme,
        speechRate = prefs[Keys.SPEECH_RATE] ?: 1.0f,
        sourceLangCode = prefs[Keys.SOURCE_LANG] ?: "mg",
        targetLangCode = prefs[Keys.TARGET_LANG] ?: "fr",
      )
    }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
  }

  suspend fun setSpeechRate(rate: Float) {
    context.dataStore.edit { it[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.5f) }
  }

  suspend fun setLanguagePair(sourceCode: String, targetCode: String) {
    context.dataStore.edit {
      it[Keys.SOURCE_LANG] = sourceCode
      it[Keys.TARGET_LANG] = targetCode
    }
  }
}
