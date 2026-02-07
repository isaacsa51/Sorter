package com.serranoie.app.media.sorter.presentation.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.serranoie.app.media.sorter.R

val RobotoFlex = FontFamily(
    Font(R.font.roboto_flex, FontWeight.Normal),
    Font(R.font.roboto_flex, FontWeight.Medium),
    Font(R.font.roboto_flex, FontWeight.SemiBold),
    Font(R.font.roboto_flex, FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplayLargeEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(155f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplayMediumEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(155f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplaySmallEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(155f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexHeadlineLargeEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(800),
            FontVariation.width(150f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexHeadlineMediumEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(150f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexHeadlineSmallEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(135f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexTitleLargeEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(135f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexTitleMediumEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(135f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexTitleSmallEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(135f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexBodyLargeEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.width(115f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexBodyMediumEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.width(115f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexBodySmallEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.width(115f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexLabelLargeEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(125f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexLabelMediumEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(125f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexLabelSmallEmphasized = FontFamily(
    Font(
        R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(125f)
        )
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    titleLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    bodyLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    labelLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun Typography.withEmphasizedStyles(): Typography {
    return this.copy(
        displayLargeEmphasized = TextStyle(
            fontFamily = RobotoFlexDisplayLargeEmphasized,
            fontSize = 64.sp,
            lineHeight = 72.sp,
            letterSpacing = 0.sp
        ),
        displayMediumEmphasized = TextStyle(
            fontFamily = RobotoFlexDisplayMediumEmphasized,
            fontSize = 52.sp,
            lineHeight = 60.sp,
            letterSpacing = 0.sp
        ),
        displaySmallEmphasized = TextStyle(
            fontFamily = RobotoFlexDisplaySmallEmphasized,
            fontSize = 44.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        
        // Emphasized Headline - Bold and wide for attention-grabbing headers
        headlineLargeEmphasized = TextStyle(
            fontFamily = RobotoFlexHeadlineLargeEmphasized,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineMediumEmphasized = TextStyle(
            fontFamily = RobotoFlexHeadlineMediumEmphasized,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineSmallEmphasized = TextStyle(
            fontFamily = RobotoFlexHeadlineSmallEmphasized,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        titleLargeEmphasized = TextStyle(
            fontFamily = RobotoFlexTitleLargeEmphasized,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.15.sp
        ),
        titleMediumEmphasized = TextStyle(
            fontFamily = RobotoFlexTitleMediumEmphasized,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.2.sp
        ),
        titleSmallEmphasized = TextStyle(
            fontFamily = RobotoFlexTitleSmallEmphasized,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyLargeEmphasized = TextStyle(
            fontFamily = RobotoFlexBodyLargeEmphasized,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.6.sp
        ),
        bodyMediumEmphasized = TextStyle(
            fontFamily = RobotoFlexBodyMediumEmphasized,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.4.sp
        ),
        bodySmallEmphasized = TextStyle(
            fontFamily = RobotoFlexBodySmallEmphasized,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelLargeEmphasized = TextStyle(
            fontFamily = RobotoFlexLabelLargeEmphasized,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        labelMediumEmphasized = TextStyle(
            fontFamily = RobotoFlexLabelMediumEmphasized,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.6.sp
        ),
        labelSmallEmphasized = TextStyle(
            fontFamily = RobotoFlexLabelSmallEmphasized,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.6.sp
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ExpressiveTypography = Typography.withEmphasizedStyles()
