package org.tensorflow.lite.examples.shravan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import org.tensorflow.lite.examples.shravan.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

// Legible Typography for visually impaired (bold, clear sans-serif)
private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun ShravanTheme(
    themeIndex: Int,
    content: @Composable () -> Unit
) {
    val theme = ThemeCatalog.themes.getOrElse(themeIndex) { ThemeCatalog.themes[3] }
    val colors = theme.colors

    val colorScheme = lightColorScheme(
        primary = colors[2],
        secondary = colors[3],
        tertiary = colors[1],
        background = colors[4],
        surface = colors[4],
        onPrimary = colors[0],
        onSecondary = colors[0],
        onTertiary = colors[0],
        onBackground = colors[0],
        onSurface = colors[0],
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
