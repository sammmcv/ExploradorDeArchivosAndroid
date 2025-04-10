package com.escom.practica3.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores IPN (Guinda)
private val IpnDarkColorScheme = darkColorScheme(
    primary = IpnPrimaryDark,
    secondary = IpnSecondaryDark,
    tertiary = IpnSecondaryLight,
    background = IpnBackgroundDark,
    surface = IpnBackgroundDark
)

private val IpnLightColorScheme = lightColorScheme(
    primary = IpnPrimaryLight,
    secondary = IpnSecondaryLight,
    tertiary = IpnPrimaryDark,
    background = IpnBackgroundLight,
    surface = IpnBackgroundLight
)

// Esquema de colores ESCOM (Azul)
private val EscomDarkColorScheme = darkColorScheme(
    primary = EscomPrimaryDark,
    secondary = EscomSecondaryDark,
    tertiary = EscomSecondaryLight,
    background = EscomBackgroundDark,
    surface = EscomBackgroundDark
)

private val EscomLightColorScheme = lightColorScheme(
    primary = EscomPrimaryLight,
    secondary = EscomSecondaryLight,
    tertiary = EscomPrimaryDark,
    background = EscomBackgroundLight,
    surface = EscomBackgroundLight
)

// Esquemas de colores originales
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

enum class ThemeType {
    DEFAULT, IPN, ESCOM
}

@Composable
fun Practica3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    themeType: ThemeType = ThemeType.IPN, // Por defecto usamos el tema IPN
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeType == ThemeType.IPN -> {
            if (darkTheme) IpnDarkColorScheme else IpnLightColorScheme
        }
        themeType == ThemeType.ESCOM -> {
            if (darkTheme) EscomDarkColorScheme else EscomLightColorScheme
        }
        else -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Set status bar color (with deprecation warning suppression)
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            
            // Use the WindowCompat API for controlling the appearance
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}