package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.pack.LanguagePackInstaller
import com.example.data.repository.MahayRahaRepository
import com.example.domain.translation.OfflineLexiconTranslationEngine
import com.example.domain.translation.TranslationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var repository: MahayRahaRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    database =
      Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val installer = LanguagePackInstaller(context, database)
    val engine =
      OfflineLexiconTranslationEngine(
        languagePackDao = database.languagePackDao(),
        dictionaryDao = database.dictionaryDao(),
      )
    repository = MahayRahaRepository(database, installer, engine)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun appNameMatchesOfficialMahayRahaName() {
    val appName = context.getString(R.string.app_name)
    assertEquals("MAHAY RAHA", appName)
  }

  @Test
  fun offlineTranslationRefusesToRunWhenPacksAreNotInstalled() = runBlocking {
    repository.initializeCatalog()
    val result = repository.translate("Salama", "mg", "fr", saveToHistory = true)
    assertTrue(result is TranslationResult.ModelNotInstalled)
    val missing = (result as TranslationResult.ModelNotInstalled).missingLanguageCodes
    assertEquals(listOf("mg", "fr"), missing)
  }

  @Test
  fun installingPacksEnablesOfflineTranslationDictionaryHistoryAndFavorites() = runBlocking {
    repository.initializeCatalog()
    assertTrue(repository.downloadPack("mg").isSuccess)
    assertTrue(repository.downloadPack("fr").isSuccess)
    assertTrue(repository.downloadPack("en").isSuccess)

    val result = repository.translate("Salama", "mg", "fr", saveToHistory = true)
    assertTrue(result is TranslationResult.Success)
    val success = result as TranslationResult.Success
    assertEquals("Bonjour", success.translatedText)
    assertEquals(100, success.coveragePercent)

    val dictResults = repository.searchDictionary("rakibolana", "mg", "ALL").first()
    assertTrue(dictResults.any { it.term == "Rakibolana" && it.example.isNotBlank() })

    val history = repository.observeHistory("").first()
    assertEquals(1, history.size)
    assertEquals("Bonjour", history.first().translatedText)

    val favorited =
      repository.toggleFavorite(
        type = "TRANSLATION",
        sourceLang = "mg",
        targetLang = "fr",
        titleText = "Salama",
        subtitleText = "Bonjour",
      )
    assertTrue(favorited)
    assertEquals(1, repository.favoriteCount.first())

    // Uninstalling a pack cleanly revokes offline translation for that language
    repository.deletePack("mg")
    val afterUninstall = repository.translate("Salama", "mg", "fr", saveToHistory = false)
    assertTrue(afterUninstall is TranslationResult.ModelNotInstalled)
  }
}
