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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.TranslationHistoryEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageCodeBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  query: String,
  items: List<TranslationHistoryEntity>,
  allFavorites: List<FavoriteEntity>,
  onUpdateQuery: (String) -> Unit,
  onOpenInTranslator: (TranslationHistoryEntity) -> Unit,
  onToggleFavorite: (TranslationHistoryEntity) -> Unit,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  onDeleteItem: (Long) -> Unit,
  onClearAll: () -> Unit,
  onNavigateTranslator: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showClearDialog by remember { mutableStateOf(false) }
  val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = onUpdateQuery,
          placeholder = { Text("Filtrer l'historique…") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { onUpdateQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Effacer")
              }
            }
          },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.weight(1f).testTag("history_search_input"),
        )
        if (items.isNotEmpty()) {
          IconButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.testTag("clear_history_button"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Tout effacer",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          EmptyStateCard(
            icon = Icons.Default.History,
            title = if (query.isBlank()) "Aucune traduction enregistrée" else "Aucun résultat",
            message =
              if (query.isBlank()) {
                "Vos traductions hors ligne apparaîtront automatiquement ici pour un accès rapide."
              } else {
                "Aucune entrée de l'historique ne correspond à « $query »."
              },
            actionLabel = if (query.isBlank()) "Ouvrir le Traducteur" else null,
            onAction = if (query.isBlank()) onNavigateTranslator else null,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(items, key = { it.id }) { item ->
            val isFav =
              allFavorites.any {
                it.type == "TRANSLATION" &&
                  it.sourceLangCode == item.sourceLangCode &&
                  it.targetLangCode == item.targetLangCode &&
                  it.titleText == item.sourceText &&
                  it.subtitleText == item.translatedText
              }

            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable { onOpenInTranslator(item) },
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
                  LanguageCodeBadge(badge = item.sourceLangCode, isInstalled = true)
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  LanguageCodeBadge(badge = item.targetLangCode, isInstalled = true)
                  Spacer(modifier = Modifier.weight(1f))
                  Text(
                    text = dateFormatter.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                Text(
                  text = item.sourceText,
                  style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                  text = item.translatedText,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold,
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                ) {
                  IconButton(onClick = { onSpeak(item.translatedText, item.targetLangCode) }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                      contentDescription = "Prononcer",
                    )
                  }
                  IconButton(onClick = { onCopy(item.translatedText) }) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copier",
                    )
                  }
                  IconButton(onClick = { onToggleFavorite(item) }) {
                    Icon(
                      imageVector =
                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                      contentDescription = "Favori",
                      tint =
                        if (isFav) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  IconButton(onClick = { onDeleteItem(item.id) }) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Supprimer",
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

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("Effacer l'historique ?") },
      text = { Text("Toutes les traductions enregistrées localement seront supprimées.") },
      confirmButton = {
        TextButton(
          onClick = {
            onClearAll()
            showClearDialog = false
          }
        ) {
          Text("Effacer", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) { Text("Annuler") }
      },
    )
  }
}
