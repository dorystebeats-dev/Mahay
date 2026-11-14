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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.FavoriteEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageCodeBadge

private val favoriteFilters =
  listOf(
    "ALL" to "Tous",
    "TRANSLATION" to "Traductions",
    "WORD" to "Mots",
  )

@Composable
fun FavoritesScreen(
  filterType: String,
  favorites: List<FavoriteEntity>,
  onSelectFilter: (String) -> Unit,
  onOpenFavoriteInTranslator: (FavoriteEntity) -> Unit,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  onRemoveFavorite: (Long) -> Unit,
  onNavigateDictionary: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)) {
      LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(favoriteFilters, key = { it.first }) { (key, label) ->
          FilterChip(
            selected = filterType == key,
            onClick = { onSelectFilter(key) },
            label = { Text(label) },
          )
        }
      }

      if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          EmptyStateCard(
            icon = Icons.Default.Favorite,
            title = "Aucun favori enregistré",
            message =
              "Appuyez sur l'icône cœur dans le Traducteur ou le Dictionnaire pour garder vos mots et phrases essentiels hors ligne.",
            actionLabel = "Explorer le Dictionnaire",
            onAction = onNavigateDictionary,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(favorites, key = { it.id }) { fav ->
            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable { onOpenFavoriteInTranslator(fav) },
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              shape = MaterialTheme.shapes.medium,
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  LanguageCodeBadge(badge = fav.sourceLangCode, isInstalled = true)
                  if (fav.type == "TRANSLATION") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LanguageCodeBadge(badge = fav.targetLangCode, isInstalled = true)
                  }
                  Spacer(modifier = Modifier.weight(1f))
                  Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                  ) {
                    Text(
                      text = if (fav.type == "TRANSLATION") "Traduction" else "Dictionnaire",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                  }
                }

                Text(
                  text = fav.titleText,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = fav.subtitleText,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                    if (fav.type == "TRANSLATION") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (fav.detailNote.isNotBlank()) {
                  Text(
                    text = fav.detailNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                ) {
                  val speakText =
                    if (fav.type == "TRANSLATION") fav.subtitleText else fav.titleText
                  val speakLang =
                    if (fav.type == "TRANSLATION") fav.targetLangCode else fav.sourceLangCode
                  IconButton(onClick = { onSpeak(speakText, speakLang) }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                      contentDescription = "Prononcer",
                    )
                  }
                  IconButton(onClick = { onCopy("${fav.titleText} — ${fav.subtitleText}") }) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copier",
                    )
                  }
                  IconButton(onClick = { onRemoveFavorite(fav.id) }) {
                    Icon(
                      imageVector = Icons.Default.Favorite,
                      contentDescription = "Retirer des favoris",
                      tint = MaterialTheme.colorScheme.secondary,
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
}
