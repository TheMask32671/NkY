package br.com.ariane.controle.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Vinho = Color(0xFF7B2C43)
val VinhoEscuro = Color(0xFF5C1F31)
val VinhoClaro = Color(0xFF9D4760)
val RosaForte = Color(0xFFE0A0B4)
val RosaMedio = Color(0xFFEFC7D4)
val Rosa = Color(0xFFF7DEE6)
val RosaClaro = Color(0xFFFDF2F6)
val FundoClaro = Color(0xFFFFF9FB)
val Dourado = Color(0xFFC9A227)
val DouradoClaro = Color(0xFFE8CE8E)
val Verde = Color(0xFF2E7D5B)
val VerdeClaro = Color(0xFFE3F2EA)
val Vermelho = Color(0xFFB3261E)
val VermelhoClaro = Color(0xFFFCE8E6)
val TextoEscuro = Color(0xFF3A2A2F)
val TextoSuave = Color(0xFF86686F)

private val EsquemaDeCores = lightColorScheme(
    primary = Vinho,
    onPrimary = Color.White,
    primaryContainer = Rosa,
    onPrimaryContainer = VinhoEscuro,
    secondary = Dourado,
    onSecondary = Color.White,
    secondaryContainer = DouradoClaro,
    onSecondaryContainer = Color(0xFF4A3B00),
    tertiary = VinhoClaro,
    onTertiary = Color.White,
    tertiaryContainer = RosaClaro,
    onTertiaryContainer = VinhoEscuro,
    background = FundoClaro,
    onBackground = TextoEscuro,
    surface = Color.White,
    onSurface = TextoEscuro,
    surfaceVariant = RosaClaro,
    onSurfaceVariant = TextoSuave,
    outline = RosaForte,
    outlineVariant = RosaMedio,
    error = Vermelho,
    onError = Color.White,
    errorContainer = VermelhoClaro,
    onErrorContainer = Vermelho
)

private val TipografiaAriane = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp)
)

@Composable
fun ArianeTema(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDeCores,
        typography = TipografiaAriane,
        content = conteudo
    )
}
