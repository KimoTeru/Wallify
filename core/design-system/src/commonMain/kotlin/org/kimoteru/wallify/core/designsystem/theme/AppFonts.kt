package org.kimoteru.wallify.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.plus_jakarta_sans_bold
import org.kimoteru.wallify.core.resources.generated.resources.plus_jakarta_sans_medium
import org.kimoteru.wallify.core.resources.generated.resources.plus_jakarta_sans_regular
import org.kimoteru.wallify.core.resources.generated.resources.plus_jakarta_sans_semibold

/** Plus Jakarta Sans in the four weights the typography uses. Loads from `core:resources`. */
@Composable
fun rememberAppFontFamily(): FontFamily {
    val regular = Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal)
    val medium = Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold)

    return remember(regular, medium, semiBold, bold) {
        FontFamily(regular, medium, semiBold, bold)
    }
}