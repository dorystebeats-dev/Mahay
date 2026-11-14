package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.local.entity.TranslationHistoryEntity
import com.example.data.pack.PackDownloadProgress
import com.example.ui.components.LanguageCodeBadge
import com.example.ui.components.MahayRahaBrandEmblem
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.BrandTurquoise
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun HomeScreen(
  allPacks: List<LanguagePackEntity>,
  installedPacks: List<LanguagePackEntity>,
  activeDownloads: Map<String, PackDownloadProgress>,
  totalWords: Int,
  favoriteCount: Int,
  recentHistory: List<TranslationHistoryEntity>,
  dictionarySample: List<DictionaryEntryEntity>,
  onNavigateTranslator: () -> Unit,
  onNavigateDictionary: () -> Unit,
  onNavigateSettings: () -> Unit,
  onNavigateHistory: () -> Unit,
  onDownloadPack: (String) -> Unit,
  onOpenTranslation: (TranslationHistoryEntity) -> Unit,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val wordOfTheDay =
    remember(dictionarySample) {
      dictionarySample.firstOrNull { it.languageCode == "mg" }
        ?: dictionarySample.firstOrNull()
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Banner Card
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
            Image(
              painter = painterResource(id = R.drawable.img_hero_banner),
              contentDescription = "Illustration MAHAY RAHA livre ouvert et globe terrestre",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors =
                        listOf(
                          BrandRoyalBlue.copy(alpha = 0.55f),
                          BrandRoyalBlue.copy(alpha = 0.88f),
                        )
                    )
                  )
            )
            Column(
              modifier = Modifier.fillMaxSize().padding(18.dp),
              verticalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                MahayRahaBrandEmblem(size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "MAHAY RAHA",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "Traduire, apprendre et comprendre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                  )
                }
                Surface(
                  color =
                    if (installedPacks.isNotEmpty()) StatusSuccess.copy(alpha = 0.25f)
                    else StatusWarning.copy(alpha = 0.28f),
                  shape = CircleShape,
                  border =
                    BorderStroke(
                      1.dp,
                      if (installedPacks.isNotEmpty()) BrandTurquoise else StatusWarning,
                    ),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                  ) {
                    Icon(
                      imageVector =
                        if (installedPacks.isNotEmpty()) Icons.Default.OfflinePin
                        else Icons.Default.CloudOff,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text =
                        if (installedPacks.isNotEmpty()) "${installedPacks.size} packs"
                        else "0 pack",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Button(
                  onClick = onNavigateTranslator,
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = BrandTurquoise,
                      contentColor = Color(0xFF042F3B),
                    ),
                  modifier = Modifier.weight(1f).testTag("home_translate_cta"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Traduire", fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                  onClick = onNavigateDictionary,
                  colors =
                    ButtonDefaults.filledTonalButtonColors(
                      containerColor = Color.White.copy(alpha = 0.18f),
                      contentColor = Color.White,
                    ),
                  modifier = Modifier.weight(1f).testTag("home_dictionary_cta"),
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Dictionnaire")
                }
              }
            }
          }
        }
      }

      // Honest Offline Readiness Card when fewer than 2 packs installed
      if (installedPacks.size < 2) {
        item {
          val recommendedCodes = listOf("mg", "fr", "en")
          val recommendedPacks =
            allPacks.filter { it.code in recommendedCodes }
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
              ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text =
                      if (installedPacks.isEmpty()) "Activez le mode hors ligne"
                      else "Ajoutez une 2e langue pour traduire",
                    style = MaterialTheme.typography.titleMedium,
                  )
                  Text(
                    text =
                      "Aucun modèle n'est simulé : téléchargez au moins deux packs locaux pour traduire hors ligne.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recommendedPacks, key = { it.code }) { pack ->
                  val download = activeDownloads[pack.code]
                  Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border =
                      BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.width(156.dp),
                  ) {
                    Column(
                      modifier = Modifier.padding(12.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageCodeBadge(badge = pack.regionBadge, isInstalled = pack.isInstalled)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = pack.nameFr,
                          style = MaterialTheme.typography.titleSmall,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                        )
                      }
                      if (download != null) {
                        LinearProgressIndicator(
                          progress = { download.progress },
                          modifier = Modifier.fillMaxWidth().clip(CircleShape),
                        )
                        Text(
                          text = download.stageLabel,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary,
                        )
                      } else if (pack.isInstalled) {
                        Text(
                          text = "Installé (${pack.wordCount} mots)",
                          style = MaterialTheme.typography.labelMedium,
                          color = StatusSuccess,
                        )
                      } else {
                        Button(
                          onClick = { onDownloadPack(pack.code) },
                          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                          modifier = Modifier.fillMaxWidth().testTag("quick_download_${pack.code}"),
                        ) {
                          Text("Télécharger", style = MaterialTheme.typography.labelMedium)
                        }
                      }
                    }
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
              ) {
                TextButton(onClick = onNavigateSettings) {
                  Text("Voir les ${allPacks.size} langues disponibles")
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }
      }

      // Stats Strip
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          StatPillCard(
            label = "Packs locaux",
            value = "${installedPacks.size}/${allPacks.size}",
            accent = BrandBlue,
            onClick = onNavigateSettings,
            modifier = Modifier.weight(1f),
          )
          StatPillCard(
            label = "Mots indexés",
            value = "$totalWords",
            accent = BrandViolet,
            onClick = onNavigateDictionary,
            modifier = Modifier.weight(1f),
          )
          StatPillCard(
            label = "Favoris",
            value = "$favoriteCount",
            accent = BrandTurquoise,
            onClick = {},
            modifier = Modifier.weight(1f),
          )
        }
      }

      // Word of the Day Card (when at least 1 pack is installed)
      if (wordOfTheDay != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = "Mot à découvrir",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                LanguageCodeBadge(badge = wordOfTheDay.languageCode, isInstalled = true)
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = wordOfTheDay.term,
                    style = MaterialTheme.typography.headlineSmall,
                  )
                  if (wordOfTheDay.phonetic.isNotBlank()) {
                    Text(
                      text = "${wordOfTheDay.phonetic} • ${wordOfTheDay.partOfSpeech}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.tertiary,
                    )
                  }
                }
                FilledTonalIconButton(
                  onClick = { onSpeak(wordOfTheDay.term, wordOfTheDay.languageCode) }
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Écouter",
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                FilledTonalIconButton(
                  onClick = { onCopy("${wordOfTheDay.term} : ${wordOfTheDay.definition}") }
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copier",
                  )
                }
              }
              Text(
                text = wordOfTheDay.definition,
                style = MaterialTheme.typography.bodyMedium,
              )
              if (wordOfTheDay.example.isNotBlank()) {
                Surface(
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  shape = MaterialTheme.shapes.small,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "« ${wordOfTheDay.example} »",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium,
                    )
                    if (wordOfTheDay.exampleGloss.isNotBlank()) {
                      Text(
                        text = wordOfTheDay.exampleGloss,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Recent Translations
      if (recentHistory.isNotEmpty()) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Traductions récentes",
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onNavigateHistory) {
              Text("Tout voir")
            }
          }
        }
        items(recentHistory.take(3), key = { it.id }) { item ->
          Card(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onOpenTranslation(item) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            shape = MaterialTheme.shapes.medium,
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                LanguageCodeBadge(badge = item.sourceLangCode, isInstalled = true)
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LanguageCodeBadge(badge = item.targetLangCode, isInstalled = true)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                  text = "${item.coveragePercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.tertiary,
                )
              }
              Text(
                text = item.sourceText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = item.translatedText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }

      // Quick Manage Packs Footer Card
      item {
        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(MaterialTheme.shapes.medium)
              .clickable(onClick = onNavigateSettings),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
          shape = MaterialTheme.shapes.medium,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Gestionnaire de packs & Paramètres",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = "Téléchargez de nouvelles langues, ajoutez vos mots ou réglez la voix.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatPillCard(
  label: String,
  value: String,
  accent: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        color = accent,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
