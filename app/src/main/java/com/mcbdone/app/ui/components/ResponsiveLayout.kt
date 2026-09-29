package com.mcbdone.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Universal Responsive Container for all screen sizes.
 * Automatically adapts between:
 * - Compact (Phones): Full width with standard padding.
 * - Medium (Foldables, Landscape, Small Tablets): Elegantly centered with comfortable reading max-width.
 * - Expanded (Tablets, Large Screens, DeX): Centered hero column or multi-column grid layouts.
 */
@Composable
fun ResponsiveScreenContainer(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 760.dp,
    content: @Composable BoxScope.(isTablet: Boolean, isExpanded: Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTablet = maxWidth >= 600.dp
        val isExpanded = maxWidth >= 840.dp

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = maxContentWidth)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            content(isTablet, isExpanded)
        }
    }
}
