package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.ui.components.DictionaryDetailSheet
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageCodeBadge
import com.example.ui.viewmodel.DictionaryDetailState

private val dictionaryCategories =
  listOf(
    "ALL" to "Toutes",
    "Salutations" to "Salutations",
    "Vie quotidienne" to "Quotidien",
    "Éducation" to "Éducation",
    "Voyage" to "Voyage",
    "Nourriture" to "Nourriture",
    "Famille" to "Famille",
    "Nature" to "Nature",
    "Temps" to "Temps",
    "Santé" to "Santé",
    "Personnalisé" to "Perso",
  )

@Composable
fun DictionaryScreen(
  installedPacks: List<LanguagePackEntity>,
  query: String,
  langFilter: String,
  categoryFilter: String,
  entries: List<DictionaryEntryEntity>,
  selectedDetail: DictionaryDetailState?,
  allFavorites: List<FavoriteEntity>,
  onUpdateQuery: (String) -> Unit,
  onSelectLangFilter: (String) -> Unit,
  onSelectCategoryFilter: (String) -> Unit,
  onOpenEntry: (DictionaryEntryEntity) -> Unit,
  onCloseDetail: () -> Unit,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  onToggleFavorite: (DictionaryEntryEntity) -> Unit,
  onTranslateEntry: (DictionaryEntryEntity) -> Unit,
  onOpenPackManager: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
    ) {
      // Search & Filters Header
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = onUpdateQuery,
          placeholder = { Text("Rechercher un mot, une définition, un exemple…") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { onUpdateQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Effacer la recherche")
              }
            }
          },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().testTag("dictionary_search_input"),
        )

        if (installedPacks.isNotEmpty()) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChip(
                selected = langFilter == "ALL",
                onClick = { onSelectLangFilter("ALL") },
                label = { Text("Toutes (${installedPacks.size})") },
              )
            }
            items(installedPacks, key = { it.code }) { pack ->
              FilterChip(
                selected = langFilter == pack.code,
                onClick = { onSelectLangFilter(pack.code) },
                label = { Text(pack.nameFr) },
              )
            }
          }

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dictionaryCategories, key = { it.first }) { (key, label) ->
              FilterChip(
                selected = categoryFilter == key,
                onClick = { onSelectCategoryFilter(key) },
                label = { Text(label) },
              )
            }
          }
        }
      }

      if (installedPacks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          EmptyStateCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = "Dictionnaire hors ligne vide",
            message =
              "Téléchargez au moins un pack linguistique (Malagasy, Français, Anglais…) pour consulter les mots, définitions et exemples hors connexion.",
            actionLabel = "Télécharger des packs",
            onAction = onOpenPackManager,
          )
        }
      } else if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          EmptyStateCard(
            icon = Icons.Default.Search,
            title = "Aucun résultat trouvé",
            message =
              "Aucun mot ne correspond à votre recherche dans les packs actuellement installés.",
            actionLabel = "Réinitialiser les filtres",
            onAction = {
              onUpdateQuery("")
              onSelectLangFilter("ALL")
              onSelectCategoryFilter("ALL")
            },
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          item {
            Text(
              text = "${entries.size} entrées disponibles hors ligne",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          items(entries, key = { it.id }) { entry ->
            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable { onOpenEntry(entry) }
                  .testTag("dict_entry_${entry.id}"),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              shape = MaterialTheme.shapes.medium,
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  LanguageCodeBadge(badge = entry.languageCode, isInstalled = true)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = entry.term,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    if (entry.phonetic.isNotBlank()) {
                      Text(
                        text = "${entry.phonetic} • ${entry.partOfSpeech}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                      )
                    }
                  }
                  IconButton(onClick = { onSpeak(entry.term, entry.languageCode) }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                      contentDescription = "Prononcer ${entry.term}",
                    )
                  }
                  IconButton(onClick = { onCopy("${entry.term} : ${entry.definition}") }) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copier ${entry.term}",
                    )
                  }
                }
                Text(
                  text = entry.definition,
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                if (entry.example.isNotBlank()) {
                  Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Text(
                      text = "Ex. « ${entry.example} »",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (selectedDetail != null) {
    val isFav =
      allFavorites.any {
        it.type == "WORD" &&
          it.sourceLangCode == selectedDetail.entry.languageCode &&
          it.titleText == selectedDetail.entry.term
      }
    DictionaryDetailSheet(
      detail = selectedDetail,
      isFavorite = isFav,
      onSpeak = onSpeak,
      onCopy = onCopy,
      onToggleFavorite = onToggleFavorite,
      onTranslateInApp = onTranslateEntry,
      onDismiss = onCloseDetail,
    )
  }
}
