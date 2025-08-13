package com.example.booknest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.booknest.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val RobotoCondensed = FontFamily(
    Font(R.font.roboto_condensed_regular),
    Font(R.font.roboto_condensed_thin_italic, FontWeight.Thin, FontStyle.Italic),

)
val RobotoCondensedBold = FontFamily(
    Font(R.font.roboto_condensed_bold, FontWeight.Bold)
)
val WorkSans = FontFamily(
    Font(R.font.worksans_regular)
)
val WorkSansBold = FontFamily(
    Font(
        R.font.worksans_bold
    )
)