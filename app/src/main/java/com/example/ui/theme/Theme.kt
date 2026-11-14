package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MahayRahaLightColorScheme =
  lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandRoyalBlue,
    secondary = BrandViolet,
    onSecondary = BrandWhite,
    secondaryContainer = BrandVioletLight,
    onSecondaryContainer = BrandVioletDeep,
    tertiary = BrandTurquoiseDeep,
    onTertiary = BrandWhite,
    tertiaryContainer = BrandTurquoiseLight,
    onTertiaryContainer = Color(0xFF083344),
    background = BrandSurfaceLight,
    onBackground = BrandTextPrimaryLight,
    surface = BrandWhite,
    onSurface = BrandTextPrimaryLight,
    surfaceVariant = BrandSurfaceVariantLight,
    onSurfaceVariant = BrandTextSecondaryLight,
    outline = BrandOutlineLight,
    error = StatusError,
    onError = BrandWhite,
  )

private val MahayRahaDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1F4B),
    primaryContainer = BrandBlueContainerDark,
    onPrimaryContainer = BrandBlueLight,
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF281054),
    secondaryContainer = BrandVioletContainerDark,
    onSecondaryContainer = BrandVioletLight,
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF042F3B),
    tertiaryContainer = BrandTurquoiseContainerDark,
    onTertiaryContainer = BrandTurquoiseLight,
    background = BrandBackgroundDark,
    onBackground = BrandTextPrimaryDark,
    surface = BrandSurfaceDark,
    onSurface = BrandTextPrimaryDark,
    surfaceVariant = BrandSurfaceVariantDark,
    onSurfaceVariant = BrandTextSecondaryDark,
    outline = BrandOutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
  )

val MahayRahaShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun MahayRahaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MahayRahaDarkColorScheme else MahayRahaLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = MahayRahaTypography,
    shapes = MahayRahaShapes,
    content = content,
  )
}
