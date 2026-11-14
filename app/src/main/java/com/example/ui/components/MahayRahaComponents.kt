package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.pack.PackDownloadProgress
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandRoyalBlue
import com.example.ui.theme.BrandTurquoise
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.DictionaryDetailState

@Composable
fun MahayRahaBrandEmblem(
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
) {
  Box(
    modifier =
      modifier
        .size(size)
        .clip(RoundedCornerShape(size * 0.28f))
        .background(
          Brush.linearGradient(
            colors = listOf(BrandRoyalBlue, BrandViolet, BrandTurquoise),
          )
        ),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(size * 0.68f)) {
      val w = this.size.width
      val h = this.size.height

      // Open book pages at bottom
      val bookPath =
        Path().apply {
          moveTo(w * 0.08f, h * 0.42f)
          quadraticTo(w * 0.30f, h * 0.36f, w * 0.50f, h * 0.48f)
          quadraticTo(w * 0.70f, h * 0.36f, w * 0.92f, h * 0.42f)
          lineTo(w * 0.92f, h * 0.84f)
          quadraticTo(w * 0.70f, h * 0.78f, w * 0.50f, h * 0.88f)
          quadraticTo(w * 0.30f, h * 0.78f, w * 0.08f, h * 0.84f)
          close()
        }
      drawPath(bookPath, color = Color.White.copy(alpha = 0.95f))

      // Spine line
      drawLine(
        color = BrandRoyalBlue,
        start = Offset(w * 0.50f, h * 0.48f),
        end = Offset(w * 0.50f, h * 0.88f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round,
      )

      // Terrestrial globe floating above the open book
      val globeRadius = w * 0.26f
      val globeCenter = Offset(w * 0.50f, h * 0.33f)
      drawCircle(
        brush = Brush.radialGradient(listOf(BrandTurquoise, BrandBlue)),
        radius = globeRadius,
        center = globeCenter,
      )
      drawCircle(
        color = Color.White,
        radius = globeRadius,
        center = globeCenter,
        style = Stroke(width = w * 0.045f),
      )
      // Meridian + Equator lines
      drawOval(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(globeCenter.x - globeRadius * 0.48f, globeCenter.y - globeRadius),
        size = Size(globeRadius * 0.96f, globeRadius * 2f),
        style = Stroke(width = w * 0.03f),
      )
      drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(globeCenter.x - globeRadius, globeCenter.y),
        end = Offset(globeCenter.x + globeRadius, globeCenter.y),
        strokeWidth = w * 0.03f,
      )
    }
  }
}

@Composable
fun LanguageCodeBadge(
  badge: String,
  isInstalled: Boolean,
  modifier: Modifier = Modifier,
) {
  val bg =
    if (isInstalled) {
      Brush.linearGradient(listOf(BrandBlue, BrandTurquoise))
    } else {
      Brush.linearGradient(
        listOf(
          MaterialTheme.colorScheme.surfaceVariant,
          MaterialTheme.colorScheme.surfaceVariant,
        )
      )
    }
  val textColor =
    if (isInstalled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .background(bg)
        .padding(horizontal = 10.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = badge.uppercase(),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = textColor,
    )
  }
}

@Composable
fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  message: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
      ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(
                  BrandBlue.copy(alpha = 0.14f),
                  BrandViolet.copy(alpha = 0.14f),
                  BrandTurquoise.copy(alpha = 0.14f),
                )
              )
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(30.dp),
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
          Text(actionLabel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorSheet(
  title: String,
  packs: List<LanguagePackEntity>,
  selectedCode: String,
  activeDownloads: Map<String, PackDownloadProgress>,
  onSelect: (String) -> Unit,
  onDownloadPack: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Language,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = title,
          style = MaterialTheme.typography.headlineSmall,
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Les langues marquées « Hors ligne » sont prêtes sans connexion.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(14.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        items(packs, key = { it.code }) { pack ->
          val download = activeDownloads[pack.code]
          val isSelected = pack.code == selectedCode
          Surface(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                  onSelect(pack.code)
                  onDismiss()
                },
            color =
              if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border =
              if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
            shape = MaterialTheme.shapes.medium,
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
              ) {
                LanguageCodeBadge(badge = pack.regionBadge, isInstalled = pack.isInstalled)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = pack.nameFr,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "${pack.nativeName} • ${pack.wordCount} entrées",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (pack.isInstalled) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Pack installé",
                      tint = StatusSuccess,
                      modifier = Modifier.size(18.dp),
                    )
                    Text(
                      text = "Hors ligne",
                      style = MaterialTheme.typography.labelMedium,
                      color = StatusSuccess,
                    )
                  }
                } else if (download == null) {
                  FilledTonalIconButton(
                    onClick = { onDownloadPack(pack.code) },
                  ) {
                    Icon(
                      imageVector = Icons.Default.CloudDownload,
                      contentDescription = "Télécharger ${pack.nameFr}",
                    )
                  }
                }
              }
              if (download != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { download.progress },
                  modifier = Modifier.fillMaxWidth().clip(CircleShape),
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryDetailSheet(
  detail: DictionaryDetailState,
  isFavorite: Boolean,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  onToggleFavorite: (DictionaryEntryEntity) -> Unit,
  onTranslateInApp: (DictionaryEntryEntity) -> Unit,
  onDismiss: () -> Unit,
) {
  val entry = detail.entry
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LanguageCodeBadge(badge = entry.languageCode, isInstalled = true)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = "${entry.partOfSpeech} • ${entry.category}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        FilledTonalIconButton(
          onClick = { onToggleFavorite(entry) },
          colors =
            IconButtonDefaults.filledTonalIconButtonColors(
              containerColor =
                if (isFavorite) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favori",
            tint =
              if (isFavorite) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = entry.term,
          style = MaterialTheme.typography.headlineMedium,
        )
        if (entry.phonetic.isNotBlank()) {
          Text(
            text = entry.phonetic,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = { onSpeak(entry.term, entry.languageCode) },
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Écouter")
        }
        Button(
          onClick = { onCopy("${entry.term} — ${entry.definition}") },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copier")
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "Définition",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = entry.definition,
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }

      if (entry.example.isNotBlank()) {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Exemple d'utilisation",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
              text = "« ${entry.example} »",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
            )
            if (entry.exampleGloss.isNotBlank()) {
              Text(
                text = entry.exampleGloss,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      val otherEquivalents = detail.equivalents.filter { it.languageCode != entry.languageCode }
      if (otherEquivalents.isNotEmpty()) {
        HorizontalDivider()
        Text(
          text = "Équivalents dans vos packs installés",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth().height(150.dp),
        ) {
          items(otherEquivalents, key = { "${it.languageCode}_${it.id}" }) { eq ->
            Surface(
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              shape = MaterialTheme.shapes.small,
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                LanguageCodeBadge(badge = eq.languageCode, isInstalled = true)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = eq.term, style = MaterialTheme.typography.titleSmall)
                  if (eq.phonetic.isNotBlank()) {
                    Text(
                      text = eq.phonetic,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                FilledTonalIconButton(onClick = { onSpeak(eq.term, eq.languageCode) }) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Prononcer ${eq.term}",
                  )
                }
              }
            }
          }
        }
      }

      Button(
        onClick = {
          onTranslateInApp(entry)
          onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(
          imageVector = Icons.Default.Translate,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Ouvrir dans le Traducteur")
      }
    }
  }
}

fun formatBytesLabel(bytes: Long): String {
  return when {
    bytes <= 0L -> "~20 Ko"
    bytes < 1024L -> "$bytes o"
    bytes < 1024L * 1024L -> "${(bytes / 1024.0).toInt().coerceAtLeast(1)} Ko"
    else -> String.format("%.1f Mo", bytes / (1024.0 * 1024.0))
  }
}
