package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MahayRahaBrandEmblem
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.viewmodel.MahayRahaViewModel

enum class AppRoute(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  HOME("home", "Accueil", Icons.Filled.Home, Icons.Outlined.Home),
  TRANSLATOR("translator", "Traducteur", Icons.Filled.Translate, Icons.Outlined.Translate),
  DICTIONARY(
    "dictionary",
    "Dictionnaire",
    Icons.AutoMirrored.Filled.MenuBook,
    Icons.AutoMirrored.Outlined.MenuBook,
  ),
  HISTORY("history", "Historique", Icons.Filled.History, Icons.Outlined.History),
  FAVORITES("favorites", "Favoris", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
  SETTINGS("settings", "Paramètres", Icons.Filled.Settings, Icons.Outlined.Settings),
}

private val bottomBarRoutes =
  listOf(
    AppRoute.HOME,
    AppRoute.TRANSLATOR,
    AppRoute.DICTIONARY,
    AppRoute.HISTORY,
    AppRoute.FAVORITES,
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahayRahaApp(viewModel: MahayRahaViewModel) {
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: AppRoute.HOME.route
  val currentAppRoute = AppRoute.entries.find { it.route == currentRoute } ?: AppRoute.HOME

  val snackbarHostState = remember { SnackbarHostState() }
  val clipboardManager = LocalClipboardManager.current

  val preferences by viewModel.preferences.collectAsStateWithLifecycle()
  val allPacks by viewModel.allPacks.collectAsStateWithLifecycle()
  val installedPacks by viewModel.installedPacks.collectAsStateWithLifecycle()
  val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
  val totalWords by viewModel.totalInstalledWords.collectAsStateWithLifecycle()
  val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
  val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()

  val inputText by viewModel.inputText.collectAsStateWithLifecycle()
  val translationResult by viewModel.translationResult.collectAsStateWithLifecycle()
  val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
  val isCurrentFavorite by viewModel.isCurrentTranslationFavorite.collectAsStateWithLifecycle()

  val dictQuery by viewModel.dictQuery.collectAsStateWithLifecycle()
  val dictLangFilter by viewModel.dictLangFilter.collectAsStateWithLifecycle()
  val dictCategoryFilter by viewModel.dictCategoryFilter.collectAsStateWithLifecycle()
  val dictionaryEntries by viewModel.dictionaryEntries.collectAsStateWithLifecycle()
  val selectedDictDetail by viewModel.selectedDictionaryDetail.collectAsStateWithLifecycle()

  val historyQuery by viewModel.historyQuery.collectAsStateWithLifecycle()
  val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()

  val favoritesFilter by viewModel.favoritesFilter.collectAsStateWithLifecycle()
  val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
  val allFavorites by viewModel.allFavorites.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.uiMessages.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  val copyText: (String) -> Unit = { text ->
    clipboardManager.setText(AnnotatedString(text))
    viewModel.notifyMessage("Copié dans le presse-papiers")
  }

  val navigateToTopLevel: (String) -> Unit = { targetRoute ->
    navController.navigate(targetRoute) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  val sourceSuggestions =
    remember(dictionaryEntries, preferences.sourceLangCode) {
      dictionaryEntries.filter { it.languageCode == preferences.sourceLangCode }
    }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail) {
        NavigationRail(modifier = Modifier.fillMaxHeight()) {
          Spacer(modifier = Modifier.padding(top = 8.dp))
          MahayRahaBrandEmblem(size = 40.dp)
          Spacer(modifier = Modifier.padding(top = 12.dp))
          AppRoute.entries.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTopLevel(dest.route) },
              icon = {
                Icon(
                  imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                  contentDescription = dest.label,
                )
              },
              label = { Text(dest.label) },
              modifier = Modifier.testTag("nav_${dest.route}"),
            )
          }
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f).fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          TopAppBar(
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
              ),
            navigationIcon = {
              if (currentAppRoute == AppRoute.SETTINGS) {
                IconButton(onClick = { navController.popBackStack() }) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                  )
                }
              }
            },
            title = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentAppRoute != AppRoute.SETTINGS) {
                  MahayRahaBrandEmblem(size = 32.dp)
                  Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                  Text(
                    text =
                      if (currentAppRoute == AppRoute.HOME) "MAHAY RAHA"
                      else currentAppRoute.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                  )
                  if (currentAppRoute == AppRoute.HOME) {
                    Text(
                      text = "Traduire, apprendre et comprendre.",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            },
            actions = {
              if (currentAppRoute != AppRoute.SETTINGS) {
                IconButton(
                  onClick = { navigateToTopLevel(AppRoute.SETTINGS.route) },
                  modifier = Modifier.testTag("topbar_settings_button"),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Paramètres et packs hors ligne",
                  )
                }
              }
            },
          )
        },
        bottomBar = {
          if (!useNavigationRail) {
            NavigationBar {
              bottomBarRoutes.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateToTopLevel(dest.route) },
                  icon = {
                    Icon(
                      imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                      contentDescription = dest.label,
                    )
                  },
                  label = {
                    Text(
                      text =
                        when (dest) {
                          AppRoute.DICTIONARY -> "Dico"
                          else -> dest.label
                        },
                      maxLines = 1,
                    )
                  },
                  modifier = Modifier.testTag("nav_${dest.route}"),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        NavHost(
          navController = navController,
          startDestination = AppRoute.HOME.route,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
          composable(AppRoute.HOME.route) {
            HomeScreen(
              allPacks = allPacks,
              installedPacks = installedPacks,
              activeDownloads = activeDownloads,
              totalWords = totalWords,
              favoriteCount = favoriteCount,
              recentHistory = recentHistory,
              dictionarySample = dictionaryEntries,
              onNavigateTranslator = { navigateToTopLevel(AppRoute.TRANSLATOR.route) },
              onNavigateDictionary = { navigateToTopLevel(AppRoute.DICTIONARY.route) },
              onNavigateSettings = { navigateToTopLevel(AppRoute.SETTINGS.route) },
              onNavigateHistory = { navigateToTopLevel(AppRoute.HISTORY.route) },
              onDownloadPack = viewModel::downloadPack,
              onOpenTranslation = { hist ->
                viewModel.loadInTranslator(
                  sourceText = hist.sourceText,
                  sourceLangCode = hist.sourceLangCode,
                  targetLangCode = hist.targetLangCode,
                )
                navigateToTopLevel(AppRoute.TRANSLATOR.route)
              },
              onSpeak = viewModel::speak,
              onCopy = copyText,
            )
          }

          composable(AppRoute.TRANSLATOR.route) {
            TranslatorScreen(
              allPacks = allPacks,
              activeDownloads = activeDownloads,
              sourceLangCode = preferences.sourceLangCode,
              targetLangCode = preferences.targetLangCode,
              inputText = inputText,
              translationResult = translationResult,
              isTranslating = isTranslating,
              isCurrentFavorite = isCurrentFavorite,
              sourceSuggestions = sourceSuggestions,
              onUpdateInput = viewModel::updateInputText,
              onSelectLanguagePair = viewModel::setLanguagePair,
              onSwapLanguages = viewModel::swapLanguages,
              onTranslate = { viewModel.translateNow(saveToHistory = true) },
              onDownloadPack = viewModel::downloadPack,
              onToggleFavorite = viewModel::toggleCurrentTranslationFavorite,
              onSpeak = viewModel::speak,
              onCopy = copyText,
              onOpenPackManager = { navigateToTopLevel(AppRoute.SETTINGS.route) },
            )
          }

          composable(AppRoute.DICTIONARY.route) {
            DictionaryScreen(
              installedPacks = installedPacks,
              query = dictQuery,
              langFilter = dictLangFilter,
              categoryFilter = dictCategoryFilter,
              entries = dictionaryEntries,
              selectedDetail = selectedDictDetail,
              allFavorites = allFavorites,
              onUpdateQuery = viewModel::updateDictQuery,
              onSelectLangFilter = viewModel::updateDictLangFilter,
              onSelectCategoryFilter = viewModel::updateDictCategoryFilter,
              onOpenEntry = viewModel::openDictionaryDetail,
              onCloseDetail = viewModel::closeDictionaryDetail,
              onSpeak = viewModel::speak,
              onCopy = copyText,
              onToggleFavorite = viewModel::toggleDictionaryFavorite,
              onTranslateEntry = { entry ->
                val targetCode =
                  installedPacks.firstOrNull { it.code != entry.languageCode }?.code
                    ?: preferences.targetLangCode
                viewModel.loadInTranslator(
                  sourceText = entry.term,
                  sourceLangCode = entry.languageCode,
                  targetLangCode = targetCode,
                )
                navigateToTopLevel(AppRoute.TRANSLATOR.route)
              },
              onOpenPackManager = { navigateToTopLevel(AppRoute.SETTINGS.route) },
            )
          }

          composable(AppRoute.HISTORY.route) {
            HistoryScreen(
              query = historyQuery,
              items = historyItems,
              allFavorites = allFavorites,
              onUpdateQuery = viewModel::updateHistoryQuery,
              onOpenInTranslator = { item ->
                viewModel.loadInTranslator(
                  sourceText = item.sourceText,
                  sourceLangCode = item.sourceLangCode,
                  targetLangCode = item.targetLangCode,
                )
                navigateToTopLevel(AppRoute.TRANSLATOR.route)
              },
              onToggleFavorite = viewModel::toggleHistoryFavorite,
              onSpeak = viewModel::speak,
              onCopy = copyText,
              onDeleteItem = viewModel::deleteHistoryItem,
              onClearAll = viewModel::clearHistory,
              onNavigateTranslator = { navigateToTopLevel(AppRoute.TRANSLATOR.route) },
            )
          }

          composable(AppRoute.FAVORITES.route) {
            FavoritesScreen(
              filterType = favoritesFilter,
              favorites = favoriteItems,
              onSelectFilter = viewModel::updateFavoritesFilter,
              onOpenFavoriteInTranslator = { fav ->
                val targetCode =
                  if (fav.type == "TRANSLATION") fav.targetLangCode
                  else installedPacks.firstOrNull { it.code != fav.sourceLangCode }?.code
                    ?: preferences.targetLangCode
                viewModel.loadInTranslator(
                  sourceText = fav.titleText,
                  sourceLangCode = fav.sourceLangCode,
                  targetLangCode = targetCode,
                )
                navigateToTopLevel(AppRoute.TRANSLATOR.route)
              },
              onSpeak = viewModel::speak,
              onCopy = copyText,
              onRemoveFavorite = viewModel::removeFavorite,
              onNavigateDictionary = { navigateToTopLevel(AppRoute.DICTIONARY.route) },
            )
          }

          composable(AppRoute.SETTINGS.route) {
            SettingsScreen(
              allPacks = allPacks,
              installedPacks = installedPacks,
              activeDownloads = activeDownloads,
              themeMode = preferences.themeMode,
              speechRate = preferences.speechRate,
              onDownloadPack = viewModel::downloadPack,
              onDeletePack = viewModel::deletePack,
              onCreateCustomLanguage = viewModel::createCustomLanguage,
              onAddCustomWordPair = viewModel::addCustomWordPair,
              onSelectThemeMode = viewModel::setThemeMode,
              onChangeSpeechRate = viewModel::setSpeechRate,
              onTestVoice = {
                viewModel.speak("MAHAY RAHA : traduire, apprendre et comprendre.", "fr")
              },
              onClearHistory = viewModel::clearHistory,
            )
          }
        }
      }
    }
  }
}
